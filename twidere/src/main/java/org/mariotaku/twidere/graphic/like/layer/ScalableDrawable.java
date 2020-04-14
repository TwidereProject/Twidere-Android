package org.mariotaku.twidere.graphic.like.layer;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import android.view.Gravity;

/**
 * Created by mariotaku on 16/2/22.
 */
public class ScalableDrawable extends Drawable implements Drawable.Callback {
    private boolean mMutated;
    private ScaleConstantState mState;

    public ScalableDrawable(Drawable drawable) {
        if (drawable == null) throw new NullPointerException();
        mState = new ScaleConstantState(drawable, this);
        setScale(1);
    }

    ScalableDrawable(ScaleConstantState state) {
        mState = state;
    }

    /**
     * Returns the drawable scaled by this ScaleDrawable.
     */
    public Drawable getDrawable() {
        return mState.mDrawable;
    }

    // overrides from Drawable.Callback
    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        if (getCallback() != null) {
            getCallback().invalidateDrawable(this);
        }
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        if (getCallback() != null) {
            getCallback().scheduleDrawable(this, what, when);
        }
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        if (getCallback() != null) {
            getCallback().unscheduleDrawable(this, what);
        }
    }

    // overrides from Drawable
    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mState.getScale() <= 0) return;
        mState.mDrawable.draw(canvas);
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mState.getChangingConfigurations();
    }

    @Override
    public boolean getPadding(@NonNull Rect padding) {
        // XXX need to adjust padding!
        return mState.mDrawable.getPadding(padding);
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        return mState.mDrawable.setVisible(visible, restart);
    }

    @Override
    public void setAlpha(int alpha) {
        mState.mDrawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mState.mDrawable.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return mState.mDrawable.getOpacity();
    }

    @Override
    public boolean isStateful() {
        return mState.mDrawable.isStateful();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        final boolean changed = mState.mDrawable.setState(state);
        onBoundsChange(getBounds());
        return changed;
    }

    @Override
    protected boolean onLevelChange(int level) {
        mState.mDrawable.setLevel(level);
        onBoundsChange(getBounds());
        invalidateSelf();
        return true;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        updateBounds(bounds);
    }

    @Override
    public int getIntrinsicWidth() {
        return mState.mDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mState.mDrawable.getIntrinsicHeight();
    }

    @NonNull
    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mState = new ScaleConstantState(this);
            mMutated = true;
        }
        return this;
    }

    public float getScale() {
        return mState.getScale();
    }

    public void setScale(float scale) {
        mState.setScale(scale);
        updateBounds(getBounds());
    }

    @Override
    public ConstantState getConstantState() {
        return mState;
    }

    static class ScaleConstantState extends ConstantState {

        private final Drawable mDrawable;
        private float mScale;
        private final Rect mTmpRect = new Rect();

        ScaleConstantState(Drawable drawable, ScalableDrawable owner) {
            mDrawable = drawable;
            mDrawable.setCallback(owner);
        }

        ScaleConstantState(ScalableDrawable owner) {
            final ScaleConstantState state = owner.mState;
            mDrawable = state.mDrawable.getConstantState().newDrawable();
            mDrawable.mutate();
            mDrawable.setCallback(owner);
            mScale = state.getScale();
        }

        @NonNull
        @Override
        public Drawable newDrawable() {
            return new ScalableDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return mDrawable.getChangingConfigurations();
        }

        public void setScale(float scale) {
            mScale = scale;
        }

        public float getScale() {
            return mScale;
        }
    }

    private void updateBounds(Rect bounds) {
        final Rect r = mState.mTmpRect;
        final int w = Math.round(mState.mDrawable.getIntrinsicWidth() * mState.getScale());
        final int h = Math.round(mState.mDrawable.getIntrinsicHeight() * mState.getScale());
        Gravity.apply(Gravity.CENTER, w, h, bounds, r);

        if (w > 0 && h > 0) {
            mState.mDrawable.setBounds(r.left, r.top, r.right, r.bottom);
        }
        invalidateSelf();
    }

}
