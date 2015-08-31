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
    int DATABASES_VERSION = 109;

    int MENU_GROUP_STATUS_EXTENSION = 10;
    int MENU_GROUP_COMPOSE_EXTENSION = 11;
    int MENU_GROUP_IMAGE_EXTENSION = 12;
    int MENU_GROUP_USER_EXTENSION = 13;
    int MENU_GROUP_USER_LIST_EXTENSION = 14;
    int MENU_GROUP_STATUS_SHARE = 20;

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
    int LINK_ID_SCHEDULED_STATUSES = 61;
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
