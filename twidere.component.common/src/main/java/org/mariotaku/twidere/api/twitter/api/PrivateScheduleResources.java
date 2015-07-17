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

import org.mariotaku.restfu.annotation.method.DELETE;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.method.PUT;
import org.mariotaku.restfu.annotation.param.Body;
import org.mariotaku.restfu.annotation.param.Form;
import org.mariotaku.restfu.annotation.param.MethodExtra;
import org.mariotaku.restfu.annotation.param.Path;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ScheduledStatus;
import org.mariotaku.twidere.api.twitter.model.ScheduledStatusesList;
import org.mariotaku.twidere.api.twitter.model.StatusSchedule;

/**
 * Created by mariotaku on 15/7/6.
 */
public interface PrivateScheduleResources {

    @POST("/schedule/status/tweet.json")
    @Body(BodyType.FORM)
    ScheduledStatus scheduleTweet(@Form StatusSchedule schedule) throws TwitterException;

    @DELETE("/schedule/status/{id}.json")
    ScheduledStatus destroyScheduleTweet(@Path("id") long id) throws TwitterException;

    @PUT("/schedule/status/{id}.json")
    @Body(BodyType.FORM)
    ScheduledStatus updateScheduleTweet(@Path("id") long id, @Form StatusSchedule schedule) throws TwitterException;

    @GET("/schedule/status/list.json")
    @MethodExtra(name = "extra_params", values = {"include_entities", "include_cards", "cards_platform"})
    ScheduledStatusesList getScheduledStatusesList(@Query Paging paging, @Query("state") ScheduledStatus.State[] states) throws TwitterException;

}
