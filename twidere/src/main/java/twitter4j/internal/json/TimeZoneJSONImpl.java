/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.internal.json;

import static twitter4j.internal.util.InternalParseUtil.getInt;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.TimeZone;
import twitter4j.TwitterException;

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
