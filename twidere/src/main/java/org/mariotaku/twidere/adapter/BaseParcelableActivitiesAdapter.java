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

import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getDisplayName;
import static org.mariotaku.twidere.util.Utils.isCompactCards;
import static org.mariotaku.twidere.util.Utils.isPlainListStyle;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.view.holder.ActivityViewHolder;

import java.util.List;

public abstract class BaseParcelableActivitiesAdapter extends BaseArrayAdapter<ParcelableActivity> implements
		IBaseCardAdapter {

	private final MultiSelectManager mMultiSelectManager;
	private final ImageLoaderWrapper mImageLoader;

	private boolean mShowAbsoluteTime, mAnimationEnabled;
	private int mMaxAnimationPosition;

	private MenuButtonClickListener mListener;
	private final boolean mPlainList;

	public BaseParcelableActivitiesAdapter(final Context context) {
		this(context, isCompactCards(context), isPlainListStyle(context));
	}

	public BaseParcelableActivitiesAdapter(final Context context, final boolean compactCards, final boolean plainList) {
		super(context, getItemResource(compactCards));
		mPlainList = plainList;
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mMultiSelectManager = app.getMultiSelectManager();
		mImageLoader = app.getImageLoaderWrapper();
		configBaseCardAdapter(context, this);
	}

	public abstract void bindView(final int position, final ActivityViewHolder holder, final ParcelableActivity item);

	@Override
	public ImageLoaderWrapper getImageLoader() {
		return mImageLoader;
	}

	@Override
	public long getItemId(final int position) {
		final Object obj = getItem(position);
		return obj != null ? obj.hashCode() : 0;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final Object tag = view.getTag();
		final ActivityViewHolder holder = tag instanceof ActivityViewHolder ? (ActivityViewHolder) tag
				: new ActivityViewHolder(view);
		if (!(tag instanceof ActivityViewHolder)) {
			if (mPlainList) {
				((View) holder.content).setPadding(0, 0, 0, 0);
				holder.content.setItemBackground(null);
			}
			view.setTag(holder);
		}

		final boolean showAccountColor = isShowAccountColor();

		holder.setTextSize(getTextSize());
		holder.my_profile_image.setVisibility(View.GONE);
		final ParcelableActivity item = getItem(position);
		holder.setAccountColorEnabled(showAccountColor);
		if (showAccountColor) {
			holder.setAccountColor(getAccountColor(getContext(), item.account_id));
		}
		if (mShowAbsoluteTime) {
			holder.time.setTime(item.activity_timestamp);
		} else {
			holder.time.setTime(item.activity_timestamp);
		}
		bindView(position, holder, item);
		return view;
	}

	public void onItemSelected(final Object item) {
		notifyDataSetChanged();
	}

	public void onItemUnselected(final Object item) {
		notifyDataSetChanged();
	}

	@Override
	public void setAnimationEnabled(final boolean anim) {
		if (mAnimationEnabled == anim) return;
		mAnimationEnabled = anim;
	}

	public void setData(final List<ParcelableActivity> data) {
		clear();
		if (data == null) return;
		addAll(data);
	}

	@Override
	public void setMaxAnimationPosition(final int position) {
		mMaxAnimationPosition = position;
	}

	@Override
	public void setMenuButtonClickListener(final MenuButtonClickListener listener) {
		mListener = listener;
	}

	public void setShowAbsoluteTime(final boolean show) {
		if (show != mShowAbsoluteTime) {
			mShowAbsoluteTime = show;
			notifyDataSetChanged();
		}
	}

	protected void displayActivityUserProfileImages(final ActivityViewHolder holder, final ParcelableStatus[] statuses) {
		if (statuses == null) {
			displayActivityUserProfileImages(holder, new String[0]);
		} else {
			final String[] urls = new String[statuses.length];
			for (int i = 0, j = statuses.length; i < j; i++) {
				urls[i] = statuses[i].user_profile_image_url;
			}
			displayActivityUserProfileImages(holder, urls);
		}
	}

	protected void displayActivityUserProfileImages(final ActivityViewHolder holder, final ParcelableUser[] users) {
		if (users == null) {
			displayActivityUserProfileImages(holder, new String[0]);
		} else {
			final String[] urls = new String[users.length];
			for (int i = 0, j = users.length; i < j; i++) {
				urls[i] = users[i].profile_image_url;
			}
			displayActivityUserProfileImages(holder, urls);
		}
	}

	protected void displayProfileImage(final ImageView view, final ParcelableUser user) {
		if (isDisplayProfileImage()) {
			mImageLoader.displayProfileImage(view, user.profile_image_url);
		} else {
			view.setImageDrawable(null);
		}
	}

	protected String getName(final ParcelableStatus status) {
		if (status == null) return null;
		return getDisplayName(getContext(), status.user_id, status.user_name, status.user_screen_name,
				isDisplayNameFirst(), isNicknameOnly());
	}

	protected String getName(final ParcelableUser user) {
		if (user == null) return null;
		return getDisplayName(getContext(), user.id, user.name, user.screen_name, isDisplayNameFirst(),
				isNicknameOnly());
	}

	protected void setProfileImage(final ImageView view, final ParcelableStatus status) {
		if (isDisplayProfileImage()) {
			mImageLoader.displayProfileImage(view, status.user_profile_image_url);
		} else {
			view.setImageDrawable(null);
		}
	}

	protected boolean shouldDisplayProfileImage() {
		return isDisplayProfileImage();
	}

	private void displayActivityUserProfileImages(final ActivityViewHolder holder, final String[] urls) {
		final int length = urls != null ? Math.min(holder.activity_profile_images.length, urls.length) : 0;
		final boolean shouldDisplayImages = isDisplayProfileImage() && length > 0;
		holder.activity_profile_images_container.setVisibility(shouldDisplayImages ? View.VISIBLE : View.GONE);
		if (!shouldDisplayImages) return;
		for (int i = 0, j = holder.activity_profile_images.length; i < j; i++) {
			final ImageView view = holder.activity_profile_images[i];
			view.setImageDrawable(null);
			if (i < length) {
				view.setVisibility(View.VISIBLE);
				mImageLoader.displayProfileImage(view, urls[i]);
			} else {
				view.setVisibility(View.GONE);
			}
		}
		if (urls.length > holder.activity_profile_images.length) {
			final int moreNumber = urls.length - holder.activity_profile_images.length;
			holder.activity_profile_image_more_number.setVisibility(View.VISIBLE);
			holder.activity_profile_image_more_number.setText(getContext().getString(R.string.and_more, moreNumber));
		} else {
			holder.activity_profile_image_more_number.setVisibility(View.GONE);
		}
	}

	private static int getItemResource(final boolean compactCards) {
		return compactCards ? R.layout.card_item_activity_compact : R.layout.card_item_activity;
	}

}
