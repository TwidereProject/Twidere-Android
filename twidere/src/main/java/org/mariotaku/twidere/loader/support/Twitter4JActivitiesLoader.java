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

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.AsyncTaskLoader;

import org.mariotaku.jsonserializer.JSONFileIO;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.util.collection.NoDuplicatesArrayList;

import twitter4j.Activity;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class Twitter4JActivitiesLoader extends AsyncTaskLoader<List<ParcelableActivity>> implements Constants {
	private final Context mContext;

	private final long[] mAccountIds;
	private final List<ParcelableActivity> mData = new NoDuplicatesArrayList<ParcelableActivity>();
	private final boolean mIsFirstLoad;
	private final boolean mUseCache;

	private final Object[] mSavedActivitiesFileArgs;

	public Twitter4JActivitiesLoader(final Context context, final long[] accountIds,
			final List<ParcelableActivity> data, final String[] saveFileArgs, final boolean useCache) {
		super(context);
		mContext = context;
		mAccountIds = accountIds;
		mIsFirstLoad = data == null;
		if (data != null) {
			mData.addAll(data);
		}
		mUseCache = useCache;
		mSavedActivitiesFileArgs = saveFileArgs;
	}

	public final long[] getAccountIds() {
		return mAccountIds;
	}

	@Override
	public final List<ParcelableActivity> loadInBackground() {
		if (mAccountIds == null) return Collections.emptyList();
		final File serializationFile = getSerializationFile();
		if (mIsFirstLoad && mUseCache && serializationFile != null) {
			final List<ParcelableActivity> cached = getCachedData(serializationFile);
			if (cached != null) {
				Collections.sort(cached);
				return new CopyOnWriteArrayList<ParcelableActivity>(cached);
			}
		}
		final SharedPreferences prefs = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int loadItemLimit = prefs.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
		final List<ParcelableActivity> result = new ArrayList<ParcelableActivity>();
		for (final long accountId : mAccountIds) {
			final List<Activity> activities;
			try {
				final Paging paging = new Paging();
				paging.setCount(Math.min(100, loadItemLimit));
				activities = getActivities(getTwitter(accountId), paging);
			} catch (final TwitterException e) {
				e.printStackTrace();
				final List<ParcelableActivity> cached = getCachedData(serializationFile);
				if (cached == null) return Collections.emptyList();
				return new CopyOnWriteArrayList<ParcelableActivity>(cached);
			}
			if (activities == null) return new CopyOnWriteArrayList<ParcelableActivity>(mData);
			for (final Activity activity : activities) {
				result.add(new ParcelableActivity(activity, accountId));
			}
		}
		Collections.sort(result);
		saveCachedData(serializationFile, result);
		return new CopyOnWriteArrayList<ParcelableActivity>(result);
	}

	protected abstract List<Activity> getActivities(Twitter twitter, Paging paging) throws TwitterException;

	protected final Twitter getTwitter(final long accountId) {
		return getTwitterInstance(mContext, accountId, true);
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	private List<ParcelableActivity> getCachedData(final File file) {
		if (file == null) return null;
		try {
			return JSONFileIO.readArrayList(file);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private File getSerializationFile() {
		if (mSavedActivitiesFileArgs == null) return null;
		try {
			return JSONFileIO.getSerializationFile(mContext, mSavedActivitiesFileArgs);
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
			JSONFileIO.writeArray(file, activities.toArray(new ParcelableActivity[activities.size()]));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
