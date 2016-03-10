package org.mariotaku.twidere.model.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.twidere.api.fanfou.model.FanfouSearchStatus;
import org.mariotaku.twidere.api.statusnet.model.Attention;
import org.mariotaku.twidere.api.twitter.model.Place;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserMentionEntity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.HtmlEscapeHelper;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.TwitterContentUtils;

import java.util.Date;

/**
 * Created by mariotaku on 16/1/3.
 */
public class ParcelableStatusUtils {

    public static void makeOriginalStatus(@NonNull ParcelableStatus status) {
        if (!status.is_retweet) return;
        status.id = status.retweet_id;
        status.retweeted_by_user_id = null;
        status.retweeted_by_user_name = null;
        status.retweeted_by_user_screen_name = null;
        status.retweeted_by_user_profile_image = null;
        status.retweet_timestamp = -1;
        status.retweet_id = null;
    }

    public static ParcelableStatus fromStatus(final Status orig, final UserKey accountKey,
                                              final boolean isGap) {
        final ParcelableStatus result = new ParcelableStatus();
        result.is_gap = isGap;
        result.account_key = accountKey;
        result.id = orig.getId();
        result.sort_id = orig.getSortId();
        result.timestamp = getTime(orig.getCreatedAt());

        result.extras = new ParcelableStatus.Extras();
        result.extras.external_url = orig.getExternalUrl();
        result.extras.support_entities = orig.getEntities() != null;

        final Status retweetedStatus = orig.getRetweetedStatus();
        result.is_retweet = orig.isRetweet();
        result.retweeted = orig.wasRetweeted();
        if (retweetedStatus != null) {
            final User retweetUser = orig.getUser();
            result.retweet_id = retweetedStatus.getId();
            result.retweet_timestamp = getTime(retweetedStatus.getCreatedAt());
            result.retweeted_by_user_id = UserKeyUtils.fromUser(retweetUser);
            result.retweeted_by_user_name = retweetUser.getName();
            result.retweeted_by_user_screen_name = retweetUser.getScreenName();
            result.retweeted_by_user_profile_image = TwitterContentUtils.getProfileImageUrl(retweetUser);
        }

        final Status quoted = orig.getQuotedStatus();
        result.is_quote = orig.isQuote();
        if (quoted != null) {
            final User quoted_user = quoted.getUser();
            result.quoted_id = quoted.getId();
            result.quoted_text_html = InternalTwitterContentUtils.formatStatusText(quoted);
            result.quoted_text_plain = InternalTwitterContentUtils.unescapeTwitterStatusText(quoted.getText());
            result.quoted_text_unescaped = HtmlEscapeHelper.toPlainText(result.quoted_text_html);
            result.quoted_timestamp = quoted.getCreatedAt().getTime();
            result.quoted_source = quoted.getSource();
            result.quoted_media = ParcelableMediaUtils.fromStatus(quoted);
            result.quoted_location = ParcelableLocationUtils.fromGeoLocation(quoted.getGeoLocation());
            result.quoted_place_full_name = getPlaceFullName(quoted.getPlace());

            result.quoted_user_id = UserKeyUtils.fromUser(quoted_user);
            result.quoted_user_name = quoted_user.getName();
            result.quoted_user_screen_name = quoted_user.getScreenName();
            result.quoted_user_profile_image = TwitterContentUtils.getProfileImageUrl(quoted_user);
            result.quoted_user_is_protected = quoted_user.isProtected();
            result.quoted_user_is_verified = quoted_user.isVerified();
        }

        final Status status;
        if (retweetedStatus != null) {
            status = retweetedStatus;
            result.reply_count = retweetedStatus.getReplyCount();
            result.retweet_count = retweetedStatus.getRetweetCount();
            result.favorite_count = retweetedStatus.getFavoriteCount();


            result.in_reply_to_name = getInReplyToName(retweetedStatus);
            result.in_reply_to_screen_name = retweetedStatus.getInReplyToScreenName();
            result.in_reply_to_status_id = retweetedStatus.getInReplyToStatusId();
            result.in_reply_to_user_id = getInReplyToUserId(retweetedStatus, accountKey);
        } else {
            status = orig;
            result.reply_count = orig.getReplyCount();
            result.retweet_count = orig.getRetweetCount();
            result.favorite_count = orig.getFavoriteCount();

            result.in_reply_to_name = getInReplyToName(orig);
            result.in_reply_to_screen_name = orig.getInReplyToScreenName();
            result.in_reply_to_status_id = orig.getInReplyToStatusId();
            result.in_reply_to_user_id = getInReplyToUserId(orig, accountKey);
        }

        final User user = status.getUser();
        result.user_key = UserKeyUtils.fromUser(user);
        result.user_name = user.getName();
        result.user_screen_name = user.getScreenName();
        result.user_profile_image_url = TwitterContentUtils.getProfileImageUrl(user);
        result.user_is_protected = user.isProtected();
        result.user_is_verified = user.isVerified();
        result.user_is_following = user.isFollowing();
        result.extras.user_profile_image_url_profile_size = user.getProfileImageUrlProfileSize();
        if (result.extras.user_profile_image_url_profile_size == null) {
            result.extras.user_profile_image_url_profile_size = user.getProfileImageUrlLarge();
        }
        if (status instanceof FanfouSearchStatus) {
            result.text_html = status.getText();
            result.text_plain = HtmlEscapeHelper.toPlainText(result.text_html);
        } else {
            result.text_html = InternalTwitterContentUtils.formatStatusText(status);
            result.text_plain = InternalTwitterContentUtils.unescapeTwitterStatusText(status.getText());
        }
        result.media = ParcelableMediaUtils.fromStatus(status);
        result.source = status.getSource();
        result.location = ParcelableLocationUtils.fromGeoLocation(status.getGeoLocation());
        result.is_favorite = status.isFavorited();
        result.text_unescaped = HtmlEscapeHelper.toPlainText(result.text_html);
        if (result.account_key.maybeEquals(result.retweeted_by_user_id)) {
            result.my_retweet_id = result.id;
        } else {
            result.my_retweet_id = status.getCurrentUserRetweet();
        }
        result.is_possibly_sensitive = status.isPossiblySensitive();
        result.mentions = ParcelableUserMentionUtils.fromUserMentionEntities(user,
                status.getUserMentionEntities());
        result.card = ParcelableCardEntityUtils.fromCardEntity(status.getCard(), accountKey);
        result.place_full_name = getPlaceFullName(status.getPlace());
        result.card_name = result.card != null ? result.card.name : null;
        result.lang = status.getLang();
        return result;
    }

    @Nullable
    private static UserKey getInReplyToUserId(Status status, UserKey accountKey) {
        final String inReplyToUserId = status.getInReplyToUserId();
        if (inReplyToUserId == null) return null;
        final UserMentionEntity[] entities = status.getUserMentionEntities();
        if (entities != null) {
            for (final UserMentionEntity entity : entities) {
                if (TextUtils.equals(inReplyToUserId, entity.getId())) {
                    return new UserKey(inReplyToUserId, accountKey.getHost());
                }
            }
        }
        final Attention[] attentions = status.getAttentions();
        if (attentions != null) {
            for (Attention attention : attentions) {
                if (TextUtils.equals(inReplyToUserId, attention.getId())) {
                    final String host = UserKeyUtils.getUserHost(attention.getOstatusUri(),
                            accountKey.getHost());
                    return new UserKey(inReplyToUserId, host);
                }
            }
        }
        return new UserKey(inReplyToUserId, accountKey.getHost());
    }

    public static ParcelableStatus[] fromStatuses(Status[] statuses, UserKey accountKey) {
        if (statuses == null) return null;
        int size = statuses.length;
        final ParcelableStatus[] result = new ParcelableStatus[size];
        for (int i = 0; i < size; i++) {
            result[i] = fromStatus(statuses[i], accountKey, false);
        }
        return result;
    }

    @Nullable
    private static String getPlaceFullName(@Nullable Place place) {
        if (place == null) return null;
        return place.getFullName();
    }

    private static long getTime(final Date date) {
        return date != null ? date.getTime() : 0;
    }

    @NonNull
    public static String getInReplyToName(@NonNull final Status status) {
        final String inReplyToUserId = status.getInReplyToUserId();
        final UserMentionEntity[] entities = status.getUserMentionEntities();
        if (entities != null) {
            for (final UserMentionEntity entity : entities) {
                if (TextUtils.equals(inReplyToUserId, entity.getId())) return entity.getName();
            }
        }
        final Attention[] attentions = status.getAttentions();
        if (attentions != null) {
            for (Attention attention : attentions) {
                if (TextUtils.equals(inReplyToUserId, attention.getId())) {
                    return attention.getFullName();
                }
            }
        }
        return status.getInReplyToScreenName();
    }
}
