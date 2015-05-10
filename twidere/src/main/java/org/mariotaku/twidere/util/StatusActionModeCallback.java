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

package org.mariotaku.twidere.util;

import android.content.Context;
import android.support.v4.view.MenuItemCompat;
import android.text.SpannableString;
import android.text.style.URLSpan;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.widget.TextView;

import org.mariotaku.twidere.R;

/**
 * Created by mariotaku on 15/5/10.
 */
public class StatusActionModeCallback implements ActionMode.Callback {
    private final TextView textView;
    private final Context context;

    public StatusActionModeCallback(TextView textView, Context context) {
        this.textView = textView;
        this.context = context;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.action_status_text_selection, menu);
        mode.setTitle(android.R.string.selectTextMode);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        final int start = textView.getSelectionStart(), end = textView.getSelectionEnd();
        final SpannableString string = SpannableString.valueOf(textView.getText());
        final URLSpan[] spans = string.getSpans(start, end, URLSpan.class);
        final boolean avail = spans.length == 1 && URLUtil.isValidUrl(spans[0].getURL());
        MenuUtils.setMenuItemAvailability(menu, android.R.id.copyUrl, avail);
        MenuUtils.setMenuItemShowAsActionFlags(menu, android.R.id.copyUrl, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.copyUrl: {
                final int start = textView.getSelectionStart(), end = textView.getSelectionEnd();
                final SpannableString string = SpannableString.valueOf(textView.getText());
                final URLSpan[] spans = string.getSpans(start, end, URLSpan.class);
                if (spans.length != 1) return true;
                ClipboardUtils.setText(context, spans[0].getURL());
                mode.finish();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }
}
