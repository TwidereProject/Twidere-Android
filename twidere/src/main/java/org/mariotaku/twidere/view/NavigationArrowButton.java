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
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.widget.ImageButton;

import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.ThemeUtils;

public class NavigationArrowButton extends ImageButton {

	private final int mHighlightColor;

	public NavigationArrowButton(final Context context) {
		this(context, null);
	}

	public NavigationArrowButton(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.imageButtonStyle);
	}

	public NavigationArrowButton(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mHighlightColor = isInEditMode() ? 0 : ThemeUtils.getUserThemeColor(context);
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		updateColorFilter();
	}

	private void updateColorFilter() {
		if (isClickable() && isEnabled() && ArrayUtils.contains(getDrawableState(), android.R.attr.state_pressed)) {
			setColorFilter(mHighlightColor, Mode.MULTIPLY);
		} else {
			clearColorFilter();
		}
	}
}
