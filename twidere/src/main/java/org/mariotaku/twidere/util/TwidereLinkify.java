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

import android.support.annotation.IntDef;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.widget.TextView;

import com.twitter.Extractor;
import com.twitter.Extractor.Entity;
import com.twitter.Regex;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.text.TwidereURLSpan;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mariotaku.twidere.util.MediaPreviewUtils.AVAILABLE_IMAGE_SHUFFIX;
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
    public static final int LINK_TYPE_LINK = 4;
    public static final int LINK_TYPE_LIST = 6;
    public static final int LINK_TYPE_CASHTAG = 7;
    public static final int LINK_TYPE_USER_ID = 8;
    public static final int LINK_TYPE_STATUS = 9;

    public static final int[] ALL_LINK_TYPES = new int[]{LINK_TYPE_LINK, LINK_TYPE_MENTION, LINK_TYPE_HASHTAG,
            LINK_TYPE_STATUS, LINK_TYPE_CASHTAG};

    public static final String AVAILABLE_URL_SCHEME_PREFIX = "(https?://)?";

    public static final String TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES = "(bigger|normal|mini|reasonably_small)";
    private static final String STRING_PATTERN_TWITTER_PROFILE_IMAGES_NO_SCHEME = "(twimg[\\d\\w\\-]+\\.akamaihd\\.net|[\\w\\d]+\\.twimg\\.com)/profile_images/([\\d\\w\\-_]+)/([\\d\\w\\-_]+)_"
            + TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES + "(\\.?" + AVAILABLE_IMAGE_SHUFFIX + ")?";
    private static final String STRING_PATTERN_TWITTER_PROFILE_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
            + STRING_PATTERN_TWITTER_PROFILE_IMAGES_NO_SCHEME;

    public static final Pattern PATTERN_TWITTER_PROFILE_IMAGES = Pattern.compile(STRING_PATTERN_TWITTER_PROFILE_IMAGES,
            Pattern.CASE_INSENSITIVE);
    public static final int GROUP_ID_TWITTER_STATUS_SCREEN_NAME = 4;
    public static final int GROUP_ID_TWITTER_STATUS_STATUS_ID = 6;
    public static final int GROUP_ID_TWITTER_LIST_SCREEN_NAME = 4;
    public static final int GROUP_ID_TWITTER_LIST_LIST_NAME = 5;
    private static final String STRING_PATTERN_TWITTER_STATUS_NO_SCHEME = "((mobile|www)\\.)?twitter\\.com/(?:#!/)?(\\w+)/status(es)?/(\\d+)(/photo/\\d)?/?";
    private static final String STRING_PATTERN_TWITTER_STATUS = AVAILABLE_URL_SCHEME_PREFIX
            + STRING_PATTERN_TWITTER_STATUS_NO_SCHEME;
    public static final Pattern PATTERN_TWITTER_STATUS = Pattern.compile(STRING_PATTERN_TWITTER_STATUS,
            Pattern.CASE_INSENSITIVE);
    private static final String STRING_PATTERN_TWITTER_LIST_NO_SCHEME = "((mobile|www)\\.)?twitter\\.com/(?:#!/)?(\\w+)/lists/(.+)/?";
    private static final String STRING_PATTERN_TWITTER_LIST = AVAILABLE_URL_SCHEME_PREFIX
            + STRING_PATTERN_TWITTER_LIST_NO_SCHEME;
    public static final Pattern PATTERN_TWITTER_LIST = Pattern.compile(STRING_PATTERN_TWITTER_LIST,
            Pattern.CASE_INSENSITIVE);
    private final OnLinkClickListener mOnLinkClickListener;
    private final Extractor mExtractor = new Extractor();
    private final boolean mAddMovementMethod;
    private int mHighlightOption;

    public TwidereLinkify(final OnLinkClickListener listener) {
        this(listener, VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH);
    }

    public TwidereLinkify(final OnLinkClickListener listener, final int highlightOption) {
        this(listener, highlightOption, true);
    }

    public TwidereLinkify(final OnLinkClickListener listener, boolean addMovementMethod) {
        this(listener, VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH, addMovementMethod);

    }

    public TwidereLinkify(final OnLinkClickListener listener, final int highlightOption, boolean addMovementMethod) {
        mOnLinkClickListener = listener;
        mAddMovementMethod = addMovementMethod;
        setHighlightOption(highlightOption);
    }

    public final void applyAllLinks(final TextView view, final long accountId, final long extraId, final boolean sensitive) {
        applyAllLinks(view, mOnLinkClickListener, accountId, extraId, sensitive, mHighlightOption);
    }

    public final void applyAllLinks(final TextView view, final long accountId, final boolean sensitive) {
        applyAllLinks(view, mOnLinkClickListener, accountId, -1, sensitive, mHighlightOption);
    }

    public final void applyAllLinks(final TextView view, final long accountId, final long extraId,
                                    final boolean sensitive, final int highlightOption) {
        applyAllLinks(view, mOnLinkClickListener, accountId, extraId, sensitive, highlightOption);
    }

    public final void applyAllLinks(final TextView view, final OnLinkClickListener listener, final long accountId, final long extraId,
                                    final boolean sensitive, final int highlightOption) {
        view.setMovementMethod(LinkMovementMethod.getInstance());
        final SpannableString string = SpannableString.valueOf(view.getText());
        for (final int type : ALL_LINK_TYPES) {
            addLinks(string, accountId, extraId, type, sensitive, listener, highlightOption);
        }
        view.setText(string);
        if (mAddMovementMethod) {
            addLinkMovementMethod(view);
        }
    }

    public final void applyUserProfileLink(final TextView view, final long accountId, final long extraId,
                                           final long userId, final String screenName) {
        applyUserProfileLink(view, accountId, extraId, userId, screenName, mHighlightOption);
    }

    public final void applyUserProfileLink(final TextView view, final long accountId, final long extraId,
                                           final long userId, final String screenName, final int highlightOption) {
        applyUserProfileLink(view, accountId, extraId, userId, screenName, highlightOption, mOnLinkClickListener);
    }

    public final void applyUserProfileLink(final TextView view, final long accountId, final long extraId,
                                           final long userId, final String screenName, final int highlightOption, final OnLinkClickListener listener) {
        view.setMovementMethod(LinkMovementMethod.getInstance());
        final SpannableString string = SpannableString.valueOf(view.getText());
        final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
        for (final URLSpan span : spans) {
            string.removeSpan(span);
        }
        if (userId > 0) {
            applyLink(String.valueOf(userId), 0, string.length(), string, accountId, extraId,
                    LINK_TYPE_USER_ID, false, highlightOption, listener);
        } else if (screenName != null) {
            applyLink(screenName, 0, string.length(), string, accountId, extraId,
                    LINK_TYPE_MENTION, false, highlightOption, listener);
        }
        view.setText(string);
        if (mAddMovementMethod) {
            addLinkMovementMethod(view);
        }
    }

    public void setHighlightOption(@HighlightStyle final int style) {
        mHighlightOption = style;
    }

    private boolean addCashtagLinks(final Spannable spannable, final long accountId, final long extraId,
                                    final OnLinkClickListener listener, final int highlightOption) {
        boolean hasMatches = false;
        for (final Entity entity : mExtractor.extractCashtagsWithIndices(spannable.toString())) {
            final int start = entity.getStart();
            final int end = entity.getEnd();
            applyLink(entity.getValue(), start, end, spannable, accountId, extraId, LINK_TYPE_CASHTAG,
                    false, highlightOption, listener);
            hasMatches = true;
        }
        return hasMatches;
    }

    private boolean addHashtagLinks(final Spannable spannable, final long accountId, final long extraId,
                                    final OnLinkClickListener listener, final int highlightOption) {
        boolean hasMatches = false;
        for (final Entity entity : mExtractor.extractHashtagsWithIndices(spannable.toString())) {
            final int start = entity.getStart();
            final int end = entity.getEnd();
            applyLink(entity.getValue(), start, end, spannable, accountId, extraId, LINK_TYPE_HASHTAG,
                    false, highlightOption, listener);
            hasMatches = true;
        }
        return hasMatches;
    }

    private static void addLinkMovementMethod(final TextView t) {
        final MovementMethod m = t.getMovementMethod();
        if (m == null || !(m instanceof LinkMovementMethod)) {
            if (t.getLinksClickable()) {
                t.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    /**
     * Applies a regex to the text of a TextView turning the matches into links.
     * If links are found then UrlSpans are applied to the link text match
     * areas, and the movement method for the text is changed to
     * LinkMovementMethod.
     *
     * @param highlightOption
     * @param listener
     */
    private void addLinks(final SpannableString string, final long accountId, final long extraId, final int type,
                          final boolean sensitive, final OnLinkClickListener listener, final int highlightOption) {
        switch (type) {
            case LINK_TYPE_MENTION: {
                addMentionOrListLinks(string, accountId, extraId, highlightOption, listener);
                break;
            }
            case LINK_TYPE_HASHTAG: {
                addHashtagLinks(string, accountId, extraId, listener, highlightOption);
                break;
            }
            case LINK_TYPE_LINK: {
                final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
                for (final URLSpan span : spans) {
                    final int start = string.getSpanStart(span);
                    final int end = string.getSpanEnd(span);
                    if (start < 0 || end > string.length() || start > end) {
                        continue;
                    }
                    string.removeSpan(span);
                    applyLink(span.getURL(), start, end, string, accountId, extraId, LINK_TYPE_LINK, sensitive, highlightOption, listener);
                }
                final List<Extractor.Entity> urls = mExtractor.extractURLsWithIndices(ParseUtils.parseString(string));
                for (final Extractor.Entity entity : urls) {
                    final int start = entity.getStart(), end = entity.getEnd();
                    if (entity.getType() != Extractor.Entity.Type.URL
                            || string.getSpans(start, end, URLSpan.class).length > 0) {
                        continue;
                    }
                    applyLink(entity.getValue(), start, end, string, accountId, extraId, LINK_TYPE_LINK, sensitive, highlightOption, listener);
                }
                break;
            }
            case LINK_TYPE_STATUS: {
                final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
                for (final URLSpan span : spans) {
                    final Matcher matcher = PATTERN_TWITTER_STATUS.matcher(span.getURL());
                    if (matcher.matches()) {
                        final int start = string.getSpanStart(span);
                        final int end = string.getSpanEnd(span);
                        final String url = matcherGroup(matcher, GROUP_ID_TWITTER_STATUS_STATUS_ID);
                        string.removeSpan(span);
                        applyLink(url, start, end, string, accountId, extraId, LINK_TYPE_STATUS, sensitive, highlightOption, listener);
                    }
                }
                break;
            }
            case LINK_TYPE_CASHTAG: {
                addCashtagLinks(string, accountId, extraId, listener, highlightOption);
                break;
            }
        }
    }

    private boolean addMentionOrListLinks(final Spannable spannable, final long accountId,
                                          final long extraId, final int highlightOption, final OnLinkClickListener listener) {
        boolean hasMatches = false;
        // Extract lists from status text
        final Matcher matcher = Regex.VALID_MENTION_OR_LIST.matcher(spannable);
        while (matcher.find()) {
            final int start = matcherStart(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_AT);
            final int username_end = matcherEnd(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME);
            final int listStart = matcherStart(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
            final int listEnd = matcherEnd(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
            final String username = matcherGroup(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME);
            final String list = matcherGroup(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
            applyLink(username, start, username_end, spannable, accountId, extraId, LINK_TYPE_MENTION,
                    false, highlightOption, listener);
            if (listStart >= 0 && listEnd >= 0) {
                applyLink(String.format("%s/%s", username, list.substring(list.startsWith("/") ? 1 : 0)), listStart,
                        listEnd, spannable, accountId, extraId, LINK_TYPE_LIST, false, highlightOption, listener);
            }
            hasMatches = true;
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
                applyLink(screenName + "/" + listName, start, end, spannable, accountId, extraId,
                        LINK_TYPE_LIST, false, highlightOption, listener);
                hasMatches = true;
            }
        }
        return hasMatches;
    }

    private void applyLink(final String url, final String orig, final int start, final int end,
                           final Spannable text, final long accountId, final long extraId, final int type, final boolean sensitive,
                           final int highlightOption, final OnLinkClickListener listener) {
        final TwidereURLSpan span = new TwidereURLSpan(url, orig, accountId, extraId, type, sensitive,
                highlightOption, start, end, listener);
        text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void applyLink(final String url, final int start, final int end, final Spannable text,
                           final long accountId, final long extraId, final int type, final boolean sensitive,
                           final int highlightOption, final OnLinkClickListener listener) {
        applyLink(url, null, start, end, text, accountId, extraId, type, sensitive, highlightOption,
                listener);
    }

    @IntDef({VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE, VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT,
            VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE, VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface HighlightStyle {

    }

    public interface OnLinkClickListener {
        void onLinkClick(String link, String orig, long accountId, long extraId, int type,
                         boolean sensitive, int start, int end);
    }
}
