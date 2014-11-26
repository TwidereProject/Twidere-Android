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

package org.mariotaku.twidere.animation;

import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Created by mariotaku on 14/11/23.
 */
public class CardItemAnimator extends ItemAnimator {
    @Override
    public void runPendingAnimations() {

    }

    @Override
    public boolean animateRemove(ViewHolder holder) {
        return false;
    }

    @Override
    public boolean animateAdd(ViewHolder holder) {
        return false;
    }

    @Override
    public boolean animateMove(ViewHolder holder, int fromX, int fromY,
                               int toX, int toY) {
        return false;
    }

    @Override
    public boolean animateChange(ViewHolder oldHolder,
                                 ViewHolder newHolder, int fromLeft, int fromTop, int toLeft, int toTop) {
        return false;
    }

    @Override
    public void endAnimation(ViewHolder holder) {

    }

    @Override
    public void endAnimations() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
