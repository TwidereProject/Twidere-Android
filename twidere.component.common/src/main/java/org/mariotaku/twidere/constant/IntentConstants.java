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

package org.mariotaku.twidere.constant;

public interface IntentConstants {

    String INTENT_PACKAGE_PREFIX = "org.mariotaku.twidere.";

    String INTENT_ACTION_HOME = INTENT_PACKAGE_PREFIX + "HOME";
    String INTENT_ACTION_COMPOSE = INTENT_PACKAGE_PREFIX + "COMPOSE";
    String INTENT_ACTION_QUICK_SEARCH = INTENT_PACKAGE_PREFIX + "QUICK_SEARCH";
    String INTENT_ACTION_REPLY = INTENT_PACKAGE_PREFIX + "REPLY";
    String INTENT_ACTION_QUOTE = INTENT_PACKAGE_PREFIX + "QUOTE";
    String INTENT_ACTION_EDIT_DRAFT = INTENT_PACKAGE_PREFIX + "EDIT_DRAFT";
    String INTENT_ACTION_MENTION = INTENT_PACKAGE_PREFIX + "MENTION";
    String INTENT_ACTION_REPLY_MULTIPLE = INTENT_PACKAGE_PREFIX + "REPLY_MULTIPLE";
    String INTENT_ACTION_SETTINGS = INTENT_PACKAGE_PREFIX + "SETTINGS";
    String INTENT_ACTION_SELECT_ACCOUNT = INTENT_PACKAGE_PREFIX + "SELECT_ACCOUNT";
    String INTENT_ACTION_VIEW_MEDIA = INTENT_PACKAGE_PREFIX + "VIEW_MEDIA";
    String INTENT_ACTION_TWITTER_LOGIN = INTENT_PACKAGE_PREFIX + "TWITTER_LOGIN";
    String INTENT_ACTION_PICK_FILE = INTENT_PACKAGE_PREFIX + "PICK_FILE";
    String INTENT_ACTION_PICK_DIRECTORY = INTENT_PACKAGE_PREFIX + "PICK_DIRECTORY";
    String INTENT_ACTION_EXTENSIONS = INTENT_PACKAGE_PREFIX + "EXTENSIONS";
    String INTENT_ACTION_CUSTOM_TABS = INTENT_PACKAGE_PREFIX + "CUSTOM_TABS";
    String INTENT_ACTION_SERVICE_COMMAND = INTENT_PACKAGE_PREFIX + "SERVICE_COMMAND";
    String INTENT_ACTION_REQUEST_PERMISSIONS = INTENT_PACKAGE_PREFIX + "REQUEST_PERMISSIONS";
    String INTENT_ACTION_SELECT_USER_LIST = INTENT_PACKAGE_PREFIX + "SELECT_USER_LIST";
    String INTENT_ACTION_SELECT_USER = INTENT_PACKAGE_PREFIX + "SELECT_USER";
    String INTENT_ACTION_COMPOSE_TAKE_PHOTO = INTENT_PACKAGE_PREFIX + "COMPOSE_TAKE_PHOTO";
    String INTENT_ACTION_COMPOSE_PICK_IMAGE = INTENT_PACKAGE_PREFIX + "COMPOSE_PICK_IMAGE";
    String INTENT_ACTION_HIDDEN_SETTINGS_ENTRY = INTENT_PACKAGE_PREFIX + "HIDDEN_SETTINGS_ENTRY";
    String INTENT_ACTION_EMOJI_SUPPORT_ABOUT = INTENT_PACKAGE_PREFIX + "EMOJI_SUPPORT_ABOUT";

    String INTENT_ACTION_EXTENSION_EDIT_IMAGE = INTENT_PACKAGE_PREFIX + "EXTENSION_EDIT_IMAGE";
    String INTENT_ACTION_EXTENSION_UPLOAD = INTENT_PACKAGE_PREFIX + "EXTENSION_UPLOAD";
    String INTENT_ACTION_EXTENSION_OPEN_STATUS = INTENT_PACKAGE_PREFIX + "EXTENSION_OPEN_STATUS";
    String INTENT_ACTION_EXTENSION_OPEN_USER = INTENT_PACKAGE_PREFIX + "EXTENSION_OPEN_USER";
    String INTENT_ACTION_EXTENSION_OPEN_USER_LIST = INTENT_PACKAGE_PREFIX
            + "EXTENSION_OPEN_USER_LIST";
    String INTENT_ACTION_EXTENSION_COMPOSE = INTENT_PACKAGE_PREFIX + "EXTENSION_COMPOSE";
    String INTENT_ACTION_EXTENSION_UPLOAD_MEDIA = INTENT_PACKAGE_PREFIX + "EXTENSION_UPLOAD_MEDIA";
    String INTENT_ACTION_EXTENSION_SHORTEN_STATUS = INTENT_PACKAGE_PREFIX
            + "EXTENSION_SHORTEN_STATUS";
    String INTENT_ACTION_EXTENSION_SYNC_TIMELINE = INTENT_PACKAGE_PREFIX
            + "EXTENSION_SYNC_TIMELINE";
    String INTENT_ACTION_EXTENSION_SETTINGS = INTENT_PACKAGE_PREFIX + "EXTENSION_SETTINGS";

    String INTENT_ACTION_UPDATE_STATUS = INTENT_PACKAGE_PREFIX + "UPDATE_STATUS";
    String INTENT_ACTION_SEND_DIRECT_MESSAGE = INTENT_PACKAGE_PREFIX + "SEND_DIRECT_MESSAGE";
    String INTENT_ACTION_DISCARD_DRAFT = INTENT_PACKAGE_PREFIX + "DISCARD_DRAFT";
    String INTENT_ACTION_SEND_DRAFT = INTENT_PACKAGE_PREFIX + "SEND_DRAFT";

    String INTENT_ACTION_PEBBLE_NOTIFICATION = "com.getpebble.action.SEND_NOTIFICATION";

    String BROADCAST_NOTIFICATION_DELETED = INTENT_PACKAGE_PREFIX + "NOTIFICATION_DELETED";
    String BROADCAST_PROMOTIONS_ACCEPTED = INTENT_PACKAGE_PREFIX + "PROMOTIONS_ACCEPTED";
    String BROADCAST_PROMOTIONS_DENIED = INTENT_PACKAGE_PREFIX + "PROMOTIONS_DENIED";

    String EXTRA_LATITUDE = "latitude";
    String EXTRA_LONGITUDE = "longitude";
    String EXTRA_URI = "uri";
    String EXTRA_URI_ORIG = "uri_orig";
    String EXTRA_MENTIONS = "mentions";
    String EXTRA_ACCOUNT_KEY = "account_key";
    String EXTRA_ACCOUNT_HOST = "account_host";
    String EXTRA_ACCOUNT_TYPE = "account_type";
    String EXTRA_ACCOUNT_TYPES = "account_types";
    String EXTRA_ACCOUNT_KEYS = "account_keys";
    String EXTRA_PAGE = "page";
    String EXTRA_DATA = "data";
    String EXTRA_QUERY = "query";
    String EXTRA_USER_KEY = "user_key";
    String EXTRA_USER_IDS = "user_ids";
    String EXTRA_LIST_ID = "list_id";
    String EXTRA_GROUP_ID = "group_id";
    String EXTRA_MAX_ID = "max_id";
    String EXTRA_SINCE_ID = "since_id";
    String EXTRA_MAX_SORT_ID = "max_sort_id";
    String EXTRA_SINCE_SORT_ID = "since_sort_id";
    String EXTRA_STATUS_ID = "status_id";
    String EXTRA_SCREEN_NAME = "screen_name";
    String EXTRA_EXCLUDE_REPLIES = "exclude_replies";
    String EXTRA_SCREEN_NAMES = "screen_names";
    String EXTRA_LIST_NAME = "list_name";
    String EXTRA_GROUP_NAME = "group_name";
    String EXTRA_GROUP = "group";
    String EXTRA_DESCRIPTION = "description";
    String EXTRA_IN_REPLY_TO_ID = "in_reply_to_id";
    String EXTRA_IN_REPLY_TO_NAME = "in_reply_to_name";
    String EXTRA_IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
    String EXTRA_IN_REPLY_TO_STATUS = "in_reply_to_status";
    String EXTRA_TEXT = "text";
    String EXTRA_TITLE = "title";
    String EXTRA_TYPE = "type";
    String EXTRA_VALUE = "value";
    String EXTRA_SCOPE = "scope";
    //	public static final String EXTRA_SUCCEED = "succeed";
    String EXTRA_IDS = "ids";
    String EXTRA_REFRESH_IDS = "refresh_ids";
    String EXTRA_IS_SHARE = "is_share";
    String EXTRA_STATUS = "status";
    String EXTRA_MESSAGE = "message";
    String EXTRA_STATUS_JSON = "status_json";
    String EXTRA_STATUSES = "statuses";
    String EXTRA_DRAFT = "draft";
    String EXTRA_FAVORITED = "favorited";
    String EXTRA_RETWEETED = "retweeted";
    String EXTRA_FILENAME = "filename";
    String EXTRA_FILE_SOURCE = "file_source";
    String EXTRA_FILE_EXTENSIONS = "file_extensions";
    String EXTRA_ITEMS_INSERTED = "items_inserted";
    String EXTRA_INITIAL_TAB = "initial_tab";
    String EXTRA_NOTIFICATION_ID = "notification_id";
    String EXTRA_NOTIFICATION_ACCOUNT = "notification_account";
    String EXTRA_FROM_NOTIFICATION = "from_notification";
    String EXTRA_IS_PUBLIC = "is_public";
    String EXTRA_USER = "user";
    String EXTRA_USERS = "users";
    String EXTRA_OPEN_CONVERSATION = "open_conversation";
    String EXTRA_ITEMS = "items";
    String EXTRA_USER_LIST = "user_list";
    String EXTRA_USER_LISTS = "user_lists";
    String EXTRA_APPEND_TEXT = "append_text";
    String EXTRA_IS_REPLACE_MODE = "is_replace_mode";
    String EXTRA_NAME = "name";
    String EXTRA_POSITION = "position";
    String EXTRA_ARGUMENTS = "arguments";
    String EXTRA_ICON = "icon";
    String EXTRA_ID = "id";
    String EXTRA_RESID = "resid";
    String EXTRA_SETTINGS_INTENT_ACTION = "settings_intent_action";
    String EXTRA_IMAGE_URI = "image_uri";
    String EXTRA_ACTIVATED_ONLY = "activated_only";
    String EXTRA_TAB_POSITION = "tab_position";
    String EXTRA_TAB_ID = "tab_id";
    String EXTRA_OAUTH_VERIFIER = "oauth_verifier";
    String EXTRA_CODE = "code";
    String EXTRA_COOKIE = "cookie";
    String EXTRA_ACCESS_TOKEN = "access_token";
    String EXTRA_REQUEST_TOKEN = "request_token";
    String EXTRA_REQUEST_TOKEN_SECRET = "request_token_secret";
    String EXTRA_CLIENT_ID = "client_id";
    String EXTRA_CLIENT_SECRET = "client_secret";
    String EXTRA_OMIT_INTENT_EXTRA = "omit_intent_extra";
    String EXTRA_COMMAND = "command";
    String EXTRA_WIDTH = "width";
    String EXTRA_ALLOW_SELECT_NONE = "allow_select_none";
    String EXTRA_SINGLE_SELECTION = "single_selection";
    String EXTRA_OAUTH_ONLY = "oauth_only";
    String EXTRA_PERMISSIONS = "permissions";
    String EXTRA_LOCATION = "location";
    String EXTRA_URL = "url";
    String EXTRA_PROFILE_URL = "profile_url";
    String EXTRA_NEXT_PAGINATION = "next_pagination";
    String EXTRA_PREV_PAGINATION = "prev_pagination";
    String EXTRA_PAGINATION = "pagination";
    String EXTRA_IS_MY_ACCOUNT = "is_my_account";
    String EXTRA_TAB_TYPE = "tab_type";
    String EXTRA_ACCOUNT = "account";
    String EXTRA_ACTIVITY_SCREENSHOT_ID = "activity_screenshot_id";
    String EXTRA_COLOR = "color";
    String EXTRA_ALPHA_SLIDER = "alpha_slider";
    String EXTRA_OPEN_ACCOUNTS_DRAWER = "open_accounts_drawer";
    String EXTRA_RECIPIENT_ID = "recipient_id";
    String EXTRA_CONVERSATION_ID = "conversation_id";
    String EXTRA_OFFICIAL_KEY_ONLY = "official_key_only";
    String EXTRA_SEARCH_ID = "search_id";
    String EXTRA_CLEAR_BUTTON = "clear_button";
    String EXTRA_PATH = "path";
    String EXTRA_ACTION = "action";
    String EXTRA_ACTIONS = "actions";
    String EXTRA_FLAGS = "flags";
    String EXTRA_INTENT = "intent";
    String EXTRA_BLACKLIST = "blacklist";
    String EXTRA_MEDIA = "media";
    String EXTRA_CURRENT_MEDIA = "current_media";
    String EXTRA_EXTRAS = "extras";
    String EXTRA_MY_FOLLOWING_ONLY = "my_following_only";
    String EXTRA_HIDE_RETWEETS = "hide_retweets";
    String EXTRA_HIDE_QUOTES = "hide_quotes";
    String EXTRA_HIDE_REPLIES = "hide_replies";
    String EXTRA_MENTIONS_ONLY = "mentions_only";
    String EXTRA_RESTART_ACTIVITY = "restart_activity";
    String EXTRA_RECREATE_ACTIVITY = "recreate_activity";
    String EXTRA_SHOULD_RECREATE = "should_recreate";
    String EXTRA_SHOULD_RESTART = "should_restart";
    String EXTRA_SHOULD_TERMINATE = "should_terminate";
    String EXTRA_FROM_USER = "from_user";
    String EXTRA_SHOW_MEDIA_PREVIEW = "show_media_preview";
    String EXTRA_SHOW_EXTRA_TYPE = "show_extra_type";
    String EXTRA_SOURCE = "source";
    String EXTRA_DESTINATION = "destination";
    String EXTRA_ACTIVITY_OPTIONS = "activity_options";
    String EXTRA_NEW_DOCUMENT = "new_document";
    String EXTRA_MAKE_GAP = "make_gap";
    String EXTRA_QUOTE_ORIGINAL_STATUS = "quote_original_status";
    String EXTRA_CARD = "card";
    String EXTRA_IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";
    String EXTRA_IS_ACCOUNT_PROFILE = "account";
    String EXTRA_LOADING_MORE = "loading_more";
    String EXTRA_PINNED_STATUS_IDS = "pinned_status_ids";
    String EXTRA_SHOULD_INIT_LOADER = "should_init_loader";
    String EXTRA_SELECT_ONLY_ITEM_AUTOMATICALLY = "select_only_item_automatically";
    String EXTRA_OBJECT = "object";
    String EXTRA_SIMPLE_LAYOUT = "simple_layout";
    String EXTRA_API_CONFIG = "api_config";
    String EXTRA_COUNT = "count";
    String EXTRA_REQUEST_CODE = "request_code";
    String EXTRA_FROM_CACHE = "from_cache";
    String EXTRA_SHOW_MY_LISTS = "show_my_lists";
    String EXTRA_WOEID = "woeid";
    String EXTRA_PLACE = "place";
    String EXTRA_PLACE_NAME = "place_name";
    String EXTRA_SCHEDULE_INFO = "schedule_info";
    String EXTRA_VISIBILITY = "visibility";
    String EXTRA_SELECTION = "selection";
    String EXTRA_SAVE_DRAFT = "save_draft";
    String EXTRA_HOST = "host";
    String EXTRA_LOCAL = "local";
}
