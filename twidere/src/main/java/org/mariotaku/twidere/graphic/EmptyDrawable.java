package org.mariotaku.twidere.graphic;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

public class EmptyDrawable extends Drawable {

    private final int mIntrinsicWidth, mIntrinsicHeight, mMinimumWidth, mMinimumHeight;

    @Override
    public int getMinimumHeight() {
        return mMinimumHeight;
    }

    @Override
    public int getMinimumWidth() {
        return mMinimumWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }


    public EmptyDrawable() {
        this(0, 0, -1, -1);
    }

    public EmptyDrawable(int minimumWidth, int minimumHeight, int intrinsicWidth, int intrinsicHeight) {
        mMinimumWidth = minimumWidth;
        mMinimumHeight = minimumHeight;
        mIntrinsicWidth = intrinsicWidth;
        mIntrinsicHeight = intrinsicHeight;
    }

    public EmptyDrawable(Drawable drawableToCopySize) {
        mMinimumWidth = drawableToCopySize.getMinimumWidth();
        mMinimumHeight = drawableToCopySize.getMinimumHeight();
        mIntrinsicWidth = drawableToCopySize.getIntrinsicWidth();
        mIntrinsicHeight = drawableToCopySize.getIntrinsicHeight();
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public void setAlpha(final int alpha) {

    }

    @Override
    public void setColorFilter(final ColorFilter cf) {

    }

}
