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

package org.mariotaku.microblog.library.twitter;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.annotation.MediaCategory;
import org.mariotaku.microblog.library.twitter.model.MediaUploadResponse;
import org.mariotaku.microblog.library.twitter.model.NewMediaMetadata;
import org.mariotaku.microblog.library.twitter.model.ResponseCode;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.restfu.annotation.param.Queries;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.annotation.param.Raw;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.restfu.http.mime.Body;

public interface TwitterUpload {

    @POST("/media/upload.json")
    @BodyType(BodyType.MULTIPART)
    MediaUploadResponse uploadMedia(@Param("media") Body data,
                                    @Param(value = "additional_owners", arrayDelimiter = ',')
                                    String[] additionalOwners) throws MicroBlogException;


    @POST("/media/upload.json")
    @Params(@KeyValue(key = "command", value = "INIT"))
    MediaUploadResponse initUploadMedia(@Param("media_type") String mediaType,
                                        @Param("total_bytes") long totalBytes,
                                        @MediaCategory @Param("media_category") String mediaCategory,
                                        @Param(value = "additional_owners", arrayDelimiter = ',')
                                        String[] additionalOwners) throws MicroBlogException;

    @POST("/media/upload.json")
    @Params(@KeyValue(key = "command", value = "APPEND"))
    ResponseCode appendUploadMedia(@Param("media_id") String mediaId,
                                   @Param("segment_index") int segmentIndex,
                                   @Param("media_data") String media) throws MicroBlogException;

    @POST("/media/upload.json")
    @Params(@KeyValue(key = "command", value = "FINALIZE"))
    MediaUploadResponse finalizeUploadMedia(@Param("media_id") String mediaId) throws MicroBlogException;

    @GET("/media/upload.json")
    @Queries(@KeyValue(key = "command", value = "STATUS"))
    MediaUploadResponse getUploadMediaStatus(@Query("media_id") String mediaId) throws MicroBlogException;

    @POST("/media/metadata/create.json")
    ResponseCode createMetadata(@Raw NewMediaMetadata metadata) throws MicroBlogException;
}
