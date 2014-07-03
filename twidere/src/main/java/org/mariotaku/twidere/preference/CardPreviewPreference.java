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

package org.mariotaku.twidere.preference;

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;
import static org.mariotaku.twidere.util.Utils.getLinkHighlightOptionInt;
import static org.mariotaku.twidere.util.Utils.getSampleDisplayName;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.Preference;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.CardItemLinearLayout;
import org.mariotaku.twidere.view.ForegroundImageView;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

public class CardPreviewPreference extends Preference implements Constants, OnSharedPreferenceChangeListener {

	private final LayoutInflater mInflater;
	private final SharedPreferences mPreferences;
	private final TwidereLinkify mLinkify;
	private StatusViewHolder mHolder;
	private boolean mCompactModeChanged;

	public CardPreviewPreference(final Context context) {
		this(context, null);
	}

	public CardPreviewPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CardPreviewPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mInflater = LayoutInflater.from(context);
		mLinkify = new TwidereLinkify(null);
		mLinkify.setLinkTextColor(ThemeUtils.getUserLinkTextColor(context));
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public View getView(final View convertView, final ViewGroup parent) {
		if (mCompactModeChanged) return super.getView(null, parent);
		return super.getView(convertView, parent);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
		if (mHolder == null) return;
		if (KEY_COMPACT_CARDS.equals(key)) {
			mCompactModeChanged = true;
		}
		notifyChanged();
	}

	@Override
	protected void onBindView(final View view) {
		if (mPreferences == null) return;
		mCompactModeChanged = false;
		final Context context = getContext();
		final int highlightOption = getLinkHighlightOptionInt(context);
		final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST, true);
		final boolean display_image_preview = mPreferences.getBoolean(KEY_DISPLAY_IMAGE_PREVIEW, false);
		final boolean display_profile_image = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean nickname_only = mPreferences.getBoolean(KEY_NICKNAME_ONLY, false);
		mHolder = new StatusViewHolder(view);
		mLinkify.setHighlightOption(highlightOption);
		mHolder.setDisplayNameFirst(nameFirst);
		mHolder.setNicknameOnly(nickname_only);
		mHolder.setShowAsGap(false);
		mHolder.setIsMyStatus(false);
		mHolder.setTextSize(mPreferences.getInt(KEY_TEXT_SIZE, getDefaultTextSize(context)));
		mHolder.image_preview_container.setVisibility(display_image_preview ? View.VISIBLE : View.GONE);
		mHolder.profile_image.setVisibility(display_profile_image ? View.VISIBLE : View.GONE);
		mHolder.image_preview_progress.setVisibility(View.GONE);

		if (mHolder.profile_image instanceof ForegroundImageView) {
			((ForegroundImageView) mHolder.profile_image).setForeground(null);
		}
		if (mHolder.image_preview instanceof ForegroundImageView) {
			((ForegroundImageView) mHolder.image_preview).setForeground(null);
		}
		if (mHolder.content instanceof CardItemLinearLayout) {
			((CardItemLinearLayout) mHolder.content).setItemSelector(null);
		}
		mHolder.profile_image.setImageResource(R.drawable.ic_launcher);
		mHolder.image_preview.setImageResource(R.drawable.twidere_feature_graphic);
		mHolder.name.setText(nickname_only ? TWIDERE_PREVIEW_NICKNAME : context.getString(R.string.name_with_nickname,
				TWIDERE_PREVIEW_NAME, TWIDERE_PREVIEW_NICKNAME));
		mHolder.screen_name.setText("@" + TWIDERE_PREVIEW_SCREEN_NAME);
		if (highlightOption != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
			mHolder.text.setText(Html.fromHtml(TWIDERE_PREVIEW_TEXT_HTML));
			mLinkify.applyAllLinks(mHolder.text, 0, false);
			mLinkify.applyUserProfileLinkNoHighlight(mHolder.name, 0, 0, TWIDERE_PREVIEW_SCREEN_NAME);
			mLinkify.applyUserProfileLinkNoHighlight(mHolder.screen_name, 0, 0, TWIDERE_PREVIEW_SCREEN_NAME);
		} else {
			mHolder.text.setText(toPlainText(TWIDERE_PREVIEW_TEXT_HTML));
		}
		final String display_name = getSampleDisplayName(context, nameFirst, nickname_only);
		mHolder.reply_retweet_status.setText(context.getString(R.string.retweeted_by, display_name));
		mHolder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_retweet, 0, 0, 0);
		mHolder.time.setTime(System.currentTimeMillis() - 360000);
		mHolder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_indicator_media, 0);
		super.onBindView(view);
	}

	@Override
	protected View onCreateView(final ViewGroup parent) {
		if (mPreferences != null && mPreferences.getBoolean(KEY_COMPACT_CARDS, false))
			return mInflater.inflate(R.layout.card_item_status_compact, parent, false);
		return mInflater.inflate(R.layout.card_item_status, parent, false);
	}

}
