

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

package org.mariotaku.twidere.api.twitter.model;

import org.mariotaku.library.logansquare.extension.annotation.EnumClass;
import org.mariotaku.library.logansquare.extension.annotation.Implementation;
import org.mariotaku.twidere.api.twitter.model.impl.MediaEntityImpl;

import java.util.Map;


@Implementation(MediaEntityImpl.class)
public interface MediaEntity extends UrlEntity {

    long getId();


    Map<String, Feature> getFeatures();

    String getMediaUrl();


    String getMediaUrlHttps();


    Map<String, Size> getSizes();


    Type getType();

    @EnumClass
    enum Type {
        PHOTO, VIDEO, ANIMATED_GIF, UNKNOWN;

        public static Type parse(String typeString) {
            if ("photo".equalsIgnoreCase(typeString)) {
                return PHOTO;
            } else if ("video".equalsIgnoreCase(typeString)) {
                return VIDEO;
            } else if ("animated_gif".equalsIgnoreCase(typeString)) {
                return ANIMATED_GIF;
            }
            return UNKNOWN;
        }
    }

    VideoInfo getVideoInfo();

    @Implementation(MediaEntityImpl.VideoInfoImpl.class)
    interface VideoInfo {

        Variant[] getVariants();

        long[] getAspectRatio();

        long getDuration();

        @Implementation(MediaEntityImpl.VideoInfoImpl.VariantImpl.class)
        interface Variant {

            String getContentType();

            String getUrl();

            long getBitrate();
        }

    }

    @Implementation(MediaEntityImpl.SizeImpl.class)
    interface Size {
        String THUMB = "thumb";
        String SMALL = "small";
        String MEDIUM = "medium";
        String LARGE = "large";
        int FIT = 100;
        int CROP = 101;

        int getHeight();

        String getResize();

        int getWidth();
    }

    /**
     * Created by mariotaku on 15/3/31.
     */
    @Implementation(MediaEntityImpl.FeatureImpl.class)
    interface Feature {

        @Implementation(MediaEntityImpl.FeatureImpl.FaceImpl.class)
        interface Face {

            int getX();

            int getY();

            int getHeight();

            int getWidth();
        }
    }
}
