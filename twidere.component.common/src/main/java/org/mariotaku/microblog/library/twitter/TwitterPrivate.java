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
