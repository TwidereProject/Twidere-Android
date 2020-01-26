package org.mariotaku.twidere.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import org.mariotaku.twidere.R;

public class ProfileBannerSpace extends View {

    private int mStatusBarHeight, mToolbarHeight;
    private float mBannerAspectRatio;

    /**
     * {@inheritDoc}
     */
    public ProfileBannerSpace(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProfileBannerSpace);
        setBannerAspectRatio(a.getFraction(R.styleable.ProfileBannerSpace_bannerAspectRatio, 1, 1, 2f));
        a.recycle();
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
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = Math.round(width / mBannerAspectRatio) - mStatusBarHeight - mToolbarHeight;
        setMeasuredDimension(width, height);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    public void setBannerAspectRatio(final float bannerAspectRatio) {
        mBannerAspectRatio = bannerAspectRatio;
    }

    public float getBannerAspectRatio() {
        return mBannerAspectRatio;
    }
}
