package org.mariotaku.twidere.graphic.like.state;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import org.mariotaku.twidere.graphic.like.layer.ParticleLayerDrawable;
import org.mariotaku.twidere.graphic.like.palette.Palette;

/**
 * Created by mariotaku on 16/2/22.
 */
public class ParticleLayerState extends AbsLayerState {

    private final Paint mPaint;
    private float mFullRadius;
    private float mParticleSize;

    public ParticleLayerState(int intrinsicWidth, int intrinsicHeight, Palette palette) {
        super(intrinsicWidth, intrinsicHeight, palette);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        setProgress(-1);
    }

    @Override
    public Drawable newDrawable() {
        return new ParticleLayerDrawable(mIntrinsicWidth, mIntrinsicHeight, mPalette);
    }

    @Override
    public int getChangingConfigurations() {
        return 0;
    }

    public float getFullRadius() {
        return mFullRadius;
    }

    public float getParticleSize() {
        return mParticleSize;
    }

    public Paint getPaint() {
        return mPaint;
    }

    public void setFullRadius(int fullRadius) {
        mFullRadius = fullRadius;
        mParticleSize = fullRadius / 4f;
    }
}
