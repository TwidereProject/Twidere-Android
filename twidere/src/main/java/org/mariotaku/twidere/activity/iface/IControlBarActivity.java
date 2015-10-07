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

        private static final long DURATION = 200l;

        private final IControlBarActivity mActivity;
        private int mControlAnimationDirection;

        public ControlBarShowHideHelper(IControlBarActivity activity) {
            mActivity = activity;
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
            if (mControlAnimationDirection != 0) return;
            final ObjectAnimator animator;
            final float offset = mActivity.getControlBarOffset();
            if (visible) {
                if (offset >= 1) return;
                animator = ObjectAnimator.ofFloat(mActivity, ControlBarOffsetProperty.SINGLETON, offset, 1);
            } else {
                if (offset <= 0) return;
                animator = ObjectAnimator.ofFloat(mActivity, ControlBarOffsetProperty.SINGLETON, offset, 0);
            }
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mControlAnimationDirection = 0;
                    if (listener != null) {
                        listener.onControlBarVisibleAnimationFinish(visible);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mControlAnimationDirection = 0;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.setDuration(DURATION);
            animator.start();
            mControlAnimationDirection = visible ? 1 : -1;
        }
    }
}
