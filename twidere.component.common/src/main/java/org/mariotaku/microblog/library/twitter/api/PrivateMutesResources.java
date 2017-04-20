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

package org.mariotaku.microblog.library.twitter.api;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.IDs;
import org.mariotaku.microblog.library.twitter.model.MutedKeyword;
import org.mariotaku.microblog.library.twitter.model.PageableResponseList;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.template.UserAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.restfu.annotation.param.Query;

/**
 * Created by mariotaku on 2017/3/26.
 */
@Params(template = UserAnnotationTemplate.class)
public interface PrivateMutesResources {

    @GET("/mutes/keywords/ids.json")
    IDs getMutesKeywordsIDs(Paging paging) throws MicroBlogException;

    @GET("/mutes/keywords/list.json")
    PageableResponseList<MutedKeyword> getMutesKeywordsList(@Query Paging paging) throws MicroBlogException;
}
