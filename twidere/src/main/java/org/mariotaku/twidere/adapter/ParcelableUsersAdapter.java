/*
 * 				Twidere - Twitter client for Android
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

package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IUsersAdapter;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.UserViewHolder;

import java.util.List;

public class ParcelableUsersAdapter extends LoadMoreSupportAdapter<RecyclerView.ViewHolder>
        implements Constants, IUsersAdapter<List<ParcelableUser>> {

    public static final int ITEM_VIEW_TYPE_USER = 2;
    private final LayoutInflater mInflater;
    private final int mCardBackgroundColor;
    private final int mProfileImageStyle;
    private final int mTextSize;
    private final boolean mDisplayProfileImage;
    private final boolean mShowAbsoluteTime;
    private List<ParcelableUser> mData;
    private UserClickListener mUserClickListener;
    private RequestClickListener mRequestClickListener;
    private FollowClickListener mFollowClickListener;


    public ParcelableUsersAdapter(Context context) {
        super(context);
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context,
                ThemeUtils.getThemeBackgroundOption(context),
                ThemeUtils.getUserThemeBackgroundAlpha(context));
        mInflater = LayoutInflater.from(context);
        mTextSize = mPreferences.getInt(KEY_TEXT_SIZE, context.getResources().getInteger(R.integer.default_text_size));
        mProfileImageStyle = Utils.getProfileImageStyle(mPreferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
        mDisplayProfileImage = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE);
        mShowAbsoluteTime = mPreferences.getBoolean(KEY_SHOW_ABSOLUTE_TIME);
    }

    public List<ParcelableUser> getData() {
        return mData;
    }


    @Override
    public void setData(List<ParcelableUser> data) {
        mData = data;
        notifyDataSetChanged();
    }

    protected void bindUser(UserViewHolder holder, int position) {
        holder.displayUser(getUser(position));
    }

    @Override
    public int getItemCount() {
        final int position = getLoadMoreIndicatorPosition();
        int count = getUserCount();
        if ((position & IndicatorPosition.START) != 0) {
            count++;
        }
        if ((position & IndicatorPosition.END) != 0) {
            count++;
        }
        return count;
    }

    @Override
    public ParcelableUser getUser(int adapterPosition) {
        int dataPosition = adapterPosition - getUserStartIndex();
        if (dataPosition < 0 || dataPosition >= getUserCount()) return null;
        return mData.get(dataPosition);
    }

    public int getUserStartIndex() {
        final int position = getLoadMoreIndicatorPosition();
        int start = 0;
        if ((position & IndicatorPosition.START) != 0) {
            start += 1;
        }
        return start;
    }

    @Nullable
    @Override
    public String getUserId(int position) {
        if (position == getUserCount()) return null;
        return mData.get(position).key.getId();
    }

    @Override
    public int getUserCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    public boolean removeUserAt(int adapterPosition) {
        int dataPosition = adapterPosition - getUserStartIndex();
        if (dataPosition < 0 || dataPosition >= getUserCount()) return false;
        mData.remove(dataPosition);
        notifyItemRemoved(adapterPosition);
        return true;
    }

    public int findPosition(@NonNull final UserKey accountKey, @NonNull final UserKey userKey) {
        if (mData == null) return RecyclerView.NO_POSITION;
        for (int i = getUserStartIndex(), j = i + getUserCount(); i < j; i++) {
            final ParcelableUser user = mData.get(i);
            if (accountKey.equals(user.account_key) && userKey.equals(user.key)) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public int getProfileImageStyle() {
        return mProfileImageStyle;
    }

    @Override
    public float getTextSize() {
        return mTextSize;
    }

    @Override
    public boolean isProfileImageEnabled() {
        return mDisplayProfileImage;
    }

    @Override
    public boolean isShowAbsoluteTime() {
        return mShowAbsoluteTime;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_USER: {
                return createUserViewHolder(this, mInflater, parent, mCardBackgroundColor);
            }
            case ITEM_VIEW_TYPE_LOAD_INDICATOR: {
                final View view = mInflater.inflate(R.layout.card_item_load_indicator, parent, false);
                return new LoadIndicatorViewHolder(view);
            }
        }
        throw new IllegalStateException("Unknown view type " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM_VIEW_TYPE_USER: {
                bindUser(((UserViewHolder) holder), position);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if ((getLoadMoreIndicatorPosition() & IndicatorPosition.START) != 0 && position == 0) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        }
        if (position == getUserCount()) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        }
        return ITEM_VIEW_TYPE_USER;
    }

    @Nullable
    @Override
    public UserClickListener getUserClickListener() {
        return mUserClickListener;
    }

    public void setUserClickListener(UserClickListener userClickListener) {
        mUserClickListener = userClickListener;
    }

    @Override
    public RequestClickListener getRequestClickListener() {
        return mRequestClickListener;
    }

    public void setRequestClickListener(RequestClickListener requestClickListener) {
        mRequestClickListener = requestClickListener;
    }

    @Override
    public FollowClickListener getFollowClickListener() {
        return mFollowClickListener;
    }

    public void setFollowClickListener(FollowClickListener followClickListener) {
        mFollowClickListener = followClickListener;
    }

    @Override
    public boolean shouldShowAccountsColor() {
        return false;
    }

    public static UserViewHolder createUserViewHolder(IUsersAdapter<?> adapter,
                                                      LayoutInflater inflater, ViewGroup parent,
                                                      int cardBackgroundColor) {
        final View view = inflater.inflate(R.layout.card_item_user_compact, parent, false);
        final View itemContent = view.findViewById(R.id.item_content);
        itemContent.setBackgroundColor(cardBackgroundColor);
        final UserViewHolder holder = new UserViewHolder(adapter, view);
        holder.setOnClickListeners();
        holder.setupViewOptions();
        return holder;
    }
}
