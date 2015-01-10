package org.mariotaku.twidere.extension.streaming.util;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.TwidereSharedPreferences;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.HtmlBuilder;

import twitter4j.DirectMessage;
import twitter4j.EntitySupport;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import android.content.Context;
import android.database.Cursor;

public class Utils implements TwidereConstants {

	public static final String AVAILABLE_URL_SCHEME_PREFIX = "(https?:\\/\\/)?";

	public static final String AVAILABLE_IMAGE_SHUFFIX = "(png|jpeg|jpg|gif|bmp)";
	public static final String TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES = "(bigger|normal|mini)";
	private static final String STRING_PATTERN_TWITTER_PROFILE_IMAGES_NO_SCHEME = "(twimg[\\d\\w\\-]+\\.akamaihd\\.net|[\\w\\d]+\\.twimg\\.com)\\/profile_images\\/([\\d\\w\\-_]+)\\/([\\d\\w\\-_]+)_"
			+ TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES + "(\\.?" + AVAILABLE_IMAGE_SHUFFIX + ")?";

	private static final String STRING_PATTERN_TWITTER_PROFILE_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_TWITTER_PROFILE_IMAGES_NO_SCHEME;

	public static final Pattern PATTERN_TWITTER_PROFILE_IMAGES = Pattern.compile(STRING_PATTERN_TWITTER_PROFILE_IMAGES,
			Pattern.CASE_INSENSITIVE);

	private static Map<Long, String> sAccountScreenNames = new LinkedHashMap<Long, String>();

	public static String formatDirectMessageText(final DirectMessage message) {
		if (message == null) return null;
		final String text = message.getRawText();
		if (text == null) return null;
		final HtmlBuilder builder = new HtmlBuilder(text, false, true, true);
		parseEntities(builder, message);
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
		parseEntities(builder, status);
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

	public static String getAccountScreenName(final Context context, final long account_id) {
		if (context == null) return null;
		String screen_name = sAccountScreenNames.get(account_id);
		if (screen_name == null) {
			final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
					new String[] { Accounts.SCREEN_NAME }, Accounts.ACCOUNT_ID + " = " + account_id, null, null);
			if (cur == null) return screen_name;

			if (cur.getCount() > 0) {
				cur.moveToFirst();
				screen_name = cur.getString(cur.getColumnIndex(Accounts.SCREEN_NAME));
				sAccountScreenNames.put(account_id, screen_name);
			}
			cur.close();
		}
		return screen_name;
	}

	public static long[] getActivatedAccountIds(final Context context) {
		long[] accounts = new long[0];
		if (context == null) return accounts;
		final String[] cols = new String[] { Accounts.ACCOUNT_ID };
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, Accounts.IS_ACTIVATED + "=1",
				null, Accounts.ACCOUNT_ID);
		if (cur != null) {
			final int idx = cur.getColumnIndexOrThrow(Accounts.ACCOUNT_ID);
			cur.moveToFirst();
			accounts = new long[cur.getCount()];
			int i = 0;
			while (!cur.isAfterLast()) {
				accounts[i] = cur.getLong(idx);
				i++;
				cur.moveToNext();
			}
			cur.close();
		}
		return accounts;
	}

	public static String getBiggerTwitterProfileImage(final String url) {
		if (url == null) return null;
		if (PATTERN_TWITTER_PROFILE_IMAGES.matcher(url).matches())
			return replaceLast(url, "_" + TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES, "_bigger");
		return url;
	}

	public static String getInReplyToName(final Status status) {
		if (status == null) return null;
		final Status orig = status.isRetweet() ? status.getRetweetedStatus() : status;
		final long in_reply_to_user_id = status.getInReplyToUserId();
		final UserMentionEntity[] entities = status.getUserMentionEntities();
		if (entities == null) return orig.getInReplyToScreenName();
		for (final UserMentionEntity entity : entities) {
			if (in_reply_to_user_id == entity.getId()) return entity.getName();
		}
		return orig.getInReplyToScreenName();
	}

	public static String getNonEmptyString(final TwidereSharedPreferences pref, final String key, final String def) {
		if (pref == null) return def;
		final String val = pref.getString(key, def);
		return isEmpty(val) ? def : val;
	}

	public static final String matcherGroup(final Matcher matcher, final int group) {
		try {
			return matcher.group(group);
		} catch (final IllegalStateException e) {
			// Ignore.
		}
		return null;
	}

	public static String parseString(final Object object) {
		if (object == null) return null;
		return String.valueOf(object);
	}

	public static String replaceLast(final String text, final String regex, final String replacement) {
		if (text == null || regex == null || replacement == null) return text;
		return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
	}

	private static void parseEntities(final HtmlBuilder builder, final EntitySupport entities) {
		final URLEntity[] urls = entities.getURLEntities();
		// Format media.
		final MediaEntity[] medias = entities.getMediaEntities();
		if (medias != null) {
			for (final MediaEntity media : medias) {
				final URL media_url = media.getMediaURL();
				if (media_url != null) {
					builder.addLink(parseString(media_url), media.getDisplayURL(), media.getStart(), media.getEnd());
				}
			}
		}
		if (urls != null) {
			for (final URLEntity url : urls) {
				final URL expanded_url = url.getExpandedURL();
				if (expanded_url != null) {
					builder.addLink(parseString(expanded_url), url.getDisplayURL(), url.getStart(), url.getEnd());
				}
			}
		}
	}
}
