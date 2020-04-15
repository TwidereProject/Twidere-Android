package org.mariotaku.twidere.graphic.like.layer;

import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable;

/**
 * Created by mariotaku on 16/2/22.
 */
public abstract class AnimationLayerDrawable extends Drawable implements LikeAnimationDrawable.Layer {

    protected AnimationLayerState mState;
    private boolean mMutated;

    public AnimationLayerDrawable(final int intrinsicWidth, final int intrinsicHeight, final LikeAnimationDrawable.Palette palette) {
        mState = createConstantState(intrinsicWidth, intrinsicHeight, palette);
    }

    protected abstract AnimationLayerState createConstantState(int intrinsicWidth, int intrinsicHeight, final LikeAnimationDrawable.Palette palette);

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

    @NonNull
    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mState = createConstantState(mState.getIntrinsicWidth(), mState.getIntrinsicHeight(),
                    mState.getPalette());
            mMutated = true;
        }
        return this;
    }

    /**
     * Created by mariotaku on 16/2/22.
     */
    public abstract static class AnimationLayerState extends ConstantState {
        protected final int mIntrinsicWidth;
        protected final int mIntrinsicHeight;
        protected final LikeAnimationDrawable.Palette mPalette;

        private float mProgress;

        public AnimationLayerState(int intrinsicWidth, int intrinsicHeight, LikeAnimationDrawable.Palette palette) {
            this.mPalette = palette;
            this.mIntrinsicHeight = intrinsicHeight;
            this.mIntrinsicWidth = intrinsicWidth;
        }

        public final float getProgress() {
            return mProgress;
        }

        public final void setProgress(float progress) {
            mProgress = progress;
        }

        public final LikeAnimationDrawable.Palette getPalette() {
            return mPalette;
        }

        public final int getIntrinsicWidth() {
            return mIntrinsicWidth;
        }

        public final int getIntrinsicHeight() {
            return mIntrinsicHeight;
        }
    }
}
