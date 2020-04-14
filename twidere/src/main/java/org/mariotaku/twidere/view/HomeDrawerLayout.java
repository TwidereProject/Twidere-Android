package org.mariotaku.twidere.view;

import android.content.Context;
import androidx.core.view.GravityCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.mariotaku.chameleon.view.ChameleonDrawerLayout;

public class HomeDrawerLayout extends ChameleonDrawerLayout {

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


    public interface ShouldDisableDecider {
        boolean shouldDisableTouch(MotionEvent e);
    }
}
