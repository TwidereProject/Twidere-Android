package org.mariotaku.twidere.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_REQUEST_CODE
import org.mariotaku.twidere.model.analyzer.PurchaseConfirm
import org.mariotaku.twidere.model.analyzer.PurchaseFinished
import org.mariotaku.twidere.model.analyzer.PurchaseIntroduction
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.premium.ExtraFeaturesService

/**
 * Created by mariotaku on 2016/12/25.
 */

class ExtraFeaturesIntroductionDialogFragment : BaseDialogFragment() {

    private lateinit var extraFeaturesService: ExtraFeaturesService

    val feature: String get() = arguments.getString(EXTRA_FEATURE)
    val requestCode: Int get() = arguments.getInt(EXTRA_REQUEST_CODE, 0)

    override fun onDestroy() {
        extraFeaturesService.release()
        super.onDestroy()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        extraFeaturesService = ExtraFeaturesService.newInstance(context)
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.title_extra_features)
        builder.setView(R.layout.dialog_extra_features_introduction)
        builder.setPositiveButton(R.string.action_purchase) { dialog, which ->
            startPurchase(feature)
            Analyzer.log(PurchaseConfirm(PurchaseFinished.NAME_EXTRA_FEATURES))
        }
        builder.setNegativeButton(R.string.action_later) { dialog, which ->

        }
        val restorePurchaseIntent = extraFeaturesService.createRestorePurchaseIntent(context, feature)
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
            val description = ExtraFeaturesService.getIntroduction(context, feature)
            val featureIcon = it.findViewById(R.id.featureIcon) as ImageView
            val featureDescription = it.findViewById(R.id.featureDescription) as TextView
            featureIcon.setImageResource(description.icon)
            featureDescription.text = description.description
            it.findViewById(R.id.buyFeaturesPack).setOnClickListener {
                startPurchase(ExtraFeaturesService.FEATURE_FEATURES_PACK)
                dismiss()
            }
        }
        if (savedInstanceState == null) {
            Analyzer.log(PurchaseIntroduction(PurchaseFinished.NAME_EXTRA_FEATURES, "introduction dialog"))
        }
        return dialog
    }

    private fun startPurchase(feature: String) {
        val purchaseIntent = extraFeaturesService.createPurchaseIntent(context, feature)
        if (requestCode == 0) {
            startActivity(purchaseIntent)
        } else if (parentFragment != null) {
            parentFragment.startActivityForResult(purchaseIntent, requestCode)
        } else {
            activity.startActivityForResult(purchaseIntent, requestCode)
        }
    }

    companion object {
        const val EXTRA_FEATURE = "feature"
        fun show(fm: FragmentManager, feature: String, requestCode: Int = 0): ExtraFeaturesIntroductionDialogFragment {
            val df = ExtraFeaturesIntroductionDialogFragment()
            df.arguments = Bundle {
                this[EXTRA_FEATURE] = feature
                this[EXTRA_REQUEST_CODE] = requestCode
            }
            df.show(fm, "extra_features_introduction")
            return df
        }
    }
}
