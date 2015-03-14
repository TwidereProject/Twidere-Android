/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.model.ComposingStatus;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.DNS;
import org.mariotaku.twidere.util.TwidereArrayUtils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static android.text.TextUtils.isEmpty;

@SuppressWarnings("unused")
public final class Twidere implements TwidereConstants {

    public static void appendComposeActivityText(final Activity activity, final String text) {
        if (activity == null) return;
        final Intent intent = new Intent();
        final Bundle extras = new Bundle();
        extras.putString(EXTRA_APPEND_TEXT, text);
        intent.putExtras(extras);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
    }

    public static ParcelFileDescriptor getCachedImageFd(final Context context, final String url) {
        if (context == null || url == null) return null;
        final ContentResolver resolver = context.getContentResolver();
        final Uri.Builder builder = TwidereDataStore.CachedImages.CONTENT_URI.buildUpon();
        builder.appendQueryParameter(QUERY_PARAM_URL, url);
        try {
            return resolver.openFileDescriptor(builder.build(), "r");
        } catch (final Exception e) {
            return null;
        }
    }

    public static String getCachedImagePath(final Context context, final String url) {
        if (context == null || url == null) return null;
        final ContentResolver resolver = context.getContentResolver();
        final Uri.Builder builder = TwidereDataStore.CachedImages.CONTENT_URI.buildUpon();
        builder.appendQueryParameter(QUERY_PARAM_URL, url);
        final Cursor cur = resolver.query(builder.build(), TwidereDataStore.CachedImages.MATRIX_COLUMNS, null, null, null);
        if (cur == null) return null;
        try {
            if (cur.getCount() == 0) return null;
            final int path_idx = cur.getColumnIndex(TwidereDataStore.CachedImages.PATH);
            cur.moveToFirst();
            return cur.getString(path_idx);
        } finally {
            cur.close();
        }
    }

    public static ParcelFileDescriptor getCacheFileFd(final Context context, final String name) {
        if (context == null || name == null) return null;
        final ContentResolver resolver = context.getContentResolver();
        final Uri.Builder builder = TwidereDataStore.CacheFiles.CONTENT_URI.buildUpon();
        builder.appendQueryParameter(QUERY_PARAM_NAME, name);
        try {
            return resolver.openFileDescriptor(builder.build(), "r");
        } catch (final Exception e) {
            return null;
        }
    }

    public static ComposingStatus getComposingStatusFromIntent(final Intent intent) {
        if (intent == null) return null;
        return new ComposingStatus(intent);
    }

    public static TwidereSharedPreferences getSharedPreferences(final Context context) {
        if (context == null) return null;
        return new TwidereSharedPreferences(context);
    }

    public static ParcelableStatus getStatusFromIntent(final Intent intent) {
        if (intent == null) return null;
        return intent.getParcelableExtra(EXTRA_STATUS);
    }

    public static ParcelableUser getUserFromIntent(final Intent intent) {
        if (intent == null) return null;
        return intent.getParcelableExtra(EXTRA_USER);
    }

    public static ParcelableUserList getUserListFromIntent(final Intent intent) {
        if (intent == null) return null;
        return intent.getParcelableExtra(EXTRA_USER_LIST);
    }

    public static boolean isPermissionGranted(final Context context) {
        final ContentResolver resolver = context.getContentResolver();
        final PackageManager pm = context.getPackageManager();
        final String pname = context.getPackageName();
        final ApplicationInfo info;
        try {
            info = pm.getPackageInfo(pname, PackageManager.GET_META_DATA).applicationInfo;
        } catch (final PackageManager.NameNotFoundException e) {
            return false;
        }
        if (info.metaData == null) return false;
        final String[] required = parsePermissions(info.metaData.getString(METADATA_KEY_EXTENSION_PERMISSIONS));
        final Cursor c = resolver.query(TwidereDataStore.Permissions.CONTENT_URI, null, null, null, null);
        if (c == null) return false;
        try {
            c.moveToFirst();
            final int idxPackageName = c.getColumnIndex(TwidereDataStore.Permissions.PACKAGE_NAME), idxPermissions = c
                    .getColumnIndex(TwidereDataStore.Permissions.PERMISSION);
            while (!c.isAfterLast()) {
                if (pname.equals(c.getString(idxPackageName))) {
                    final String[] permissions = parsePermissions(c.getString(idxPermissions));
                    return TwidereArrayUtils.contains(permissions, required);
                }
                c.moveToNext();
            }
        } catch (final SecurityException ignore) {

        } finally {
            c.close();
        }
        return false;
    }

    public static String[] parsePermissions(final String permissionsString) {
        if (isEmpty(permissionsString)) return new String[0];
        return permissionsString.split(SEPARATOR_PERMISSION_REGEX);
    }

    public static void replaceComposeActivityText(final Activity activity, final String text) {
        if (activity == null) return;
        final Intent intent = new Intent();
        intent.putExtra(EXTRA_TEXT, text);
        activity.setResult(Activity.RESULT_OK, intent);
        activity.finish();
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
    public static InetAddress[] resolveHost(final Context context, final String host) throws UnknownHostException {
        if (context == null || host == null) return InetAddress.getAllByName(host);
        final ContentResolver resolver = context.getContentResolver();
        final Uri uri = Uri.withAppendedPath(DNS.CONTENT_URI, host);
        final Cursor cur = resolver.query(uri, DNS.MATRIX_COLUMNS, null, null, null);
        if (cur == null) return InetAddress.getAllByName(host);
        try {
            cur.moveToFirst();
            final ArrayList<InetAddress> addresses = new ArrayList<>();
            final int idxHost = cur.getColumnIndex(DNS.HOST), idxAddr = cur.getColumnIndex(DNS.ADDRESS);
            while (!cur.isAfterLast()) {
                addresses.add(fromAddressString(cur.getString(idxHost), cur.getString(idxAddr)));
                cur.moveToNext();
            }
            if (addresses.isEmpty()) {
                throw new UnknownHostException("Unknown host " + host);
            }
            return addresses.toArray(new InetAddress[addresses.size()]);
        } finally {
            cur.close();
        }
    }

}
