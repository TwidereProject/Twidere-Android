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

import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;

public class TwidereURLSpan extends URLSpan implements Constants {

    private final int type, highlightStyle;
    private final long accountId;
    private final long extraId;
    private final String url, orig;
    private final boolean sensitive;
    private final OnLinkClickListener listener;
    private final int start, end;

    public TwidereURLSpan(final String url, final String orig, final long accountId, final long extraId,
                          final int type, final boolean sensitive, final int highlightStyle, int start, int end,
                          final OnLinkClickListener listener) {
        super(url);
        this.url = url;
        this.orig = orig;
        this.accountId = accountId;
        this.extraId = extraId;
        this.type = type;
        this.sensitive = sensitive;
        this.highlightStyle = highlightStyle;
        this.start = start;
        this.end = end;
        this.listener = listener;
    }

    @Override
    public void onClick(@NonNull final View widget) {
        if (listener != null) {
            listener.onLinkClick(url, orig, accountId, extraId, type, sensitive, start, end);
        }
    }

    @Override
    public void updateDrawState(@NonNull final TextPaint ds) {
        if ((highlightStyle & VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE) != 0) {
            ds.setUnderlineText(true);
        }
        if ((highlightStyle & VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT) != 0) {
            ds.setColor(ds.linkColor);
        }
    }
}
