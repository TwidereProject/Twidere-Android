/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2018 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Process
import android.text.TextUtils
import android.text.TextUtils.isEmpty
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder

class PermissionsManager private constructor(private val context: Context) {

    private val preferences = context.getSharedPreferences(PERMISSION_PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val packageManager = context.packageManager

    val all: Map<String, String>
        get() = preferences.all.filterValues { it is String }.mapValues { (_, v) -> v as String }

    fun accept(packageName: String?, permissions: Array<String>?): Boolean {
        if (packageName == null || permissions == null) return false
        val editor = preferences.edit()
        editor.putString(packageName, TwidereArrayUtils.toString(permissions, '|', false))
        return editor.commit()
    }

    fun checkCallingPermission(vararg requiredPermissions: String): Boolean {
        return checkPermission(Binder.getCallingUid(), *requiredPermissions)
    }

    fun checkPermission(uid: Int, vararg requiredPermissions: String): Boolean {
        if (requiredPermissions.isEmpty()) return true
        if (Process.myUid() == uid) return true
        if (checkSignature(uid)) return true
        val pname = getPackageNameByUid(uid)
        val permissions = getPermissions(pname)
        return TwidereArrayUtils.contains(permissions, requiredPermissions)
    }

    fun checkPermission(packageName: String, vararg requiredPermissions: String): Boolean {
        if (requiredPermissions.isEmpty()) return true
        if (context.packageName == packageName) return true
        if (checkSignature(packageName)) return true
        val permissions = getPermissions(packageName)
        return TwidereArrayUtils.contains(permissions, requiredPermissions)
    }

    fun checkSignature(uid: Int): Boolean {
        return checkSignature(getPackageNameByUid(uid))
    }

    fun checkSignature(pname: String?): Boolean {
        if (context.packageName == pname) return true
        return if (BuildConfig.DEBUG) false else packageManager.checkSignatures(pname, context.packageName) == PackageManager.SIGNATURE_MATCH
    }

    fun deny(packageName: String?): Boolean {
        if (packageName == null) return false
        val editor = preferences.edit()
        editor.putString(packageName, PERMISSION_DENIED)
        return editor.commit()

    }

    fun getPackageNameByUid(uid: Int): String? {
        return packageManager.getPackagesForUid(uid)?.firstOrNull()
    }

    fun getPermissions(uid: Int): Array<String> {
        return getPermissions(getPackageNameByUid(uid))
    }

    fun getPermissions(packageName: String?): Array<String> {
        if (isEmpty(packageName)) return emptyArray()
        val permissionsString = preferences.getString(packageName, null)
        if (isEmpty(permissionsString)) return emptyArray()
        return if (permissionsString!!.contains(PERMISSION_DENIED)) PERMISSIONS_DENIED else permissionsString.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    fun revoke(packageName: String?): Boolean {
        if (packageName == null) return false
        val editor = preferences.edit()
        editor.remove(packageName)
        return editor.commit()
    }

    fun isDenied(packageName: String): Boolean {
        return getPermissions(packageName).contains(PERMISSION_DENIED)
    }

    companion object : ApplicationContextSingletonHolder<PermissionsManager>(::PermissionsManager) {

        private val PERMISSIONS_DENIED = arrayOf(PERMISSION_DENIED)

        fun hasPermissions(permissions: Array<String>, vararg requiredPermissions: String): Boolean {
            return TwidereArrayUtils.contains(permissions, requiredPermissions)
        }

        fun isPermissionValid(permissionsString: String): Boolean {
            return TextUtils.isEmpty(permissionsString)
        }

        fun isPermissionValid(vararg permissions: String): Boolean {
            return permissions.isNotEmpty()
        }

        fun parsePermissions(permissionsString: String): Array<String> {
            return permissionsString.split(SEPARATOR_PERMISSION).filterNot { it.isBlank() }.toTypedArray()
        }
    }
}
