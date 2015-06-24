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

import org.mariotaku.twidere.annotation.Preference;

import static org.mariotaku.twidere.annotation.Preference.Type.BOOLEAN;
import static org.mariotaku.twidere.annotation.Preference.Type.STRING;

/**
 * Constants requires full application to build or useless for other
 * applications
 *
 * @author mariotaku
 */
public interface Constants extends TwidereConstants {

    String DATABASES_NAME = "twidere.sqlite";
    int DATABASES_VERSION = 104;

    int MENU_GROUP_STATUS_EXTENSION = 10;
    int MENU_GROUP_COMPOSE_EXTENSION = 11;
    int MENU_GROUP_IMAGE_EXTENSION = 12;
    int MENU_GROUP_USER_EXTENSION = 13;
    int MENU_GROUP_USER_LIST_EXTENSION = 14;
    int MENU_GROUP_STATUS_SHARE = 20;

    int MENU_HOME = android.R.id.home;
    int MENU_SEARCH = R.id.search;
    int MENU_ACTIONS = R.id.actions;
    int MENU_COMPOSE = R.id.compose;
    int MENU_SEND = R.id.send;
    int MENU_EDIT = R.id.edit;
    int MENU_INFO = R.id.info;
    int MENU_SELECT_ACCOUNT = R.id.select_account;
    int MENU_SETTINGS = R.id.settings;
    int MENU_ADD_LOCATION = R.id.add_location;
    int MENU_TAKE_PHOTO = R.id.take_photo;
    int MENU_ADD_IMAGE = R.id.add_image;
    int MENU_LOCATION = R.id.location;
    int MENU_IMAGE = R.id.image;
    int MENU_VIEW = R.id.view;
    int MENU_VIEW_PROFILE = R.id.view_profile;
    int MENU_DELETE = R.id.delete;
    int MENU_DELETE_SUBMENU = R.id.delete_submenu;
    int MENU_TOGGLE = R.id.toggle;
    int MENU_ADD = R.id.add;
    int MENU_PICK_FROM_GALLERY = R.id.pick_from_gallery;
    int MENU_PICK_FROM_MAP = R.id.pick_from_map;
    int MENU_EDIT_API = R.id.edit_api;
    int MENU_OPEN_IN_BROWSER = R.id.open_in_browser;
    int MENU_SET_COLOR = R.id.set_color;
    int MENU_ADD_ACCOUNT = R.id.add_account;
    int MENU_REPLY = R.id.reply;
    int MENU_FAVORITE = R.id.favorite;
    int MENU_RETWEET = R.id.retweet;
    int MENU_QUOTE = R.id.quote;
    int MENU_SHARE = R.id.share;
    int MENU_DRAFTS = R.id.drafts;
    int MENU_DELETE_ALL = R.id.delete_all;
    int MENU_SET_AS_DEFAULT = R.id.set_as_default;
    int MENU_SAVE = R.id.save;
    int MENU_CANCEL = R.id.cancel;
    int MENU_BLOCK = R.id.block;
    int MENU_REPORT_SPAM = R.id.report_spam;
    int MENU_MUTE_SOURCE = R.id.mute_source;
    int MENU_MUTE_USER = R.id.mute_user;
    int MENU_REFRESH = R.id.refresh;
    int MENU_MENTION = R.id.mention;
    int MENU_SEND_DIRECT_MESSAGE = R.id.send_direct_message;
    int MENU_VIEW_USER_LIST = R.id.view_user_list;
    int MENU_UP = R.id.up;
    int MENU_DOWN = R.id.down;
    int MENU_COPY = R.id.copy;
    int MENU_TOGGLE_SENSITIVE = R.id.toggle_sensitive;
    int MENU_REVOKE = R.id.revoke;
    int MENU_ADD_TO_LIST = R.id.add_to_list;
    int MENU_DELETE_FROM_LIST = R.id.delete_from_list;
    int MENU_STATUSES = R.id.statuses;
    int MENU_FAVORITES = R.id.favorites;
    int MENU_LISTS = R.id.lists;
    int MENU_LIST_MEMBERSHIPS = R.id.list_memberships;
    int MENU_CENTER = R.id.center;
    int MENU_FILTERS = R.id.filters;
    int MENU_SET_NICKNAME = R.id.set_nickname;
    int MENU_CLEAR_NICKNAME = R.id.clear_nickname;
    int MENU_ADD_TO_FILTER = R.id.add_to_filter;
    int MENU_FOLLOW = R.id.follow;
    int MENU_UNFOLLOW = R.id.unfollow;
    int MENU_BACK = R.id.back;
    int MENU_TRANSLATE = R.id.translate;
    int MENU_ACCEPT = R.id.accept;
    int MENU_DENY = R.id.deny;
    int MENU_IMPORT_SETTINGS = R.id.import_settings;
    int MENU_EXPORT_SETTINGS = R.id.export_settings;
    int MENU_PROGRESS = R.id.progress;
    int MENU_OPEN_WITH_ACCOUNT = R.id.open_with_account;
    int MENU_ACCOUNTS = R.id.accounts;
    int MENU_INVERSE_SELECTION = R.id.inverse_selection;
    int MENU_EDIT_MEDIA = R.id.edit_media;
    int MENU_RESET = R.id.reset;
    int MENU_ENABLE_RETWEETS = R.id.enable_retweets;

    int LINK_ID_STATUS = 1;
    int LINK_ID_USER = 2;
    int LINK_ID_USER_TIMELINE = 3;
    int LINK_ID_USER_FAVORITES = 4;
    int LINK_ID_USER_FOLLOWERS = 5;
    int LINK_ID_USER_FRIENDS = 6;
    int LINK_ID_USER_BLOCKS = 7;
    int LINK_ID_USER_MEDIA_TIMELINE = 8;
    int LINK_ID_DIRECT_MESSAGES_CONVERSATION = 9;
    int LINK_ID_USER_LIST = 10;
    int LINK_ID_USER_LISTS = 11;
    int LINK_ID_USER_LIST_TIMELINE = 12;
    int LINK_ID_USER_LIST_MEMBERS = 13;
    int LINK_ID_USER_LIST_SUBSCRIBERS = 14;
    int LINK_ID_USER_LIST_MEMBERSHIPS = 15;
    int LINK_ID_SAVED_SEARCHES = 19;
    int LINK_ID_USER_MENTIONS = 21;
    int LINK_ID_INCOMING_FRIENDSHIPS = 22;
    int LINK_ID_USERS = 23;
    int LINK_ID_STATUSES = 24;
    int LINK_ID_STATUS_RETWEETERS = 25;
    int LINK_ID_STATUS_REPLIES = 26;
    int LINK_ID_STATUS_FAVORITERS = 27;
    int LINK_ID_SEARCH = 28;
    int LINK_ID_MUTES_USERS = 41;
    int LINK_ID_MAP = 51;
    int LINK_ID_ACCOUNTS = 101;
    int LINK_ID_DRAFTS = 102;
    int LINK_ID_FILTERS = 103;
    int LINK_ID_PROFILE_EDITOR = 104;

    String DIR_NAME_IMAGE_CACHE = "image_cache";
    String DIR_NAME_FULL_IMAGE_CACHE = "full_image_cache";

    String FRAGMENT_TAG_API_UPGRADE_NOTICE = "api_upgrade_notice";

    String TWIDERE_PREVIEW_NICKNAME = "Twidere";
    String TWIDERE_PREVIEW_NAME = "Twidere Project";
    String TWIDERE_PREVIEW_SCREEN_NAME = "TwidereProject";
    String TWIDERE_PREVIEW_TEXT_HTML = "Twidere is an open source twitter client for Android, see <a href='https://github.com/mariotaku/twidere'>github.com/mariotak&#8230;<a/>";
    String TWIDERE_PREVIEW_SOURCE = "Twidere for Android";

    long HONDAJOJO_ID = 514378421;
    String HONDAJOJO_SCREEN_NAME = "HondaJOJO";
    String EASTER_EGG_TRIGGER_TEXT = "\u718A\u5B69\u5B50";
    String EASTER_EGG_RESTORE_TEXT_PART1 = "\u5927\u738B";
    String EASTER_EGG_RESTORE_TEXT_PART2 = "\u5C0F\u7684";
    String EASTER_EGG_RESTORE_TEXT_PART3 = "\u77E5\u9519";

    int[] PRESET_COLORS = {R.color.material_red, R.color.material_pink,
            R.color.material_purple, R.color.material_deep_purple, R.color.material_indigo,
            R.color.material_blue, R.color.material_light_blue, R.color.material_cyan,
            R.color.material_teal, R.color.material_green, R.color.material_light_green,
            R.color.material_lime, R.color.material_yellow, R.color.material_amber,
            R.color.material_orange, R.color.material_deep_orange};

    // SharedPreferences constants
    @Preference(type = BOOLEAN, exportable = false)
    String KEY_USAGE_STATISTICS = "usage_statistics";
    @Preference(type = STRING, exportable = false)
    String KEY_DEVICE_SERIAL = "device_serial";
}
