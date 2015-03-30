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

package org.mariotaku.twidere.popup;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.ThemedViewFactory;
import org.mariotaku.twidere.util.Utils;

import java.util.List;

/**
 * Created by mariotaku on 15/1/12.
 */
public class AccountSelectorPopupWindow {

    private final Context mContext;
    private final View mAnchor;
    private final PopupWindow mPopup;
    private final AccountsGridAdapter mAdapter;
    private final GridView mGridView;
    private AccountSelectionListener mAccountSelectionListener;

    public AccountSelectorPopupWindow(Context context, View anchor) {
        mContext = context;
        mAnchor = anchor;
        final int themeAttr;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            themeAttr = android.R.attr.actionOverflowMenuStyle;
        } else {
            themeAttr = android.R.attr.popupMenuStyle;
        }
        mAdapter = new AccountsGridAdapter(context);
        final Resources resources = context.getResources();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final int themeColor = ThemeUtils.getUserAccentColor(context);
        if (!(context instanceof Factory)) {
            inflater.setFactory2(new ThemedViewFactory(themeColor));
        }
        final View contentView = inflater.inflate(R.layout.popup_account_selector, null);
        mGridView = (GridView) contentView.findViewById(R.id.grid_view);
        mGridView.setAdapter(mAdapter);
        mGridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAccountSelectionListener == null) return;

                mAccountSelectionListener.onSelectionChanged(getSelectedAccountIds());
            }
        });
        mPopup = new PopupWindow(context, null, themeAttr);
        mPopup.setFocusable(true);
        mPopup.setWidth(Utils.getActionBarHeight(context) * 2);
        mPopup.setWindowLayoutMode(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopup.setContentView(contentView);
    }

    public void setAccountSelectionListener(AccountSelectionListener listener) {
        mAccountSelectionListener = listener;
    }

    public boolean isShowing() {
        return mPopup.isShowing();
    }

    public void dismiss() {
        mPopup.dismiss();
    }

    public void show() {
        mPopup.showAsDropDown(mAnchor);
    }

    public interface AccountSelectionListener {

        public void onSelectionChanged(long[] accountIds);

    }

    public void setSelectedAccountIds(long[] accountIds) {
        if (accountIds == null) {
            mGridView.clearChoices();
        }
        for (int i = 0, j = mAdapter.getCount(); i < j; i++) {
            mGridView.setItemChecked(i, ArrayUtils.contains(accountIds, mAdapter.getItem(i).account_id));
        }
    }

    @NonNull
    public long[] getSelectedAccountIds() {
        final long[] accountIds = new long[mGridView.getCheckedItemCount()];
        final SparseBooleanArray positions = mGridView.getCheckedItemPositions();
        for (int i = 0, j = positions.size(), k = 0; i < j; i++) {
            if (positions.valueAt(i)) {
                accountIds[k++] = mAdapter.getItem(positions.keyAt(i)).account_id;
            }
        }
        return accountIds;
    }

    public void setAccounts(List<ParcelableAccount> accounts) {
        mAdapter.clear();
        if (accounts != null) {
            mAdapter.addAll(accounts);
        }
    }

    private static class AccountsGridAdapter extends ArrayAdapter<ParcelableAccount> {

        private final MediaLoaderWrapper mImageLoader;

        public AccountsGridAdapter(Context context) {
            super(context, R.layout.grid_item_selector_account);
            mImageLoader = TwidereApplication.getInstance(context).getMediaLoaderWrapper();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            final ParcelableAccount account = getItem(position);
            final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
            mImageLoader.displayProfileImage(icon, account.profile_image_url);
            return view;
        }
    }

}
