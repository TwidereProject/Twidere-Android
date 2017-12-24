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

package org.mariotaku.microblog.library;

import org.mariotaku.microblog.library.api.twitter.DirectMessagesEventResources;
import org.mariotaku.microblog.library.api.twitter.HelpResources;
import org.mariotaku.microblog.library.api.twitter.ListResources;
import org.mariotaku.microblog.library.api.twitter.MutesResources;
import org.mariotaku.microblog.library.api.twitter.PlacesGeoResources;
import org.mariotaku.microblog.library.api.twitter.SavedSearchesResources;
import org.mariotaku.microblog.library.api.twitter.SearchResources;
import org.mariotaku.microblog.library.api.twitter.SpamReportingResources;
import org.mariotaku.microblog.library.api.twitter.TrendsResources;
import org.mariotaku.microblog.library.api.microblog.StatusesResources;

public interface Twitter extends MicroBlog, SearchResources, StatusesResources, ListResources,
        DirectMessagesEventResources, SpamReportingResources,
        SavedSearchesResources, TrendsResources, PlacesGeoResources,
        HelpResources, MutesResources, TwitterPrivate {
}
