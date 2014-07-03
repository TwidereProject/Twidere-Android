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
import android.text.Html;
import android.text.TextUtils;
import android.view.View;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.holder.ActivityViewHolder;

public class ParcelableActivitiesByFriendsAdapter extends BaseParcelableActivitiesAdapter {

	public ParcelableActivitiesByFriendsAdapter(final Context context, final boolean compactCards,
			final boolean plainList) {
		super(context, compactCards, plainList);
	}

	@Override
	public void bindView(final int position, final ActivityViewHolder holder, final ParcelableActivity item) {
		if (item == null) return;
		final ParcelableUser[] sources = item.sources;
		final ParcelableStatus[] targetStatuses = item.target_statuses;
		final ParcelableUser[] targetUsers = item.target_users;
		final ParcelableStatus[] target_object_statuses = item.target_object_statuses;
		final ParcelableUserList[] targetUserLists = item.target_user_lists;
		final ParcelableUserList[] target_object_user_lists = item.target_object_user_lists;
		final int sourcesLength = sources != null ? sources.length : 0;
		final int targetStatusesLength = targetStatuses != null ? targetStatuses.length : 0;
		final int target_users_length = targetUsers != null ? targetUsers.length : 0;
		final int target_object_user_lists_length = target_object_user_lists != null ? target_object_user_lists.length
				: 0;
		final int target_user_lists_length = targetUserLists != null ? targetUserLists.length : 0;
		final int action = item.action;
		final Context context = getContext();

		final TwidereLinkify linkify = getLinkify();
		final int highlightOption = getLinkHighlightOption();

		holder.name.setSingleLine(false);
		holder.screen_name.setVisibility(View.GONE);
		holder.reply_retweet_status.setVisibility(View.GONE);
		if (holder.divider != null) {
			holder.divider.setVisibility(View.VISIBLE);
		}
		if (sources != null && sources.length != 0) {
			final ParcelableUser firstSource = sources[0];
			final String firstSourceName = getName(firstSource);
			switch (action) {
				case ParcelableActivity.ACTION_FAVORITE: {
					if (targetStatuses == null || targetStatuses.length == 0) return;
					final ParcelableStatus status = targetStatuses[0];
					if (targetStatusesLength == 1) {
						holder.text.setVisibility(View.VISIBLE);
						if (highlightOption != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
							holder.text.setText(Html.fromHtml(status.text_html));
							linkify.applyAllLinks(holder.text, status.account_id, status.is_possibly_sensitive);
							holder.text.setMovementMethod(null);
						} else {
							holder.text.setText(status.text_unescaped);
						}
						holder.name.setText(context.getString(R.string.activity_by_friends_favorite, firstSourceName,
								getName(status)));
					} else {
						holder.text.setVisibility(View.GONE);
						holder.name.setText(context.getString(R.string.activity_by_friends_favorite_multi,
								firstSourceName, getName(status), targetStatusesLength - 1));
					}
					displayProfileImage(holder.profile_image, firstSource);
					displayActivityUserProfileImages(holder, targetStatuses);
					break;
				}
				case ParcelableActivity.ACTION_FOLLOW: {
					holder.text.setVisibility(View.GONE);
					if (targetUsers == null || targetUsers.length == 0) return;
					if (targetUsers.length == 1) {
						holder.name.setText(context.getString(R.string.activity_by_friends_follow, firstSourceName,
								getName(targetUsers[0])));
					} else {
						holder.name.setText(context.getString(R.string.activity_by_friends_follow_multi,
								firstSourceName, getName(targetUsers[0]), target_users_length - 1));
					}
					displayProfileImage(holder.profile_image, firstSource);
					displayActivityUserProfileImages(holder, targetUsers);
					break;
				}
				case ParcelableActivity.ACTION_RETWEET: {
					holder.text.setVisibility(View.VISIBLE);
					if (target_object_statuses != null && target_object_statuses.length > 0) {
						final ParcelableStatus status = target_object_statuses[0];
						if (highlightOption != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
							holder.text.setText(Html.fromHtml(status.text_html));
							linkify.applyAllLinks(holder.text, status.account_id, status.is_possibly_sensitive);
							holder.text.setMovementMethod(null);
						} else {
							holder.text.setText(status.text_unescaped);
						}
					}
					if (sourcesLength == 1) {
						holder.name.setText(context.getString(R.string.activity_by_friends_retweet, firstSourceName,
								getName(targetStatuses[0])));
					} else {
						holder.name.setText(context.getString(R.string.activity_about_me_retweet_multi,
								firstSourceName, sourcesLength - 1));
					}
					displayActivityUserProfileImages(holder, sources);
					break;
				}
				case ParcelableActivity.ACTION_LIST_MEMBER_ADDED: {
					holder.text.setVisibility(View.GONE);
					if (target_object_user_lists_length == 1) {
						holder.name.setText(context.getString(R.string.activity_by_friends_list_member_added,
								firstSourceName, getName(targetUsers[0])));
					} else {
						holder.name.setText(context.getString(R.string.activity_about_me_list_member_added_multi,
								firstSourceName, sourcesLength - 1));
					}
					displayProfileImage(holder.profile_image, firstSource);
					displayActivityUserProfileImages(holder, targetUsers);
					break;
				}
				case ParcelableActivity.ACTION_LIST_CREATED: {
					if (target_user_lists_length == 0) return;
					holder.activity_profile_images_container.setVisibility(View.GONE);
					final ParcelableUserList userList = targetUserLists[0];
					if (target_user_lists_length == 1) {
						if (!TextUtils.isEmpty(userList.description)) {
							holder.text.setVisibility(View.VISIBLE);
							holder.text.setText(userList.description);
						} else {
							if (holder.divider != null) {
								holder.divider.setVisibility(View.GONE);
							}
							holder.text.setVisibility(View.GONE);
						}
						holder.name.setText(context.getString(R.string.activity_by_friends_list_created,
								firstSourceName, userList.name));
					} else {
						holder.text.setVisibility(View.GONE);
						holder.name.setText(context.getString(R.string.activity_by_friends_list_created_multi,
								firstSourceName, userList.name, target_user_lists_length - 1));
					}
					displayProfileImage(holder.profile_image, firstSource);
					break;
				}
			}
		}
	}

}
