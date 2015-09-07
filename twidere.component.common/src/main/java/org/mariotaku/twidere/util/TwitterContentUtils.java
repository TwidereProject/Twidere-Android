/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.DirectMessage;
import org.mariotaku.twidere.api.twitter.model.EntitySupport;
import org.mariotaku.twidere.api.twitter.model.MediaEntity;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.UrlEntity;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserMentionEntity;
import org.mariotaku.twidere.api.twitter.model.impl.StatusImpl;
import org.mariotaku.twidere.common.R;
import org.mariotaku.twidere.model.ConsumerKeyType;
import org.mariotaku.twidere.util.collection.LongSparseMap;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;

/**
 * Created by mariotaku on 15/1/11.
 */
public class TwitterContentUtils {

    public static final int TWITTER_BULK_QUERY_COUNT = 100;
    private static final Pattern PATTERN_TWITTER_STATUS_LINK = Pattern.compile("https?://twitter\\.com/(?:#!/)?(\\w+)/status(es)?/(\\d+)");

    public static String formatDirectMessageText(final DirectMessage message) {
        if (message == null) return null;
        final HtmlBuilder builder = new HtmlBuilder(message.getText(), false, true, true);
        TwitterContentUtils.parseEntities(builder, message);
        return builder.build().replace("\n", "<br/>");
    }

    public static String formatExpandedUserDescription(final User user) {
        if (user == null) return null;
        final String text = user.getDescription();
        if (text == null) return null;
        final HtmlBuilder builder = new HtmlBuilder(text, false, true, true);
        final UrlEntity[] urls = user.getDescriptionEntities();
        if (urls != null) {
            for (final UrlEntity url : urls) {
                final String expanded_url = url.getExpandedUrl();
                if (expanded_url != null) {
                    builder.addLink(expanded_url, expanded_url, url.getStart(), url.getEnd());
                }
            }
        }
        return toPlainText(builder.build().replace("\n", "<br/>"));
    }

    public static String formatStatusText(final Status status) {
        if (status == null) return null;
        final HtmlBuilder builder = new HtmlBuilder(status.getText(), false, true, true);
        TwitterContentUtils.parseEntities(builder, status);
        return builder.build().replace("\n", "<br/>");
    }

    public static String formatUserDescription(final User user) {
        if (user == null) return null;
        final String text = user.getDescription();
        if (text == null) return null;
        final HtmlBuilder builder = new HtmlBuilder(text, false, true, true);
        final UrlEntity[] urls = user.getDescriptionEntities();
        if (urls != null) {
            for (final UrlEntity url : urls) {
                final String expanded_url = url.getExpandedUrl();
                if (expanded_url != null) {
                    builder.addLink(expanded_url, url.getDisplayUrl(), url.getStart(), url.getEnd());
                }
            }
        }
        return builder.build().replace("\n", "<br/>");
    }

    @NonNull
    public static String getInReplyToName(@NonNull final Status status) {
        final Status orig = status.isRetweet() ? status.getRetweetedStatus() : status;
        final long inReplyToUserId = status.getInReplyToUserId();
        final UserMentionEntity[] entities = status.getUserMentionEntities();
        if (entities == null) return orig.getInReplyToScreenName();
        for (final UserMentionEntity entity : entities) {
            if (inReplyToUserId == entity.getId()) return entity.getName();
        }
        return orig.getInReplyToScreenName();
    }

    public static boolean isOfficialKey(final Context context, final String consumerKey,
                                        final String consumerSecret) {
        if (context == null || consumerKey == null || consumerSecret == null) return false;
        final String[] keySecrets = context.getResources().getStringArray(R.array.values_official_consumer_secret_crc32);
        final CRC32 crc32 = new CRC32();
        final byte[] consumerSecretBytes = consumerSecret.getBytes(Charset.forName("UTF-8"));
        crc32.update(consumerSecretBytes, 0, consumerSecretBytes.length);
        final long value = crc32.getValue();
        crc32.reset();
        for (final String keySecret : keySecrets) {
            if (Long.parseLong(keySecret, 16) == value) return true;
        }
        return false;
    }

    public static String getOfficialKeyName(final Context context, final String consumerKey,
                                            final String consumerSecret) {
        if (context == null || consumerKey == null || consumerSecret == null) return null;
        final String[] keySecrets = context.getResources().getStringArray(R.array.values_official_consumer_secret_crc32);
        final String[] keyNames = context.getResources().getStringArray(R.array.names_official_consumer_secret);
        final CRC32 crc32 = new CRC32();
        final byte[] consumerSecretBytes = consumerSecret.getBytes(Charset.forName("UTF-8"));
        crc32.update(consumerSecretBytes, 0, consumerSecretBytes.length);
        final long value = crc32.getValue();
        crc32.reset();
        for (int i = 0, j = keySecrets.length; i < j; i++) {
            if (Long.parseLong(keySecrets[i], 16) == value) return keyNames[i];
        }
        return null;
    }

    @NonNull
    public static ConsumerKeyType getOfficialKeyType(final Context context, final String consumerKey,
                                                     final String consumerSecret) {
        if (context == null || consumerKey == null || consumerSecret == null) {
            return ConsumerKeyType.UNKNOWN;
        }
        final String[] keySecrets = context.getResources().getStringArray(R.array.values_official_consumer_secret_crc32);
        final String[] keyNames = context.getResources().getStringArray(R.array.types_official_consumer_secret);
        final CRC32 crc32 = new CRC32();
        final byte[] consumerSecretBytes = consumerSecret.getBytes(Charset.forName("UTF-8"));
        crc32.update(consumerSecretBytes, 0, consumerSecretBytes.length);
        final long value = crc32.getValue();
        crc32.reset();
        for (int i = 0, j = keySecrets.length; i < j; i++) {
            if (Long.parseLong(keySecrets[i], 16) == value) {
                return ConsumerKeyType.parse(keyNames[i]);
            }
        }
        return ConsumerKeyType.UNKNOWN;
    }

    public static String unescapeTwitterStatusText(final String str) {
        if (str == null) return null;
        return str.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
    }

    public static <T extends List<Status>> T getStatusesWithQuoteData(Twitter twitter, @NonNull T list) throws TwitterException {
        LongSparseMap<Status> quotes = new LongSparseMap<>();
        // Phase 1: collect all statuses contains a status link, and put it in the map
        for (Status status : list) {
            if (status.isQuote()) continue;
            final UrlEntity[] entities = status.getUrlEntities();
            if (entities == null || entities.length <= 0) continue;
            // Seems Twitter will find last status link for quote target, so we search backward
            for (int i = entities.length - 1; i >= 0; i--) {
                final Matcher m = PATTERN_TWITTER_STATUS_LINK.matcher(entities[i].getExpandedUrl());
                if (!m.matches()) continue;
                final long quoteId = ParseUtils.parseLong(m.group(3), -1);
                if (quoteId > 0) {
                    quotes.put(quoteId, status);
                }
                break;
            }
        }
        // Phase 2: look up quoted tweets. Each lookup can fetch up to 100 tweets, so we split quote
        // ids into batches
        final long[] quoteIds = quotes.keys();
        for (int currentBulkIdx = 0, totalLength = quoteIds.length; currentBulkIdx < totalLength; currentBulkIdx += TWITTER_BULK_QUERY_COUNT) {
            final int currentBulkCount = Math.min(totalLength, currentBulkIdx + TWITTER_BULK_QUERY_COUNT) - currentBulkIdx;
            final long[] ids = new long[currentBulkCount];
            System.arraycopy(quoteIds, currentBulkIdx, ids, 0, currentBulkCount);
            // Lookup quoted statuses, then set each status into original status
            for (Status quoted : twitter.lookupStatuses(ids)) {
                final Set<Status> orig = quotes.get(quoted.getId());
                // This set shouldn't be null here, add null check to make inspector happy.
                if (orig == null) continue;
                for (Status status : orig) {
                    StatusImpl.setQuotedStatus(status, quoted);
                }
            }
        }
        return list;
    }

    public static String getMediaUrl(MediaEntity entity) {
        return TextUtils.isEmpty(entity.getMediaUrlHttps()) ? entity.getMediaUrl() : entity.getMediaUrlHttps();
    }

    public static String getProfileImageUrl(@Nullable User user) {
        if (user == null) return null;
        return TextUtils.isEmpty(user.getProfileImageUrlHttps()) ? user.getProfileImageUrl() : user.getProfileImageUrlHttps();
    }

    private static void parseEntities(final HtmlBuilder builder, final EntitySupport entities) {
        // Format media.
        final MediaEntity[] mediaEntities = entities.getMediaEntities();
        if (mediaEntities != null) {
            for (final MediaEntity mediaEntity : mediaEntities) {
                final int start = mediaEntity.getStart(), end = mediaEntity.getEnd();
                final String mediaUrl = TwitterContentUtils.getMediaUrl(mediaEntity);
                if (mediaUrl != null && start >= 0 && end >= 0) {
                    builder.addLink(mediaUrl, mediaEntity.getDisplayUrl(), start, end);
                }
            }
        }
        final UrlEntity[] urlEntities = entities.getUrlEntities();
        if (urlEntities != null) {
            for (final UrlEntity urlEntity : urlEntities) {
                final int start = urlEntity.getStart(), end = urlEntity.getEnd();
                final String expandedUrl = urlEntity.getExpandedUrl();
                if (expandedUrl != null && start >= 0 && end >= 0) {
                    builder.addLink(expandedUrl, urlEntity.getDisplayUrl(), start, end);
                }
            }
        }
    }

}
