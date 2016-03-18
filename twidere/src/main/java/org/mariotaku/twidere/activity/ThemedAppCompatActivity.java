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

package org.mariotaku.twidere.activity;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.TwidereActionMenuView;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;

import com.afollestad.appthemeengine.ATEActivity;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.iface.IAppCompatActivity;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class ThemedAppCompatActivity extends ATEActivity implements Constants,
        IThemedActivity, IAppCompatActivity {

    // Data fields
    private int mCurrentThemeColor;
    private int mCurrentThemeBackgroundAlpha;
    private String mCurrentThemeBackgroundOption;

    @Override
    public int getCurrentThemeBackgroundAlpha() {
        return mCurrentThemeBackgroundAlpha;
    }

    @Override
    public String getCurrentThemeBackgroundOption() {
        return mCurrentThemeBackgroundOption;
    }

    @Override
    public int getCurrentThemeColor() {
        return mCurrentThemeColor;
    }

    @Override
    public int getThemeBackgroundAlpha() {
        return ThemeUtils.getUserThemeBackgroundAlpha(this);
    }

    @Override
    public String getThemeBackgroundOption() {
        return ThemeUtils.getThemeBackgroundOption(this);
    }

    @Override
    public String getThemeFontFamily() {
        return ThemeUtils.getThemeFontFamily(this);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            StrictModeUtils.detectAllVmPolicy();
            StrictModeUtils.detectAllThreadPolicy();
        }
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public String getATEKey() {
        return ThemeUtils.getATEKey(this);
    }

    @Override
    protected void onApplyThemeResource(@NonNull Resources.Theme theme, int resId, boolean first) {
        mCurrentThemeColor = getThemeColor();
        mCurrentThemeBackgroundAlpha = getThemeBackgroundAlpha();
        mCurrentThemeBackgroundOption = getThemeBackgroundOption();
        super.onApplyThemeResource(theme, resId, first);
        final Window window = getWindow();
        if (shouldApplyWindowBackground()) {
            ThemeUtils.applyWindowBackground(this, window, mCurrentThemeBackgroundOption,
                    mCurrentThemeBackgroundAlpha);
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        ThemeUtils.fixNightMode(getResources(), newConfig);
        super.onConfigurationChanged(newConfig);
    }

    protected boolean shouldApplyWindowBackground() {
        return true;
    }


    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.view.",
            "android.webkit."
    };

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        // Fix for https://github.com/afollestad/app-theme-engine/issues/109
        if (context != this) {
            final AppCompatDelegate delegate = getDelegate();
            View view = delegate.createView(parent, name, context, attrs);
            if (view == null) {
                view = newInstance(name, context, attrs);
            }
            if (view == null) {
                view = newInstance(name, context, attrs);
            }
            if (view != null) {
                return view;
            }
        }
        if (parent instanceof TwidereActionMenuView) {
            final Class<?> cls = findClass(name);
            if (cls != null && ActionMenuItemView.class.isAssignableFrom(cls)) {
                return ((TwidereActionMenuView) parent).createActionMenuView(context, attrs);
            }
        }
        return super.onCreateView(parent, name, context, attrs);
    }

    private Class<?> findClass(String name) {
        Class<?> cls = null;
        try {
            cls = Class.forName(name);
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        if (cls != null) return cls;
        for (String prefix : sClassPrefixList) {
            try {
                cls = Class.forName(prefix + name);
            } catch (ClassNotFoundException e) {
                // Ignore
            }
            if (cls != null) return cls;
        }
        return null;
    }

    private View newInstance(String name, Context context, AttributeSet attrs) {
        try {
            final Class<?> cls = findClass(name);
            if (cls == null) throw new ClassNotFoundException(name);
            final Constructor<?> constructor = cls.getConstructor(Context.class, AttributeSet.class);
            return (View) constructor.newInstance(context, attrs);
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
