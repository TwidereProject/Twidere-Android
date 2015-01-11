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

package twitter4j.internal.json;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.TimeZone;
import twitter4j.TwitterException;

import static twitter4j.internal.util.InternalParseUtil.getInt;

/**
 * @author Alessandro Bahgat - ale.bahgat at gmail.com
 */
public class TimeZoneJSONImpl implements TimeZone {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4715603575648656436L;
	private final String NAME;
	private final String TZINFO_NAME;
	private final int UTC_OFFSET;

	TimeZoneJSONImpl(final JSONObject jSONObject) throws TwitterException {
		try {
			UTC_OFFSET = getInt("utc_offset", jSONObject);
			NAME = jSONObject.getString("name");
			TZINFO_NAME = jSONObject.getString("tzinfo_name");
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String tzinfoName() {
		return TZINFO_NAME;
	}

	@Override
	public int utcOffset() {
		return UTC_OFFSET;
	}

}
