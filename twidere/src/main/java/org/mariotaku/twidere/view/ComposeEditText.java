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
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView;
import android.text.method.ArrowKeyMovementMethod;
import android.util.AttributeSet;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.UserHashtagAutoCompleteAdapter;
import org.mariotaku.twidere.util.widget.StatusTextTokenizer;

public class ComposeEditText extends AppCompatMultiAutoCompleteTextView {

    private UserHashtagAutoCompleteAdapter mAdapter;
    private long mAccountId;

    public ComposeEditText(final Context context) {
        this(context, null);
    }

    public ComposeEditText(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.autoCompleteTextViewStyle);
    }

    public ComposeEditText(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setTokenizer(new StatusTextTokenizer());
        setMovementMethod(ArrowKeyMovementMethod.getInstance());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode() && mAdapter == null) {
            mAdapter = new UserHashtagAutoCompleteAdapter(getContext());
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

    public void setAccountId(long accountId) {
        mAccountId = accountId;
        updateAccountId();
    }

    private void updateAccountId() {
        if (mAdapter == null) return;
        mAdapter.setAccountId(mAccountId);
    }
}
