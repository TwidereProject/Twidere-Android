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

package org.mariotaku.twidere.graphic;

import android.annotation.TargetApi;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import androidx.annotation.NonNull;

/**
 * Created by mariotaku on 14/12/8.
 */
public class ActionBarColorDrawable extends ActionBarColorDrawableBase {
    public ActionBarColorDrawable(boolean outlineEnabled) {
        super(outlineEnabled);
    }

    public ActionBarColorDrawable(int color, boolean outlineEnabled) {
        super(color, outlineEnabled);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void getOutline(@NonNull Outline outline) {
        if (!isOutlineEnabled()) return;
        final Rect bounds = getBounds();
        // Very very dirty hack to make outline shadow in action bar not visible beneath status bar
        outline.setRect(bounds.left - bounds.width() / 2, -bounds.height(),
                bounds.right + bounds.width() / 2, bounds.bottom);
        outline.setAlpha(getAlpha() / 255f);
    }
}
