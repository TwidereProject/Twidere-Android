package org.mariotaku.twidere.fragment.sync

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.squareup.otto.Subscribe
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.ui.alwaysUi
import org.mariotaku.ktextension.weak
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.SYNC_PREFERENCES_NAME
import org.mariotaku.twidere.constant.dataSyncProviderInfoKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.dismissProgressDialog
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.extension.showProgressDialog
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.BasePreferenceFragment
import org.mariotaku.twidere.util.TaskServiceRunner
import org.mariotaku.twidere.util.sync.DataSyncProvider

/**
 * Created by mariotaku on 2017/1/3.
 */

class SyncSettingsFragment : BasePreferenceFragment() {

    private var syncProvider: DataSyncProvider? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        syncProvider = kPreferences[dataSyncProviderInfoKey]
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
        val providerInfo = kPreferences[dataSyncProviderInfoKey] ?: return
        val weakThis = weak()
        val task = showProgressDialog("cleanup_sync_cache").
                and(syncController.cleanupSyncCache(providerInfo))
        task.alwaysUi {
            val f = weakThis.get() ?: return@alwaysUi
            f.dismissProgressDialog("cleanup_sync_cache")
            f.kPreferences[dataSyncProviderInfoKey] = null
            f.context?.let { DataSyncProvider.Factory.notifyUpdate(it) }
            f.activity?.finish()
        }
    }

    class DisconnectSyncConfirmDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireContext())
            val providerInfo = kPreferences[dataSyncProviderInfoKey]!!
            val entry = DataSyncProvider.Factory.getProviderEntry(requireContext(), providerInfo.type)!!
            builder.setMessage(getString(R.string.message_sync_disconnect_from_name_confirm, entry.name))
            builder.setPositiveButton(R.string.action_sync_disconnect) { _, _ ->
                (parentFragment as SyncSettingsFragment).cleanupAndDisconnect()
            }
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }

    }
}
