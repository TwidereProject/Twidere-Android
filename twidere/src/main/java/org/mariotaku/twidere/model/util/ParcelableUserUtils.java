package org.mariotaku.twidere.model.util;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import org.mariotaku.microblog.library.twitter.model.UrlEntity;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SpanItem;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.TwitterContentUtils;
import org.mariotaku.twidere.util.UserColorNameManager;

/**
 * Processing ParcelableUser
 * <p/>
 * Created by mariotaku on 16/2/24.
 */
public class ParcelableUserUtils implements TwidereConstants {
    private ParcelableUserUtils() {
    }

    public static ParcelableUser fromUser(@NonNull User user, @Nullable UserKey accountKey) {
        return fromUser(user, accountKey, 0);
    }

    public static ParcelableUser fromUser(@NonNull User user, @Nullable UserKey accountKey, long position) {
        final UrlEntity[] urlEntities = user.getUrlEntities();
        final ParcelableUser obj = new ParcelableUser();
        obj.position = position;
        obj.account_key = accountKey;
        obj.key = UserKeyUtils.fromUser(user);
        obj.created_at = user.getCreatedAt().getTime();
        obj.is_protected = user.isProtected();
        obj.is_verified = user.isVerified();
        obj.name = user.getName();
        obj.screen_name = user.getScreenName();
        obj.description_plain = user.getDescription();
        final Pair<String, SpanItem[]> userDescription = InternalTwitterContentUtils.formatUserDescription(user);
        if (userDescription != null) {
            obj.description_unescaped = userDescription.first;
            obj.description_spans = userDescription.second;
        }
        obj.location = user.getLocation();
        obj.profile_image_url = TwitterContentUtils.getProfileImageUrl(user);
        obj.profile_banner_url = user.getProfileBannerImageUrl();
        obj.profile_background_url = user.getProfileBackgroundImageUrlHttps();
        if (TextUtils.isEmpty(obj.profile_background_url)) {
            obj.profile_background_url = user.getProfileBackgroundImageUrl();
        }
        obj.url = user.getUrl();
        if (obj.url != null && urlEntities != null && urlEntities.length > 0) {
            obj.url_expanded = urlEntities[0].getExpandedUrl();
        }
        obj.is_follow_request_sent = user.isFollowRequestSent();
        obj.followers_count = user.getFollowersCount();
        obj.friends_count = user.getFriendsCount();
        obj.statuses_count = user.getStatusesCount();
        obj.favorites_count = user.getFavouritesCount();
        obj.listed_count = user.getListedCount();
        obj.media_count = user.getMediaCount();
        obj.is_following = user.isFollowing();
        obj.background_color = parseColor(user.getProfileBackgroundColor());
        obj.link_color = parseColor(user.getProfileLinkColor());
        obj.text_color = parseColor(user.getProfileTextColor());
        obj.is_cache = false;
        obj.is_basic = false;

        ParcelableUser.Extras extras = new ParcelableUser.Extras();
        extras.ostatus_uri = user.getOstatusUri();
        extras.blocking = user.isBlocking();
        extras.blocked_by = user.isBlockedBy();
        extras.followed_by = user.isFollowedBy();
        extras.muting = user.isMuting();
        extras.statusnet_profile_url = user.getStatusnetProfileUrl();
        extras.profile_image_url_original = user.getProfileImageUrlOriginal();
        extras.profile_image_url_profile_size = user.getProfileImageUrlProfileSize();
        extras.pinned_status_ids = user.getPinnedTweetIds();
        if (extras.profile_image_url_profile_size == null) {
            extras.profile_image_url_profile_size = user.getProfileImageUrlLarge();
        }
        extras.groups_count = user.getGroupsCount();
        extras.unique_id = user.getUniqueId();
        obj.extras = extras;
        return obj;
    }

    public static ParcelableUser fromDirectMessageConversationEntry(final Cursor cursor) {
        final UserKey accountId = UserKey.valueOf(cursor.getString(DirectMessages.ConversationEntries.IDX_ACCOUNT_KEY));
        final UserKey id = UserKey.valueOf(cursor.getString(DirectMessages.ConversationEntries.IDX_CONVERSATION_ID));
        final String name = cursor.getString(DirectMessages.ConversationEntries.IDX_NAME);
        final String screenName = cursor.getString(DirectMessages.ConversationEntries.IDX_SCREEN_NAME);
        final String profileImageUrl = cursor.getString(DirectMessages.ConversationEntries.IDX_PROFILE_IMAGE_URL);
        return new ParcelableUser(accountId, id, name, screenName, profileImageUrl);
    }

    public static ParcelableUser[] fromUsers(final User[] users, UserKey accountKey) {
        if (users == null) return null;
        int size = users.length;
        final ParcelableUser[] result = new ParcelableUser[size];
        for (int i = 0; i < size; i++) {
            result[i] = fromUser(users[i], accountKey);
        }
        return result;
    }

    private static int parseColor(@Nullable String colorString) {
        if (colorString == null) return 0;
        if (!colorString.startsWith("#")) {
            colorString = "#" + colorString;
        }
        return ParseUtils.parseColor(colorString, 0);
    }

    @Nullable
    public static String getProfileBannerUrl(@NonNull ParcelableUser user) {
        if (!TextUtils.isEmpty(user.profile_banner_url)) return user.profile_banner_url;
        if (USER_TYPE_FANFOU_COM.equals(user.key.getHost())) {
            return user.profile_background_url;
        }
        return null;
    }

    public static void updateExtraInformation(ParcelableUser user, ParcelableAccount account, UserColorNameManager manager) {
        user.account_color = account.color;
        user.color = manager.getUserColor(user.key);
        user.nickname = manager.getUserNickname(user.key);
    }

    public static String getExpandedDescription(ParcelableUser user) {
        if (TextUtils.isEmpty(user.description_unescaped)) {
            return user.description_plain;
        }
        if (user.description_spans != null) {
            // TODO expand description
        }
        return user.description_unescaped;
    }
}
