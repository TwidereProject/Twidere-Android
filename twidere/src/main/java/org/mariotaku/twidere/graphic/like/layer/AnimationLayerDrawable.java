package org.mariotaku.twidere.graphic.like.layer;

import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import org.mariotaku.twidere.graphic.like.palette.Palette;
import org.mariotaku.twidere.graphic.like.state.AbsLayerState;

/**
 * Created by mariotaku on 16/2/22.
 */
public abstract class AnimationLayerDrawable<S extends AbsLayerState> extends Drawable implements Layer {

    protected S mState;
    private boolean mMutated;

    public AnimationLayerDrawable(final int intrinsicWidth, final int intrinsicHeight, final Palette palette) {
        mState = createConstantState(intrinsicWidth, intrinsicHeight, palette);
    }

    protected abstract S createConstantState(int intrinsicWidth, int intrinsicHeight, final Palette palette);

    @Override
    public void setAlpha(final int alpha) {

    }

    @Override
    public final float getProgress() {
        return mState.getProgress();
    }

    @Override
    public final void setProgress(float progress) {
        mState.setProgress(progress);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(final ColorFilter colorFilter) {

    }

    @Override
    public final int getIntrinsicHeight() {
        return mState.getIntrinsicHeight();
    }

    @Override
    public final int getIntrinsicWidth() {
        return mState.getIntrinsicWidth();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public ConstantState getConstantState() {
        return mState;
    }

    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mState = createConstantState(mState.getIntrinsicWidth(), mState.getIntrinsicHeight(),
                    mState.getPalette());
            mMutated = true;
        }
        return this;
    }

}
