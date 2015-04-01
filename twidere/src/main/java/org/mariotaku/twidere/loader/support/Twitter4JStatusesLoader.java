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

import org.mariotaku.jsonserializer.JSONFileIO;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableStatus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.truncateStatuses;

public abstract class Twitter4JStatusesLoader extends ParcelableStatusesLoader {

    private final Context mContext;
    private final long mAccountId;
    private final long mMaxId, mSinceId;
    private final SQLiteDatabase mDatabase;
    private final Object[] mSavedStatusesFileArgs;
    private Comparator<ParcelableStatus> mComparator;

    public Twitter4JStatusesLoader(final Context context, final long account_id, final long sinceId, final long maxId,
                                   final List<ParcelableStatus> data, final String[] savedStatusesArgs,
                                   final int tabPosition, boolean fromUser) {
        super(context, data, tabPosition, fromUser);
        mContext = context;
        mAccountId = account_id;
        mMaxId = maxId;
        mSinceId = sinceId;
        mDatabase = TwidereApplication.getInstance(context).getSQLiteDatabase();
        mSavedStatusesFileArgs = savedStatusesArgs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final List<ParcelableStatus> loadInBackground() {
        final File serializationFile = getSerializationFile();
        final List<ParcelableStatus> data = getData();
        if (isFirstLoad() && getTabPosition() >= 0 && serializationFile != null) {
            final List<ParcelableStatus> cached = getCachedData(serializationFile);
            if (cached != null) {
                data.addAll(cached);
                if (mComparator != null) {
                    Collections.sort(data, mComparator);
                } else {
                    Collections.sort(data);
                }
                return new CopyOnWriteArrayList<>(data);
            }
        }
        if (!isFromUser()) return data;
        final List<Status> statuses;
        final boolean truncated;
        final Context context = getContext();
        final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final int loadItemLimit = prefs.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
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
            final Twitter twitter = getTwitter();
            if (twitter == null) {
                throw new TwitterException("Account is null");
            }
            truncated = truncateStatuses(getStatuses(twitter, paging), statuses, mSinceId);
        } catch (final TwitterException e) {
            // mHandler.post(new ShowErrorRunnable(e));
            Log.w(LOGTAG, e);
            return new CopyOnWriteArrayList<>(data);
        }
        final long minStatusId = statuses.isEmpty() ? -1 : Collections.min(statuses).getId();
        final boolean insertGap = minStatusId > 0 && statuses.size() > 1 && !data.isEmpty() && !truncated;
        for (final Status status : statuses) {
            final long id = status.getId();
            final boolean deleted = deleteStatus(data, id);
            data.add(new ParcelableStatus(status, mAccountId, minStatusId == id && insertGap && !deleted));
        }
        final ParcelableStatus[] array = data.toArray(new ParcelableStatus[data.size()]);
        for (int i = 0, size = array.length; i < size; i++) {
            final ParcelableStatus status = array[i];
            if (shouldFilterStatus(mDatabase, status) && !status.is_gap && i != size - 1) {
                deleteStatus(data, status.id);
            }
        }
        if (mComparator != null) {
            Collections.sort(data, mComparator);
        } else {
            Collections.sort(data);
        }
        saveCachedData(serializationFile, data);
        return new CopyOnWriteArrayList<>(data);
    }

    public final void setComparator(Comparator<ParcelableStatus> comparator) {
        mComparator = comparator;
    }


    @NonNull
    protected abstract List<Status> getStatuses(@NonNull Twitter twitter, Paging paging) throws TwitterException;

    @Nullable
    protected final Twitter getTwitter() {
        return getTwitterInstance(mContext, mAccountId, true, true);
    }

    protected abstract boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status);

    private List<ParcelableStatus> getCachedData(final File file) {
        if (file == null) return null;
        try {
            return JSONFileIO.readArrayList(file);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File getSerializationFile() {
        if (mSavedStatusesFileArgs == null) return null;
        try {
            return JSONFileIO.getSerializationFile(mContext, mSavedStatusesFileArgs);
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
            final List<ParcelableStatus> activities = data.subList(0, Math.min(databaseItemLimit, data.size()));
            JSONFileIO.writeArray(file, activities.toArray(new ParcelableStatus[activities.size()]));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
