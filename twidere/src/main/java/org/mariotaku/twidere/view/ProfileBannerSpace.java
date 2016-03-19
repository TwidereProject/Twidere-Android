package org.mariotaku.twidere.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

public class ProfileBannerSpace extends View {

    private int mStatusBarHeight, mToolbarHeight;

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
    }

    /**
     * Draw nothing.
     *
     * @param canvas an unused parameter.
     */
    @SuppressLint("MissingSuperCall")
    @Override
    public void draw(@NonNull final Canvas canvas) {
    }

    public void setStatusBarHeight(int offset) {
        mStatusBarHeight = offset;
        requestLayout();
    }

    public void setToolbarHeight(int toolbarHeight) {
        mToolbarHeight = toolbarHeight;
        requestLayout();
    }

    public int getStatusBarHeight() {
        return mStatusBarHeight;
    }

    public int getToolbarHeight() {
        return mToolbarHeight;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec), height = width / 2
                - mStatusBarHeight - mToolbarHeight;
        setMeasuredDimension(width, height);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

}
