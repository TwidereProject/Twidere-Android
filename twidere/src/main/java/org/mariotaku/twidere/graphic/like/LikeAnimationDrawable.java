package org.mariotaku.twidere.graphic.like;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.util.Property;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import org.mariotaku.twidere.graphic.iface.DoNotWrapDrawable;
import org.mariotaku.twidere.graphic.like.layer.AnimationLayerDrawable;
import org.mariotaku.twidere.graphic.like.layer.CircleLayerDrawable;
import org.mariotaku.twidere.graphic.like.layer.IconLayerDrawable;
import org.mariotaku.twidere.graphic.like.layer.Layer;
import org.mariotaku.twidere.graphic.like.layer.ParticleLayerDrawable;
import org.mariotaku.twidere.graphic.like.layer.ShineLayerDrawable;
import org.mariotaku.twidere.graphic.like.palette.FavoritePalette;
import org.mariotaku.twidere.graphic.like.palette.LikePalette;
import org.mariotaku.twidere.graphic.like.palette.Palette;

import java.lang.ref.WeakReference;

/**
 * Created by mariotaku on 15/11/4.
 */
public class LikeAnimationDrawable extends Drawable implements Animatable, Drawable.Callback, DoNotWrapDrawable {

    private static final Property<IconLayerDrawable, Float> ICON_SCALE = new Property<IconLayerDrawable, Float>(Float.class, "icon_scale") {
        @Override
        public void set(IconLayerDrawable object, Float value) {
            object.setScale(value);
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public Float get(IconLayerDrawable object) {
            return object.getScale();
        }
    };
    private static final Property<Layer, Float> LAYER_PROGRESS = new Property<Layer, Float>(Float.class, "layer_progress") {
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
    };

    private LikeAnimationState mState;
    private boolean mMutated;

    public LikeAnimationDrawable(final Context context, final int likeIcon, final int defaultColor,
                                 final int likeColor, @Style final int style) {
        mState = new LikeAnimationState(context, likeIcon, defaultColor, likeColor, style, this);
    }


    public LikeAnimationDrawable(LikeAnimationState state) {
        mState = state;
    }

    @Override
    public void start() {
        if (mState.mCurrentAnimator != null) return;

        final AnimatorSet animatorSet = new AnimatorSet();

        final AnimationLayerDrawable particleLayer = mState.mParticleLayer;
        final AnimationLayerDrawable circleLayer = mState.mCircleLayer;
        final IconLayerDrawable iconLayer = mState.mIconLayer;

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
                iconLayer.setColorFilter(mState.mDefaultColor, PorterDuff.Mode.SRC_ATOP);
                particleLayer.setProgress(-1);
            }
        });
        animatorSet.start();
        mState.mCurrentAnimator = animatorSet;
    }

    private void setupFavoriteAnimation(final AnimatorSet animatorSet, final Layer particleLayer,
                                        final Layer circleLayer, final IconLayerDrawable iconLayer) {
        setupLikeAnimation(animatorSet, particleLayer, circleLayer, iconLayer);
    }

    private void setupLikeAnimation(final AnimatorSet animatorSet, final Layer particleLayer,
                                    final Layer circleLayer, final IconLayerDrawable iconLayer) {
        final long duration = mState.mDuration;
        final long scaleDownDuration = Math.round(1f / 24f * duration);
        final long ovalExpandDuration = Math.round(4f / 24f * duration);
        final long iconExpandOffset = Math.round(6f / 24f * duration);
        final long iconExpandDuration = Math.round(8f / 24f * duration);
        final long iconNormalDuration = Math.round(4f / 24f * duration);
        final long particleExpandDuration = Math.round(12f / 24f * duration);
        final long circleExplodeDuration = Math.round(5f / 24f * duration);

        final ObjectAnimator iconScaleDown = ObjectAnimator.ofFloat(iconLayer, ICON_SCALE, 1, 0);
        iconScaleDown.setDuration(scaleDownDuration);
        iconScaleDown.setInterpolator(new AccelerateInterpolator(2));
        iconScaleDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                iconLayer.setColorFilter(mState.mDefaultColor, PorterDuff.Mode.SRC_ATOP);
            }

        });

        final ObjectAnimator ovalExpand = ObjectAnimator.ofFloat(circleLayer, LAYER_PROGRESS, 0, 0.5f);
        ovalExpand.setDuration(ovalExpandDuration);


        final ObjectAnimator iconExpand = ObjectAnimator.ofFloat(iconLayer, ICON_SCALE, 0, 1.25f);
        iconExpand.setDuration(iconExpandDuration);
        iconExpand.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                iconLayer.setColorFilter(mState.mLikeColor, PorterDuff.Mode.SRC_ATOP);
            }

        });

        final ObjectAnimator particleExplode = ObjectAnimator.ofFloat(particleLayer, LAYER_PROGRESS, 0, 0.5f);
        particleExplode.setDuration(iconExpandDuration);

        final ObjectAnimator iconNormal = ObjectAnimator.ofFloat(iconLayer, ICON_SCALE, 1.25f, 1);
        iconNormal.setDuration(iconNormalDuration);
        final ObjectAnimator circleExplode = ObjectAnimator.ofFloat(circleLayer, LAYER_PROGRESS, 0.5f, 0.95f, 0.95f, 1);
        circleExplode.setDuration(circleExplodeDuration);
        circleExplode.setInterpolator(new DecelerateInterpolator());


        final ObjectAnimator particleFade = ObjectAnimator.ofFloat(particleLayer, LAYER_PROGRESS, 0.5f, 1);
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
        return mState.mIconLayer.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mState.mIconLayer.getIntrinsicHeight();
    }

    @Override
    public void draw(Canvas canvas) {
        mState.mCircleLayer.draw(canvas);
        mState.mParticleLayer.draw(canvas);
        mState.mIconLayer.draw(canvas);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        mState.setBounds(left, top, right, bottom);
    }

    @Override
    public void setAlpha(int alpha) {
        mState.mIconLayer.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mState.mIconLayer.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }

    @Override
    public ConstantState getConstantState() {
        return mState;
    }

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
        private final int mLikeColor;
        @Style
        private final int mStyle;
        // Layers
        private final AnimationLayerDrawable mCircleLayer;
        private final AnimationLayerDrawable mParticleLayer;
        private final IconLayerDrawable mIconLayer;

        private long mDuration = 500;

        private AnimatorSet mCurrentAnimator;
        private WeakReference<OnLikedListener> mListenerRef;

        public LikeAnimationState(final Context context, final int likeIcon, final int defaultColor,
                                  final int likeColor, @Style final int style, Callback callback) {
            mDefaultColor = defaultColor;
            mLikeColor = likeColor;
            mStyle = style;

            mIconLayer = new IconLayerDrawable(ContextCompat.getDrawable(context, likeIcon));
            mIconLayer.setColorFilter(defaultColor, PorterDuff.Mode.SRC_ATOP);
            final Palette palette;
            switch (style) {
                case Style.FAVORITE: {
                    palette = new FavoritePalette();
                    mParticleLayer = new ShineLayerDrawable(mIconLayer.getIntrinsicWidth(),
                            mIconLayer.getIntrinsicHeight(), palette);
                    break;
                }
                case Style.LIKE: {
                    palette = new LikePalette();
                    mParticleLayer = new ParticleLayerDrawable(mIconLayer.getIntrinsicWidth(),
                            mIconLayer.getIntrinsicHeight(), palette);
                    break;
                }
                default: {
                    throw new IllegalArgumentException();
                }
            }
            mParticleLayer.setProgress(-1);
            mCircleLayer = new CircleLayerDrawable(mIconLayer.getIntrinsicWidth(), mIconLayer.getIntrinsicHeight(), palette);

            mIconLayer.setCallback(callback);
            mParticleLayer.setCallback(callback);
            mCircleLayer.setCallback(callback);
        }

        public LikeAnimationState(LikeAnimationState state, LikeAnimationDrawable owner) {
            mDefaultColor = state.mDefaultColor;
            mLikeColor = state.mLikeColor;
            mStyle = state.mStyle;
            mCircleLayer = (AnimationLayerDrawable) clone(state.mCircleLayer, owner);
            mParticleLayer = (AnimationLayerDrawable) clone(state.mParticleLayer, owner);
            mIconLayer = (IconLayerDrawable) clone(state.mIconLayer, owner);
        }

        private Drawable clone(Drawable orig, LikeAnimationDrawable owner) {
            final Drawable clone = orig.getConstantState().newDrawable();
            clone.setCallback(owner);
            clone.setBounds(orig.getBounds());
            return clone;
        }

        public void setBounds(int left, int top, int right, int bottom) {
            mCircleLayer.setBounds(left, top, right, bottom);
            mParticleLayer.setBounds(left, top, right, bottom);
            mIconLayer.setBounds(left, top, right, bottom);
        }

        @Override
        public Drawable newDrawable() {
            return new LikeAnimationDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }
    }

}
