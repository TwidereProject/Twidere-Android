package org.mariotaku.twidere.model.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.api.statusnet.model.Attention;
import org.mariotaku.twidere.api.twitter.model.GeoLocation;
import org.mariotaku.twidere.api.twitter.model.Place;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserMentionEntity;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SpanItem;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.HtmlSpanBuilder;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.TwitterContentUtils;
import org.mariotaku.twidere.util.UserColorNameManager;

import java.util.Date;
import java.util.List;

/**
 * Created by mariotaku on 16/1/3.
 */
public class ParcelableStatusUtils implements Constants {

    public static void makeOriginalStatus(@NonNull ParcelableStatus status) {
        if (!status.is_retweet) return;
        status.id = status.retweet_id;
        status.retweeted_by_user_key = null;
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
        result.extras.statusnet_conversation_id = orig.getStatusnetConversationId();

        final Status retweetedStatus = orig.getRetweetedStatus();
        result.is_retweet = orig.isRetweet();
        result.retweeted = orig.wasRetweeted();
        if (retweetedStatus != null) {
            final User retweetUser = orig.getUser();
            result.retweet_id = retweetedStatus.getId();
            result.retweet_timestamp = getTime(retweetedStatus.getCreatedAt());
            result.retweeted_by_user_key = UserKeyUtils.fromUser(retweetUser);
            result.retweeted_by_user_name = retweetUser.getName();
            result.retweeted_by_user_screen_name = retweetUser.getScreenName();
            result.retweeted_by_user_profile_image = TwitterContentUtils.getProfileImageUrl(retweetUser);
        }

        final Status quoted = orig.getQuotedStatus();
        result.is_quote = orig.isQuote();
        if (quoted != null) {
            final User quotedUser = quoted.getUser();
            result.quoted_id = quoted.getId();

            final String quotedText = quoted.getHtmlText();
            // Twitter will escape <> to &lt;&gt;, so if a status contains those symbols unescaped
            // We should treat this as an html
            if (isHtml(quotedText)) {
                final CharSequence html = HtmlSpanBuilder.fromHtml(quotedText, quoted.getText());
                result.quoted_text_unescaped = html.toString();
                result.quoted_text_plain = result.quoted_text_unescaped;
                result.quoted_spans = getSpanItems(html);
            } else {
                final Pair<String, List<SpanItem>> textWithIndices = InternalTwitterContentUtils.formatStatusTextWithIndices(quoted);
                result.quoted_text_plain = InternalTwitterContentUtils.unescapeTwitterStatusText(quotedText);
                result.quoted_text_unescaped = textWithIndices.first;
                result.quoted_spans = textWithIndices.second.toArray(new SpanItem[textWithIndices.second.size()]);
            }

            result.quoted_timestamp = quoted.getCreatedAt().getTime();
            result.quoted_source = quoted.getSource();
            result.quoted_media = ParcelableMediaUtils.fromStatus(quoted);
            result.quoted_location = getLocation(quoted);
            result.quoted_place_full_name = getPlaceFullName(quoted);

            result.quoted_user_key = UserKeyUtils.fromUser(quotedUser);
            result.quoted_user_name = quotedUser.getName();
            result.quoted_user_screen_name = quotedUser.getScreenName();
            result.quoted_user_profile_image = TwitterContentUtils.getProfileImageUrl(quotedUser);
            result.quoted_user_is_protected = quotedUser.isProtected();
            result.quoted_user_is_verified = quotedUser.isVerified();
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
        final String text = status.getHtmlText();
        // Twitter will escape <> to &lt;&gt;, so if a status contains those symbols unescaped
        // We should treat this as an html
        if (isHtml(text)) {
            final CharSequence html = HtmlSpanBuilder.fromHtml(text, status.getText());
            result.text_unescaped = html.toString();
            result.text_plain = result.text_unescaped;
            result.spans = getSpanItems(html);
        } else {
            final Pair<String, List<SpanItem>> textWithIndices = InternalTwitterContentUtils.formatStatusTextWithIndices(status);
            result.text_plain = InternalTwitterContentUtils.unescapeTwitterStatusText(text);
            result.text_unescaped = textWithIndices.first;
            result.spans = textWithIndices.second.toArray(new SpanItem[textWithIndices.second.size()]);
        }
        result.media = ParcelableMediaUtils.fromStatus(status);
        result.source = status.getSource();
        result.location = getLocation(status);
        result.is_favorite = status.isFavorited();
        if (result.account_key.maybeEquals(result.retweeted_by_user_key)) {
            result.my_retweet_id = result.id;
        } else {
            result.my_retweet_id = status.getCurrentUserRetweet();
        }
        result.is_possibly_sensitive = status.isPossiblySensitive();
        result.mentions = ParcelableUserMentionUtils.fromUserMentionEntities(user,
                status.getUserMentionEntities());
        result.card = ParcelableCardEntityUtils.fromCardEntity(status.getCard(), accountKey);
        result.place_full_name = getPlaceFullName(status);
        result.card_name = result.card != null ? result.card.name : null;
        result.lang = status.getLang();
        return result;
    }

    private static SpanItem[] getSpanItems(CharSequence html) {
        if (!(html instanceof Spanned)) return null;
        Spanned spanned = (Spanned) html;
        URLSpan[] spans = spanned.getSpans(0, html.length(), URLSpan.class);
        SpanItem[] items = new SpanItem[spans.length];
        for (int i = 0, j = spans.length; i < j; i++) {
            items[i] = SpanItem.from(spanned, spans[i]);
        }
        return items;
    }

    private static boolean isHtml(String text) {
        return text.contains("<") && text.contains(">");
    }

    private static boolean isFanfouStatus(UserKey accountKey) {
        return USER_TYPE_FANFOU_COM.equals(accountKey.getHost());
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
    private static String getPlaceFullName(@NonNull Status status) {
        Place place = status.getPlace();
        if (place != null) return place.getFullName();
        final String location = status.getLocation();
        if (ParcelableLocation.valueOf(location) == null) {
            return location;
        }
        return null;
    }

    @Nullable
    private static ParcelableLocation getLocation(@NonNull Status status) {
        GeoLocation geoLocation = status.getGeoLocation();
        if (geoLocation != null) {
            return ParcelableLocationUtils.fromGeoLocation(geoLocation);
        }
        final String locationString = status.getLocation();
        final ParcelableLocation location = ParcelableLocation.valueOf(locationString);
        if (location != null) {
            return location;
        }
        return null;
    }

    private static long getTime(final Date date) {
        return date != null ? date.getTime() : 0;
    }

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

    public static void applySpans(@NonNull SpannableStringBuilder text, @Nullable SpanItem[] spans) {
        if (spans == null) return;
        for (SpanItem span : spans) {
            text.setSpan(new URLSpan(span.link), span.start, span.end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public static void updateExtraInformation(ParcelableStatus status, ParcelableCredentials credentials, UserColorNameManager manager) {
        status.account_color = credentials.color;
        status.user_color = manager.getUserColor(status.user_key);
        status.user_nickname = manager.getUserNickname(status.user_key);

        if (status.quoted_user_key != null) {
            status.quoted_user_color = manager.getUserColor(status.quoted_user_key);
            status.quoted_user_nickname = manager.getUserNickname(status.quoted_user_key);
        }
        if (status.retweeted_by_user_key != null) {
            status.retweet_user_color = manager.getUserColor(status.retweeted_by_user_key);
            status.retweet_user_nickname = manager.getUserNickname(status.retweeted_by_user_key);
        }

        if (status.in_reply_to_user_id != null) {
            status.in_reply_to_user_nickname = manager.getUserNickname(status.in_reply_to_user_id);
        }
    }
}
