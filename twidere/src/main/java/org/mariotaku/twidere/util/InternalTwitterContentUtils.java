package org.mariotaku.twidere.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.DirectMessage;
import org.mariotaku.microblog.library.twitter.model.EntitySupport;
import org.mariotaku.microblog.library.twitter.model.ExtendedEntitySupport;
import org.mariotaku.microblog.library.twitter.model.MediaEntity;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.UrlEntity;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.restfu.http.MultiValueMap;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SpanItem;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mariotaku on 16/2/24.
 */
public class InternalTwitterContentUtils {

    public static final int TWITTER_BULK_QUERY_COUNT = 100;
    private static final Pattern PATTERN_TWITTER_STATUS_LINK = Pattern.compile("https?://twitter\\.com/(?:#!/)?(\\w+)/status(es)?/(\\d+)");
    private static final CharSequenceTranslator UNESCAPE_TWITTER_RAW_TEXT = new LookupTranslator(EntityArrays.BASIC_UNESCAPE());
    private static final CharSequenceTranslator ESCAPE_TWITTER_RAW_TEXT = new LookupTranslator(EntityArrays.BASIC_ESCAPE());

    private InternalTwitterContentUtils() {
    }

    public static <T extends List<? extends Status>> T getStatusesWithQuoteData(MicroBlog twitter, @NonNull T list) throws MicroBlogException {
        MultiValueMap<Status> quotes = new MultiValueMap<>();
        // Phase 1: collect all statuses contains a status link, and put it in the map
        for (Status status : list) {
            if (status.isQuote()) continue;
            final UrlEntity[] entities = status.getUrlEntities();
            if (entities == null || entities.length <= 0) continue;
            // Seems Twitter will find last status link for quote target, so we search backward
            for (int i = entities.length - 1; i >= 0; i--) {
                final Matcher m = PATTERN_TWITTER_STATUS_LINK.matcher(entities[i].getExpandedUrl());
                if (!m.matches()) continue;
                final String quoteId = m.group(3);
                if (!TextUtils.isEmpty(quoteId)) {
                    quotes.add(quoteId, status);
                }
                break;
            }
        }
        // Phase 2: look up quoted tweets. Each lookup can fetch up to 100 tweets, so we split quote
        // ids into batches
        final Set<String> keySet = quotes.keySet();
        final String[] quoteIds = keySet.toArray(new String[keySet.size()]);
        for (int currentBulkIdx = 0, totalLength = quoteIds.length; currentBulkIdx < totalLength;
             currentBulkIdx += TWITTER_BULK_QUERY_COUNT) {
            final int currentBulkCount = Math.min(totalLength, currentBulkIdx + TWITTER_BULK_QUERY_COUNT) - currentBulkIdx;
            final String[] ids = new String[currentBulkCount];
            System.arraycopy(quoteIds, currentBulkIdx, ids, 0, currentBulkCount);
            // Lookup quoted statuses, then set each status into original status
            for (Status quoted : twitter.lookupStatuses(ids)) {
                final List<Status> orig = quotes.get(quoted.getId());
                // This set shouldn't be null here, add null check to make inspector happy.
                if (orig == null) continue;
                for (Status status : orig) {
                    Status.setQuotedStatus(status, quoted);
                }
            }
        }
        return list;
    }

    public static boolean isFiltered(final SQLiteDatabase database, final UserKey userKey,
                                     final String textPlain, final String quotedTextPlain,
                                     final SpanItem[] spans, final SpanItem[] quotedSpans,
                                     final String source, final String quotedSource,
                                     final UserKey retweetedById, final UserKey quotedUserId) {
        return isFiltered(database, userKey, textPlain, quotedTextPlain, spans, quotedSpans, source,
                quotedSource, retweetedById, quotedUserId, true);
    }

    public static boolean isFiltered(final SQLiteDatabase database, final UserKey userKey,
                                     final String textPlain, final String quotedTextPlain,
                                     final SpanItem[] spans, final SpanItem[] quotedSpans,
                                     final String source, final String quotedSource,
                                     final UserKey retweetedByKey, final UserKey quotedUserKey,
                                     final boolean filterRts) {
        if (database == null) return false;
        if (textPlain == null && spans == null && userKey == null && source == null)
            return false;
        final StringBuilder builder = new StringBuilder();
        final List<String> selectionArgs = new ArrayList<>();
        builder.append("SELECT ");
        if (textPlain != null) {
            selectionArgs.add(textPlain);
            addTextPlainStatement(builder);
        }
        if (quotedTextPlain != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ");
            }
            selectionArgs.add(quotedTextPlain);
            addTextPlainStatement(builder);
        }
        if (spans != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ");
            }
            addSpansStatement(spans, builder, selectionArgs);
        }
        if (quotedSpans != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ");
            }
            addSpansStatement(quotedSpans, builder, selectionArgs);
        }
        if (userKey != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ");
            }
            selectionArgs.add(String.valueOf(userKey));
            createUserKeyStatement(builder);
        }
        if (retweetedByKey != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ");
            }
            selectionArgs.add(String.valueOf(retweetedByKey));
            createUserKeyStatement(builder);
        }
        if (quotedUserKey != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ");
            }
            selectionArgs.add(String.valueOf(quotedUserKey));
            createUserKeyStatement(builder);
        }
        if (source != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ");
            }
            selectionArgs.add(source);
            appendSourceStatement(builder);
        }
        if (quotedSource != null) {
            if (!selectionArgs.isEmpty()) {
                builder.append(" OR ");
            }
            selectionArgs.add(quotedSource);
            appendSourceStatement(builder);
        }
        final Cursor cur = database.rawQuery(builder.toString(),
                selectionArgs.toArray(new String[selectionArgs.size()]));
        if (cur == null) return false;
        try {
            return cur.moveToFirst() && cur.getInt(0) != 0;
        } finally {
            cur.close();
        }
    }

    static void addTextPlainStatement(StringBuilder builder) {
        builder.append("(SELECT 1 IN (SELECT ? LIKE '%'||").append(Filters.Keywords.TABLE_NAME).append(".").append(Filters.VALUE).append("||'%' FROM ").append(Filters.Keywords.TABLE_NAME).append("))");
    }

    static void addSpansStatement(SpanItem[] spans, StringBuilder builder, List<String> selectionArgs) {
        StringBuilder spansFlat = new StringBuilder();
        for (SpanItem span : spans) {
            spansFlat.append(span.link);
            spansFlat.append(' ');
        }
        selectionArgs.add(spansFlat.toString());
        builder.append("(SELECT 1 IN (SELECT ? LIKE '%'||")
                .append(Filters.Links.TABLE_NAME)
                .append(".")
                .append(Filters.VALUE)
                .append("||'%' FROM ")
                .append(Filters.Links.TABLE_NAME)
                .append("))");
    }

    static void createUserKeyStatement(StringBuilder builder) {
        builder.append("(SELECT ").append("?").append(" IN (SELECT ").append(Filters.Users.USER_KEY).append(" FROM ").append(Filters.Users.TABLE_NAME).append("))");
    }

    static void appendSourceStatement(StringBuilder builder) {
        builder.append("(SELECT 1 IN (SELECT ? LIKE '%>'||")
                .append(Filters.Sources.TABLE_NAME).append(".")
                .append(Filters.VALUE).append("||'</a>%' FROM ")
                .append(Filters.Sources.TABLE_NAME)
                .append("))");
    }

    public static boolean isFiltered(final SQLiteDatabase database, final ParcelableStatus status,
                                     final boolean filterRTs) {
        if (database == null || status == null) return false;
        return isFiltered(database, status.user_key, status.text_plain, status.quoted_text_plain,
                status.spans, status.quoted_spans, status.source, status.quoted_source,
                status.retweeted_by_user_key, status.quoted_user_key, filterRTs);
    }

    @Nullable
    public static String getBestBannerUrl(@Nullable final String baseUrl, final int width) {
        if (baseUrl == null) return null;
        final String type = getBestBannerType(width);
        final String authority = UriUtils.getAuthority(baseUrl);
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
        return builder.buildWithIndices().first;
    }

    public static Pair<String, SpanItem[]> formatUserDescription(final User user) {
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
        return builder.buildWithIndices();
    }

    public static String unescapeTwitterStatusText(final CharSequence text) {
        if (text == null) return null;
        return UNESCAPE_TWITTER_RAW_TEXT.translate(text);
    }

    public static Pair<String, SpanItem[]> formatDirectMessageText(final DirectMessage message) {
        if (message == null) return null;
        final HtmlBuilder builder = new HtmlBuilder(message.getText(), false, true, true);
        parseEntities(builder, message, null);
        return builder.buildWithIndices();
    }

    public static Pair<String, SpanItem[]> formatStatusTextWithIndices(final Status status) {
        if (status == null) return null;
        //TODO handle twitter video url

        String text = status.getFullText();
        CodePointArray source;
        int[] range = null;
        if (text == null) {
            text = status.getText();
            source = new CodePointArray(text);
        } else {
            range = status.getDisplayTextRange();
            if (range != null && range.length == 2) {
                source = new CodePointArray(text).subCodePointArray(range[0], range[1]);
            } else {
                range = null;
                source = new CodePointArray(text);
            }
        }
        final HtmlBuilder builder = new HtmlBuilder(source, false, true, false);
        parseEntities(builder, status, range);
        return builder.buildWithIndices();
    }

    public static String getMediaUrl(MediaEntity entity) {
        return TextUtils.isEmpty(entity.getMediaUrlHttps()) ? entity.getMediaUrl() : entity.getMediaUrlHttps();
    }

    private static void parseEntities(final HtmlBuilder builder, final EntitySupport entities, @Nullable int[] range) {
        // Format media.
        MediaEntity[] mediaEntities = null;
        if (entities instanceof ExtendedEntitySupport) {
            mediaEntities = ((ExtendedEntitySupport) entities).getExtendedMediaEntities();
        }
        if (mediaEntities == null) {
            mediaEntities = entities.getMediaEntities();
        }
        int[] startEnd = new int[2];
        if (mediaEntities != null) {
            for (final MediaEntity mediaEntity : mediaEntities) {
                final String mediaUrl = getMediaUrl(mediaEntity);
                if (mediaUrl != null && getStartEndForEntity(mediaEntity, range, startEnd)) {
                    builder.addLink(mediaEntity.getExpandedUrl(), mediaEntity.getDisplayUrl(),
                            startEnd[0], startEnd[1]);
                }
            }
        }
        final UrlEntity[] urlEntities = entities.getUrlEntities();
        if (urlEntities != null) {
            for (final UrlEntity urlEntity : urlEntities) {
                final String expandedUrl = urlEntity.getExpandedUrl();
                if (expandedUrl != null && getStartEndForEntity(urlEntity, range, startEnd)) {
                    builder.addLink(expandedUrl, urlEntity.getDisplayUrl(), startEnd[0],
                            startEnd[1]);
                }
            }
        }
    }

    private static boolean getStartEndForEntity(UrlEntity entity, @Nullable int[] range,
                                                @NonNull int[] out) {
        int offset = 0;
        if (range != null) {
            offset = range[0];
        }
        out[0] = entity.getStart() - offset;
        out[1] = entity.getEnd() - offset;
        if (range != null) {
            return out[0] >= 0 && out[1] <= range[1] && out[0] <= out[1];
        }
        return true;
    }

    public static String getOriginalId(@NonNull ParcelableStatus status) {
        return status.is_retweet ? status.retweet_id : status.id;
    }
}
