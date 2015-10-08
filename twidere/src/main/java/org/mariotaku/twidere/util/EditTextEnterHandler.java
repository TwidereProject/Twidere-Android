/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.ArrayList;

/**
 * Created by mariotaku on 15/4/22.
 */
public class EditTextEnterHandler implements View.OnKeyListener, OnEditorActionListener, TextWatcher {

    @Nullable
    private EnterListener listener;
    private boolean enabled;
    private ArrayList<TextWatcher> textWatchers;
    private boolean appendText;

    public EditTextEnterHandler(@Nullable EnterListener listener, boolean enabled) {
        this.listener = listener;
        this.enabled = enabled;
    }

    public void addTextChangedListener(TextWatcher watcher) {
        if (textWatchers == null) {
            textWatchers = new ArrayList<>();
        }
        textWatchers.add(watcher);
    }

    public static EditTextEnterHandler attach(@NonNull EditText editText, @Nullable EnterListener listener, boolean enabled) {
        final EditTextEnterHandler enterHandler = new EditTextEnterHandler(listener, enabled);
        editText.setOnKeyListener(enterHandler);
        editText.setOnEditorActionListener(enterHandler);
        editText.addTextChangedListener(enterHandler);
        return enterHandler;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (textWatchers != null) {
            for (TextWatcher textWatcher : textWatchers) {
                textWatcher.beforeTextChanged(s, start, count, after);
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (textWatchers != null) {
            for (TextWatcher textWatcher : textWatchers) {
                textWatcher.onTextChanged(s, start, before, count);
            }
        }
        appendText = count > before;
    }

    @Override
    public void afterTextChanged(final Editable s) {
        final int length = s.length();
        if (enabled && length > 0 && s.charAt(length - 1) == '\n' && appendText) {
            if (shouldCallListener()) {
                s.delete(length - 1, length);
                dispatchHitEnter();
            }
        } else if (textWatchers != null) {
            for (TextWatcher textWatcher : textWatchers) {
                textWatcher.afterTextChanged(s);
            }
        }
    }

    @Override
    public boolean onEditorAction(final TextView view, final int actionId, final KeyEvent event) {
        if (!enabled) return false;
        if (event != null && actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (shouldCallListener()) return dispatchHitEnter();
        }
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && enabled && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (shouldCallListener()) return dispatchHitEnter();
        }
        return false;
    }

    private boolean dispatchHitEnter() {
        return listener != null && listener.onHitEnter();
    }

    private boolean shouldCallListener() {
        return listener != null && listener.shouldCallListener();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setListener(@Nullable EnterListener listener) {
        this.listener = listener;
    }

    public interface EnterListener {
        boolean shouldCallListener();

        boolean onHitEnter();
    }

}
