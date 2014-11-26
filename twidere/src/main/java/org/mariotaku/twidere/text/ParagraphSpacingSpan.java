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

package org.mariotaku.twidere.text;

import android.graphics.Paint.FontMetricsInt;
import android.text.Spanned;
import android.text.style.LineHeightSpan;

/**
 * Created by mariotaku on 14/11/22.
 */
public class ParagraphSpacingSpan implements LineHeightSpan {

    private final float spacingMultiplier;

    public ParagraphSpacingSpan(float spacingMultiplier) {
        this.spacingMultiplier = spacingMultiplier;
    }

    @Override
    public void chooseHeight(CharSequence text, int start, int end,
                             int spanstartv, int v, FontMetricsInt fm) {
        Spanned spanned = (Spanned) text;
        int en = spanned.getSpanEnd(this);
        if (end - 1 == en) {
            final int extra = Math.round((fm.bottom - fm.top) * (spacingMultiplier - 1));
            fm.descent += extra;
            fm.bottom += extra;
        }
    }
}
