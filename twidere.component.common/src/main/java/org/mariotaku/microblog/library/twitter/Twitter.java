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

package org.mariotaku.microblog.library.twitter;

import org.mariotaku.microblog.library.twitter.api.DirectMessagesEventResources;
import org.mariotaku.microblog.library.twitter.api.DirectMessagesResources;
import org.mariotaku.microblog.library.twitter.api.FavoritesResources;
import org.mariotaku.microblog.library.twitter.api.FriendsFollowersResources;
import org.mariotaku.microblog.library.twitter.api.HelpResources;
import org.mariotaku.microblog.library.twitter.api.ListResources;
import org.mariotaku.microblog.library.twitter.api.MutesResources;
import org.mariotaku.microblog.library.twitter.api.PlacesGeoResources;
import org.mariotaku.microblog.library.twitter.api.SavedSearchesResources;
import org.mariotaku.microblog.library.twitter.api.SearchResources;
import org.mariotaku.microblog.library.twitter.api.SpamReportingResources;
import org.mariotaku.microblog.library.twitter.api.TimelineResources;
import org.mariotaku.microblog.library.twitter.api.TrendsResources;
import org.mariotaku.microblog.library.twitter.api.TweetResources;
import org.mariotaku.microblog.library.twitter.api.UsersResources;

/**
 * Created by mariotaku on 16/5/13.
 */
public interface Twitter extends SearchResources, TimelineResources, TweetResources, UsersResources,
        ListResources, DirectMessagesResources, DirectMessagesEventResources,
        FriendsFollowersResources, FavoritesResources, SpamReportingResources,
        SavedSearchesResources, TrendsResources, PlacesGeoResources,
        HelpResources, MutesResources, TwitterPrivate {
}
