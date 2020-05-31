/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.adapter.decorator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.State;
import androidx.appcompat.widget.TintTypedArray;
import android.view.View;

public class ExtendedDividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private final Rect mPaddingRect = new Rect();
    private final Drawable mDivider;

    private int mOrientation;
    private Padding mPadding;
    private int mDecorationStart = -1, mDecorationEnd = -1, mDecorationEndOffset;

    @SuppressWarnings("RestrictedApi")
    public ExtendedDividerItemDecoration(Context context, int orientation) {
        final TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, null, ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);
    }


    public void setDecorationStart(int start) {
        mDecorationStart = start;
    }

    public void setDecorationEnd(int end) {
        mDecorationEnd = end;
        mDecorationEndOffset = -1;
    }

    public void setDecorationEndOffset(int endOffset) {
        mDecorationEndOffset = endOffset;
        mDecorationEnd = -1;
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, State state) {
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    public void setPadding(final int left, final int top, final int right, final int bottom) {
        mPadding = (position, rect) -> {
            rect.set(left, top, right, bottom);
            return true;
        };
    }

    public void setPadding(Padding padding) {
        mPadding = padding;
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        if (mDivider == null) return;
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final int childPos = parent.getChildAdapterPosition(child);
            if (childPos < 0 || !isDividerEnabled(childPos)) continue;
            final int start = getDecorationStart(), end = getDecorationEnd(parent);
            if (start >= 0 && childPos < start || end >= 0 && childPos > end) continue;
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin +
                    Math.round(child.getTranslationY());
            final int bottom = top + mDivider.getIntrinsicHeight();
            if (mPadding != null && mPadding.get(childPos, mPaddingRect)) {
                mDivider.setBounds(left + mPaddingRect.left, top + mPaddingRect.top, right - mPaddingRect.right,
                        bottom - mPaddingRect.bottom);
            } else {
                mDivider.setBounds(left, top, right, bottom);
            }
            mDivider.draw(c);
        }
    }

    protected boolean isDividerEnabled(int childPos) {
        return true;
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {
        if (mDivider == null) return;
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final int childPos = parent.getChildAdapterPosition(child);
            if (childPos < 0 || !isDividerEnabled(childPos)) continue;
            final int start = getDecorationStart(), end = getDecorationEnd(parent);
            if (start >= 0 && childPos < start || end >= 0 && childPos > end) continue;
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getRight() + params.rightMargin +
                    Math.round(child.getTranslationX());
            final int right = left + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left + mPaddingRect.left, top + mPaddingRect.top, right - mPaddingRect.right,
                    bottom - mPaddingRect.bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        if (mDivider == null) return;
        final int childPos = parent.getChildAdapterPosition(view);
        if (childPos < 0 || !isDividerEnabled(childPos)) return;
        final int start = getDecorationStart(), end = getDecorationEnd(parent);
        if (start >= 0 && childPos < start || end >= 0 && childPos > end) {
            outRect.setEmpty();
            return;
        }
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        }
    }

    private int getDecorationEnd(RecyclerView parent) {
        if (mDecorationEnd != -1) return mDecorationEnd;
        if (mDecorationEndOffset != -1) {
            final Adapter adapter = parent.getAdapter();
            return adapter.getItemCount() - 1 - mDecorationEndOffset;
        }
        return -1;
    }

    private int getDecorationStart() {
        return mDecorationStart;
    }

    public interface Padding {
        boolean get(int position, Rect rect);
    }
}
