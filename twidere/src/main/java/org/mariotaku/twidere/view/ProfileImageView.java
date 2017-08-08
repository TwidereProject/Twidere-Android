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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by mariotaku on 15/6/6.
 */
public class ProfileImageView extends ShapedImageView {
    public ProfileImageView(final Context context) {
        super(context);
    }

    public ProfileImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public ProfileImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec), height = MeasureSpec.getSize(heightMeasureSpec);
        final ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT && lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.onMeasure(makeSpec(heightMeasureSpec, MeasureSpec.EXACTLY), makeSpec(heightMeasureSpec, MeasureSpec.EXACTLY));
        } else if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT && lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.onMeasure(makeSpec(widthMeasureSpec, MeasureSpec.EXACTLY), makeSpec(widthMeasureSpec, MeasureSpec.EXACTLY));
        } else {
            if (width > height) {
                super.onMeasure(makeSpec(heightMeasureSpec, MeasureSpec.EXACTLY), makeSpec(heightMeasureSpec, MeasureSpec.EXACTLY));
            } else {
                super.onMeasure(makeSpec(widthMeasureSpec, MeasureSpec.EXACTLY), makeSpec(widthMeasureSpec, MeasureSpec.EXACTLY));
            }
        }
    }

    private static int makeSpec(int spec, int mode) {
        return MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(spec), mode);
    }
}
