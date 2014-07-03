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

package org.mariotaku.twidere.preference;

import android.content.Context;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ParseUtils;

public class ThemeFontFamilyPreference extends AutoInvalidateListPreference implements Constants {

	private static final int[] ENTRIES_RES = { R.string.font_family_regular, R.string.font_family_condensed,
			R.string.font_family_light };
	private static final String[] VALUES = { VALUE_THEME_FONT_FAMILY_REGULAR, VALUE_THEME_FONT_FAMILY_CONDENSED,
			VALUE_THEME_FONT_FAMILY_LIGHT };

	public ThemeFontFamilyPreference(final Context context) {
		this(context, null);
	}

	public ThemeFontFamilyPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			setEnabled(false);
			return;
		}
		final CharSequence[] entries = new CharSequence[VALUES.length];
		for (int i = 0, j = entries.length; i < j; i++) {
			final SpannableString str = new SpannableString(context.getString(ENTRIES_RES[i]));
			str.setSpan(new TypefaceSpan(VALUES[i]), 0, str.length(), 0);
			entries[i] = str;
		}
		setEntries(entries);
		setEntryValues(VALUES);
	}

	@Override
	protected void onBindView(final View view) {
		super.onBindView(view);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
		final TextView summary = (TextView) view.findViewById(android.R.id.summary);
		summary.setVisibility(View.VISIBLE);
		final String defEntry = getContext().getString(R.string.font_family_regular);
		final SpannableString str = new SpannableString(ParseUtils.parseString(getEntry(), defEntry));
		str.setSpan(new TypefaceSpan(getValue()), 0, str.length(), 0);
		summary.setText(str);
	}

	@Override
	protected void onClick() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
		super.onClick();
	}
}
