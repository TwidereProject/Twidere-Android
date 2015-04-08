/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.constant;

public interface IntentConstants {

    public static final String INTENT_PACKAGE_PREFIX = "org.mariotaku.twidere.";

    public static final String INTENT_ACTION_HOME = INTENT_PACKAGE_PREFIX + "HOME";
    public static final String INTENT_ACTION_COMPOSE = INTENT_PACKAGE_PREFIX + "COMPOSE";
    public static final String INTENT_ACTION_REPLY = INTENT_PACKAGE_PREFIX + "REPLY";
    public static final String INTENT_ACTION_QUOTE = INTENT_PACKAGE_PREFIX + "QUOTE";
    public static final String INTENT_ACTION_EDIT_DRAFT = INTENT_PACKAGE_PREFIX + "EDIT_DRAFT";
    public static final String INTENT_ACTION_MENTION = INTENT_PACKAGE_PREFIX + "MENTION";
    public static final String INTENT_ACTION_REPLY_MULTIPLE = INTENT_PACKAGE_PREFIX + "REPLY_MULTIPLE";
    public static final String INTENT_ACTION_SETTINGS = INTENT_PACKAGE_PREFIX + "SETTINGS";
    public static final String INTENT_ACTION_SELECT_ACCOUNT = INTENT_PACKAGE_PREFIX + "SELECT_ACCOUNT";
    public static final String INTENT_ACTION_VIEW_MEDIA = INTENT_PACKAGE_PREFIX + "VIEW_MEDIA";
    public static final String INTENT_ACTION_FILTERS = INTENT_PACKAGE_PREFIX + "FILTERS";
    public static final String INTENT_ACTION_TWITTER_LOGIN = INTENT_PACKAGE_PREFIX + "TWITTER_LOGIN";
    public static final String INTENT_ACTION_DRAFTS = INTENT_PACKAGE_PREFIX + "DRAFTS";
    public static final String INTENT_ACTION_PICK_FILE = INTENT_PACKAGE_PREFIX + "PICK_FILE";
    public static final String INTENT_ACTION_PICK_DIRECTORY = INTENT_PACKAGE_PREFIX + "PICK_DIRECTORY";
    public static final String INTENT_ACTION_VIEW_WEBPAGE = INTENT_PACKAGE_PREFIX + "VIEW_WEBPAGE";
    public static final String INTENT_ACTION_EXTENSIONS = INTENT_PACKAGE_PREFIX + "EXTENSIONS";
    public static final String INTENT_ACTION_CUSTOM_TABS = INTENT_PACKAGE_PREFIX + "CUSTOM_TABS";
    public static final String INTENT_ACTION_ADD_TAB = INTENT_PACKAGE_PREFIX + "ADD_TAB";
    public static final String INTENT_ACTION_EDIT_TAB = INTENT_PACKAGE_PREFIX + "EDIT_TAB";
    public static final String INTENT_ACTION_EDIT_USER_PROFILE = INTENT_PACKAGE_PREFIX + "EDIT_USER_PROFILE";
    public static final String INTENT_ACTION_SERVICE_COMMAND = INTENT_PACKAGE_PREFIX + "SERVICE_COMMAND";
    public static final String INTENT_ACTION_REQUEST_PERMISSIONS = INTENT_PACKAGE_PREFIX + "REQUEST_PERMISSIONS";
    public static final String INTENT_ACTION_SELECT_USER_LIST = INTENT_PACKAGE_PREFIX + "SELECT_USER_LIST";
    public static final String INTENT_ACTION_SELECT_USER = INTENT_PACKAGE_PREFIX + "SELECT_USER";
    public static final String INTENT_ACTION_COMPOSE_TAKE_PHOTO = INTENT_PACKAGE_PREFIX + "COMPOSE_TAKE_PHOTO";
    public static final String INTENT_ACTION_COMPOSE_PICK_IMAGE = INTENT_PACKAGE_PREFIX + "COMPOSE_PICK_IMAGE";
    public static final String INTENT_ACTION_TAKE_PHOTO = INTENT_PACKAGE_PREFIX + "TAKE_PHOTO";
    public static final String INTENT_ACTION_PICK_IMAGE = INTENT_PACKAGE_PREFIX + "PICK_IMAGE";
    public static final String INTENT_ACTION_HIDDEN_SETTINGS_ENTRY = INTENT_PACKAGE_PREFIX + "HIDDEN_SETTINGS_ENTRY";

    public static final String INTENT_ACTION_EXTENSION_EDIT_IMAGE = INTENT_PACKAGE_PREFIX + "EXTENSION_EDIT_IMAGE";
    public static final String INTENT_ACTION_EXTENSION_UPLOAD = INTENT_PACKAGE_PREFIX + "EXTENSION_UPLOAD";
    public static final String INTENT_ACTION_EXTENSION_OPEN_STATUS = INTENT_PACKAGE_PREFIX + "EXTENSION_OPEN_STATUS";
    public static final String INTENT_ACTION_EXTENSION_OPEN_USER = INTENT_PACKAGE_PREFIX + "EXTENSION_OPEN_USER";
    public static final String INTENT_ACTION_EXTENSION_OPEN_USER_LIST = INTENT_PACKAGE_PREFIX
            + "EXTENSION_OPEN_USER_LIST";
    public static final String INTENT_ACTION_EXTENSION_COMPOSE = INTENT_PACKAGE_PREFIX + "EXTENSION_COMPOSE";
    public static final String INTENT_ACTION_EXTENSION_UPLOAD_MEDIA = INTENT_PACKAGE_PREFIX + "EXTENSION_UPLOAD_MEDIA";
    public static final String INTENT_ACTION_EXTENSION_SHORTEN_STATUS = INTENT_PACKAGE_PREFIX
            + "EXTENSION_SHORTEN_STATUS";
    public static final String INTENT_ACTION_EXTENSION_SYNC_TIMELINE = INTENT_PACKAGE_PREFIX
            + "EXTENSION_SYNC_TIMELINE";
    public static final String INTENT_ACTION_EXTENSION_SETTINGS = INTENT_PACKAGE_PREFIX + "EXTENSION_SETTINGS";

    public static final String INTENT_ACTION_UPDATE_STATUS = INTENT_PACKAGE_PREFIX + "UPDATE_STATUS";
    public static final String INTENT_ACTION_SEND_DIRECT_MESSAGE = INTENT_PACKAGE_PREFIX + "SEND_DIRECT_MESSAGE";
    public static final String INTENT_ACTION_DISCARD_DRAFT = INTENT_PACKAGE_PREFIX + "DISCARD_DRAFT";
    public static final String INTENT_ACTION_PICK_ACTIVITY = "org.mariotaku.twidere.PICK_ACTIVITY";

    public static final String INTENT_ACTION_PEBBLE_NOTIFICATION = "com.getpebble.action.SEND_NOTIFICATION";

    public static final String BROADCAST_NOTIFICATION_DELETED = INTENT_PACKAGE_PREFIX + "NOTIFICATION_DELETED";
    public static final String BROADCAST_USER_LIST_DETAILS_UPDATED = INTENT_PACKAGE_PREFIX
            + "USER_LIST_DETAILS_UPDATED";
    public static final String BROADCAST_FRIENDSHIP_ACCEPTED = INTENT_PACKAGE_PREFIX + "FRIENDSHIP_ACCEPTED";
    public static final String BROADCAST_FRIENDSHIP_DENIED = INTENT_PACKAGE_PREFIX + "FRIENDSHIP_DENIED";

    public static final String BROADCAST_USER_LIST_MEMBERS_DELETED = INTENT_PACKAGE_PREFIX + "USER_LIST_MEMBER_DELETED";
    public static final String BROADCAST_USER_LIST_MEMBERS_ADDED = INTENT_PACKAGE_PREFIX + "USER_LIST_MEMBER_ADDED";
    public static final String BROADCAST_USER_LIST_SUBSCRIBED = INTENT_PACKAGE_PREFIX + "USER_LIST_SUBSRCIBED";
    public static final String BROADCAST_USER_LIST_UNSUBSCRIBED = INTENT_PACKAGE_PREFIX + "USER_LIST_UNSUBSCRIBED";
    public static final String BROADCAST_USER_LIST_CREATED = INTENT_PACKAGE_PREFIX + "USER_LIST_CREATED";
    public static final String BROADCAST_USER_LIST_DELETED = INTENT_PACKAGE_PREFIX + "USER_LIST_DELETED";
    public static final String BROADCAST_FILTERS_UPDATED = INTENT_PACKAGE_PREFIX + "FILTERS_UPDATED";
    public static final String BROADCAST_REFRESH_HOME_TIMELINE = INTENT_PACKAGE_PREFIX + "REFRESH_HOME_TIMELINE";
    public static final String BROADCAST_REFRESH_MENTIONS = INTENT_PACKAGE_PREFIX + "REFRESH_MENTIONS";
    public static final String BROADCAST_REFRESH_DIRECT_MESSAGES = INTENT_PACKAGE_PREFIX + "REFRESH_DIRECT_MESSAGES";
    public static final String BROADCAST_REFRESH_TRENDS = INTENT_PACKAGE_PREFIX + "REFRESH_TRENDS";
    public static final String BROADCAST_RESCHEDULE_HOME_TIMELINE_REFRESHING = INTENT_PACKAGE_PREFIX
            + "RESCHEDULE_HOME_TIMELINE_REFRESHING";
    public static final String BROADCAST_RESCHEDULE_MENTIONS_REFRESHING = INTENT_PACKAGE_PREFIX
            + "RESCHEDULE_MENTIONS_REFRESHING";
    public static final String BROADCAST_RESCHEDULE_DIRECT_MESSAGES_REFRESHING = INTENT_PACKAGE_PREFIX
            + "RESCHEDULE_DIRECT_MESSAGES_REFRESHING";
    public static final String BROADCAST_RESCHEDULE_TRENDS_REFRESHING = INTENT_PACKAGE_PREFIX
            + "RESCHEDULE_TRENDS_REFRESHING";
    public static final String BROADCAST_MULTI_BLOCKSTATE_CHANGED = INTENT_PACKAGE_PREFIX + "MULTI_BLOCKSTATE_CHANGED";
    public static final String BROADCAST_MULTI_MUTESTATE_CHANGED = INTENT_PACKAGE_PREFIX + "MULTI_MUTESTATE_CHANGED";
    public static final String BROADCAST_HOME_ACTIVITY_ONCREATE = INTENT_PACKAGE_PREFIX + "HOME_ACTIVITY_ONCREATE";
    public static final String BROADCAST_HOME_ACTIVITY_ONSTART = INTENT_PACKAGE_PREFIX + "HOME_ACTIVITY_ONSTART";
    public static final String BROADCAST_HOME_ACTIVITY_ONRESUME = INTENT_PACKAGE_PREFIX + "HOME_ACTIVITY_ONRESUME";
    public static final String BROADCAST_HOME_ACTIVITY_ONPAUSE = INTENT_PACKAGE_PREFIX + "HOME_ACTIVITY_ONPAUSE";
    public static final String BROADCAST_HOME_ACTIVITY_ONSTOP = INTENT_PACKAGE_PREFIX + "HOME_ACTIVITY_ONSTOP";
    public static final String BROADCAST_HOME_ACTIVITY_ONDESTROY = INTENT_PACKAGE_PREFIX + "HOME_ACTIVITY_ONDESTROY";
    public static final String BROADCAST_DATABASE_READY = INTENT_PACKAGE_PREFIX + "DATABASE_READY";

    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_URI = "uri";
    public static final String EXTRA_URI_ORIG = "uri_orig";
    public static final String EXTRA_MENTIONS = "mentions";
    public static final String EXTRA_ACCOUNT_ID = "account_id";
    public static final String EXTRA_ACCOUNT_IDS = "account_ids";
    public static final String EXTRA_PAGE = "page";
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_QUERY = "query";
    public static final String EXTRA_QUERY_TYPE = "query_type";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_USER_IDS = "user_ids";
    public static final String EXTRA_LIST_ID = "list_id";
    public static final String EXTRA_MAX_ID = "max_id";
    public static final String EXTRA_MAX_IDS = "max_ids";
    public static final String EXTRA_SINCE_ID = "since_id";
    public static final String EXTRA_SINCE_IDS = "since_ids";
    public static final String EXTRA_STATUS_ID = "status_id";
    public static final String EXTRA_SCREEN_NAME = "screen_name";
    public static final String EXTRA_SCREEN_NAMES = "screen_names";
    public static final String EXTRA_LIST_NAME = "list_name";
    public static final String EXTRA_DESCRIPTION = "description";
    public static final String EXTRA_IN_REPLY_TO_ID = "in_reply_to_id";
    public static final String EXTRA_IN_REPLY_TO_NAME = "in_reply_to_name";
    public static final String EXTRA_IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
    public static final String EXTRA_TEXT = "text";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TYPE = "type";
    //	public static final String EXTRA_SUCCEED = "succeed";
    public static final String EXTRA_IDS = "ids";
    public static final String EXTRA_REFRESH_IDS = "refresh_ids";
    public static final String EXTRA_IS_SHARE = "is_share";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_STATUS_JSON = "status_json";
    public static final String EXTRA_STATUSES = "statuses";
    public static final String EXTRA_DRAFT = "draft";
    public static final String EXTRA_FAVORITED = "favorited";
    public static final String EXTRA_RETWEETED = "retweeted";
    public static final String EXTRA_FILENAME = "filename";
    public static final String EXTRA_FILE_SOURCE = "file_source";
    public static final String EXTRA_FILE_EXTENSIONS = "file_extensions";
    public static final String EXTRA_ITEMS_INSERTED = "items_inserted";
    public static final String EXTRA_INITIAL_TAB = "initial_tab";
    public static final String EXTRA_NOTIFICATION_ID = "notification_id";
    public static final String EXTRA_NOTIFICATION_ACCOUNT = "notification_account";
    public static final String EXTRA_FROM_NOTIFICATION = "from_notification";
    public static final String EXTRA_IS_PUBLIC = "is_public";
    public static final String EXTRA_USER = "user";
    public static final String EXTRA_USERS = "users";
    public static final String EXTRA_USER_LIST = "user_list";
    public static final String EXTRA_APPEND_TEXT = "append_text";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_TEXT1 = "text1";
    public static final String EXTRA_TEXT2 = "text2";
    public static final String EXTRA_POSITION = "position";
    public static final String EXTRA_ARGUMENTS = "arguments";
    public static final String EXTRA_ICON = "icon";
    public static final String EXTRA_ID = "id";
    public static final String EXTRA_RESID = "resid";
    public static final String EXTRA_SETTINGS_INTENT_ACTION = "settings_intent_action";
    public static final String EXTRA_IMAGE_URI = "image_uri";
    public static final String EXTRA_ATTACHED_IMAGE_TYPE = "attached_image_type";
    public static final String EXTRA_ACTIVATED_ONLY = "activated_only";
    public static final String EXTRA_TAB_POSITION = "tab_position";
    public static final String EXTRA_HAS_RUNNING_TASK = "has_running_task";
    public static final String EXTRA_OAUTH_VERIFIER = "oauth_verifier";
    public static final String EXTRA_REQUEST_TOKEN = "request_token";
    public static final String EXTRA_REQUEST_TOKEN_SECRET = "request_token_secret";
    public static final String EXTRA_OMIT_INTENT_EXTRA = "omit_intent_extra";
    public static final String EXTRA_COMMAND = "command";
    public static final String EXTRA_WIDTH = "width";
    public static final String EXTRA_ALLOW_SELECT_NONE = "allow_select_none";
    public static final String EXTRA_SINGLE_SELECTION = "single_selection";
    public static final String EXTRA_OAUTH_ONLY = "oauth_only";
    public static final String EXTRA_PERMISSIONS = "permissions";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_URL = "url";
    public static final String EXTRA_NEXT_CURSOR = "next_cursor";
    public static final String EXTRA_PREV_CURSOR = "prev_cursor";
    public static final String EXTRA_EXTRA_INTENT = "extra_intent";
    public static final String EXTRA_IS_MY_ACCOUNT = "is_my_account";
    public static final String EXTRA_TAB_TYPE = "tab_type";
    public static final String EXTRA_ACCOUNT = "account";
    public static final String EXTRA_ACTIVITY_SCREENSHOT_ID = "activity_screenshot_id";
    public static final String EXTRA_COLOR = "color";
    public static final String EXTRA_ALPHA_SLIDER = "alpha_slider";
    public static final String EXTRA_OPEN_ACCOUNTS_DRAWER = "open_accounts_drawer";
    public static final String EXTRA_RECIPIENT_ID = "recipient_id";
    public static final String EXTRA_OFFICIAL_KEY_ONLY = "official_key_only";
    public static final String EXTRA_SEARCH_ID = "search_id";
    public static final String EXTRA_CLEAR_BUTTON = "clear_button";
    public static final String EXTRA_PATH = "path";
    public static final String EXTRA_ACTION = "action";
    public static final String EXTRA_FLAGS = "flags";
    public static final String EXTRA_INTENT = "intent";
    public static final String EXTRA_BLACKLIST = "blacklist";
    public static final String EXTRA_MEDIA = "media";
    public static final String EXTRA_CURRENT_MEDIA = "current_media";
    public static final String EXTRA_EXTRAS = "extras";
    public static final String EXTRA_MY_FOLLOWING_ONLY = "my_following_only";
    public static final String EXTRA_RESTART_ACTIVITY = "restart_activity";
    public static final String EXTRA_FROM_USER = "from_user";
    public static final String EXTRA_SHOW_MEDIA_PREVIEW = "show_media_preview";
    public static final String EXTRA_SHOW_EXTRA_TYPE = "show_extra_type";
    public static final String EXTRA_BITMAP = "bitmap";
    public static final String EXTRA_SOURCE = "source";
    public static final String EXTRA_DESTINATION = "destination";

}
