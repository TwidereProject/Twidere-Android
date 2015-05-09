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

package org.mariotaku.twidere.api.twitter.api;

import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.param.Query;

import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.StatusActivitySummary;
import org.mariotaku.twidere.api.twitter.model.TranslationResult;
import org.mariotaku.twidere.api.twitter.TwitterException;

@SuppressWarnings("RedundantThrows")
public interface PrivateTweetResources extends PrivateResources {

    StatusActivitySummary getStatusActivitySummary(@Query("id") long statusId) throws TwitterException;

    StatusActivitySummary getStatusActivitySummary(long statusId, boolean includeUserEntities) throws TwitterException;

    @GET("/conversation/show.json")
    ResponseList<Status> showConversation(@Query("id") long statusId) throws TwitterException;

    @GET("/conversation/show.json")
    ResponseList<Status> showConversation(@Query("id") long statusId, @Query Paging paging) throws TwitterException;

    @GET("/translations/show.json")
    TranslationResult showTranslation(@Query("id") long statusId, @Query("dest") String dest) throws TwitterException;
}
