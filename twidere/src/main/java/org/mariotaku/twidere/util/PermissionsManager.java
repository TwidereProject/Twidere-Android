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

package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Process;
import android.text.TextUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;

import java.util.HashMap;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

public class PermissionsManager implements Constants {

    private static final String[] PERMISSIONS_DENIED = {PERMISSION_DENIED};

    private final SharedPreferencesWrapper mPreferences;
    private final PackageManager mPackageManager;
    private final Context mContext;

    public PermissionsManager(final Context context) {
        mContext = context;
        mPreferences = SharedPreferencesWrapper.getInstance(context, PERMISSION_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mPackageManager = context.getPackageManager();
    }

    public boolean accept(final String packageName, final String[] permissions) {
        if (packageName == null || permissions == null) return false;
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(packageName, TwidereArrayUtils.toString(permissions, '|', false));
        return editor.commit();
    }

    public boolean checkCallingPermission(final String... requiredPermissions) {
        return checkPermission(Binder.getCallingUid(), requiredPermissions);
    }

    public boolean checkPermission(final int uid, final String... requiredPermissions) {
        if (requiredPermissions == null || requiredPermissions.length == 0) return true;
        if (Process.myUid() == uid) return true;
        if (checkSignature(uid)) return true;
        final String pname = getPackageNameByUid(uid);
        final String[] permissions = getPermissions(pname);
        return TwidereArrayUtils.contains(permissions, requiredPermissions);
    }

    public boolean checkPermission(final String packageName, final String... requiredPermissions) {
        if (requiredPermissions == null || requiredPermissions.length == 0) return true;
        if (mContext.getPackageName().equals(packageName)) return true;
        if (checkSignature(packageName)) return true;
        final String[] permissions = getPermissions(packageName);
        return TwidereArrayUtils.contains(permissions, requiredPermissions);
    }

    public boolean checkSignature(final int uid) {
        return checkSignature(getPackageNameByUid(uid));
    }

    public boolean checkSignature(final String pname) {
        if (mContext.getPackageName().equals(pname)) return true;
        if (BuildConfig.DEBUG) return false;
        return mPackageManager.checkSignatures(pname, mContext.getPackageName()) == PackageManager.SIGNATURE_MATCH;
    }

    public boolean deny(final String packageName) {
        if (packageName == null) return false;
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(packageName, PERMISSION_DENIED);
        return editor.commit();

    }

    public Map<String, String> getAll() {
        final Map<String, String> map = new HashMap<>();
        for (final Map.Entry<String, ?> entry : mPreferences.getAll().entrySet()) {
            if (entry.getValue() instanceof String) {
                map.put(entry.getKey(), (String) entry.getValue());
            }
        }
        return map;
    }

    public String getPackageNameByUid(final int uid) {
        final String[] pkgs = mPackageManager.getPackagesForUid(uid);
        if (pkgs != null && pkgs.length > 0) return pkgs[0];
        return null;
    }

    public String[] getPermissions(final int uid) {
        return getPermissions(getPackageNameByUid(uid));
    }

    public String[] getPermissions(final String packageName) {
        if (isEmpty(packageName)) return new String[0];
        final String permissionsString = mPreferences.getString(packageName, null);
        if (isEmpty(permissionsString)) return new String[0];
        if (permissionsString.contains(PERMISSION_DENIED)) return PERMISSIONS_DENIED;
        return permissionsString.split("\\|");
    }

    public boolean revoke(final String packageName) {
        if (packageName == null) return false;
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(packageName);
        return editor.commit();
    }

    public static boolean hasPermissions(final String[] permissions, final String... requiredPermissions) {
        return TwidereArrayUtils.contains(permissions, requiredPermissions);
    }

    public static boolean isPermissionValid(final String permissionsString) {
        return TextUtils.isEmpty(permissionsString);
    }

    public static boolean isPermissionValid(final String... permissions) {
        return permissions != null && permissions.length != 0;
    }

    public static String[] parsePermissions(final String permissionsString) {
        if (isEmpty(permissionsString)) return new String[0];
        return permissionsString.split(SEPARATOR_PERMISSION_REGEX);
    }

    public boolean isDenied(String packageName) {
        return ArrayUtils.contains(getPermissions(packageName), PERMISSION_DENIED);
    }
}
