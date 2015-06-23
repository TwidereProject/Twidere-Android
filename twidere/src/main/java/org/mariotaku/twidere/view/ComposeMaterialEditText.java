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
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView;
import android.text.InputType;
import android.text.method.ArrowKeyMovementMethod;
import android.util.AttributeSet;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.UserHashtagAutoCompleteAdapter;
import org.mariotaku.twidere.util.widget.ScreenNameTokenizer;
import org.mariotaku.twidere.view.iface.IThemeBackgroundTintView;

public class ComposeMaterialEditText extends AppCompatMultiAutoCompleteTextView implements IThemeBackgroundTintView {

    private UserHashtagAutoCompleteAdapter mAdapter;
    private long mAccountId;

    public ComposeMaterialEditText(final Context context) {
        this(context, null);
    }

    public ComposeMaterialEditText(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.autoCompleteTextViewStyle);
    }

    public ComposeMaterialEditText(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setTokenizer(new ScreenNameTokenizer());
        setMovementMethod(ArrowKeyMovementMethod.getInstance());
        setupComposeInputType();
    }

    private void setupComposeInputType() {
        int rawInputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
        rawInputType |= InputType.TYPE_TEXT_FLAG_MULTI_LINE;
        setRawInputType(rawInputType);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode() && mAdapter == null) {
            mAdapter = new UserHashtagAutoCompleteAdapter(this);
        }
        setAdapter(mAdapter);
        updateAccountId();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAdapter != null) {
            mAdapter.closeCursor();
            mAdapter = null;
        }
    }

    @Override
    public void setBackgroundTintColor(@NonNull ColorStateList color) {
        setSupportBackgroundTintList(color);
    }

    public void setAccountId(long accountId) {
        mAccountId = accountId;
        updateAccountId();
    }

    private void updateAccountId() {
        if (mAdapter == null) return;
        mAdapter.setAccountId(mAccountId);
    }

    @Override
    protected void replaceText(final CharSequence text) {
        super.replaceText(text);
        append(" ");
    }

}
