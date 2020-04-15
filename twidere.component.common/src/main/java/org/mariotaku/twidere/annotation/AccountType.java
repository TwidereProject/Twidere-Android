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

package org.mariotaku.twidere.annotation;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 2016/12/3.
 */
@StringDef({AccountType.TWITTER, AccountType.STATUSNET, AccountType.FANFOU, AccountType.MASTODON})
@Retention(RetentionPolicy.SOURCE)
public @interface AccountType {
    String TWITTER = "twitter";
    String STATUSNET = "statusnet";
    String FANFOU = "fanfou";
    String MASTODON = "mastodon";
}
