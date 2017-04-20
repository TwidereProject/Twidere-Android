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

import org.mariotaku.microblog.library.mastodon.api.AccountResources;
import org.mariotaku.microblog.library.mastodon.api.ApplicationResources;
import org.mariotaku.microblog.library.mastodon.api.BlockResources;
import org.mariotaku.microblog.library.mastodon.api.FavouriteResources;
import org.mariotaku.microblog.library.mastodon.api.FollowRequestResources;
import org.mariotaku.microblog.library.mastodon.api.FollowResources;
import org.mariotaku.microblog.library.mastodon.api.InstanceResources;
import org.mariotaku.microblog.library.mastodon.api.MediaResources;
import org.mariotaku.microblog.library.mastodon.api.MuteResources;
import org.mariotaku.microblog.library.mastodon.api.NotificationResources;
import org.mariotaku.microblog.library.mastodon.api.ReportResources;
import org.mariotaku.microblog.library.mastodon.api.SearchResources;
import org.mariotaku.microblog.library.mastodon.api.StatusResources;
import org.mariotaku.microblog.library.mastodon.api.TimelineResources;

/**
 * Created by mariotaku on 2017/4/17.
 */
public interface Mastodon extends AccountResources, ApplicationResources, BlockResources,
        FavouriteResources, FollowRequestResources, FollowResources, InstanceResources,
        MediaResources, MuteResources, NotificationResources, ReportResources, SearchResources,
        StatusResources, TimelineResources {

}
