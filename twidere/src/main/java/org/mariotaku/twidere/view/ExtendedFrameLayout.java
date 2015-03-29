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
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import org.mariotaku.twidere.view.iface.IExtendedView;

public class ExtendedFrameLayout extends FrameLayout implements IExtendedView {

    private TouchInterceptor mTouchInterceptor;
    private OnSizeChangedListener mOnSizeChangedListener;
    private OnFitSystemWindowsListener mOnFitSystemWindowsListener;

    public ExtendedFrameLayout(final Context context) {
        super(context);
    }

    public ExtendedFrameLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedFrameLayout(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public final boolean dispatchTouchEvent(final MotionEvent event) {
        if (mTouchInterceptor != null) {
            final boolean ret = mTouchInterceptor.dispatchTouchEvent(this, event);
            if (ret) return true;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public final boolean onInterceptTouchEvent(final MotionEvent event) {
        if (mTouchInterceptor != null) {
            final boolean ret = mTouchInterceptor.onInterceptTouchEvent(this, event);
            if (ret) return true;
        }
        return super.onInterceptTouchEvent(event);
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
    protected boolean fitSystemWindows(@NonNull Rect insets) {
//        if (mOnFitSystemWindowsListener != null && Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
//            mOnFitSystemWindowsListener.onFitSystemWindows(insets);
//        }
        if (mOnFitSystemWindowsListener != null) {
            mOnFitSystemWindowsListener.onFitSystemWindows(insets);
        }
        return super.fitSystemWindows(insets);
    }

    Rect insets = new Rect();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        if (mOnFitSystemWindowsListener != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//            final OnFitSystemWindowsListener l = mOnFitSystemWindowsListener;
//            final Activity activity = Utils.findActivity(getContext());
//            if (activity instanceof ActionBarActivity) {
//                final ActionBarActivity actionBarActivity = (ActionBarActivity) activity;
//                insets.top = Utils.getActionBarHeight(actionBarActivity.getSupportActionBar());
//                l.onFitSystemWindows(insets);
//            } else if (activity != null && activity.getWindow().hasFeature(Window.FEATURE_ACTION_BAR
//                    | Window.FEATURE_ACTION_BAR_OVERLAY)) {
//                insets.top = Utils.getActionBarHeight(activity.getActionBar());
//                l.onFitSystemWindows(insets);
//            }
//        }

    }

    @Override
    public final boolean onTouchEvent(final MotionEvent event) {
        if (mTouchInterceptor != null) {
            final boolean ret = mTouchInterceptor.onTouchEvent(this, event);
            if (ret) return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected final void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mOnSizeChangedListener != null) {
            mOnSizeChangedListener.onSizeChanged(this, w, h, oldw, oldh);
        }
    }

}
