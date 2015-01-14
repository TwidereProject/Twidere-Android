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

package org.mariotaku.twidere;

import org.mariotaku.twidere.constant.IntentConstants;
import org.mariotaku.twidere.constant.SharedPreferenceConstants;

/**
 * Public constants for both Twidere app and its extensions
 *
 * @author mariotaku
 */
public interface TwidereConstants extends SharedPreferenceConstants, IntentConstants {

    public static final String APP_NAME = "Twidere";
    public static final String APP_PACKAGE_NAME = "org.mariotaku.twidere";
    public static final String APP_PROJECT_URL = "https://github.com/mariotaku/twidere";
    public static final String APP_PROJECT_EMAIL = "twidere.project@gmail.com";

    public static final String LOGTAG = APP_NAME;

    public static final String USER_NICKNAME_PREFERENCES_NAME = "user_nicknames";
    public static final String USER_COLOR_PREFERENCES_NAME = "user_colors";
    public static final String HOST_MAPPING_PREFERENCES_NAME = "host_mapping";
    public static final String MESSAGE_DRAFTS_PREFERENCES_NAME = "message_drafts";
    public static final String SHARED_PREFERENCES_NAME = "preferences";
    public static final String PERMISSION_PREFERENCES_NAME = "app_permissions";
    public static final String SILENT_NOTIFICATIONS_PREFERENCE_NAME = "silent_notifications";
    public static final String TIMELINE_POSITIONS_PREFERENCES_NAME = "timeline_positions";
    public static final String ACCOUNT_PREFERENCES_NAME_PREFIX = "account_preferences_";

    public static final String TWITTER_CONSUMER_KEY = "uAFVpMhBntJutfVj6abfA";
    public static final String TWITTER_CONSUMER_SECRET = "JARXkJTfxo0F8MyctYy9bUmrLISjo8vXAHsZHYuk2E";
    public static final String TWITTER_CONSUMER_KEY_2 = "UyaS0xmUQXKiJ48vZP4dXQ";
    public static final String TWITTER_CONSUMER_SECRET_2 = "QlYVMWA751Dl5yNve41CNEN46GV4nxk57FmLeAXAV0";
    public static final String TWITTER_CONSUMER_KEY_3 = "YljS7Zmbw3JkouhZkxCINAsn6";
    public static final String TWITTER_CONSUMER_SECRET_3 = "AYrXN6eAJ3Luf9o5zS4Flq2bSBhrB6A9eioI8JENRx8HMh9YuS";

    public static final String SCHEME_HTTP = "http";
    public static final String SCHEME_HTTPS = "https";
    public static final String SCHEME_CONTENT = "content";
    public static final String SCHEME_TWIDERE = "twidere";

    public static final String PROTOCOL_HTTP = SCHEME_HTTP + "://";
    public static final String PROTOCOL_HTTPS = SCHEME_HTTPS + "://";
    public static final String PROTOCOL_CONTENT = SCHEME_CONTENT + "://";
    public static final String PROTOCOL_TWIDERE = SCHEME_TWIDERE + "://";

    public static final String AUTHORITY_USER = "user";
    public static final String AUTHORITY_USERS = "users";
    public static final String AUTHORITY_USER_TIMELINE = "user_timeline";
    public static final String AUTHORITY_USER_MEDIA_TIMELINE = "user_media_timeline";
    public static final String AUTHORITY_USER_FAVORITES = "user_favorites";
    public static final String AUTHORITY_USER_FOLLOWERS = "user_followers";
    public static final String AUTHORITY_USER_FRIENDS = "user_friends";
    public static final String AUTHORITY_USER_BLOCKS = "user_blocks";
    public static final String AUTHORITY_STATUS = "status";
    public static final String AUTHORITY_STATUSES = "statuses";
    public static final String AUTHORITY_DIRECT_MESSAGES_CONVERSATION = "direct_messages_conversation";
    public static final String AUTHORITY_SEARCH = "search";
    public static final String AUTHORITY_MAP = "map";
    public static final String AUTHORITY_USER_LIST = "user_list";
    public static final String AUTHORITY_USER_LIST_TIMELINE = "user_list_timeline";
    public static final String AUTHORITY_USER_LIST_MEMBERS = "user_list_members";
    public static final String AUTHORITY_USER_LIST_SUBSCRIBERS = "user_list_subscribers";
    public static final String AUTHORITY_USER_LIST_MEMBERSHIPS = "user_list_memberships";
    public static final String AUTHORITY_USER_LISTS = "user_lists";
    public static final String AUTHORITY_USERS_RETWEETED_STATUS = "users_retweeted_status";
    public static final String AUTHORITY_SAVED_SEARCHES = "saved_searches";
    public static final String AUTHORITY_SEARCH_USERS = "search_users";
    public static final String AUTHORITY_SEARCH_TWEETS = "search_tweets";
    public static final String AUTHORITY_TRENDS = "trends";
    public static final String AUTHORITY_USER_MENTIONS = "user_mentions";
    public static final String AUTHORITY_ACTIVITIES_ABOUT_ME = "activities_about_me";
    public static final String AUTHORITY_ACTIVITIES_BY_FRIENDS = "activities_by_friends";
    public static final String AUTHORITY_INCOMING_FRIENDSHIPS = "incoming_friendships";
    public static final String AUTHORITY_STATUS_RETWEETERS = "status_retweeters";
    public static final String AUTHORITY_STATUS_FAVORITERS = "status_favoriters";
    public static final String AUTHORITY_STATUS_REPLIES = "status_replies";
    public static final String AUTHORITY_RETWEETS_OF_ME = "retweets_of_me";
    public static final String AUTHORITY_MUTES_USERS = "mutes_users";

    public static final String QUERY_PARAM_ACCOUNT_ID = "account_id";
    public static final String QUERY_PARAM_ACCOUNT_IDS = "account_ids";
    public static final String QUERY_PARAM_ACCOUNT_NAME = "account_name";
    public static final String QUERY_PARAM_STATUS_ID = "status_id";
    public static final String QUERY_PARAM_USER_ID = "user_id";
    public static final String QUERY_PARAM_LIST_ID = "list_id";
    public static final String QUERY_PARAM_SCREEN_NAME = "screen_name";
    public static final String QUERY_PARAM_LIST_NAME = "list_name";
    public static final String QUERY_PARAM_QUERY = "query";
    public static final String QUERY_PARAM_TYPE = "type";
    public static final String QUERY_PARAM_VALUE_USERS = "users";
    public static final String QUERY_PARAM_VALUE_TWEETS = "tweets";
    public static final String QUERY_PARAM_NOTIFY = "notify";
    public static final String QUERY_PARAM_LAT = "lat";
    public static final String QUERY_PARAM_LNG = "lng";
    public static final String QUERY_PARAM_URL = "url";
    public static final String QUERY_PARAM_NAME = "name";
    public static final String QUERY_PARAM_FINISH_ONLY = "finish_only";
    public static final String QUERY_PARAM_NEW_ITEMS_COUNT = "new_items_count";
    public static final String QUERY_PARAM_RECIPIENT_ID = "recipient_id";

    public static final String DEFAULT_PROTOCOL = PROTOCOL_HTTPS;

    public static final String OAUTH_CALLBACK_OOB = "oob";
    public static final String OAUTH_CALLBACK_URL = PROTOCOL_TWIDERE + "com.twitter.oauth/";

    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_PICK_IMAGE = 2;
    public static final int REQUEST_SELECT_ACCOUNT = 3;
    public static final int REQUEST_COMPOSE = 4;
    public static final int REQUEST_EDIT_API = 5;
    public static final int REQUEST_BROWSER_SIGN_IN = 6;
    public static final int REQUEST_SET_COLOR = 7;
    public static final int REQUEST_SAVE_FILE = 8;
    public static final int REQUEST_EDIT_IMAGE = 9;
    public static final int REQUEST_EXTENSION_COMPOSE = 10;
    public static final int REQUEST_ADD_TAB = 11;
    public static final int REQUEST_EDIT_TAB = 12;
    public static final int REQUEST_PICK_FILE = 13;
    public static final int REQUEST_PICK_DIRECTORY = 14;
    public static final int REQUEST_ADD_TO_LIST = 15;
    public static final int REQUEST_SELECT_USER = 16;
    public static final int REQUEST_SELECT_USER_LIST = 17;
    public static final int REQUEST_PICK_ACTIVITY = 18;
    public static final int REQUEST_SETTINGS = 19;
    public static final int REQUEST_OPEN_DOCUMENT = 20;
    public static final int REQUEST_SWIPEBACK_ACTIVITY = 101;

    public static final int TABLE_ID_ACCOUNTS = 1;
    public static final int TABLE_ID_STATUSES = 12;
    public static final int TABLE_ID_MENTIONS = 13;
    public static final int TABLE_ID_DIRECT_MESSAGES = 21;
    public static final int TABLE_ID_DIRECT_MESSAGES_INBOX = 22;
    public static final int TABLE_ID_DIRECT_MESSAGES_OUTBOX = 23;
    public static final int TABLE_ID_DIRECT_MESSAGES_CONVERSATION = 24;
    public static final int TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME = 25;
    public static final int TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES = 26;
    public static final int TABLE_ID_FILTERED_USERS = 31;
    public static final int TABLE_ID_FILTERED_KEYWORDS = 32;
    public static final int TABLE_ID_FILTERED_SOURCES = 33;
    public static final int TABLE_ID_FILTERED_LINKS = 34;
    public static final int TABLE_ID_TRENDS_LOCAL = 41;
    public static final int TABLE_ID_SAVED_SEARCHES = 42;
    public static final int TABLE_ID_SEARCH_HISTORY = 43;
    public static final int TABLE_ID_DRAFTS = 51;
    public static final int TABLE_ID_TABS = 52;
    public static final int TABLE_ID_CACHED_USERS = 61;
    public static final int TABLE_ID_CACHED_STATUSES = 62;
    public static final int TABLE_ID_CACHED_HASHTAGS = 63;
    public static final int TABLE_ID_CACHED_RELATIONSHIPS = 64;
    public static final int VIRTUAL_TABLE_ID_DATABASE_READY = 100;
    public static final int VIRTUAL_TABLE_ID_NOTIFICATIONS = 101;
    public static final int VIRTUAL_TABLE_ID_PREFERENCES = 102;
    public static final int VIRTUAL_TABLE_ID_ALL_PREFERENCES = 103;
    public static final int VIRTUAL_TABLE_ID_PERMISSIONS = 104;
    public static final int VIRTUAL_TABLE_ID_DNS = 105;
    public static final int VIRTUAL_TABLE_ID_CACHED_IMAGES = 106;
    public static final int VIRTUAL_TABLE_ID_CACHE_FILES = 107;
    public static final int VIRTUAL_TABLE_ID_UNREAD_COUNTS = 108;
    public static final int VIRTUAL_TABLE_ID_UNREAD_COUNTS_BY_TYPE = 109;
    public static final int VIRTUAL_TABLE_ID_CACHED_USERS_WITH_RELATIONSHIP = 121;
    public static final int VIRTUAL_TABLE_ID_CACHED_USERS_WITH_SCORE = 122;
    public static final int VIRTUAL_TABLE_ID_DRAFTS_UNSENT = 131;

    public static final int NOTIFICATION_ID_HOME_TIMELINE = 1;
    public static final int NOTIFICATION_ID_MENTIONS_TIMELINE = 2;
    public static final int NOTIFICATION_ID_DIRECT_MESSAGES = 3;
    public static final int NOTIFICATION_ID_DRAFTS = 4;
    public static final int NOTIFICATION_ID_DATA_PROFILING = 5;
    public static final int NOTIFICATION_ID_UPDATE_STATUS = 101;
    public static final int NOTIFICATION_ID_SEND_DIRECT_MESSAGE = 102;

    public static final String ICON_SPECIAL_TYPE_CUSTOMIZE = "_customize";

    public static final String TASK_TAG_GET_HOME_TIMELINE = "get_home_tomeline";
    public static final String TASK_TAG_GET_MENTIONS = "get_mentions";
    public static final String TASK_TAG_GET_SENT_DIRECT_MESSAGES = "get_sent_direct_messages";
    public static final String TASK_TAG_GET_RECEIVED_DIRECT_MESSAGES = "get_received_direct_messages";
    public static final String TASK_TAG_GET_TRENDS = "get_trends";
    public static final String TASK_TAG_STORE_HOME_TIMELINE = "store_home_tomeline";
    public static final String TASK_TAG_STORE_MENTIONS = "store_mentions";
    public static final String TASK_TAG_STORE_SENT_DIRECT_MESSAGES = "store_sent_direct_messages";
    public static final String TASK_TAG_STORE_RECEIVED_DIRECT_MESSAGES = "store_received_direct_messages";
    public static final String TASK_TAG_STORE_TRENDS = "store_trends";

    public static final String SERVICE_COMMAND_REFRESH_ALL = "refresh_all";
    public static final String SERVICE_COMMAND_GET_HOME_TIMELINE = "get_home_timeline";
    public static final String SERVICE_COMMAND_GET_MENTIONS = "get_mentions";
    public static final String SERVICE_COMMAND_GET_SENT_DIRECT_MESSAGES = "get_sent_direct_messages";
    public static final String SERVICE_COMMAND_GET_RECEIVED_DIRECT_MESSAGES = "get_received_direct_messages";

    public static final String METADATA_KEY_EXTENSION = "org.mariotaku.twidere.extension";
    public static final String METADATA_KEY_EXTENSION_PERMISSIONS = "org.mariotaku.twidere.extension.permissions";
    public static final String METADATA_KEY_EXTENSION_SETTINGS = "org.mariotaku.twidere.extension.settings";
    public static final String METADATA_KEY_EXTENSION_ICON = "org.mariotaku.twidere.extension.icon";
    public static final String METADATA_KEY_EXTENSION_USE_JSON = "org.mariotaku.twidere.extension.use_json";

    public static final char SEPARATOR_PERMISSION = '|';
    public static final String SEPARATOR_PERMISSION_REGEX = "\\" + SEPARATOR_PERMISSION;

    public static final String PERMISSION_DENIED = "denied";
    public static final String PERMISSION_REFRESH = "refresh";
    public static final String PERMISSION_READ = "read";
    public static final String PERMISSION_WRITE = "write";
    public static final String PERMISSION_DIRECT_MESSAGES = "direct_messages";
    public static final String PERMISSION_ACCOUNTS = "accounts";
    public static final String PERMISSION_PREFERENCES = "preferences";

    public static final String TAB_TYPE_HOME_TIMELINE = "home_timeline";
    public static final String TAB_TYPE_MENTIONS_TIMELINE = "mentions_timeline";
    public static final String TAB_TYPE_TRENDS_SUGGESTIONS = "trends_suggestions";
    public static final String TAB_TYPE_DIRECT_MESSAGES = "direct_messages";
    public static final String TAB_TYPE_FAVORITES = "favorites";
    public static final String TAB_TYPE_USER_TIMELINE = "user_timeline";
    public static final String TAB_TYPE_SEARCH_STATUSES = "search_statuses";
    public static final String TAB_TYPE_LIST_TIMELINE = "list_timeline";
    public static final String TAB_TYPE_ACTIVITIES_ABOUT_ME = "activities_about_me";
    public static final String TAB_TYPE_ACTIVITIES_BY_FRIENDS = "activities_by_friends";
    public static final String TAB_TYPE_RETWEETS_OF_ME = "retweets_of_me";
    public static final String TAB_TYPE_STAGGERED_HOME_TIMELINE = "staggered_home_timeline";

    public static final int TWITTER_MAX_IMAGE_SIZE = 3145728;
    public static final int TWITTER_MAX_IMAGE_WIDTH = 1024;
    public static final int TWITTER_MAX_IMAGE_HEIGHT = 2048;

}
