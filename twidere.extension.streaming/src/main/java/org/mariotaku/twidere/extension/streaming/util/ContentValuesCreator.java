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

package org.mariotaku.twidere.extension.streaming.util;

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.jsonserializer.JSONSerializer;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.extension.streaming.model.ParcelableMedia;
import org.mariotaku.twidere.extension.streaming.model.ParcelableUserMention;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import twitter4j.DirectMessage;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import android.content.ContentValues;

public final class ContentValuesCreator implements TwidereConstants {

	public static ContentValues makeAccountContentValues(final Configuration conf, final String basic_password,
			final AccessToken access_token, final User user, final int auth_type, final int color) {
		if (user == null || user.getId() <= 0) return null;
		final ContentValues values = new ContentValues();
		switch (auth_type) {
			case Accounts.AUTH_TYPE_TWIP_O_MODE: {
				break;
			}
			case Accounts.AUTH_TYPE_BASIC: {
				if (basic_password == null) return null;
				values.put(Accounts.BASIC_AUTH_PASSWORD, basic_password);
				break;
			}
			case Accounts.AUTH_TYPE_OAUTH:
			case Accounts.AUTH_TYPE_XAUTH: {
				if (access_token == null) return null;
				if (user.getId() != access_token.getUserId()) return null;
				values.put(Accounts.OAUTH_TOKEN, access_token.getToken());
				values.put(Accounts.OAUTH_TOKEN_SECRET, access_token.getTokenSecret());
				values.put(Accounts.CONSUMER_KEY, conf.getOAuthConsumerKey());
				values.put(Accounts.CONSUMER_SECRET, conf.getOAuthConsumerSecret());
				break;
			}
		}
		values.put(Accounts.AUTH_TYPE, auth_type);
		values.put(Accounts.ACCOUNT_ID, user.getId());
		values.put(Accounts.SCREEN_NAME, user.getScreenName());
		values.put(Accounts.NAME, user.getName());
		values.put(Accounts.PROFILE_IMAGE_URL, ParseUtils.parseString(user.getProfileImageUrlHttps()));
		values.put(Accounts.PROFILE_BANNER_URL, ParseUtils.parseString(user.getProfileBannerImageUrl()));
		values.put(Accounts.COLOR, color);
		values.put(Accounts.IS_ACTIVATED, 1);
		values.put(Accounts.REST_BASE_URL, conf.getRestBaseURL());
		values.put(Accounts.SIGNING_REST_BASE_URL, conf.getSigningRestBaseURL());
		values.put(Accounts.OAUTH_BASE_URL, conf.getOAuthBaseURL());
		values.put(Accounts.SIGNING_OAUTH_BASE_URL, conf.getSigningOAuthBaseURL());
		return values;
	}

	public static ContentValues makeCachedUserContentValues(final User user) {
		if (user == null || user.getId() <= 0) return null;
		final String profile_image_url = ParseUtils.parseString(user.getProfileImageUrlHttps());
		final String url = ParseUtils.parseString(user.getURL());
		final URLEntity[] urls = user.getURLEntities();
		final ContentValues values = new ContentValues();
		values.put(CachedUsers.USER_ID, user.getId());
		values.put(CachedUsers.NAME, user.getName());
		values.put(CachedUsers.SCREEN_NAME, user.getScreenName());
		values.put(CachedUsers.PROFILE_IMAGE_URL, profile_image_url);
		values.put(CachedUsers.CREATED_AT, user.getCreatedAt().getTime());
		values.put(CachedUsers.IS_PROTECTED, user.isProtected());
		values.put(CachedUsers.IS_VERIFIED, user.isVerified());
		values.put(CachedUsers.IS_FOLLOWING, user.isFollowing());
		values.put(CachedUsers.FAVORITES_COUNT, user.getFavouritesCount());
		values.put(CachedUsers.FOLLOWERS_COUNT, user.getFollowersCount());
		values.put(CachedUsers.FRIENDS_COUNT, user.getFriendsCount());
		values.put(CachedUsers.STATUSES_COUNT, user.getStatusesCount());
		values.put(CachedUsers.LOCATION, user.getLocation());
		values.put(CachedUsers.DESCRIPTION_PLAIN, user.getDescription());
		values.put(CachedUsers.DESCRIPTION_HTML, Utils.formatUserDescription(user));
		values.put(CachedUsers.DESCRIPTION_EXPANDED, Utils.formatExpandedUserDescription(user));
		values.put(CachedUsers.URL, url);
		values.put(CachedUsers.URL_EXPANDED,
				url != null && urls != null && urls.length > 0 ? ParseUtils.parseString(urls[0].getExpandedURL())
						: null);
		values.put(CachedUsers.PROFILE_BANNER_URL, user.getProfileBannerImageUrl());
		return values;
	}

	public static ContentValues makeDirectMessageContentValues(final DirectMessage message, final long account_id,
			final boolean is_outgoing) {
		if (message == null || message.getId() <= 0) return null;
		final ContentValues values = new ContentValues();
		final User sender = message.getSender(), recipient = message.getRecipient();
		if (sender == null || recipient == null) return null;
		final String sender_profile_image_url = ParseUtils.parseString(sender.getProfileImageUrlHttps());
		final String recipient_profile_image_url = ParseUtils.parseString(recipient.getProfileImageUrlHttps());
		values.put(DirectMessages.ACCOUNT_ID, account_id);
		values.put(DirectMessages.MESSAGE_ID, message.getId());
		values.put(DirectMessages.MESSAGE_TIMESTAMP, message.getCreatedAt().getTime());
		values.put(DirectMessages.SENDER_ID, sender.getId());
		values.put(DirectMessages.RECIPIENT_ID, recipient.getId());
		values.put(DirectMessages.TEXT_HTML, Utils.formatDirectMessageText(message));
		values.put(DirectMessages.TEXT_PLAIN, message.getText());
		values.put(DirectMessages.IS_OUTGOING, is_outgoing);
		values.put(DirectMessages.SENDER_NAME, sender.getName());
		values.put(DirectMessages.SENDER_SCREEN_NAME, sender.getScreenName());
		values.put(DirectMessages.RECIPIENT_NAME, recipient.getName());
		values.put(DirectMessages.RECIPIENT_SCREEN_NAME, recipient.getScreenName());
		values.put(DirectMessages.SENDER_PROFILE_IMAGE_URL, sender_profile_image_url);
		values.put(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL, recipient_profile_image_url);
		return values;
	}

	public static ContentValues makeDirectMessageContentValues(final ParcelableDirectMessage message) {
		if (message == null || message.id <= 0) return null;
		final ContentValues values = new ContentValues();
		values.put(DirectMessages.ACCOUNT_ID, message.account_id);
		values.put(DirectMessages.MESSAGE_ID, message.id);
		values.put(DirectMessages.MESSAGE_TIMESTAMP, message.timestamp);
		values.put(DirectMessages.SENDER_ID, message.sender_id);
		values.put(DirectMessages.RECIPIENT_ID, message.recipient_id);
		values.put(DirectMessages.TEXT_HTML, message.text_html);
		values.put(DirectMessages.TEXT_PLAIN, message.text_plain);
		values.put(DirectMessages.IS_OUTGOING, message.is_outgoing);
		values.put(DirectMessages.SENDER_NAME, message.sender_name);
		values.put(DirectMessages.SENDER_SCREEN_NAME, message.sender_screen_name);
		values.put(DirectMessages.RECIPIENT_NAME, message.recipient_name);
		values.put(DirectMessages.RECIPIENT_SCREEN_NAME, message.recipient_screen_name);
		values.put(DirectMessages.SENDER_PROFILE_IMAGE_URL, message.sender_profile_image_url);
		values.put(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL, message.recipient_profile_image_url);
		return values;
	}

	public static ContentValues makeDirectMessageDraftContentValues(final long accountId, final long recipientId,
			final String text) {
		final ContentValues values = new ContentValues();
		values.put(Drafts.ACTION_TYPE, Drafts.ACTION_SEND_DIRECT_MESSAGE);
		values.put(Drafts.TEXT, text);
		values.put(Drafts.ACCOUNT_IDS, ArrayUtils.toString(new long[] { accountId }, ',', false));
		values.put(Drafts.TIMESTAMP, System.currentTimeMillis());
		final JSONObject extras = new JSONObject();
		try {
			extras.put(EXTRA_RECIPIENT_ID, recipientId);
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		values.put(Drafts.ACTION_EXTRAS, extras.toString());
		return values;
	}

	public static ContentValues makeFilterdUserContentValues(final ParcelableStatus status) {
		if (status == null) return null;
		final ContentValues values = new ContentValues();
		values.put(Filters.Users.USER_ID, status.user_id);
		values.put(Filters.Users.NAME, status.user_name);
		values.put(Filters.Users.SCREEN_NAME, status.user_screen_name);
		return values;
	}

	public static ContentValues makeFilterdUserContentValues(final ParcelableUser user) {
		if (user == null) return null;
		final ContentValues values = new ContentValues();
		values.put(Filters.Users.USER_ID, user.id);
		values.put(Filters.Users.NAME, user.name);
		values.put(Filters.Users.SCREEN_NAME, user.screen_name);
		return values;
	}

	public static ContentValues makeFilterdUserContentValues(final ParcelableUserMention user) {
		if (user == null) return null;
		final ContentValues values = new ContentValues();
		values.put(Filters.Users.USER_ID, user.id);
		values.put(Filters.Users.NAME, user.name);
		values.put(Filters.Users.SCREEN_NAME, user.screen_name);
		return values;
	}

	public static ContentValues makeStatusContentValues(final Status orig, final long account_id) {
		if (orig == null || orig.getId() <= 0) return null;
		final ContentValues values = new ContentValues();
		values.put(Statuses.ACCOUNT_ID, account_id);
		values.put(Statuses.STATUS_ID, orig.getId());
		values.put(Statuses.MY_RETWEET_ID, orig.getCurrentUserRetweet());
		final boolean is_retweet = orig.isRetweet();
		final Status status;
		final Status retweeted_status = is_retweet ? orig.getRetweetedStatus() : null;
		if (retweeted_status != null) {
			final User retweet_user = orig.getUser();
			values.put(Statuses.RETWEET_ID, retweeted_status.getId());
			values.put(Statuses.RETWEETED_BY_USER_ID, retweet_user.getId());
			values.put(Statuses.RETWEETED_BY_USER_NAME, retweet_user.getName());
			values.put(Statuses.RETWEETED_BY_USER_SCREEN_NAME, retweet_user.getScreenName());
			status = retweeted_status;
		} else {
			status = orig;
		}
		final User user = status.getUser();
		if (user != null) {
			final long userId = user.getId();
			final String profileImageUrl = ParseUtils.parseString(user.getProfileImageUrlHttps());
			final String name = user.getName(), screenName = user.getScreenName();
			values.put(Statuses.USER_ID, userId);
			values.put(Statuses.USER_NAME, name);
			values.put(Statuses.USER_SCREEN_NAME, screenName);
			values.put(Statuses.IS_PROTECTED, user.isProtected());
			values.put(Statuses.IS_VERIFIED, user.isVerified());
			values.put(Statuses.USER_PROFILE_IMAGE_URL, profileImageUrl);
			values.put(CachedUsers.IS_FOLLOWING, user.isFollowing());
		}
		if (status.getCreatedAt() != null) {
			values.put(Statuses.STATUS_TIMESTAMP, status.getCreatedAt().getTime());
		}
		final String text_html = Utils.formatStatusText(status);
		values.put(Statuses.TEXT_HTML, text_html);
		values.put(Statuses.TEXT_PLAIN, status.getText());
		values.put(Statuses.TEXT_UNESCAPED, toPlainText(text_html));
		values.put(Statuses.RETWEET_COUNT, status.getRetweetCount());
		values.put(Statuses.IN_REPLY_TO_STATUS_ID, status.getInReplyToStatusId());
		values.put(Statuses.IN_REPLY_TO_USER_ID, status.getInReplyToUserId());
		values.put(Statuses.IN_REPLY_TO_USER_NAME, Utils.getInReplyToName(status));
		values.put(Statuses.IN_REPLY_TO_USER_SCREEN_NAME, status.getInReplyToScreenName());
		values.put(Statuses.SOURCE, status.getSource());
		values.put(Statuses.IS_POSSIBLY_SENSITIVE, status.isPossiblySensitive());
		final GeoLocation location = status.getGeoLocation();
		if (location != null) {
			values.put(Statuses.LOCATION, location.getLatitude() + "," + location.getLongitude());
		}
		values.put(Statuses.IS_RETWEET, is_retweet);
		values.put(Statuses.IS_FAVORITE, status.isFavorited());
		final ParcelableMedia[] medias = ParcelableMedia.fromEntities(status);
		if (medias != null) {
			values.put(Statuses.MEDIAS, ParseUtils.parseString(JSONSerializer.toJSONArray(medias)));
			values.put(Statuses.FIRST_MEDIA, medias[0].url);
		}
		final ParcelableUserMention[] mentions = (ParcelableUserMention.fromStatus(status));
		if (mentions != null) {
			values.put(Statuses.MENTIONS, ParseUtils.parseString(JSONSerializer.toJSONArray(mentions)));
		}
		return values;
	}

	public static ContentValues makeStatusDraftContentValues(final ParcelableStatusUpdate status) {
		return makeStatusDraftContentValues(status, Account.getAccountIds(status.accounts));
	}

	public static ContentValues makeStatusDraftContentValues(final ParcelableStatusUpdate status,
			final long[] accountIds) {
		final ContentValues values = new ContentValues();
		values.put(Drafts.ACTION_TYPE, Drafts.ACTION_UPDATE_STATUS);
		values.put(Drafts.TEXT, status.text);
		values.put(Drafts.ACCOUNT_IDS, ArrayUtils.toString(accountIds, ',', false));
		values.put(Drafts.IN_REPLY_TO_STATUS_ID, status.in_reply_to_status_id);
		values.put(Drafts.LOCATION, ParcelableLocation.toString(status.location));
		values.put(Drafts.IS_POSSIBLY_SENSITIVE, status.is_possibly_sensitive);
		values.put(Drafts.TIMESTAMP, System.currentTimeMillis());
		return values;
	}

	public static ContentValues[] makeTrendsContentValues(final List<Trends> trends_list) {
		if (trends_list == null) return new ContentValues[0];
		final List<ContentValues> result_list = new ArrayList<ContentValues>();
		for (final Trends trends : trends_list) {
			if (trends == null) {
				continue;
			}
			final long timestamp = trends.getTrendAt().getTime();
			for (final Trend trend : trends.getTrends()) {
				final ContentValues values = new ContentValues();
				values.put(CachedTrends.NAME, trend.getName());
				values.put(CachedTrends.TIMESTAMP, timestamp);
				result_list.add(values);
			}
		}
		return result_list.toArray(new ContentValues[result_list.size()]);
	}

}
