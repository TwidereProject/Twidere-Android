package org.mariotaku.twidere.graphic.like.state;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import org.mariotaku.twidere.graphic.like.layer.CircleLayerDrawable;
import org.mariotaku.twidere.graphic.like.palette.Palette;

/**
 * Created by mariotaku on 16/2/22.
 */
public class CircleState extends AbsLayerState {
    private final Paint mPaint;
    private int mFullRadius;

    public CircleState(int intrinsicWidth, int intrinsicHeight, Palette palette) {
        super(intrinsicWidth, intrinsicHeight, palette);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public Drawable newDrawable() {
        return new CircleLayerDrawable(mIntrinsicWidth, mIntrinsicHeight, mPalette);
    }

    @Override
    public int getChangingConfigurations() {
        return 0;
    }

    public void setFullRadius(int fullRadius) {
        mFullRadius = fullRadius;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public int getFullRadius() {
        return mFullRadius;
    }
}
