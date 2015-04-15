/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.view.helper;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

/**
 * Created by mariotaku on 14-7-30.
 */
public class PressElevateViewHelper implements Animator.AnimatorListener {

    private static final float ELEVATION = 2f;

    private final View mView;

    private Animator mCurrentAnimator;
    private AnimatorRunnable mAnimatorRunnable;

    public PressElevateViewHelper(View view) {
        mView = view;
    }


    public boolean getState() {
        return mView.isPressed();
    }

    public View getView() {
        return mView;
    }

    @Override
    public void onAnimationStart(Animator animation) {
        mCurrentAnimator = animation;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        mCurrentAnimator = null;
        final AnimatorRunnable runnable = mAnimatorRunnable;
        if (runnable != null) {
            runnable.run();
        }
        mAnimatorRunnable = null;
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        mCurrentAnimator = null;
        final AnimatorRunnable runnable = mAnimatorRunnable;
        if (runnable != null) {
            runnable.run();
        }
        mAnimatorRunnable = null;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    public void updateButtonState() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        final boolean state = getState();
        final AnimatorRunnable runnable = new AnimatorRunnable(this, state);
        if (mCurrentAnimator != null) {
            mAnimatorRunnable = runnable;
            mCurrentAnimator = null;
            return;
        }
        runnable.run();
        mAnimatorRunnable = null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class AnimatorRunnable implements Runnable {

        private final PressElevateViewHelper helper;
        private final View view;
        private final boolean state;
        private final float elevation;

        AnimatorRunnable(PressElevateViewHelper helper, boolean state) {
            this.helper = helper;
            this.state = state;
            this.view = helper.getView();
            this.elevation = view.getResources().getDisplayMetrics().density * ELEVATION;
        }

        @Override
        public void run() {
            final float from = state ? 0 : elevation;
            final float to = state ? elevation : 0;
            final ObjectAnimator translationZ = ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, from, to);
            translationZ.setDuration(200);
            translationZ.addListener(helper);
            translationZ.start();
        }
    }
}
