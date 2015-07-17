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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.TypedValue;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.themed.ThemedTextView;

/**
 * Created by mariotaku on 15/5/28.
 */
public class NameView extends ThemedTextView {

    private boolean mNameFirst;

    private String mName, mScreenName;

    private ForegroundColorSpan mPrimaryTextColor, mSecondaryTextColor;
    private StyleSpan mPrimaryTextStyle, mSecondaryTextStyle;
    private AbsoluteSizeSpan mPrimaryTextSize, mSecondaryTextSize;

    public NameView(final Context context) {
        this(context, null);
    }

    public NameView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NameView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSingleLine(true);
        setEllipsize(TextUtils.TruncateAt.END);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NameView, defStyleAttr, 0);
        setPrimaryTextColor(a.getColor(R.styleable.NameView_nv_primaryTextColor, 0));
        setSecondaryTextColor(a.getColor(R.styleable.NameView_nv_secondaryTextColor, 0));
        mPrimaryTextStyle = new StyleSpan(a.getInt(R.styleable.NameView_nv_primaryTextStyle, 0));
        mSecondaryTextStyle = new StyleSpan(a.getInt(R.styleable.NameView_nv_secondaryTextStyle, 0));
        a.recycle();
        setNameFirst(true);
    }

    public void setPrimaryTextColor(final int color) {
        mPrimaryTextColor = new ForegroundColorSpan(color);
    }

    public void setSecondaryTextColor(final int color) {
        mSecondaryTextColor = new ForegroundColorSpan(color);
    }

    public void setName(String name) {
        mName = name;
    }

    public void setScreenName(String screenName) {
        mScreenName = screenName;
    }

    public void setNameFirst(final boolean nameFirst) {
        mNameFirst = nameFirst;
    }

    public void updateText() {
        if (isInEditMode()) return;
        final SpannableStringBuilder sb = new SpannableStringBuilder();
        final String primaryText = mNameFirst ? mName : mScreenName;
        final String secondaryText = mNameFirst ? mScreenName : mName;
        if (primaryText != null) {
            int start = sb.length();
            sb.append(primaryText);
            int end = sb.length();
            sb.setSpan(mPrimaryTextColor, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(mPrimaryTextStyle, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(mPrimaryTextSize, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        sb.append(" ");
        if (secondaryText != null) {
            int start = sb.length();
            sb.append(secondaryText);
            int end = sb.length();
            sb.setSpan(mSecondaryTextColor, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(mSecondaryTextStyle, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(mSecondaryTextSize, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        setText(sb);
    }

    public void setPrimaryTextSize(final float textSize) {
        mPrimaryTextSize = new AbsoluteSizeSpan((int) calculateTextSize(TypedValue.COMPLEX_UNIT_SP, textSize));
    }

    private float calculateTextSize(final int unit, final float size) {
        Context c = getContext();
        Resources r;

        if (c == null)
            r = Resources.getSystem();
        else
            r = c.getResources();
        return TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
    }

    public void setSecondaryTextSize(final float textSize) {
        mSecondaryTextSize = new AbsoluteSizeSpan((int) calculateTextSize(TypedValue.COMPLEX_UNIT_SP, textSize));
    }

}
