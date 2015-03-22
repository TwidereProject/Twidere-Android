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

package edu.tsinghua.spice.Utilies;

import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.TwidereLinkify;

/**
 * Created by Denny C. Ng on 2/26/15.
 */


public class TypeMappingUtil {

    public static String getLinkType(int type) {
        String linkType = "";
        switch (type) {
            case TwidereLinkify.LINK_TYPE_MENTION:
                linkType = "mention";
                break;
            case TwidereLinkify.LINK_TYPE_CASHTAG:
                linkType = "cashTag";
                break;
            case TwidereLinkify.LINK_TYPE_LINK:
                linkType = "urlLink";
                break;
            case TwidereLinkify.LINK_TYPE_LIST:
                linkType = "userList";
                break;
            case TwidereLinkify.LINK_TYPE_STATUS:
                linkType = "status";
                break;
            case TwidereLinkify.LINK_TYPE_USER_ID:
                linkType = "userID";
                break;
            case TwidereLinkify.LINK_TYPE_HASHTAG:
                linkType = "hashTag";
                break;
            default:
                linkType = "unknown";
                break;
        }
        return linkType;
    }


    public static String getMediaType(int type) {
        String mediaType = "";
        switch (type) {
            case ParcelableMedia.TYPE_IMAGE:
                mediaType = "image";
                break;
            case ParcelableMedia.TYPE_VIDEO:
                mediaType = "video";
                break;
            default:
                mediaType = "unknown";
                break;
        }
        return mediaType;
    }
}
