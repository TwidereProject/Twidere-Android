package org.mariotaku.microblog.library.twitter;

import org.mariotaku.microblog.library.twitter.api.DirectMessagesResources;
import org.mariotaku.microblog.library.twitter.api.FavoritesResources;
import org.mariotaku.microblog.library.twitter.api.FriendsFollowersResources;
import org.mariotaku.microblog.library.twitter.api.HelpResources;
import org.mariotaku.microblog.library.twitter.api.ListResources;
import org.mariotaku.microblog.library.twitter.api.PlacesGeoResources;
import org.mariotaku.microblog.library.twitter.api.SavedSearchesResources;
import org.mariotaku.microblog.library.twitter.api.SearchResource;
import org.mariotaku.microblog.library.twitter.api.SpamReportingResources;
import org.mariotaku.microblog.library.twitter.api.TimelineResources;
import org.mariotaku.microblog.library.twitter.api.TrendsResources;
import org.mariotaku.microblog.library.twitter.api.TweetResources;
import org.mariotaku.microblog.library.twitter.api.UsersResources;

/**
 * Created by mariotaku on 16/5/13.
 */
public interface Twitter extends SearchResource, TimelineResources, TweetResources, UsersResources,
        ListResources, DirectMessagesResources, FriendsFollowersResources, FavoritesResources,
        SpamReportingResources, SavedSearchesResources, TrendsResources, PlacesGeoResources,
        HelpResources, TwitterPrivate {
}
