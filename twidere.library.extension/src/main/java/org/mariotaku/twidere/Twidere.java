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

package org.mariotaku.twidere;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import org.mariotaku.twidere.model.AccountDetails;
import org.mariotaku.twidere.model.ComposingStatus;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.Permissions;
import org.mariotaku.twidere.util.JsonSerializer;
import org.mariotaku.twidere.util.model.AccountDetailsUtils;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static android.text.TextUtils.isEmpty;

@SuppressWarnings("unused")
public final class Twidere implements TwidereConstants {

    public static void setComposeExtensionResult(@NonNull final Activity activity,
            @Nullable final String text, final boolean isReplacementMode, @Nullable Uri[] media) {
        final Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra(EXTRA_IS_REPLACE_MODE, isReplacementMode);

        if (media != null && media.length > 0) {
            intent.setData(media[0]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clipData = ClipData.newUri(activity.getContentResolver(), "Media",
                        media[0]);
                for (int i = 1, j = media.length; i < j; i++) {
                    Uri uri = media[i];
                    clipData.addItem(new ClipData.Item(uri));
                }
                intent.setClipData(clipData);
            }
        }
        intent.putExtra(EXTRA_IS_REPLACE_MODE, isReplacementMode);
        activity.setResult(Activity.RESULT_OK, intent);
    }

    public static ComposingStatus getComposingStatusFromIntent(@NonNull final Intent intent) {
        return new ComposingStatus(intent);
    }

    public static ParcelableStatus getStatusFromIntent(@NonNull final Intent intent) {
        return intent.getParcelableExtra(EXTRA_STATUS);
    }

    public static ParcelableUser getUserFromIntent(@NonNull final Intent intent) {
        return intent.getParcelableExtra(EXTRA_USER);
    }

    public static ParcelableUserList getUserListFromIntent(@NonNull final Intent intent) {
        return intent.getParcelableExtra(EXTRA_USER_LIST);
    }

    @Permission
    public static int isPermissionGranted(@NonNull final Context context) {
        final PackageManager pm = context.getPackageManager();
        final String pname = context.getPackageName();
        final ApplicationInfo info;
        try {
            info = pm.getPackageInfo(pname, PackageManager.GET_META_DATA).applicationInfo;
        } catch (final PackageManager.NameNotFoundException e) {
            return Permission.NONE;
        }
        if (info.metaData == null) return Permission.NONE;
        final String[] required = parsePermissions(info.metaData.getString(METADATA_KEY_EXTENSION_PERMISSIONS));
        final String[] permissions = getPermissions(context, pname);
        return checkPermissionRequirement(required, permissions);
    }

    public static int checkPermissionRequirement(@NonNull String[] required, @NonNull String[] permissions) {
        if (indexOf(permissions, PERMISSION_DENIED) != -1) {
            return Permission.DENIED;
        } else {
            for (String s : required) {
                if (indexOf(permissions, s) == -1) return Permission.NONE;
            }
            return Permission.GRANTED;
        }
    }

    @NonNull
    public static String[] getPermissions(@NonNull final Context context, @NonNull String pname) {
        final ContentResolver resolver = context.getContentResolver();
        final Cursor c = resolver.query(Permissions.CONTENT_URI, null, null, null, null);
        if (c == null) return new String[0];
        try {
            c.moveToFirst();
            final int idxPackageName = c.getColumnIndex(Permissions.PACKAGE_NAME), idxPermissions = c
                    .getColumnIndex(Permissions.PERMISSION);
            while (!c.isAfterLast()) {
                if (pname.equals(c.getString(idxPackageName))) {
                    return parsePermissions(c.getString(idxPermissions));
                }
                c.moveToNext();
            }
        } catch (final SecurityException ignore) {

        } finally {
            c.close();
        }
        return new String[0];
    }

    @NonNull
    public static String[] parsePermissions(final String permissionsString) {
        if (isEmpty(permissionsString)) return new String[0];
        return permissionsString.split(SEPARATOR_PERMISSION_REGEX);
    }

    @Nullable
    @RequiresPermission(allOf = {Manifest.permission.GET_ACCOUNTS}, conditional = true)
    public static Account findByAccountKey(@NonNull Context context, @NonNull UserKey userKey) {
        final AccountManager am = AccountManager.get(context);
        for (Account account : am.getAccountsByType(ACCOUNT_TYPE)) {
            if (userKey.equals(getAccountKey(account, am))) {
                return account;
            }
        }
        return null;
    }

    @RequiresPermission(allOf = {"android.permission.AUTHENTICATE_ACCOUNTS"}, conditional = true)
    @NonNull
    public static AccountDetails getAccountDetails(Context context, Account account) throws SecurityException {
        final AccountManager am = AccountManager.get(context);
        final AccountDetails details = new AccountDetails();
        details.account = account;
        details.key = getAccountKey(account, am);
        //noinspection WrongConstant
        details.type = am.getUserData(account, ACCOUNT_USER_DATA_TYPE);
        details.color = Color.parseColor(am.getUserData(account, ACCOUNT_USER_DATA_COLOR));
        details.position = Integer.parseInt(am.getUserData(account, ACCOUNT_USER_DATA_POSITION));
        //noinspection WrongConstant
        details.credentials_type = am.getUserData(account, ACCOUNT_USER_DATA_CREDS_TYPE);
        try {
            details.user = JsonSerializer.parse(am.getUserData(account, ACCOUNT_USER_DATA_USER),
                    ParcelableUser.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        details.activated = Boolean.parseBoolean(am.getUserData(account, ACCOUNT_USER_DATA_ACTIVATED));

        try {
            details.credentials = AccountDetailsUtils.parseCredentials(am.peekAuthToken(account, ACCOUNT_AUTH_TOKEN_TYPE),
                    details.credentials_type);
        } catch (SecurityException e) {
            // Ignore
        }
        details.extras = AccountDetailsUtils.parseAccountExtras(am.getUserData(account, ACCOUNT_USER_DATA_EXTRAS), details.type);

        details.user.color = details.color;

        return details;
    }

    private static InetAddress fromAddressString(String host, String address) throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getByName(address);
        if (inetAddress instanceof Inet4Address) {
            return Inet4Address.getByAddress(host, inetAddress.getAddress());
        } else if (inetAddress instanceof Inet6Address) {
            return Inet6Address.getByAddress(host, inetAddress.getAddress());
        }
        throw new UnknownHostException("Bad address " + host + " = " + address);
    }

    @NonNull
    @RequiresPermission(allOf = {"android.permission.AUTHENTICATE_ACCOUNTS"}, conditional = true)
    private static UserKey getAccountKey(Account account, AccountManager am) {
        return UserKey.valueOf(am.getUserData(account, ACCOUNT_USER_DATA_KEY));
    }

    private static int indexOf(String[] input, String find) {
        for (int i = 0, inputLength = input.length; i < inputLength; i++) {
            if (find == null) {
                if (input[i] == null) return i;
            } else if (find.equals(input[i])) return i;
        }
        return -1;
    }

    @IntDef({Permission.DENIED, Permission.NONE, Permission.GRANTED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Permission {
        int NONE = 0;
        int GRANTED = 1;
        int DENIED = -1;
    }


}
