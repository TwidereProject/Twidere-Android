package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ProfileBannerSpace extends View {

    private final Rect mSystemWindowsInsets;

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
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        mSystemWindowsInsets.set(insets);
        return super.fitSystemWindows(insets);
    }

    /**
     * Draw nothing.
     *
     * @param canvas an unused parameter.
     */
    @Override
    public void draw(final Canvas canvas) {
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec), height = width / 2 - mSystemWindowsInsets.top;
        setMeasuredDimension(width, height);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

}
