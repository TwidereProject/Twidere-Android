/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.view.themed;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.mariotaku.twidere.view.iface.IExtendedView;
import org.mariotaku.twidere.view.iface.IThemeAccentView;

/**
 * Created by mariotaku on 15/4/25.
 */
public class AccentSwipeRefreshLayout extends SwipeRefreshLayout implements IThemeAccentView, IExtendedView {

    private TouchInterceptor mTouchInterceptor;
    private OnSizeChangedListener mOnSizeChangedListener;
    private OnFitSystemWindowsListener mOnFitSystemWindowsListener;

    public AccentSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AccentSwipeRefreshLayout(Context context) {
        super(context);
    }

    @Override
    public void setAccentTintColor(@NonNull ColorStateList color) {
        setColorSchemeColors(color.getDefaultColor());
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
    @SuppressWarnings("deprecation")
    protected boolean fitSystemWindows(@NonNull Rect insets) {
        if (mOnFitSystemWindowsListener != null) {
            mOnFitSystemWindowsListener.onFitSystemWindows(insets);
        }
        return super.fitSystemWindows(insets);
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
    protected final void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mOnSizeChangedListener != null) {
            mOnSizeChangedListener.onSizeChanged(this, w, h, oldw, oldh);
        }
    }
}
