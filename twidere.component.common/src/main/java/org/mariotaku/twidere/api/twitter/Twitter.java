/*
 *                 Twidere - Twitter client for Android
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
import org.mariotaku.twidere.api.twitter.api.PrivateScheduleResources;
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
        PrivateFriendsFollowersResources, PrivateDirectMessagesResources, PrivateScheduleResources {
}
