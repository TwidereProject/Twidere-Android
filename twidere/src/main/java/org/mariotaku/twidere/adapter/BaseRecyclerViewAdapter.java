/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.text.BidiFormatter;
import android.support.v7.widget.RecyclerView;

import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import javax.inject.Inject;

/**
 * Created by mariotaku on 15/10/5.
 */
public abstract class BaseRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final Context mContext;
    @Inject
    protected AsyncTwitterWrapper mTwitterWrapper;
    @Inject
    protected ReadStateManager mReadStateManager;
    @Inject
    protected MediaLoaderWrapper mMediaLoader;
    @Inject
    protected MultiSelectManager mMultiSelectManager;
    @Inject
    protected UserColorNameManager mUserColorNameManager;
    @Inject
    protected SharedPreferencesWrapper mPreferences;
    @Inject
    protected BidiFormatter mBidiFormatter;

    public BaseRecyclerViewAdapter(Context context) {
        mContext = context;
        //noinspection unchecked
        GeneralComponentHelper.build(context).inject((BaseRecyclerViewAdapter<RecyclerView.ViewHolder>) this);
    }

    @NonNull
    public final Context getContext() {
        return mContext;
    }

    public final SharedPreferencesWrapper getPreferences() {
        return mPreferences;
    }

    @NonNull
    public final UserColorNameManager getUserColorNameManager() {
        return mUserColorNameManager;
    }

    public final MultiSelectManager getMultiSelectManager() {
        return mMultiSelectManager;
    }

    @NonNull
    public final MediaLoaderWrapper getMediaLoader() {
        return mMediaLoader;
    }

    public final ReadStateManager getReadStateManager() {
        return mReadStateManager;
    }

    @NonNull
    public final AsyncTwitterWrapper getTwitterWrapper() {
        return mTwitterWrapper;
    }

    @NonNull
    public final BidiFormatter getBidiFormatter() {
        return mBidiFormatter;
    }

    public int findPositionByItemId(long itemId) {
        for (int i = 0, j = getItemCount(); i < j; i++) {
            if (getItemId(i) == itemId) return i;
        }
        return RecyclerView.NO_POSITION;
    }
}
