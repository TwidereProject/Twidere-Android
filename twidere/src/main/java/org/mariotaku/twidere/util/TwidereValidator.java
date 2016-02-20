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

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.twitter.Validator;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.twidere.Constants;

public class TwidereValidator implements Constants {

    private final int mMaxTweetLength;
    private final Validator mValidator;

    public TwidereValidator(@Nullable final SharedPreferencesWrapper preferences) {
        mValidator = new Validator();
        if (preferences != null) {
            final String textLimit = preferences.getString(KEY_STATUS_TEXT_LIMIT, null);
            mMaxTweetLength = NumberUtils.toInt(textLimit, Validator.MAX_TWEET_LENGTH);
        } else {
            mMaxTweetLength = Validator.MAX_TWEET_LENGTH;
        }
    }

    public int getMaxTweetLength() {
        return mMaxTweetLength;
    }

    public int getTweetLength(@Nullable final String text) {
        if (text == null) return 0;
        return mValidator.getTweetLength(text);
    }

    public boolean isValidTweet(final String text) {
        return !TextUtils.isEmpty(text) && getTweetLength(text) <= getMaxTweetLength();
    }

    public boolean isValidDirectMessage(final CharSequence text) {
        return !TextUtils.isEmpty(text);
    }

}
