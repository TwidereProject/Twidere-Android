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

package org.mariotaku.twidere.activity

import android.app.Activity
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils.isEmpty
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import kotlinx.android.synthetic.main.activity_request_permissions.*
import kotlinx.android.synthetic.main.activity_request_permissions.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.util.HtmlEscapeHelper
import org.mariotaku.twidere.util.PermissionsManager

class RequestPermissionsActivity : BaseActivity(), OnClickListener {

    private var permissions: Array<String>? = null

    override fun onClick(view: View) {
        when (view) {
            buttonsContainer.accept -> {
                permissionsManager.accept(callingPackage, permissions)
                setResult(Activity.RESULT_OK)
                finish()
            }
            buttonsContainer.deny -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_permissions)
        buttonsContainer.accept.setOnClickListener(this)
        buttonsContainer.deny.setOnClickListener(this)
        val caller = callingPackage
        if (caller == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        if (permissionsManager.isDenied(caller)) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }
        loadInfo(caller)
    }

    private fun appendPermission(sb: SpannableStringBuilder, name: String, danger: Boolean) {
        if (danger) {
            val start = sb.length
            sb.append(name)
            val end = sb.length
            sb.setSpan(ForegroundColorSpan(0xffff8000.toInt()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            sb.append(name)
        }
    }

    private fun loadInfo(pname: String) {
        val pm = packageManager
        try {
            val info = pm.getApplicationInfo(pname, PackageManager.GET_META_DATA)
            val meta = info.metaData
            if (meta == null || !meta.getBoolean(METADATA_KEY_EXTENSION)) {
                setResult(Activity.RESULT_CANCELED)
                finish()
                return
            }
            iconView.setImageDrawable(info.loadIcon(pm))
            nameView.text = info.loadLabel(pm)
            val desc = info.loadDescription(pm)
            descriptionView.text = desc
            descriptionView.visibility = if (isEmpty(desc)) View.GONE else View.VISIBLE
            val permissions = PermissionsManager.parsePermissions(meta.getString(METADATA_KEY_EXTENSION_PERMISSIONS))
            this.permissions = permissions
            val builder = SpannableStringBuilder()
            builder.append(HtmlEscapeHelper.escape(getString(R.string.permissions_request_message)))
            builder.append("\n")
            if (PermissionsManager.isPermissionValid(*permissions)) {
                if (PermissionsManager.hasPermissions(permissions, PERMISSION_PREFERENCES)) {
                    appendPermission(builder, getString(R.string.permission_description_preferences), true)
                }
                if (PermissionsManager.hasPermissions(permissions, PERMISSION_ACCOUNTS)) {
                    appendPermission(builder, getString(R.string.permission_description_accounts), true)
                }
                if (PermissionsManager.hasPermissions(permissions, PERMISSION_DIRECT_MESSAGES)) {
                    appendPermission(builder, getString(R.string.permission_description_direct_messages), true)
                }
                if (PermissionsManager.hasPermissions(permissions, PERMISSION_WRITE)) {
                    appendPermission(builder, getString(R.string.permission_description_write), false)
                }
                if (PermissionsManager.hasPermissions(permissions, PERMISSION_READ)) {
                    appendPermission(builder, getString(R.string.permission_description_read), false)
                }
                if (PermissionsManager.hasPermissions(permissions, PERMISSION_REFRESH)) {
                    appendPermission(builder, getString(R.string.permission_description_refresh), false)
                }
            } else {
                appendPermission(builder, getString(R.string.permission_description_none), false)
            }
            messageView.text = builder
        } catch (e: NameNotFoundException) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

    }

}
