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
    String TWIDERE_PROJECT_URL = "https://github.com/TwidereProject/";
    String TWIDERE_PROJECT_EMAIL = "twidere.project@gmail.com";
    String TWIDERE_PACKAGE_NAME = "org.mariotaku.twidere";

    String ACCOUNT_TYPE = "org.mariotaku.twidere.account";
    String ACCOUNT_AUTH_TOKEN_TYPE = "org.mariotaku.twidere.account.token";
    String ACCOUNT_USER_DATA_KEY = "key";
    String ACCOUNT_USER_DATA_TYPE = "type";
    String ACCOUNT_USER_DATA_CREDS_TYPE = "creds_type";
    String ACCOUNT_USER_DATA_ACTIVATED = "activated";
    String ACCOUNT_USER_DATA_USER = "user";
    String ACCOUNT_USER_DATA_EXTRAS = "extras";
    String ACCOUNT_USER_DATA_COLOR = "color";
    String ACCOUNT_USER_DATA_POSITION = "position";
    String ACCOUNT_USER_DATA_TEST = "test";

    String LOGTAG = TWIDERE_APP_NAME;

    String SHARED_PREFERENCES_NAME = "preferences";
    String USER_NICKNAME_PREFERENCES_NAME = "user_nicknames";
    String USER_COLOR_PREFERENCES_NAME = "user_colors";
    String HOST_MAPPING_PREFERENCES_NAME = "host_mapping";
    String MESSAGE_DRAFTS_PREFERENCES_NAME = "message_drafts";
    String PERMISSION_PREFERENCES_NAME = "app_permissions";
    String SYNC_PREFERENCES_NAME = "sync_preferences";
    String TIMELINE_POSITIONS_PREFERENCES_NAME = "timeline_positions";
    String TIMELINE_SYNC_CACHE_PREFERENCES_NAME = "timeline_sync_cache";
    String KEYBOARD_SHORTCUTS_PREFERENCES_NAME = "keyboard_shortcuts_preferences";
    String ETAG_CACHE_PREFERENCES_NAME = "etag_cache";
    String ETAG_MASTODON_APPS_PREFERENCES_NAME = "mastodon_apps";
    String ACCOUNT_PREFERENCES_NAME_PREFIX = "account_preferences_";

    String TWITTER_CONSUMER_KEY = "MUUBibXUognm6e9vbzrUIqPkt";
    String TWITTER_CONSUMER_SECRET = "l2uWAgQkoHvDfM2PrRFx2WN4h7QIUIktmxyeTAqRo6TkGCtNKy";

    String DEFAULT_TWITTER_API_URL_FORMAT = "https://[DOMAIN].twitter.com/";

    String SCHEME_HTTP = "http";
    String SCHEME_HTTPS = "https";
    String SCHEME_CONTENT = ContentResolver.SCHEME_CONTENT;
    String SCHEME_TWIDERE = "twidere";
    String SCHEME_TWIDERE_SETTINGS = "twidere.settings";
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
    String AUTHORITY_NETWORK_PUBLIC_TIMELINE = "network_public_timeline";
    String AUTHORITY_MESSAGES = "direct_messages";
    String AUTHORITY_SEARCH = "search";
    String AUTHORITY_MASTODON_SEARCH = "mastodon_search";
    String AUTHORITY_MAP = "map";
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
    String AUTHORITY_NOTIFICATIONS = "notifications";
    String AUTHORITY_ACCOUNTS = "accounts";
    String AUTHORITY_DRAFTS = "drafts";
    String AUTHORITY_FILTERS = "filters";
    String AUTHORITY_PROFILE_EDITOR = "profile_editor";

    String PATH_FILTERS_IMPORT_BLOCKS = "import/blocks";
    String PATH_FILTERS_IMPORT_MUTES = "import/mutes";
    String PATH_FILTERS_SUBSCRIPTIONS = "subscriptions";
    String PATH_FILTERS_SUBSCRIPTIONS_ADD = "subscriptions/add";

    String PATH_MESSAGES_CONVERSATION = "conversation";
    String PATH_MESSAGES_CONVERSATION_NEW = "conversation/new";
    String PATH_MESSAGES_CONVERSATION_INFO = "conversation/info";

    String QUERY_PARAM_ACCOUNT_KEY = "account_key";
    String QUERY_PARAM_ACCOUNT_HOST = "account_host";
    String QUERY_PARAM_ACCOUNT_TYPE = "account_type";
    String QUERY_PARAM_ACCOUNT_NAME = "account_name";
    String QUERY_PARAM_STATUS_ID = "status_id";
    String QUERY_PARAM_USER_KEY = "user_key";
    String QUERY_PARAM_LIST_ID = "list_id";
    String QUERY_PARAM_GROUP_ID = "group_id";
    String QUERY_PARAM_GROUP_NAME = "group_name";
    String QUERY_PARAM_SCREEN_NAME = "screen_name";
    String QUERY_PARAM_PROFILE_URL = "profile_url";
    String QUERY_PARAM_LIST_NAME = "list_name";
    String QUERY_PARAM_QUERY = "query";
    String QUERY_PARAM_TYPE = "type";
    String QUERY_PARAM_VALUE_USERS = "users";
    String QUERY_PARAM_VALUE_TWEETS = "tweets";
    String QUERY_PARAM_SHOW_NOTIFICATION = "show_notification";
    String QUERY_PARAM_NOTIFY_CHANGE = "notify_change";
    String QUERY_PARAM_LAT = "lat";
    String QUERY_PARAM_LNG = "lng";
    String QUERY_PARAM_URL = "url";
    String QUERY_PARAM_NAME = "name";
    String QUERY_PARAM_FINISH_ONLY = "finish_only";
    String QUERY_PARAM_NEW_ITEMS_COUNT = "new_items_count";
    String QUERY_PARAM_CONVERSATION_ID = "conversation_id";
    String QUERY_PARAM_READ_POSITION = "read_position";
    String QUERY_PARAM_LIMIT = "limit";
    String QUERY_PARAM_EXTRA = "extra";
    String QUERY_PARAM_TIMESTAMP = "timestamp";
    String QUERY_PARAM_FROM_NOTIFICATION = "from_notification";
    String QUERY_PARAM_NOTIFICATION_TYPE = "notification_type";
    String QUERY_PARAM_PREVIEW = "preview";
    String QUERY_PARAM_NOTIFY_URI = "notify_uri";

    String DEFAULT_PROTOCOL = PROTOCOL_HTTPS;

    String OAUTH_CALLBACK_OOB = "oob";
    String OAUTH_CALLBACK_URL = PROTOCOL_TWIDERE + "com.twitter.oauth/";
    String MASTODON_CALLBACK_URL = "https://org.mariotaku.twidere/auth/callback/mastodon";
    String GITHUB_CALLBACK_URL = "https://org.mariotaku.twidere/auth/callback/github";

    int REQUEST_TAKE_PHOTO = 1;
    int REQUEST_PICK_MEDIA = 2;
    int REQUEST_SELECT_ACCOUNT = 3;
    int REQUEST_COMPOSE = 4;
    int REQUEST_EDIT_API = 5;
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
    int REQUEST_PURCHASE_EXTRA_FEATURES = 41;

    int TABLE_ID_STATUSES = 12;
    int TABLE_ID_MENTIONS = 13;
    int TABLE_ID_ACTIVITIES_ABOUT_ME = 14;
    int TABLE_ID_ACTIVITIES_BY_FRIENDS = 15;
    int TABLE_ID_MESSAGES = 21;
    int TABLE_ID_MESSAGES_CONVERSATIONS = 24;
    int TABLE_ID_FILTERED_USERS = 31;
    int TABLE_ID_FILTERED_KEYWORDS = 32;
    int TABLE_ID_FILTERED_SOURCES = 33;
    int TABLE_ID_FILTERED_LINKS = 34;
    int TABLE_ID_FILTERS_SUBSCRIPTIONS = 39;
    int TABLE_ID_TRENDS_LOCAL = 41;
    int TABLE_ID_SAVED_SEARCHES = 42;
    int TABLE_ID_SEARCH_HISTORY = 43;
    int TABLE_ID_DRAFTS = 51;
    int TABLE_ID_TABS = 52;
    int TABLE_ID_CACHED_USERS = 61;
    int TABLE_ID_CACHED_STATUSES = 62;
    int TABLE_ID_CACHED_HASHTAGS = 63;
    int TABLE_ID_CACHED_RELATIONSHIPS = 64;
    int VIRTUAL_TABLE_ID_PERMISSIONS = 104;
    int VIRTUAL_TABLE_ID_CACHED_USERS_WITH_RELATIONSHIP = 121;
    int VIRTUAL_TABLE_ID_CACHED_USERS_WITH_SCORE = 122;
    int VIRTUAL_TABLE_ID_DRAFTS_UNSENT = 131;
    int VIRTUAL_TABLE_ID_DRAFTS_NOTIFICATIONS = 132;
    int VIRTUAL_TABLE_ID_SUGGESTIONS_AUTO_COMPLETE = 141;
    int VIRTUAL_TABLE_ID_SUGGESTIONS_SEARCH = 142;

    int VIRTUAL_TABLE_ID_NULL = 200;
    int VIRTUAL_TABLE_ID_EMPTY = 201;
    int VIRTUAL_TABLE_ID_DATABASE_PREPARE = 203;

    int VIRTUAL_TABLE_ID_RAW_QUERY = 300;

    int NOTIFICATION_ID_HOME_TIMELINE = 1;
    int NOTIFICATION_ID_INTERACTIONS_TIMELINE = 2;
    int NOTIFICATION_ID_DIRECT_MESSAGES = 3;
    int NOTIFICATION_ID_DRAFTS = 4;
    int NOTIFICATION_ID_DATA_PROFILING = 5;
    int NOTIFICATION_ID_PROMOTIONS_OFFER = 6;
    int NOTIFICATION_ID_USER_NOTIFICATION = 10;
    int NOTIFICATION_ID_UPDATE_STATUS = 101;
    int NOTIFICATION_ID_SEND_DIRECT_MESSAGE = 102;


    String METADATA_KEY_EXTENSION = "org.mariotaku.twidere.extension";
    String METADATA_KEY_EXTENSION_PERMISSIONS = "org.mariotaku.twidere.extension.permissions";
    String METADATA_KEY_EXTENSION_SETTINGS = "org.mariotaku.twidere.extension.settings";
    String METADATA_KEY_EXTENSION_ICON = "org.mariotaku.twidere.extension.icon";
    String METADATA_KEY_EXTENSION_USE_JSON = "org.mariotaku.twidere.extension.use_json";
    String METADATA_KEY_EXTENSION_VERSION_STATUS_SHORTENER = "org.mariotaku.twidere.extension.version.status_shortener";
    String METADATA_KEY_EXTENSION_VERSION_MEDIA_UPLOADER = "org.mariotaku.twidere.extension.version.media_uploader";

    char SEPARATOR_PERMISSION = '|';
    String SEPARATOR_PERMISSION_REGEX = "\\" + SEPARATOR_PERMISSION;

    String PERMISSION_DENIED = "denied";
    String PERMISSION_REFRESH = "refresh";
    String PERMISSION_READ = "read";
    String PERMISSION_WRITE = "write";
    String PERMISSION_DIRECT_MESSAGES = "direct_messages";
    String PERMISSION_PREFERENCES = "preferences";

    int TAB_CODE_HOME_TIMELINE = 1;
    int TAB_CODE_NOTIFICATIONS_TIMELINE = 2;
    int TAB_CODE_DIRECT_MESSAGES = 4;

    String USER_TYPE_TWITTER_COM = "twitter.com";
    String USER_TYPE_FANFOU_COM = "fanfou.com";

}
