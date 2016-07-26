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

package org.mariotaku.twidere.fragment

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.ListView
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.adapter.ExtensionsAdapter
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.loader.ExtensionsListLoader
import org.mariotaku.twidere.loader.ExtensionsListLoader.ExtensionInfo
import org.mariotaku.twidere.util.PermissionsManager

class ExtensionsListFragment : BaseListFragment(), LoaderCallbacks<List<ExtensionInfo>> {

    private var adapter: ExtensionsAdapter? = null
    private var packageManager: PackageManager? = null
    private var permissionsManager: PermissionsManager? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        packageManager = activity.packageManager
        permissionsManager = PermissionsManager(activity)
        adapter = ExtensionsAdapter(activity)
        listAdapter = adapter
        listView.setOnCreateContextMenuListener(this)
        loaderManager.initLoader(0, null, this)
        setEmptyText(getString(R.string.no_extension_installed))
        setListShown(false)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ExtensionInfo>> {
        return ExtensionsListLoader(activity, packageManager)
    }

    override fun onLoadFinished(loader: Loader<List<ExtensionInfo>>, data: List<ExtensionInfo>) {
        adapter!!.setData(data)
        setListShown(true)
    }

    override fun onLoaderReset(loader: Loader<List<ExtensionInfo>>) {
        adapter!!.setData(null)
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        openSettings(adapter!!.getItem(position))
    }

    override fun onResume() {
        super.onResume()
        adapter!!.notifyDataSetChanged()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        val inflater = MenuInflater(v.context)
        inflater.inflate(R.menu.action_extension, menu)
        val adapterMenuInfo = menuInfo as AdapterContextMenuInfo
        val extensionInfo = adapter!!.getItem(adapterMenuInfo.position)
        if (extensionInfo.pname != null && extensionInfo.settings != null) {
            val intent = Intent(IntentConstants.INTENT_ACTION_EXTENSION_SETTINGS)
            intent.setClassName(extensionInfo.pname, extensionInfo.settings)
            menu.setItemAvailability(R.id.settings, packageManager!!.queryIntentActivities(intent, 0).size == 1)
        } else {
            menu.setItemAvailability(R.id.settings, false)
        }

    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        val adapterMenuInfo = item!!.menuInfo as AdapterContextMenuInfo
        val extensionInfo = adapter!!.getItem(adapterMenuInfo.position)
        when (item.itemId) {
            R.id.settings -> {
                openSettings(extensionInfo)
            }
            R.id.delete -> {
                uninstallExtension(extensionInfo)
            }
            R.id.revoke -> {
                permissionsManager!!.revoke(extensionInfo.pname)
                adapter!!.notifyDataSetChanged()
            }
            else -> {
                return false
            }
        }
        return true
    }

    private fun openSettings(info: ExtensionInfo): Boolean {
        val intent = Intent(IntentConstants.INTENT_ACTION_EXTENSION_SETTINGS)
        intent.`package` = info.pname
        if (info.settings != null) {
            intent.setClassName(info.pname, info.settings)
        } else {
            val pm = activity.packageManager
            val activities = pm.queryIntentActivities(intent, 0)
            if (activities.isEmpty()) {
                return false
            }
            val resolveInfo = activities[0]
            intent.setClassName(info.pname, resolveInfo.activityInfo.name)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.w(LOGTAG, e)
            return false
        }

        return true
    }

    private fun uninstallExtension(info: ExtensionInfo?): Boolean {
        if (info == null) return false
        val packageUri = Uri.parse("package:${info.pname}")
        val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri)
        try {
            startActivity(uninstallIntent)
        } catch (e: Exception) {
            Log.w(LOGTAG, e)
            return false
        }

        return true
    }

}
