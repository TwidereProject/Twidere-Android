package org.mariotaku.twidere.fragment.premium

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_extra_features_sync_status.*
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.REQUEST_PURCHASE_EXTRA_FEATURES
import org.mariotaku.twidere.activity.FragmentContentActivity
import org.mariotaku.twidere.constant.dataSyncProviderInfoKey
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.BaseFragment
import org.mariotaku.twidere.fragment.ExtraFeaturesIntroductionDialogFragment
import org.mariotaku.twidere.fragment.sync.SyncSettingsFragment
import org.mariotaku.twidere.model.analyzer.PurchaseFinished
import org.mariotaku.twidere.model.sync.SyncProviderEntry
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.sync.SyncProviderInfoFactory

/**
 * Created by mariotaku on 2016/12/28.
 */

class SyncStatusFragment : BaseFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateSyncSettingActions()
        connectButton.setOnClickListener {
            if (!extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_SYNC_DATA)) {
                showExtraFeaturesIntroduction()
                return@setOnClickListener
            }
            val df = ConnectNetworkStorageSelectionDialogFragment()
            df.show(childFragmentManager, "connect_to_storage")
        }
        settingsButton.setOnClickListener {
            if (!extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_SYNC_DATA)) {
                showExtraFeaturesIntroduction()
                return@setOnClickListener
            }
            val intent = Intent(context, FragmentContentActivity::class.java)
            intent.putExtra(FragmentContentActivity.EXTRA_FRAGMENT, SyncSettingsFragment::class.java.name)
            intent.putExtra(FragmentContentActivity.EXTRA_TITLE, getString(R.string.title_sync_settings))
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CONNECT_NETWORK_STORAGE -> {
                updateSyncSettingActions()
            }
            REQUEST_PURCHASE_EXTRA_FEATURES -> {
                if (resultCode == Activity.RESULT_OK) {
                    Analyzer.log(PurchaseFinished.create(data!!))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateSyncSettingActions()
    }

    private fun showExtraFeaturesIntroduction() {
        ExtraFeaturesIntroductionDialogFragment.show(childFragmentManager,
                feature = ExtraFeaturesService.FEATURE_SYNC_DATA,
                requestCode = REQUEST_PURCHASE_EXTRA_FEATURES)
    }

    private fun updateSyncSettingActions() {
        val providerInfo = preferences[dataSyncProviderInfoKey]
        if (providerInfo == null) {
            statusText.text = getText(R.string.message_sync_data_connect_hint)
            connectButton.visibility = View.VISIBLE
            settingsButton.visibility = View.GONE
        } else {
            val providerEntry = SyncProviderInfoFactory.getProviderEntry(context, providerInfo.type)!!
            statusText.text = getString(R.string.message_sync_data_synced_with_name, providerEntry.name)
            connectButton.visibility = View.GONE
            settingsButton.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_extra_features_sync_status, container, false)
    }

    class ConnectNetworkStorageSelectionDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val providers = SyncProviderInfoFactory.getSupportedProviders(context)
            val itemNames = providers.map { it.name }.toTypedArray()

            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.title_dialog_sync_connect_to)
            builder.setItems(itemNames) { dialog, which ->
                activity.startActivityForResult(providers[which].authIntent, REQUEST_CONNECT_NETWORK_STORAGE)
            }
            return builder.create()
        }
    }

    companion object {
        private val REQUEST_CONNECT_NETWORK_STORAGE: Int = 201
    }

}
