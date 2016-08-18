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

package org.mariotaku.microblog.library.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 15/10/21.
 */
@JsonObject
public class UniversalSearchResult {
    @JsonField(name = "metadata")
    Metadata metadata;
    @JsonField(name = "modules")
    Module[] modules;

    public Metadata getMetadata() {
        return metadata;
    }

    public Module[] getModules() {
        return modules;
    }

    @JsonObject
    public static class Module {
        @JsonField(name = "status")
        StatusModule status;
        @JsonField(name = "user_gallery")
        UserGalleryModule userGallery;
    }

    @JsonObject
    public static class StatusModule {
        @JsonField(name = "metadata")
        Metadata metadata;
        @JsonField(name = "data")
        Status data;

        public Metadata getMetadata() {
            return metadata;
        }

        public Status getData() {
            return data;
        }

        @JsonObject
        public static class Metadata {
            @JsonField(name = "social_context")
            SocialContext socialContext;

            @JsonField(name = "result_type")
            String resultType;

            @JsonField(name = "auto_expand")
            boolean autoExpand;

            public SocialContext getSocialContext() {
                return socialContext;
            }

            public String getResultType() {
                return resultType;
            }

            public boolean isAutoExpand() {
                return autoExpand;
            }

            @JsonObject
            public static class SocialContext {
                @JsonField(name = "following")
                boolean following;
                @JsonField(name = "followed_by")
                boolean followedBy;

                public boolean isFollowing() {
                    return following;
                }

                public boolean isFollowedBy() {
                    return followedBy;
                }
            }
        }
    }

    @JsonObject
    public static class UserGalleryModule {
        @JsonField(name = "metadata")
        Metadata metadata;
        @JsonField(name = "data")
        UserModule[] data;

        public Metadata getMetadata() {
            return metadata;
        }

        public UserModule[] getData() {
            return data;
        }

        @JsonObject
        public static class Metadata {
            @JsonField(name = "result_type")
            String resultType;
        }
    }

    @JsonObject
    public static class UserModule {
        @JsonField(name = "metadata")
        Object metadata;
        @JsonField(name = "data")
        User data;

        public Object getMetadata() {
            return metadata;
        }

        public User getData() {
            return data;
        }

        @JsonObject
        public static class Metadata {
            @JsonField(name = "result_type")
            String resultType;
        }
    }

    @JsonObject
    public static class Metadata {
        @JsonField(name = "cursor")
        String cursor;

        public String getCursor() {
            return cursor;
        }
    }
}
