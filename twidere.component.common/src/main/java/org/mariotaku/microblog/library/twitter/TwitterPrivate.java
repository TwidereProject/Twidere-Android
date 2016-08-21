package org.mariotaku.microblog.library.twitter;

import org.mariotaku.microblog.library.twitter.api.PrivateAccountResources;
import org.mariotaku.microblog.library.twitter.api.PrivateActivityResources;
import org.mariotaku.microblog.library.twitter.api.PrivateDirectMessagesResources;
import org.mariotaku.microblog.library.twitter.api.PrivateFriendsFollowersResources;
import org.mariotaku.microblog.library.twitter.api.PrivateSearchResources;
import org.mariotaku.microblog.library.twitter.api.PrivateTimelineResources;
import org.mariotaku.microblog.library.twitter.api.PrivateTweetResources;

/**
 * Created by mariotaku on 16/3/4.
 */
public interface TwitterPrivate extends PrivateActivityResources, PrivateTweetResources,
        PrivateTimelineResources, PrivateFriendsFollowersResources, PrivateDirectMessagesResources,
        PrivateSearchResources, PrivateAccountResources {
}
