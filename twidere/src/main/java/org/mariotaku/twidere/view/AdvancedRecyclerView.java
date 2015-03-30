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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import org.mariotaku.twidere.Constants;

/**
 * Created by mariotaku on 15/3/30.
 */
public class AdvancedRecyclerView extends RecyclerView {
    public AdvancedRecyclerView(Context context) {
        super(context);
    }

    public AdvancedRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdvancedRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        Log.d(Constants.LOGTAG, event.toString());
        return super.dispatchTrackballEvent(event);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        final LayoutManager lm = getLayoutManager();
        if (!(lm instanceof LinearLayoutManager)) return false;
        final LinearLayoutManager llm = (LinearLayoutManager) lm;
        Log.d(Constants.LOGTAG, event.toString());
        return true;
    }

}
