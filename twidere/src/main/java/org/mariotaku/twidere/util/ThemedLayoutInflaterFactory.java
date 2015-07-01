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

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v4.view.TintableBackgroundView;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.AppCompatDelegateTrojan;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.AppCompatPreferenceActivity;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.support.ViewSupport;
import org.mariotaku.twidere.view.ProfileImageView;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.TwidereToolbar;
import org.mariotaku.twidere.view.iface.IThemeAccentView;
import org.mariotaku.twidere.view.iface.IThemeBackgroundTintView;
import org.mariotaku.twidere.view.themed.ThemedTextView;

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

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = delegate.onCreateView(parent, name, context, attrs);
        if (view == null) {
            view = createCustomView(name, context, attrs);
        }
        initView(view, activity);
        return view;
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
        //noinspection TryWithIdenticalCatches
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
        } catch (NoSuchMethodException e) {
            throw new InflateException(e);
        } catch (InvocationTargetException e) {
            throw new InflateException(e);
        } catch (InstantiationException e) {
            throw new InflateException(e);
        } catch (IllegalAccessException e) {
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
        if (view instanceof ProfileImageView) {
            final ProfileImageView profileImageView = (ProfileImageView) view;
            profileImageView.setOval(activity.getCurrentProfileImageStyle() == ShapedImageView.SHAPE_CIRCLE);
        }
        if (view instanceof ThemedTextView) {
            final String fontFamily = activity.getCurrentThemeFontFamily();
            final TextView textView = (TextView) view;
            final Typeface defTypeface = textView.getTypeface();
            textView.setTypeface(ThemeUtils.getUserTypeface((Context) activity, fontFamily, defTypeface));
        }
        initViewTint(view, activity);
    }

    private static void initViewTint(View view, IThemedActivity activity) {
        final int noTintColor, accentColor, backgroundTintColor;
        final boolean isColorTint;
        // View context is not derived from ActionBar, apply color tint directly
        final Resources resources = ((Activity) activity).getResources();
        final Context viewContext = view.getContext();
        final boolean isActionBarContext = isActionBarContext(viewContext, getActionBarContext((Activity) activity));
        final int themeResourceId = activity.getCurrentThemeResourceId();
        final boolean isDarkTheme = ThemeUtils.isDarkTheme(themeResourceId);
        final int backgroundColorApprox;
        if (!isActionBarContext) {
            accentColor = activity.getCurrentThemeColor();
            final int[] darkLightColors = new int[2];
            ThemeUtils.getDarkLightForegroundColors((Context) activity,
                    activity.getCurrentThemeResourceId(), darkLightColors);
            noTintColor = TwidereColorUtils.getContrastYIQ(accentColor, ThemeUtils.ACCENT_COLOR_THRESHOLD,
                    darkLightColors[0], darkLightColors[1]);
            backgroundTintColor = accentColor;
            backgroundColorApprox = isDarkTheme ? Color.BLACK : Color.WHITE;
            isColorTint = true;
        } else if (isDarkTheme) {
            // View context is derived from ActionBar but is currently dark theme, so we should show
            // light
            noTintColor = Color.WHITE;
            accentColor = activity.getCurrentThemeColor();
            backgroundTintColor = noTintColor;
            backgroundColorApprox = Color.BLACK;
            isColorTint = true;
        } else {
            // View context is derived from ActionBar and it's light theme, so we use contrast color
            final int actionBarColor = activity.getCurrentThemeColor();
            final int actionBarTheme = ThemeUtils.getActionBarThemeResource(activity.getThemeResourceId(), actionBarColor);
            accentColor = ThemeUtils.getColorFromAttribute(viewContext, android.R.attr.colorForeground, 0);
            noTintColor = ThemeUtils.getColorFromAttribute(viewContext, android.R.attr.colorBackground, 0);
            backgroundTintColor = accentColor;
            backgroundColorApprox = Color.WHITE;
            isColorTint = false;
        }
        final boolean isAccentOptimal = Math.abs(TwidereColorUtils.getYIQContrast(backgroundColorApprox, accentColor)) > 64;
        if (view instanceof TextView) {
            final TextView textView = (TextView) view;
            if (isAccentOptimal) {
                textView.setLinkTextColor(accentColor);
            }
        }
        if (view instanceof IThemeAccentView) {
            if (isAccentOptimal || !isColorTint) {
                ((IThemeAccentView) view).setAccentTintColor(ColorStateList.valueOf(accentColor));
            } else {
                final int defaultAccentColor = ThemeUtils.getColorFromAttribute(viewContext,
                        R.attr.colorAccent, resources.getColor(R.color.branding_color));
                ((IThemeAccentView) view).setAccentTintColor(ColorStateList.valueOf(defaultAccentColor));
            }
        } else if (view instanceof IThemeBackgroundTintView) {
            if (isAccentOptimal || !isColorTint) {
                ((IThemeBackgroundTintView) view).setBackgroundTintColor(ColorStateList.valueOf(backgroundTintColor));
            }
        } else if (view instanceof TintableBackgroundView) {
            final TintableBackgroundView tintable = (TintableBackgroundView) view;
            if (isAccentOptimal || !isColorTint) {
                applyTintableBackgroundViewTint(tintable, accentColor, noTintColor, backgroundTintColor, isColorTint);
            }
        } else if (view instanceof TwidereToolbar) {
            final Context context = viewContext;
            if (context instanceof android.support.v7.internal.view.ContextThemeWrapper) {
                ((TwidereToolbar) view).setItemColor(ThemeUtils.getThemeForegroundColor(context,
                        ((ContextThemeWrapper) context).getThemeResId()));
            } else {
                ((TwidereToolbar) view).setItemColor(ThemeUtils.getThemeForegroundColor(context));
            }
        } else if (view instanceof EditText) {
            if (isAccentOptimal || !isColorTint) {
                ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(backgroundTintColor));
            }
        } else if (view instanceof ProgressBar) {
            if (isAccentOptimal || !isColorTint) {
                ViewSupport.setIndeterminateTintList((ProgressBar) view, ColorStateList.valueOf(accentColor));
                ViewSupport.setProgressTintList((ProgressBar) view, ColorStateList.valueOf(accentColor));
                ViewSupport.setProgressBackgroundTintList((ProgressBar) view, ColorStateList.valueOf(accentColor));
            }
        }
    }

    private static void applyTintableBackgroundViewTint(TintableBackgroundView tintable, int accentColor, int noTintColor, int backgroundTintColor, boolean isColorTint) {
        if (tintable instanceof Button) {
        } else if (tintable instanceof EditText) {
            tintable.setSupportBackgroundTintList(ColorStateList.valueOf(backgroundTintColor));
        } else if (isColorTint) {
            final int[][] states = {{android.R.attr.state_selected}, {android.R.attr.state_focused},
                    {android.R.attr.state_pressed}, {0}};
            final int[] colors = {accentColor, accentColor, accentColor, noTintColor};
            tintable.setSupportBackgroundTintList(new ColorStateList(states, colors));
        } else {
            tintable.setSupportBackgroundTintList(ColorStateList.valueOf(accentColor));
        }
    }

    private static boolean isActionBarContext(@NonNull Context context, @Nullable Context actionBarContext) {
        if (context instanceof ThemeUtils.ActionBarContextThemeWrapper) return true;
        if (actionBarContext == null) return false;
        if (context == actionBarContext) return true;
        Context base = context;
        while (base instanceof ContextWrapper && (base = ((ContextWrapper) base).getBaseContext()) != null) {
            if (base == actionBarContext) return true;
        }
        return false;
    }

    @Nullable
    private static Context getActionBarContext(@NonNull Activity activity) {
        Context actionBarContext = null;
        if (activity instanceof AppCompatActivity) {
            final AppCompatDelegate delegate = ((AppCompatActivity) activity).getDelegate();
            final ActionBar actionBar = AppCompatDelegateTrojan.peekActionBar(delegate);
            if (actionBar != null) {
                actionBarContext = actionBar.getThemedContext();
            }
        } else if (activity instanceof AppCompatPreferenceActivity) {
            final AppCompatDelegate delegate = ((AppCompatPreferenceActivity) activity).getDelegate();
            final ActionBar actionBar = AppCompatDelegateTrojan.peekActionBar(delegate);
            if (actionBar != null) {
                actionBarContext = actionBar.getThemedContext();
            }
        }
        if (activity != actionBarContext) return actionBarContext;
        return null;
    }
}
