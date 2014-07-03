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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import org.mariotaku.twidere.content.iface.ITwidereContextWrapper;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;

public class ActionBarSplitThemedContainer extends FrameLayout {

	public ActionBarSplitThemedContainer(final Context context) {
		this(context, null);
	}

	public ActionBarSplitThemedContainer(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarSplitThemedContainer(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.layout });
		final int resId = a.getResourceId(0, 0);
		a.recycle();
		if (resId == 0) throw new IllegalArgumentException("You must specify a layout resource in layout XML file.");
		final View view = LayoutInflater.from(getThemedContext(context)).inflate(resId, this, false);
		final int themeResId;
		if (context instanceof ITwidereContextWrapper) {
			themeResId = ((ITwidereContextWrapper) context).getThemeResourceId();
		} else {
			themeResId = ThemeUtils.getThemeResource(context);
		}
		ViewAccessor.setBackground(view, ThemeUtils.getActionBarSplitBackground(context, themeResId));
		addView(view);
	}

	private static Context getThemedContext(final Context context) {
		return ThemeUtils.getActionBarContext(context);
	}

}
