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

package org.mariotaku.twidere.api.twitter;

import org.mariotaku.twidere.api.twitter.api.DirectMessagesResources;
import org.mariotaku.twidere.api.twitter.api.FavoritesResources;
import org.mariotaku.twidere.api.twitter.api.FriendsFollowersResources;
import org.mariotaku.twidere.api.twitter.api.HelpResources;
import org.mariotaku.twidere.api.twitter.api.ListsResources;
import org.mariotaku.twidere.api.twitter.api.PlacesGeoResources;
import org.mariotaku.twidere.api.twitter.api.PrivateActivityResources;
import org.mariotaku.twidere.api.twitter.api.PrivateDirectMessagesResources;
import org.mariotaku.twidere.api.twitter.api.PrivateFriendsFollowersResources;
import org.mariotaku.twidere.api.twitter.api.PrivateTimelinesResources;
import org.mariotaku.twidere.api.twitter.api.PrivateTweetResources;
import org.mariotaku.twidere.api.twitter.api.SavedSearchesResources;
import org.mariotaku.twidere.api.twitter.api.SearchResource;
import org.mariotaku.twidere.api.twitter.api.SpamReportingResources;
import org.mariotaku.twidere.api.twitter.api.TimelinesResources;
import org.mariotaku.twidere.api.twitter.api.TrendsResources;
import org.mariotaku.twidere.api.twitter.api.TweetResources;
import org.mariotaku.twidere.api.twitter.api.UsersResources;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.0
 */
public interface Twitter extends SearchResource, TimelinesResources,
        TweetResources, UsersResources, ListsResources, DirectMessagesResources, FriendsFollowersResources,
        FavoritesResources, SpamReportingResources, SavedSearchesResources, TrendsResources, PlacesGeoResources,
        HelpResources, PrivateActivityResources, PrivateTweetResources, PrivateTimelinesResources,
        PrivateFriendsFollowersResources, PrivateDirectMessagesResources {
}
