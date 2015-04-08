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

package org.mariotaku.twidere.util;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.jsonserializer.JSONSerializer;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableMediaUpdate;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatus.ParcelableCardEntity;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserMention;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.provider.TwidereDataStore.SavedSearches;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;

import java.util.ArrayList;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.GeoLocation;
import twitter4j.Place;
import twitter4j.Relationship;
import twitter4j.SavedSearch;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;

public final class ContentValuesCreator implements TwidereConstants {

    public static ContentValues createAccount(final Configuration conf, final String basicUsername,
                                              final String basicPassword, final User user,
                                              final int color, final String apiUrlFormat,
                                              final boolean noVersionSuffix) {
        if (user == null || user.getId() <= 0) return null;
        final ContentValues values = new ContentValues();
        if (basicUsername == null || basicPassword == null) return null;
        values.put(Accounts.BASIC_AUTH_USERNAME, basicUsername);
        values.put(Accounts.BASIC_AUTH_PASSWORD, basicPassword);
        values.put(Accounts.AUTH_TYPE, Accounts.AUTH_TYPE_BASIC);
        values.put(Accounts.ACCOUNT_ID, user.getId());
        values.put(Accounts.SCREEN_NAME, user.getScreenName());
        values.put(Accounts.NAME, user.getName());
        values.put(Accounts.PROFILE_IMAGE_URL, user.getProfileImageUrlHttps());
        values.put(Accounts.PROFILE_BANNER_URL, user.getProfileBannerImageUrl());
        values.put(Accounts.COLOR, color);
        values.put(Accounts.IS_ACTIVATED, 1);
        values.put(Accounts.API_URL_FORMAT, apiUrlFormat);
        values.put(Accounts.NO_VERSION_SUFFIX, noVersionSuffix);
        return values;
    }

    public static ContentValues createAccount(final Configuration conf, final AccessToken accessToken,
                                              final User user, final int authType, final int color,
                                              final String apiUrlFormat, final boolean sameOAuthSigningUrl,
                                              final boolean noVersionSuffix) {
        if (user == null || user.getId() <= 0 || accessToken == null || user.getId() != accessToken.getUserId())
            return null;
        final ContentValues values = new ContentValues();
        values.put(Accounts.OAUTH_TOKEN, accessToken.getToken());
        values.put(Accounts.OAUTH_TOKEN_SECRET, accessToken.getTokenSecret());
        values.put(Accounts.CONSUMER_KEY, conf.getOAuthConsumerKey());
        values.put(Accounts.CONSUMER_SECRET, conf.getOAuthConsumerSecret());
        values.put(Accounts.AUTH_TYPE, authType);
        values.put(Accounts.ACCOUNT_ID, user.getId());
        values.put(Accounts.SCREEN_NAME, user.getScreenName());
        values.put(Accounts.NAME, user.getName());
        values.put(Accounts.PROFILE_IMAGE_URL, user.getProfileImageUrlHttps());
        values.put(Accounts.PROFILE_BANNER_URL, user.getProfileBannerImageUrl());
        values.put(Accounts.COLOR, color);
        values.put(Accounts.IS_ACTIVATED, 1);
        values.put(Accounts.API_URL_FORMAT, apiUrlFormat);
        values.put(Accounts.SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl);
        values.put(Accounts.NO_VERSION_SUFFIX, noVersionSuffix);
        return values;
    }

    public static ContentValues createAccount(final Configuration conf, final User user, final int color,
                                              final String apiUrlFormat, final boolean noVersionSuffix) {
        if (user == null || user.getId() <= 0) return null;
        final ContentValues values = new ContentValues();
        values.put(Accounts.AUTH_TYPE, Accounts.AUTH_TYPE_TWIP_O_MODE);
        values.put(Accounts.ACCOUNT_ID, user.getId());
        values.put(Accounts.SCREEN_NAME, user.getScreenName());
        values.put(Accounts.NAME, user.getName());
        values.put(Accounts.PROFILE_IMAGE_URL, (user.getProfileImageUrlHttps()));
        values.put(Accounts.PROFILE_BANNER_URL, (user.getProfileBannerImageUrl()));
        values.put(Accounts.COLOR, color);
        values.put(Accounts.IS_ACTIVATED, 1);
        values.put(Accounts.API_URL_FORMAT, apiUrlFormat);
        values.put(Accounts.NO_VERSION_SUFFIX, noVersionSuffix);
        return values;
    }

    public static ContentValues createCachedRelationship(final Relationship relationship,
                                                         final long accountId) {
        final ContentValues values = new ContentValues();
        values.put(CachedRelationships.ACCOUNT_ID, accountId);
        values.put(CachedRelationships.USER_ID, relationship.getTargetUserId());
        values.put(CachedRelationships.FOLLOWING, relationship.isSourceFollowingTarget());
        values.put(CachedRelationships.FOLLOWED_BY, relationship.isSourceFollowedByTarget());
        values.put(CachedRelationships.BLOCKING, relationship.isSourceBlockingTarget());
        values.put(CachedRelationships.BLOCKED_BY, relationship.isSourceBlockedByTarget());
        values.put(CachedRelationships.MUTING, relationship.isSourceMutingTarget());
        return values;
    }

    public static ContentValues createCachedUser(final User user) {
        if (user == null || user.getId() <= 0) return null;
        final String profile_image_url = user.getProfileImageUrlHttps();
        final String url = user.getURL();
        final URLEntity[] urls = user.getURLEntities();
        final ContentValues values = new ContentValues();
        values.put(CachedUsers.USER_ID, user.getId());
        values.put(CachedUsers.NAME, user.getName());
        values.put(CachedUsers.SCREEN_NAME, user.getScreenName());
        values.put(CachedUsers.PROFILE_IMAGE_URL, profile_image_url);
        values.put(CachedUsers.PROFILE_BANNER_URL, user.getProfileBannerImageUrl());
        values.put(CachedUsers.CREATED_AT, user.getCreatedAt().getTime());
        values.put(CachedUsers.IS_PROTECTED, user.isProtected());
        values.put(CachedUsers.IS_VERIFIED, user.isVerified());
        values.put(CachedUsers.IS_FOLLOWING, user.isFollowing());
        values.put(CachedUsers.FAVORITES_COUNT, user.getFavouritesCount());
        values.put(CachedUsers.FOLLOWERS_COUNT, user.getFollowersCount());
        values.put(CachedUsers.FRIENDS_COUNT, user.getFriendsCount());
        values.put(CachedUsers.STATUSES_COUNT, user.getStatusesCount());
        values.put(CachedUsers.LISTED_COUNT, user.getListedCount());
        values.put(CachedUsers.LOCATION, user.getLocation());
        values.put(CachedUsers.DESCRIPTION_PLAIN, user.getDescription());
        values.put(CachedUsers.DESCRIPTION_HTML, TwitterContentUtils.formatUserDescription(user));
        values.put(CachedUsers.DESCRIPTION_EXPANDED, TwitterContentUtils.formatExpandedUserDescription(user));
        values.put(CachedUsers.URL, url);
        if (url != null && urls != null && urls.length > 0) {
            values.put(CachedUsers.URL_EXPANDED, urls[0].getExpandedURL());
        }
        values.put(CachedUsers.BACKGROUND_COLOR, ParseUtils.parseColor("#" + user.getProfileBackgroundColor(), 0));
        values.put(CachedUsers.LINK_COLOR, ParseUtils.parseColor("#" + user.getProfileLinkColor(), 0));
        values.put(CachedUsers.TEXT_COLOR, ParseUtils.parseColor("#" + user.getProfileTextColor(), 0));
        return values;
    }

    public static ContentValues createDirectMessage(final DirectMessage message, final long accountId,
                                                    final boolean isOutgoing) {
        if (message == null) return null;
        final ContentValues values = new ContentValues();
        final User sender = message.getSender(), recipient = message.getRecipient();
        if (sender == null || recipient == null) return null;
        final String sender_profile_image_url = sender.getProfileImageUrlHttps();
        final String recipient_profile_image_url = recipient.getProfileImageUrlHttps();
        values.put(DirectMessages.ACCOUNT_ID, accountId);
        values.put(DirectMessages.MESSAGE_ID, message.getId());
        values.put(DirectMessages.MESSAGE_TIMESTAMP, message.getCreatedAt().getTime());
        values.put(DirectMessages.SENDER_ID, sender.getId());
        values.put(DirectMessages.RECIPIENT_ID, recipient.getId());
        if (isOutgoing) {
            values.put(DirectMessages.CONVERSATION_ID, recipient.getId());
        } else {
            values.put(DirectMessages.CONVERSATION_ID, sender.getId());
        }
        final String text_html = TwitterContentUtils.formatDirectMessageText(message);
        values.put(DirectMessages.TEXT_HTML, text_html);
        values.put(DirectMessages.TEXT_PLAIN, message.getText());
        values.put(DirectMessages.TEXT_UNESCAPED, toPlainText(text_html));
        values.put(DirectMessages.IS_OUTGOING, isOutgoing);
        values.put(DirectMessages.SENDER_NAME, sender.getName());
        values.put(DirectMessages.SENDER_SCREEN_NAME, sender.getScreenName());
        values.put(DirectMessages.RECIPIENT_NAME, recipient.getName());
        values.put(DirectMessages.RECIPIENT_SCREEN_NAME, recipient.getScreenName());
        values.put(DirectMessages.SENDER_PROFILE_IMAGE_URL, sender_profile_image_url);
        values.put(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL, recipient_profile_image_url);
        final ParcelableMedia[] mediaArray = ParcelableMedia.fromEntities(message);
        if (mediaArray != null) {
            values.put(DirectMessages.MEDIA_LIST, SimpleValueSerializer.toSerializedString(mediaArray));
        }
        return values;
    }

    public static ContentValues createDirectMessage(final ParcelableDirectMessage message) {
        if (message == null) return null;
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
        if (message.media != null) {
            values.put(Statuses.MEDIA_LIST, SimpleValueSerializer.toSerializedString(message.media));
        }
        return values;
    }

    public static ContentValues createFilteredUser(final ParcelableStatus status) {
        if (status == null) return null;
        final ContentValues values = new ContentValues();
        values.put(Filters.Users.USER_ID, status.user_id);
        values.put(Filters.Users.NAME, status.user_name);
        values.put(Filters.Users.SCREEN_NAME, status.user_screen_name);
        return values;
    }

    public static ContentValues createFilteredUser(final ParcelableUser user) {
        if (user == null) return null;
        final ContentValues values = new ContentValues();
        values.put(Filters.Users.USER_ID, user.id);
        values.put(Filters.Users.NAME, user.name);
        values.put(Filters.Users.SCREEN_NAME, user.screen_name);
        return values;
    }

    public static ContentValues createFilteredUser(final ParcelableUserMention user) {
        if (user == null) return null;
        final ContentValues values = new ContentValues();
        values.put(Filters.Users.USER_ID, user.id);
        values.put(Filters.Users.NAME, user.name);
        values.put(Filters.Users.SCREEN_NAME, user.screen_name);
        return values;
    }

    public static ContentValues createMessageDraft(final long accountId, final long recipientId,
                                                   final String text, final String imageUri) {
        final ContentValues values = new ContentValues();
        values.put(Drafts.ACTION_TYPE, Drafts.ACTION_SEND_DIRECT_MESSAGE);
        values.put(Drafts.TEXT, text);
        values.put(Drafts.ACCOUNT_IDS, TwidereArrayUtils.toString(new long[]{accountId}, ',', false));
        values.put(Drafts.TIMESTAMP, System.currentTimeMillis());
        if (imageUri != null) {
            final ParcelableMediaUpdate[] mediaArray = {new ParcelableMediaUpdate(imageUri, 0)};
            values.put(Drafts.MEDIA, JSONSerializer.toJSONArrayString(mediaArray));
        }
        final JSONObject extras = new JSONObject();
        try {
            extras.put(EXTRA_RECIPIENT_ID, recipientId);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
        values.put(Drafts.ACTION_EXTRAS, extras.toString());
        return values;
    }

    public static ContentValues createSavedSearch(final SavedSearch savedSearch, final long accountId) {
        final ContentValues values = new ContentValues();
        values.put(SavedSearches.ACCOUNT_ID, accountId);
        values.put(SavedSearches.SEARCH_ID, savedSearch.getId());
        values.put(SavedSearches.CREATED_AT, savedSearch.getCreatedAt().getTime());
        values.put(SavedSearches.NAME, savedSearch.getName());
        values.put(SavedSearches.QUERY, savedSearch.getQuery());
        return values;
    }

    public static ContentValues[] createSavedSearches(final List<SavedSearch> savedSearches, long accountId) {
        final ContentValues[] resultValuesArray = new ContentValues[savedSearches.size()];
        for (int i = 0, j = savedSearches.size(); i < j; i++) {
            resultValuesArray[i] = createSavedSearch(savedSearches.get(i), accountId);
        }
        return resultValuesArray;
    }

    public static ContentValues createStatus(final Status orig, final long accountId) {
        if (orig == null || orig.getId() <= 0) return null;
        final ContentValues values = new ContentValues();
        values.put(Statuses.ACCOUNT_ID, accountId);
        values.put(Statuses.STATUS_ID, orig.getId());
        values.put(Statuses.STATUS_TIMESTAMP, orig.getCreatedAt().getTime());
        final Status status;
        if (orig.isRetweet()) {
            final Status retweetedStatus = orig.getRetweetedStatus();
            final User retweetUser = orig.getUser();
            final long retweetedById = retweetUser.getId();
            values.put(Statuses.RETWEET_ID, retweetedStatus.getId());
            values.put(Statuses.RETWEET_TIMESTAMP, retweetedStatus.getCreatedAt().getTime());
            values.put(Statuses.RETWEETED_BY_USER_ID, retweetedById);
            values.put(Statuses.RETWEETED_BY_USER_NAME, retweetUser.getName());
            values.put(Statuses.RETWEETED_BY_USER_SCREEN_NAME, retweetUser.getScreenName());
            values.put(Statuses.RETWEETED_BY_USER_PROFILE_IMAGE, (retweetUser.getProfileImageUrlHttps()));
            values.put(Statuses.IS_RETWEET, true);
            if (retweetedById == accountId) {
                values.put(Statuses.MY_RETWEET_ID, orig.getId());
            } else {
                values.put(Statuses.MY_RETWEET_ID, orig.getCurrentUserRetweet());
            }
            status = retweetedStatus;
        } else if (orig.isQuote()) {
            final Status quotedStatus = orig.getQuotedStatus();
            final User quoteUser = orig.getUser();
            final long quotedById = quoteUser.getId();
            values.put(Statuses.QUOTE_ID, quotedStatus.getId());
            final String textHtml = TwitterContentUtils.formatStatusText(orig);
            values.put(Statuses.QUOTE_TEXT_HTML, textHtml);
            values.put(Statuses.QUOTE_TEXT_PLAIN, orig.getText());
            values.put(Statuses.QUOTE_TEXT_UNESCAPED, toPlainText(textHtml));
            values.put(Statuses.QUOTE_TIMESTAMP, orig.getCreatedAt().getTime());
            values.put(Statuses.QUOTE_SOURCE, orig.getSource());

            values.put(Statuses.QUOTED_BY_USER_ID, quotedById);
            values.put(Statuses.QUOTED_BY_USER_NAME, quoteUser.getName());
            values.put(Statuses.QUOTED_BY_USER_SCREEN_NAME, quoteUser.getScreenName());
            values.put(Statuses.QUOTED_BY_USER_PROFILE_IMAGE, quoteUser.getProfileImageUrlHttps());
            values.put(Statuses.QUOTED_BY_USER_IS_VERIFIED, quoteUser.isVerified());
            values.put(Statuses.QUOTED_BY_USER_IS_PROTECTED, quoteUser.isProtected());
            values.put(Statuses.IS_QUOTE, true);
            if (quotedById == accountId) {
                values.put(Statuses.MY_QUOTE_ID, orig.getId());
//            } else {
//                values.put(Statuses.MY_QUOTE_ID, orig.getCurrentUserRetweet());
            }
            status = quotedStatus;
        } else {
            values.put(Statuses.MY_RETWEET_ID, orig.getCurrentUserRetweet());
            status = orig;
        }
        final User user = status.getUser();
        final long userId = user.getId();
        final String profileImageUrl = (user.getProfileImageUrlHttps());
        final String name = user.getName(), screenName = user.getScreenName();
        values.put(Statuses.USER_ID, userId);
        values.put(Statuses.USER_NAME, name);
        values.put(Statuses.USER_SCREEN_NAME, screenName);
        values.put(Statuses.IS_PROTECTED, user.isProtected());
        values.put(Statuses.IS_VERIFIED, user.isVerified());
        values.put(Statuses.USER_PROFILE_IMAGE_URL, profileImageUrl);
        values.put(CachedUsers.IS_FOLLOWING, user.isFollowing());
        final String textHtml = TwitterContentUtils.formatStatusText(status);
        values.put(Statuses.TEXT_HTML, textHtml);
        values.put(Statuses.TEXT_PLAIN, status.getText());
        values.put(Statuses.TEXT_UNESCAPED, toPlainText(textHtml));
        values.put(Statuses.RETWEET_COUNT, status.getRetweetCount());
        values.put(Statuses.REPLY_COUNT, status.getReplyCount());
        values.put(Statuses.FAVORITE_COUNT, status.getFavoriteCount());
        values.put(Statuses.DESCENDENT_REPLY_COUNT, status.getDescendentReplyCount());
        values.put(Statuses.IN_REPLY_TO_STATUS_ID, status.getInReplyToStatusId());
        values.put(Statuses.IN_REPLY_TO_USER_ID, status.getInReplyToUserId());
        values.put(Statuses.IN_REPLY_TO_USER_NAME, TwitterContentUtils.getInReplyToName(status));
        values.put(Statuses.IN_REPLY_TO_USER_SCREEN_NAME, status.getInReplyToScreenName());
        values.put(Statuses.SOURCE, status.getSource());
        values.put(Statuses.IS_POSSIBLY_SENSITIVE, status.isPossiblySensitive());
        final GeoLocation location = status.getGeoLocation();
        if (location != null) {
            values.put(Statuses.LOCATION, ParcelableLocation.toString(location.getLatitude(), location.getLongitude()));
        }
        final Place place = status.getPlace();
        if (place != null) {
            values.put(Statuses.PLACE_FULL_NAME, place.getFullName());
        }
        values.put(Statuses.IS_FAVORITE, status.isFavorited());
        final ParcelableMedia[] media = ParcelableMedia.fromEntities(status);
        if (media != null) {
            values.put(Statuses.MEDIA_LIST, SimpleValueSerializer.toSerializedString(media));
        }
        final ParcelableUserMention[] mentions = ParcelableUserMention.fromStatus(status);
        if (mentions != null) {
            values.put(Statuses.MENTIONS_LIST, SimpleValueSerializer.toSerializedString(mentions));
        }
        final ParcelableCardEntity card = ParcelableCardEntity.fromCardEntity(status.getCard(), accountId);
        if (card != null) {
            values.put(Statuses.CARD_NAME, card.name);
            values.put(Statuses.CARD, JSONSerializer.toJSONObjectString(card));
        }
        return values;
    }

    public static ContentValues createStatusDraft(final ParcelableStatusUpdate status) {
        return createStatusDraft(status, ParcelableAccount.getAccountIds(status.accounts));
    }

    public static ContentValues createStatusDraft(final ParcelableStatusUpdate status,
                                                  final long[] accountIds) {
        final ContentValues values = new ContentValues();
        values.put(Drafts.ACTION_TYPE, Drafts.ACTION_UPDATE_STATUS);
        values.put(Drafts.TEXT, status.text);
        values.put(Drafts.ACCOUNT_IDS, TwidereArrayUtils.toString(accountIds, ',', false));
        values.put(Drafts.IN_REPLY_TO_STATUS_ID, status.in_reply_to_status_id);
        values.put(Drafts.LOCATION, ParcelableLocation.toString(status.location));
        values.put(Drafts.IS_POSSIBLY_SENSITIVE, status.is_possibly_sensitive);
        values.put(Drafts.TIMESTAMP, System.currentTimeMillis());
        if (status.media != null) {
            values.put(Drafts.MEDIA, JSONSerializer.toJSONArrayString(status.media));
        }
        return values;
    }

    public static ContentValues[] createTrends(final List<Trends> trendsList) {
        if (trendsList == null) return new ContentValues[0];
        final List<ContentValues> resultList = new ArrayList<>();
        for (final Trends trends : trendsList) {
            final long timestamp = trends.getTrendAt().getTime();
            for (final Trend trend : trends.getTrends()) {
                final ContentValues values = new ContentValues();
                values.put(CachedTrends.NAME, trend.getName());
                values.put(CachedTrends.TIMESTAMP, timestamp);
                resultList.add(values);
            }
        }
        return resultList.toArray(new ContentValues[resultList.size()]);
    }

}
