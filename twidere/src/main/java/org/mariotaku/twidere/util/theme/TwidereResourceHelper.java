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

package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.content.res.Resources;

import org.mariotaku.twidere.content.res.NoAccentResources;

public class TwidereResourceHelper {

    private final int mOverrideThemeRes;

    private NoAccentResources mResources;
    private OnInitListener mInitListener;

    public TwidereResourceHelper(final int overrideThemeRes, OnInitListener listener) {
        mOverrideThemeRes = overrideThemeRes;
        mInitListener = listener;
    }

    public Resources getResources(final Context c, final Resources resources) {
        if (mResources != null) return mResources;
        mResources = new NoAccentResources(c, resources);
        if (mInitListener != null)
            mInitListener.onInitResources(mResources);
        return mResources;
    }

    /**
     * Set a listener to be notified when the instance of AccentResources is created.
     *
     * @param listener The actual listener or null to disable any event reporting.
     */
    public void setOnInitListener(OnInitListener listener) {
        mInitListener = listener;
    }

    public interface OnInitListener {
        public void onInitResources(NoAccentResources resources);
    }

}
