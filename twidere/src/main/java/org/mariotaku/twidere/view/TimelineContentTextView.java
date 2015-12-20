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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.mariotaku.twidere.text.util.EmojiSpannableFactory;
import org.mariotaku.twidere.view.themed.ThemedTextView;

/**
 * Returns true when not clicking links
 * Created by mariotaku on 15/11/20.
 */
public class TimelineContentTextView extends ThemedTextView {
    private boolean mFirstNotLink;

    public TimelineContentTextView(Context context) {
        super(context);
        setSpannableFactory(new EmojiSpannableFactory(this));
    }

    public TimelineContentTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSpannableFactory(new EmojiSpannableFactory(this));
    }

    public TimelineContentTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setSpannableFactory(new EmojiSpannableFactory(this));
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return LinkMovementMethod.getInstance();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                Layout layout = getLayout();
                final float x = event.getX() - getPaddingLeft() + getScrollX();
                final float y = event.getY() - getPaddingTop() + getScrollY();
                final int line = layout.getLineForVertical(Math.round(y));
                int offset = layout.getOffsetForHorizontal(line, x);
                final CharSequence text = getText();
                if (text instanceof Spannable) {
                    final ClickableSpan[] spans = ((Spannable) text).getSpans(offset, offset, ClickableSpan.class);
                    mFirstNotLink = spans.length == 0;
                } else {
                    mFirstNotLink = true;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                mFirstNotLink = false;
                break;
            }
        }
        if (mFirstNotLink) {
            super.onTouchEvent(event);
            return false;
        } else {
            return super.onTouchEvent(event);
        }
    }
}
