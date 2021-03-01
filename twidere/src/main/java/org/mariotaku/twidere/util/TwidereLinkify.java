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

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.URLSpan;

import com.twitter.twittertext.Extractor;
import com.twitter.twittertext.Extractor.Entity;
import com.twitter.twittertext.Regex;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.text.AcctMentionSpan;
import org.mariotaku.twidere.text.HashtagSpan;
import org.mariotaku.twidere.text.TwidereURLSpan;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mariotaku.twidere.util.RegexUtils.matcherEnd;
import static org.mariotaku.twidere.util.RegexUtils.matcherGroup;
import static org.mariotaku.twidere.util.RegexUtils.matcherStart;

/**
 * Linkify take a piece of text and a regular expression and turns all of the
 * regex matches in the text into clickable links. This is particularly useful
 * for matching things like email addresses, web urls, etc. and making them
 * actionable. Alone with the pattern that is to be matched, a url scheme prefix
 * is also required. Any pattern match that does not begin with the supplied
 * scheme will have the scheme prepended to the matched text when the clickable
 * url is created. For instance, if you are matching web urls you would supply
 * the scheme <code>http://</code>. If the pattern matches example.com, which
 * does not have a url scheme prefix, the supplied scheme will be prepended to
 * create <code>http://example.com</code> when the clickable url link is
 * created.
 */

public final class TwidereLinkify implements Constants {

    public static final int LINK_TYPE_MENTION = 1;
    public static final int LINK_TYPE_HASHTAG = 2;
    public static final int LINK_TYPE_BANGTAG = 3;
    public static final int LINK_TYPE_ENTITY_URL = 4;
    public static final int LINK_TYPE_LINK_IN_TEXT = 5;
    public static final int LINK_TYPE_LIST = 6;
    public static final int LINK_TYPE_CASHTAG = 7;
    public static final int LINK_TYPE_USER_ID = 8;
    public static final int LINK_TYPE_USER_ACCT = 9;

    public static final int[] ALL_LINK_TYPES = new int[]{LINK_TYPE_ENTITY_URL, LINK_TYPE_LINK_IN_TEXT,
            LINK_TYPE_MENTION, LINK_TYPE_HASHTAG, LINK_TYPE_CASHTAG};

    public static final String AVAILABLE_URL_SCHEME_PREFIX = "(https?://)?";

    public static final String TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES = "(bigger|normal|mini|reasonably_small)";
    private static final String STRING_PATTERN_TWITTER_PROFILE_IMAGES_NO_SCHEME = "(twimg[\\d\\w\\-]+\\.akamaihd\\.net|[\\w\\d]+\\.twimg\\.com)/profile_images/([\\d\\w\\-_]+)/([\\d\\w\\-_]+)_"
            + TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES + "(\\.?" + "(png|jpeg|jpg|gif|bmp)" + ")?";
    private static final String STRING_PATTERN_TWITTER_PROFILE_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
            + STRING_PATTERN_TWITTER_PROFILE_IMAGES_NO_SCHEME;

    public static final Pattern PATTERN_TWITTER_PROFILE_IMAGES = Pattern.compile(STRING_PATTERN_TWITTER_PROFILE_IMAGES,
            Pattern.CASE_INSENSITIVE);
    public static final int GROUP_ID_TWITTER_LIST_SCREEN_NAME = 4;
    public static final int GROUP_ID_TWITTER_LIST_LIST_NAME = 5;
    private static final String STRING_PATTERN_TWITTER_LIST_NO_SCHEME = "((mobile|www)\\.)?twitter\\.com/(?:#!/)?(\\w+)/lists/(.+)/?";
    private static final String STRING_PATTERN_TWITTER_LIST = AVAILABLE_URL_SCHEME_PREFIX
            + STRING_PATTERN_TWITTER_LIST_NO_SCHEME;
    public static final Pattern PATTERN_TWITTER_LIST = Pattern.compile(STRING_PATTERN_TWITTER_LIST,
            Pattern.CASE_INSENSITIVE);
    private final OnLinkClickListener mOnLinkClickListener;
    private final Extractor mExtractor = new Extractor();
    private int mHighlightOption;

    public TwidereLinkify(final OnLinkClickListener listener) {
        this(listener, VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH);
    }

    public TwidereLinkify(final OnLinkClickListener listener, final int highlightOption) {
        mOnLinkClickListener = listener;
        setHighlightOption(highlightOption);
    }

    public void applyAllLinks(@Nullable Spannable text, @Nullable final UserKey accountKey,
                              final long extraId, final boolean sensitive,
                              final boolean skipLinksInText) {
        applyAllLinks(text, mOnLinkClickListener, accountKey, extraId, sensitive,
                mHighlightOption, skipLinksInText);
    }

    public void applyAllLinks(@Nullable Spannable text, @Nullable final UserKey accountKey,
            final boolean sensitive, final boolean skipLinksInText) {
        applyAllLinks(text, mOnLinkClickListener, accountKey, -1, sensitive, mHighlightOption,
                skipLinksInText);
    }

    public void applyAllLinks(@Nullable Spannable text, @Nullable final UserKey accountKey,
            final long extraId, final boolean sensitive, final int highlightOption,
            final boolean skipLinksInText) {
        applyAllLinks(text, mOnLinkClickListener, accountKey, extraId, sensitive, highlightOption,
                skipLinksInText);
    }

    public void applyAllLinks(@Nullable final Spannable text, final OnLinkClickListener listener,
            @Nullable final UserKey accountKey, final long extraId, final boolean sensitive,
            final int highlightOption, boolean skipLinksInText) {
        if (text == null) return;
        for (final int type : ALL_LINK_TYPES) {
            if (type == LINK_TYPE_LINK_IN_TEXT && skipLinksInText) continue;
            addLinks(text, accountKey, extraId, type, sensitive, listener, highlightOption);
        }
    }

    public void setHighlightOption(@HighlightStyle final int style) {
        mHighlightOption = style;
    }

    private boolean addCashtagLinks(final Spannable spannable, @Nullable final UserKey accountKey,
            final long extraId, final OnLinkClickListener listener, final int highlightOption) {
        boolean hasMatches = false;
        for (final Entity entity : mExtractor.extractCashtagsWithIndices(spannable.toString())) {
            final int start = entity.getStart();
            final int end = entity.getEnd();
            applyLink(entity.getValue(), null, start, end, spannable, accountKey, extraId,
                    LINK_TYPE_CASHTAG, false, highlightOption, listener);
            hasMatches = true;
        }
        return hasMatches;
    }

    private boolean addHashtagLinks(final Spannable spannable, @Nullable final UserKey accountKey,
            final long extraId, final OnLinkClickListener listener, final int highlightOption) {
        boolean hasMatches = false;
        for (final Entity entity : mExtractor.extractHashtagsWithIndices(spannable.toString())) {
            final int start = entity.getStart();
            final int end = entity.getEnd();
            applyLink(entity.getValue(), null, start, end, spannable, accountKey, extraId,
                    LINK_TYPE_HASHTAG, false, highlightOption, listener);
            hasMatches = true;
        }
        return hasMatches;
    }

    /**
     * Applies a regex to the text of a TextView turning the matches into links.
     */
    private void addLinks(final Spannable string, @Nullable final UserKey accountKey,
            final long extraId, final int type, final boolean sensitive,
            final OnLinkClickListener listener, final int highlightOption) {
        switch (type) {
            case LINK_TYPE_MENTION: {
                addMentionOrListLinks(string, accountKey, extraId, highlightOption, listener);
                break;
            }
            case LINK_TYPE_HASHTAG: {
                addHashtagLinks(string, accountKey, extraId, listener, highlightOption);
                break;
            }
            case LINK_TYPE_ENTITY_URL: {
                final int length = string.length();
                final URLSpan[] spans = string.getSpans(0, length, URLSpan.class);
                for (final URLSpan span : spans) {
                    int start = string.getSpanStart(span), end = string.getSpanEnd(span);
                    if (span instanceof TwidereURLSpan || start < 0 || end > length || start > end) {
                        continue;
                    }
                    string.removeSpan(span);
                    String url = span.getURL();
                    if (url == null) break;
                    int linkType = type;
                    if (span instanceof AcctMentionSpan) {
                        linkType = LINK_TYPE_USER_ACCT;
                    } else if (span instanceof HashtagSpan) {
                        linkType = LINK_TYPE_HASHTAG;
                    } else if (accountKey != null && USER_TYPE_FANFOU_COM.equals(accountKey.getHost())) {
                        // Fix search path
                        if (url.startsWith("/")) {
                            url = "http://fanfou.com" + url;
                        }
                        if ("fanfou.com".equals(UriUtils.getAuthority(url)) && start > 0) {
                            // Process special case for fanfou
                            final char ch = string.charAt(start - 1);
                            // Extend selection
                            if (isAtSymbol(ch)) {
                                start = start - 1;
                            } else if (isHashSymbol(ch) && end < length && isHashSymbol(string.charAt(end))) {
                                start = start - 1;
                                end = end + 1;
                            }
                        }
                    }
                    applyLink(url, String.valueOf(string.subSequence(start, end)), start, end,
                            string, accountKey, extraId, linkType, sensitive, highlightOption,
                            listener);
                }
                break;
            }
            case LINK_TYPE_LINK_IN_TEXT: {
                final List<Extractor.Entity> urls = mExtractor.extractURLsWithIndices(ParseUtils.parseString(string));
                for (final Extractor.Entity entity : urls) {
                    final int start = entity.getStart(), end = entity.getEnd();
                    if (entity.getType() != Extractor.Entity.Type.URL
                            || string.getSpans(start, end, URLSpan.class).length > 0) {
                        continue;
                    }
                    applyLink(entity.getValue(), null, start, end, string, accountKey, extraId,
                            LINK_TYPE_LINK_IN_TEXT, sensitive, highlightOption, listener);
                }
                break;
            }
            case LINK_TYPE_CASHTAG: {
                addCashtagLinks(string, accountKey, extraId, listener, highlightOption);
                break;
            }
        }
    }

    static boolean isAtSymbol(char ch) {
        return ch == '@' || ch == '\uff20';
    }

    static boolean isHashSymbol(char ch) {
        return ch == '#' || ch == '\uff03';
    }

    private boolean addMentionOrListLinks(final Spannable spannable, final UserKey accountKey,
                                          final long extraId, final int highlightOption, final OnLinkClickListener listener) {
        boolean hasMatches = false;
        // Extract lists from status text
        final Matcher matcher = Regex.VALID_MENTION_OR_LIST.matcher(spannable);
        while (matcher.find()) {
            final int start = matcherStart(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_AT);
            final int usernameEnd = matcherEnd(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME);
            final int listStart = matcherStart(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
            final int listEnd = matcherEnd(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
            final String username = matcherGroup(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME);
            final String list = matcherGroup(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
            if (username != null) {
                applyLink(username, null, start, usernameEnd, spannable, accountKey, extraId,
                        LINK_TYPE_MENTION, false, highlightOption, listener);
                if (listStart >= 0 && listEnd >= 0 && list != null) {
                    StringBuilder sb = new StringBuilder(username);
                    if (!list.startsWith("/")) {
                        sb.append("/");
                    }
                    sb.append(list);
                    applyLink(sb.toString(), null, listStart, listEnd, spannable, accountKey, extraId,
                            LINK_TYPE_LIST, false, highlightOption, listener);
                }
                hasMatches = true;
            }
        }
        // Extract lists from twitter.com links.
        final URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (final URLSpan span : spans) {
            final Matcher m = PATTERN_TWITTER_LIST.matcher(span.getURL());
            if (m.matches()) {
                final int start = spannable.getSpanStart(span);
                final int end = spannable.getSpanEnd(span);
                final String screenName = matcherGroup(m, GROUP_ID_TWITTER_LIST_SCREEN_NAME);
                final String listName = matcherGroup(m, GROUP_ID_TWITTER_LIST_LIST_NAME);
                spannable.removeSpan(span);
                applyLink(screenName + "/" + listName, null, start, end, spannable, accountKey,
                        extraId, LINK_TYPE_LIST, false, highlightOption, listener);
                hasMatches = true;
            }
        }
        return hasMatches;
    }

    private void applyLink(@NonNull final String url, @Nullable final String orig, final int start, final int end,
                           final Spannable text, @Nullable final UserKey accountKey, final long extraId, final int type, final boolean sensitive,
                           final int highlightOption, final OnLinkClickListener listener) {
        final TwidereURLSpan span = new TwidereURLSpan(url, orig, accountKey, extraId, type, sensitive,
                highlightOption, start, end, listener);
        text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @IntDef({VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE, VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT,
            VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE, VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HighlightStyle {

    }

    public interface OnLinkClickListener {
        boolean onLinkClick(@NonNull String link, @Nullable String orig, @Nullable UserKey accountKey, long extraId, int type,
                            boolean sensitive, int start, int end);
    }
}
