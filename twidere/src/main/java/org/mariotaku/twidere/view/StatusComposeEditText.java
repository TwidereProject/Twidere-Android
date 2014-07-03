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
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.util.AttributeSet;

import org.mariotaku.twidere.adapter.UserHashtagAutoCompleteAdapter;
import org.mariotaku.twidere.view.themed.ThemedMultiAutoCompleteTextView;

public class StatusComposeEditText extends ThemedMultiAutoCompleteTextView implements InputType {

	private UserHashtagAutoCompleteAdapter mAdapter;

	public StatusComposeEditText(final Context context) {
		this(context, null);
	}

	public StatusComposeEditText(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.autoCompleteTextViewStyle);
	}

	public StatusComposeEditText(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mAdapter = new UserHashtagAutoCompleteAdapter(this);
		setTokenizer(new ScreenNameTokenizer());
		setMovementMethod(ArrowKeyMovementMethod.getInstance());
		setRawInputType(TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_CAP_SENTENCES | TYPE_TEXT_FLAG_MULTI_LINE);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mAdapter == null || mAdapter.isCursorClosed()) {
			mAdapter = new UserHashtagAutoCompleteAdapter(this);
		}
		setAdapter(mAdapter);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mAdapter != null) {
			mAdapter.closeCursor();
			mAdapter = null;
		}
		setAdapter(mAdapter);
	}

	@Override
	protected void replaceText(final CharSequence text) {
		super.replaceText(text);
		append(" ");
	}

	private static class ScreenNameTokenizer implements Tokenizer {

		@Override
		public int findTokenEnd(final CharSequence text, final int cursor) {
			int i = cursor;
			final int len = text.length();

			while (i < len) {
				if (text.charAt(i) == ' ')
					return i;
				else {
					i++;
				}
			}

			return len;
		}

		@Override
		public int findTokenStart(final CharSequence text, final int cursor) {
			int start = cursor;

			while (start > 0 && text.charAt(start - 1) != ' ') {
				start--;
			}

			while (start < cursor && text.charAt(start) == ' ') {
				start++;
			}

			if (start < cursor && isToken(text.charAt(start))) {
				start++;
			} else {
				start = cursor;
			}

			return start;
		}

		@Override
		public CharSequence terminateToken(final CharSequence text) {
			int i = text.length();

			while (i > 0 && isToken(text.charAt(i - 1))) {
				i--;
			}

			if (i > 0 && text.charAt(i - 1) == ' ' || !(text instanceof Spanned)) return text;
			final SpannableString sp = new SpannableString(text);
			TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
			return sp;
		}

		private static boolean isToken(final char character) {
			switch (character) {
				case '\uff20':
				case '@':
				case '\uff03':
				case '#':
					return true;
			}
			return false;
		}
	}
}
