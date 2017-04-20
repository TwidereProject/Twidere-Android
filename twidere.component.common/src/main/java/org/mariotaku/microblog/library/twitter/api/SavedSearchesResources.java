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

package org.mariotaku.microblog.library.twitter.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Path;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.SavedSearch;

@SuppressWarnings("RedundantThrows")
public interface SavedSearchesResources {

    @POST("/saved_searches/create.json")
    @BodyType(BodyType.FORM)
    SavedSearch createSavedSearch(@Param("query") String query) throws MicroBlogException;

    @POST("/saved_searches/destroy/{id}.json")
    @BodyType(BodyType.FORM)
    SavedSearch destroySavedSearch(@Path("id") long id) throws MicroBlogException;

    @GET("/saved_searches/list.json")
    ResponseList<SavedSearch> getSavedSearches() throws MicroBlogException;

    @POST("/saved_searches/show/{id}.json")
    SavedSearch showSavedSearch(@Path("id") long id) throws MicroBlogException;
}
