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

package org.mariotaku.twidere.util;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewParent;
import android.widget.ProgressBar;

import org.mariotaku.twidere.R;

public class MediaLoadingHandler {

    private final SparseArray<String> mLoadingUris = new SparseArray<>();
    private final int[] mProgressBarIds;

    public MediaLoadingHandler() {
        this(R.id.media_preview_progress);
    }

    public MediaLoadingHandler(final int... progressBarIds) {
        mProgressBarIds = progressBarIds;
    }

    public String getLoadingUri(final View view) {
        return mLoadingUris.get(System.identityHashCode(view));
    }

    private ProgressBar findProgressBar(final ViewParent viewParent) {
        if (mProgressBarIds == null || !(viewParent instanceof View)) return null;
        final View parent = (View) viewParent;
        for (final int id : mProgressBarIds) {
            final View progress = parent.findViewById(id);
            if (progress instanceof ProgressBar) return (ProgressBar) progress;
        }
        return null;
    }

}
