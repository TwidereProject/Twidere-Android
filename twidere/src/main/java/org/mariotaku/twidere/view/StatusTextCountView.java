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

import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereValidator;

import java.util.Locale;

public class StatusTextCountView extends TextView {

	private final int mTextColor;
	private final Locale mLocale;
	private final TwidereValidator mValidator;

	public StatusTextCountView(final Context context) {
		this(context, null);
	}

	public StatusTextCountView(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
	}

	public StatusTextCountView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mValidator = new TwidereValidator(context);
		if (isInEditMode()) {
			mTextColor = 0;
			mLocale = Locale.getDefault();
		} else {
			final int textAppearance = ThemeUtils.getTitleTextAppearance(context);
			final TypedArray a = context.obtainStyledAttributes(textAppearance, new int[] { android.R.attr.textColor });
			mTextColor = a.getColor(0, 0);
			a.recycle();
			mLocale = getResources().getConfiguration().locale;
			setTextColor(mTextColor);
		}
	}

	public void setTextCount(final int count) {
		final int maxLength = mValidator.getMaxTweetLength();
		setText(getLocalizedNumber(mLocale, maxLength - count));
		final boolean exceeded_limit = count < maxLength;
		final boolean near_limit = count >= maxLength - 10;
		final float hue = exceeded_limit ? near_limit ? 5 * (maxLength - count) : 50 : 0;
		final float[] textColorHsv = new float[3];
		Color.colorToHSV(mTextColor, textColorHsv);
		final float[] errorColorHsv = { hue, 1.0f, 0.75f + textColorHsv[2] / 4 };
		setTextColor(count >= maxLength - 10 ? Color.HSVToColor(errorColorHsv) : mTextColor);
	}

}
