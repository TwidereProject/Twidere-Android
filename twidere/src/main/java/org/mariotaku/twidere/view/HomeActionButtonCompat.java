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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.iface.IHomeActionButton;

public class HomeActionButtonCompat extends FrameLayout implements IHomeActionButton {

    private final ImageView mIconView;
    private final ProgressBar mProgressBar;

    public HomeActionButtonCompat(final Context context) {
        this(context, null);
    }

    public HomeActionButtonCompat(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeActionButtonCompat(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        inflate(ThemeUtils.getActionBarContext(context), R.layout.action_item_home_actions_compat, this);
        mIconView = (ImageView) findViewById(android.R.id.icon);
        mProgressBar = (ProgressBar) findViewById(android.R.id.progress);
    }

    @Override
    public void setButtonColor(int color) {
        final Drawable drawable = getBackground();
        if (drawable instanceof LayerDrawable) {
            final Drawable layer = ((LayerDrawable) drawable).findDrawableByLayerId(R.id.color_layer);
            if (layer != null) {
                layer.setColorFilter(color, Mode.SRC_ATOP);
            }
        }
    }

    @Override
    public void setIconColor(int color, Mode mode) {
        mIconView.setColorFilter(color, mode);
    }

    public void setIcon(final Bitmap bm) {
        mIconView.setImageBitmap(bm);
    }

    public void setIcon(final Drawable drawable) {
        mIconView.setImageDrawable(drawable);
    }

    public void setIcon(final int resId) {
        mIconView.setImageResource(resId);
    }

    public void setShowProgress(final boolean showProgress) {
        mProgressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        mIconView.setVisibility(showProgress ? View.GONE : View.VISIBLE);
    }

    public void setTitle(final CharSequence title) {
        setContentDescription(title);
    }

    public void setTitle(final int title) {
        setTitle(getResources().getText(title));
    }

}
