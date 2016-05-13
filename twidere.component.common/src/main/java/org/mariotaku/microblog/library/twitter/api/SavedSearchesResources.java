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
