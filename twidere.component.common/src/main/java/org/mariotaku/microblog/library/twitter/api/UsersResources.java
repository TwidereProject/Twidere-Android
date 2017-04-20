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

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.AccountSettings;
import org.mariotaku.microblog.library.twitter.model.Category;
import org.mariotaku.microblog.library.twitter.model.IDs;
import org.mariotaku.microblog.library.twitter.model.PageableResponseList;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ProfileUpdate;
import org.mariotaku.microblog.library.twitter.model.ResponseCode;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.SettingsUpdate;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.microblog.library.twitter.template.UserAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.restfu.http.mime.FileBody;

@SuppressWarnings("RedundantThrows")
@Params(template = UserAnnotationTemplate.class)
public interface UsersResources {

    @POST("/blocks/create.json")
    User createBlock(@Param("user_id") String userId) throws MicroBlogException;

    @POST("/blocks/create.json")
    User createBlockByScreenName(@Query("screen_name") String screenName) throws MicroBlogException;


    @POST("/blocks/destroy.json")
    User destroyBlock(@Param("user_id") String userId) throws MicroBlogException;

    @POST("/blocks/destroy.json")
    User destroyBlockByScreenName(@Query("screen_name") String screenName) throws MicroBlogException;


    @GET("/account/settings.json")
    AccountSettings getAccountSettings() throws MicroBlogException;

    @GET("/blocks/ids.json")
    IDs getBlocksIDs(@Query Paging paging) throws MicroBlogException;

    @GET("/blocks/list.json")
    PageableResponseList<User> getBlocksList(@Query Paging paging) throws MicroBlogException;

    ResponseList<User> getMemberSuggestions(String categorySlug) throws MicroBlogException;


    ResponseList<Category> getSuggestedUserCategories() throws MicroBlogException;

    ResponseList<User> getUserSuggestions(String categorySlug) throws MicroBlogException;

    @POST("/users/lookup.json")
    @BodyType(BodyType.FORM)
    ResponseList<User> lookupUsers(@Param(value = "user_id", arrayDelimiter = ',') String[] ids) throws MicroBlogException;

    @GET("/users/lookup.json")
    ResponseList<User> lookupUsersByScreenName(@Param(value = "screen_name", arrayDelimiter = ',') String[] screenNames) throws MicroBlogException;

    @POST("/account/remove_profile_banner.json")
    ResponseCode removeProfileBannerImage() throws MicroBlogException;

    @GET("/users/search.json")
    ResponseList<User> searchUsers(@Query("q") String query, @Query Paging paging) throws MicroBlogException;

    @GET("/users/show.json")
    User showUser(@Query("user_id") String userId) throws MicroBlogException;

    @GET("/users/show.json")
    User showUserByScreenName(@Query("screen_name") String screenName) throws MicroBlogException;

    @POST("/account/settings.json")
    AccountSettings updateAccountSettings(@Param SettingsUpdate settingsUpdate) throws MicroBlogException;

    @POST("/account/update_profile.json")
    User updateProfile(@Param ProfileUpdate profileUpdate) throws MicroBlogException;

    @POST("/account/update_profile_background_image.json")
    User updateProfileBackgroundImage(@Param("image") Body data, @Param("tile") boolean tile) throws MicroBlogException;

    @POST("/account/update_profile_background_image.json")
    User updateProfileBackgroundImage(@Param("media_id") long mediaId, @Param("tile") boolean tile) throws MicroBlogException;

    @POST("/account/update_profile_banner.json")
    ResponseCode updateProfileBannerImage(@Param("banner") FileBody data, @Param("width") int width,
            @Param("height") int height, @Param("offset_left") int offsetLeft,
            @Param("offset_top") int offsetTop)
            throws MicroBlogException;

    @POST("/account/update_profile_banner.json")
    ResponseCode updateProfileBannerImage(@Param("banner") Body data) throws MicroBlogException;

    @POST("/account/update_profile_image.json")
    User updateProfileImage(@Param("image") Body data) throws MicroBlogException;

    @GET("/account/verify_credentials.json")
    User verifyCredentials() throws MicroBlogException;
}
