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
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.support.ViewSupport;
import org.mariotaku.twidere.util.support.graphics.OutlineCompat;
import org.mariotaku.twidere.util.support.view.ViewOutlineProviderCompat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An ImageView class with a circle mask so that all images are drawn in a
 * circle instead of a square.
 */
public class ShapedImageView extends ImageView {

    @ShapeStyle
    public static final int SHAPE_CIRCLE = 0x1;
    @ShapeStyle
    public static final int SHAPE_RECTANGLE = 0x2;
    private static final int SHADOW_START_COLOR = 0x37000000;
    private static final boolean OUTLINE_DRAW = false;
    private final Matrix mMatrix;
    private final RectF mSource;
    private final RectF mDestination;
    private final RectF mTempDestination;
    private final Paint mBitmapPaint;
    private final Paint mSolidColorPaint;
    private final Paint mBorderPaint;
    private final Paint mBackgroundPaint;
    private boolean mBorderEnabled;
    private Bitmap mShadowBitmap;
    private float mShadowRadius;
    private int mStyle;
    private float mCornerRadius, mCornerRadiusRatio;
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

        mMatrix = new Matrix();
        mSource = new RectF();
        mDestination = new RectF();
        mTempDestination = new RectF();

        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint.setFilterBitmap(true);
        mBitmapPaint.setDither(true);
        mSolidColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (a.hasValue(R.styleable.ShapedImageView_sivBorder)) {
            setBorderEnabled(a.getBoolean(R.styleable.ShapedImageView_sivBorder, false));
        } else if (a.hasValue(R.styleable.ShapedImageView_sivBorderColor)
                || a.hasValue(R.styleable.ShapedImageView_sivBorderWidth)) {
            setBorderEnabled(true);
        }
        setBorderColor(a.getColor(R.styleable.ShapedImageView_sivBorderColor, Color.TRANSPARENT));
        setBorderWidth(a.getDimensionPixelSize(R.styleable.ShapedImageView_sivBorderWidth, 0));
        @ShapeStyle
        final int shapeStyle = a.getInt(R.styleable.ShapedImageView_sivShape, SHAPE_RECTANGLE);
        setStyle(shapeStyle);
        setCornerRadius(a.getDimension(R.styleable.ShapedImageView_sivCornerRadius, 0));
        setCornerRadiusRatio(a.getFraction(R.styleable.ShapedImageView_sivCornerRadiusRatio, 1, 1, -1));

        if (useOutline()) {
            if (a.hasValue(R.styleable.ShapedImageView_sivElevation)) {
                ViewCompat.setElevation(this,
                        a.getDimensionPixelSize(R.styleable.ShapedImageView_sivElevation, 0));
            }
        } else {
            mShadowRadius = a.getDimensionPixelSize(R.styleable.ShapedImageView_sivElevation, 0);
        }
        setBackgroundColor(a.getColor(R.styleable.ShapedImageView_sivBackgroundColor, 0));
        a.recycle();

        initOutlineProvider();
    }

    /**
     * Given the source bitmap and a canvas, draws the bitmap through a circular
     * mask. Only draws a circle with diameter equal to the destination width.
     *
     * @param bitmap The source bitmap to draw.
     * @param canvas The canvas to draw it on.
     * @param source The source bound of the bitmap.
     * @param dest   The destination bound on the canvas.
     */
    public void drawBitmapWithCircleOnCanvas(Bitmap bitmap, Canvas canvas,
                                             RectF source, @NonNull RectF dest) {
        if (bitmap == null) {
            if (getStyle() == SHAPE_CIRCLE) {
                canvas.drawCircle(dest.centerX(), dest.centerY(), Math.min(dest.width(), dest.height()) / 2f,
                        mSolidColorPaint);
            } else {
                final float cornerRadius = getCalculatedCornerRadius();
                canvas.drawRoundRect(dest, cornerRadius, cornerRadius, mSolidColorPaint);
            }
            return;
        }
        // Draw bitmap through shader first.
        final BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        mMatrix.reset();

        switch (getScaleType()) {
            case CENTER_CROP: {
                final float srcRatio = source.width() / source.height();
                final float dstRatio = dest.width() / dest.height();
                if (srcRatio > dstRatio) {
                    // Source is wider than destination, fit height
                    mTempDestination.top = dest.top;
                    mTempDestination.bottom = dest.bottom;
                    final float dstWidth = dest.height() * srcRatio;
                    mTempDestination.left = dest.centerX() - dstWidth / 2;
                    mTempDestination.right = dest.centerX() + dstWidth / 2;
                } else if (srcRatio < dstRatio) {
                    mTempDestination.left = dest.left;
                    mTempDestination.right = dest.right;
                    final float dstHeight = dest.width() / srcRatio;
                    mTempDestination.top = dest.centerY() - dstHeight / 2;
                    mTempDestination.bottom = dest.centerY() + dstHeight / 2;
                } else {
                    mTempDestination.set(dest);
                }
                break;
            }
            default: {
                mTempDestination.set(dest);
                break;
            }
        }

        // Fit bitmap to bounds.
        mMatrix.setRectToRect(source, mTempDestination, ScaleToFit.CENTER);

        shader.setLocalMatrix(mMatrix);
        mBitmapPaint.setShader(shader);


        if (mBorderEnabled) {
            final float inset = mBorderPaint.getStrokeWidth() / 2;
            if (getStyle() == SHAPE_CIRCLE) {
                final float circleRadius = Math.min(dest.width(), dest.height()) / 2f - inset / 2;
                canvas.drawCircle(dest.centerX(), dest.centerY(), circleRadius, mBitmapPaint);
            } else {
                final float cornerRadius = getCalculatedCornerRadius();
                dest.inset(inset, inset);
                canvas.drawRoundRect(dest, cornerRadius, cornerRadius, mBitmapPaint);
                dest.inset(-inset, -inset);
            }
        } else {
            if (getStyle() == SHAPE_CIRCLE) {
                final float circleRadius = Math.min(dest.width(), dest.height()) / 2f;
                canvas.drawCircle(dest.centerX(), dest.centerY(), circleRadius, mBitmapPaint);
            } else {
                final float cornerRadius = getCalculatedCornerRadius();
                canvas.drawRoundRect(dest, cornerRadius, cornerRadius, mBitmapPaint);
            }
        }

    }

    public int[] getBorderColors() {
        return mBorderColors;
    }

    @ShapeStyle
    public int getStyle() {
        return mStyle;
    }

    public void setStyle(@ShapeStyle final int style) {
        mStyle = style;
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

    public void setCornerRadiusRatio(float ratio) {
        mCornerRadiusRatio = ratio;
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

        if (getStyle() == SHAPE_CIRCLE) {
            canvas.drawOval(mDestination, mBackgroundPaint);
        } else {
            final float radius = getCalculatedCornerRadius();
            canvas.drawRoundRect(mDestination, radius, radius, mBackgroundPaint);
        }

        if (OUTLINE_DRAW) {
            super.onDraw(canvas);
        } else {
            final int contentLeft = getPaddingLeft(), contentTop = getPaddingTop(),
                    contentRight = getWidth() - getPaddingRight(),
                    contentBottom = getHeight() - getPaddingBottom();
            final int contentWidth = contentRight - contentLeft,
                    contentHeight = contentBottom - contentTop;
            final int size = Math.min(contentWidth, contentHeight);
            if (mShadowBitmap != null) {
                canvas.drawBitmap(mShadowBitmap, contentLeft + (contentWidth - size) / 2 - mShadowRadius,
                        contentTop + (contentHeight - size) / 2 - mShadowRadius, null);
            }
            Drawable drawable = getDrawable();
            BitmapDrawable bitmapDrawable = null;
            // support state list drawable by getting the current state
            if (drawable instanceof StateListDrawable) {
                if (drawable.getCurrent() != null) {
                    bitmapDrawable = (BitmapDrawable) drawable.getCurrent();
                }
            } else if (drawable instanceof BitmapDrawable) {
                bitmapDrawable = (BitmapDrawable) drawable;
            } else if (drawable instanceof ColorDrawable) {
                mSolidColorPaint.setColor(((ColorDrawable) drawable).getColor());
            } else {
                mSolidColorPaint.setColor(0);
            }

            Bitmap bitmap = null;
            if (bitmapDrawable != null) {
                bitmap = bitmapDrawable.getBitmap();
            }
            if (bitmap != null) {
                mSource.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
            }
            drawBitmapWithCircleOnCanvas(bitmap, canvas, mSource, mDestination);
        }

        // Then draw the border.
        if (mBorderEnabled) {
            drawBorder(canvas, mDestination);
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (OUTLINE_DRAW) {
            super.setColorFilter(cf);
            return;
        }
        mBitmapPaint.setColorFilter(cf);
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
    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(0xFF000000 | color);
        mBackgroundPaint.setAlpha(Color.alpha(color));
        invalidate();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void setBackground(Drawable background) {
    }

    @Deprecated
    @Override
    public void setBackgroundDrawable(Drawable background) {
        // No-op
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
        if (getStyle() == SHAPE_CIRCLE) {
            final float circleRadius = Math.min(dest.width(), dest.height()) / 2f - strokeWidth / 2;
            canvas.drawCircle(dest.centerX(), dest.centerY(), circleRadius, mBorderPaint);
        } else {
            final float radius = getCalculatedCornerRadius();
            final float inset = mStrokeWidth / 2;
            dest.inset(inset, inset);
            canvas.drawRoundRect(dest, radius, radius, mBorderPaint);
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
        ViewSupport.setOutlineProvider(this, new CircularOutlineProvider());
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
        if (useOutline()) return;
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
        paint.setColor(0xFF000000 | mBackgroundPaint.getColor());
        paint.setShadowLayer(radius, 0, radius * 1.5f / 2, SHADOW_START_COLOR);
        final RectF rect = new RectF(radius, radius, size - radius, size - radius);
        if (getStyle() == SHAPE_CIRCLE) {
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

    @IntDef({SHAPE_CIRCLE, SHAPE_RECTANGLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShapeStyle {
    }

    private static class CircularOutlineProvider extends ViewOutlineProviderCompat {
        @Override
        public void getOutline(View view, OutlineCompat outline) {
            final int viewWidth = view.getWidth(), viewHeight = view.getHeight();
            final int contentLeft = view.getPaddingLeft(), contentTop = view.getPaddingTop(),
                    contentRight = viewWidth - view.getPaddingRight(),
                    contentBottom = viewHeight - view.getPaddingBottom();
            final ShapedImageView imageView = (ShapedImageView) view;
            if (imageView.getStyle() == SHAPE_CIRCLE) {
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
        }
    }
}