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

package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.Space;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.library.objectcursor.ObjectCursor;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusCursorIndices;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.CardMediaContainer;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.holder.EmptyViewHolder;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

import java.util.List;

/**
 * Created by mariotaku on 15/10/26.
 */
public abstract class ParcelableStatusesAdapter extends LoadMoreSupportAdapter<RecyclerView.ViewHolder>
        implements Constants, IStatusesAdapter<List<ParcelableStatus>> {
    public static final int ITEM_VIEW_TYPE_STATUS = 2;
    public static final int ITEM_VIEW_TYPE_EMPTY = 3;
    private final LayoutInflater mInflater;
    private final MediaLoadingHandler mLoadingHandler;
    private final TwidereLinkify mLinkify;
    private final int mTextSize;
    @ShapedImageView.ShapeStyle
    private final int mProfileImageStyle;
    @CardMediaContainer.PreviewStyle
    private final int mMediaPreviewStyle;
    @TwidereLinkify.HighlightStyle
    private final int mLinkHighlightingStyle;
    private final boolean mNameFirst;
    private final boolean mDisplayMediaPreview;
    private final boolean mDisplayProfileImage;
    private final boolean mSensitiveContentEnabled;
    private final boolean mShowCardActions;
    private final boolean mUseStarsForLikes;
    private final boolean mShowAbsoluteTime;
    @Nullable
    private IStatusViewHolder.StatusClickListener mStatusClickListener;
    private boolean mShowInReplyTo;
    private boolean mShowAccountsColor;
    private List<ParcelableStatus> mData;
    private long mShowingActionCardId = RecyclerView.NO_ID;
    private boolean mLastItemFiltered;

    public ParcelableStatusesAdapter(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        mLoadingHandler = new MediaLoadingHandler(getProgressViewIds());
        mTextSize = mPreferences.getInt(KEY_TEXT_SIZE, context.getResources().getInteger(R.integer.default_text_size));
        mProfileImageStyle = Utils.getProfileImageStyle(mPreferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
        mMediaPreviewStyle = Utils.getMediaPreviewStyle(mPreferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
        mLinkHighlightingStyle = Utils.getLinkHighlightingStyleInt(mPreferences.getString(KEY_LINK_HIGHLIGHT_OPTION, null));
        mNameFirst = mPreferences.getBoolean(KEY_NAME_FIRST, true);
        mDisplayProfileImage = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
        mDisplayMediaPreview = Utils.isMediaPreviewEnabled(context, mPreferences);
        mSensitiveContentEnabled = mPreferences.getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, false);
        mShowCardActions = !mPreferences.getBoolean(KEY_HIDE_CARD_ACTIONS, false);
        mUseStarsForLikes = mPreferences.getBoolean(KEY_I_WANT_MY_STARS_BACK);
        mShowAbsoluteTime = mPreferences.getBoolean(KEY_SHOW_ABSOLUTE_TIME, false);
        final StatusAdapterLinkClickHandler<List<ParcelableStatus>> handler = new StatusAdapterLinkClickHandler<>(context,
                mPreferences);
        mLinkify = new TwidereLinkify(handler);
        handler.setAdapter(this);
        setShowInReplyTo(true);
        setHasStableIds(true);
    }

    @Override
    public boolean isGapItem(int position) {
        int dataPosition = position - getStatusStartIndex();
        final int statusCount = getStatusCount();
        if (dataPosition < 0 || dataPosition >= statusCount) return false;
        // Don't show gap if it's last item
        if (dataPosition == statusCount - 1) return false;
        if (mData instanceof ObjectCursor) {
            final Cursor cursor = ((ObjectCursor) mData).getCursor();
            if (!cursor.moveToPosition(dataPosition)) return false;
            final ParcelableStatusCursorIndices indices = (ParcelableStatusCursorIndices) ((ObjectCursor) mData).getIndices();
            return cursor.getShort(indices.is_gap) == 1;
        }
        return mData.get(dataPosition).is_gap;
    }

    @Override
    public ParcelableStatus getStatus(int adapterPosition) {
        int dataPosition = adapterPosition - getStatusStartIndex();
        if (dataPosition < 0 || dataPosition >= getRawStatusCount()) return null;
        return mData.get(dataPosition);
    }

    @Override
    public int getStatusCount() {
        if (mData == null) return 0;
        if (mLastItemFiltered) return mData.size() - 1;
        return mData.size();
    }

    @Override
    public int getRawStatusCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    @Override
    public long getItemId(int adapterPosition) {
        int dataPosition = adapterPosition - getStatusStartIndex();
        if (dataPosition < 0 || dataPosition >= getStatusCount()) return adapterPosition;
        if (mData instanceof ObjectCursor) {
            final Cursor cursor = ((ObjectCursor) mData).getCursor();
            if (!cursor.moveToPosition(dataPosition)) return -1;
            final ParcelableStatusCursorIndices indices = (ParcelableStatusCursorIndices) ((ObjectCursor) mData).getIndices();
            final UserKey accountKey = UserKey.valueOf(cursor.getString(indices.account_key));
            final String id = cursor.getString(indices.id);
            return ParcelableStatus.calculateHashCode(accountKey, id);
        }
        return mData.get(dataPosition).hashCode();
    }

    @Nullable
    @Override
    public String getStatusId(int adapterPosition) {
        int dataPosition = adapterPosition - getStatusStartIndex();
        if (dataPosition < 0 || dataPosition >= getRawStatusCount()) return null;
        if (mData instanceof ObjectCursor) {
            final Cursor cursor = ((ObjectCursor) mData).getCursor();
            if (!cursor.moveToPosition(dataPosition)) return null;
            final ParcelableStatusCursorIndices indices = (ParcelableStatusCursorIndices) ((ObjectCursor) mData).getIndices();
            return cursor.getString(indices.id);
        }
        return mData.get(dataPosition).id;
    }

    @Override
    public long getStatusTimestamp(int adapterPosition) {
        int dataPosition = adapterPosition - getStatusStartIndex();
        if (dataPosition < 0 || dataPosition >= getRawStatusCount()) return -1;
        if (mData instanceof ObjectCursor) {
            final Cursor cursor = ((ObjectCursor) mData).getCursor();
            if (!cursor.moveToPosition(dataPosition)) return -1;
            final ParcelableStatusCursorIndices indices = (ParcelableStatusCursorIndices) ((ObjectCursor) mData).getIndices();
            return cursor.getLong(indices.timestamp);
        }
        return mData.get(dataPosition).timestamp;
    }

    @Override
    public long getStatusPositionKey(int adapterPosition) {
        int dataPosition = adapterPosition - getStatusStartIndex();
        if (dataPosition < 0 || dataPosition >= getRawStatusCount()) return -1;
        if (mData instanceof ObjectCursor) {
            final Cursor cursor = ((ObjectCursor) mData).getCursor();
            if (!cursor.moveToPosition(dataPosition)) return -1;
            final ParcelableStatusCursorIndices indices = (ParcelableStatusCursorIndices) ((ObjectCursor) mData).getIndices();
            final long positionKey = cursor.getLong(indices.position_key);
            if (positionKey > 0) return positionKey;
            return cursor.getLong(indices.timestamp);
        }
        final ParcelableStatus status = mData.get(dataPosition);
        final long positionKey = status.position_key;
        if (positionKey > 0) return positionKey;
        return status.timestamp;
    }

    @Override
    public UserKey getAccountKey(int adapterPosition) {
        int dataPosition = adapterPosition - getStatusStartIndex();
        if (dataPosition < 0 || dataPosition >= getRawStatusCount()) return null;
        if (mData instanceof ObjectCursor) {
            final Cursor cursor = ((ObjectCursor) mData).getCursor();
            if (!cursor.moveToPosition(dataPosition)) return null;
            final ParcelableStatusCursorIndices indices = (ParcelableStatusCursorIndices) ((ObjectCursor) mData).getIndices();
            return UserKey.valueOf(cursor.getString(indices.account_key));
        }
        return mData.get(dataPosition).account_key;
    }

    @Override
    public boolean setData(List<ParcelableStatus> data) {
        boolean changed = true;
        if (data instanceof ObjectCursor || data == null || data.isEmpty()) {
            mLastItemFiltered = false;
        } else {
            mLastItemFiltered = data.get(data.size() - 1).is_filtered;
            changed = !data.equals(mData);
        }
        mData = data;
        notifyDataSetChanged();
        return changed;
    }

    public List<ParcelableStatus> getData() {
        return mData;
    }

    protected abstract int[] getProgressViewIds();

    @Override
    public boolean shouldShowAccountsColor() {
        return mShowAccountsColor;
    }

    @Override
    public final MediaLoadingHandler getMediaLoadingHandler() {
        return mLoadingHandler;
    }

    @Nullable
    @Override
    public IStatusViewHolder.StatusClickListener getStatusClickListener() {
        return mStatusClickListener;
    }

    public void setStatusClickListener(@Nullable IStatusViewHolder.StatusClickListener statusClickListener) {
        mStatusClickListener = statusClickListener;
    }

    @Override
    public final int getProfileImageStyle() {
        return mProfileImageStyle;
    }

    @Override
    public final int getMediaPreviewStyle() {
        return mMediaPreviewStyle;
    }

    @Override
    public final float getTextSize() {
        return mTextSize;
    }

    @Override
    public TwidereLinkify getTwidereLinkify() {
        return mLinkify;
    }

    @Override
    public boolean isMediaPreviewEnabled() {
        return mDisplayMediaPreview;
    }

    @Override
    public int getLinkHighlightingStyle() {
        return mLinkHighlightingStyle;
    }

    @Override
    public boolean isNameFirst() {
        return mNameFirst;
    }

    @Override
    public boolean isSensitiveContentEnabled() {
        return mSensitiveContentEnabled;
    }

    @Override
    public boolean isCardActionsShown(int position) {
        if (position == RecyclerView.NO_POSITION) return mShowCardActions;
        return mShowCardActions || mShowingActionCardId == getItemId(position);
    }

    @Override
    public void showCardActions(int position) {
        if (mShowingActionCardId != RecyclerView.NO_ID) {
            final int pos = findPositionByItemId(mShowingActionCardId);
            if (pos != RecyclerView.NO_POSITION) {
                notifyItemChanged(pos);
            }

        }
        mShowingActionCardId = getItemId(position);
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position);
        }
    }

    @Override
    public boolean isProfileImageEnabled() {
        return mDisplayProfileImage;
    }

    @Override
    public boolean shouldUseStarsForLikes() {
        return mUseStarsForLikes;
    }

    public boolean isShowInReplyTo() {
        return mShowInReplyTo;
    }

    public void setShowInReplyTo(boolean showInReplyTo) {
        if (mShowInReplyTo == showInReplyTo) return;
        mShowInReplyTo = showInReplyTo;
        notifyDataSetChanged();
    }

    public boolean isStatus(int position) {
        return position < getStatusCount();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_STATUS: {
                return (RecyclerView.ViewHolder) onCreateStatusViewHolder(parent);
            }
            case ITEM_VIEW_TYPE_GAP: {
                final View view = mInflater.inflate(R.layout.card_item_gap, parent, false);
                return new GapViewHolder(this, view);
            }
            case ITEM_VIEW_TYPE_LOAD_INDICATOR: {
                final View view = mInflater.inflate(R.layout.card_item_load_indicator, parent, false);
                return new LoadIndicatorViewHolder(view);
            }
            case ITEM_VIEW_TYPE_EMPTY: {
                return new EmptyViewHolder(new Space(getContext()));
            }
        }
        throw new IllegalStateException("Unknown view type " + viewType);
    }

    protected LayoutInflater getInflater() {
        return mInflater;
    }

    @NonNull
    protected abstract IStatusViewHolder onCreateStatusViewHolder(ViewGroup parent);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM_VIEW_TYPE_STATUS: {
                bindStatus((IStatusViewHolder) holder, position);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if ((getLoadMoreIndicatorPosition() & IndicatorPosition.START) != 0 && position == 0) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        }
        if (position == getStatusCount()) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        } else if (isGapItem(position)) {
            return ITEM_VIEW_TYPE_GAP;
        }
        if (isFiltered(position)) return ITEM_VIEW_TYPE_EMPTY;
        return ITEM_VIEW_TYPE_STATUS;
    }

    @Override
    public boolean isShowAbsoluteTime() {
        return mShowAbsoluteTime;
    }

    @Override
    public final int getItemCount() {
        final int position = getLoadMoreIndicatorPosition();
        int count = 0;
        if ((position & IndicatorPosition.START) != 0) {
            count += 1;
        }
        count += getStatusCount();
        if ((position & IndicatorPosition.END) != 0) {
            count += 1;
        }
        return count;
    }

    public void setShowAccountsColor(boolean showAccountsColor) {
        if (mShowAccountsColor == showAccountsColor) return;
        mShowAccountsColor = showAccountsColor;
        notifyDataSetChanged();
    }

    @Nullable
    @Override
    public ParcelableStatus findStatusById(UserKey accountKey, String statusId) {
        for (int i = 0, j = getStatusCount(); i < j; i++) {
            if (accountKey.equals(getAccountKey(i)) && statusId.equals(getStatusId(i))) {
                return getStatus(i);
            }
        }
        return null;
    }

    protected void bindStatus(IStatusViewHolder holder, int position) {
        holder.displayStatus(getStatus(position), isShowInReplyTo());
    }

    @Nullable
    @Override
    public GapClickListener getGapClickListener() {
        return mStatusClickListener;
    }

    public int getStatusStartIndex() {
        final int position = getLoadMoreIndicatorPosition();
        int start = 0;
        if ((position & IndicatorPosition.START) != 0) {
            start += 1;
        }
        return start;
    }

    private boolean isFiltered(int position) {
        if (mData instanceof ObjectCursor) return false;
        return getStatus(position).is_filtered;
    }

}
