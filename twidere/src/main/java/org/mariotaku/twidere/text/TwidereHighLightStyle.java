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

package org.mariotaku.twidere.text;

import android.text.TextPaint;
import android.text.style.CharacterStyle;

import org.mariotaku.twidere.Constants;

public class TwidereHighLightStyle extends CharacterStyle implements Constants {

	private final int option;

	public TwidereHighLightStyle(final int option) {
		this.option = option;
	}

	@Override
	public void updateDrawState(final TextPaint ds) {
		if ((option & VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE) != 0) {
			ds.setUnderlineText(true);
		}
		if ((option & VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT) != 0) {
			ds.setColor(ds.linkColor);
		}
	}
}
