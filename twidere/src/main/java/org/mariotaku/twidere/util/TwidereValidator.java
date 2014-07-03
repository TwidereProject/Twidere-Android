package org.mariotaku.twidere.util;

import android.content.Context;
import android.text.TextUtils;

import com.twitter.Validator;

import org.mariotaku.twidere.Constants;

public class TwidereValidator implements Constants {

	private final int mMaxTweetLength;
	private final Validator mValidator;

	public TwidereValidator(final Context context) {
		final SharedPreferencesWrapper prefs = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		mValidator = new Validator();
		if (prefs != null) {
			final String textLimit = prefs.getString(KEY_STATUS_TEXT_LIMIT, null);
			mMaxTweetLength = ParseUtils.parseInt(textLimit, Validator.MAX_TWEET_LENGTH);
		} else {
			mMaxTweetLength = Validator.MAX_TWEET_LENGTH;
		}
	}

	public int getMaxTweetLength() {
		return mMaxTweetLength;
	}

	public int getTweetLength(final String text) {
		return mValidator.getTweetLength(text);
	}

	public boolean isValidTweet(final String text) {
		return !TextUtils.isEmpty(text) && getTweetLength(text) <= getMaxTweetLength();
	}

}
