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

import org.mariotaku.twidere.api.statusnet.StatusNet;
import org.mariotaku.twidere.api.twitter.api.DirectMessagesResources;
import org.mariotaku.twidere.api.twitter.api.FavoritesResources;
import org.mariotaku.twidere.api.twitter.api.FriendsFollowersResources;
import org.mariotaku.twidere.api.twitter.api.HelpResources;
import org.mariotaku.twidere.api.twitter.api.ListResources;
import org.mariotaku.twidere.api.twitter.api.PlacesGeoResources;
import org.mariotaku.twidere.api.twitter.api.SavedSearchesResources;
import org.mariotaku.twidere.api.twitter.api.SearchResource;
import org.mariotaku.twidere.api.twitter.api.SpamReportingResources;
import org.mariotaku.twidere.api.twitter.api.TimelineResources;
import org.mariotaku.twidere.api.twitter.api.TrendsResources;
import org.mariotaku.twidere.api.twitter.api.TweetResources;
import org.mariotaku.twidere.api.twitter.api.UsersResources;

public interface Twitter extends SearchResource, TimelineResources,
        TweetResources, UsersResources, ListResources, DirectMessagesResources, FriendsFollowersResources,
        FavoritesResources, SpamReportingResources, SavedSearchesResources, TrendsResources, PlacesGeoResources,
        HelpResources, TwitterPrivate, StatusNet {
}
