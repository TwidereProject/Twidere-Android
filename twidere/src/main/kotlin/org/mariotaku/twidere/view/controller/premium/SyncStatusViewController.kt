package org.mariotaku.twidere.view.controller.premium

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.REQUEST_PURCHASE_EXTRA_FEATURES
import org.mariotaku.twidere.activity.FragmentContentActivity
import org.mariotaku.twidere.activity.PremiumDashboardActivity
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_POSITION
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.ExtraFeaturesIntroductionDialogFragment
import org.mariotaku.twidere.fragment.sync.SyncSettingsFragment
import org.mariotaku.twidere.model.sync.SyncProviderInfo
import org.mariotaku.twidere.util.premium.ExtraFeaturesService

class SyncStatusViewController : PremiumDashboardActivity.ExtraFeatureViewController() {

    override fun onCreate() {
        super.onCreate()
        updateSyncSettingActions()
        titleView.setText(R.string.title_sync)
        button1.setText(R.string.action_sync_connect_to_storage)
        button2.setText(R.string.action_sync_settings)
        button1.setOnClickListener {
            if (!extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_SYNC_DATA)) {
                showExtraFeaturesIntroduction()
                return@setOnClickListener
            }
            val df = ConnectNetworkStorageSelectionDialogFragment()
            df.arguments = Bundle { this[EXTRA_POSITION] = position }
            df.show(activity.supportFragmentManager, "connect_to_storage")
        }
        button2.setOnClickListener {
            if (!extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_SYNC_DATA)) {
                showExtraFeaturesIntroduction()
                return@setOnClickListener
            }
            val intent = Intent(context, FragmentContentActivity::class.java)
            intent.putExtra(FragmentContentActivity.EXTRA_FRAGMENT, SyncSettingsFragment::class.java.name)
            intent.putExtra(FragmentContentActivity.EXTRA_TITLE, context.getString(R.string.title_sync_settings))
            activity.startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateSyncSettingActions()
    }

    private fun updateSyncSettingActions() {
        val providerConfig = dataSyncProvider.providerConfig
        if (providerConfig == null) {
            messageView.text = context.getString(R.string.message_sync_data_connect_hint)
            button1.visibility = View.VISIBLE
            button2.visibility = View.GONE

            if (extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_SYNC_DATA)) {
                button1.setText(R.string.action_sync_connect_to_storage)
            } else {
                button1.setText(R.string.action_purchase)
            }
        } else {
            val info = dataSyncProvider.providerInfo(providerConfig.type)!!
            messageView.text = context.getString(R.string.message_sync_data_synced_with_name, info.name)
            button1.visibility = View.GONE
            button2.visibility = View.VISIBLE
        }
    }

    private fun showExtraFeaturesIntroduction() {
        ExtraFeaturesIntroductionDialogFragment.show(activity.supportFragmentManager,
                feature = ExtraFeaturesService.FEATURE_SYNC_DATA,
                requestCode = REQUEST_PURCHASE_EXTRA_FEATURES)
    }


    class ConnectNetworkStorageSelectionDialogFragment : BaseDialogFragment() {
        private val position: Int
            get() = arguments!!.getInt(EXTRA_POSITION)

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val providers = dataSyncProvider.supportedProviders()
            val itemNames = providers.mapToArray(SyncProviderInfo::name)

            val builder = AlertDialog.Builder(context!!)
            builder.setTitle(R.string.title_dialog_sync_connect_to)
            builder.setItems(itemNames) { _, which ->
                val activity = activity as PremiumDashboardActivity
                activity.startActivityForControllerResult(providers[which].authIntent,
                        position, REQUEST_CONNECT_NETWORK_STORAGE)
            }
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CONNECT_NETWORK_STORAGE -> {
                updateSyncSettingActions()
            }
        }
    }

    companion object {
        private val REQUEST_CONNECT_NETWORK_STORAGE: Int = 201
    }
}
