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

package org.mariotaku.twidere.util;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v4.view.TintableBackgroundView;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.mariotaku.twidere.activity.AppCompatPreferenceActivity;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.iface.IThemedView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mariotaku on 15/4/22.
 */
public class ThemedLayoutInflaterFactory implements LayoutInflaterFactory {

    private static final String[] sCustomViewPrefixWhiteList = {"org.mariotaku.twidere.view"};
    private static final Map<String, Constructor> sConstructorCache = new HashMap<>();

    private final IThemedActivity activity;
    private final LayoutInflaterFactory delegate;

    public ThemedLayoutInflaterFactory(IThemedActivity activity, LayoutInflaterFactory delegate) {
        this.activity = activity;
        this.delegate = delegate;
    }

    public static View createCustomView(String name, Context context, AttributeSet attrs) {
        if (!name.contains(".")) return null;
        boolean whiteListed = false;
        for (String prefix : sCustomViewPrefixWhiteList) {
            if (name.startsWith(prefix)) {
                whiteListed = true;
                break;
            }
        }
        if (!whiteListed) return null;
        try {
            Constructor<?> constructor = sConstructorCache.get(name);
            if (constructor == null) {
                final Class<?> viewCls = Class.forName(name);
                if (!View.class.isAssignableFrom(viewCls)) return null;
                constructor = viewCls.getConstructor(Context.class, AttributeSet.class);
                sConstructorCache.put(name, constructor);
            }
            return (View) constructor.newInstance(context, attrs);
        } catch (ClassNotFoundException ignore) {
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new InflateException(e);
        }
        return null;
    }

    public static void initView(View view, IThemedActivity activity) {
        if (view == null) return;
        if (view instanceof ShapedImageView) {
            final ShapedImageView shapedImageView = (ShapedImageView) view;
            shapedImageView.setStyle(activity.getCurrentProfileImageStyle());
        }
        if (view instanceof TextView) {
            final String fontFamily = activity.getCurrentThemeFontFamily();
            final TextView textView = (TextView) view;
            final Typeface defTypeface = textView.getTypeface();
            textView.setTypeface(ThemeUtils.getUserTypeface((Context) activity, fontFamily, defTypeface));
        }
        initViewTint(view, activity);
    }

    private static void initViewTint(View view, IThemedActivity activity) {
        final ColorStateList tintList;
        if (!isActionBarContext(view.getContext(), getActionBarContext((Activity) activity))) {
            tintList = ColorStateList.valueOf(activity.getCurrentThemeColor());
        } else if (ThemeUtils.isDarkTheme(activity.getCurrentThemeResourceId())) {
            tintList = ColorStateList.valueOf(Color.WHITE);
        } else {
            final int themeColor = activity.getCurrentThemeColor();
            tintList = ColorStateList.valueOf(TwidereColorUtils.getContrastYIQ(themeColor, 192));
        }
        if (view instanceof IThemedView) {
            ((IThemedView) view).setThemeTintColor(tintList);
        } else if (view instanceof TintableBackgroundView) {
            ((TintableBackgroundView) view).setSupportBackgroundTintList(tintList);
        } else if (view instanceof EditText) {
            ViewCompat.setBackgroundTintList(view, tintList);
        }
    }

    private static boolean isActionBarContext(Context context, Context actionBarContext) {
        if (context == actionBarContext) return true;
        Context base = context;
        while (base instanceof ContextWrapper && (base = ((ContextWrapper) base).getBaseContext()) != null) {
            if (base == actionBarContext) return true;
        }
        return base == actionBarContext;
    }

    private static Context getActionBarContext(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            final android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (actionBar != null)
                return actionBar.getThemedContext();
        } else if (activity instanceof AppCompatPreferenceActivity) {
            final android.support.v7.app.ActionBar actionBar = ((AppCompatPreferenceActivity) activity).getSupportActionBar();
            if (actionBar != null)
                return actionBar.getThemedContext();
        }
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) return actionBar.getThemedContext();
        return null;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = delegate.onCreateView(parent, name, context, attrs);
        if (view == null) {
            view = createCustomView(name, context, attrs);
        }
        initView(view, activity);
        return view;
    }

}
