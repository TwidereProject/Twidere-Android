package org.mariotaku.twidere.graphic.like;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.TwidereActionMenuItemView;
import android.util.Property;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import org.mariotaku.twidere.graphic.iface.DoNotWrapDrawable;
import org.mariotaku.twidere.graphic.like.layer.AnimationLayerDrawable;
import org.mariotaku.twidere.graphic.like.layer.CircleLayerDrawable;
import org.mariotaku.twidere.graphic.like.layer.ParticleLayerDrawable;
import org.mariotaku.twidere.graphic.like.layer.ScalableDrawable;
import org.mariotaku.twidere.graphic.like.layer.ShineLayerDrawable;
import org.mariotaku.twidere.graphic.like.palette.FavoritePalette;
import org.mariotaku.twidere.graphic.like.palette.LikePalette;

import java.lang.ref.WeakReference;

/**
 * Created by mariotaku on 15/11/4.
 */
public class LikeAnimationDrawable extends Drawable implements Animatable, Drawable.Callback,
        DoNotWrapDrawable, TwidereActionMenuItemView.IgnoreTinting {

    @NonNull
    private LikeAnimationState mState;
    private boolean mMutated;

    public LikeAnimationDrawable(final Drawable icon, @ColorInt final int defaultColor,
                                 @ColorInt final int activatedColor,
                                 @Style final int style) {
        mState = new LikeAnimationState(icon, defaultColor, activatedColor, style, this);
    }


    LikeAnimationDrawable(@NonNull LikeAnimationState state) {
        mState = state;
    }

    @Override
    public void start() {
        if (mState.mCurrentAnimator != null) return;

        final AnimatorSet animatorSet = new AnimatorSet();

        final AnimationLayerDrawable particleLayer = mState.mParticleLayer;
        final AnimationLayerDrawable circleLayer = mState.mCircleLayer;
        final ScalableDrawable iconLayer = mState.mIconDrawable;

        switch (mState.mStyle) {
            case Style.LIKE: {
                setupLikeAnimation(animatorSet, particleLayer, circleLayer, iconLayer);
                break;
            }
            case Style.FAVORITE: {
                setupFavoriteAnimation(animatorSet, particleLayer, circleLayer, iconLayer);
                break;
            }
        }


        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                resetState();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mState.mCurrentAnimator = null;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mState.mCurrentAnimator = null;
                if (mState.mListenerRef == null) return;
                final OnLikedListener listener = mState.mListenerRef.get();
                if (listener == null) return;
                if (!listener.onLiked()) {
                    resetState();
                }
            }

            private void resetState() {
                setColorFilter(mState.mDefaultColor, PorterDuff.Mode.SRC_ATOP);
                particleLayer.setProgress(-1);
            }
        });
        animatorSet.start();
        mState.mCurrentAnimator = animatorSet;
    }

    private void setupFavoriteAnimation(final AnimatorSet animatorSet, final Layer particleLayer,
                                        final Layer circleLayer, final ScalableDrawable iconLayer) {
        setupLikeAnimation(animatorSet, particleLayer, circleLayer, iconLayer);
    }

    private void setupLikeAnimation(final AnimatorSet animatorSet, final Layer particleLayer,
                                    final Layer circleLayer, final ScalableDrawable iconLayer) {
        final long duration = mState.mDuration;
        final long scaleDownDuration = Math.round(1f / 24f * duration);
        final long ovalExpandDuration = Math.round(4f / 24f * duration);
        final long iconExpandOffset = Math.round(6f / 24f * duration);
        final long iconExpandDuration = Math.round(8f / 24f * duration);
        final long iconNormalDuration = Math.round(4f / 24f * duration);
        final long particleExpandDuration = Math.round(12f / 24f * duration);
        final long circleExplodeDuration = Math.round(5f / 24f * duration);

        final ObjectAnimator iconScaleDown = ObjectAnimator.ofFloat(iconLayer, IconScaleProperty.SINGLETON, 1, 0);
        iconScaleDown.setDuration(scaleDownDuration);
        iconScaleDown.setInterpolator(new AccelerateInterpolator(2));
        iconScaleDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setColorFilter(mState.mDefaultColor, PorterDuff.Mode.SRC_ATOP);
            }

        });

        final ObjectAnimator ovalExpand = ObjectAnimator.ofFloat(circleLayer, LayerProgressProperty.SINGLETON, 0, 0.5f);
        ovalExpand.setDuration(ovalExpandDuration);


        final ObjectAnimator iconExpand = ObjectAnimator.ofFloat(iconLayer, IconScaleProperty.SINGLETON, 0, 1.25f);
        iconExpand.setDuration(iconExpandDuration);
        iconExpand.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setColorFilter(mState.mActivatedColor, PorterDuff.Mode.SRC_ATOP);
            }

        });

        final ObjectAnimator particleExplode = ObjectAnimator.ofFloat(particleLayer, LayerProgressProperty.SINGLETON, 0, 0.5f);
        particleExplode.setDuration(iconExpandDuration);

        final ObjectAnimator iconNormal = ObjectAnimator.ofFloat(iconLayer, IconScaleProperty.SINGLETON, 1.25f, 1);
        iconNormal.setDuration(iconNormalDuration);
        final ObjectAnimator circleExplode = ObjectAnimator.ofFloat(circleLayer, LayerProgressProperty.SINGLETON, 0.5f, 0.95f, 0.95f, 1);
        circleExplode.setDuration(circleExplodeDuration);
        circleExplode.setInterpolator(new DecelerateInterpolator());


        final ObjectAnimator particleFade = ObjectAnimator.ofFloat(particleLayer, LayerProgressProperty.SINGLETON, 0.5f, 1);
        particleFade.setDuration(particleExpandDuration);


        animatorSet.play(iconScaleDown);
        animatorSet.play(ovalExpand).after(iconScaleDown);
        animatorSet.play(iconExpand).after(iconExpandOffset);
        animatorSet.play(particleExplode).after(iconExpandOffset);
        animatorSet.play(circleExplode).after(iconExpandOffset);

        animatorSet.play(iconNormal).after(iconExpand);
        animatorSet.play(particleFade).after(iconExpand);
    }

    @Override
    public void stop() {
        if (mState.mCurrentAnimator == null) return;
        mState.mCurrentAnimator.cancel();
    }

    @Override
    public boolean isRunning() {
        return mState.mCurrentAnimator != null && mState.mCurrentAnimator.isRunning();
    }

    public long getDuration() {
        return mState.mDuration;
    }

    public void setDuration(long duration) {
        mState.mDuration = duration;
    }

    public void setOnLikedListener(OnLikedListener listener) {
        mState.mListenerRef = new WeakReference<>(listener);
    }

    @Override
    public int getIntrinsicWidth() {
        return mState.mIconDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mState.mIconDrawable.getIntrinsicHeight();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mState.mCircleLayer.draw(canvas);
        mState.mParticleLayer.draw(canvas);
        mState.mIconDrawable.draw(canvas);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        mState.setBounds(left, top, right, bottom);
    }

    @Override
    public void setAlpha(int alpha) {
        mState.mIconDrawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mState.setIconColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        unscheduleSelf(what);
    }

    @Override
    public ConstantState getConstantState() {
        return mState;
    }

    @Override
    public int getChangingConfigurations() {
        return super.getChangingConfigurations() | mState.getChangingConfigurations();
    }

    @NonNull
    @Override
    public Drawable mutate() {
        if (!mMutated && super.mutate() == this) {
            mState = new LikeAnimationState(mState, this);
            mMutated = true;
        }
        return this;
    }

    public interface OnLikedListener {
        boolean onLiked();
    }

    @IntDef({Style.LIKE, Style.FAVORITE})
    public @interface Style {
        int LIKE = 1;
        int FAVORITE = 2;
    }

    static class LikeAnimationState extends ConstantState {

        // Default values
        private final int mDefaultColor;
        private final int mActivatedColor;
        @Style
        private final int mStyle;
        // Layers
        private final AnimationLayerDrawable mCircleLayer;
        private final AnimationLayerDrawable mParticleLayer;
        private final ScalableDrawable mIconDrawable;

        private long mDuration = 500;

        private AnimatorSet mCurrentAnimator;
        private WeakReference<OnLikedListener> mListenerRef;

        public LikeAnimationState(final Drawable icon, final int defaultColor, final int activatedColor,
                                  @Style final int style, Callback callback) {
            mDefaultColor = defaultColor;
            mActivatedColor = activatedColor;
            mStyle = style;

            final int intrinsicWidth = icon.getIntrinsicWidth();
            final int intrinsicHeight = icon.getIntrinsicHeight();
            mIconDrawable = new ScalableDrawable(icon);
            setIconColorFilter(new PorterDuffColorFilter(defaultColor, PorterDuff.Mode.SRC_ATOP));
            final Palette palette;
            switch (style) {
                case Style.FAVORITE: {
                    palette = new FavoritePalette();
                    mParticleLayer = new ShineLayerDrawable(intrinsicWidth, intrinsicHeight,
                            palette);
                    break;
                }
                case Style.LIKE: {
                    palette = new LikePalette();
                    mParticleLayer = new ParticleLayerDrawable(intrinsicWidth, intrinsicHeight,
                            palette);
                    break;
                }
                default: {
                    throw new IllegalArgumentException();
                }
            }
            mParticleLayer.setProgress(-1);
            mCircleLayer = new CircleLayerDrawable(intrinsicWidth, intrinsicHeight, palette);

            mIconDrawable.setCallback(callback);
            mParticleLayer.setCallback(callback);
            mCircleLayer.setCallback(callback);
        }

        public LikeAnimationState(LikeAnimationState state, LikeAnimationDrawable owner) {
            mDefaultColor = state.mDefaultColor;
            mActivatedColor = state.mActivatedColor;
            mStyle = state.mStyle;
            mCircleLayer = (AnimationLayerDrawable) clone(state.mCircleLayer, owner);
            mParticleLayer = (AnimationLayerDrawable) clone(state.mParticleLayer, owner);
            mIconDrawable = (ScalableDrawable) clone(state.mIconDrawable, owner);
        }

        public void setIconColorFilter(ColorFilter cf) {
            mIconDrawable.setColorFilter(cf);
        }

        private static Drawable clone(Drawable orig, LikeAnimationDrawable owner) {
            final Drawable clone = orig.getConstantState().newDrawable();
            clone.mutate();
            clone.setCallback(owner);
            clone.setBounds(orig.getBounds());
            return clone;
        }

        public void setBounds(int left, int top, int right, int bottom) {
            mCircleLayer.setBounds(left, top, right, bottom);
            mParticleLayer.setBounds(left, top, right, bottom);
            mIconDrawable.setBounds(left, top, right, bottom);
        }

        @NonNull
        @Override
        public Drawable newDrawable() {
            return new LikeAnimationDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return mCircleLayer.getChangingConfigurations() |
                    mParticleLayer.getChangingConfigurations() |
                    mIconDrawable.getChangingConfigurations();
        }
    }

    static final class IconScaleProperty extends Property<ScalableDrawable, Float> {
        static final Property<ScalableDrawable, Float> SINGLETON = new IconScaleProperty();

        private IconScaleProperty() {
            super(Float.class, "icon_scale");
        }

        @Override
        public void set(ScalableDrawable object, Float value) {
            object.setScale(value);
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public Float get(ScalableDrawable object) {
            return object.getScale();
        }
    }

    static final class LayerProgressProperty extends Property<Layer, Float> {
        static final Property<Layer, Float> SINGLETON = new LayerProgressProperty();

        private LayerProgressProperty() {
            super(Float.class, "layer_progress");
        }

        @Override
        public void set(Layer object, Float value) {
            object.setProgress(value);
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public Float get(Layer object) {
            return object.getProgress();
        }
    }

    /**
     * Created by mariotaku on 16/2/22.
     */
    public interface Layer {

        float getProgress();

        void setProgress(float progress);
    }

    /**
     * Created by mariotaku on 16/2/22.
     */
    public interface Palette {
        int getParticleColor(int count, int index, float progress);

        int getCircleColor(float progress);
    }
}
