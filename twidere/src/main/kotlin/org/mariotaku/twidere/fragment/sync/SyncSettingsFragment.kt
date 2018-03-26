package org.mariotaku.twidere.fragment.sync

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.squareup.otto.Subscribe
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.ui.alwaysUi
import org.mariotaku.ktextension.toWeak
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.SYNC_PREFERENCES_NAME
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.BasePreferenceFragment
import org.mariotaku.twidere.singleton.BusSingleton
import org.mariotaku.twidere.util.TaskServiceRunner

class SyncSettingsFragment : BasePreferenceFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        BusSingleton.register(this)
    }

    override fun onStop() {
        BusSingleton.unregister(this)
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
                dataSyncProvider.newSyncTaskRunner()?.syncAll()
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
        val taskRunner = dataSyncProvider.newSyncTaskRunner() ?: return
        val weakThis = toWeak()
        val task = showProgressDialog("cleanup_sync_cache").
                and(taskRunner.cleanupSyncCache())
        task.alwaysUi {
            val f = weakThis.get() ?: return@alwaysUi
            f.dismissProgressDialog("cleanup_sync_cache")
            dataSyncProvider.providerConfig = null
            f.activity?.finish()
        }
    }

    class DisconnectSyncConfirmDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context!!)
            val providerInfo = dataSyncProvider.providerInfo(dataSyncProvider.providerConfig!!.type)!!
            builder.setMessage(getString(R.string.message_sync_disconnect_from_name_confirm, providerInfo.name))
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
