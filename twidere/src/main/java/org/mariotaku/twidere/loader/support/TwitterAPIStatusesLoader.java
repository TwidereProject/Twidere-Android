/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.loader.support;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.utils.IoUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.model.util.ParcelableStatusUtils;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.JsonSerializer;
import org.mariotaku.twidere.util.LoganSquareMapperFinder;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwidereArrayUtils;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

public abstract class TwitterAPIStatusesLoader extends ParcelableStatusesLoader {

    private final UserKey mAccountKey;
    private final String mMaxId, mSinceId;
    @Nullable
    private final Object[] mSavedStatusesFileArgs;
    private final boolean mLoadingMore;
    // Statuses sorted descending by default
    private Comparator<ParcelableStatus> mComparator = ParcelableStatus.REVERSE_COMPARATOR;

    @Inject
    protected DiskCache mFileCache;
    @Inject
    protected SharedPreferencesWrapper mPreferences;

    public TwitterAPIStatusesLoader(@NonNull final Context context,
                                    @Nullable final UserKey accountKey,
                                    final String sinceId, final String maxId,
                                    @Nullable final List<ParcelableStatus> data,
                                    @Nullable final String[] savedStatusesArgs,
                                    final int tabPosition, final boolean fromUser, boolean loadingMore) {
        super(context, data, tabPosition, fromUser);
        GeneralComponentHelper.build(context).inject(this);
        mAccountKey = accountKey;
        mMaxId = maxId;
        mSinceId = sinceId;
        mSavedStatusesFileArgs = savedStatusesArgs;
        mLoadingMore = loadingMore;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final ListResponse<ParcelableStatus> loadInBackground() {
        final Context context = getContext();
        if (mAccountKey == null) {
            return ListResponse.getListInstance(new TwitterException("No Account"));
        }
        final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(context,
                mAccountKey);
        if (credentials == null) {
            return ListResponse.getListInstance(new TwitterException("No Account"));
        }

        List<ParcelableStatus> data = getData();
        if (data == null) {
            data = new CopyOnWriteArrayList<>();
        }
        if (isFirstLoad() && getTabPosition() >= 0) {
            final List<ParcelableStatus> cached = getCachedData();
            if (cached != null) {
                data.addAll(cached);
                if (mComparator != null) {
                    Collections.sort(data, mComparator);
                } else {
                    Collections.sort(data);
                }
                return ListResponse.getListInstance(new CopyOnWriteArrayList<>(data));
            }
        }
        if (!isFromUser()) return ListResponse.getListInstance(data);
        final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, credentials, true,
                true);
        if (twitter == null) {
            return ListResponse.getListInstance(new TwitterException("No Account"));
        }
        final List<? extends Status> statuses;
        final boolean noItemsBefore = data.isEmpty();
        final int loadItemLimit = mPreferences.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
        try {
            final Paging paging = new Paging();
            processPaging(credentials, loadItemLimit, paging);
            statuses = getStatuses(twitter, credentials, paging);
            if (!Utils.isOfficialCredentials(context, credentials)) {
                InternalTwitterContentUtils.getStatusesWithQuoteData(twitter, statuses);
            }
        } catch (final TwitterException e) {
            // mHandler.post(new ShowErrorRunnable(e));
            if (BuildConfig.DEBUG) {
                Log.w(LOGTAG, e);
            }
            return ListResponse.getListInstance(new CopyOnWriteArrayList<>(data), e);
        }

        final String[] statusIds = new String[statuses.size()];
        int minIdx = -1;
        int rowsDeleted = 0;
        for (int i = 0, j = statuses.size(); i < j; i++) {
            final Status status = statuses.get(i);
            if (minIdx == -1 || status.compareTo(statuses.get(minIdx)) < 0) {
                minIdx = i;
            }
            statusIds[i] = status.getId();
            if (deleteStatus(data, status.getId())) {
                rowsDeleted++;
            }
        }

        // Insert a gap.
        final boolean deletedOldGap = rowsDeleted > 0 && ArrayUtils.contains(statusIds, mMaxId);
        final boolean noRowsDeleted = rowsDeleted == 0;
        final boolean insertGap = minIdx != -1 && (noRowsDeleted || deletedOldGap) && !noItemsBefore
                && statuses.size() >= loadItemLimit && !mLoadingMore;
        for (int i = 0, j = statuses.size(); i < j; i++) {
            final Status status = statuses.get(i);
            final ParcelableStatus item = ParcelableStatusUtils.fromStatus(status, mAccountKey,
                    insertGap && isGapEnabled() && minIdx == i);
            item.account_color = credentials.color;
            data.add(item);
        }

        final SQLiteDatabase db = TwidereApplication.getInstance(context).getSQLiteDatabase();
        final ParcelableStatus[] array = data.toArray(new ParcelableStatus[data.size()]);
        for (int i = 0, size = array.length; i < size; i++) {
            final ParcelableStatus status = array[i];
            if (shouldFilterStatus(db, status) && !status.is_gap && i != size - 1) {
                deleteStatus(data, status.id);
            }
        }
        if (mComparator != null) {
            Collections.sort(data, mComparator);
        } else {
            Collections.sort(data);
        }
        saveCachedData(data);
        return ListResponse.getListInstance(new CopyOnWriteArrayList<>(data));
    }

    public final void setComparator(Comparator<ParcelableStatus> comparator) {
        mComparator = comparator;
    }

    public String getSinceId() {
        return mSinceId;
    }

    public String getMaxId() {
        return mMaxId;
    }

    public UserKey getAccountKey() {
        return mAccountKey;
    }

    @NonNull
    protected abstract List<? extends Status> getStatuses(@NonNull Twitter twitter,
                                                @NonNull ParcelableCredentials credentials,
                                                @NonNull Paging paging) throws TwitterException;

    @WorkerThread
    protected abstract boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status);

    protected void processPaging(@NonNull ParcelableCredentials credentials, int loadItemLimit, @NonNull final Paging paging) {


        paging.setCount(loadItemLimit);
        if (mMaxId != null) {
            paging.setMaxId(mMaxId);
        }
        if (mSinceId != null) {
            paging.setSinceId(mSinceId);
            if (mMaxId == null) {
                paging.setLatestResults(true);
            }
        }
    }

    protected boolean isGapEnabled() {
        return true;
    }

    private List<ParcelableStatus> getCachedData() {
        final String key = getSerializationKey();
        if (key == null) return null;
        final File file = mFileCache.get(key);
        if (file == null) return null;
        return JsonSerializer.parseList(file, ParcelableStatus.class);
    }

    private String getSerializationKey() {
        if (mSavedStatusesFileArgs == null) return null;
        return TwidereArrayUtils.toString(mSavedStatusesFileArgs, '_', false);
    }

    private static final ExecutorService pool = Executors.newSingleThreadExecutor();

    private void saveCachedData(final List<ParcelableStatus> data) {
        final String key = getSerializationKey();
        if (key == null || data == null) return;
        final int databaseItemLimit = mPreferences.getInt(KEY_DATABASE_ITEM_LIMIT, DEFAULT_DATABASE_ITEM_LIMIT);
        try {
            final List<ParcelableStatus> statuses = data.subList(0, Math.min(databaseItemLimit, data.size()));
            final PipedOutputStream pos = new PipedOutputStream();
            final PipedInputStream pis = new PipedInputStream(pos);
            final Future<Object> future = pool.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    LoganSquareMapperFinder.mapperFor(ParcelableStatus.class).serialize(statuses, pos);
                    return null;
                }
            });
            final boolean saved = mFileCache.save(key, pis, new IoUtils.CopyListener() {
                @Override
                public boolean onBytesCopied(int current, int total) {
                    return !future.isDone();
                }
            });
            if (BuildConfig.DEBUG) {
                Log.v(LOGTAG, key + " saved: " + saved);
            }
        } catch (final Exception e) {
            // Ignore
            if (BuildConfig.DEBUG && !(e instanceof IOException)) {
                Log.w(LOGTAG, e);
            }
        }
    }

}
