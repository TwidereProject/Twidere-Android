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

package org.mariotaku.microblog.library.fanfou.model;

import org.mariotaku.restfu.http.SimpleValueMap;
import org.mariotaku.restfu.http.mime.Body;

/**
 * Created by mariotaku on 16/3/20.
 */
public class PhotoStatusUpdate extends SimpleValueMap {

    public PhotoStatusUpdate(Body photo, String status) {
        put("photo", photo);
        put("status", status);
    }

    public void setLocation(String location) {
        if (location == null) {
            remove("location");
            return;
        }
        put("location", location);
    }

}
