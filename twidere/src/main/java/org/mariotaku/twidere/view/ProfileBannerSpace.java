package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import org.mariotaku.twidere.util.ThemeUtils;

public class ProfileBannerSpace extends View {

    private final Rect mSystemWindowsInsets;
    private final int mActionBarHeight;

    /**
     * {@inheritDoc}
     */
    public ProfileBannerSpace(final Context context) {
        // noinspection NullableProblems
        this(context, null);
    }

    /**
     * {@inheritDoc}
     */
    public ProfileBannerSpace(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * {@inheritDoc}
     */
    public ProfileBannerSpace(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mSystemWindowsInsets = new Rect();
        mActionBarHeight = ThemeUtils.getActionBarHeight(context);
    }

    /**
     * Draw nothing.
     *
     * @param canvas an unused parameter.
     */
    @Override
    public void draw(@NonNull final Canvas canvas) {
    }

    @Deprecated
    @Override
    protected boolean fitSystemWindows(@NonNull Rect insets) {
        mSystemWindowsInsets.set(insets);
        return super.fitSystemWindows(insets);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int insetsTop = mSystemWindowsInsets.top;
        final int top = insetsTop <= 0 || insetsTop < mActionBarHeight ? insetsTop + mActionBarHeight : insetsTop;
        final int width = MeasureSpec.getSize(widthMeasureSpec), height = width / 2 - top;
        setMeasuredDimension(width, height);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

}
