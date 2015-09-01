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

import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.annotation.Preference;
import org.mariotaku.twidere.model.ParcelableCredentials;

import static org.mariotaku.twidere.annotation.Preference.Type.BOOLEAN;
import static org.mariotaku.twidere.annotation.Preference.Type.INT;
import static org.mariotaku.twidere.annotation.Preference.Type.LONG;
import static org.mariotaku.twidere.annotation.Preference.Type.STRING;

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
    String VALUE_MEDIA_PREVIEW_STYLE_NONE = VALUE_NONE;

    int VALUE_MEDIA_PREVIEW_STYLE_CODE_CROP = 1;
    int VALUE_MEDIA_PREVIEW_STYLE_CODE_SCALE = 2;
    int VALUE_MEDIA_PREVIEW_STYLE_CODE_NONE = 0;

    String VALUE_THEME_FONT_FAMILY_REGULAR = "sans-serif";
    String VALUE_THEME_FONT_FAMILY_CONDENSED = "sans-serif-condensed";
    String VALUE_THEME_FONT_FAMILY_LIGHT = "sans-serif-light";
    String VALUE_THEME_FONT_FAMILY_THIN = "sans-serif-thin";

    int VALUE_NOTIFICATION_FLAG_NONE = 0x0;
    int VALUE_NOTIFICATION_FLAG_RINGTONE = 0x1;
    int VALUE_NOTIFICATION_FLAG_VIBRATION = 0x2;
    int VALUE_NOTIFICATION_FLAG_LIGHT = 0x4;

    String VALUE_COMPOSE_QUIT_ACTION_ASK = "ask";
    String VALUE_COMPOSE_QUIT_ACTION_SAVE = "save";
    String VALUE_COMPOSE_QUIT_ACTION_DISCARD = "discard";

    String VALUE_TAB_DISPLAY_OPTION_ICON = "icon";
    String VALUE_TAB_DISPLAY_OPTION_LABEL = "label";
    String VALUE_TAB_DISPLAY_OPTION_BOTH = "both";
    int VALUE_TAB_DISPLAY_OPTION_CODE_LABEL = 0x1;
    int VALUE_TAB_DISPLAY_OPTION_CODE_ICON = 0x2;
    int VALUE_TAB_DISPLAY_OPTION_CODE_BOTH = VALUE_TAB_DISPLAY_OPTION_CODE_ICON
            | VALUE_TAB_DISPLAY_OPTION_CODE_LABEL;

    String VALUE_THEME_BACKGROUND_DEFAULT = "default";
    String VALUE_THEME_BACKGROUND_SOLID = "solid";
    String VALUE_THEME_BACKGROUND_TRANSPARENT = "transparent";

    String VALUE_THEME_NAME_TWIDERE = "twidere";
    String VALUE_THEME_NAME_DARK = "dark";
    String VALUE_THEME_NAME_LIGHT = "light";

    String VALUE_PROFILE_IMAGE_STYLE_ROUND = "round";
    String VALUE_PROFILE_IMAGE_STYLE_SQUARE = "square";

    String VALUE_COMPOSE_NOW_ACTION_COMPOSE = "compose";
    String VALUE_COMPOSE_NOW_ACTION_TAKE_PHOTO = "take_photo";
    String VALUE_COMPOSE_NOW_ACTION_PICK_IMAGE = "pick_image";

    String VALUE_CARD_HIGHLIGHT_OPTION_NONE = VALUE_NONE;
    String VALUE_CARD_HIGHLIGHT_OPTION_BACKGROUND = "background";
    String VALUE_CARD_HIGHLIGHT_OPTION_LINE = "line";

    int VALUE_CARD_HIGHLIGHT_OPTION_CODE_NONE = 0x0;
    int VALUE_CARD_HIGHLIGHT_OPTION_CODE_BACKGROUND = 0x1;
    int VALUE_CARD_HIGHLIGHT_OPTION_CODE_LINE = 0x2;

    String DEFAULT_THEME = VALUE_THEME_NAME_TWIDERE;
    String DEFAULT_THEME_BACKGROUND = VALUE_THEME_BACKGROUND_DEFAULT;
    String DEFAULT_THEME_FONT_FAMILY = VALUE_THEME_FONT_FAMILY_REGULAR;
    int DEFAULT_THEME_BACKGROUND_ALPHA = 160;

    String DEFAULT_QUOTE_FORMAT = "RT @" + FORMAT_PATTERN_NAME + ": " + FORMAT_PATTERN_TEXT;
    String DEFAULT_SHARE_FORMAT = FORMAT_PATTERN_TITLE + " - " + FORMAT_PATTERN_TEXT;
    String DEFAULT_IMAGE_UPLOAD_FORMAT = FORMAT_PATTERN_TEXT + " " + FORMAT_PATTERN_LINK;

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
    String DEFAULT_CARD_HIGHLIGHT_OPTION = VALUE_CARD_HIGHLIGHT_OPTION_BACKGROUND;

    @Preference(type = INT, hasDefault = true, defaultInt = DEFAULT_DATABASE_ITEM_LIMIT)
    String KEY_DATABASE_ITEM_LIMIT = "database_item_limit";
    @Preference(type = INT, hasDefault = true, defaultInt = DEFAULT_LOAD_ITEM_LIMIT)
    String KEY_LOAD_ITEM_LIMIT = "load_item_limit";
    @Preference(type = INT, hasDefault = true, defaultInt = 15)
    String KEY_TEXT_SIZE = "text_size_int";
    @Preference(type = STRING, hasDefault = true, defaultString = DEFAULT_THEME)
    String KEY_THEME = "theme";
    @Preference(type = STRING, hasDefault = true, defaultString = DEFAULT_THEME_BACKGROUND)
    String KEY_THEME_BACKGROUND = "theme_background";
    @Preference(type = INT, hasDefault = true, defaultInt = DEFAULT_THEME_BACKGROUND_ALPHA)
    String KEY_THEME_BACKGROUND_ALPHA = "theme_background_alpha";
    @Preference(type = INT)
    String KEY_THEME_COLOR = "theme_color";
    @Preference(type = STRING, hasDefault = true, defaultString = DEFAULT_THEME_FONT_FAMILY)
    String KEY_THEME_FONT_FAMILY = "theme_font_family";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = true)
    String KEY_DISPLAY_PROFILE_IMAGE = "display_profile_image";
    @Preference(type = BOOLEAN)
    String KEY_LEFTSIDE_COMPOSE_BUTTON = "leftside_compose_button";
    @Preference(type = BOOLEAN, exportable = false, hasDefault = true, defaultBoolean = false)
    String KEY_ATTACH_LOCATION = "attach_location";
    @Preference(type = BOOLEAN)
    String KEY_IGNORE_SSL_ERROR = "ignore_ssl_error";
    @Preference(type = STRING)
    String KEY_QUOTE_FORMAT = "quote_format";
    @Preference(type = BOOLEAN)
    String KEY_REMEMBER_POSITION = "remember_position";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_READ_FROM_BOTTOM = "read_from_bottom";
    @Preference(type = INT, exportable = false)
    String KEY_SAVED_TAB_POSITION = "saved_tab_position";
    @Preference(type = BOOLEAN)
    String KEY_ENABLE_PROXY = "enable_proxy";
    @Preference(type = STRING)
    String KEY_PROXY_HOST = "proxy_host";
    @Preference(type = STRING)
    String KEY_PROXY_PORT = "proxy_port";
    @Preference(type = BOOLEAN)
    String KEY_REFRESH_ON_START = "refresh_on_start";
    @Preference(type = BOOLEAN)
    String KEY_REFRESH_AFTER_TWEET = "refresh_after_tweet";
    @Preference(type = BOOLEAN)
    String KEY_AUTO_REFRESH = "auto_refresh";
    @Preference(type = STRING)
    String KEY_REFRESH_INTERVAL = "refresh_interval";
    @Preference(type = BOOLEAN)
    String KEY_AUTO_REFRESH_HOME_TIMELINE = "auto_refresh_home_timeline";
    @Preference(type = BOOLEAN)
    String KEY_AUTO_REFRESH_MENTIONS = "auto_refresh_mentions";
    @Preference(type = BOOLEAN)
    String KEY_AUTO_REFRESH_DIRECT_MESSAGES = "auto_refresh_direct_messages";
    @Preference(type = BOOLEAN)
    String KEY_AUTO_REFRESH_TRENDS = "auto_refresh_trends";
    @Preference(type = BOOLEAN)
    String KEY_HOME_TIMELINE_NOTIFICATION = "home_timeline_notification";
    @Preference(type = BOOLEAN)
    String KEY_MENTIONS_NOTIFICATION = "mentions_notification";
    @Preference(type = BOOLEAN)
    String KEY_DIRECT_MESSAGES_NOTIFICATION = "direct_messages_notification";
    @Preference(type = BOOLEAN)
    String KEY_ENABLE_STREAMING = "enable_streaming";
    @Preference(type = INT)
    String KEY_LOCAL_TRENDS_WOEID = "local_trends_woeid";
    String KEY_NOTIFICATION_RINGTONE = "notification_ringtone";
    String KEY_NOTIFICATION_LIGHT_COLOR = "notification_light_color";
    String KEY_SHARE_FORMAT = "share_format";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = true)
    String KEY_HOME_REFRESH_MENTIONS = "home_refresh_mentions";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = true)
    String KEY_HOME_REFRESH_DIRECT_MESSAGES = "home_refresh_direct_messages";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = true)
    String KEY_HOME_REFRESH_TRENDS = "home_refresh_trends";
    String KEY_IMAGE_UPLOAD_FORMAT = "image_upload_format";
    String KEY_STATUS_SHORTENER = "status_shortener";
    String KEY_MEDIA_UPLOADER = "media_uploader";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_SHOW_ABSOLUTE_TIME = "show_absolute_time";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_QUICK_SEND = "quick_send";
    @Preference(type = STRING, exportable = false)
    String KEY_COMPOSE_ACCOUNTS = "compose_accounts";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_TCP_DNS_QUERY = "tcp_dns_query";
    @Preference(type = STRING, hasDefault = true, defaultString = "8.8.8.8")
    String KEY_DNS_SERVER = "dns_server";
    String KEY_CONNECTION_TIMEOUT = "connection_timeout";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = true)
    String KEY_NAME_FIRST = "name_first";
    String KEY_STOP_AUTO_REFRESH_WHEN_BATTERY_LOW = "stop_auto_refresh_when_battery_low";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_DISPLAY_SENSITIVE_CONTENTS = "display_sensitive_contents";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = true)
    String KEY_PHISHING_LINK_WARNING = "phishing_link_warning";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_FAST_SCROLL_THUMB = "fast_scroll_thumb";
    String KEY_LINK_HIGHLIGHT_OPTION = "link_highlight_option";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = true)
    String KEY_INDICATE_MY_STATUS = "indicate_my_status";
    String KEY_PRELOAD_PROFILE_IMAGES = "preload_profile_images";
    String KEY_PRELOAD_PREVIEW_IMAGES = "preload_preview_images";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = true)
    String KEY_PRELOAD_WIFI_ONLY = "preload_wifi_only";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = true)
    String KEY_LINK_TO_QUOTED_TWEET = "link_to_quoted_tweet";
    @Preference(type = BOOLEAN)
    String KEY_NO_CLOSE_AFTER_TWEET_SENT = "no_close_after_tweet_sent";
    @Preference(type = STRING, hasDefault = false)
    String KEY_API_URL_FORMAT = "api_url_format";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_SAME_OAUTH_SIGNING_URL = "same_oauth_signing_url";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_NO_VERSION_SUFFIX = "no_version_suffix";
    @Preference(type = INT, hasDefault = true, defaultInt = ParcelableCredentials.AUTH_TYPE_OAUTH)
    String KEY_AUTH_TYPE = "auth_type";
    @Preference(type = STRING, hasDefault = true, defaultString = TwidereConstants.TWITTER_CONSUMER_KEY)
    String KEY_CONSUMER_KEY = "consumer_key";
    @Preference(type = STRING, hasDefault = true, defaultString = TwidereConstants.TWITTER_CONSUMER_SECRET)
    String KEY_CONSUMER_SECRET = "consumer_secret";
    String KEY_SETTINGS_WIZARD_COMPLETED = "settings_wizard_completed";
    String KEY_CONSUMER_KEY_SECRET_SET = "consumer_key_secret_set";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = true)
    String KEY_CARD_ANIMATION = "card_animation";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = true)
    String KEY_UNREAD_COUNT = "unread_count";
    String KEY_NOTIFICATION = "notification";
    String KEY_NOTIFICATION_TYPE_HOME = "notification_type_home";
    String KEY_NOTIFICATION_TYPE_MENTIONS = "notification_type_mentions";
    String KEY_NOTIFICATION_TYPE_DIRECT_MESSAGES = "notification_type_direct_messages";
    String KEY_NOTIFICATION_FOLLOWING_ONLY = "notification_following_only";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_PEBBLE_NOTIFICATIONS = "pebble_notifications";

    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_COMPACT_CARDS = "compact_cards";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_FORCE_USING_PRIVATE_APIS = "force_using_private_apis";
    @Preference(type = STRING, hasDefault = true, defaultString = "140")
    String KEY_STATUS_TEXT_LIMIT = "status_text_limit";
    @Preference(type = STRING, hasDefault = true, defaultString = VALUE_COMPOSE_NOW_ACTION_COMPOSE)
    String KEY_COMPOSE_NOW_ACTION = "compose_now_action";
    String KEY_FALLBACK_TWITTER_LINK_HANDLER = "fallback_twitter_link_handler";

    @Preference(type = STRING, hasDefault = true, defaultString = VALUE_MEDIA_PREVIEW_STYLE_CROP)
    String KEY_MEDIA_PREVIEW_STYLE = "media_preview_style";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_MEDIA_PREVIEW = "media_preview";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_SORT_TIMELINE_BY_ID = "sort_timeline_by_id";
    @Preference(type = STRING, hasDefault = true)
    String KEY_PROFILE_IMAGE_STYLE = "profile_image_style";

    String KEY_QUICK_MENU_EXPANDED = "quick_menu_expanded";

    @Preference(type = STRING)
    String KEY_TRANSLATION_DESTINATION = "translation_destination";
    @Preference(type = STRING)
    String KEY_TAB_DISPLAY_OPTION = "tab_display_option";
    @Preference(type = STRING)
    String KEY_CARD_HIGHLIGHT_OPTION = "card_highlight_option";
    @Preference(type = INT, exportable = false)
    String KEY_LIVE_WALLPAPER_SCALE = "live_wallpaper_scale";
    @Preference(type = LONG, exportable = false)
    String KEY_API_LAST_CHANGE = "api_last_change";
    @Preference(type = LONG, exportable = false)
    String KEY_DEFAULT_ACCOUNT_ID = "default_account_id";


    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_THUMBOR_ENABLED = "thumbor_enabled";
    @Preference(type = STRING, hasDefault = false)
    String KEY_THUMBOR_ADDRESS = "thumbor_address";
    @Preference(type = STRING, hasDefault = false)
    String KEY_THUMBOR_SECURITY_KEY = "thumbor_security_key";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = false)
    String KEY_HIDE_CARD_ACTIONS = "hide_card_actions";
    @Preference(type = INT, hasDefault = true, defaultInt = 300)
    String KEY_CACHE_SIZE_LIMIT = "cache_size_limit";
    @Preference(type = BOOLEAN, hasDefault = true, defaultBoolean = true)
    String KEY_BUG_REPORTS = "bug_reports";
}
