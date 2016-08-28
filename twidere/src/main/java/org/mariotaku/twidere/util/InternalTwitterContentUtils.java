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
import org.mariotaku.commons.text.CodePointArray;
import org.mariotaku.microblog.library.twitter.model.DirectMessage;
import org.mariotaku.microblog.library.twitter.model.EntitySupport;
import org.mariotaku.microblog.library.twitter.model.ExtendedEntitySupport;
import org.mariotaku.microblog.library.twitter.model.MediaEntity;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.UrlEntity;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SpanItem;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;

import java.util.ArrayList;
import java.util.List;
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

    @Nullable
    public static Pair<String, SpanItem[]> formatUserDescription(@NonNull final User user) {
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

    @Nullable
    public static String unescapeTwitterStatusText(final CharSequence text) {
        if (text == null) return null;
        return UNESCAPE_TWITTER_RAW_TEXT.translate(text);
    }

    @NonNull
    public static Pair<String, SpanItem[]> formatDirectMessageText(@NonNull final DirectMessage message) {
        final HtmlBuilder builder = new HtmlBuilder(message.getText(), false, true, true);
        parseEntities(builder, message);
        return builder.buildWithIndices();
    }

    @NonNull
    public static StatusTextWithIndices formatStatusTextWithIndices(@NonNull final Status status) {
        //TODO handle twitter video url

        String text = status.getFullText();
        CodePointArray source;
        // Display text range
        int[] range = null;
        if (text == null) {
            text = status.getText();
            source = new CodePointArray(text);
        } else {
            range = status.getDisplayTextRange();
            source = new CodePointArray(text);
        }
        final HtmlBuilder builder = new HtmlBuilder(source, false, true, false);
        parseEntities(builder, status);
        StatusTextWithIndices textWithIndices = new StatusTextWithIndices();
        final Pair<String, SpanItem[]> pair = builder.buildWithIndices();
        textWithIndices.text = pair.first;
        textWithIndices.spans = pair.second;
        if (range != null && range.length == 2) {
            textWithIndices.range = new int[2];
            textWithIndices.range[0] = getResultRangeLength(source, pair.second, 0, range[0]);
            textWithIndices.range[1] = pair.first.length() - getResultRangeLength(source,
                    pair.second, range[1], source.length());
        }
        return textWithIndices;
    }

    /**
     * @param spans Ordered spans
     * @param start orig_start
     * @param end   orig_end
     */
    @NonNull
    static List<SpanItem> findByOrigRange(SpanItem[] spans, int start, int end) {
        List<SpanItem> result = new ArrayList<>();
        for (SpanItem span : spans) {
            if (span.orig_start >= start && span.orig_end <= end) {
                result.add(span);
            }
        }
        return result;
    }

    static int getResultRangeLength(CodePointArray source, SpanItem[] spans, int origStart, int origEnd) {
        List<SpanItem> findResult = findByOrigRange(spans, origStart, origEnd);
        if (findResult.isEmpty()) {
            return source.charCount(origStart, origEnd);
        }
        SpanItem first = findResult.get(0), last = findResult.get(findResult.size() - 1);
        if (first.orig_start == -1 || last.orig_end == -1)
            return source.charCount(origStart, origEnd);
        return source.charCount(origStart, first.orig_start) + (last.end - first.start)
                + source.charCount(first.orig_end, origEnd);
    }

    public static class StatusTextWithIndices {
        public String text;
        public SpanItem[] spans;
        @Nullable
        public int[] range;
    }

    public static String getMediaUrl(MediaEntity entity) {
        return TextUtils.isEmpty(entity.getMediaUrlHttps()) ? entity.getMediaUrl() : entity.getMediaUrlHttps();
    }

    private static void parseEntities(final HtmlBuilder builder, final EntitySupport entities) {
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
                if (mediaUrl != null && getStartEndForEntity(mediaEntity, startEnd)) {
                    builder.addLink(mediaEntity.getExpandedUrl(), mediaEntity.getDisplayUrl(),
                            startEnd[0], startEnd[1]);
                }
            }
        }
        final UrlEntity[] urlEntities = entities.getUrlEntities();
        if (urlEntities != null) {
            for (final UrlEntity urlEntity : urlEntities) {
                final String expandedUrl = urlEntity.getExpandedUrl();
                if (expandedUrl != null && getStartEndForEntity(urlEntity, startEnd)) {
                    builder.addLink(expandedUrl, urlEntity.getDisplayUrl(), startEnd[0],
                            startEnd[1]);
                }
            }
        }
    }

    private static boolean getStartEndForEntity(UrlEntity entity, @NonNull int[] out) {
        out[0] = entity.getStart();
        out[1] = entity.getEnd();
        return true;
    }

    public static String getOriginalId(@NonNull ParcelableStatus status) {
        return status.is_retweet ? status.retweet_id : status.id;
    }
}
