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

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesListAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.StatusListViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.iface.ICardItemView.OnOverflowIconClickListener;

import java.util.List;
import java.util.Locale;

import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.getCardHighlightOptionInt;
import static org.mariotaku.twidere.util.Utils.isFiltered;
import static org.mariotaku.twidere.util.Utils.openImage;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

public class ParcelableStatusesListAdapter extends BaseArrayAdapter<ParcelableStatus> implements
        IStatusesListAdapter<List<ParcelableStatus>>, OnClickListener, OnOverflowIconClickListener {

    private final Context mContext;
    private final MultiSelectManager mMultiSelectManager;
    private final SQLiteDatabase mDatabase;
    private final ImageLoadingHandler mImageLoadingHandler;

    private MenuButtonClickListener mListener;


    private boolean mDisplayImagePreview, mGapDisallowed, mMentionsHighlightDisabled, mFavoritesHighlightDisabled,
            mDisplaySensitiveContents, mIndicateMyStatusDisabled, mIsLastItemFiltered, mFiltersEnabled,
            mAnimationEnabled;
    private boolean mFilterIgnoreUser, mFilterIgnoreSource, mFilterIgnoreTextHtml, mFilterIgnoreTextPlain,
            mFilterRetweetedById;
    private int mMaxAnimationPosition, mCardHighlightOption;
    private ScaleType mImagePreviewScaleType;
    private String[] mHighlightKeywords;

    public ParcelableStatusesListAdapter(final Context context) {
        this(context, Utils.isCompactCards(context));
    }

    public ParcelableStatusesListAdapter(final Context context, final boolean compactCards) {
        super(context, getItemResource(compactCards));
        mContext = context;
        final TwidereApplication app = TwidereApplication.getInstance(context);
        mMultiSelectManager = app.getMultiSelectManager();
        mDatabase = app.getSQLiteDatabase();
        mImageLoadingHandler = new ImageLoadingHandler();
        configBaseCardAdapter(context, this);
        setMaxAnimationPosition(-1);
    }

    @Override
    public int findPositionByStatusId(final long status_id) {
        for (int i = 0, count = getCount(); i < count; i++) {
            if (getItem(i).id == status_id) return i;
        }
        return -1;
    }

    @Override
    public long getAccountId(final int position) {
        if (position >= 0 && position < getCount()) {
            final ParcelableStatus status = getItem(position);
            return status != null ? status.account_id : -1;
        }
        return -1;
    }

    @Override
    public int getStatusCount() {
        return super.getCount();
    }

    @Override
    public void onItemMenuClick(StatusViewHolder holder, int position) {

    }

    @Override
    public void onStatusClick(StatusViewHolder holder, int position) {

    }

    @Override
    public void onUserProfileClick(StatusViewHolder holder, int position) {

    }

    @Override
    public int getCount() {
        final int count = super.getCount();
        return mFiltersEnabled && mIsLastItemFiltered && count > 0 ? count - 1 : count;
    }

    @Override
    public long getItemId(final int position) {
        final ParcelableStatus item = getItem(position);
        return item != null ? item.id : -1;
    }

    @Override
    public ParcelableStatus getLastStatus() {
        if (super.getCount() == 0) return null;
        return getItem(super.getCount() - 1);
    }

    @Override
    public long getLastStatusId() {
        if (super.getCount() == 0) return -1;
        return getItem(super.getCount() - 1).id;
    }

    @Override
    public ImageLoadingHandler getImageLoadingHandler() {
        return mImageLoadingHandler;
    }

    @Override
    public ParcelableStatus getStatus(final int position) {
        return getItem(position);
    }

    @Override
    public long getStatusId(final int position) {
        if (position >= 0 && position < getCount()) {
            final ParcelableStatus status = getItem(position);
            return status != null ? status.id : -1;
        }
        return -1;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        final Object tag = view.getTag();
        final StatusViewHolder holder;
        if (tag instanceof StatusViewHolder) {
            holder = (StatusViewHolder) tag;
        } else {
            holder = new StatusViewHolder(this, view);
            view.setTag(holder);
        }
        final ParcelableStatus status = getItem(position);
        holder.displayStatus(status);
        return view;
    }

    @Override
    public boolean isGapItem(int position) {
        return false;
    }

    @Override
    public void onGapClick(StatusViewHolder holder, int position) {

    }

    @Override
    public boolean isLastItemFiltered() {
        return mIsLastItemFiltered;
    }

    @Override
    public void onClick(final View view) {
        if (mMultiSelectManager.isActive()) return;
        final Object tag = view.getTag();
        final int position = tag instanceof Integer ? (Integer) tag : -1;
        if (position == -1) return;
        switch (view.getId()) {
            case R.id.image_preview: {
                final ParcelableStatus status = getStatus(position);
                if (status == null || status.first_media == null) return;
                openImage(mContext, status.account_id, status.first_media, status.is_possibly_sensitive);
                break;
            }
            case R.id.my_profile_image:
            case R.id.profile_image: {
                final ParcelableStatus status = getStatus(position);
                if (status == null) return;
                if (mContext instanceof Activity) {
                    openUserProfile(mContext, status.account_id, status.user_id,
                            status.user_screen_name, null);
                }
                break;
            }
        }
    }

    @Override
    public void onOverflowIconClick(final View view) {
        if (mMultiSelectManager.isActive()) return;
        final Object tag = view.getTag();
        if (tag instanceof StatusListViewHolder) {
            final StatusListViewHolder holder = (StatusListViewHolder) tag;
            final int position = holder.position;
            if (position == -1 || mListener == null) return;
            mListener.onMenuButtonClick(view, position, getItemId(position));
        }
    }

    @Override
    public void setAnimationEnabled(final boolean anim) {
        mAnimationEnabled = anim;
    }

    @Override
    public void setCardHighlightOption(final String option) {
        mCardHighlightOption = getCardHighlightOptionInt(option);
    }

    @Override
    public void setData(final List<ParcelableStatus> data) {
        clear();
        if (data != null && !data.isEmpty()) {
            addAll(data);
        }
        rebuildFilterInfo();
    }

    @Override
    public void setDisplayImagePreview(final boolean display) {
        mDisplayImagePreview = display;
    }

    @Override
    public void setDisplaySensitiveContents(final boolean display) {
        mDisplaySensitiveContents = display;
    }

    @Override
    public void setFavoritesHightlightDisabled(final boolean disable) {
        mFavoritesHighlightDisabled = disable;
    }

    @Override
    public void setFiltersEnabled(final boolean enabled) {
        if (mFiltersEnabled == enabled) return;
        mFiltersEnabled = enabled;
        rebuildFilterInfo();
    }

    @Override
    public void setGapDisallowed(final boolean disallowed) {
        mGapDisallowed = disallowed;
    }

    @Override
    public void setHighlightKeyword(final String... keywords) {
        mHighlightKeywords = keywords;
    }

    @Override
    public void setIgnoredFilterFields(final boolean user, final boolean textPlain, final boolean textHtml,
                                       final boolean source, final boolean retweetedById) {
        mFilterIgnoreTextPlain = textPlain;
        mFilterIgnoreTextHtml = textHtml;
        mFilterIgnoreUser = user;
        mFilterIgnoreSource = source;
        mFilterRetweetedById = retweetedById;
        rebuildFilterInfo();
    }

    @Override
    public void setImagePreviewScaleType(final String scaleTypeString) {
        final ScaleType scaleType = ScaleType.valueOf(scaleTypeString.toUpperCase(Locale.US));
        mImagePreviewScaleType = scaleType;
    }

    @Override
    public void setIndicateMyStatusDisabled(final boolean disable) {
        mIndicateMyStatusDisabled = disable;
    }

    @Override
    public void setMaxAnimationPosition(final int position) {
        mMaxAnimationPosition = position;
    }

    @Override
    public void setMentionsHightlightDisabled(final boolean disable) {
        mMentionsHighlightDisabled = disable;
    }

    @Override
    public void setMenuButtonClickListener(final MenuButtonClickListener listener) {
        mListener = listener;
    }

    private void rebuildFilterInfo() {
        if (!isEmpty()) {
            final ParcelableStatus last = getItem(super.getCount() - 1);
            final long user_id = mFilterIgnoreUser ? -1 : last.user_id;
            final String text_plain = mFilterIgnoreTextPlain ? null : last.text_plain;
            final String text_html = mFilterIgnoreTextHtml ? null : last.text_html;
            final String source = mFilterIgnoreSource ? null : last.source;
            final long retweeted_by_id = mFilterRetweetedById ? -1 : last.retweeted_by_id;
            mIsLastItemFiltered = isFiltered(mDatabase, user_id, text_plain, text_html, source, retweeted_by_id);
        } else {
            mIsLastItemFiltered = false;
        }
        notifyDataSetChanged();
    }

    private static int getItemResource(final boolean compactCards) {
        return compactCards ? R.layout.card_item_list_status_compat : R.layout.card_item_list_status;
    }

}
