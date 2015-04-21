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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.KeyEvent;
import android.view.View;

/**
 * Created by mariotaku on 15/4/21.
 */
public class RecyclerViewNavigationHelper {

    private int positionBackup;
    private final KeyboardShortcutsHandler handler;
    private final RecyclerView view;
    private final LinearLayoutManager manager;
    private final Adapter<ViewHolder> adapter;

    public RecyclerViewNavigationHelper(KeyboardShortcutsHandler handler, RecyclerView view,
                                        LinearLayoutManager manager, Adapter<ViewHolder> adapter) {
        this.handler = handler;
        this.view = view;
        this.manager = manager;
        this.adapter = adapter;
    }

    public boolean handleKeyboardShortcutRepeat(int keyCode, int repeatCount, @NonNull KeyEvent event) {
        final String action = handler.getKeyAction("navigation", keyCode, event);
        if (action == null) return false;
        final int direction;
        switch (action) {
            case "navigation.previous": {
                direction = -1;
                break;
            }
            case "navigation.next": {
                direction = 1;
                break;
            }
            default: {
                return false;
            }
        }
        final LinearLayoutManager layoutManager = this.manager;
        final View focusedChild = RecyclerViewUtils.findRecyclerViewChild(view, layoutManager.getFocusedChild());
        final int position;
        final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        final int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        final int itemCount = adapter.getItemCount();
        if (focusedChild != null) {
            position = view.getChildLayoutPosition(focusedChild);
        } else if (firstVisibleItemPosition == 0) {
            position = -1;
        } else if (lastVisibleItemPosition == itemCount - 1) {
            position = itemCount;
        } else if (direction > 0 && positionBackup < firstVisibleItemPosition) {
            position = firstVisibleItemPosition;
        } else if (direction < 0 && positionBackup > lastVisibleItemPosition) {
            position = lastVisibleItemPosition;
        } else {
            position = positionBackup;
        }
        positionBackup = position;
        RecyclerViewUtils.focusNavigate(view, layoutManager, position, direction);
        return false;
    }
}
