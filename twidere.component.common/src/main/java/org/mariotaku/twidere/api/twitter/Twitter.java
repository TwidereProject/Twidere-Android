package org.mariotaku.twidere.api.twitter;

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

/**
 * Created by mariotaku on 16/5/13.
 */
public interface Twitter extends SearchResource, TimelineResources, TweetResources, UsersResources,
        ListResources, DirectMessagesResources, FriendsFollowersResources, FavoritesResources,
        SpamReportingResources, SavedSearchesResources, TrendsResources, PlacesGeoResources,
        HelpResources, TwitterPrivate {
}
