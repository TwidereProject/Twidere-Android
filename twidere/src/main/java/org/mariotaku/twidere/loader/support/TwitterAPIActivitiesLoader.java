/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.util.Log;
import android.util.Pair;

import com.bluelinelabs.logansquare.LoganSquare;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.util.LoganSquareWrapper;
import org.mariotaku.twidere.util.TwitterAPIFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.mariotaku.twidere.util.Utils.truncateActivities;

public abstract class TwitterAPIActivitiesLoader extends ParcelableActivitiesLoader {

    private final Context mContext;
    private final long mAccountIds;
    private final long mMaxId, mSinceId;
    private final Object[] mSavedStatusesFileArgs;
    private Comparator<ParcelableActivity> mComparator;

    public TwitterAPIActivitiesLoader(final Context context, final long accountId, final long sinceId,
                                      final long maxId, final List<ParcelableActivity> data, final String[] savedStatusesArgs,
                                      final int tabPosition) {
        super(context, data, tabPosition);
        mContext = context;
        mAccountIds = accountId;
        mSinceId = sinceId;
        mMaxId = maxId;
        mSavedStatusesFileArgs = savedStatusesArgs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final List<ParcelableActivity> loadInBackground() {
        final File serializationFile = getSerializationFile();
        final List<ParcelableActivity> data = getData();
        if (isFirstLoad() && getTabPosition() >= 0 && serializationFile != null) {
            final List<ParcelableActivity> cached = getCachedData(serializationFile);
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
        final List<Activity> activities;
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
                paging.setSinceId(mSinceId);
            }
            activities = new ArrayList<>();
            truncated = truncateActivities(getActivities(getTwitter(), paging), activities, mSinceId);
        } catch (final TwitterException e) {
            // mHandler.post(new ShowErrorRunnable(e));
            e.printStackTrace();
            return new CopyOnWriteArrayList<>(data);
        }
        final Pair<Long, Long> position;
        if (activities.isEmpty()) {
            position = new Pair<>(-1L, -1L);
        } else {
            final Activity minActivity = Collections.min(activities);
            position = new Pair<>(minActivity.getMinPosition(), minActivity.getMaxPosition());
        }
        final boolean insertGap = position.first > 0 && position.second > 0 && activities.size() > 1
                && !data.isEmpty() && !truncated;
//        mHandler.post(CacheUsersStatusesTask.getRunnable(context, new StatusListResponse(mAccountIds, activities)));
        for (final Activity activity : activities) {
            final long min = activity.getMinPosition(), max = activity.getMaxPosition();
            final boolean deleted = deleteActivity(data, max);
            final boolean isGap = position.first == min && position.second == max && insertGap && !deleted;
            data.add(new ParcelableActivity(activity, mAccountIds, isGap));
        }
//        final ParcelableActivity[] array = data.toArray(new ParcelableActivity[data.size()]);
//        for (int i = 0, size = array.length; i < size; i++) {
//            final ParcelableActivity status = array[i];
//            if (shouldFilterActivity(mDatabase, status) && !status.is_gap && i != size - 1) {
//                deleteActivity(data, status.max_position);
//            }
//        }
        if (mComparator != null) {
            Collections.sort(data, mComparator);
        } else {
            Collections.sort(data);
        }
        saveCachedData(serializationFile, data);
        return new CopyOnWriteArrayList<>(data);
    }

    public final void setComparator(Comparator<ParcelableActivity> comparator) {
        mComparator = comparator;
    }

    protected abstract List<Activity> getActivities(Twitter twitter, Paging paging) throws TwitterException;

    protected final Twitter getTwitter() {
        return TwitterAPIFactory.getTwitterInstance(mContext, mAccountIds, true, true);
    }

    protected abstract boolean shouldFilterActivity(final SQLiteDatabase database, final ParcelableActivity activity);

    private List<ParcelableActivity> getCachedData(final File file) {
        if (file == null) return null;
        try {
            return LoganSquareWrapper.parseList(file, ParcelableActivity.class);
        } catch (final IOException e) {
            if (BuildConfig.DEBUG) {
                Log.w(LOGTAG, e);
            }
        } catch (RuntimeException e) {
            if (BuildConfig.DEBUG) {
                throw e;
            }
            Log.e(LOGTAG, "Error unserializing data", e);
        }
        return null;
    }

    private File getSerializationFile() {
        if (mSavedStatusesFileArgs == null) return null;
        try {
            return LoganSquareWrapper.getSerializationFile(mContext, mSavedStatusesFileArgs);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveCachedData(final File file, final List<ParcelableActivity> data) {
        if (file == null || data == null) return;
        final SharedPreferences prefs = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final int databaseItemLimit = prefs.getInt(KEY_DATABASE_ITEM_LIMIT, DEFAULT_DATABASE_ITEM_LIMIT);
        try {
            final List<ParcelableActivity> activities = data.subList(0, Math.min(databaseItemLimit, data.size()));
            LoganSquare.serialize(activities, new FileOutputStream(file), ParcelableActivity.class);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
