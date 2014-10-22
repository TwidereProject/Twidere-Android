package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by mariotaku on 14/10/20.
 */
public class MainFrameLayout extends FrameLayout {
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
    protected boolean fitSystemWindows(Rect insets) {
        final Context context = getContext();
        if (context instanceof FitSystemWindowsCallback) {
            ((FitSystemWindowsCallback) context).fitSystemWindows(insets);
        }
        return super.fitSystemWindows(insets);
    }


    public static interface FitSystemWindowsCallback {
        void fitSystemWindows(Rect insets);
    }
}
