/*
 *                 Twidere - Twitter client for Android
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

package edu.tsinghua.hotmobi.model;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;

/**
 * Created by mariotaku on 15/8/13.
 */
public enum TweetType {
    TEXT("text"), PHOTO("photo"), VIDEO("video"), OTHER("other");

    private final String value;

    TweetType(String value) {
        this.value = value;
    }

    public static TweetType getTweetType(ParcelableStatus status) {
        if (status.media != null) {
            boolean hasImage = false;
            for (ParcelableMedia media : status.media) {
                switch (media.type) {
                    case ParcelableMedia.TYPE_ANIMATED_GIF:
                    case ParcelableMedia.TYPE_CARD_ANIMATED_GIF:
                    case ParcelableMedia.TYPE_VIDEO:
                        return VIDEO;
                    case ParcelableMedia.TYPE_IMAGE: {
                        hasImage = true;
                        break;
                    }
                }
            }
            if (hasImage) {
                return PHOTO;
            }
        }
        return TEXT;
    }

    public static TweetType parse(String type) {
        if (TEXT.value.equalsIgnoreCase(type)) {
            return TEXT;
        } else if (PHOTO.value.equalsIgnoreCase(type)) {
            return PHOTO;
        } else if (VIDEO.value.equalsIgnoreCase(type)) {
            return VIDEO;
        }
        return OTHER;
    }

    public String getValue() {
        return value;
    }

    public static class TweetTypeConverter extends StringBasedTypeConverter<TweetType> {

        @Override
        public TweetType getFromString(String string) {
            return TweetType.parse(string);
        }

        @Override
        public String convertToString(TweetType tweetType) {
            if (tweetType == null) return null;
            return tweetType.value;
        }
    }
}
