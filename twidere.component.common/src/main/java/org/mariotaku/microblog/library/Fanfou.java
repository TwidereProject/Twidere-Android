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

import org.mariotaku.microblog.library.fanfou.api.BlocksResources;
import org.mariotaku.microblog.library.fanfou.api.DirectMessagesResources;
import org.mariotaku.microblog.library.fanfou.api.FavoritesResources;
import org.mariotaku.microblog.library.fanfou.api.FriendshipsResources;
import org.mariotaku.microblog.library.fanfou.api.PhotosResources;
import org.mariotaku.microblog.library.fanfou.api.SearchResources;
import org.mariotaku.microblog.library.fanfou.api.StatusesResources;
import org.mariotaku.microblog.library.fanfou.api.TrendsResources;
import org.mariotaku.microblog.library.fanfou.api.UsersResources;

/**
 * Created by mariotaku on 16/3/10.
 */
public interface Fanfou extends StatusesResources, SearchResources, UsersResources, PhotosResources,
        FriendshipsResources, BlocksResources, FavoritesResources, DirectMessagesResources,
        TrendsResources {
}
