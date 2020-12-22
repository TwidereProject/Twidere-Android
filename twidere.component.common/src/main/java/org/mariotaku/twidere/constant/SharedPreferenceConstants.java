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

import org.mariotaku.library.exportablepreferences.annotation.ExportablePreference;

import static org.mariotaku.library.exportablepreferences.annotation.PreferenceType.BOOLEAN;
import static org.mariotaku.library.exportablepreferences.annotation.PreferenceType.INT;
import static org.mariotaku.library.exportablepreferences.annotation.PreferenceType.STRING;

public interface SharedPreferenceConstants {

    String FORMAT_PATTERN_TITLE = "[TITLE]";
    String FORMAT_PATTERN_TEXT = "[TEXT]";
    String FORMAT_PATTERN_NAME = "[NAME]";
    String FORMAT_PATTERN_LINK = "[LINK]";

    String VALUE_NONE = "none";
    String VALUE_LINK_HIGHLIGHT_OPTION_NONE = VALUE_NONE;
    String VALUE_LINK_HIGHLIGHT_OPTION_HIGHLIGHT = "highlight";
    String VALUE_LINK_HIGHLIGHT_OPTION_UNDERLINE = "underline";
    String VALUE_LINK_HIGHLIGHT_OPTION_BOTH = "both";
    int VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE = 0x0;
    int VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT = 0x1;
    int VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE = 0x2;
    int VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH = VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT
            | VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE;

    String VALUE_MEDIA_PREVIEW_STYLE_CROP = "crop";
    String VALUE_MEDIA_PREVIEW_STYLE_SCALE = "scale";
    String VALUE_MEDIA_PREVIEW_STYLE_REAL_SIZE = "real_size";
    String VALUE_MEDIA_PREVIEW_STYLE_NONE = VALUE_NONE;

    int VALUE_NOTIFICATION_FLAG_NONE = 0x0;
    int VALUE_NOTIFICATION_FLAG_RINGTONE = 0x1;
    int VALUE_NOTIFICATION_FLAG_VIBRATION = 0x2;
    int VALUE_NOTIFICATION_FLAG_LIGHT = 0x4;

    String VALUE_TAB_DISPLAY_OPTION_ICON = "icon";
    String VALUE_TAB_DISPLAY_OPTION_LABEL = "label";
    String VALUE_TAB_DISPLAY_OPTION_BOTH = "both";

    String VALUE_THEME_BACKGROUND_DEFAULT = "default";
    String VALUE_THEME_BACKGROUND_SOLID = "solid";
    String VALUE_THEME_BACKGROUND_TRANSPARENT = "transparent";

    String VALUE_THEME_NAME_AUTO = "auto";
    String VALUE_THEME_NAME_DARK = "dark";
    String VALUE_THEME_NAME_LIGHT = "light";

    String VALUE_PROFILE_IMAGE_STYLE_ROUND = "round";
    String VALUE_PROFILE_IMAGE_STYLE_SQUARE = "square";

    String VALUE_COMPOSE_NOW_ACTION_COMPOSE = "compose";
    String VALUE_COMPOSE_NOW_ACTION_TAKE_PHOTO = "take_photo";
    String VALUE_COMPOSE_NOW_ACTION_PICK_IMAGE = "pick_image";

    String VALUE_TAB_POSITION_TOP = "top";
    String VALUE_TAB_POSITION_BOTTOM = "bottom";

    String DEFAULT_THEME = VALUE_THEME_NAME_LIGHT;
    String DEFAULT_THEME_BACKGROUND = VALUE_THEME_BACKGROUND_DEFAULT;
    int DEFAULT_THEME_BACKGROUND_ALPHA = 160;

    String DEFAULT_QUOTE_FORMAT = "RT @" + FORMAT_PATTERN_NAME + ": " + FORMAT_PATTERN_TEXT;
    String DEFAULT_SHARE_FORMAT = FORMAT_PATTERN_TITLE + " - " + FORMAT_PATTERN_TEXT;

    String DEFAULT_REFRESH_INTERVAL = "15";
    boolean DEFAULT_AUTO_REFRESH = true;
    boolean DEFAULT_AUTO_REFRESH_HOME_TIMELINE = false;
    boolean DEFAULT_AUTO_REFRESH_MENTIONS = true;
    boolean DEFAULT_AUTO_REFRESH_DIRECT_MESSAGES = true;
    boolean DEFAULT_AUTO_REFRESH_TRENDS = false;
    boolean DEFAULT_NOTIFICATION = true;
    int DEFAULT_NOTIFICATION_TYPE_HOME = VALUE_NOTIFICATION_FLAG_NONE;
    int DEFAULT_NOTIFICATION_TYPE_MENTIONS = VALUE_NOTIFICATION_FLAG_VIBRATION
            | VALUE_NOTIFICATION_FLAG_LIGHT;
    int DEFAULT_NOTIFICATION_TYPE_DIRECT_MESSAGES = VALUE_NOTIFICATION_FLAG_RINGTONE
            | VALUE_NOTIFICATION_FLAG_VIBRATION | VALUE_NOTIFICATION_FLAG_LIGHT;

    boolean DEFAULT_HOME_TIMELINE_NOTIFICATION = false;
    boolean DEFAULT_MENTIONS_NOTIFICATION = true;
    boolean DEFAULT_DIRECT_MESSAGES_NOTIFICATION = true;

    int DEFAULT_DATABASE_ITEM_LIMIT = 100;
    int DEFAULT_LOAD_ITEM_LIMIT = 20;

    String DEFAULT_TAB_POSITION = VALUE_TAB_POSITION_TOP;

    // Exportable preferences

    @ExportablePreference(INT)
    String KEY_DATABASE_ITEM_LIMIT = "database_item_limit";
    @ExportablePreference(INT)
    String KEY_LOAD_ITEM_LIMIT = "load_item_limit";
    @ExportablePreference(INT)
    String KEY_TEXT_SIZE = "text_size_int";
    @ExportablePreference(STRING)
    String KEY_THEME = "theme";
    @ExportablePreference(STRING)
    String KEY_THEME_BACKGROUND = "theme_background";
    @ExportablePreference(INT)
    String KEY_THEME_BACKGROUND_ALPHA = "theme_background_alpha";
    @ExportablePreference(INT)
    String KEY_THEME_COLOR = "theme_color";
    @ExportablePreference(BOOLEAN)
    String KEY_DISPLAY_PROFILE_IMAGE = "display_profile_image";
    @ExportablePreference(STRING)
    String KEY_QUOTE_FORMAT = "quote_format";
    @ExportablePreference(BOOLEAN)
    String KEY_REMEMBER_POSITION = "remember_position";
    @ExportablePreference(BOOLEAN)
    String KEY_READ_FROM_BOTTOM = "read_from_bottom";
    @ExportablePreference(BOOLEAN)
    String KEY_ENABLE_PROXY = "enable_proxy";
    @ExportablePreference(STRING)
    String KEY_PROXY_HOST = "proxy_host";
    @ExportablePreference(STRING)
    String KEY_PROXY_TYPE = "proxy_type";
    @ExportablePreference(STRING)
    String KEY_PROXY_PORT = "proxy_port";
    @ExportablePreference(STRING)
    String KEY_PROXY_USERNAME = "proxy_username";
    @ExportablePreference(STRING)
    String KEY_PROXY_PASSWORD = "proxy_password";
    @ExportablePreference(BOOLEAN)
    String KEY_REFRESH_ON_START = "refresh_on_start";
    @ExportablePreference(BOOLEAN)
    String KEY_REFRESH_AFTER_TWEET = "refresh_after_tweet";
    @ExportablePreference(BOOLEAN)
    String KEY_AUTO_REFRESH = "auto_refresh";
    @ExportablePreference(STRING)
    String KEY_REFRESH_INTERVAL = "refresh_interval";
    @ExportablePreference(BOOLEAN)
    String KEY_AUTO_REFRESH_HOME_TIMELINE = "auto_refresh_home_timeline";
    @ExportablePreference(BOOLEAN)
    String KEY_AUTO_REFRESH_MENTIONS = "auto_refresh_mentions";
    @ExportablePreference(BOOLEAN)
    String KEY_AUTO_REFRESH_DIRECT_MESSAGES = "auto_refresh_direct_messages";
    @ExportablePreference(BOOLEAN)
    String KEY_AUTO_REFRESH_TRENDS = "auto_refresh_trends";
    @ExportablePreference(BOOLEAN)
    String KEY_HOME_TIMELINE_NOTIFICATION = "home_timeline_notification";
    @ExportablePreference(BOOLEAN)
    String KEY_MENTIONS_NOTIFICATION = "mentions_notification";
    @ExportablePreference(BOOLEAN)
    String KEY_DIRECT_MESSAGES_NOTIFICATION = "direct_messages_notification";
    @ExportablePreference(STRING)
    String KEY_NOTIFICATION_RINGTONE = "notification_ringtone";
    @ExportablePreference(INT)
    String KEY_NOTIFICATION_LIGHT_COLOR = "notification_light_color";
    @ExportablePreference(BOOLEAN)
    String KEY_HOME_REFRESH_MENTIONS = "home_refresh_mentions";
    @ExportablePreference(BOOLEAN)
    String KEY_HOME_REFRESH_DIRECT_MESSAGES = "home_refresh_direct_messages";
    @ExportablePreference(BOOLEAN)
    String KEY_HOME_REFRESH_TRENDS = "home_refresh_trends";
    @ExportablePreference(BOOLEAN)
    String KEY_HOME_REFRESH_SAVED_SEARCHES = "home_refresh_saved_searches";
    @ExportablePreference(BOOLEAN)
    String KEY_SHOW_ABSOLUTE_TIME = "show_absolute_time";
    @ExportablePreference(BOOLEAN)
    String KEY_QUICK_SEND = "quick_send";
    @ExportablePreference(STRING)
    String KEY_COMPOSE_ACCOUNTS = "compose_accounts";
    @ExportablePreference(BOOLEAN)
    String KEY_BUILTIN_DNS_RESOLVER = "builtin_dns_resolver";
    @ExportablePreference(BOOLEAN)
    String KEY_TCP_DNS_QUERY = "tcp_dns_query";
    @ExportablePreference(STRING)
    String KEY_DNS_SERVER = "dns_server";
    @ExportablePreference(INT)
    String KEY_CONNECTION_TIMEOUT = "connection_timeout";
    @ExportablePreference(BOOLEAN)
    String KEY_NAME_FIRST = "name_first";
    @ExportablePreference(BOOLEAN)
    String KEY_STOP_AUTO_REFRESH_WHEN_BATTERY_LOW = "stop_auto_refresh_when_battery_low";
    @ExportablePreference(BOOLEAN)
    String KEY_DISPLAY_SENSITIVE_CONTENTS = "display_sensitive_contents";
    @ExportablePreference(BOOLEAN)
    String KEY_PHISHING_LINK_WARNING = "phishing_link_warning";
    @ExportablePreference(STRING)
    String KEY_LINK_HIGHLIGHT_OPTION = "link_highlight_option";
    @ExportablePreference(BOOLEAN)
    String KEY_MEDIA_PRELOAD = "media_preload";
    @ExportablePreference(BOOLEAN)
    String KEY_PRELOAD_WIFI_ONLY = "preload_wifi_only";
    @ExportablePreference(BOOLEAN)
    String KEY_NO_CLOSE_AFTER_TWEET_SENT = "no_close_after_tweet_sent";
    @ExportablePreference(STRING)
    String KEY_API_URL_FORMAT = "api_url_format";
    @ExportablePreference(BOOLEAN)
    String KEY_SAME_OAUTH_SIGNING_URL = "same_oauth_signing_url";
    @ExportablePreference(BOOLEAN)
    String KEY_NO_VERSION_SUFFIX = "no_version_suffix";
    @ExportablePreference(STRING)
    String KEY_CREDENTIALS_TYPE = "credentials_type";
    @ExportablePreference(STRING)
    String KEY_CUSTOM_API_TYPE = "api_config_type";
    @ExportablePreference(STRING)
    String KEY_CONSUMER_KEY = "consumer_key";
    @ExportablePreference(STRING)
    String KEY_CONSUMER_SECRET = "consumer_secret";
    @ExportablePreference(BOOLEAN)
    String KEY_UNREAD_COUNT = "unread_count";
    @ExportablePreference(BOOLEAN)
    String KEY_PEBBLE_NOTIFICATIONS = "pebble_notifications";
    @ExportablePreference(BOOLEAN)
    String KEY_FAB_VISIBLE = "fab_visible";
    @ExportablePreference(STRING)
    String KEY_COMPOSE_NOW_ACTION = "compose_now_action";
    @ExportablePreference(STRING)
    String KEY_MEDIA_PREVIEW_STYLE = "media_preview_style";
    @ExportablePreference(BOOLEAN)
    String KEY_MEDIA_PREVIEW = "media_preview";
    @ExportablePreference(STRING)
    String KEY_PROFILE_IMAGE_STYLE = "profile_image_style";
    @ExportablePreference(BOOLEAN)
    String KEY_BANDWIDTH_SAVING_MODE = "bandwidth_saving_mode";
    @ExportablePreference(STRING)
    String KEY_TRANSLATION_DESTINATION = "translation_destination";
    @ExportablePreference(STRING)
    String KEY_TAB_DISPLAY_OPTION = "tab_display_option";
    @ExportablePreference(INT)
    String KEY_LIVE_WALLPAPER_SCALE = "live_wallpaper_scale";
    @ExportablePreference(BOOLEAN)
    String KEY_RETRY_ON_NETWORK_ISSUE = "retry_on_network_issue";
    @ExportablePreference(BOOLEAN)
    String KEY_THUMBOR_ENABLED = "thumbor_enabled";
    @ExportablePreference(STRING)
    String KEY_THUMBOR_ADDRESS = "thumbor_address";
    @ExportablePreference(STRING)
    String KEY_THUMBOR_SECURITY_KEY = "thumbor_security_key";
    @ExportablePreference(BOOLEAN)
    String KEY_HIDE_CARD_ACTIONS = "hide_card_actions";
    @ExportablePreference(INT)
    String KEY_CACHE_SIZE_LIMIT = "cache_size_limit";
    @ExportablePreference(BOOLEAN)
    String KEY_COMBINED_NOTIFICATIONS = "combined_notifications";
    @ExportablePreference(BOOLEAN)
    String KEY_I_WANT_MY_STARS_BACK = "i_want_my_stars_back";
    @ExportablePreference(BOOLEAN)
    String KEY_NEW_DOCUMENT_API = "new_document_api";
    @ExportablePreference(BOOLEAN)
    String KEY_DRAWER_TOGGLE = "drawer_toggle";
    @ExportablePreference(BOOLEAN)
    String KEY_RANDOMIZE_ACCOUNT_NAME = "randomize_account_name";
    @ExportablePreference(BOOLEAN)
    String KEY_DEFAULT_AUTO_REFRESH = "default_auto_refresh";
    @ExportablePreference(BOOLEAN)
    String KEY_FAVORITE_CONFIRMATION = "favorite_confirmation";
    @ExportablePreference(BOOLEAN)
    String KEY_FILTER_UNAVAILABLE_QUOTE_STATUSES = "filter_unavailable_quote_statuses";
    @ExportablePreference(BOOLEAN)
    String KEY_FILTER_POSSIBILITY_SENSITIVE_STATUSES = "filter_possibility_sensitive_statuses";
    @ExportablePreference(BOOLEAN)
    String KEY_CHROME_CUSTOM_TAB = "chrome_custom_tab";
    @ExportablePreference(BOOLEAN)
    String KEY_LIGHT_FONT = "light_font";
    @ExportablePreference(BOOLEAN)
    String KEY_AUTO_REFRESH_COMPATIBILITY_MODE = "auto_refresh_compatibility_mode";
    @ExportablePreference(BOOLEAN)
    String KEY_FLOATING_DETAILED_CONTENTS = "floating_detailed_contents";
    @ExportablePreference(STRING)
    String KEY_MULTI_COLUMN_TAB_WIDTH = "multi_column_tab_width";
    @ExportablePreference(STRING)
    String KEY_NAVBAR_STYLE = "navbar_style";
    @ExportablePreference(STRING)
    String KEY_OVERRIDE_LANGUAGE = "override_language";
    @ExportablePreference(STRING)
    String KEY_TAB_POSITION = "tab_position";
    @ExportablePreference(BOOLEAN)
    String KEY_AUTO_HIDE_TABS = "auto_hide_tabs";
    @ExportablePreference(BOOLEAN)
    String KEY_HIDE_CARD_NUMBERS = "hide_card_numbers";
    @ExportablePreference(BOOLEAN)
    String KEY_SHOW_LINK_PREVIEW = "show_link_preview";

    // Internal preferences

    String KEY_DROPBOX_ACCESS_TOKEN = "dropbox_access_token";
    String KEY_ATTACH_LOCATION = "attach_location";
    String KEY_ATTACH_PRECISE_LOCATION = "attach_precise_location";
    String KEY_SAVED_TAB_POSITION = "saved_tab_position";
    String KEY_LOCAL_TRENDS_WOEID = "local_trends_woeid";
    String KEY_SETTINGS_WIZARD_COMPLETED = "settings_wizard_completed";
    String KEY_API_LAST_CHANGE = "api_last_change";
    String KEY_BUG_REPORTS = "bug_reports";
    String KEY_EMOJI_SUPPORT = "emoji_support";
    String KEY_SYNC_PROVIDER_TYPE = "sync_provider_type";
    String KEY_DEFAULT_ACCOUNT_KEY = "default_account_key";
    String KEY_STATUS_SHORTENER = "status_shortener";
    String KEY_MEDIA_UPLOADER = "media_uploader";

    // Per-account preferences
    String KEY_NOTIFICATION = "notification";
    String KEY_NOTIFICATION_TYPE_HOME = "notification_type_home";
    String KEY_NOTIFICATION_TYPE_MENTIONS = "notification_type_mentions";
    String KEY_NOTIFICATION_TYPE_DIRECT_MESSAGES = "notification_type_direct_messages";
    String KEY_NOTIFICATION_FOLLOWING_ONLY = "notification_following_only";
    String KEY_NOTIFICATION_MENTIONS_ONLY = "notification_mentions_only";

}
