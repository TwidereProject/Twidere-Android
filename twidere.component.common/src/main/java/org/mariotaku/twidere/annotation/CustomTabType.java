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

package org.mariotaku.twidere.annotation;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 16/1/28.
 */
@StringDef({
        CustomTabType.HOME_TIMELINE,
        CustomTabType.NOTIFICATIONS_TIMELINE,
        CustomTabType.TRENDS_SUGGESTIONS,
        CustomTabType.DIRECT_MESSAGES,
        CustomTabType.FAVORITES,
        CustomTabType.USER_TIMELINE,
        CustomTabType.SEARCH_STATUSES,
        CustomTabType.LIST_TIMELINE,
})
@Retention(RetentionPolicy.SOURCE)
public @interface CustomTabType {
    String HOME_TIMELINE = "home_timeline";
    String NOTIFICATIONS_TIMELINE = "notifications_timeline";
    String TRENDS_SUGGESTIONS = "trends_suggestions";
    String DIRECT_MESSAGES = "direct_messages";
    String FAVORITES = "favorites";
    String USER_TIMELINE = "user_timeline";
    String SEARCH_STATUSES = "search_statuses";
    String LIST_TIMELINE = "list_timeline";
}
