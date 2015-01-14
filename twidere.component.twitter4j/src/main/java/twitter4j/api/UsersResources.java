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

import java.io.File;
import java.io.InputStream;

import twitter4j.AccountSettings;
import twitter4j.Category;
import twitter4j.CursorPaging;
import twitter4j.IDs;
import twitter4j.PageableResponseList;
import twitter4j.ResponseList;
import twitter4j.SettingsUpdate;
import twitter4j.TwitterException;
import twitter4j.User;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface UsersResources {
    /**
     * Blocks the user specified in the ID parameter as the authenticating user.
     * Returns the blocked user in the requested format when successful. <br>
     * This method calls http://api.twitter.com/1.1/blocks/create/[id].json
     *
     * @param userId the ID of the user to block
     * @return the blocked user
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/post/blocks/create">POST
     * blocks/create | Twitter Developers</a>
     * @since Twitter4J 2.1.0
     */
    User createBlock(long userId) throws TwitterException;

    /**
     * Blocks the user specified in the ID parameter as the authenticating user.
     * Returns the blocked user in the requested format when successful. <br>
     * This method calls http://api.twitter.com/1.1/blocks/create/[id].json
     *
     * @param screenName the screen_name of the user to block
     * @return the blocked user
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/post/blocks/create">POST
     * blocks/create | Twitter Developers</a>
     * @since Twitter4J 2.0.1
     */
    User createBlock(String screenName) throws TwitterException;

    User createMute(long userId) throws TwitterException;

    User createMute(String screenName) throws TwitterException;

    /**
     * Un-blocks the user specified in the ID parameter as the authenticating
     * user. Returns the un-blocked user in the requested format when
     * successful. <br>
     * This method calls http://api.twitter.com/1.1/blocks/destroy/[id].json
     *
     * @param userId the ID of the user to block
     * @return the unblocked user
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/post/blocks/destroy">POST
     * blocks/destroy | Twitter Developers</a>
     * @since Twitter4J 2.0.1
     */
    User destroyBlock(long userId) throws TwitterException;

    /**
     * Un-blocks the user specified in the ID parameter as the authenticating
     * user. Returns the un-blocked user in the requested format when
     * successful. <br>
     * This method calls http://api.twitter.com/1.1/blocks/destroy/[id].json
     *
     * @param screenName the screen name of the user to block
     * @return the unblocked user
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/post/blocks/destroy">POST
     * blocks/destroy | Twitter Developers</a>
     * @since Twitter4J 2.0.1
     */
    User destroyBlock(String screenName) throws TwitterException;

    User destroyMute(long userId) throws TwitterException;

    User destroyMute(String screenName) throws TwitterException;

    /**
     * Returns the current trend, geo, language, timezone and sleep time
     * information for the authenticating user. <br>
     * This method has not been finalized and the interface is subject to change
     * in incompatible ways. <br>
     * This method calls http://api.twitter.com/1.1/account/settings.json
     *
     * @return the current trend, geo and sleep time information for the
     * authenticating user.
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/get/account/totals">GET
     * account/settings | Twitter Developers</a>
     * @since Twitter4J 2.1.9
     */
    AccountSettings getAccountSettings() throws TwitterException;

    /**
     * Returns an array of numeric user ids the authenticating user is blocking. <br>
     * This method calls http://api.twitter.com/1.1/blocks/blocks/ids
     *
     * @return Returns an array of numeric user ids the authenticating user is
     * blocking.
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a href="https://dev.twitter.com/docs/api/1.1/get/blocks/ids">GET
     * blocks/ids | Twitter Developers</a>
     * @since Twitter4J 2.0.4
     */
    IDs getBlocksIDs() throws TwitterException;

    IDs getBlocksIDs(CursorPaging paging) throws TwitterException;

    /**
     * Returns a list of user objects that the authenticating user is blocking. <br>
     * This method calls http://api.twitter.com/1.1/blocks/blocking.json
     *
     * @return a list of user objects that the authenticating user
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/get/blocks/blocking">GET
     * blocks/blocking | Twitter Developers</a>
     * @since Twitter4J 2.0.4
     */
    PageableResponseList<User> getBlocksList() throws TwitterException;

    PageableResponseList<User> getBlocksList(CursorPaging paging) throws TwitterException;

    /**
     * Access the users in a given category of the Twitter suggested user list
     * and return their most recent status if they are not a protected user. <br>
     * This method has not been finalized and the interface is subject to change
     * in incompatible ways. <br>
     * This method calls
     * http://api.twitter.com/1.1/users/suggestions/:slug/members.json
     *
     * @param categorySlug slug
     * @return list of suggested users
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="http://groups.google.com/group/twitter-api-announce/msg/34909da7c399169e">#newtwitter
     * and the API - Twitter API Announcements | Google Group</a>
     * @since Twitter4J 2.1.9
     */
    ResponseList<User> getMemberSuggestions(String categorySlug) throws TwitterException;

    IDs getMutesUsersIDs() throws TwitterException;

    IDs getMutesUsersIDs(CursorPaging paging) throws TwitterException;

    PageableResponseList<User> getMutesUsersList() throws TwitterException;

    PageableResponseList<User> getMutesUsersList(CursorPaging paging) throws TwitterException;

    /**
     * Access to Twitter's suggested user list. This returns the list of
     * suggested user categories. The category can be used in the
     * users/suggestions/category endpoint to get the users in that category. <br>
     * This method calls http://api.twitter.com/1.1/users/suggestions/:slug.json
     *
     * @return list of suggested user categories.
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/get/users/suggestions/:slug">GET
     * users/suggestions/:slug | Twitter Developers</a>
     * @since Twitter4J 2.1.1
     */
    ResponseList<Category> getSuggestedUserCategories() throws TwitterException;

    /**
     * Access the users in a given category of the Twitter suggested user list.<br>
     * It is recommended that end clients cache this data for no more than one
     * hour. <br>
     * This method calls http://api.twitter.com/1.1/users/suggestions/:slug.json
     *
     * @param categorySlug slug
     * @return list of suggested users
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/get/users/suggestions/slug">GET
     * users/suggestions/slug | Twitter Developers</a>
     * @since Twitter4J 2.1.1
     */
    ResponseList<User> getUserSuggestions(String categorySlug) throws TwitterException;

    /**
     * Return up to 100 users worth of extended information, specified by either
     * ID, screen name, or combination of the two. The author's most recent
     * status (if the authenticating user has permission) will be returned
     * inline. <br>
     * This method calls http://api.twitter.com/1.1/users/lookup.json
     *
     * @param ids Specifies the screen names of the users to return.
     * @return users
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a href="https://dev.twitter.com/docs/api/1.1/get/users/lookup">GET
     * users/lookup | Twitter Developers</a>
     * @since Twitter4J 2.1.1
     */
    ResponseList<User> lookupUsers(long[] ids) throws TwitterException;

    /**
     * Return up to 100 users worth of extended information, specified by either
     * ID, screen name, or combination of the two. The author's most recent
     * status (if the authenticating user has permission) will be returned
     * inline. <br>
     * This method calls http://api.twitter.com/1.1/users/lookup.json
     *
     * @param screenNames Specifies the screen names of the users to return.
     * @return users
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a href="https://dev.twitter.com/docs/api/1.1/get/users/lookup">GET
     * users/lookup | Twitter Developers</a>
     * @since Twitter4J 2.1.1
     */
    ResponseList<User> lookupUsers(String[] screenNames) throws TwitterException;

    void removeProfileBannerImage() throws TwitterException;

    /**
     * Run a search for users similar to the Find People button on Twitter.com;
     * the same results returned by people search on Twitter.com will be
     * returned by using this API.<br>
     * Usage note: It is only possible to retrieve the first 1000 matches from
     * this API. <br>
     * This method calls http://api.twitter.com/1.1/users/search.json
     *
     * @param query The query to run against people search.
     * @param page  Specifies the page of results to retrieve. Number of statuses
     *              per page is fixed to 20.
     * @return the list of Users matches the provided
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a href="https://dev.twitter.com/docs/api/1.1/get/users/search">GET
     * users/search | Twitter Developers</a>
     */
    ResponseList<User> searchUsers(String query, int page) throws TwitterException;

    /**
     * Returns extended information of a given user, specified by ID or screen
     * name as per the required id parameter. The author's most recent status
     * will be returned inline. <br>
     * This method calls http://api.twitter.com/1.1/users/show.json
     *
     * @param userId the ID of the user for whom to request the detail
     * @return users
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a href="https://dev.twitter.com/docs/api/1.1/get/users/show">GET
     * users/show | Twitter Developers</a>
     * @since Twitter4J 2.1.0
     */
    User showUser(long userId) throws TwitterException;

    /**
     * Returns extended information of a given user, specified by ID or screen
     * name as per the required id parameter. The author's most recent status
     * will be returned inline. <br>
     * This method calls http://api.twitter.com/1.1/users/show.json
     *
     * @param screenName the screen name of the user for whom to request the
     *                   detail
     * @return User
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a href="https://dev.twitter.com/docs/api/1.1/get/users/show">GET
     * users/show | Twitter Developers</a>
     */
    User showUser(String screenName) throws TwitterException;

    /**
     * Updates the current trend, geo, language, timezone and sleep time
     * information for the authenticating user. <br>
     * This method has not been finalized and the interface is subject to change
     * in incompatible ways. <br>
     * This method calls http://api.twitter.com/1.1/account/settings.json
     *
     * @param settingsUpdate Settings to be updated
     * @return the current trend, geo and sleep time information for the
     * authenticating user.
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/post/account/settings">POST
     * account/settings | Twitter Developers</a>
     * @since Twitter4J 2.2.4
     */
    AccountSettings updateAccountSettings(SettingsUpdate settingsUpdate) throws TwitterException;

    /**
     * Sets values that users are able to set under the "Account" tab of their
     * settings page. Only the parameters specified(non-null) will be updated. <br>
     * This method calls http://api.twitter.com/1.1/account/update_profile.json
     *
     * @param name        Optional. Maximum of 20 characters.
     * @param url         Optional. Maximum of 100 characters. Will be prepended with
     *                    "http://" if not present.
     * @param location    Optional. Maximum of 30 characters. The contents are not
     *                    normalized or geocoded in any way.
     * @param description Optional. Maximum of 160 characters.
     * @return the updated user
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/post/account/update_profile">POST
     * account/update_profile | Twitter Developers</a>
     * @since Twitter4J 2.1.8
     */
    User updateProfile(String name, String url, String location, String description) throws TwitterException;

    /**
     * Updates the authenticating user's profile background image. <br>
     * This method calls
     * http://api.twitter.com/1.1/account/update_profile_background_image.json
     *
     * @param image Must be a valid GIF, JPG, or PNG image of less than 800
     *              kilobytes in size. Images with width larger than 2048 pixels
     *              will be forceably scaled down.
     * @param tile  If set to true the background image will be displayed tiled.
     *              The image will not be tiled otherwise.
     * @return the updated user
     * @throws TwitterException when Twitter service or network is unavailable,
     *                          or when the specified file is not found
     *                          (FileNotFoundException will be nested), or when the specified
     *                          file object in not representing a file (IOException will be
     *                          nested)
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/post/account/update_profile_background_image">POST
     * account/update_profile_background_image | Twitter Developers</a>
     * @since Twitter4J 2.1.0
     */
    User updateProfileBackgroundImage(File image, boolean tile) throws TwitterException;

    /**
     * Updates the authenticating user's profile background image. <br>
     * This method calls
     * http://api.twitter.com/1.1/account/update_profile_background_image.json
     *
     * @param image Must be a valid GIF, JPG, or PNG image of less than 800
     *              kilobytes in size. Images with width larger than 2048 pixels
     *              will be forceably scaled down.
     * @param tile  If set to true the background image will be displayed tiled.
     *              The image will not be tiled otherwise.
     * @return the updated user
     * @throws TwitterException when Twitter service or network is unavailable,
     *                          or when the specified file is not found
     *                          (FileNotFoundException will be nested), or when the specified
     *                          file object in not representing a file (IOException will be
     *                          nested)
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/post/account/update_profile_background_image">POST
     * account/update_profile_background_image | Twitter Developers</a>
     * @since Twitter4J 2.1.11
     */
    User updateProfileBackgroundImage(InputStream image, boolean tile) throws TwitterException;

    /**
     * <table border="1" width="50%" align="center" cellpadding="1">
     * <thead>
     * <tr>
     * <th>Code(s)</th>
     * <th>Meaning</th>
     * </tr>
     * </thead>
     * <td>200,201,202</td>
     * <td>Profile banner image successfully uploaded</td>
     * <tr>
     * <td>400</td>
     * <td>Either an image was not provided or the image data could be processed
     * </td>
     * </tr>
     * <tr>
     * <td>422</td>
     * <td>The image could not be resized or it too large</td>
     * </tr>
     * </table>
     *
     * @throws TwitterException when Twitter service or network is unavailable,
     *                          or when the specified file is not found
     *                          (FileNotFoundException will be nested), or when the specified
     *                          file object in not representing a file (IOException will be
     *                          nested)
     */
    void updateProfileBannerImage(File banner) throws TwitterException;

    /**
     * <table border="1" width="50%" align="center" cellpadding="1">
     * <thead>
     * <tr>
     * <th>Code(s)</th>
     * <th>Meaning</th>
     * </tr>
     * </thead>
     * <td>200,201,202</td>
     * <td>Profile banner image successfully uploaded</td>
     * <tr>
     * <td>400</td>
     * <td>Either an image was not provided or the image data could be processed
     * </td>
     * </tr>
     * <tr>
     * <td>422</td>
     * <td>The image could not be resized or it too large</td>
     * </tr>
     * </table>
     *
     * @throws TwitterException when Twitter service or network is unavailable,
     *                          or when the specified file is not found
     *                          (FileNotFoundException will be nested), or when the specified
     *                          file object in not representing a file (IOException will be
     *                          nested)
     */
    void updateProfileBannerImage(File banner, int width, int height, int offsetLeft, int offsetTop)
            throws TwitterException;

    /**
     * <table border="1" width="50%" align="center" cellpadding="1">
     * <thead>
     * <tr>
     * <th>Code(s)</th>
     * <th>Meaning</th>
     * </tr>
     * </thead>
     * <td>200,201,202</td>
     * <td>Profile banner image successfully uploaded</td>
     * <tr>
     * <td>400</td>
     * <td>Either an image was not provided or the image data could be processed
     * </td>
     * </tr>
     * <tr>
     * <td>422</td>
     * <td>The image could not be resized or it too large</td>
     * </tr>
     * </table>
     *
     * @throws TwitterException when Twitter service or network is unavailable,
     *                          or when the specified file is not found
     *                          (FileNotFoundException will be nested), or when the specified
     *                          file object in not representing a file (IOException will be
     *                          nested)
     */
    void updateProfileBannerImage(InputStream banner) throws TwitterException;

    /**
     * <table border="1" width="50%" align="center" cellpadding="1">
     * <thead>
     * <tr>
     * <th>Code(s)</th>
     * <th>Meaning</th>
     * </tr>
     * </thead>
     * <td>200,201,202</td>
     * <td>Profile banner image successfully uploaded</td>
     * <tr>
     * <td>400</td>
     * <td>Either an image was not provided or the image data could be processed
     * </td>
     * </tr>
     * <tr>
     * <td>422</td>
     * <td>The image could not be resized or it too large</td>
     * </tr>
     * </table>
     *
     * @throws TwitterException when Twitter service or network is unavailable,
     *                          or when the specified file is not found
     *                          (FileNotFoundException will be nested), or when the specified
     *                          file object in not representing a file (IOException will be
     *                          nested)
     */
    void updateProfileBannerImage(InputStream banner, int width, int height, int offsetLeft, int offsetTop)
            throws TwitterException;

    /**
     * Sets one or more hex values that control the color scheme of the
     * authenticating user's profile page on twitter.com. Each parameter's value
     * must be a valid hexidecimal value, and may be either three or six
     * characters (ex: #fff or #ffffff). <br>
     * This method calls
     * http://api.twitter.com/1.1/account/update_profile_colors.json
     *
     * @param profileBackgroundColor    optional, can be null
     * @param profileTextColor          optional, can be null
     * @param profileLinkColor          optional, can be null
     * @param profileSidebarFillColor   optional, can be null
     * @param profileSidebarBorderColor optional, can be null
     * @return the updated user
     * @throws TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/post/account/update_profile_colors">POST
     * account/update_profile_colors | Twitter Developers</a>
     * @since Twitter4J 2.0.0
     */
    User updateProfileColors(String profileBackgroundColor, String profileTextColor, String profileLinkColor,
                             String profileSidebarFillColor, String profileSidebarBorderColor) throws TwitterException;

    /**
     * Updates the authenticating user's profile image. <br>
     * This method calls
     * http://api.twitter.com/1.1/account/update_profile_image.json
     *
     * @param image Must be a valid GIF, JPG, or PNG image of less than 700
     *              kilobytes in size. Images with width larger than 500 pixels
     *              will be scaled down.
     * @return the updated user
     * @throws TwitterException when Twitter service or network is unavailable,
     *                          or when the specified file is not found
     *                          (FileNotFoundException will be nested), or when the specified
     *                          file object in not representing a file (IOException will be
     *                          nested)
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image">POST
     * account/update_profile_image | Twitter Developers</a>
     * @since Twitter4J 2.1.0
     */
    User updateProfileImage(File image) throws TwitterException;

    /**
     * Updates the authenticating user's profile image. <br>
     * This method calls
     * http://api.twitter.com/1.1/account/update_profile_image.json
     *
     * @param image Must be a valid GIF, JPG, or PNG image of less than 700
     *              kilobytes in size. Images with width larger than 500 pixels
     *              will be scaled down.
     * @return the updated user
     * @throws TwitterException when Twitter service or network is unavailable,
     *                          or when the specified file is not found
     *                          (FileNotFoundException will be nested), or when the specified
     *                          file object in not representing a file (IOException will be
     *                          nested)
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image">POST
     * account/update_profile_image | Twitter Developers</a>
     * @since Twitter4J 2.1.11
     */
    User updateProfileImage(InputStream image) throws TwitterException;

    /**
     * Returns an HTTP 200 OK response code and a representation of the
     * requesting user if authentication was successful; returns a 401 status
     * code and an error message if not. Use this method to test if supplied
     * user credentials are valid. <br>
     * This method calls
     * http://api.twitter.com/1.1/account/verify_credentials.json
     *
     * @return user
     * @throws twitter4j.TwitterException when Twitter service or network is
     *                                    unavailable, or if supplied credential is wrong
     *                                    (TwitterException.getStatusCode() == 401)
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/get/account/verify_credentials">GET
     * account/verify_credentials | Twitter Developers</a>
     * @since Twitter4J 2.0.0
     */
    User verifyCredentials() throws TwitterException;
}
