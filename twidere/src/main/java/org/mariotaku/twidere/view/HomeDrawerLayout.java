package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonUtils;
import org.mariotaku.chameleon.ChameleonView;
import org.mariotaku.twidere.util.support.WindowSupport;

public class HomeDrawerLayout extends DrawerLayout implements ChameleonView, ChameleonView.StatusBarThemeable {

    private ShouldDisableDecider mShouldDisableDecider;
    private int mStartLockMode, mEndLockMode;

    public HomeDrawerLayout(Context context) {
        super(context);
    }

    public HomeDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setShouldDisableDecider(ShouldDisableDecider shouldDisableDecider) {
        mShouldDisableDecider = shouldDisableDecider;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mStartLockMode = getDrawerLockMode(GravityCompat.START);
                mEndLockMode = getDrawerLockMode(GravityCompat.END);
                if (isDrawerOpen(GravityCompat.START) || isDrawerOpen(GravityCompat.END)) {
                    // Opened, disable close if requested
                    if (mShouldDisableDecider != null && mShouldDisableDecider.shouldDisableTouch(ev)) {
                        setDrawerLockMode(LOCK_MODE_LOCKED_OPEN, GravityCompat.START);
                        setDrawerLockMode(LOCK_MODE_LOCKED_OPEN, GravityCompat.END);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                setDrawerLockMode(mStartLockMode, GravityCompat.START);
                setDrawerLockMode(mEndLockMode, GravityCompat.END);
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean isPostApplyTheme() {
        return false;
    }

    @Nullable
    @Override
    public Appearance createAppearance(Context context, AttributeSet attributeSet, Chameleon.Theme theme) {
        Appearance appearance = new Appearance();
        WindowSupport.setStatusBarColor(ChameleonUtils.getActivity(context).getWindow(), Color.TRANSPARENT);
        appearance.setStatusBarBackgroundColor(ChameleonUtils.darkenColor(theme.getColorToolbar()));
        return appearance;
    }

    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        Appearance a = (Appearance) appearance;
        setStatusBarBackgroundColor(a.getStatusBarBackgroundColor());
    }

    @Override
    public boolean isStatusBarColorHandled() {
        return true;
    }

    public static class Appearance implements ChameleonView.Appearance {
        int statusBarBackgroundColor;

        public int getStatusBarBackgroundColor() {
            return statusBarBackgroundColor;
        }

        public void setStatusBarBackgroundColor(int statusBarBackgroundColor) {
            this.statusBarBackgroundColor = statusBarBackgroundColor;
        }
    }

    public interface ShouldDisableDecider {
        boolean shouldDisableTouch(MotionEvent e);
    }
}
