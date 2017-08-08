/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util.view

import android.graphics.Rect
import android.view.View

/**
 * Created by mariotaku on 2017/2/21.
 */

class ConversationTitlesTransformation : AppBarChildBehavior.TextViewTransformation() {
    override fun onTargetChanged(child: View, frame: Rect, target: View, targetFrame: Rect, percent: Float, offset: Int) {
        super.onTargetChanged(child, frame, target, targetFrame, percent, offset)
        if (percent < 1) {
            child.alpha = 1f
            target.alpha = 0f
        } else {
            child.alpha = 0f
            target.alpha = 1f
        }
    }
}
