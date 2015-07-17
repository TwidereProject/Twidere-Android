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
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.TwitterContentUtils;
import org.mariotaku.twidere.util.content.ContentResolverUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ParcelablePlease(allFields = false)
@JsonObject
public class ParcelableAccount implements Parcelable {

    public static final Parcelable.Creator<ParcelableAccount> CREATOR = new Parcelable.Creator<ParcelableAccount>() {

        @Override
        public ParcelableAccount createFromParcel(final Parcel in) {
            return new ParcelableAccount(in);
        }

        @Override
        public ParcelableAccount[] newArray(final int size) {
            return new ParcelableAccount[size];
        }
    };

    @ParcelableThisPlease
    @JsonField(name = "screen_name")
    public String screen_name;

    @ParcelableThisPlease
    @JsonField(name = "name")
    public String name;

    @ParcelableThisPlease
    @JsonField(name = "profile_image_url")
    public String profile_image_url;

    @ParcelableThisPlease
    @JsonField(name = "profile_banner_url")
    public String profile_banner_url;

    @ParcelableThisPlease
    @JsonField(name = "account_id")
    public long account_id;

    @ParcelableThisPlease
    @JsonField(name = "color")
    public int color;

    @ParcelableThisPlease
    @JsonField(name = "is_activated")
    public boolean is_activated;

    @ParcelableThisPlease
    @JsonField(name = "is_dummy")
    public boolean is_dummy;


    public ParcelableAccount(final Cursor cursor, final Indices indices) {
        is_dummy = false;
        screen_name = indices.screen_name != -1 ? cursor.getString(indices.screen_name) : null;
        name = indices.name != -1 ? cursor.getString(indices.name) : null;
        account_id = indices.account_id != -1 ? cursor.getLong(indices.account_id) : -1;
        profile_image_url = indices.profile_image_url != -1 ? cursor.getString(indices.profile_image_url) : null;
        profile_banner_url = indices.profile_banner_url != -1 ? cursor.getString(indices.profile_banner_url) : null;
        color = indices.color != -1 ? cursor.getInt(indices.color) : Color.TRANSPARENT;
        is_activated = indices.is_activated != -1 && cursor.getInt(indices.is_activated) == 1;
    }

    public ParcelableAccount(final Parcel source) {
        ParcelableAccountParcelablePlease.readFromParcel(this, source);
    }

    public ParcelableAccount() {
        is_dummy = true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        ParcelableAccountParcelablePlease.writeToParcel(this, out, flags);
    }

    public static ParcelableAccount dummyAccount() {
        return new ParcelableAccount();
    }

    public static ParcelableCredentials dummyCredentials() {
        return new ParcelableCredentials();
    }

    public static ParcelableAccount getAccount(final Context context, final long account_id) {
        if (context == null) return null;
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
                Accounts.COLUMNS, Accounts.ACCOUNT_ID + " = " + account_id, null, null);
        if (cur != null) {
            try {
                if (cur.getCount() > 0 && cur.moveToFirst()) {
                    final Indices indices = new Indices(cur);
                    cur.moveToFirst();
                    return new ParcelableAccount(cur, indices);
                }
            } finally {
                cur.close();
            }
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
        return getAccounts(cur, new Indices(cur));
    }


    @NonNull
    public static ParcelableAccount[] getAccounts(@Nullable final Cursor cursor) {
        if (cursor == null) return new ParcelableAccount[0];
        return getAccounts(cursor, new Indices(cursor));
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(@Nullable final Cursor cursor, @Nullable final Indices indices) {
        if (cursor == null || indices == null) return new ParcelableAccount[0];
        try {
            cursor.moveToFirst();
            final ParcelableAccount[] names = new ParcelableAccount[cursor.getCount()];
            while (!cursor.isAfterLast()) {
                names[cursor.getPosition()] = new ParcelableAccount(cursor, indices);
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
        final Indices indices = new Indices(cur);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            if (!officialKeyOnly) {
                accounts.add(new ParcelableAccount(cur, indices));
            } else {
                final String consumerKey = cur.getString(indices.consumer_key);
                final String consumerSecret = cur.getString(indices.consumer_secret);
                if (TwitterContentUtils.isOfficialKey(context, consumerKey, consumerSecret)) {
                    accounts.add(new ParcelableAccount(cur, indices));
                }
            }
            cur.moveToNext();
        }
        cur.close();
        return accounts;
    }

    public static ParcelableCredentials getCredentials(final Context context, final long accountId) {
        if (context == null) return null;
        final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
                Accounts.COLUMNS, Accounts.ACCOUNT_ID + " = " + accountId, null, null);
        if (cur != null) {
            try {
                if (cur.getCount() > 0 && cur.moveToFirst()) {
                    final Indices indices = new Indices(cur);
                    cur.moveToFirst();
                    return new ParcelableCredentials(cur, indices);
                }
            } finally {
                cur.close();
            }
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
        final Indices indices = new Indices(cur);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            if (officialKeyOnly) {
                final String consumerKey = cur.getString(indices.consumer_key);
                final String consumerSecret = cur.getString(indices.consumer_secret);
                if (TwitterContentUtils.isOfficialKey(context, consumerKey, consumerSecret)) {
                    accounts.add(new ParcelableCredentials(cur, indices));
                }
            } else {
                accounts.add(new ParcelableCredentials(cur, indices));
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

    public static final class Indices {

        public final int screen_name, name, account_id, profile_image_url, profile_banner_url, color, is_activated,
                auth_type, consumer_key, consumer_secret, basic_auth_username, basic_auth_password, oauth_token,
                oauth_token_secret, api_url_format, same_oauth_signing_url, no_version_suffix;

        public Indices(@NonNull final Cursor cursor) {
            screen_name = cursor.getColumnIndex(Accounts.SCREEN_NAME);
            name = cursor.getColumnIndex(Accounts.NAME);
            account_id = cursor.getColumnIndex(Accounts.ACCOUNT_ID);
            profile_image_url = cursor.getColumnIndex(Accounts.PROFILE_IMAGE_URL);
            profile_banner_url = cursor.getColumnIndex(Accounts.PROFILE_BANNER_URL);
            color = cursor.getColumnIndex(Accounts.COLOR);
            is_activated = cursor.getColumnIndex(Accounts.IS_ACTIVATED);
            auth_type = cursor.getColumnIndex(Accounts.AUTH_TYPE);
            consumer_key = cursor.getColumnIndex(Accounts.CONSUMER_KEY);
            consumer_secret = cursor.getColumnIndex(Accounts.CONSUMER_SECRET);
            basic_auth_username = cursor.getColumnIndex(Accounts.BASIC_AUTH_USERNAME);
            basic_auth_password = cursor.getColumnIndex(Accounts.BASIC_AUTH_PASSWORD);
            oauth_token = cursor.getColumnIndex(Accounts.OAUTH_TOKEN);
            oauth_token_secret = cursor.getColumnIndex(Accounts.OAUTH_TOKEN_SECRET);
            api_url_format = cursor.getColumnIndex(Accounts.API_URL_FORMAT);
            same_oauth_signing_url = cursor.getColumnIndex(Accounts.SAME_OAUTH_SIGNING_URL);
            no_version_suffix = cursor.getColumnIndex(Accounts.NO_VERSION_SUFFIX);
        }

        @Override
        public String toString() {
            return "Indices{screen_name=" + screen_name + ", name=" + name + ", account_id=" + account_id
                    + ", profile_image_url=" + profile_image_url + ", profile_banner_url=" + profile_banner_url
                    + ", color=" + color + ", is_activated=" + is_activated + ", auth_type=" + auth_type
                    + ", consumer_key=" + consumer_key + ", consumer_secret=" + consumer_secret
                    + ", basic_auth_password=" + basic_auth_password + ", oauth_token=" + oauth_token
                    + ", oauth_token_secret=" + oauth_token_secret + ", api_url_format=" + api_url_format
                    + ", same_oauth_signing_url=" + same_oauth_signing_url + "}";
        }
    }

}
