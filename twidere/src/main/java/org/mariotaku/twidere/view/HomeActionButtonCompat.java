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
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.view.iface.IHomeActionButton;

public class HomeActionButtonCompat extends FrameLayout implements IHomeActionButton {

    private final ImageView mIconView;
    private final ProgressBar mProgressBar;
    private final FloatingActionDrawable mBackground;

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
        final Resources resources = getResources();
        final int radius = resources.getDimensionPixelSize(R.dimen.element_spacing_small);
        mBackground = new FloatingActionDrawable(this, radius);
        ViewAccessor.setBackground(this, mBackground);
    }

    @Override
    public void setButtonColor(int color) {
        mBackground.setColor(color);
        final View child = getChildAt(0);
        if (child instanceof FrameLayout) {
            final Drawable foreground = ((FrameLayout) child).getForeground();
            if (foreground != null) {
                foreground.setColorFilter(Utils.getContrastYIQ(color, 192), Mode.MULTIPLY);
            }
        }

    }

    private static class FloatingActionDrawable extends Drawable {


        private static final int SHADOW_START_COLOR = 0x37000000;

        private final View mView;
        private final float mRadius;

        private Bitmap mBitmap;
        private Paint mColorPaint;
        private Rect mBounds;

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            mBounds.set(bounds);
            updateBitmap();
        }

        public FloatingActionDrawable(View view, float radius) {
            mView = view;
            mRadius = radius;
            mColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBounds = new Rect();
        }

        @Override
        public int getIntrinsicHeight() {
            return -1;
        }

        @Override
        public void draw(Canvas canvas) {
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, 0, 0, null);
            }
            final Rect bounds = mBounds;
            if (!bounds.isEmpty()) {
                final RectF rect = new RectF(mView.getPaddingLeft(), mView.getPaddingTop(),
                        bounds.width() - mView.getPaddingRight(), bounds.height() - mView.getPaddingBottom());
                canvas.drawOval(rect, mColorPaint);
            }
        }

        private void updateBitmap() {
            final Rect bounds = mBounds;
            if (bounds.isEmpty()) return;
            mBitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Config.ARGB_8888);
            Canvas canvas = new Canvas(mBitmap);
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE);
            final float radius = mRadius;
            paint.setShadowLayer(radius, 0, radius * 1.5f / 2, SHADOW_START_COLOR);
            final RectF rect = new RectF(mView.getPaddingLeft(), mView.getPaddingTop(),
                    bounds.width() - mView.getPaddingRight(), bounds.height() - mView.getPaddingBottom());
            canvas.drawOval(rect, paint);
            paint.setShadowLayer(0, 0, 0, 0);
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            canvas.drawOval(rect, paint);
            invalidateSelf();
        }

        @Override
        public void setAlpha(int alpha) {
            // No-op
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            mColorPaint.setColorFilter(cf);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public int getIntrinsicWidth() {
            return -1;
        }

        public void setColor(int color) {
            mColorPaint.setColor(color);
            invalidateSelf();
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
