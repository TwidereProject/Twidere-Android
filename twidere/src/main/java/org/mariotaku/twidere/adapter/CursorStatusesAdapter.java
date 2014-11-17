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
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUserMention;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.iface.ICardItemView.OnOverflowIconClickListener;

import java.util.Locale;

import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserColor;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserNickname;
import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.findStatusInDatabases;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getCardHighlightColor;
import static org.mariotaku.twidere.util.Utils.getCardHighlightOptionInt;
import static org.mariotaku.twidere.util.Utils.isFiltered;
import static org.mariotaku.twidere.util.Utils.openImage;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

public class CursorStatusesAdapter extends BaseCursorAdapter implements IStatusesAdapter<Cursor>, OnClickListener,
        OnOverflowIconClickListener {

    public static final String[] CURSOR_COLS = Statuses.COLUMNS;

    private final Context mContext;
    private final ImageLoaderWrapper mImageLoader;
    private final MultiSelectManager mMultiSelectManager;
    private final SQLiteDatabase mDatabase;
    private final ImageLoadingHandler mImageLoadingHandler;

    private MenuButtonClickListener mListener;

    private final boolean mPlainList;

    private boolean mDisplayImagePreview, mGapDisallowed, mMentionsHighlightDisabled, mFavoritesHighlightDisabled,
            mDisplaySensitiveContents, mIndicateMyStatusDisabled, mIsLastItemFiltered, mFiltersEnabled,
            mAnimationEnabled;
    private boolean mFilterIgnoreUser, mFilterIgnoreSource, mFilterIgnoreTextHtml, mFilterIgnoreTextPlain,
            mFilterRetweetedById;
    private int mMaxAnimationPosition, mCardHighlightOption;

    private ParcelableStatus.CursorIndices mIndices;

    private ScaleType mImagePreviewScaleType;

    public CursorStatusesAdapter(final Context context) {
        this(context, Utils.isCompactCards(context), Utils.isPlainListStyle(context));
    }

    public CursorStatusesAdapter(final Context context, final boolean compactCards, final boolean plainList) {
        super(context, getItemResource(compactCards), null, new String[0], new int[0], 0);
        mPlainList = plainList;
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
        final int position = cursor.getPosition();
        final StatusViewHolder holder = (StatusViewHolder) view.getTag();

        final boolean isGap = cursor.getShort(mIndices.is_gap) == 1;
        final boolean showGap = isGap && !mGapDisallowed && position != getCount() - 1;

        holder.setShowAsGap(showGap);
        holder.position = position;
        holder.setDisplayProfileImage(isDisplayProfileImage());
        holder.setCardHighlightOption(mCardHighlightOption);

        if (!showGap) {

            // Clear images in prder to prevent images in recycled view shown.

            final TwidereLinkify linkify = getLinkify();
            final boolean showAccountColor = isShowAccountColor();

            final long accountId = cursor.getLong(mIndices.account_id);
            final long userId = cursor.getLong(mIndices.user_id);
            final long timestamp = cursor.getLong(mIndices.status_timestamp);
            final long retweetTimestamp = cursor.getLong(mIndices.retweet_timestamp);
            final long retweetCount = cursor.getLong(mIndices.retweet_count);
            final long retweetedByUserId = cursor.getLong(mIndices.retweeted_by_user_id);
            final long inReplyToUserId = cursor.getLong(mIndices.in_reply_to_user_id);

            final String retweetedByName = cursor.getString(mIndices.retweeted_by_user_name);
            final String retweetedByScreenName = cursor.getString(mIndices.retweeted_by_user_screen_name);
            final String text = getLinkHighlightOption() != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE ? cursor
                    .getString(mIndices.text_html) : cursor.getString(mIndices.text_unescaped);
            final String screen_name = cursor.getString(mIndices.user_screen_name);
            final String name = cursor.getString(mIndices.user_name);
            final String inReplyToName = cursor.getString(mIndices.in_reply_to_user_name);
            final String inReplyToScreenName = cursor.getString(mIndices.in_reply_to_user_screen_name);
            final ParcelableMedia[] media = ParcelableMedia.fromJSONString(cursor.getString(mIndices.media));
            final String firstMedia = media != null && media.length > 0 ? media[0].media_url : null;

            // Tweet type (favorite/location/media)
            final boolean isFavorite = cursor.getShort(mIndices.is_favorite) == 1;
            final boolean hasLocation = !TextUtils.isEmpty(cursor.getString(mIndices.location));
            final boolean possiblySensitive = cursor.getInt(mIndices.is_possibly_sensitive) == 1;
            final boolean hasMedia = media != null && media.length > 0;

            // User type (protected/verified)
            final boolean isVerified = cursor.getShort(mIndices.is_verified) == 1;
            final boolean isProtected = cursor.getShort(mIndices.is_protected) == 1;

            final boolean isRetweet = cursor.getShort(mIndices.is_retweet) == 1;
            final boolean isReply = cursor.getLong(mIndices.in_reply_to_status_id) > 0;
            final boolean isMention = ParcelableUserMention.hasMention(cursor.getString(mIndices.mentions), accountId);
            final boolean isMyStatus = accountId == userId;

            holder.setUserColor(getUserColor(mContext, userId));
            if (isRetweet) {
                holder.setUserColor(getUserColor(mContext, userId), getUserColor(mContext, retweetedByUserId));
            } else {
                holder.setUserColor(getUserColor(mContext, userId));
            }
            holder.setHighlightColor(getCardHighlightColor(!mMentionsHighlightDisabled && isMention,
                    !mFavoritesHighlightDisabled && isFavorite, isRetweet));

            holder.setAccountColorEnabled(showAccountColor);

            if (showAccountColor) {
                holder.setAccountColor(getAccountColor(mContext, accountId));
            }

            holder.setTextSize(getTextSize());

            holder.setIsMyStatus(isMyStatus && !mIndicateMyStatusDisabled);
            if (getLinkHighlightOption() != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
                holder.text.setText(Html.fromHtml(text));
                linkify.applyAllLinks(holder.text, accountId, possiblySensitive);
                holder.text.setMovementMethod(null);
            } else {
                holder.text.setText(text);
            }
            holder.setUserType(isVerified, isProtected);
            holder.setDisplayNameFirst(isDisplayNameFirst());
            holder.setNicknameOnly(isNicknameOnly());
            final String nick = getUserNickname(context, userId);
            holder.name.setText(TextUtils.isEmpty(nick) ? name : isNicknameOnly() ? nick : context.getString(
                    R.string.name_with_nickname, name, nick));
            holder.screen_name.setText("@" + screen_name);
            if (getLinkHighlightOption() != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
                linkify.applyUserProfileLinkNoHighlight(holder.name, accountId, userId, screen_name);
                linkify.applyUserProfileLinkNoHighlight(holder.screen_name, accountId, userId, screen_name);
                holder.name.setMovementMethod(null);
                holder.screen_name.setMovementMethod(null);
            }
            holder.time.setTime(retweetTimestamp > 0 ? retweetTimestamp : timestamp);
            holder.setStatusType(!mFavoritesHighlightDisabled && isFavorite, hasLocation, hasMedia, possiblySensitive);

            holder.setIsReplyRetweet(isReply, isRetweet);
            if (isRetweet) {
                holder.setRetweetedBy(retweetCount, retweetedByUserId, retweetedByName, retweetedByScreenName);
            } else if (isReply) {
                holder.setReplyTo(inReplyToUserId, inReplyToName, inReplyToScreenName);
            }

            if (isDisplayProfileImage()) {
                final String profile_image_url = cursor.getString(mIndices.user_profile_image_url);
                mImageLoader.displayProfileImage(holder.my_profile_image, profile_image_url);
                mImageLoader.displayProfileImage(holder.profile_image, profile_image_url);
                holder.profile_image.setTag(position);
                holder.my_profile_image.setTag(position);
            } else {
                mImageLoader.cancelDisplayTask(holder.profile_image);
                mImageLoader.cancelDisplayTask(holder.my_profile_image);
                holder.profile_image.setVisibility(View.GONE);
                holder.my_profile_image.setVisibility(View.GONE);
            }
            final boolean hasPreview = mDisplayImagePreview && hasMedia;
            holder.image_preview_container.setVisibility(hasPreview ? View.VISIBLE : View.GONE);
            if (hasPreview && firstMedia != null && media != null) {
                if (mImagePreviewScaleType != null) {
                    holder.image_preview.setScaleType(mImagePreviewScaleType);
                }
                if (possiblySensitive && !mDisplaySensitiveContents) {
                    holder.image_preview.setImageDrawable(null);
                    holder.image_preview.setBackgroundResource(R.drawable.image_preview_nsfw);
                    holder.image_preview_progress.setVisibility(View.GONE);
                    mImageLoader.cancelDisplayTask(holder.image_preview);
                } else if (!firstMedia.equals(mImageLoadingHandler.getLoadingUri(holder.image_preview))) {
                    holder.image_preview.setBackgroundResource(0);
                    mImageLoader.displayPreviewImage(holder.image_preview, firstMedia, mImageLoadingHandler);
                }
                final Resources res = mContext.getResources();
                final int count = media.length;
                holder.image_preview_count.setText(res.getQuantityString(R.plurals.N_media, count, count));
                holder.image_preview.setTag(position);
            } else {
                mImageLoader.cancelDisplayTask(holder.image_preview);
            }
        } else {
            mImageLoader.cancelDisplayTask(holder.profile_image);
            mImageLoader.cancelDisplayTask(holder.my_profile_image);
            mImageLoader.cancelDisplayTask(holder.image_preview);
        }
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
    public int getActualCount() {
        return super.getCount();
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
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        final Object tag = view.getTag();
        // animate the item
        if (tag instanceof StatusViewHolder && position > mMaxAnimationPosition) {
            if (mAnimationEnabled) {
                view.startAnimation(((StatusViewHolder) tag).item_animation);
            }
            mMaxAnimationPosition = position;
        }
        return view;
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
            final StatusViewHolder holder = new StatusViewHolder(view);
            holder.profile_image.setOnClickListener(this);
            holder.my_profile_image.setOnClickListener(this);
            holder.image_preview.setOnClickListener(this);
            holder.content.setOnOverflowIconClickListener(this);
            if (mPlainList) {
                ((View) holder.content).setPadding(0, 0, 0, 0);
                holder.content.setItemBackground(null);
            }
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
                if (mContext instanceof Activity) {
                    openUserProfile((Activity) mContext, status.account_id, status.user_id, status.user_screen_name);
                }
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
        return compactCards ? R.layout.card_item_status_compact : R.layout.card_item_status;
    }

    private static boolean moveCursorToLast(final Cursor c) {
        if (c == null || c.isClosed()) return false;
        try {
            return c.moveToNext();
        } catch (final Exception e) {
            return false;
        }
    }
}
