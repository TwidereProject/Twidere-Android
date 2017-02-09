/*
 *         Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mariotaku.twidere.model;

import android.support.annotation.NonNull;

/**
 * Created by mariotaku on 15/4/20.
 */
public enum ConsumerKeyType {
    TWITTER_FOR_ANDROID, TWITTER_FOR_IPHONE, TWITTER_FOR_IPAD, TWITTER_FOR_MAC,
    TWITTER_FOR_WINDOWS_PHONE, TWITTER_FOR_GOOGLE_TV, TWEETDECK, UNKNOWN;

    @NonNull
    public static ConsumerKeyType parse(String type) {
        try {
            return ConsumerKeyType.valueOf(type);
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
}
