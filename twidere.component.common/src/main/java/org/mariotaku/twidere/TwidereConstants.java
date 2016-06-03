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

import android.content.ContentResolver;

import org.mariotaku.twidere.constant.CompatibilityConstants;
import org.mariotaku.twidere.constant.IntentConstants;
import org.mariotaku.twidere.constant.SharedPreferenceConstants;

/**
 * Public constants for both Twidere app and its extensions
 *
 * @author mariotaku
 */
public interface TwidereConstants extends SharedPreferenceConstants, IntentConstants, CompatibilityConstants {

    String TWIDERE_APP_NAME = "Twidere";
    String TWIDERE_PROJECT_URL = "https://github.com/mariotaku/twidere";
    String TWIDERE_PROJECT_EMAIL = "twidere.project@gmail.com";
    String TWIDERE_PACKAGE_NAME = "org.mariotaku.twidere";

    String LOGTAG = TWIDERE_APP_NAME;

    String USER_NICKNAME_PREFERENCES_NAME = "user_nicknames";
    String USER_COLOR_PREFERENCES_NAME = "user_colors";
    String HOST_MAPPING_PREFERENCES_NAME = "host_mapping";
    String MESSAGE_DRAFTS_PREFERENCES_NAME = "message_drafts";
    String SHARED_PREFERENCES_NAME = "preferences";
    String PERMISSION_PREFERENCES_NAME = "app_permissions";
    String TIMELINE_POSITIONS_PREFERENCES_NAME = "timeline_positions";
    String ACCOUNT_PREFERENCES_NAME_PREFIX = "account_preferences_";
    String KEYBOARD_SHORTCUTS_PREFERENCES_NAME = "keyboard_shortcuts_preferences";

    String TWITTER_CONSUMER_KEY_LEGACY = "eGhsWfndW5tqIDAR10BxhYCZX";
    String TWITTER_CONSUMER_SECRET_LEGACY = "CVB2nVrXUn7xUVjzVHph2Mr5b4ynR9iDZMqXyKwBKqHInZixN9";
    String TWITTER_CONSUMER_KEY = "eGhsWfndW5tqIDAR10BxhYCZX";
    String TWITTER_CONSUMER_SECRET = "CVB2nVrXUn7xUVjzVHph2Mr5b4ynR9iDZMqXyKwBKqHInZixN9";

    String DEFAULT_TWITTER_API_URL_FORMAT = "https://[DOMAIN.]twitter.com/";

    String SCHEME_HTTP = "http";
    String SCHEME_HTTPS = "https";
    String SCHEME_CONTENT = ContentResolver.SCHEME_CONTENT;
    String SCHEME_TWIDERE = "twidere";
    String SCHEME_DATA = "data";

    String PROTOCOL_HTTP = SCHEME_HTTP + "://";
    String PROTOCOL_HTTPS = SCHEME_HTTPS + "://";
    String PROTOCOL_CONTENT = SCHEME_CONTENT + "://";
    String PROTOCOL_TWIDERE = SCHEME_TWIDERE + "://";

    String AUTHORITY_TWIDERE_SHARE = "twidere.share";
    String AUTHORITY_TWIDERE_CACHE = "twidere.cache";

    String AUTHORITY_USER = "user";
    String AUTHORITY_ITEMS = "items";
    String AUTHORITY_USER_TIMELINE = "user_timeline";
    String AUTHORITY_USER_MEDIA_TIMELINE = "user_media_timeline";
    String AUTHORITY_USER_FAVORITES = "user_favorites";
    String AUTHORITY_USER_FOLLOWERS = "user_followers";
    String AUTHORITY_USER_FRIENDS = "user_friends";
    String AUTHORITY_USER_BLOCKS = "user_blocks";
    String AUTHORITY_STATUS = "status";
    String AUTHORITY_PUBLIC_TIMELINE = "public_timeline";
    String AUTHORITY_DIRECT_MESSAGES_CONVERSATION = "direct_messages_conversation";
    String AUTHORITY_DIRECT_MESSAGES = "direct_messages";
    String AUTHORITY_SEARCH = "search";
    String AUTHORITY_MAP = "map";
    String AUTHORITY_SCHEDULED_STATUSES = "scheduled_statuses";
    String AUTHORITY_USER_LIST = "user_list";
    String AUTHORITY_USER_LIST_TIMELINE = "user_list_timeline";
    String AUTHORITY_GROUP = "group";
    String AUTHORITY_GROUP_TIMELINE = "group_timeline";
    String AUTHORITY_USER_LIST_MEMBERS = "user_list_members";
    String AUTHORITY_USER_LIST_SUBSCRIBERS = "user_list_subscribers";
    String AUTHORITY_USER_LIST_MEMBERSHIPS = "user_list_memberships";
    String AUTHORITY_USER_LISTS = "user_lists";
    String AUTHORITY_USER_GROUPS = "user_groups";
    String AUTHORITY_USERS_RETWEETED_STATUS = "users_retweeted_status";
    String AUTHORITY_SAVED_SEARCHES = "saved_searches";
    String AUTHORITY_SEARCH_USERS = "search_users";
    String AUTHORITY_SEARCH_TWEETS = "search_tweets";
    String AUTHORITY_TRENDS = "trends";
    String AUTHORITY_USER_MENTIONS = "user_mentions";
    String AUTHORITY_INCOMING_FRIENDSHIPS = "incoming_friendships";
    String AUTHORITY_STATUS_RETWEETERS = "status_retweeters";
    String AUTHORITY_STATUS_FAVORITERS = "status_favoriters";
    String AUTHORITY_RETWEETS_OF_ME = "retweets_of_me";
    String AUTHORITY_MUTES_USERS = "mutes_users";
    String AUTHORITY_INTERACTIONS = "interactions";
    String AUTHORITY_ACCOUNTS = "accounts";
    String AUTHORITY_DRAFTS = "drafts";
    String AUTHORITY_FILTERS = "filters";
    String AUTHORITY_PROFILE_EDITOR = "profile_editor";

    String QUERY_PARAM_ACCOUNT_KEY = "account_key";
    String QUERY_PARAM_ACCOUNT_HOST = "account_host";
    String QUERY_PARAM_ACCOUNT_NAME = "account_name";
    String QUERY_PARAM_STATUS_ID = "status_id";
    String QUERY_PARAM_USER_KEY = "user_key";
    String QUERY_PARAM_LIST_ID = "list_id";
    String QUERY_PARAM_GROUP_ID = "group_id";
    String QUERY_PARAM_GROUP_NAME = "group_name";
    String QUERY_PARAM_SCREEN_NAME = "screen_name";
    String QUERY_PARAM_LIST_NAME = "list_name";
    String QUERY_PARAM_QUERY = "query";
    String QUERY_PARAM_TYPE = "type";
    String QUERY_PARAM_VALUE_USERS = "users";
    String QUERY_PARAM_VALUE_TWEETS = "tweets";
    String QUERY_PARAM_NOTIFY = "notify";
    String QUERY_PARAM_LAT = "lat";
    String QUERY_PARAM_LNG = "lng";
    String QUERY_PARAM_URL = "url";
    String QUERY_PARAM_NAME = "name";
    String QUERY_PARAM_FINISH_ONLY = "finish_only";
    String QUERY_PARAM_NEW_ITEMS_COUNT = "new_items_count";
    String QUERY_PARAM_RECIPIENT_ID = "recipient_id";
    String QUERY_PARAM_READ_POSITION = "param_read_position";
    String QUERY_PARAM_READ_POSITIONS = "param_read_positions";
    String QUERY_PARAM_LIMIT = "limit";
    String QUERY_PARAM_EXTRA = "extra";
    String QUERY_PARAM_TIMESTAMP = "timestamp";
    String QUERY_PARAM_FROM_NOTIFICATION = "from_notification";
    String QUERY_PARAM_NOTIFICATION_TYPE = "notification_type";
    String QUERY_PARAM_PREVIEW = "preview";

    String DEFAULT_PROTOCOL = PROTOCOL_HTTPS;

    String OAUTH_CALLBACK_OOB = "oob";
    String OAUTH_CALLBACK_URL = PROTOCOL_TWIDERE + "com.twitter.oauth/";

    int REQUEST_TAKE_PHOTO = 1;
    int REQUEST_PICK_IMAGE = 2;
    int REQUEST_SELECT_ACCOUNT = 3;
    int REQUEST_COMPOSE = 4;
    int REQUEST_EDIT_API = 5;
    int REQUEST_BROWSER_SIGN_IN = 6;
    int REQUEST_SET_COLOR = 7;
    int REQUEST_SET_NICKNAME = 8;
    int REQUEST_EDIT_IMAGE = 9;
    int REQUEST_EXTENSION_COMPOSE = 10;
    int REQUEST_ADD_TAB = 11;
    int REQUEST_EDIT_TAB = 12;
    int REQUEST_PICK_FILE = 13;
    int REQUEST_PICK_DIRECTORY = 14;
    int REQUEST_ADD_TO_LIST = 15;
    int REQUEST_SELECT_USER = 16;
    int REQUEST_SELECT_USER_LIST = 17;
    int REQUEST_SETTINGS = 19;
    int REQUEST_OPEN_DOCUMENT = 20;
    int REQUEST_REQUEST_PERMISSIONS = 30;

    int TABLE_ID_ACCOUNTS = 1;
    int TABLE_ID_STATUSES = 12;
    int TABLE_ID_MENTIONS = 13;
    int TABLE_ID_ACTIVITIES_ABOUT_ME = 14;
    int TABLE_ID_ACTIVITIES_BY_FRIENDS = 15;
    int TABLE_ID_DIRECT_MESSAGES = 21;
    int TABLE_ID_DIRECT_MESSAGES_INBOX = 22;
    int TABLE_ID_DIRECT_MESSAGES_OUTBOX = 23;
    int TABLE_ID_DIRECT_MESSAGES_CONVERSATION = 24;
    int TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME = 25;
    int TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES = 26;
    int TABLE_ID_FILTERED_USERS = 31;
    int TABLE_ID_FILTERED_KEYWORDS = 32;
    int TABLE_ID_FILTERED_SOURCES = 33;
    int TABLE_ID_FILTERED_LINKS = 34;
    int TABLE_ID_TRENDS_LOCAL = 41;
    int TABLE_ID_SAVED_SEARCHES = 42;
    int TABLE_ID_SEARCH_HISTORY = 43;
    int TABLE_ID_DRAFTS = 51;
    int TABLE_ID_TABS = 52;
    int TABLE_ID_CACHED_USERS = 61;
    int TABLE_ID_CACHED_STATUSES = 62;
    int TABLE_ID_CACHED_HASHTAGS = 63;
    int TABLE_ID_CACHED_RELATIONSHIPS = 64;
    int VIRTUAL_TABLE_ID_DATABASE_READY = 100;
    int VIRTUAL_TABLE_ID_NOTIFICATIONS = 101;
    int VIRTUAL_TABLE_ID_PREFERENCES = 102;
    int VIRTUAL_TABLE_ID_ALL_PREFERENCES = 103;
    int VIRTUAL_TABLE_ID_PERMISSIONS = 104;
    int VIRTUAL_TABLE_ID_DNS = 105;
    int VIRTUAL_TABLE_ID_CACHED_IMAGES = 106;
    int VIRTUAL_TABLE_ID_CACHE_FILES = 107;
    int VIRTUAL_TABLE_ID_UNREAD_COUNTS = 108;
    int VIRTUAL_TABLE_ID_UNREAD_COUNTS_BY_TYPE = 109;
    int VIRTUAL_TABLE_ID_CACHED_USERS_WITH_RELATIONSHIP = 121;
    int VIRTUAL_TABLE_ID_CACHED_USERS_WITH_SCORE = 122;
    int VIRTUAL_TABLE_ID_DRAFTS_UNSENT = 131;
    int VIRTUAL_TABLE_ID_DRAFTS_NOTIFICATIONS = 132;
    int VIRTUAL_TABLE_ID_SUGGESTIONS_AUTO_COMPLETE = 141;
    int VIRTUAL_TABLE_ID_SUGGESTIONS_SEARCH = 142;

    int VIRTUAL_TABLE_ID_EMPTY = 200;

    int VIRTUAL_TABLE_ID_RAW_QUERY = 300;

    int NOTIFICATION_ID_HOME_TIMELINE = 1;
    int NOTIFICATION_ID_INTERACTIONS_TIMELINE = 2;
    int NOTIFICATION_ID_DIRECT_MESSAGES = 3;
    int NOTIFICATION_ID_DRAFTS = 4;
    int NOTIFICATION_ID_DATA_PROFILING = 5;
    int NOTIFICATION_ID_UPDATE_STATUS = 101;
    int NOTIFICATION_ID_SEND_DIRECT_MESSAGE = 102;

    String ICON_SPECIAL_TYPE_CUSTOMIZE = "_customize";

    String TASK_TAG_GET_HOME_TIMELINE = "get_home_tomeline";
    String TASK_TAG_GET_MENTIONS = "get_mentions";
    String TASK_TAG_GET_SENT_DIRECT_MESSAGES = "get_sent_direct_messages";
    String TASK_TAG_GET_RECEIVED_DIRECT_MESSAGES = "get_received_direct_messages";
    String TASK_TAG_GET_TRENDS = "get_trends";

    String METADATA_KEY_EXTENSION = "org.mariotaku.twidere.extension";
    String METADATA_KEY_EXTENSION_PERMISSIONS = "org.mariotaku.twidere.extension.permissions";
    String METADATA_KEY_EXTENSION_SETTINGS = "org.mariotaku.twidere.extension.settings";
    String METADATA_KEY_EXTENSION_ICON = "org.mariotaku.twidere.extension.icon";
    String METADATA_KEY_EXTENSION_USE_JSON = "org.mariotaku.twidere.extension.use_json";
    String METADATA_KEY_EXTENSION_VERSION_STATUS_SHORTENER = "org.mariotaku.twidere.extension.version.status_shortener";
    String METADATA_KEY_EXTENSION_VERSION_MEDIA_UPLOADER = "org.mariotaku.twidere.extension.version.media_uploader";

    String METADATA_KEY_PLUS_SERVICE_SIGN_IN_LABEL = "org.mariotaku.twidere.plus_service.sign_in_label";

    char SEPARATOR_PERMISSION = '|';
    String SEPARATOR_PERMISSION_REGEX = "\\" + SEPARATOR_PERMISSION;

    String PERMISSION_DENIED = "denied";
    String PERMISSION_REFRESH = "refresh";
    String PERMISSION_READ = "read";
    String PERMISSION_WRITE = "write";
    String PERMISSION_DIRECT_MESSAGES = "direct_messages";
    String PERMISSION_ACCOUNTS = "accounts";
    String PERMISSION_PREFERENCES = "preferences";

    int TAB_CODE_HOME_TIMELINE = 1;
    int TAB_CODE_NOTIFICATIONS_TIMELINE = 2;
    int TAB_CODE_DIRECT_MESSAGES = 4;

    int TWITTER_MAX_IMAGE_SIZE = 3145728;
    int TWITTER_MAX_IMAGE_WIDTH = 1024;
    int TWITTER_MAX_IMAGE_HEIGHT = 2048;

    String USER_TYPE_TWITTER_COM = "twitter.com";
    String USER_TYPE_FANFOU_COM = "fanfou.com";

}
