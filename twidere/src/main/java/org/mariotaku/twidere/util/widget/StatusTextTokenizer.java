/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.util.widget;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView;

/**
 * Created by mariotaku on 15/5/14.
 */
public class StatusTextTokenizer implements MultiAutoCompleteTextView.Tokenizer {

    @Override
    public int findTokenStart(CharSequence text, int cursor) {
        // Search backward to find start symbol
        int i = cursor - 1;
        int len = text.length();
        while (i >= 0 && i < len && !isStartSymbol(text.charAt(i))) {
            i--;
        }
        if (i < 0) return cursor;
        return i;
    }

    @Override
    public int findTokenEnd(CharSequence text, int cursor) {
        int i = cursor - 1;
        int len = text.length();
        // Search backward to find start symbol
        while (i >= 0 && i < len && isStartSymbol(text.charAt(i))) {
            i--;
        }
        // Search forward to find space
        while (i < len && !isSpace(text.charAt(i))) {
            i++;
        }
        if (i < 0) return cursor;
        return i;
    }

    @Override
    public CharSequence terminateToken(CharSequence text) {
        // We already have spaces at the end, so just ignore
        if (text instanceof Spanned) {
            SpannableString sp = new SpannableString(text + " ");
            TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                    Object.class, sp, 0);
            return sp;
        } else {
            return text + " ";
        }
    }

    private static boolean isSpace(final char c) {
        return Character.isSpaceChar(c) || Character.isWhitespace(c);
    }

    private static boolean isStartSymbol(final char c) {
        switch (c) {
            case '\uff20':
            case '@':
            case '\uff03':
            case '#':
                return true;
        }
        return false;
    }
}
