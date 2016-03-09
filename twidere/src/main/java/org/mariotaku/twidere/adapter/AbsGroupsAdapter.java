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
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IGroupsAdapter;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.GroupViewHolder;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;

public abstract class AbsGroupsAdapter<D> extends LoadMoreSupportAdapter<ViewHolder> implements Constants,
        IGroupsAdapter<D> {

    public static final int ITEM_VIEW_TYPE_USER_LIST = 2;

    private final LayoutInflater mInflater;

    private final int mCardBackgroundColor;
    private final int mProfileImageStyle;
    private final int mTextSize;
    private final boolean mDisplayProfileImage;
    private final boolean mShowAbsoluteTime;

    private final boolean mNameFirst;
    private final EventListener mEventListener;

    private GroupAdapterListener mGroupAdapterListener;

    public AbsGroupsAdapter(final Context context) {
        super(context);
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context,
                ThemeUtils.getThemeBackgroundOption(context),
                ThemeUtils.getUserThemeBackgroundAlpha(context));
        mEventListener = new EventListener(this);
        mInflater = LayoutInflater.from(context);
        mTextSize = mPreferences.getInt(KEY_TEXT_SIZE, context.getResources().getInteger(R.integer.default_text_size));
        mProfileImageStyle = Utils.getProfileImageStyle(mPreferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
        mDisplayProfileImage = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
        mNameFirst = mPreferences.getBoolean(KEY_NAME_FIRST, true);
        mShowAbsoluteTime = mPreferences.getBoolean(KEY_SHOW_ABSOLUTE_TIME, false);
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
    public boolean isNameFirst() {
        return mNameFirst;
    }

    public abstract D getData();

    @Override
    public boolean isShowAbsoluteTime() {
        return mShowAbsoluteTime;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_USER_LIST: {
                final View view;
                view = mInflater.inflate(R.layout.card_item_group_compact, parent, false);
                final View itemContent = view.findViewById(R.id.item_content);
                itemContent.setBackgroundColor(mCardBackgroundColor);
                final GroupViewHolder holder = new GroupViewHolder(this, view);
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
                bindGroup(((GroupViewHolder) holder), position);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if ((getLoadMoreIndicatorPosition() & IndicatorPosition.START) != 0 && position == 0) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        }
        if (position == getGroupsCount()) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        }
        return ITEM_VIEW_TYPE_USER_LIST;
    }

    public void setListener(GroupAdapterListener groupAdapterListener) {
        mGroupAdapterListener = groupAdapterListener;
    }

    @Override
    public boolean shouldShowAccountsColor() {
        return false;
    }

    protected abstract void bindGroup(GroupViewHolder holder, int position);

    @Nullable
    @Override
    public IGroupsAdapter.GroupAdapterListener getGroupAdapterListener() {
        return mEventListener;
    }

    public interface GroupAdapterListener {

        void onGroupClick(GroupViewHolder holder, int position);

        boolean onGroupLongClick(GroupViewHolder holder, int position);

    }

    static class EventListener implements IGroupsAdapter.GroupAdapterListener {

        private final AbsGroupsAdapter<?> mAdapter;

        public EventListener(AbsGroupsAdapter<?> adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onGroupClick(GroupViewHolder holder, int position) {
            final GroupAdapterListener listener = mAdapter.mGroupAdapterListener;
            if (listener == null) return;
            listener.onGroupClick(holder, position);
        }

        @Override
        public boolean onGroupLongClick(GroupViewHolder holder, int position) {
            final GroupAdapterListener listener = mAdapter.mGroupAdapterListener;
            return listener != null && listener.onGroupLongClick(holder, position);
        }
    }
}
