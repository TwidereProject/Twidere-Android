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

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.AfterCursorObjectCreated;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;

@SuppressWarnings("DeprecatedIsStillUsed")
@CursorObject
@Deprecated
public class ParcelableAccount {


    @CursorField(value = Accounts._ID, excludeWrite = true)
    public long id;


    @CursorField(value = Accounts.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;


    @CursorField(Accounts.SCREEN_NAME)
    public String screen_name;


    @CursorField(Accounts.NAME)
    public String name;

    @Nullable
    @AccountType
    @CursorField(Accounts.ACCOUNT_TYPE)
    public String account_type;


    @CursorField(Accounts.PROFILE_IMAGE_URL)
    public String profile_image_url;


    @CursorField(Accounts.PROFILE_BANNER_URL)
    public String profile_banner_url;


    @CursorField(Accounts.COLOR)
    public int color;


    @CursorField(Accounts.IS_ACTIVATED)
    public boolean is_activated;
    @Nullable


    @CursorField(value = Accounts.ACCOUNT_USER, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUser account_user;

    public boolean is_dummy;

    @AfterCursorObjectCreated
    void afterObjectCreated() {
        if (account_user != null) {
            account_user.is_cache = true;
            account_user.account_color = color;
        }
    }

}
