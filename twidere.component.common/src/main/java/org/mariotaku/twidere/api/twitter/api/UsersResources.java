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

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Body;
import org.mariotaku.restfu.annotation.param.Form;
import org.mariotaku.restfu.annotation.param.MethodExtra;
import org.mariotaku.restfu.annotation.param.Part;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.restfu.http.mime.FileTypedData;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.AccountSettings;
import org.mariotaku.twidere.api.twitter.model.Category;
import org.mariotaku.twidere.api.twitter.model.IDs;
import org.mariotaku.twidere.api.twitter.model.PageableResponseList;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ProfileUpdate;
import org.mariotaku.twidere.api.twitter.model.ResponseCode;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.SettingsUpdate;
import org.mariotaku.twidere.api.twitter.model.User;

@SuppressWarnings("RedundantThrows")
@MethodExtra(name = "extra_params", values = {"include_entities"})
public interface UsersResources {

    @POST("/blocks/create.json")
    @Body(BodyType.FORM)
    User createBlock(@Form("user_id") long userId) throws TwitterException;

    @POST("/blocks/create.json")
    @Body(BodyType.FORM)
    User createBlock(@Query("screen_name") String screenName) throws TwitterException;

    @POST("/mutes/users/create.json")
    @Body(BodyType.FORM)
    User createMute(@Form("user_id") long userId) throws TwitterException;

    @POST("/mutes/users/create.json")
    @Body(BodyType.FORM)
    User createMute(@Query("screen_name") String screenName) throws TwitterException;

    @POST("/blocks/destroy.json")
    @Body(BodyType.FORM)
    User destroyBlock(@Form("user_id") long userId) throws TwitterException;

    @POST("/blocks/destroy.json")
    @Body(BodyType.FORM)
    User destroyBlock(@Query("screen_name") String screenName) throws TwitterException;

    @POST("/mutes/users/destroy.json")
    @Body(BodyType.FORM)
    User destroyMute(@Form("user_id") long userId) throws TwitterException;

    @POST("/mutes/users/destroy.json")
    @Body(BodyType.FORM)
    User destroyMute(@Query("screen_name") String screenName) throws TwitterException;

    @GET("/account/settings.json")
    AccountSettings getAccountSettings() throws TwitterException;

    @GET("/blocks/ids.json")
    IDs getBlocksIDs(@Query Paging paging) throws TwitterException;

    @GET("/blocks/list.json")
    PageableResponseList<User> getBlocksList(@Query Paging paging) throws TwitterException;

    ResponseList<User> getMemberSuggestions(String categorySlug) throws TwitterException;

    @GET("/mutes/users/ids.json")
    IDs getMutesUsersIDs(Paging paging) throws TwitterException;

    @GET("/mutes/users/list.json")
    PageableResponseList<User> getMutesUsersList(@Query Paging paging) throws TwitterException;

    ResponseList<Category> getSuggestedUserCategories() throws TwitterException;

    ResponseList<User> getUserSuggestions(String categorySlug) throws TwitterException;

    @POST("/users/lookup.json")
    @Body(BodyType.FORM)
    ResponseList<User> lookupUsers(@Form("user_id") long[] ids) throws TwitterException;

    @GET("/users/lookup.json")
    ResponseList<User> lookupUsers(@Form("screen_name") String[] screenNames) throws TwitterException;

    @POST("/account/remove_profile_banner.json")
    @Body(BodyType.FORM)
    ResponseCode removeProfileBannerImage() throws TwitterException;

    @GET("/users/search.json")
    ResponseList<User> searchUsers(@Query("q") String query, @Query Paging paging) throws TwitterException;

    @GET("/users/show.json")
    User showUser(@Query("user_id") long userId) throws TwitterException;

    @GET("/users/show.json")
    User showUser(@Query("screen_name") String screenName) throws TwitterException;

    @POST("/account/settings.json")
    @Body(BodyType.FORM)
    AccountSettings updateAccountSettings(@Form SettingsUpdate settingsUpdate) throws TwitterException;

    @POST("/account/update_profile.json")
    @Body(BodyType.FORM)
    User updateProfile(@Form ProfileUpdate profileUpdate) throws TwitterException;

    @POST("/account/update_profile_background_image.json")
    @Body(BodyType.MULTIPART)
    User updateProfileBackgroundImage(@Part("image") FileTypedData data, @Part("tile") boolean tile) throws TwitterException;

    @POST("/account/update_profile_background_image.json")
    @Body(BodyType.FORM)
    User updateProfileBackgroundImage(@Form("media_id") long mediaId, @Part("tile") boolean tile) throws TwitterException;

    @POST("/account/update_profile_banner.json")
    @Body(BodyType.MULTIPART)
    ResponseCode updateProfileBannerImage(@Part("banner") FileTypedData data, @Part("width") int width,
                                          @Part("height") int height, @Part("offset_left") int offsetLeft,
                                          @Part("offset_top") int offsetTop)
            throws TwitterException;

    @POST("/account/update_profile_banner.json")
    @Body(BodyType.MULTIPART)
    ResponseCode updateProfileBannerImage(@Part("banner") FileTypedData data) throws TwitterException;

    @POST("/account/update_profile_image.json")
    @Body(BodyType.MULTIPART)
    User updateProfileImage(@Part("image") FileTypedData data) throws TwitterException;

    @GET("/account/verify_credentials.json")
    User verifyCredentials() throws TwitterException;
}
