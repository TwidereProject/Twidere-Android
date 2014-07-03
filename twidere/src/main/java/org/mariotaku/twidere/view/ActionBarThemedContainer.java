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
import android.widget.FrameLayout;

import org.mariotaku.twidere.util.ThemeUtils;

public class ActionBarThemedContainer extends FrameLayout {

	public ActionBarThemedContainer(final Context context) {
		this(context, null);
	}

	public ActionBarThemedContainer(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarThemedContainer(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.layout });
		final int resId = a.getResourceId(0, 0);
		a.recycle();
		if (resId == 0) throw new IllegalArgumentException("You must specify a layout resource in layout XML file.");
		inflate(getThemedContext(context), resId, this);
	}

	private static Context getThemedContext(final Context context) {
		return ThemeUtils.getActionBarContext(context);
	}

}
