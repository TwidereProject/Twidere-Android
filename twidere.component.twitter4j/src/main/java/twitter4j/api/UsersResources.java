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

import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.param.Query;

import java.io.File;
import java.io.InputStream;

import twitter4j.AccountSettings;
import twitter4j.Category;
import twitter4j.IDs;
import twitter4j.PageableResponseList;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.SettingsUpdate;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface UsersResources {
    User createBlock(long userId) throws TwitterException;

    User createBlock(String screenName) throws TwitterException;

    User createMute(long userId) throws TwitterException;

    User createMute(String screenName) throws TwitterException;

    User destroyBlock(long userId) throws TwitterException;

    User destroyBlock(String screenName) throws TwitterException;

    User destroyMute(long userId) throws TwitterException;

    User destroyMute(String screenName) throws TwitterException;

    AccountSettings getAccountSettings() throws TwitterException;
    IDs getBlocksIDs() throws TwitterException;

    IDs getBlocksIDs(Paging paging) throws TwitterException;

    PageableResponseList<User> getBlocksList() throws TwitterException;

    PageableResponseList<User> getBlocksList(Paging paging) throws TwitterException;

    ResponseList<User> getMemberSuggestions(String categorySlug) throws TwitterException;

    IDs getMutesUsersIDs() throws TwitterException;

    IDs getMutesUsersIDs(Paging paging) throws TwitterException;

    PageableResponseList<User> getMutesUsersList() throws TwitterException;

    PageableResponseList<User> getMutesUsersList(Paging paging) throws TwitterException;

    ResponseList<Category> getSuggestedUserCategories() throws TwitterException;

    ResponseList<User> getUserSuggestions(String categorySlug) throws TwitterException;

    ResponseList<User> lookupUsers(long[] ids) throws TwitterException;

    ResponseList<User> lookupUsers(String[] screenNames) throws TwitterException;

    void removeProfileBannerImage() throws TwitterException;

    ResponseList<User> searchUsers(String query, int page) throws TwitterException;


    @GET("/users/show.json")
    User showUser(@Query("user_id") long userId) throws TwitterException;

    @GET("/users/show.json")
    User showUser(@Query("screen_name") String screenName) throws TwitterException;

    AccountSettings updateAccountSettings(SettingsUpdate settingsUpdate) throws TwitterException;

    User updateProfile(String name, String url, String location, String description) throws TwitterException;

    User updateProfileBackgroundImage(File image, boolean tile) throws TwitterException;

    User updateProfileBackgroundImage(InputStream image, boolean tile) throws TwitterException;

    void updateProfileBannerImage(File banner) throws TwitterException;

    void updateProfileBannerImage(File banner, int width, int height, int offsetLeft, int offsetTop)
            throws TwitterException;

    void updateProfileBannerImage(InputStream banner) throws TwitterException;

    void updateProfileBannerImage(InputStream banner, int width, int height, int offsetLeft, int offsetTop)
            throws TwitterException;

    User updateProfileColors(String profileBackgroundColor, String profileTextColor, String profileLinkColor,
                             String profileSidebarFillColor, String profileSidebarBorderColor) throws TwitterException;

    User updateProfileImage(File image) throws TwitterException;

    User updateProfileImage(InputStream image) throws TwitterException;

    @GET(" account/verify_credentials.json")
    User verifyCredentials() throws TwitterException;
}
