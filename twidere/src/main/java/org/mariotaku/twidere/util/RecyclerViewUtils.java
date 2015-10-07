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
import android.view.View;
import android.view.ViewParent;

/**
 * Created by mariotaku on 15/4/13.
 */
public class RecyclerViewUtils {

    public static View findRecyclerViewChild(RecyclerView recyclerView, View view) {
        if (view == null) return null;
        final ViewParent parent = view.getParent();
        if (parent == recyclerView) return view;
        if (parent instanceof View) {
            return findRecyclerViewChild(recyclerView, (View) parent);
        }
        return null;
    }

    public static void focusNavigate(RecyclerView recyclerView, LinearLayoutManager layoutManager, int currentFocus, int direction) {
        if (direction == 0) return;
        if (currentFocus < 0) {
            focusFallback(layoutManager);
        } else {
            final View view;
            if (direction > 0 && currentFocus == layoutManager.findLastVisibleItemPosition()) {
                view = recyclerView.focusSearch(recyclerView.getFocusedChild(), View.FOCUS_DOWN);
            } else if (direction < 0 && currentFocus == layoutManager.findFirstVisibleItemPosition()) {
                view = recyclerView.focusSearch(recyclerView.getFocusedChild(), View.FOCUS_UP);
            } else {
                view = null;
            }
            final View firstVisibleView = layoutManager.findViewByPosition(currentFocus + (direction > 0 ? 1 : -1));
            final View viewToFocus;
            if (firstVisibleView != null) {
                viewToFocus = firstVisibleView;
            } else if (view != null) {
                viewToFocus = findRecyclerViewChild(recyclerView, view);
            } else {
                viewToFocus = null;
            }
            if (viewToFocus == null) return;
            final int nextPos = layoutManager.getPosition(viewToFocus);
            if (nextPos < 0 || (nextPos - currentFocus) * direction < 0) {
                focusFallback(layoutManager);
                return;
            }
            focus(viewToFocus);
        }
    }

    public static void pageScroll(RecyclerView recyclerView, LinearLayoutManager layoutManager, int direction) {
        final int contentHeight = layoutManager.getHeight() - layoutManager.getPaddingTop() - layoutManager.getPaddingBottom();
        recyclerView.smoothScrollBy(0, direction > 0 ? contentHeight : -contentHeight);
    }

    private static void focus(@NonNull View view) {
        if (view.isInTouchMode()) {
            view.requestFocusFromTouch();
        } else {
            view.requestFocus();
        }
    }

    private static void focusFallback(LinearLayoutManager layoutManager) {
        final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        final View firstVisibleView = layoutManager.findViewByPosition(firstVisibleItemPosition);
        if (firstVisibleView == null) return;
        focus(firstVisibleView);
    }
}
