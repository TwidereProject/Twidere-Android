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

package org.mariotaku.microblog.library.api.fanfou;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.microblog.library.model.fanfou.PhotoStatusUpdate;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.model.Paging;
import org.mariotaku.microblog.library.model.microblog.ResponseList;
import org.mariotaku.microblog.library.model.microblog.Status;

/**
 * Created by mariotaku on 16/3/10.
 */
public interface PhotosResources {

    @GET("/photos/user_timeline.json")
    ResponseList<Status> getPhotosUserTimeline(@Query("id") String id, @Query Paging paging) throws MicroBlogException;

    @POST("/photos/upload.json")
    @BodyType(BodyType.MULTIPART)
    Status uploadPhoto(@Param PhotoStatusUpdate update) throws MicroBlogException;

}
