/*
 * Twidere - Twitter client for Android
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

package twitter4j.api;

import org.mariotaku.simplerestapi.http.BodyType;
import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.method.POST;
import org.mariotaku.simplerestapi.param.Body;
import org.mariotaku.simplerestapi.param.Form;
import org.mariotaku.simplerestapi.param.Query;

import java.io.File;
import java.io.InputStream;

import twitter4j.AccountSettings;
import twitter4j.Category;
import twitter4j.IDs;
import twitter4j.PageableResponseList;
import twitter4j.Paging;
import twitter4j.ProfileUpdate;
import twitter4j.ResponseList;
import twitter4j.SettingsUpdate;
import twitter4j.TwitterException;
import twitter4j.User;

public interface UsersResources {

    @POST("/blocks/create.json")
    @Body(BodyType.FORM)
    User createBlock(@Form("user_id") long userId) throws TwitterException;

    @POST("/blocks/create.json")
    @Body(BodyType.FORM)
    User createBlock(@Query("screen_name") String screenName) throws TwitterException;

    @POST("/blocks/create.json")
    @Body(BodyType.FORM)
    User createMute(@Form("user_id") long userId) throws TwitterException;

    @POST("/blocks/create.json")
    @Body(BodyType.FORM)
    User createMute(@Query("screen_name") String screenName) throws TwitterException;

    @POST("/blocks/create.json")
    @Body(BodyType.FORM)
    User destroyBlock(@Form("user_id") long userId) throws TwitterException;

    @POST("/blocks/create.json")
    @Body(BodyType.FORM)
    User destroyBlock(@Query("screen_name") String screenName) throws TwitterException;

    @POST("/blocks/create.json")
    @Body(BodyType.FORM)
    User destroyMute(@Form("user_id") long userId) throws TwitterException;

    @POST("/blocks/create.json")
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
    ResponseList<User> lookupUsers(@Form("id") long[] ids) throws TwitterException;

    @GET("/users/lookup.json")
    ResponseList<User> lookupUsers(@Form("id") String[] screenNames) throws TwitterException;

    @POST("/account/remove_profile_banner.json")
    void removeProfileBannerImage() throws TwitterException;

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

    User updateProfileBackgroundImage(File image, boolean tile) throws TwitterException;

    User updateProfileBackgroundImage(InputStream image, boolean tile) throws TwitterException;

    void updateProfileBannerImage(File banner) throws TwitterException;

    void updateProfileBannerImage(File banner, int width, int height, int offsetLeft, int offsetTop)
            throws TwitterException;

    void updateProfileBannerImage(InputStream banner) throws TwitterException;

    void updateProfileBannerImage(InputStream banner, int width, int height, int offsetLeft, int offsetTop)
            throws TwitterException;

    User updateProfileImage(File image) throws TwitterException;

    User updateProfileImage(InputStream image) throws TwitterException;

    @GET("/account/verify_credentials.json")
    User verifyCredentials() throws TwitterException;
}
