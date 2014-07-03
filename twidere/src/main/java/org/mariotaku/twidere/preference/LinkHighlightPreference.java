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
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.text.TwidereHighLightStyle;
import org.mariotaku.twidere.util.Utils;

public class LinkHighlightPreference extends AutoInvalidateListPreference implements Constants {

	private static final int[] ENTRIES_RES = { R.string.none, R.string.highlight, R.string.underline,
			R.string.highlight_and_underline };
	private static final String[] VALUES = { VALUE_LINK_HIGHLIGHT_OPTION_NONE, VALUE_LINK_HIGHLIGHT_OPTION_HIGHLIGHT,
			VALUE_LINK_HIGHLIGHT_OPTION_UNDERLINE, VALUE_LINK_HIGHLIGHT_OPTION_BOTH };
	private static final int[] OPTIONS = { VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE,
			VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT, VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE,
			VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH };

	public LinkHighlightPreference(final Context context) {
		this(context, null);
	}

	public LinkHighlightPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		final CharSequence[] entries = new CharSequence[VALUES.length];
		for (int i = 0, j = entries.length; i < j; i++) {
			entries[i] = getStyledEntry(OPTIONS[i], context.getString(ENTRIES_RES[i]));
		}
		setEntries(entries);
		setEntryValues(VALUES);
	}

	@Override
	protected void onBindView(final View view) {
		super.onBindView(view);
		final TextView summary = (TextView) view.findViewById(android.R.id.summary);
		summary.setVisibility(View.VISIBLE);
		summary.setText(getStyledEntry(Utils.getLinkHighlightOptionInt(getValue()), getEntry()));
	}

	private static CharSequence getStyledEntry(final int option, final CharSequence entry) {
		final SpannableString str = new SpannableString(entry);
		str.setSpan(new TwidereHighLightStyle(option), 0, str.length(), 0);
		return str;
	}
}
