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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.mariotaku.twidere.view.iface.IForegroundView;

public class ForegroundImageView extends ImageView implements IForegroundView {

    private final ForegroundViewHelper mForegroundViewHelper;

    public ForegroundImageView(final Context context) {
        super(context);
        mForegroundViewHelper = new ForegroundViewHelper(this, context, null, 0);
    }

    public ForegroundImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        mForegroundViewHelper = new ForegroundViewHelper(this, context, attrs, 0);
    }

    public ForegroundImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mForegroundViewHelper = new ForegroundViewHelper(this, context, attrs, defStyle);
    }

    @Override
    public Drawable getForeground() {
        if (mForegroundViewHelper != null) {
            return mForegroundViewHelper.getForeground();
        }
        return null;
    }

    /**
     * Supply a Drawable that is to be rendered on top of all of the child views
     * in the frame layout. Any padding in the Drawable will be taken into
     * account by ensuring that the children are inset to be placed inside of
     * the padding area.
     *
     * @param drawable The Drawable to be drawn on top of the children.
     * @attr ref android.R.styleable#FrameLayout_foreground
     */
    @Override
    public void setForeground(final Drawable drawable) {
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.setForeground(drawable);
        }
    }

    /**
     * Describes how the foreground is positioned. Defaults to START and TOP.
     *
     * @param foregroundGravity See {@link android.view.Gravity}
     * @attr ref android.R.styleable#FrameLayout_foregroundGravity
     */
    @Override
    public void setForegroundGravity(final int foregroundGravity) {
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.setForegroundGravity(foregroundGravity);
        }
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.dispatchOnSizeChanged(w, h, oldw, oldh);
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.dispatchOnLayout(changed, left, top, right, bottom);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected boolean verifyDrawable(final Drawable who) {
        return super.verifyDrawable(who) || (mForegroundViewHelper != null && mForegroundViewHelper.verifyDrawable(who));
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.jumpDrawablesToCurrentState();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.drawableStateChanged();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.dispatchDrawableHotspotChanged(x, y);
        }
    }

    @Override
    protected void onDraw(@NonNull final Canvas canvas) {
        super.onDraw(canvas);
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.dispatchOnDraw(canvas);
        }
    }

}
