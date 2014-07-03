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

package org.mariotaku.twidere.model;

import android.support.v4.app.Fragment;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.support.ActivitiesAboutMeFragment;
import org.mariotaku.twidere.fragment.support.ActivitiesByFriendsFragment;
import org.mariotaku.twidere.fragment.support.DirectMessagesFragment;
import org.mariotaku.twidere.fragment.support.HomeTimelineFragment;
import org.mariotaku.twidere.fragment.support.MentionsTimelineFragment;
import org.mariotaku.twidere.fragment.support.SearchStatusesFragment;
import org.mariotaku.twidere.fragment.support.TrendsSuggectionsFragment;
import org.mariotaku.twidere.fragment.support.UserFavoritesFragment;
import org.mariotaku.twidere.fragment.support.UserListTimelineFragment;
import org.mariotaku.twidere.fragment.support.UserTimelineFragment;

import java.util.Comparator;
import java.util.Map.Entry;

public enum CustomTabConfiguration2 implements Constants {

	HOME_TIMELINE(HomeTimelineFragment.class, R.string.home, R.drawable.ic_iconic_action_home,
			CustomTabConfiguration.ACCOUNT_OPTIONAL, CustomTabConfiguration.FIELD_TYPE_NONE, 0, false),

	MENTIONS_TIMELINE(MentionsTimelineFragment.class, R.string.mentions, R.drawable.ic_iconic_action_mention,
			CustomTabConfiguration.ACCOUNT_OPTIONAL, CustomTabConfiguration.FIELD_TYPE_NONE, 1, false),

	DIRECT_MESSAGES(DirectMessagesFragment.class, R.string.direct_messages, R.drawable.ic_iconic_action_message,
			CustomTabConfiguration.ACCOUNT_OPTIONAL, CustomTabConfiguration.FIELD_TYPE_NONE, 2, false),

	TRENDS_SUGGESTIONS(TrendsSuggectionsFragment.class, R.string.trends, R.drawable.ic_iconic_action_hashtag,
			CustomTabConfiguration.ACCOUNT_NONE, CustomTabConfiguration.FIELD_TYPE_NONE, 3, true),

	FAVORITES(UserFavoritesFragment.class, R.string.favorites, R.drawable.ic_iconic_action_star,
			CustomTabConfiguration.ACCOUNT_REQUIRED, CustomTabConfiguration.FIELD_TYPE_USER, 4),

	USER_TIMELINE(UserTimelineFragment.class, R.string.users_statuses, R.drawable.ic_iconic_action_quote,
			CustomTabConfiguration.ACCOUNT_REQUIRED, CustomTabConfiguration.FIELD_TYPE_USER, 5),

	SEARCH_STATUSES(SearchStatusesFragment.class, R.string.search_statuses, R.drawable.ic_iconic_action_search,
			CustomTabConfiguration.ACCOUNT_REQUIRED, CustomTabConfiguration.FIELD_TYPE_TEXT, R.string.query,
			EXTRA_QUERY, 6),

	LIST_TIMELINE(UserListTimelineFragment.class, R.string.list_timeline, R.drawable.ic_iconic_action_list,
			CustomTabConfiguration.ACCOUNT_REQUIRED, CustomTabConfiguration.FIELD_TYPE_USER_LIST, 7),

	ACTIVITIES_ABOUT_ME(ActivitiesAboutMeFragment.class, R.string.activities_about_me,
			R.drawable.ic_iconic_action_user, CustomTabConfiguration.ACCOUNT_OPTIONAL,
			CustomTabConfiguration.FIELD_TYPE_NONE, 8),

	ACTIVITIES_BY_FRIENDS(ActivitiesByFriendsFragment.class, R.string.activities_by_friends,
			R.drawable.ic_iconic_action_accounts, CustomTabConfiguration.ACCOUNT_REQUIRED,
			CustomTabConfiguration.FIELD_TYPE_NONE, 9);

	public static final int FIELD_TYPE_NONE = 0;
	public static final int FIELD_TYPE_USER = 1;
	public static final int FIELD_TYPE_USER_LIST = 2;
	public static final int FIELD_TYPE_TEXT = 3;

	public static final int ACCOUNT_NONE = 0;
	public static final int ACCOUNT_REQUIRED = 1;
	public static final int ACCOUNT_OPTIONAL = 2;

	private final int title, icon, secondaryFieldType, secondaryFieldTitle, sortPosition, accountRequirement;
	private final Class<? extends Fragment> cls;
	private final String secondaryFieldTextKey;
	private final boolean singleTab;

	CustomTabConfiguration2(final Class<? extends Fragment> cls, final int title, final int icon,
			final int accountRequirement, final int secondaryFieldType, final int sortPosition) {
		this(cls, title, icon, accountRequirement, secondaryFieldType, 0, EXTRA_TEXT, sortPosition, false);
	}

	CustomTabConfiguration2(final Class<? extends Fragment> cls, final int title, final int icon,
			final int accountRequirement, final int secondaryFieldType, final int sortPosition, final boolean singleTab) {
		this(cls, title, icon, accountRequirement, secondaryFieldType, 0, EXTRA_TEXT, sortPosition, singleTab);
	}

	CustomTabConfiguration2(final Class<? extends Fragment> cls, final int title, final int icon,
			final int accountRequirement, final int secondaryFieldType, final int secondaryFieldTitle,
			final String secondaryFieldTextKey, final int sortPosition) {
		this(cls, title, icon, accountRequirement, secondaryFieldType, 0, secondaryFieldTextKey, sortPosition, false);
	}

	CustomTabConfiguration2(final Class<? extends Fragment> cls, final int title, final int icon,
			final int accountRequirement, final int secondaryFieldType, final int secondaryFieldTitle,
			final String secondaryFieldTextKey, final int sortPosition, final boolean singleTab) {
		this.cls = cls;
		this.title = title;
		this.icon = icon;
		this.sortPosition = sortPosition;
		this.accountRequirement = accountRequirement;
		this.secondaryFieldType = secondaryFieldType;
		this.secondaryFieldTitle = secondaryFieldTitle;
		this.secondaryFieldTextKey = secondaryFieldTextKey;
		this.singleTab = singleTab;
	}

	public int getAccountRequirement() {
		return accountRequirement;
	}

	public int getDefaultIcon() {
		return icon;
	}

	public int getDefaultTitle() {
		return title;
	}

	public Class<? extends Fragment> getFragmentClass() {
		return cls;
	}

	public String getSecondaryFieldTextKey() {
		return secondaryFieldTextKey;
	}

	public int getSecondaryFieldTitle() {
		return secondaryFieldTitle;
	}

	public int getSecondaryFieldType() {
		return secondaryFieldType;
	}

	public int getSortPosition() {
		return sortPosition;
	}

	public boolean isSingleTab() {
		return singleTab;
	}

	@Override
	public String toString() {
		return "CustomTabConfiguration{title=" + title + ", icon=" + icon + ", secondaryFieldType="
				+ secondaryFieldType + ", secondaryFieldTitle=" + secondaryFieldTitle + ", sortPosition="
				+ sortPosition + ", accountRequirement=" + accountRequirement + ", cls=" + cls
				+ ", secondaryFieldTextKey=" + secondaryFieldTextKey + ", singleTab=" + singleTab + "}";
	}

	public static class CustomTabConfigurationComparator implements Comparator<Entry<String, CustomTabConfiguration2>> {

		public static final CustomTabConfigurationComparator SINGLETON = new CustomTabConfigurationComparator();

		@Override
		public int compare(final Entry<String, CustomTabConfiguration2> lhs,
				final Entry<String, CustomTabConfiguration2> rhs) {
			return lhs.getValue().getSortPosition() - rhs.getValue().getSortPosition();
		}

	}

}
