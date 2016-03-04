package org.mariotaku.twidere.api.twitter;

import org.mariotaku.twidere.api.twitter.api.PrivateActivityResources;
import org.mariotaku.twidere.api.twitter.api.PrivateDirectMessagesResources;
import org.mariotaku.twidere.api.twitter.api.PrivateFriendsFollowersResources;
import org.mariotaku.twidere.api.twitter.api.PrivateScheduleResources;
import org.mariotaku.twidere.api.twitter.api.PrivateTimelinesResources;
import org.mariotaku.twidere.api.twitter.api.PrivateTweetResources;

/**
 * Created by mariotaku on 16/3/4.
 */
public interface TwitterPrivate extends PrivateActivityResources, PrivateTweetResources,
        PrivateTimelinesResources, PrivateFriendsFollowersResources, PrivateDirectMessagesResources,
        PrivateScheduleResources {
}
