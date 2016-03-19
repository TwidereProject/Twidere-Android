/*
 * Twidere - Twitter client for Android
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.appthemeengine.inflation.ViewInterface;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.iface.TintedStatusLayout;

/**
 * Created by mariotaku on 14/11/26.
 */
public class TintedStatusFrameLayout extends ExtendedFrameLayout implements TintedStatusLayout,
        ViewInterface {

    private final Paint mColorPaint;
    private boolean mSetPadding;

    private int mStatusBarHeight;
    private Rect mSystemWindowsInsets;
    private WindowInsetsListener mWindowInsetsListener;

    public TintedStatusFrameLayout(Context context) {
        this(context, null);
    }

    public TintedStatusFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintedStatusFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TintedStatusLayout);
        setSetPaddingEnabled(a.getBoolean(R.styleable.TintedStatusLayout_setPadding, false));
        a.recycle();
        mColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSystemWindowsInsets = new Rect();
        setWillNotDraw(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            ViewCompat.setOnApplyWindowInsetsListener(this, new android.support.v4.view.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                    final int top = insets.getSystemWindowInsetTop();
                    final int left = insets.getSystemWindowInsetLeft();
                    final int right = insets.getSystemWindowInsetRight();
                    final int bottom = insets.getSystemWindowInsetBottom();
                    if (mSetPadding) {
                        setPadding(left, top, right, bottom);
                    }
                    setStatusBarHeight(top);
                    if (mWindowInsetsListener != null) {
                        mWindowInsetsListener.onApplyWindowInsets(left, top, right, bottom);
                    }
                    return insets.consumeSystemWindowInsets();
                }
            });
        }
    }


    @Override
    public void setStatusBarColor(int color) {
        mColorPaint.setColor(0xFF000000 | color);
        mColorPaint.setAlpha(Color.alpha(color));
        invalidate();
    }

    @Override
    public void setSetPaddingEnabled(boolean enabled) {
        mSetPadding = enabled;
    }

    public void setStatusBarHeight(int height) {
        mStatusBarHeight = height;
        invalidate();
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.drawRect(0, 0, canvas.getWidth(), mStatusBarHeight, mColorPaint);
    }

    @Override
    protected boolean fitSystemWindows(@NonNull Rect insets) {
        mSystemWindowsInsets.set(insets);
        return true;
    }

    @Override
    public boolean setsStatusBarColor() {
        return true;
    }

    @Override
    public boolean setsToolbarColor() {
        return false;
    }

    public void setWindowInsetsListener(WindowInsetsListener listener) {
        mWindowInsetsListener = listener;
    }

    public interface WindowInsetsListener {
        void onApplyWindowInsets(int left, int top, int right, int bottom);
    }
}
