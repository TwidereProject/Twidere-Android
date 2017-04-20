/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.microblog.library.twitter.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Language;
import org.mariotaku.microblog.library.twitter.model.RateLimitStatus;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.TwitterAPIConfiguration;

import java.util.Map;

@SuppressWarnings("RedundantThrows")
public interface HelpResources {
    /**
     * Returns the current configuration used by Twitter including twitter.com
     * slugs which are not usernames, maximum photo resolutions, and t.co URL
     * lengths.</br> It is recommended applications request this endpoint when
     * they are loaded, but no more than once a day.
     *
     * @return configuration
     * @throws MicroBlogException when Twitter service or network is
     *                          unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/get/help/configuration">GET
     * help/configuration | Twitter Developers</a>
     * @since Twitter4J 2.2.3
     */
    @GET("/help/configuration.json")
    TwitterAPIConfiguration getAPIConfiguration() throws MicroBlogException;

    /**
     * Returns the list of languages supported by Twitter along with their ISO
     * 639-1 code. The ISO 639-1 code is the two letter value to use if you
     * include lang with any of your requests.
     *
     * @return list of languages supported by Twitter
     * @throws MicroBlogException when Twitter service or network is
     *                          unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/get/help/languages">GET
     * help/languages | Twitter Developers</a>
     * @since Twitter4J 2.2.3
     */
    @GET("/help/languages.json")
    ResponseList<Language> getLanguages() throws MicroBlogException;

    String getPrivacyPolicy() throws MicroBlogException;

    Map<String, RateLimitStatus> getRateLimitStatus() throws MicroBlogException;

    Map<String, RateLimitStatus> getRateLimitStatus(String... resources) throws MicroBlogException;

    String getTermsOfService() throws MicroBlogException;

}
