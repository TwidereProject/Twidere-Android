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

package org.mariotaku.twidere.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Build;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.annotation.ImageShapeStyle;
import org.mariotaku.twidere.util.support.ViewSupport;
import org.mariotaku.twidere.util.support.graphics.OutlineCompat;
import org.mariotaku.twidere.util.support.view.ViewOutlineProviderCompat;

/**
 * An ImageView class with a circle mask so that all images are drawn in a
 * circle instead of a square.
 */
public class ShapedImageView extends AppCompatImageView {

    private static final int SHADOW_START_COLOR = 0x37000000;

    public static final boolean OUTLINE_DRAW = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    private final RectF mDestination;
    private final Paint mBackgroundPaint, mBorderPaint;
    private boolean mBorderEnabled;
    private Bitmap mShadowBitmap;
    private float mShadowRadius;
    private int mStyle;
    private float mCornerRadius, mCornerRadiusRatio;
    private boolean mDrawShadow;
    private RectF mTransitionSource, mTransitionDestination;
    private int mStrokeWidth, mBorderAlpha;
    private int[] mBorderColors;

    public ShapedImageView(Context context) {
        this(context, null, 0);
    }

    public ShapedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShapedImageView, defStyle, 0);

        mDestination = new RectF();

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);

        if (a.hasValue(R.styleable.ShapedImageView_sivBorder)) {
            setBorderEnabled(a.getBoolean(R.styleable.ShapedImageView_sivBorder, false));
        } else if (a.hasValue(R.styleable.ShapedImageView_sivBorderColor)
                || a.hasValue(R.styleable.ShapedImageView_sivBorderWidth)) {
            setBorderEnabled(true);
        }
        setBorderColor(a.getColor(R.styleable.ShapedImageView_sivBorderColor, Color.TRANSPARENT));
        setBorderWidth(a.getDimensionPixelSize(R.styleable.ShapedImageView_sivBorderWidth, 0));
        @ImageShapeStyle
        final int shapeStyle = a.getInt(R.styleable.ShapedImageView_sivShape, ImageShapeStyle.SHAPE_RECTANGLE);
        setStyle(shapeStyle);
        setCornerRadius(a.getDimension(R.styleable.ShapedImageView_sivCornerRadius, 0));
        setCornerRadiusRatio(a.getFraction(R.styleable.ShapedImageView_sivCornerRadiusRatio, 1, 1, -1));
        setDrawShadow(a.getBoolean(R.styleable.ShapedImageView_sivDrawShadow, true));

        if (useOutline()) {
            if (a.hasValue(R.styleable.ShapedImageView_sivElevation)) {
                ViewCompat.setElevation(this,
                        a.getDimensionPixelSize(R.styleable.ShapedImageView_sivElevation, 0));
            }
        } else {
            mShadowRadius = a.getDimensionPixelSize(R.styleable.ShapedImageView_sivElevation, 0);
        }
        setShapeBackground(a.getColor(R.styleable.ShapedImageView_sivBackgroundColor, 0));
        a.recycle();

        initOutlineProvider();
    }

    public void setShapeBackground(final int color) {
        mBackgroundPaint.setColor(color);
        invalidate();
    }

    public int[] getBorderColors() {
        return mBorderColors;
    }

    @ImageShapeStyle
    public int getStyle() {
        return mStyle;
    }

    public void setStyle(@ImageShapeStyle final int style) {
        mStyle = style;
        initOutlineProvider();
    }

    public void setBorderColor(int color) {
        setBorderColorsInternal(Color.alpha(color), color);
    }

    public void setBorderColors(int... colors) {
        setBorderColorsInternal(0xff, colors);
    }

    public void setBorderEnabled(boolean enabled) {
        mBorderEnabled = enabled;
        invalidate();
    }

    public void setBorderWidth(int width) {
        mBorderPaint.setStrokeWidth(width);
        mStrokeWidth = width;
        invalidate();
    }

    public void setCornerRadius(float radius) {
        mCornerRadius = radius;
    }

    public float getCornerRadius() {
        return mCornerRadius;
    }

    public void setCornerRadiusRatio(float ratio) {
        mCornerRadiusRatio = ratio;
    }

    public float getCornerRadiusRatio() {
        return mCornerRadiusRatio;
    }

    public void setDrawShadow(final boolean drawShadow) {
        mDrawShadow = drawShadow;
    }

    public void setTransitionDestination(RectF dstBounds) {
        mTransitionDestination = dstBounds;
    }

    public void setTransitionSource(RectF srcBounds) {
        mTransitionSource = srcBounds;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        mDestination.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());

        final int contentLeft = getPaddingLeft(), contentTop = getPaddingTop(),
                contentRight = getWidth() - getPaddingRight(),
                contentBottom = getHeight() - getPaddingBottom();
        final int contentWidth = contentRight - contentLeft,
                contentHeight = contentBottom - contentTop;
        final int size = Math.min(contentWidth, contentHeight);

        if (!OUTLINE_DRAW) {
            if (mShadowBitmap != null && mDrawShadow) {
                canvas.drawBitmap(mShadowBitmap, contentLeft + (contentWidth - size) / 2 - mShadowRadius,
                        contentTop + (contentHeight - size) / 2 - mShadowRadius, null);
            }
        }
        drawShape(canvas, mDestination, 0, mBackgroundPaint);
        super.onDraw(canvas);
        // Then draw the border.
        if (mBorderEnabled) {
            drawBorder(canvas, mDestination);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateBounds();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        updateBounds();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
        updateBounds();
    }

    @Override
    public void setAlpha(@FloatRange(from = 0.0, to = 1.0) final float alpha) {
        super.setAlpha(alpha);
        mBackgroundPaint.setAlpha(Math.round(alpha * 255));
    }

    private void drawBorder(@NonNull final Canvas canvas, @NonNull final RectF dest) {
        if (mBorderColors == null) return;
        final RectF transitionSrc = mTransitionSource, transitionDst = mTransitionDestination;
        final float strokeWidth;
        if (transitionSrc != null && transitionDst != null) {
            final float progress = 1 - (dest.width() - transitionDst.width())
                    / (transitionSrc.width() - transitionDst.width());
            strokeWidth = mStrokeWidth * progress;
            mBorderPaint.setAlpha(Math.round(mBorderAlpha * progress));
            ViewCompat.setTranslationZ(this, -ViewCompat.getElevation(this) * (1 - progress));
        } else {
            strokeWidth = mStrokeWidth;
            mBorderPaint.setAlpha(mBorderAlpha);
            ViewCompat.setTranslationZ(this, 0);
        }
        mBorderPaint.setStrokeWidth(strokeWidth);
        drawShape(canvas, dest, strokeWidth, mBorderPaint);
    }

    private void drawShape(@NonNull final Canvas canvas, @NonNull final RectF dest, final float strokeWidth, final Paint paint) {
        if (getStyle() == ImageShapeStyle.SHAPE_CIRCLE) {
            final float circleRadius = Math.min(dest.width(), dest.height()) / 2f - strokeWidth / 2;
            canvas.drawCircle(dest.centerX(), dest.centerY(), circleRadius, paint);
        } else {
            final float radius = getCalculatedCornerRadius();
            final float inset = mStrokeWidth / 2;
            dest.inset(inset, inset);
            canvas.drawRoundRect(dest, radius, radius, paint);
            dest.inset(-inset, -inset);
        }
    }

    private float getCalculatedCornerRadius() {
        if (mCornerRadiusRatio > 0) {
            return Math.min(getWidth(), getHeight()) * mCornerRadiusRatio;
        } else if (mCornerRadius > 0) {
            return mCornerRadius;
        }
        return 0;
    }

    private void initOutlineProvider() {
        if (!useOutline()) return;
        ViewSupport.setClipToOutline(this, true);
        ViewSupport.setOutlineProvider(this, new CircularOutlineProvider(mDrawShadow));
    }

    private void setBorderColorsInternal(int alpha, int... colors) {
        mBorderAlpha = alpha;
        mBorderColors = colors;
        updateBorderShader();
        invalidate();
    }

    private void updateBorderShader() {
        final int[] colors = mBorderColors;
        if (colors == null || colors.length == 0) {
            mBorderAlpha = 0;
            return;
        }
        mDestination.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom());
        final float cx = mDestination.centerX(), cy = mDestination.centerY();
        final int[] sweepColors = new int[colors.length * 2];
        final float[] positions = new float[colors.length * 2];
        for (int i = 0, j = colors.length; i < j; i++) {
            sweepColors[i * 2] = sweepColors[i * 2 + 1] = colors[i];
            positions[i * 2] = i == 0 ? 0 : i / (float) j;
            positions[i * 2 + 1] = i == j - 1 ? 1 : (i + 1) / (float) j;
        }
        final SweepGradient shader = new SweepGradient(cx, cy, sweepColors, positions);
        final Matrix matrix = new Matrix();
        matrix.setRotate(90, cx, cy);
        shader.setLocalMatrix(matrix);
        mBorderPaint.setShader(shader);
    }

    private void updateBounds() {
        updateBorderShader();
        updateShadowBitmap();
    }

    private void updateShadowBitmap() {
        if (useOutline() || !mDrawShadow) return;
        final int width = getWidth(), height = getHeight();
        if (width <= 0 || height <= 0) return;
        final int contentLeft = getPaddingLeft(), contentTop = getPaddingTop(),
                contentRight = width - getPaddingRight(),
                contentBottom = height - getPaddingBottom();
        final int contentWidth = contentRight - contentLeft,
                contentHeight = contentBottom - contentTop;
        final float radius = mShadowRadius, dy = radius * 1.5f / 2;
        final int size = Math.round(Math.min(contentWidth, contentHeight) + radius * 2);
        mShadowBitmap = Bitmap.createBitmap(size, Math.round(size + dy), Config.ARGB_8888);
        if (mShadowBitmap == null) return;
        Canvas canvas = new Canvas(mShadowBitmap);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xFF000000);
        paint.setShadowLayer(radius, 0, radius * 1.5f / 2, SHADOW_START_COLOR);
        final RectF rect = new RectF(radius, radius, size - radius, size - radius);
        if (getStyle() == ImageShapeStyle.SHAPE_CIRCLE) {
            canvas.drawOval(rect, paint);
            paint.setShadowLayer(0, 0, 0, 0);
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            canvas.drawOval(rect, paint);
        } else {
            final float cr = getCalculatedCornerRadius();
            canvas.drawRoundRect(rect, cr, cr, paint);
            paint.setShadowLayer(0, 0, 0, 0);
            paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            canvas.drawRoundRect(rect, cr, cr, paint);
        }
        invalidate();
    }

    private boolean useOutline() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && !isInEditMode();
    }

    private static class CircularOutlineProvider extends ViewOutlineProviderCompat {
        boolean drawShadow;

        public CircularOutlineProvider(final boolean drawShadow) {
            this.drawShadow = drawShadow;
        }

        @Override
        public void getOutline(View view, OutlineCompat outline) {
            final int viewWidth = view.getWidth(), viewHeight = view.getHeight();
            final int contentLeft = view.getPaddingLeft(), contentTop = view.getPaddingTop(),
                    contentRight = viewWidth - view.getPaddingRight(),
                    contentBottom = viewHeight - view.getPaddingBottom();
            final ShapedImageView imageView = (ShapedImageView) view;
            if (imageView.getStyle() == ImageShapeStyle.SHAPE_CIRCLE) {
                final int contentWidth = contentRight - contentLeft,
                        contentHeight = contentBottom - contentTop;
                final int size = Math.min(contentWidth, contentHeight);
                outline.setOval(contentLeft + (contentWidth - size) / 2,
                        contentTop + (contentHeight - size) / 2,
                        contentRight - (contentWidth - size) / 2,
                        contentBottom - (contentHeight - size) / 2);
            } else {
                final float radius = imageView.getCalculatedCornerRadius();
                outline.setRoundRect(contentLeft, contentTop, contentRight, contentBottom, radius);
            }
            if (drawShadow) {
                outline.setAlpha(1);
            } else {
                outline.setAlpha(0);
            }
        }
    }
}