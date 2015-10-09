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
import android.support.v7.widget.RecyclerView;

import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.dagger.ApplicationModule;
import org.mariotaku.twidere.util.dagger.DaggerGeneralComponent;

import javax.inject.Inject;

/**
 * Created by mariotaku on 15/10/5.
 */
public abstract class BaseRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
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

    public BaseRecyclerViewAdapter(Context context) {
        //noinspection unchecked
        DaggerGeneralComponent.builder()
                .applicationModule(ApplicationModule.get(context))
                .build()
                .inject((BaseRecyclerViewAdapter<RecyclerView.ViewHolder>) this);
    }
}
