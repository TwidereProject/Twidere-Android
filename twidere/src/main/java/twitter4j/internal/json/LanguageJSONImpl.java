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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.api.HelpResources;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.3
 */
public class LanguageJSONImpl implements HelpResources.Language {
	private String name;
	private String code;
	private String status;

	LanguageJSONImpl(final JSONObject json) throws TwitterException {
		super();
		init(json);
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getStatus() {
		return status;
	}

	private void init(final JSONObject json) throws TwitterException {
		try {
			name = json.getString("name");
			code = json.getString("code");
			status = json.getString("status");

		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

	static ResponseList<HelpResources.Language> createLanguageList(final HttpResponse res, final Configuration conf)
			throws TwitterException {
		return createLanguageList(res.asJSONArray(), res, conf);
	}

	/* package */
	static ResponseList<HelpResources.Language> createLanguageList(final JSONArray list, final HttpResponse res,
			final Configuration conf) throws TwitterException {
		try {
			final int size = list.length();
			final ResponseList<HelpResources.Language> languages = new ResponseListImpl<HelpResources.Language>(size,
					res);
			for (int i = 0; i < size; i++) {
				final JSONObject json = list.getJSONObject(i);
				final HelpResources.Language language = new LanguageJSONImpl(json);
				languages.add(language);
			}
			return languages;
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		} catch (final TwitterException te) {
			throw te;
		}
	}
}
