package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by mariotaku on 14/10/20.
 */
public class MainFrameLayout extends TintedStatusFrameLayout {
    public MainFrameLayout(Context context) {
        super(context);
    }

    public MainFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
    }

    @Override
    public void setStatusBarHeight(int height) {
        setPadding(0, height, 0, 0);
        super.setStatusBarHeight(height);
    }

}
