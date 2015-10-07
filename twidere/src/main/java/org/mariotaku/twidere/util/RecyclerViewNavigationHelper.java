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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.KeyEvent;
import android.view.View;

import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;

/**
 * Created by mariotaku on 15/4/21.
 */
public class RecyclerViewNavigationHelper implements KeyboardShortcutCallback {

    private int positionBackup;
    @NonNull
    private final RecyclerView view;
    @NonNull
    private final LinearLayoutManager manager;
    @NonNull
    private final Adapter<ViewHolder> adapter;
    @Nullable
    private final RefreshScrollTopInterface iface;

    public RecyclerViewNavigationHelper(@NonNull final RecyclerView view,
                                        @NonNull final LinearLayoutManager manager,
                                        @NonNull final Adapter<ViewHolder> adapter,
                                        @Nullable final RefreshScrollTopInterface iface) {
        this.view = view;
        this.manager = manager;
        this.adapter = adapter;
        this.iface = iface;
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull final KeyboardShortcutsHandler handler,
                                                final int keyCode, final int repeatCount,
                                                @NonNull final KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (action == null) return false;
        final int direction;
        switch (action) {
            case ACTION_NAVIGATION_PREVIOUS: {
                direction = -1;
                break;
            }
            case ACTION_NAVIGATION_NEXT: {
                direction = 1;
                break;
            }
            case ACTION_NAVIGATION_PAGE_DOWN: {
                RecyclerViewUtils.pageScroll(view, manager, 1);
                return true;
            }
            case ACTION_NAVIGATION_PAGE_UP: {
                RecyclerViewUtils.pageScroll(view, manager, -1);
                return true;
            }
            default: {
                return false;
            }
        }
        final View focusedChild = RecyclerViewUtils.findRecyclerViewChild(view, manager.getFocusedChild());
        final int position;
        final int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();
        final int lastVisibleItemPosition = manager.findLastVisibleItemPosition();
        final int itemCount = adapter.getItemCount();
        final boolean backupOutsideRange = positionBackup > lastVisibleItemPosition || positionBackup < firstVisibleItemPosition;
        if (focusedChild != null) {
            position = view.getChildLayoutPosition(focusedChild);
        } else if (firstVisibleItemPosition == 0) {
            position = -1;
        } else if (lastVisibleItemPosition == itemCount - 1) {
            position = itemCount;
        } else if (direction > 0 && backupOutsideRange) {
            position = firstVisibleItemPosition;
        } else if (direction < 0 && backupOutsideRange) {
            position = lastVisibleItemPosition;
        } else {
            position = positionBackup;
        }
        positionBackup = position;
        RecyclerViewUtils.focusNavigate(view, manager, position, direction);
        return true;
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (action == null) return false;
        switch (action) {
            case ACTION_NAVIGATION_TOP: {
                if (iface != null) {
                    iface.scrollToStart();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (action == null) return false;
        switch (action) {
            case ACTION_NAVIGATION_PREVIOUS:
            case ACTION_NAVIGATION_NEXT:
            case ACTION_NAVIGATION_TOP:
            case ACTION_NAVIGATION_PAGE_DOWN:
            case ACTION_NAVIGATION_PAGE_UP:
                return true;
        }
        return false;
    }
}
