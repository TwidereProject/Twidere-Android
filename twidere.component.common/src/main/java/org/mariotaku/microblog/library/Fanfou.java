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

package org.mariotaku.microblog.library;

import org.mariotaku.microblog.library.api.fanfou.BlocksResources;
import org.mariotaku.microblog.library.api.fanfou.DirectMessagesResources;
import org.mariotaku.microblog.library.api.fanfou.FanfouSearchResources;
import org.mariotaku.microblog.library.api.fanfou.FanfouStatusesResources;
import org.mariotaku.microblog.library.api.fanfou.FavoritesResources;
import org.mariotaku.microblog.library.api.fanfou.FriendshipsResources;
import org.mariotaku.microblog.library.api.fanfou.PhotosResources;
import org.mariotaku.microblog.library.api.fanfou.TrendsResources;
import org.mariotaku.microblog.library.api.fanfou.UsersResources;

public interface Fanfou extends MicroBlog, FanfouStatusesResources, FanfouSearchResources, UsersResources, PhotosResources,
        FriendshipsResources, BlocksResources, FavoritesResources, DirectMessagesResources,
        TrendsResources {
}
