/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.microblog.library.twitter.model;

import androidx.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
                            return list.toArray(new Index[0]);
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
        @Retention(RetentionPolicy.SOURCE)
        public @interface ResultType {
            String NORMAL = "normal";
            String TOP = "top";
        }
    }

}
