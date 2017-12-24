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

package org.mariotaku.microblog.library;

import org.mariotaku.microblog.library.annotation.twitter.StreamWith;
import org.mariotaku.microblog.library.callback.twitter.UserStreamCallback;
import org.mariotaku.microblog.library.template.twitter.StatusAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Params;

/**
 * Twitter UserStream API
 * Created by mariotaku on 15/5/26.
 */
@Deprecated
@Params(template = StatusAnnotationTemplate.class)
public interface TwitterUserStream {

    @GET("/user.json")
    void getUserStream(@StreamWith String with, UserStreamCallback callback);

}
