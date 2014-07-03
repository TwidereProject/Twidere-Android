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
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.mariotaku.twidere.view.themed.ThemedTextView;

public class HandleSpanClickTextView extends ThemedTextView {

	public HandleSpanClickTextView(final Context context) {
		super(context);
	}

	public HandleSpanClickTextView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public HandleSpanClickTextView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		final Spannable buffer = SpannableString.valueOf(getText());
		final int action = event.getAction();
		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			x -= getTotalPaddingLeft();
			y -= getTotalPaddingTop();

			x += getScrollX();
			y += getScrollY();

			final Layout layout = getLayout();
			final int line = layout.getLineForVertical(y);
			final int off = layout.getOffsetForHorizontal(line, x);
			final float lineWidth = layout.getLineWidth(line);

			final ClickableSpan[] links = buffer.getSpans(off, off, ClickableSpan.class);

			if (links.length != 0 && x <= lineWidth) {
				final ClickableSpan link = links[0];
				if (action == MotionEvent.ACTION_UP) {
					Selection.removeSelection(buffer);
					setClickable(false);
					link.onClick(this);
					return true;
				} else if (action == MotionEvent.ACTION_DOWN) {
					Selection.setSelection(buffer, buffer.getSpanStart(link), buffer.getSpanEnd(link));
					setClickable(true);
				}
			} else {
				Selection.removeSelection(buffer);
				setClickable(false);
			}
		}
		return super.onTouchEvent(event);
	}
}
