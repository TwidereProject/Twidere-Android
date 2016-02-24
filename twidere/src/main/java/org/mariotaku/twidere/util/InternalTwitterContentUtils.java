package org.mariotaku.twidere.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.DirectMessage;
import org.mariotaku.twidere.api.twitter.model.EntitySupport;
import org.mariotaku.twidere.api.twitter.model.MediaEntity;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.UrlEntity;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.util.collection.LongSparseMap;
import org.mariotaku.twidere.util.media.preview.PreviewMediaExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mariotaku on 16/2/24.
 */
public class InternalTwitterContentUtils {
    private static final Pattern PATTERN_TWITTER_STATUS_LINK = Pattern.compile("https?://twitter\\.com/(?:#!/)?(\\w+)/status(es)?/(\\d+)");
    private static final CharSequenceTranslator UNESCAPE_TWITTER_RAW_TEXT = new LookupTranslator(EntityArrays.BASIC_UNESCAPE());
    private static final CharSequenceTranslator ESCAPE_TWITTER_RAW_TEXT = new LookupTranslator(EntityArrays.BASIC_ESCAPE());

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
                final long def = -1;
                final long quoteId = NumberUtils.toLong(m.group(3), def);
                if (quoteId > 0) {
                    quotes.put(quoteId, status);
                }
                break;
            }
        }
        // Phase 2: look up quoted tweets. Each lookup can fetch up to 100 tweets, so we split quote
        // ids into batches
        final long[] quoteIds = quotes.keys();
        for (int currentBulkIdx = 0, totalLength = quoteIds.length; currentBulkIdx < totalLength; currentBulkIdx += TwitterContentUtils.TWITTER_BULK_QUERY_COUNT) {
            final int currentBulkCount = Math.min(totalLength, currentBulkIdx + TwitterContentUtils.TWITTER_BULK_QUERY_COUNT) - currentBulkIdx;
            final long[] ids = new long[currentBulkCount];
            System.arraycopy(quoteIds, currentBulkIdx, ids, 0, currentBulkCount);
            // Lookup quoted statuses, then set each status into original status
            for (Status quoted : twitter.lookupStatuses(ids)) {
                final Set<Status> orig = quotes.get(quoted.getId());
                // This set shouldn't be null here, add null check to make inspector happy.
                if (orig == null) continue;
                for (Status status : orig) {
                    Status.setQuotedStatus(status, quoted);
                }
            }
        }
        return list;
    }

    public static boolean isFiltered(final SQLiteDatabase database, final long user_id, final String text_plain,
                                     final String text_html, final String source, final long retweeted_by_id, final long quotedUserId) {
        return isFiltered(database, user_id, text_plain, text_html, source, retweeted_by_id, quotedUserId, true);
    }

    public static boolean isFiltered(final SQLiteDatabase database, final long userId,
                                     final String textPlain, final String textHtml, final String source,
                                     final long retweetedById, final long quotedUserId, final boolean filterRts) {
        if (database == null) return false;
        if (textPlain == null && textHtml == null && userId <= 0 && source == null) return false;
        final StringBuilder builder = new StringBuilder();
        final List<String> selection_args = new ArrayList<>();
        builder.append("SELECT NULL WHERE");
        if (textPlain != null) {
            selection_args.add(textPlain);
            builder.append("(SELECT 1 IN (SELECT ? LIKE '%'||" + TwidereDataStore.Filters.Keywords.TABLE_NAME + "." + TwidereDataStore.Filters.VALUE
                    + "||'%' FROM " + TwidereDataStore.Filters.Keywords.TABLE_NAME + "))");
        }
        if (textHtml != null) {
            if (!selection_args.isEmpty()) {
                builder.append(" OR ");
            }
            selection_args.add(textHtml);
            builder.append("(SELECT 1 IN (SELECT ? LIKE '%<a href=\"%'||" + TwidereDataStore.Filters.Links.TABLE_NAME + "."
                    + TwidereDataStore.Filters.VALUE + "||'%\">%' FROM " + TwidereDataStore.Filters.Links.TABLE_NAME + "))");
        }
        if (userId > 0) {
            if (!selection_args.isEmpty()) {
                builder.append(" OR ");
            }
            builder.append("(SELECT ").append(userId).append(" IN (SELECT ").append(TwidereDataStore.Filters.Users.USER_ID).append(" FROM ").append(TwidereDataStore.Filters.Users.TABLE_NAME).append("))");
        }
        if (retweetedById > 0) {
            if (!selection_args.isEmpty()) {
                builder.append(" OR ");
            }
            builder.append("(SELECT ").append(retweetedById).append(" IN (SELECT ").append(TwidereDataStore.Filters.Users.USER_ID).append(" FROM ").append(TwidereDataStore.Filters.Users.TABLE_NAME).append("))");
        }
        if (quotedUserId > 0) {
            if (!selection_args.isEmpty()) {
                builder.append(" OR ");
            }
            builder.append("(SELECT ").append(quotedUserId).append(" IN (SELECT ").append(TwidereDataStore.Filters.Users.USER_ID).append(" FROM ").append(TwidereDataStore.Filters.Users.TABLE_NAME).append("))");
        }
        if (source != null) {
            if (!selection_args.isEmpty()) {
                builder.append(" OR ");
            }
            selection_args.add(source);
            builder.append("(SELECT 1 IN (SELECT ? LIKE '%>'||" + TwidereDataStore.Filters.Sources.TABLE_NAME + "." + TwidereDataStore.Filters.VALUE
                    + "||'</a>%' FROM " + TwidereDataStore.Filters.Sources.TABLE_NAME + "))");
        }
        final Cursor cur = database.rawQuery(builder.toString(),
                selection_args.toArray(new String[selection_args.size()]));
        if (cur == null) return false;
        try {
            return cur.getCount() > 0;
        } finally {
            cur.close();
        }
    }

    public static boolean isFiltered(final SQLiteDatabase database, final ParcelableStatus status,
                                     final boolean filter_rts) {
        if (database == null || status == null) return false;
        return isFiltered(database, status.user_id, status.text_plain, status.text_html, status.source,
                status.retweeted_by_user_id, status.quoted_user_id, filter_rts);
    }

    @Nullable
    public static String getBestBannerUrl(@Nullable final String baseUrl, final int width) {
        if (baseUrl == null) return null;
        final String type = getBestBannerType(width);
        final String authority = PreviewMediaExtractor.getAuthority(baseUrl);
        return authority != null && authority.endsWith(".twimg.com") ? baseUrl + "/" + type : baseUrl;
    }

    public static String getBestBannerType(final int width) {
        if (width <= 320)
            return "mobile";
        else if (width <= 520)
            return "web";
        else if (width <= 626)
            return "ipad";
        else if (width <= 640)
            return "mobile_retina";
        else if (width <= 1040)
            return "web_retina";
        else
            return "ipad_retina";
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
        return HtmlEscapeHelper.toPlainText(builder.build());
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
        return builder.build();
    }

    public static String unescapeTwitterStatusText(final CharSequence text) {
        if (text == null) return null;
        return UNESCAPE_TWITTER_RAW_TEXT.translate(text);
    }

    public static String escapeTwitterStatusText(final CharSequence text) {
        if (text == null) return null;
        return ESCAPE_TWITTER_RAW_TEXT.translate(text);
    }

    public static String formatDirectMessageText(final DirectMessage message) {
        if (message == null) return null;
        final HtmlBuilder builder = new HtmlBuilder(message.getText(), false, true, true);
        parseEntities(builder, message);
        return builder.build();
    }

    public static String formatStatusText(final Status status) {
        if (status == null) return null;
        final HtmlBuilder builder = new HtmlBuilder(status.getText(), false, true, true);
        parseEntities(builder, status);
        return builder.build();
    }

    public static String getMediaUrl(MediaEntity entity) {
        return TextUtils.isEmpty(entity.getMediaUrlHttps()) ? entity.getMediaUrl() : entity.getMediaUrlHttps();
    }

    private static void parseEntities(final HtmlBuilder builder, final EntitySupport entities) {
        // Format media.
        final MediaEntity[] mediaEntities = entities.getMediaEntities();
        if (mediaEntities != null) {
            for (final MediaEntity mediaEntity : mediaEntities) {
                final int start = mediaEntity.getStart(), end = mediaEntity.getEnd();
                final String mediaUrl = getMediaUrl(mediaEntity);
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

    public static long getOriginalId(@NonNull ParcelableStatus status) {
        return status.is_retweet ? status.retweet_id : status.id;
    }
}
