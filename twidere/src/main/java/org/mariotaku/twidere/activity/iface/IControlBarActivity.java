package org.mariotaku.twidere.activity.iface;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.util.Property;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by mariotaku on 14/10/21.
 */
public interface IControlBarActivity {

    /**
     * @param offset 0: invisible, 1: visible
     */
    void setControlBarOffset(float offset);

    void setControlBarVisibleAnimate(boolean visible);

    void setControlBarVisibleAnimate(boolean visible, ControlBarShowHideHelper.ControlBarAnimationListener listener);

    float getControlBarOffset();

    int getControlBarHeight();

    void notifyControlBarOffsetChanged();

    void registerControlBarOffsetListener(ControlBarOffsetListener listener);

    void unregisterControlBarOffsetListener(ControlBarOffsetListener listener);

    interface ControlBarOffsetListener {
        void onControlBarOffsetChanged(IControlBarActivity activity, float offset);
    }

    final class ControlBarShowHideHelper {

        private static final long DURATION = 200L;

        private final IControlBarActivity activity;
        private int controlAnimationDirection;
        private ObjectAnimator currentControlAnimation;

        public ControlBarShowHideHelper(IControlBarActivity activity) {
            this.activity = activity;
        }

        private static class ControlBarOffsetProperty extends Property<IControlBarActivity, Float> {
            public static final ControlBarOffsetProperty SINGLETON = new ControlBarOffsetProperty();

            @Override
            public void set(IControlBarActivity object, Float value) {
                object.setControlBarOffset(value);
            }

            public ControlBarOffsetProperty() {
                super(Float.TYPE, null);
            }

            @Override
            public Float get(IControlBarActivity object) {
                return object.getControlBarOffset();
            }
        }

        public interface ControlBarAnimationListener {
            void onControlBarVisibleAnimationFinish(boolean visible);
        }

        public void setControlBarVisibleAnimate(boolean visible) {
            setControlBarVisibleAnimate(visible, null);
        }

        public void setControlBarVisibleAnimate(final boolean visible, final ControlBarAnimationListener listener) {
            final int newDirection = visible ? 1 : -1;
            if (controlAnimationDirection == newDirection) return;
            if (currentControlAnimation != null && controlAnimationDirection != 0) {
                currentControlAnimation.cancel();
                currentControlAnimation = null;
                controlAnimationDirection = newDirection;
            }
            final ObjectAnimator animator;
            final float offset = activity.getControlBarOffset();
            if (visible) {
                if (offset >= 1) return;
                animator = ObjectAnimator.ofFloat(activity, ControlBarOffsetProperty.SINGLETON, offset, 1);
            } else {
                if (offset <= 0) return;
                animator = ObjectAnimator.ofFloat(activity, ControlBarOffsetProperty.SINGLETON, offset, 0);
            }
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    controlAnimationDirection = 0;
                    currentControlAnimation = null;
                    if (listener != null) {
                        listener.onControlBarVisibleAnimationFinish(visible);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    controlAnimationDirection = 0;
                    currentControlAnimation = null;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.setDuration(DURATION);
            animator.start();
            currentControlAnimation = animator;
            controlAnimationDirection = newDirection;
        }
    }
}
