/*
 * Twidere - Twitter client for Android
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

import android.os.SystemClock;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import org.mariotaku.twidere.view.HeaderDrawerLayout.DrawerCallback;

/**
* Created by mariotaku on 14/12/2.
*/
public class SimpleDrawerCallback implements DrawerCallback {

    private final RecyclerView mRecyclerView;

    public SimpleDrawerCallback(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }


    @Override
    public void fling(float velocity) {
        mRecyclerView.fling(0, (int) velocity);
    }

    @Override
    public void scrollBy(float dy) {
        mRecyclerView.scrollBy(0, (int) dy);
    }

    @Override
    public boolean canScroll(float dy) {
        return mRecyclerView.canScrollVertically((int) dy);
    }

    @Override
    public boolean isScrollContent(float x, float y) {
        final View v = mRecyclerView;
        final int[] location = new int[2];
        v.getLocationInWindow(location);
        return x >= location[0] && x <= location[0] + v.getWidth()
                && y >= location[1] && y <= location[1] + v.getHeight();
    }

    @Override
    public void cancelTouch() {
        mRecyclerView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, 0, 0, 0));
    }

    @Override
    public boolean shouldLayoutHeaderBottom() {
        return true;
    }


    @Override
    public void topChanged(int offset) {

    }
}
