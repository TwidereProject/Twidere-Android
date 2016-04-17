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

package edu.tsinghua.hotmobi.util;

import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.TwidereLinkify;

import edu.tsinghua.hotmobi.model.TweetType;

/**
 * Created by Denny C. Ng on 2/26/15.
 */


public class TwidereDataUtils {

    private TwidereDataUtils() {
    }

    public static String getLinkType(int type) {
        switch (type) {
            case TwidereLinkify.LINK_TYPE_MENTION:
                return "mention";
            case TwidereLinkify.LINK_TYPE_CASHTAG:
                return "cashTag";
            case TwidereLinkify.LINK_TYPE_LINK_IN_TEXT:
            case TwidereLinkify.LINK_TYPE_ENTITY_URL:
                return "urlLink";
            case TwidereLinkify.LINK_TYPE_LIST:
                return "userList";
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
            case ParcelableMedia.Type.IMAGE:
                return "image";
            case ParcelableMedia.Type.VIDEO:
                return "video";
            case ParcelableMedia.Type.ANIMATED_GIF:
                return "animated_gif";
            case ParcelableMedia.Type.CARD_ANIMATED_GIF:
                return "card_animated_gif";
            case ParcelableMedia.Type.EXTERNAL_PLAYER:
                return "external_player";
            default:
                return "unknown";
        }
    }

    @TweetType
    public static String getTweetType(ParcelableStatus status) {
        if (status.media != null) {
            boolean hasImage = false;
            for (ParcelableMedia media : status.media) {
                switch (media.type) {
                    case ParcelableMedia.Type.ANIMATED_GIF:
                    case ParcelableMedia.Type.CARD_ANIMATED_GIF:
                    case ParcelableMedia.Type.VIDEO:
                        return TweetType.VIDEO;
                    case ParcelableMedia.Type.IMAGE: {
                        hasImage = true;
                        break;
                    }
                }
            }
            if (hasImage) {
                return TweetType.PHOTO;
            }
        }
        return TweetType.TEXT;
    }
}
