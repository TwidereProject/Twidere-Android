/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package edu.tsinghua.hotmobi;

import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.TwidereLinkify;

/**
 * Created by Denny C. Ng on 2/26/15.
 */


public class TypeMappingUtil {

    public static String getLinkType(int type) {
        switch (type) {
            case TwidereLinkify.LINK_TYPE_MENTION:
                return "mention";
            case TwidereLinkify.LINK_TYPE_CASHTAG:
                return "cashTag";
            case TwidereLinkify.LINK_TYPE_LINK:
                return "urlLink";
            case TwidereLinkify.LINK_TYPE_LIST:
                return "userList";
            case TwidereLinkify.LINK_TYPE_STATUS:
                return "status";
            case TwidereLinkify.LINK_TYPE_USER_ID:
                return "userID";
            case TwidereLinkify.LINK_TYPE_HASHTAG:
                return "hashTag";
            default:
                return "unknown";
        }
    }


    public static String getMediaType(int type) {
        switch (type) {
            case ParcelableMedia.TYPE_IMAGE:
                return "image";
            case ParcelableMedia.TYPE_VIDEO:
                return "video";
            case ParcelableMedia.TYPE_ANIMATED_GIF:
                return "animated_gif";
            case ParcelableMedia.TYPE_CARD_ANIMATED_GIF:
                return "card_animated_gif";
            default:
                return "unknown";
        }
    }
}
