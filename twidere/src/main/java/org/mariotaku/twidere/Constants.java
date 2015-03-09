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

/**
 * Constants requires full application to build or useless for other
 * applications
 *
 * @author mariotaku
 */
public interface Constants extends TwidereConstants {

    public static final String DATABASES_NAME = "twidere.sqlite";
    public static final int DATABASES_VERSION = 85;

    public static final int MENU_GROUP_STATUS_EXTENSION = 10;
    public static final int MENU_GROUP_COMPOSE_EXTENSION = 11;
    public static final int MENU_GROUP_IMAGE_EXTENSION = 12;
    public static final int MENU_GROUP_USER_EXTENSION = 13;
    public static final int MENU_GROUP_USER_LIST_EXTENSION = 14;
    public static final int MENU_GROUP_STATUS_SHARE = 20;

    public static final int MENU_HOME = android.R.id.home;
    public static final int MENU_SEARCH = R.id.search;
    public static final int MENU_ACTIONS = R.id.actions;
    public static final int MENU_COMPOSE = R.id.compose;
    public static final int MENU_SEND = R.id.send;
    public static final int MENU_EDIT = R.id.edit;
    public static final int MENU_SELECT_ACCOUNT = R.id.select_account;
    public static final int MENU_SETTINGS = R.id.settings;
    public static final int MENU_ADD_LOCATION = R.id.add_location;
    public static final int MENU_TAKE_PHOTO = R.id.take_photo;
    public static final int MENU_ADD_IMAGE = R.id.add_image;
    public static final int MENU_LOCATION = R.id.location;
    public static final int MENU_IMAGE = R.id.image;
    public static final int MENU_VIEW = R.id.view;
    public static final int MENU_VIEW_PROFILE = R.id.view_profile;
    public static final int MENU_DELETE = R.id.delete;
    public static final int MENU_DELETE_SUBMENU = R.id.delete_submenu;
    public static final int MENU_TOGGLE = R.id.toggle;
    public static final int MENU_ADD = R.id.add;
    public static final int MENU_PICK_FROM_GALLERY = R.id.pick_from_gallery;
    public static final int MENU_PICK_FROM_MAP = R.id.pick_from_map;
    public static final int MENU_EDIT_API = R.id.edit_api;
    public static final int MENU_OPEN_IN_BROWSER = R.id.open_in_browser;
    public static final int MENU_SET_COLOR = R.id.set_color;
    public static final int MENU_ADD_ACCOUNT = R.id.add_account;
    public static final int MENU_REPLY = R.id.reply;
    public static final int MENU_FAVORITE = R.id.favorite;
    public static final int MENU_RETWEET = R.id.retweet;
    public static final int MENU_QUOTE = R.id.quote;
    public static final int MENU_SHARE = R.id.share;
    public static final int MENU_DRAFTS = R.id.drafts;
    public static final int MENU_DELETE_ALL = R.id.delete_all;
    public static final int MENU_SET_AS_DEFAULT = R.id.set_as_default;
    public static final int MENU_SAVE = R.id.save;
    public static final int MENU_CANCEL = R.id.cancel;
    public static final int MENU_BLOCK = R.id.block;
    public static final int MENU_REPORT_SPAM = R.id.report_spam;
    public static final int MENU_MUTE_SOURCE = R.id.mute_source;
    public static final int MENU_MUTE_USER = R.id.mute_user;
    public static final int MENU_REFRESH = R.id.refresh;
    public static final int MENU_MENTION = R.id.mention;
    public static final int MENU_SEND_DIRECT_MESSAGE = R.id.send_direct_message;
    public static final int MENU_VIEW_USER_LIST = R.id.view_user_list;
    public static final int MENU_UP = R.id.up;
    public static final int MENU_DOWN = R.id.down;
    public static final int MENU_COPY = R.id.copy;
    public static final int MENU_TOGGLE_SENSITIVE = R.id.toggle_sensitive;
    public static final int MENU_REVOKE = R.id.revoke;
    public static final int MENU_ADD_TO_LIST = R.id.add_to_list;
    public static final int MENU_DELETE_FROM_LIST = R.id.delete_from_list;
    public static final int MENU_STATUSES = R.id.statuses;
    public static final int MENU_FAVORITES = R.id.favorites;
    public static final int MENU_LISTS = R.id.lists;
    public static final int MENU_LIST_MEMBERSHIPS = R.id.list_memberships;
    public static final int MENU_CENTER = R.id.center;
    public static final int MENU_FILTERS = R.id.filters;
    public static final int MENU_SET_NICKNAME = R.id.set_nickname;
    public static final int MENU_CLEAR_NICKNAME = R.id.clear_nickname;
    public static final int MENU_ADD_TO_FILTER = R.id.add_to_filter;
    public static final int MENU_FOLLOW = R.id.follow;
    public static final int MENU_UNFOLLOW = R.id.unfollow;
    public static final int MENU_BACK = R.id.back;
    public static final int MENU_TRANSLATE = R.id.translate;
    public static final int MENU_ACCEPT = R.id.accept;
    public static final int MENU_DENY = R.id.deny;
    public static final int MENU_IMPORT_SETTINGS = R.id.import_settings;
    public static final int MENU_EXPORT_SETTINGS = R.id.export_settings;
    public static final int MENU_PROGRESS = R.id.progress;
    public static final int MENU_OPEN_WITH_ACCOUNT = R.id.open_with_account;
    public static final int MENU_ACCOUNTS = R.id.accounts;
    public static final int MENU_INVERSE_SELECTION = R.id.inverse_selection;
    public static final int MENU_EDIT_MEDIA = R.id.edit_media;

    public static final int LINK_ID_STATUS = 1;
    public static final int LINK_ID_USER = 2;
    public static final int LINK_ID_USER_TIMELINE = 3;
    public static final int LINK_ID_USER_FAVORITES = 4;
    public static final int LINK_ID_USER_FOLLOWERS = 5;
    public static final int LINK_ID_USER_FRIENDS = 6;
    public static final int LINK_ID_USER_BLOCKS = 7;
    public static final int LINK_ID_USER_MEDIA_TIMELINE = 8;
    public static final int LINK_ID_DIRECT_MESSAGES_CONVERSATION = 9;
    public static final int LINK_ID_USER_LIST = 10;
    public static final int LINK_ID_USER_LISTS = 11;
    public static final int LINK_ID_USER_LIST_TIMELINE = 12;
    public static final int LINK_ID_USER_LIST_MEMBERS = 13;
    public static final int LINK_ID_USER_LIST_SUBSCRIBERS = 14;
    public static final int LINK_ID_USER_LIST_MEMBERSHIPS = 15;
    public static final int LINK_ID_SAVED_SEARCHES = 19;
    public static final int LINK_ID_USER_MENTIONS = 21;
    public static final int LINK_ID_INCOMING_FRIENDSHIPS = 22;
    public static final int LINK_ID_USERS = 23;
    public static final int LINK_ID_STATUSES = 24;
    public static final int LINK_ID_STATUS_RETWEETERS = 25;
    public static final int LINK_ID_STATUS_REPLIES = 26;
    public static final int LINK_ID_STATUS_FAVORITERS = 27;
    public static final int LINK_ID_SEARCH = 28;
    public static final int LINK_ID_MUTES_USERS = 41;

    public static final String DIR_NAME_IMAGE_CACHE = "image_cache";
    public static final String DIR_NAME_FULL_IMAGE_CACHE = "full_image_cache";

    public static final String FRAGMENT_TAG_API_UPGRADE_NOTICE = "api_upgrade_notice";

    public static final String TWIDERE_PREVIEW_NICKNAME = "Twidere";
    public static final String TWIDERE_PREVIEW_NAME = "Twidere Project";
    public static final String TWIDERE_PREVIEW_SCREEN_NAME = "TwidereProject";
    public static final String TWIDERE_PREVIEW_TEXT_HTML = "Twidere is an open source twitter client for Android, see <a href='https://github.com/mariotaku/twidere'>github.com/mariotak&#8230;<a/>";
    public static final String TWIDERE_PREVIEW_SOURCE = "Twidere for Android";

    public static final long HONDAJOJO_ID = 514378421;
    public static final String HONDAJOJO_SCREEN_NAME = "HondaJOJO";
    public static final int UUCKY_ID = 1062473329;
    public static final String UUCKY_SCREEN_NAME = "Uucky_Lee";
    public static final String EASTER_EGG_TRIGGER_TEXT = "\u718A\u5B69\u5B50";
    public static final String EASTER_EGG_RESTORE_TEXT_PART1 = "\u5927\u738B";
    public static final String EASTER_EGG_RESTORE_TEXT_PART2 = "\u5C0F\u7684";
    public static final String EASTER_EGG_RESTORE_TEXT_PART3 = "\u77E5\u9519";

    public static final float DEFAULT_PULL_TO_REFRESH_SCROLL_DISTANCE = 0.3f;

    public static final String ENTRY_PREFERENCES = "preferences.json";
    public static final String ENTRY_NICKNAMES = "nicknames.json";
    public static final String ENTRY_USER_COLORS = "user_colors.json";
    public static final String ENTRY_HOST_MAPPING = "host_mapping.json";

    public static final int FLAG_PREFERENCES = 0x1;
    public static final int FLAG_NICKNAMES = 0x2;
    public static final int FLAG_USER_COLORS = 0x4;
    public static final int FLAG_HOST_MAPPING = 0x8;

    public static final int FLAG_ALL = FLAG_PREFERENCES | FLAG_NICKNAMES | FLAG_USER_COLORS | FLAG_HOST_MAPPING;



    public static final int[] PRESET_COLORS = {R.color.material_red, R.color.material_pink,
            R.color.material_purple, R.color.material_deep_purple, R.color.material_indigo,
            R.color.material_blue, R.color.material_light_blue, R.color.material_cyan,
            R.color.material_teal, R.color.material_green, R.color.material_light_green,
            R.color.material_lime, R.color.material_yellow, R.color.material_amber,
            R.color.material_orange, R.color.material_deep_orange};
}
