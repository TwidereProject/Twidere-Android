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

package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IUserListsAdapter;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.UserListViewHolder;

public abstract class AbsUserListsAdapter<D> extends LoadMoreSupportAdapter<ViewHolder> implements Constants,
        IUserListsAdapter<D> {

    public static final int ITEM_VIEW_TYPE_USER_LIST = 2;

    private final Context mContext;
    private final LayoutInflater mInflater;

    private final int mCardBackgroundColor;
    private final boolean mCompactCards;
    private final int mProfileImageStyle;
    private final int mTextSize;
    private final boolean mDisplayProfileImage;

    private final boolean mNameFirst;

    public AbsUserListsAdapter(final Context context, final boolean compact) {
        super(context);
        mContext = context;
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context, ThemeUtils.getThemeBackgroundOption(context), ThemeUtils.getUserThemeBackgroundAlpha(context));
        mInflater = LayoutInflater.from(context);
        mTextSize = mPreferences.getInt(KEY_TEXT_SIZE, context.getResources().getInteger(R.integer.default_text_size));
        mProfileImageStyle = Utils.getProfileImageStyle(mPreferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
        mDisplayProfileImage = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
        mNameFirst = mPreferences.getBoolean(KEY_NAME_FIRST, true);
        mCompactCards = compact;
    }

    @NonNull
    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public int getProfileImageStyle() {
        return mProfileImageStyle;
    }

    @Override
    public float getTextSize() {
        return mTextSize;
    }

    @NonNull
    @Override
    public AsyncTwitterWrapper getTwitterWrapper() {
        return mTwitterWrapper;
    }

    @NonNull
    @Override
    public UserColorNameManager getUserColorNameManager() {
        return mUserColorNameManager;
    }

    @Override
    public boolean isProfileImageEnabled() {
        return mDisplayProfileImage;
    }

    @Override
    public boolean isNameFirst() {
        return mNameFirst;
    }

    public abstract D getData();

    public boolean isUserList(int position) {
        return position < getUserListsCount();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_USER_LIST: {
                final View view;
                if (mCompactCards) {
                    view = mInflater.inflate(R.layout.card_item_user_list_compact, parent, false);
                    final View itemContent = view.findViewById(R.id.item_content);
                    itemContent.setBackgroundColor(mCardBackgroundColor);
                } else {
                    view = mInflater.inflate(R.layout.card_item_user_list, parent, false);
                    final CardView cardView = (CardView) view.findViewById(R.id.card);
                    cardView.setCardBackgroundColor(mCardBackgroundColor);
                }
                final UserListViewHolder holder = new UserListViewHolder(this, view);
                holder.setOnClickListeners();
                holder.setupViewOptions();
                return holder;
            }
            case ITEM_VIEW_TYPE_LOAD_INDICATOR: {
                final View view = mInflater.inflate(R.layout.card_item_load_indicator, parent, false);
                return new LoadIndicatorViewHolder(view);
            }
        }
        throw new IllegalStateException("Unknown view type " + viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM_VIEW_TYPE_USER_LIST: {
                bindUserList(((UserListViewHolder) holder), position);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getUserListsCount()) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        }
        return ITEM_VIEW_TYPE_USER_LIST;
    }

    @Override
    public void onItemActionClick(ViewHolder holder, int id, int position) {

    }

    @Override
    public void onItemMenuClick(ViewHolder holder, View menuView, int position) {

    }

    @Override
    public void onUserListClick(UserListViewHolder holder, int position) {
        if (mUserListAdapterListener == null) return;
        mUserListAdapterListener.onUserListClick(holder, position);
    }

    @Override
    public boolean onUserListLongClick(UserListViewHolder holder, int position) {
        return mUserListAdapterListener != null && mUserListAdapterListener.onUserListLongClick(holder, position);
    }

    public void setListener(UserListAdapterListener userListAdapterListener) {
        mUserListAdapterListener = userListAdapterListener;
    }

    @Override
    public boolean shouldShowAccountsColor() {
        return false;
    }

    @NonNull
    @Override
    public MediaLoaderWrapper getMediaLoader() {
        return mMediaLoader;
    }

    protected abstract void bindUserList(UserListViewHolder holder, int position);


    private UserListAdapterListener mUserListAdapterListener;

    public interface UserListAdapterListener {

        void onUserListClick(UserListViewHolder holder, int position);

        boolean onUserListLongClick(UserListViewHolder holder, int position);

    }
}
