package org.mariotaku.twidere.fragment.sync

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.squareup.otto.Subscribe
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.SYNC_PREFERENCES_NAME
import org.mariotaku.twidere.constant.dataSyncProviderInfoKey
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.BasePreferenceFragment
import org.mariotaku.twidere.model.sync.SyncProviderInfo
import org.mariotaku.twidere.util.TaskServiceRunner
import org.mariotaku.twidere.util.sync.SyncProviderInfoFactory

/**
 * Created by mariotaku on 2017/1/3.
 */

class SyncSettingsFragment : BasePreferenceFragment() {

    private var providerInfo: SyncProviderInfo? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        providerInfo = kPreferences[dataSyncProviderInfoKey]
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SYNC_PREFERENCES_NAME
        addPreferencesFromResource(R.xml.preferences_sync)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sync_settings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.disconnect -> {
                val df = DisconnectSyncConfirmDialogFragment()
                df.show(childFragmentManager, "disconnect_confirm")
            }
            R.id.sync_now -> {
                val providerInfo = kPreferences[dataSyncProviderInfoKey]!!
                syncController.performSync(providerInfo)
            }
            else -> {
                return false
            }
        }
        return true
    }

    @Subscribe
    fun onSyncFinishedEvent(event: TaskServiceRunner.SyncFinishedEvent) {
        listView?.adapter?.notifyDataSetChanged()
    }

    private fun cleanupAndDisconnect() {
        val providerInfo = kPreferences[dataSyncProviderInfoKey]!!
        syncController.cleanupSyncCache(providerInfo)
        kPreferences[dataSyncProviderInfoKey] = null
        activity?.finish()
    }

    class DisconnectSyncConfirmDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            val providerInfo = kPreferences[dataSyncProviderInfoKey]!!
            val entry = SyncProviderInfoFactory.getProviderEntry(context, providerInfo.type)!!
            builder.setMessage(getString(R.string.message_sync_disconnect_from_name_confirm, entry.name))
            builder.setPositiveButton(R.string.action_sync_disconnect) { dialog, which ->
                (parentFragment as SyncSettingsFragment).cleanupAndDisconnect()
            }
            builder.setNegativeButton(android.R.string.cancel, null)
            return builder.create()
        }

    }
}
