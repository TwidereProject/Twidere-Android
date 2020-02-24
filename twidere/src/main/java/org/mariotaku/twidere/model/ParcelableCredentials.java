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

package org.mariotaku.twidere.model;

import androidx.annotation.Nullable;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.annotation.AuthTypeInt;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;

/**
 * Created by mariotaku on 15/5/26.
 */
@SuppressWarnings({"DeprecatedIsStillUsed", "deprecation"})
@CursorObject
@Deprecated
public class ParcelableCredentials extends ParcelableAccount {


    @CursorField(Accounts.AUTH_TYPE)
    @AuthTypeInt
    public int auth_type;

    @CursorField(Accounts.CONSUMER_KEY)
    public String consumer_key;

    @CursorField(Accounts.CONSUMER_SECRET)
    public String consumer_secret;


    @CursorField(Accounts.BASIC_AUTH_USERNAME)
    public String basic_auth_username;


    @CursorField(Accounts.BASIC_AUTH_PASSWORD)
    public String basic_auth_password;


    @CursorField(Accounts.OAUTH_TOKEN)
    public String oauth_token;


    @CursorField(Accounts.OAUTH_TOKEN_SECRET)
    public String oauth_token_secret;


    @CursorField(Accounts.API_URL_FORMAT)
    @Nullable
    public String api_url_format;


    @CursorField(Accounts.SAME_OAUTH_SIGNING_URL)
    public boolean same_oauth_signing_url;


    @CursorField(Accounts.NO_VERSION_SUFFIX)
    public boolean no_version_suffix;


    @CursorField(Accounts.ACCOUNT_EXTRAS)
    public String account_extras;

    @CursorField(Accounts.SORT_POSITION)
    public String sort_position;

}
