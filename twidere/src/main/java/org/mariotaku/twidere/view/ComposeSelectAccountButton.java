/*
 * Twidere - Twitter client for Android
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
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.view.iface.IColorLabelView.Helper;

/**
 * Created by mariotaku on 14/12/8.
 */
public class ComposeSelectAccountButton extends ViewGroup {
    private final AccountIconsAdapter mAccountIconsAdapter;
    private final Helper mColorLabelHelper;

    public ComposeSelectAccountButton(Context context) {
        this(context, null);
    }

    public ComposeSelectAccountButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ComposeSelectAccountButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mColorLabelHelper = new Helper(this, context, attrs, defStyle);
        mColorLabelHelper.setIgnorePaddings(true);
        mAccountIconsAdapter = new AccountIconsAdapter(context);
        final RecyclerView recyclerView = new InternalRecyclerView(context);
        final LinearLayoutManager linearLayoutManager = new MyLinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAccountIconsAdapter);
        ViewCompat.setOverScrollMode(recyclerView, ViewCompat.OVER_SCROLL_NEVER);
        addView(recyclerView);
    }


    @Override
    protected void dispatchDraw(@NonNull final Canvas canvas) {
        mColorLabelHelper.dispatchDrawBackground(canvas);
        super.dispatchDraw(canvas);
        mColorLabelHelper.dispatchDrawLabels(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int maxWidth = 0;
        for (int i = 0, j = getChildCount(); i < j; i++) {
            final View child = getChildAt(i);
            maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
        }
        if (maxWidth == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec), heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0, j = getChildCount(); i < j; i++) {
            final View child = getChildAt(i);
            child.layout(getPaddingLeft(), getPaddingTop(), r - l - getPaddingRight(),
                    b - t - getPaddingBottom());
        }
    }

    static class AccountIconViewHolder extends ViewHolder {

        private final ImageView iconView;

        public AccountIconViewHolder(View itemView) {
            super(itemView);
            iconView = (ImageView) itemView.findViewById(android.R.id.icon);
        }

        public void showAccount(AccountIconsAdapter adapter, ParcelableAccount account) {
            final ImageLoaderWrapper loader = adapter.getImageLoader();
            loader.displayProfileImage(iconView, account.profile_image_url);
        }
    }

    public void setSelectedAccounts(long[] accountIds) {
        final ParcelableAccount[] accounts = ParcelableAccount.getAccounts(getContext(), accountIds);
        if (accounts != null) {
            final int[] colors = new int[accounts.length];
            for (int i = 0, j = colors.length; i < j; i++) {
                colors[i] = accounts[i].color;
            }
            mColorLabelHelper.drawEnd(colors);
        } else {
            mColorLabelHelper.drawEnd(null);
        }
        mAccountIconsAdapter.setSelectedAccounts(accounts);
    }

    private static class AccountIconsAdapter extends Adapter<AccountIconViewHolder> {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private final ImageLoaderWrapper mImageLoader;
        private ParcelableAccount[] mAccounts;

        public ImageLoaderWrapper getImageLoader() {
            return mImageLoader;
        }

        @Override
        public AccountIconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = mInflater.inflate(R.layout.adapter_item_compose_account, parent, false);
            return new AccountIconViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AccountIconViewHolder holder, int position) {
            holder.showAccount(this, mAccounts[position]);
        }

        @Override
        public int getItemCount() {
            return mAccounts != null ? mAccounts.length : 0;
        }

        public void setSelectedAccounts(ParcelableAccount[] accounts) {
            if (accounts != null) {
//                ArrayUtils.reverse(accounts);
            }
            mAccounts = accounts;
            notifyDataSetChanged();
        }

        public AccountIconsAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mImageLoader = TwidereApplication.getInstance(context).getImageLoaderWrapper();
        }
    }

    private static class MyLinearLayoutManager extends LinearLayoutManager {
        private int mWidth, mHeight;

        public MyLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
            final RecyclerView.LayoutParams layoutParams = super.generateLayoutParams(c, attrs);
            return layoutParams;
        }

        @Override
        public void onLayoutChildren(Recycler recycler, State state) {
            state.getItemCount();
            super.onLayoutChildren(recycler, state);
        }

        private int findChildIndex(View view) {
            for (int i = 0, j = getChildCount(); i < j; i++) {
                if (getChildAt(i) == view) return i;
            }
            return -1;
        }

        @Override
        public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
            final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int contentWidth = mWidth - getPaddingLeft() - getPaddingRight();
            final int contentHeight = mHeight - getPaddingTop() - getPaddingBottom();
            final int itemCount = getItemCount();
            final int firstVisibleItem = findFirstVisibleItemPosition();
            final int idx = findChildIndex(child);
            if (firstVisibleItem < 1 && idx == 0) {
                // when firstVisibleItem is 0 or -1, assume view with idx == 0 is first view
                if (itemCount == 1) {
                    layoutParams.leftMargin = (contentWidth - contentHeight) / 2 - child.getPaddingLeft() - child.getPaddingRight();
                } else {
                    layoutParams.leftMargin = 0;
                }
            } else {
                layoutParams.leftMargin = -Math.min((contentHeight * itemCount - contentWidth)
                        / (itemCount - 1) + child.getPaddingLeft() + child.getPaddingRight(), contentHeight);
            }
            super.measureChildWithMargins(child, widthUsed, heightUsed);
        }

        @Override
        public void onMeasure(Recycler recycler, State state, int widthSpec, int heightSpec) {
            final int height = MeasureSpec.getSize(heightSpec), width;
            if (getItemCount() > 1) {
                width = Math.round(height * 1.5f);
            } else if (getChildCount() > 0) {
                final View firstChild = getChildAt(0);
                width = height + firstChild.getPaddingLeft() + firstChild.getPaddingRight();
            } else {
                width = height;
            }
            mWidth = width;
            mHeight = height;
            super.onMeasure(recycler, state, MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), heightSpec);
        }
    }

    private static class InternalRecyclerView extends RecyclerView {
        public InternalRecyclerView(Context context) {
            super(context);
            setChildrenDrawingOrderEnabled(true);
        }

        @Override
        protected int getChildDrawingOrder(int childCount, int i) {
            return childCount - i - 1;
        }

        @Override
        public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
            return false;
        }
    }
}
