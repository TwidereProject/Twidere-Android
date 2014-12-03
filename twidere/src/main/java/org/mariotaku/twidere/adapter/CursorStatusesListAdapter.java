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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesListAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.support.UserFragment;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.iface.ICardItemView.OnOverflowIconClickListener;

import java.util.Locale;

import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.findStatusInDatabases;
import static org.mariotaku.twidere.util.Utils.getCardHighlightOptionInt;
import static org.mariotaku.twidere.util.Utils.isFiltered;
import static org.mariotaku.twidere.util.Utils.openImage;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

public class CursorStatusesListAdapter extends BaseCursorAdapter implements IStatusesListAdapter<Cursor>, OnClickListener,
        OnOverflowIconClickListener {

    public static final String[] CURSOR_COLS = Statuses.COLUMNS;

    private final Context mContext;
    private final ImageLoaderWrapper mImageLoader;
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

    private ParcelableStatus.CursorIndices mIndices;

    private ScaleType mImagePreviewScaleType;

    public CursorStatusesListAdapter(final Context context) {
        this(context, Utils.isCompactCards(context));
    }

    public CursorStatusesListAdapter(final Context context, final boolean compactCards) {
        super(context, getItemResource(compactCards), null, new String[0], new int[0], 0);
        mContext = context;
        final TwidereApplication application = TwidereApplication.getInstance(context);
        mMultiSelectManager = application.getMultiSelectManager();
        mImageLoader = application.getImageLoaderWrapper();
        mDatabase = application.getSQLiteDatabase();
        mImageLoadingHandler = new ImageLoadingHandler();
        configBaseCardAdapter(context, this);
        setMaxAnimationPosition(-1);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final StatusViewHolder holder = (StatusViewHolder) view.getTag();
        holder.displayStatus(cursor, mIndices);
    }

    @Override
    public int findPositionByStatusId(final long status_id) {
        final Cursor c = getCursor();
        if (c == null || c.isClosed()) return -1;
        for (int i = 0, count = c.getCount(); i < count; i++) {
            if (c.moveToPosition(i) && c.getLong(mIndices.status_id) == status_id) return i;
        }
        return -1;
    }

    @Override
    public long getAccountId(final int position) {
        final Cursor c = getCursor();
        if (c == null || c.isClosed() || !c.moveToPosition(position)) return -1;
        return c.getLong(mIndices.account_id);
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
    public ParcelableStatus getLastStatus() {
        final Cursor c = getCursor();
        if (c == null || c.isClosed() || !c.moveToLast()) return null;
        final long account_id = c.getLong(mIndices.account_id);
        final long status_id = c.getLong(mIndices.status_id);
        return findStatusInDatabases(mContext, account_id, status_id);
    }

    @Override
    public long getLastStatusId() {
        final Cursor c = getCursor();
        try {
            if (c == null || c.isClosed() || !c.moveToLast()) return -1;
            return c.getLong(mIndices.status_id);
        } catch (final IllegalStateException e) {
            return -1;
        }
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public ImageLoadingHandler getImageLoadingHandler() {
        return mImageLoadingHandler;
    }

    @Override
    public ParcelableStatus getStatus(final int position) {
        final Cursor c = getCursor();
        if (c == null || c.isClosed() || !c.moveToPosition(position)) return null;
        return new ParcelableStatus(c, mIndices);
    }

    @Override
    public long getStatusId(final int position) {
        final Cursor c = getCursor();
        if (c == null || c.isClosed() || !c.moveToPosition(position)) return -1;
        return c.getLong(mIndices.status_id);
    }

    @Override
    public boolean isGapItem(int position) {
        return false;
    }

    @Override
    public boolean isLastItemFiltered() {
        return mFiltersEnabled && mIsLastItemFiltered;
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        final View view = super.newView(context, cursor, parent);
        final Object tag = view.getTag();
        if (!(tag instanceof StatusViewHolder)) {
            final StatusViewHolder holder = new StatusViewHolder(this, view);
            view.setTag(holder);
        }
        return view;
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
                final Activity activity = (Activity) getContext();
                final Bundle options = Utils.makeSceneTransitionOption(activity,
                        new Pair<>(view, UserFragment.TRANSITION_NAME_PROFILE_IMAGE));
                openUserProfile(mContext, status.account_id, status.user_id,
                        status.user_screen_name, options);
                break;
            }
        }
    }

    @Override
    public void onOverflowIconClick(final View view) {
        if (mMultiSelectManager.isActive()) return;
        final Object tag = view.getTag();
        if (tag instanceof StatusViewHolder) {
            final StatusViewHolder holder = (StatusViewHolder) tag;
            final int position = holder.getPosition();
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
    public void setData(final Cursor data) {
        swapCursor(data);
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
        rebuildFilterInfo(getCursor(), mIndices);
    }

    @Override
    public void setGapDisallowed(final boolean disallowed) {
        mGapDisallowed = disallowed;
    }

    @Override
    public void setHighlightKeyword(final String... keywords) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setIgnoredFilterFields(final boolean user, final boolean textPlain, final boolean textHtml,
                                       final boolean source, final boolean retweetedById) {
        mFilterIgnoreTextPlain = textPlain;
        mFilterIgnoreTextHtml = textHtml;
        mFilterIgnoreUser = user;
        mFilterIgnoreSource = source;
        mFilterRetweetedById = retweetedById;
        rebuildFilterInfo(getCursor(), mIndices);
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

    @Override
    public Cursor swapCursor(final Cursor cursor) {
        mIndices = cursor != null ? new ParcelableStatus.CursorIndices(cursor) : null;
        rebuildFilterInfo(cursor, mIndices);
        return super.swapCursor(cursor);
    }

    private void rebuildFilterInfo(final Cursor c, final ParcelableStatus.CursorIndices i) {
        if (i != null && c != null && moveCursorToLast(c)) {
            final long userId = mFilterIgnoreUser ? -1 : c.getLong(mIndices.user_id);
            final String textPlain = mFilterIgnoreTextPlain ? null : c.getString(mIndices.text_plain);
            final String textHtml = mFilterIgnoreTextHtml ? null : c.getString(mIndices.text_html);
            final String source = mFilterIgnoreSource ? null : c.getString(mIndices.source);
            final long retweetedById = mFilterRetweetedById ? -1 : c.getLong(mIndices.retweeted_by_user_id);
            mIsLastItemFiltered = isFiltered(mDatabase, userId, textPlain, textHtml, source, retweetedById);
        } else {
            mIsLastItemFiltered = false;
        }
    }

    private static int getItemResource(final boolean compactCards) {
        return compactCards ? R.layout.card_item_list_status_compat : R.layout.card_item_list_status;
    }

    private static boolean moveCursorToLast(final Cursor c) {
        if (c == null || c.isClosed()) return false;
        try {
            return c.moveToNext();
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public void onGapClick(StatusViewHolder holder, int position) {

    }
}
