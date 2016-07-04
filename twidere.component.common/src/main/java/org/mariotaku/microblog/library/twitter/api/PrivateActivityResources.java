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

package org.mariotaku.microblog.library.twitter.api;

import org.mariotaku.microblog.library.twitter.template.StatusAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Queries;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Activity;
import org.mariotaku.microblog.library.twitter.model.CursorTimestampResponse;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;

@SuppressWarnings("RedundantThrows")
@Queries(template = StatusAnnotationTemplate.class)
public interface PrivateActivityResources extends PrivateResources {

    @GET("/activity/about_me.json")
    ResponseList<Activity> getActivitiesAboutMe(@Query Paging paging) throws MicroBlogException;

    @Queries({})
    @GET("/activity/about_me/unread.json")
    CursorTimestampResponse getActivitiesAboutMeUnread(@Query("cursor") boolean cursor) throws MicroBlogException;

    @Queries({})
    @POST("/activity/about_me/unread.json")
    @BodyType(BodyType.FORM)
    CursorTimestampResponse setActivitiesAboutMeUnread(@Param("cursor") long cursor) throws MicroBlogException;


}
