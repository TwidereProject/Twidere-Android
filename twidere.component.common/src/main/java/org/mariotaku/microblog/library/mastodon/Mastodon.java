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

package org.mariotaku.microblog.library.mastodon;

import org.mariotaku.microblog.library.mastodon.api.AccountsResources;
import org.mariotaku.microblog.library.mastodon.api.AppsResources;
import org.mariotaku.microblog.library.mastodon.api.BlocksResources;
import org.mariotaku.microblog.library.mastodon.api.FavouritesResources;
import org.mariotaku.microblog.library.mastodon.api.FollowRequestsResources;
import org.mariotaku.microblog.library.mastodon.api.InstancesResources;
import org.mariotaku.microblog.library.mastodon.api.MediaResources;
import org.mariotaku.microblog.library.mastodon.api.MutesResources;
import org.mariotaku.microblog.library.mastodon.api.NotificationsResources;
import org.mariotaku.microblog.library.mastodon.api.ReportsResources;
import org.mariotaku.microblog.library.mastodon.api.SearchResources;
import org.mariotaku.microblog.library.mastodon.api.StatusesResources;
import org.mariotaku.microblog.library.mastodon.api.TimelinesResources;

/**
 * Created by mariotaku on 2017/4/17.
 */
public interface Mastodon extends AccountsResources, AppsResources, BlocksResources,
        FavouritesResources, FollowRequestsResources, InstancesResources,
        MediaResources, MutesResources, NotificationsResources, ReportsResources, SearchResources,
        StatusesResources, TimelinesResources {

}
