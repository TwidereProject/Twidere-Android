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
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bluelinelabs.logansquare.LoganSquare;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.JsonSerializer;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.TwitterContentUtils;
import org.mariotaku.twidere.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class TwitterAPIStatusesLoader extends ParcelableStatusesLoader {

    private final Context mContext;
    private final long mAccountId;
    private final long mMaxId, mSinceId;
    private final Object[] mSavedStatusesFileArgs;
    private Comparator<ParcelableStatus> mComparator;

    public TwitterAPIStatusesLoader(final Context context, final long accountId, final long sinceId,
                                    final long maxId, final List<ParcelableStatus> data,
                                    final String[] savedStatusesArgs, final int tabPosition, boolean fromUser) {
        super(context, data, tabPosition, fromUser);
        mContext = context;
        mAccountId = accountId;
        mMaxId = maxId;
        mSinceId = sinceId;
        mSavedStatusesFileArgs = savedStatusesArgs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final ListResponse<ParcelableStatus> loadInBackground() {
        final File serializationFile = getSerializationFile();
        List<ParcelableStatus> data = getData();
        if (data == null) {
            data = new CopyOnWriteArrayList<>();
        }
        if (isFirstLoad() && getTabPosition() >= 0 && serializationFile != null) {
            final List<ParcelableStatus> cached = getCachedData(serializationFile);
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
        final Twitter twitter = getTwitter();
        if (twitter == null) return null;
        final List<Status> statuses;
        final boolean truncated;
        final Context context = getContext();
        final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final int loadItemLimit = prefs.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
        final boolean noItemsBefore = data.isEmpty();
        try {
            final Paging paging = new Paging();
            paging.setCount(loadItemLimit);
            if (mMaxId > 0) {
                paging.setMaxId(mMaxId);
            }
            if (mSinceId > 0) {
                paging.setSinceId(mSinceId - 1);
            }
            statuses = new ArrayList<>();
            truncated = Utils.truncateStatuses(getStatuses(twitter, paging), statuses, mSinceId);
            if (!Utils.isOfficialTwitterInstance(context, twitter)) {
                TwitterContentUtils.getStatusesWithQuoteData(twitter, statuses);
            }
        } catch (final TwitterException e) {
            // mHandler.post(new ShowErrorRunnable(e));
            Log.w(LOGTAG, e);
            return ListResponse.getListInstance(new CopyOnWriteArrayList<>(data), e);
        }

        final long[] statusIds = new long[statuses.size()];
        long minId = -1;
        int minIdx = -1;
        int rowsDeleted = 0;
        for (int i = 0, j = statuses.size(); i < j; i++) {
            final Status status = statuses.get(i);
            final long id = status.getId();
            if (minId == -1 || id < minId) {
                minId = id;
                minIdx = i;
            }
            statusIds[i] = id;

            if (deleteStatus(data, status.getId())) {
                rowsDeleted++;
            }
        }

        // Insert a gap.
        final boolean deletedOldGap = rowsDeleted > 0 && ArrayUtils.contains(statusIds, mMaxId);
        final boolean noRowsDeleted = rowsDeleted == 0;
        final boolean insertGap = minId > 0 && (noRowsDeleted || deletedOldGap) && !truncated
                && !noItemsBefore && statuses.size() > 1;
        for (int i = 0, j = statuses.size(); i < j; i++) {
            final Status status = statuses.get(i);
            data.add(new ParcelableStatus(status, mAccountId, insertGap && isGapEnabled() && minIdx == i));
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
        saveCachedData(serializationFile, data);
        return ListResponse.getListInstance(new CopyOnWriteArrayList<>(data));
    }

    public final void setComparator(Comparator<ParcelableStatus> comparator) {
        mComparator = comparator;
    }


    @NonNull
    protected abstract List<Status> getStatuses(@NonNull Twitter twitter, Paging paging) throws TwitterException;

    @Nullable
    protected final Twitter getTwitter() {
        return TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, true, true);
    }

    protected abstract boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status);

    protected boolean isGapEnabled() {
        return true;
    }

    private List<ParcelableStatus> getCachedData(final File file) {
        if (file == null) return null;
        try {
            return JsonSerializer.parseList(file, ParcelableStatus.class);
        } catch (final IOException e) {
            Log.w(LOGTAG, e);
        }
        return null;
    }

    private File getSerializationFile() {
        if (mSavedStatusesFileArgs == null) return null;
        try {
            return JsonSerializer.getSerializationFile(mContext, mSavedStatusesFileArgs);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveCachedData(final File file, final List<ParcelableStatus> data) {
        if (file == null || data == null) return;
        final SharedPreferences prefs = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final int databaseItemLimit = prefs.getInt(KEY_DATABASE_ITEM_LIMIT, DEFAULT_DATABASE_ITEM_LIMIT);
        try {
            final List<ParcelableStatus> statuses = data.subList(0, Math.min(databaseItemLimit, data.size()));
            LoganSquare.serialize(statuses, new FileOutputStream(file), ParcelableStatus.class);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
