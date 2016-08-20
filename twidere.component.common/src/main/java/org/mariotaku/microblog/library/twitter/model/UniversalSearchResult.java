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

import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        @JsonField(name = "suggestion")
        SuggestionModule suggestion;

        public StatusModule getStatus() {
            return status;
        }

        public UserGalleryModule getUserGallery() {
            return userGallery;
        }

        public SuggestionModule getSuggestion() {
            return suggestion;
        }

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

            @JsonField(name = "auto_expand")
            boolean autoExpand;

            @JsonField(name = "result_type")
            @ModuleMetadata.ResultType
            String resultType;

            public SocialContext getSocialContext() {
                return socialContext;
            }

            public boolean isAutoExpand() {
                return autoExpand;
            }

            @ModuleMetadata.ResultType
            public final String getResultType() {
                return resultType;
            }

            @JsonObject
            public static class SocialContext {
                @JsonField(name = "following")
                boolean following;
                @JsonField(name = "followed_by")
                boolean followedBy;
                @JsonField(name = "related_users")
                RelatedUsers relatedUsers;

                public boolean isFollowing() {
                    return following;
                }

                public boolean isFollowedBy() {
                    return followedBy;
                }

                public RelatedUsers getRelatedUsers() {
                    return relatedUsers;
                }

                @JsonObject
                public static class RelatedUsers {

                    @JsonField(name = "follow_and_follow")
                    FollowAndFollow followAndFollow;

                    @JsonObject
                    public static class FollowAndFollow {
                        @JsonField(name = "users")
                        MiniUser[] users;
                        @JsonField(name = "num_more_users")
                        int numMoreUsers;
                    }

                    @JsonObject
                    public static class MiniUser {
                        @JsonField(name = "id_string")
                        String id;
                        @JsonField(name = "name")
                        String name;
                        @JsonField(name = "screen_name")
                        String screenName;

                    }

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
    public static class SuggestionModule {
        @JsonField(name = "metadata")
        ModuleMetadata metadata;
        @JsonField(name = "data")
        SuggestionData data;

        public ModuleMetadata getMetadata() {
            return metadata;
        }

        public SuggestionData getData() {
            return data;
        }

        @JsonObject
        public static class SuggestionData {
            @JsonField(name = "suggestion_type")
            String suggestionType;
            @JsonField(name = "suggestions")
            Suggestion[] suggestions;

            public String getSuggestionType() {
                return suggestionType;
            }

            public Suggestion[] getSuggestions() {
                return suggestions;
            }

            @JsonObject
            public static class Suggestion {
                @JsonField(name = "query")
                String query;

                @JsonField(name = "indices", typeConverter = Index.ArrayConverter.class)
                Index[] indices;

                public String getQuery() {
                    return query;
                }

                public static class Index {
                    int start;
                    int end;

                    public static class ArrayConverter implements TypeConverter<Index[]> {
                        @Override
                        public Index[] parse(JsonParser jsonParser) throws IOException {
                            if (jsonParser.getCurrentToken() == null) {
                                jsonParser.nextToken();
                            }
                            if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
                                jsonParser.skipChildren();
                                return null;
                            }
                            List<Index> list = new ArrayList<>();
                            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                                Index index = new Index();
                                index.start = jsonParser.nextIntValue(-1);
                                index.end = jsonParser.nextIntValue(-1);
                                list.add(index);
                            }
                            return list.toArray(new Index[list.size()]);
                        }

                        @Override
                        public void serialize(Index[] indices, String fieldName,
                                              boolean writeFieldNameForObject,
                                              JsonGenerator jsonGenerator) throws IOException {
                            if (writeFieldNameForObject) {
                                jsonGenerator.writeFieldName(fieldName);
                            }
                            if (indices == null) {
                                jsonGenerator.writeNull();
                            } else {
                                jsonGenerator.writeStartArray();
                                for (Index index : indices) {
                                    jsonGenerator.writeNumber(index.start);
                                    jsonGenerator.writeNumber(index.end);
                                }
                                jsonGenerator.writeEndArray();
                            }
                        }
                    }
                }
            }
        }
    }

    @JsonObject
    public static class UserModule {
        @JsonField(name = "metadata")
        ModuleMetadata metadata;
        @JsonField(name = "data")
        User data;

        public Object getMetadata() {
            return metadata;
        }

        public User getData() {
            return data;
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

    @JsonObject
    public static class ModuleMetadata {
        @JsonField(name = "result_type")
        @ResultType
        String resultType;

        @ResultType
        public final String getResultType() {
            return resultType;
        }

        @StringDef({ResultType.NORMAL, ResultType.TOP})
        public @interface ResultType {
            String NORMAL = "normal";
            String TOP = "top";
        }
    }

}
