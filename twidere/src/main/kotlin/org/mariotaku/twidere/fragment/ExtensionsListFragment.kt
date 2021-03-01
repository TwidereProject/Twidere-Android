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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.Loader
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.AdapterContextMenuInfo
import androidx.loader.app.LoaderManager
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.fragment_content_listview.*
import org.mariotaku.ktextension.isNullOrEmpty
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.adapter.ExtensionsAdapter
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.loader.ExtensionsListLoader
import org.mariotaku.twidere.loader.ExtensionsListLoader.ExtensionInfo

class ExtensionsListFragment : AbsContentListViewFragment<ExtensionsAdapter>(),
        LoaderCallbacks<List<ExtensionInfo>>, AdapterView.OnItemClickListener {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        listView.onItemClickListener = this

        LoaderManager.getInstance(this).initLoader(0, null, this)
        showProgress()
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): ExtensionsAdapter {
        return ExtensionsAdapter(requireActivity(), requestManager)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ExtensionInfo>> {
        return ExtensionsListLoader(requireActivity())
    }

    override fun onLoadFinished(loader: Loader<List<ExtensionInfo>>, data: List<ExtensionInfo>) {
        adapter.setData(data)
        if (data.isNullOrEmpty()) {
            showEmpty(R.drawable.ic_info_info_generic, getString(R.string.no_extension_installed))
        } else {
            showContent()
        }
    }

    override fun onLoaderReset(loader: Loader<List<ExtensionInfo>>) {
        adapter.setData(null)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        openSettings(adapter.getItem(position))
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        val inflater = MenuInflater(v.context)
        inflater.inflate(R.menu.action_extension, menu)
        val adapterMenuInfo = menuInfo as AdapterContextMenuInfo
        val extensionInfo = adapter.getItem(adapterMenuInfo.position)
        if (extensionInfo.settings != null) {
            val intent = Intent(IntentConstants.INTENT_ACTION_EXTENSION_SETTINGS)
            intent.setClassName(extensionInfo.packageName, extensionInfo.settings)
            menu.setItemAvailability(R.id.settings, context?.packageManager?.queryIntentActivities(intent, 0)?.size == 1)
        } else {
            menu.setItemAvailability(R.id.settings, false)
        }

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val adapterMenuInfo = item.menuInfo as AdapterContextMenuInfo
        val extensionInfo = adapter.getItem(adapterMenuInfo.position)
        when (item.itemId) {
            R.id.settings -> {
                openSettings(extensionInfo)
            }
            R.id.delete -> {
                uninstallExtension(extensionInfo)
            }
            R.id.revoke -> {
                permissionsManager.revoke(extensionInfo.packageName)
                adapter.notifyDataSetChanged()
            }
            else -> {
                return false
            }
        }
        return true
    }

    private fun openSettings(info: ExtensionInfo): Boolean {
        val intent = Intent(IntentConstants.INTENT_ACTION_EXTENSION_SETTINGS)
        intent.`package` = info.packageName
        if (info.settings != null) {
            intent.setClassName(info.packageName, info.settings)
        } else {
            val pm = requireActivity().packageManager
            val activities = pm.queryIntentActivities(intent, 0)
            if (activities.isEmpty()) {
                return false
            }
            val resolveInfo = activities[0]
            intent.setClassName(info.packageName, resolveInfo.activityInfo.name)
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
        val packageUri = Uri.parse("package:${info.packageName}")
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
