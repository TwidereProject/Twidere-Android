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
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView;
import android.text.InputType;
import android.text.Selection;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ComposeAutoCompleteAdapter;
import org.mariotaku.twidere.util.EmojiSupportUtils;
import org.mariotaku.twidere.util.widget.StatusTextTokenizer;
import org.mariotaku.twidere.view.iface.IThemeBackgroundTintView;

public class ComposeEditText extends AppCompatMultiAutoCompleteTextView implements IThemeBackgroundTintView {

    private ComposeAutoCompleteAdapter mAdapter;
    private long mAccountId;

    public ComposeEditText(final Context context) {
        this(context, null);
    }

    public ComposeEditText(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.autoCompleteTextViewStyle);
    }

    public ComposeEditText(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        EmojiSupportUtils.initForTextView(this);
        setTokenizer(new StatusTextTokenizer());
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                removeIMESuggestions();
            }
        });
        // HACK: remove AUTO_COMPLETE flag to force IME show auto completion
        setRawInputType(getInputType() & ~InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
    }

    @Override
    public void setBackgroundTintColor(@NonNull ColorStateList color) {
        setSupportBackgroundTintList(color);
    }

    public void setAccountId(long accountId) {
        mAccountId = accountId;
        updateAccountId();
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode() && mAdapter == null) {
            mAdapter = new ComposeAutoCompleteAdapter(getContext());
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

    private void updateAccountId() {
        if (mAdapter == null) return;
        mAdapter.setAccountId(mAccountId);
    }

    private void removeIMESuggestions() {
        final int selectionEnd = getSelectionEnd(), selectionStart = getSelectionStart();
        Selection.removeSelection(getText());
        setSelection(selectionStart, selectionEnd);
    }
}
