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
import android.support.annotation.NonNull;

import org.mariotaku.microblog.library.twitter.model.DirectMessage;
import org.mariotaku.microblog.library.twitter.model.Relationship;
import org.mariotaku.microblog.library.twitter.model.SavedSearch;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.Trend;
import org.mariotaku.microblog.library.twitter.model.Trends;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.model.CachedRelationship;
import org.mariotaku.twidere.model.CachedRelationshipValuesCreator;
import org.mariotaku.twidere.model.Draft;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableActivityValuesCreator;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableDirectMessageValuesCreator;
import org.mariotaku.twidere.model.ParcelableMediaUpdate;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusValuesCreator;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserMention;
import org.mariotaku.twidere.model.ParcelableUserValuesCreator;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.draft.SendDirectMessageActionExtra;
import org.mariotaku.twidere.model.util.ParcelableActivityExtensionsKt;
import org.mariotaku.twidere.model.util.ParcelableDirectMessageUtils;
import org.mariotaku.twidere.model.util.ParcelableStatusUtils;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;
import org.mariotaku.twidere.provider.TwidereDataStore.SavedSearches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ContentValuesCreator implements TwidereConstants {
    private ContentValuesCreator() {
    }

    public static ContentValues createCachedRelationship(final Relationship relationship,
                                                         final UserKey accountKey,
                                                         final UserKey userKey) {
        CachedRelationship cached = new CachedRelationship(relationship, accountKey, userKey);
        return CachedRelationshipValuesCreator.create(cached);
    }

    public static ContentValues createCachedUser(final User user) {
        if (user == null) return null;
        final ContentValues values = new ContentValues();
        ParcelableUserValuesCreator.writeTo(ParcelableUserUtils.fromUser(user, null), values);
        return values;
    }

    public static ContentValues createDirectMessage(final DirectMessage message,
                                                    final UserKey accountKey,
                                                    final boolean isOutgoing) {
        return ParcelableDirectMessageValuesCreator.create(ParcelableDirectMessageUtils.fromDirectMessage(message,
                accountKey, isOutgoing));
    }

    public static ContentValues createDirectMessage(final ParcelableDirectMessage message) {
        if (message == null) return null;
        final ContentValues values = new ContentValues();
        ParcelableDirectMessageValuesCreator.writeTo(message, values);
        return values;
    }

    public static ContentValues createFilteredUser(final ParcelableStatus status) {
        if (status == null) return null;
        final ContentValues values = new ContentValues();
        values.put(Filters.Users.USER_KEY, status.user_key.toString());
        values.put(Filters.Users.NAME, status.user_name);
        values.put(Filters.Users.SCREEN_NAME, status.user_screen_name);
        return values;
    }

    public static ContentValues createFilteredUser(final ParcelableUser user) {
        if (user == null) return null;
        final ContentValues values = new ContentValues();
        values.put(Filters.Users.USER_KEY, user.key.toString());
        values.put(Filters.Users.NAME, user.name);
        values.put(Filters.Users.SCREEN_NAME, user.screen_name);
        return values;
    }

    public static ContentValues createFilteredUser(final ParcelableUserMention user) {
        if (user == null) return null;
        final ContentValues values = new ContentValues();
        values.put(Filters.Users.USER_KEY, user.key.toString());
        values.put(Filters.Users.NAME, user.name);
        values.put(Filters.Users.SCREEN_NAME, user.screen_name);
        return values;
    }

    public static ContentValues createMessageDraft(final UserKey accountKey, final String recipientId,
                                                   final String text, final String imageUri) {
        final ContentValues values = new ContentValues();
        values.put(Drafts.ACTION_TYPE, Draft.Action.SEND_DIRECT_MESSAGE);
        values.put(Drafts.TEXT, text);
        values.put(Drafts.ACCOUNT_KEYS, accountKey.toString());
        values.put(Drafts.TIMESTAMP, System.currentTimeMillis());
        if (imageUri != null) {
            final ParcelableMediaUpdate[] mediaArray = {new ParcelableMediaUpdate(imageUri, 0)};
            values.put(Drafts.MEDIA, JsonSerializer.serialize(Arrays.asList(mediaArray),
                    ParcelableMediaUpdate.class));
        }
        final SendDirectMessageActionExtra extra = new SendDirectMessageActionExtra();
        extra.setRecipientId(recipientId);
        values.put(Drafts.ACTION_EXTRAS, JsonSerializer.serialize(extra));
        return values;
    }

    public static ContentValues createSavedSearch(final SavedSearch savedSearch,
                                                  final UserKey accountKey) {
        final ContentValues values = new ContentValues();
        values.put(SavedSearches.ACCOUNT_KEY, accountKey.toString());
        values.put(SavedSearches.SEARCH_ID, savedSearch.getId());
        values.put(SavedSearches.CREATED_AT, savedSearch.getCreatedAt().getTime());
        values.put(SavedSearches.NAME, savedSearch.getName());
        values.put(SavedSearches.QUERY, savedSearch.getQuery());
        return values;
    }

    public static ContentValues[] createSavedSearches(final List<SavedSearch> savedSearches,
                                                      final UserKey accountKey) {
        final ContentValues[] resultValuesArray = new ContentValues[savedSearches.size()];
        for (int i = 0, j = savedSearches.size(); i < j; i++) {
            resultValuesArray[i] = createSavedSearch(savedSearches.get(i), accountKey);
        }
        return resultValuesArray;
    }

    @NonNull
    public static ContentValues createStatus(final Status orig, final UserKey accountKey) {
        return ParcelableStatusValuesCreator.create(ParcelableStatusUtils.INSTANCE.fromStatus(orig,
                accountKey, false));
    }

    @NonNull
    public static ContentValues createActivity(final ParcelableActivity activity,
                                               final ParcelableCredentials credentials,
                                               final UserColorNameManager manager) {
        final ContentValues values = new ContentValues();
        final ParcelableStatus status = ParcelableActivityExtensionsKt.getActivityStatus(activity);

        activity.account_color = credentials.color;

        if (status != null) {
            ParcelableStatusUtils.INSTANCE.updateExtraInformation(status, credentials, manager);

            activity.status_id = status.id;
            activity.status_retweet_id = status.retweet_id;
            activity.status_my_retweet_id = status.my_retweet_id;

            if (status.is_retweet) {
                activity.status_retweeted_by_user_key = status.retweeted_by_user_key;
            } else if (status.is_quote) {
                activity.status_quote_spans = status.quoted_spans;
                activity.status_quote_text_plain = status.quoted_text_plain;
                activity.status_quote_source = status.quoted_source;
                activity.status_quoted_user_key = status.quoted_user_key;
            }
            activity.status_user_key = status.user_key;
            activity.status_user_following = status.user_is_following;
            activity.status_spans = status.spans;
            activity.status_text_plain = status.text_plain;
            activity.status_source = status.source;

            activity.status_user_color = status.user_color;
            activity.status_retweet_user_color = status.retweet_user_color;
            activity.status_quoted_user_color = status.quoted_user_color;

            activity.status_user_nickname = status.user_nickname;
            activity.status_in_reply_to_user_nickname = status.in_reply_to_user_nickname;
            activity.status_retweet_user_nickname = status.retweet_user_nickname;
            activity.status_quoted_user_nickname = status.quoted_user_nickname;
        }
        ParcelableActivityValuesCreator.writeTo(activity, values);
        return values;
    }

    public static ContentValues[] createTrends(final List<Trends> trendsList) {
        if (trendsList == null) return new ContentValues[0];
        final List<ContentValues> resultList = new ArrayList<>();
        for (final Trends trends : trendsList) {
//            final long timestamp = trends.getAsOf().getTime();
            for (final Trend trend : trends.getTrends()) {
                final ContentValues values = new ContentValues();
                values.put(CachedTrends.NAME, trend.getName());
                values.put(CachedTrends.TIMESTAMP, System.currentTimeMillis());
                resultList.add(values);
            }
        }
        return resultList.toArray(new ContentValues[resultList.size()]);
    }


}
