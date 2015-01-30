package org.mariotaku.twidere.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.jeremyfeinstein.slidingmenu.lib.CustomViewBehind;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.support.HomeActivity;

/**
 * Created by mariotaku on 14/10/21.
 */
public class HomeSlidingMenu extends SlidingMenu implements Constants {

    private final HomeActivity mActivity;

    public HomeSlidingMenu(final Context context) {
        this(context, null);
    }

    public HomeSlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeSlidingMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mActivity = (HomeActivity) context;
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull final MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                final boolean isTouchingMargin = isTouchingMargin(ev);
                setTouchModeAbove(isTouchingMargin ? TOUCHMODE_MARGIN : TOUCHMODE_FULLSCREEN);
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        mActivity.setSystemWindowInsets(insets);
        return false;
    }

    @Override
    protected CustomViewBehind newCustomViewBehind(final Context context) {
        if (isInEditMode()) return super.newCustomViewBehind(context);
        return new MyCustomViewBehind(context, this);
    }

    private ViewPager getViewPager() {
        if (mActivity == null) return null;
        return mActivity.getViewPager();
    }

    private boolean isTouchingMargin(final MotionEvent e) {
        final float x = e.getX(), marginThreshold = getTouchmodeMarginThreshold();
        final View content = getContent();
        final int mode = getMode(), left = content.getLeft(), right = content.getRight();
        if (mode == SlidingMenu.LEFT)
            return x >= left && x <= marginThreshold + left;
        else if (mode == SlidingMenu.RIGHT)
            return x <= right && x >= right - marginThreshold;
        else if (mode == SlidingMenu.LEFT_RIGHT)
            return x >= left && x <= marginThreshold + left || x <= right && x >= right - marginThreshold;
        return false;
    }

    @SuppressLint("ViewConstructor")
    private static class MyCustomViewBehind extends CustomViewBehind {

        private final HomeSlidingMenu mSlidingMenu;

        public MyCustomViewBehind(final Context context, final HomeSlidingMenu slidingMenu) {
            super(context);
            mSlidingMenu = slidingMenu;
        }

        @Override
        public boolean menuClosedSlideAllowed(final float dx) {
            if (mSlidingMenu.getTouchModeAbove() != SlidingMenu.TOUCHMODE_FULLSCREEN)
                return super.menuClosedSlideAllowed(dx);
            final ViewPager viewPager = mSlidingMenu.getViewPager();
            if (viewPager == null) return false;
            final boolean canScrollHorizontally = viewPager.canScrollHorizontally(Math.round(-dx));
            final int mode = getMode();
            if (mode == SlidingMenu.LEFT)
                return dx > 0 && !canScrollHorizontally;
            else if (mode == SlidingMenu.RIGHT)
                return dx < 0 && !canScrollHorizontally;
            else if (mode == SlidingMenu.LEFT_RIGHT) return !canScrollHorizontally;
            return false;
        }
    }

}
