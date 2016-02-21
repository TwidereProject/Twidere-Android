package org.mariotaku.twidere.graphic.like.state;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import org.mariotaku.twidere.graphic.like.layer.ShineLayerDrawable;
import org.mariotaku.twidere.graphic.like.palette.Palette;

/**
 * Created by mariotaku on 16/2/22.
 */
public class ShineLayerState extends AbsLayerState {

    private final Paint mPaint;
    private int mFullRadius;
    private float mLineWidth;

    public ShineLayerState(int intrinsicWidth, int intrinsicHeight, Palette palette) {
        super(intrinsicWidth, intrinsicHeight, palette);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        setProgress(-1);
    }

    public Paint getPaint() {
        return mPaint;
    }

    @Override
    public Drawable newDrawable() {
        return new ShineLayerDrawable(mIntrinsicWidth, mIntrinsicHeight, mPalette);
    }

    @Override
    public int getChangingConfigurations() {
        return 0;
    }

    public int getFullRadius() {
        return mFullRadius;
    }

    public void setFullRadius(int fullRadius) {
        mFullRadius = fullRadius;
        mLineWidth = fullRadius / 10f;
    }

    public float getLineWidth() {
        return mLineWidth;
    }

}
