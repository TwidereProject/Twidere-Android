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

import org.mariotaku.twidere.common.R;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.zip.CRC32;

import twitter4j.DirectMessage;
import twitter4j.EntitySupport;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;

/**
 * Created by mariotaku on 15/1/11.
 */
public class TwitterContentUtils {
    public static String formatDirectMessageText(final DirectMessage message) {
        if (message == null) return null;
        final String text = message.getRawText();
        if (text == null) return null;
        final HtmlBuilder builder = new HtmlBuilder(text, false, true, true);
        TwitterContentUtils.parseEntities(builder, message);
        return builder.build().replace("\n", "<br/>");
    }

    public static String formatExpandedUserDescription(final User user) {
        if (user == null) return null;
        final String text = user.getDescription();
        if (text == null) return null;
        final HtmlBuilder builder = new HtmlBuilder(text, false, true, true);
        final URLEntity[] urls = user.getDescriptionEntities();
        if (urls != null) {
            for (final URLEntity url : urls) {
                final String expanded_url = ParseUtils.parseString(url.getExpandedURL());
                if (expanded_url != null) {
                    builder.addLink(expanded_url, expanded_url, url.getStart(), url.getEnd());
                }
            }
        }
        return toPlainText(builder.build().replace("\n", "<br/>"));
    }

    public static String formatStatusText(final Status status) {
        if (status == null) return null;
        final String text = status.getRawText();
        if (text == null) return null;
        final HtmlBuilder builder = new HtmlBuilder(text, false, true, true);
        TwitterContentUtils.parseEntities(builder, status);
        return builder.build().replace("\n", "<br/>");
    }

    public static String formatUserDescription(final User user) {
        if (user == null) return null;
        final String text = user.getDescription();
        if (text == null) return null;
        final HtmlBuilder builder = new HtmlBuilder(text, false, true, true);
        final URLEntity[] urls = user.getDescriptionEntities();
        if (urls != null) {
            for (final URLEntity url : urls) {
                final URL expanded_url = url.getExpandedURL();
                if (expanded_url != null) {
                    builder.addLink(ParseUtils.parseString(expanded_url), url.getDisplayURL(), url.getStart(),
                            url.getEnd());
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

    private static void parseEntities(final HtmlBuilder builder, final EntitySupport entities) {
        // Format media.
        final MediaEntity[] mediaEntities = entities.getMediaEntities();
        if (mediaEntities != null) {
            for (final MediaEntity mediaEntity : mediaEntities) {
                final int start = mediaEntity.getStart(), end = mediaEntity.getEnd();
                final URL mediaUrl = mediaEntity.getMediaURL();
                if (mediaUrl != null && start >= 0 && end >= 0) {
                    builder.addLink(ParseUtils.parseString(mediaUrl), mediaEntity.getDisplayURL(),
                            start, end);
                }
            }
        }
        final URLEntity[] urlEntities = entities.getURLEntities();
        if (urlEntities != null) {
            for (final URLEntity urlEntity : urlEntities) {
                final int start = urlEntity.getStart(), end = urlEntity.getEnd();
                final URL expandedUrl = urlEntity.getExpandedURL();
                if (expandedUrl != null && start >= 0 && end >= 0) {
                    builder.addLink(ParseUtils.parseString(expandedUrl), urlEntity.getDisplayURL(),
                            start, end);
                }
            }
        }
    }
}
