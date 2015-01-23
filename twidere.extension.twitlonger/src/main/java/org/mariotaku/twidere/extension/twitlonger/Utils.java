package org.mariotaku.twidere.extension.twitlonger;

import java.util.regex.Pattern;

import android.text.TextUtils;

public class Utils {

	private static final Pattern PATTERN_TWITLONGER = Pattern.compile(
			"(https?:\\/\\/)(tl\\.gd|www.twitlonger.com\\/show)\\/([\\w\\d]+)", Pattern.CASE_INSENSITIVE);
	private static final int GROUP_TWITLONGER_ID = 3;

	public static String getTwitLongerId(String text) {
		if (TextUtils.isEmpty(text)) return null;
		return PATTERN_TWITLONGER.matcher(text).group(GROUP_TWITLONGER_ID);
	}

	public static String getTwitLongerUrl(String text) {
		if (TextUtils.isEmpty(text)) return null;
		return PATTERN_TWITLONGER.matcher(text).group();
	}
	
}
