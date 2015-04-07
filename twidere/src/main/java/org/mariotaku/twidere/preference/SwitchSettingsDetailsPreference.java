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

package org.mariotaku.twidere.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.fragment.SettingsDetailsFragment;

/**
 * Created by mariotaku on 15/4/7.
 */
public class SwitchSettingsDetailsPreference extends SwitchPreference implements Constants {

    public SwitchSettingsDetailsPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.src});
        setFragment(SettingsDetailsFragment.class.getName());
        final Bundle extras = getExtras();
        extras.putInt(EXTRA_RESID, a.getResourceId(0, 0));
        a.recycle();

    }

    public SwitchSettingsDetailsPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.switchPreferenceStyle);
    }

    public SwitchSettingsDetailsPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        if (view instanceof ViewGroup) {
            ((ViewGroup) view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        }
        final Switch switchView = (Switch) findViewByType(view, Switch.class);
        if (switchView != null) {
            switchView.setClickable(true);
            switchView.setFocusable(true);
        }
    }

    private static View findViewByType(View view, Class<? extends View> cls) {
        if (cls.isAssignableFrom(view.getClass())) return view;
        if (view instanceof ViewGroup) {
            for (int i = 0, j = ((ViewGroup) view).getChildCount(); i < j; i++) {
                final View found = findViewByType(((ViewGroup) view).getChildAt(i), cls);
                if (found != null) return found;
            }
        }
        return null;
    }

    @Override
    protected void onClick() {

    }
}
