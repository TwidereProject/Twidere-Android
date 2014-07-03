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

import static org.mariotaku.twidere.model.ParcelableLocation.isValidLocation;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserColor;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserNickname;
import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getCardHighlightColor;
import static org.mariotaku.twidere.util.Utils.getCardHighlightOptionInt;
import static org.mariotaku.twidere.util.Utils.isFiltered;
import static org.mariotaku.twidere.util.Utils.openImage;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
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
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUserMention;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.iface.ICardItemView.OnOverflowIconClickListener;

import java.util.List;
import java.util.Locale;

public class ParcelableStatusesAdapter extends BaseArrayAdapter<ParcelableStatus> implements
		IStatusesAdapter<List<ParcelableStatus>>, OnClickListener, OnOverflowIconClickListener {

	private final Context mContext;
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
	private ScaleType mImagePreviewScaleType;
	private String[] mHighlightKeywords;

	public ParcelableStatusesAdapter(final Context context) {
		this(context, Utils.isCompactCards(context), Utils.isPlainListStyle(context));
	}

	public ParcelableStatusesAdapter(final Context context, final boolean compactCards, final boolean plainList) {
		super(context, getItemResource(compactCards));
		mPlainList = plainList;
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
	public int getActualCount() {
		return super.getCount();
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
			holder = new StatusViewHolder(view);
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

		final ParcelableStatus status = getItem(position);

		final boolean showGap = status.is_gap && !mGapDisallowed && position != getCount() - 1;

		holder.position = position;
		holder.setShowAsGap(showGap);
		holder.setDisplayProfileImage(isDisplayProfileImage());
		holder.setCardHighlightOption(mCardHighlightOption);

		if (!showGap) {
			final TwidereLinkify linkify = getLinkify();
			final ImageLoaderWrapper loader = getImageLoader();
			final int highlightOption = getLinkHighlightOption();
			final boolean mShowAccountColor = isShowAccountColor();

			// Clear images in prder to prevent images in recycled view shown.
			holder.profile_image.setImageDrawable(null);
			holder.my_profile_image.setImageDrawable(null);
			holder.image_preview.setImageDrawable(null);

			holder.setAccountColorEnabled(mShowAccountColor);

			if (highlightOption != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
				holder.text.setText(Utils.getKeywordBoldedText(Html.fromHtml(status.text_html), mHighlightKeywords));
				linkify.applyAllLinks(holder.text, status.account_id, status.is_possibly_sensitive);
				holder.text.setMovementMethod(null);
			} else {
				if (mHighlightKeywords == null || mHighlightKeywords.length == 0) {
					holder.text.setText(status.text_unescaped);
				} else {
					holder.text.setText(Utils.getKeywordBoldedText(status.text_unescaped, mHighlightKeywords));
				}
			}

			if (mShowAccountColor) {
				holder.setAccountColor(getAccountColor(mContext, status.account_id));
			}

			final boolean isMention = ParcelableUserMention.hasMention(status.mentions, status.account_id);
			final boolean isMyStatus = status.account_id == status.user_id;
			final boolean hasMedia = status.first_media != null;
			if (status.is_retweet) {
				holder.setUserColor(getUserColor(mContext, status.user_id),
						getUserColor(mContext, status.retweeted_by_id));
			} else {
				holder.setUserColor(getUserColor(mContext, status.user_id));
			}
			holder.setHighlightColor(getCardHighlightColor(!mMentionsHighlightDisabled && isMention,
					!mFavoritesHighlightDisabled && status.is_favorite, status.is_retweet));
			holder.setTextSize(getTextSize());

			holder.setIsMyStatus(isMyStatus && !mIndicateMyStatusDisabled);

			holder.setUserType(status.user_is_verified, status.user_is_protected);
			holder.setDisplayNameFirst(isDisplayNameFirst());
			holder.setNicknameOnly(isNicknameOnly());
			final String nick = getUserNickname(mContext, status.user_id);
			holder.name.setText(TextUtils.isEmpty(nick) ? status.user_name : isNicknameOnly() ? nick : mContext
					.getString(R.string.name_with_nickname, status.user_name, nick));
			holder.screen_name.setText("@" + status.user_screen_name);
			if (highlightOption != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
				linkify.applyUserProfileLinkNoHighlight(holder.name, status.account_id, status.user_id,
						status.user_screen_name);
				linkify.applyUserProfileLinkNoHighlight(holder.screen_name, status.account_id, status.user_id,
						status.user_screen_name);
				holder.name.setMovementMethod(null);
				holder.screen_name.setMovementMethod(null);
			}
			holder.time.setTime(status.timestamp);
			holder.setStatusType(!mFavoritesHighlightDisabled && status.is_favorite, isValidLocation(status.location),
					hasMedia, status.is_possibly_sensitive);
			holder.setIsReplyRetweet(status.in_reply_to_status_id > 0, status.is_retweet);
			if (status.is_retweet) {
				holder.setRetweetedBy(status.retweet_count, status.retweeted_by_id, status.retweeted_by_name,
						status.retweeted_by_screen_name);
			} else if (status.in_reply_to_status_id > 0) {
				holder.setReplyTo(status.in_reply_to_user_id, status.in_reply_to_name, status.in_reply_to_screen_name);
			}
			if (isDisplayProfileImage()) {
				loader.displayProfileImage(holder.my_profile_image, status.user_profile_image_url);
				loader.displayProfileImage(holder.profile_image, status.user_profile_image_url);
				holder.profile_image.setTag(position);
				holder.my_profile_image.setTag(position);
			} else {
				holder.profile_image.setVisibility(View.GONE);
				holder.my_profile_image.setVisibility(View.GONE);
			}
			final boolean hasPreview = mDisplayImagePreview && hasMedia;
			holder.image_preview_container.setVisibility(hasPreview ? View.VISIBLE : View.GONE);
			if (hasPreview) {
				if (mImagePreviewScaleType != null) {
					holder.image_preview.setScaleType(mImagePreviewScaleType);
				}
				if (status.is_possibly_sensitive && !mDisplaySensitiveContents) {
					holder.image_preview.setImageDrawable(null);
					holder.image_preview.setBackgroundResource(R.drawable.image_preview_nsfw);
					holder.image_preview_progress.setVisibility(View.GONE);
				} else if (!status.first_media.equals(mImageLoadingHandler.getLoadingUri(holder.image_preview))) {
					holder.image_preview.setBackgroundResource(0);
					loader.displayPreviewImage(holder.image_preview, status.first_media, mImageLoadingHandler);
				}
				final Resources res = mContext.getResources();
				final int count = status.medias.length;
				holder.image_preview_count.setText(res.getQuantityString(R.plurals.N_medias, count, count));
				holder.image_preview.setTag(position);
			}
		}
		if (position > mMaxAnimationPosition) {
			if (mAnimationEnabled) {
				view.startAnimation(holder.item_animation);
			}
			mMaxAnimationPosition = position;
		}
		return view;
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
		return compactCards ? R.layout.card_item_status_compact : R.layout.card_item_status;
	}
}
