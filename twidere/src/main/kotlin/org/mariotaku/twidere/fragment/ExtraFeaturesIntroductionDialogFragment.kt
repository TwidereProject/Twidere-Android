package org.mariotaku.twidere.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import org.mariotaku.twidere.R
import org.mariotaku.twidere.util.premium.ExtraFeaturesChecker

/**
 * Created by mariotaku on 2016/12/25.
 */

class ExtraFeaturesIntroductionDialogFragment : BaseDialogFragment() {

    private lateinit var extraFeaturesChecker: ExtraFeaturesChecker

    override fun onDestroy() {
        extraFeaturesChecker.release()
        super.onDestroy()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        extraFeaturesChecker = ExtraFeaturesChecker.newInstance(context)
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.title_extra_features)
        builder.setView(R.layout.dialog_extra_features_introduction)
        builder.setPositiveButton(R.string.action_purchase) { dialog, which ->
            startActivity(extraFeaturesChecker.createPurchaseIntent(context))
        }
        builder.setNegativeButton(R.string.action_later) { dialog, which ->

        }
        val restorePurchaseIntent = extraFeaturesChecker.createRestorePurchaseIntent(context)
        if (restorePurchaseIntent != null) {
            builder.setNeutralButton(R.string.action_restore_purchase) { dialog, which ->
                startActivity(restorePurchaseIntent)
            }
        }
        val dialog = builder.create()
        dialog.setOnShowListener {
            it as Dialog
            it.findViewById(R.id.restorePurchaseHint).visibility = if (restorePurchaseIntent != null) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        return dialog
    }
}
