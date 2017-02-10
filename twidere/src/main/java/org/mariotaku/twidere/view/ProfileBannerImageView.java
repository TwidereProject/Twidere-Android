/*
 * 				Twidere - Twitter client for Android
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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.iface.IExtendedView;

public class ProfileBannerImageView extends ForegroundImageView implements IExtendedView {

    private OnSizeChangedListener mOnSizeChangedListener;
    private TouchInterceptor mTouchInterceptor;
    private OnFitSystemWindowsListener mOnFitSystemWindowsListener;
    private float mBannerAspectRatio;

    public ProfileBannerImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProfileBannerImageView);
        setBannerAspectRatio(a.getFraction(R.styleable.ProfileBannerImageView_bannerAspectRatio, 1, 1, 2f));
        a.recycle();
        setScaleType(ScaleType.CENTER_CROP);
    }

    @Override
    public void setOnFitSystemWindowsListener(OnFitSystemWindowsListener listener) {
        mOnFitSystemWindowsListener = listener;
    }

    @Override
    public final void setOnSizeChangedListener(final OnSizeChangedListener listener) {
        mOnSizeChangedListener = listener;
    }

    @Override
    public final void setTouchInterceptor(final TouchInterceptor listener) {
        mTouchInterceptor = listener;
    }

    @Override
    @Deprecated
    protected boolean fitSystemWindows(@NonNull Rect insets) {
        if (mOnFitSystemWindowsListener != null) {
            mOnFitSystemWindowsListener.onFitSystemWindows(insets);
        }
        return super.fitSystemWindows(insets);
    }

    @Override
    public final boolean dispatchTouchEvent(@NonNull final MotionEvent event) {
        if (mTouchInterceptor != null) {
            final boolean ret = mTouchInterceptor.dispatchTouchEvent(this, event);
            if (ret) return true;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public final boolean onTouchEvent(@NonNull final MotionEvent event) {
        if (mTouchInterceptor != null) {
            final boolean ret = mTouchInterceptor.onTouchEvent(this, event);
            if (ret) return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec),
                height = Math.round(width / mBannerAspectRatio);
        setMeasuredDimension(width, height);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    protected final void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mOnSizeChangedListener != null) {
            mOnSizeChangedListener.onSizeChanged(this, w, h, oldw, oldh);
        }
    }

    public void setBannerAspectRatio(final float bannerAspectRatio) {
        mBannerAspectRatio = bannerAspectRatio;
    }

    public float getBannerAspectRatio() {
        return mBannerAspectRatio;
    }
}
