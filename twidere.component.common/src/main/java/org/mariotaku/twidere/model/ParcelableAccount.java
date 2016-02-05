/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.TwitterContentUtils;
import org.mariotaku.twidere.util.content.ContentResolverUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CursorObject(valuesCreator = true)
@ParcelablePlease(allFields = false)
@JsonObject
public class ParcelableAccount implements Parcelable {

    @ParcelableThisPlease
    @JsonField(name = "screen_name")
    @CursorField(Accounts.SCREEN_NAME)
    public String screen_name;

    @ParcelableThisPlease
    @JsonField(name = "name")
    @CursorField(Accounts.NAME)
    public String name;

    @ParcelableThisPlease
    @JsonField(name = "profile_image_url")
    @CursorField(Accounts.PROFILE_IMAGE_URL)
    public String profile_image_url;

    @ParcelableThisPlease
    @JsonField(name = "profile_banner_url")
    @CursorField(Accounts.PROFILE_BANNER_URL)
    public String profile_banner_url;

    @ParcelableThisPlease
    @JsonField(name = "account_id")
    @CursorField(Accounts.ACCOUNT_ID)
    public long account_id;

    @ParcelableThisPlease
    @JsonField(name = "color")
    @CursorField(Accounts.COLOR)
    public int color;

    @ParcelableThisPlease
    @JsonField(name = "is_activated")
    @CursorField(Accounts.IS_ACTIVATED)
    public boolean is_activated;

    public boolean is_dummy;


    ParcelableAccount() {
    }

    public static ParcelableAccount dummyAccount() {
        final ParcelableAccount account = new ParcelableAccount();
        account.is_dummy = true;
        return account;
    }

    public static ParcelableCredentials dummyCredentials() {
        final ParcelableCredentials credentials = new ParcelableCredentials();
        credentials.is_dummy = true;
        return credentials;
    }

    public static ParcelableAccount getAccount(final Context context, final long accountId) {
        if (context == null) return null;
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
                Accounts.COLUMNS, Expression.equals(Accounts.ACCOUNT_ID, accountId).getSQL(), null, null);
        if (cur == null) return null;
        try {
            if (cur.moveToFirst()) {
                return ParcelableAccountCursorIndices.fromCursor(cur);
            }
        } finally {
            cur.close();
        }
        return null;
    }

    @NonNull
    public static long[] getAccountIds(final ParcelableAccount[] accounts) {
        final long[] ids = new long[accounts.length];
        for (int i = 0, j = accounts.length; i < j; i++) {
            ids[i] = accounts[i].account_id;
        }
        return ids;
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(final Context context, final boolean activatedOnly,
                                                  final boolean officialKeyOnly) {
        final List<ParcelableAccount> list = getAccountsList(context, activatedOnly, officialKeyOnly);
        return list.toArray(new ParcelableAccount[list.size()]);
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(@Nullable final Context context, @Nullable final long[] accountIds) {
        if (context == null) return new ParcelableAccount[0];
        final String where = accountIds != null ? Expression.in(new Column(Accounts.ACCOUNT_ID),
                new RawItemArray(accountIds)).getSQL() : null;
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
                Accounts.COLUMNS_NO_CREDENTIALS, where, null, null);
        if (cur == null) return new ParcelableAccount[0];
        return getAccounts(cur, new ParcelableAccountCursorIndices(cur));
    }


    @NonNull
    public static ParcelableAccount[] getAccounts(@Nullable final Cursor cursor) {
        if (cursor == null) return new ParcelableAccount[0];
        return getAccounts(cursor, new ParcelableAccountCursorIndices(cursor));
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(@Nullable final Cursor cursor, @Nullable final ParcelableAccountCursorIndices indices) {
        if (cursor == null || indices == null) return new ParcelableAccount[0];
        try {
            cursor.moveToFirst();
            final ParcelableAccount[] names = new ParcelableAccount[cursor.getCount()];
            while (!cursor.isAfterLast()) {
                names[cursor.getPosition()] = indices.newObject(cursor);
                cursor.moveToNext();
            }
            return names;
        } finally {
            cursor.close();
        }
    }

    public static List<ParcelableAccount> getAccountsList(final Context context, final boolean activatedOnly) {
        return getAccountsList(context, activatedOnly, false);
    }

    public static List<ParcelableAccount> getAccountsList(final Context context, final boolean activatedOnly,
                                                          final boolean officialKeyOnly) {
        if (context == null) return Collections.emptyList();
        final ArrayList<ParcelableAccount> accounts = new ArrayList<>();
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(),
                Accounts.CONTENT_URI, Accounts.COLUMNS,
                activatedOnly ? Accounts.IS_ACTIVATED + " = 1" : null, null, Accounts.SORT_POSITION);
        if (cur == null) return accounts;
        final ParcelableCredentialsCursorIndices indices = new ParcelableCredentialsCursorIndices(cur);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            if (!officialKeyOnly) {
                accounts.add(indices.newObject(cur));
            } else {
                final String consumerKey = cur.getString(indices.consumer_key);
                final String consumerSecret = cur.getString(indices.consumer_secret);
                if (TwitterContentUtils.isOfficialKey(context, consumerKey, consumerSecret)) {
                    accounts.add(indices.newObject(cur));
                }
            }
            cur.moveToNext();
        }
        cur.close();
        return accounts;
    }

    @Nullable
    public static ParcelableCredentials getCredentials(final Context context, final long accountId) {
        if (context == null || accountId < 0) return null;
        Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
                Accounts.COLUMNS, Expression.equals(Accounts.ACCOUNT_ID, accountId).getSQL(), null,
                null);
        if (cur == null) return null;
        try {
            if (cur.moveToFirst()) {
                return ParcelableCredentialsCursorIndices.fromCursor(cur);
            }
        } finally {
            cur.close();
        }
        return null;
    }

    public static List<ParcelableCredentials> getCredentialsList(final Context context, final boolean activatedOnly) {
        return getCredentialsList(context, activatedOnly, false);
    }

    public static ParcelableCredentials[] getCredentialsArray(final Context context, final boolean activatedOnly,
                                                              final boolean officialKeyOnly) {
        final List<ParcelableCredentials> credentialsList = getCredentialsList(context, activatedOnly, officialKeyOnly);
        return credentialsList.toArray(new ParcelableCredentials[credentialsList.size()]);
    }

    public static List<ParcelableCredentials> getCredentialsList(final Context context, final boolean activatedOnly,
                                                                 final boolean officialKeyOnly) {
        if (context == null) return Collections.emptyList();
        final ArrayList<ParcelableCredentials> accounts = new ArrayList<>();
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(),
                Accounts.CONTENT_URI, Accounts.COLUMNS,
                activatedOnly ? Accounts.IS_ACTIVATED + " = 1" : null, null, Accounts.SORT_POSITION);
        if (cur == null) return accounts;
        ParcelableCredentialsCursorIndices indices = new ParcelableCredentialsCursorIndices(cur);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            if (officialKeyOnly) {
                final String consumerKey = cur.getString(indices.consumer_key);
                final String consumerSecret = cur.getString(indices.consumer_secret);
                if (TwitterContentUtils.isOfficialKey(context, consumerKey, consumerSecret)) {
                    accounts.add(indices.newObject(cur));
                }
            } else {
                accounts.add(indices.newObject(cur));
            }
            cur.moveToNext();
        }
        cur.close();
        return accounts;
    }

    @Override
    public String toString() {
        return "Account{screen_name=" + screen_name + ", name=" + name + ", profile_image_url=" + profile_image_url
                + ", profile_banner_url=" + profile_banner_url + ", account_id=" + account_id + ", color=" + color
                + ", is_activated=" + is_activated + ", is_dummy=" + is_dummy + "}";
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableAccountParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableAccount> CREATOR = new Creator<ParcelableAccount>() {
        public ParcelableAccount createFromParcel(Parcel source) {
            ParcelableAccount target = new ParcelableAccount();
            ParcelableAccountParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableAccount[] newArray(int size) {
            return new ParcelableAccount[size];
        }
    };
}
