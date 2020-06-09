package org.mariotaku.twidere.fragment

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import android.view.View
import kotlinx.android.synthetic.main.dialog_extra_features_introduction.*
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_REQUEST_CODE
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.model.analyzer.PurchaseConfirm
import org.mariotaku.twidere.model.analyzer.PurchaseFinished
import org.mariotaku.twidere.model.analyzer.PurchaseIntroduction
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.premium.ExtraFeaturesService

/**
 * Show extra features introduction
 * Created by mariotaku on 2016/12/25.
 */
class ExtraFeaturesIntroductionDialogFragment : BaseDialogFragment() {

    val feature: String get() = arguments?.getString(EXTRA_FEATURE)!!
    val source: String? get() = arguments?.getString(EXTRA_SOURCE)
    val requestCode: Int get() = arguments?.getInt(EXTRA_REQUEST_CODE, 0) ?: 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.title_extra_features)
        builder.setView(R.layout.dialog_extra_features_introduction)
        builder.setPositiveButton(R.string.action_purchase) { _, _ ->
            startPurchase(feature)
            Analyzer.log(PurchaseConfirm(PurchaseFinished.NAME_EXTRA_FEATURES))
        }
        builder.setNegativeButton(R.string.action_later) { _, _ ->
            onDialogCancelled()
        }
        val restorePurchaseIntent = extraFeaturesService.createRestorePurchaseIntent(requireContext(), feature)
        if (restorePurchaseIntent != null) {
            builder.setNeutralButton(R.string.action_restore_purchase) { _, _ ->
                startActivityForResultOnTarget(restorePurchaseIntent)
            }
        }
        val dialog = builder.create()
        dialog.onShow {
            it.applyTheme()
            it.restorePurchaseHint.visibility = if (restorePurchaseIntent != null) {
                View.VISIBLE
            } else {
                View.GONE
            }
            val description = ExtraFeaturesService.getIntroduction(requireContext(), feature)
            val featureIcon = it.featureIcon
            val featureDescription = it.featureDescription
            featureIcon.setImageResource(description.icon)
            featureDescription.text = description.description
            it.buyFeaturesPack.setOnClickListener {
                startPurchase(ExtraFeaturesService.FEATURE_FEATURES_PACK)
                dismiss()
            }
        }
        if (savedInstanceState == null) {
            Analyzer.log(PurchaseIntroduction(feature, source))
        }
        return dialog
    }

    override fun onCancel(dialog: DialogInterface) {
        onDialogCancelled()
    }

    private fun onDialogCancelled() {
        if (targetRequestCode != 0) {
            targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, null)
        }
    }

    private fun startPurchase(feature: String) {
        val currentContext = context ?: return
        val purchaseIntent = extraFeaturesService.createPurchaseIntent(currentContext, feature) ?: return
        startActivityForResultOnTarget(purchaseIntent)
    }

    private fun startActivityForResultOnTarget(intent: Intent) {
        when {
            targetFragment != null -> {
                targetFragment?.startActivityForResult(intent, targetRequestCode)
            }
            requestCode == 0 -> {
                startActivity(intent)
            }
            parentFragment != null -> {
                parentFragment?.startActivityForResult(intent, requestCode)
            }
            else -> {
                activity?.startActivityForResult(intent, requestCode)
            }
        }
    }

    companion object {
        const val EXTRA_FEATURE = "feature"
        const val EXTRA_SOURCE = "source"

        const val FRAGMENT_TAG = "extra_features_introduction"

        fun create(feature: String, source: String? = null, requestCode: Int = 0):
                ExtraFeaturesIntroductionDialogFragment {
            val df = ExtraFeaturesIntroductionDialogFragment()
            df.arguments = Bundle {
                this[EXTRA_FEATURE] = feature
                this[EXTRA_SOURCE] = source
                this[EXTRA_REQUEST_CODE] = requestCode
            }
            return df
        }

        fun show(fm: FragmentManager, feature: String, source: String? = null, requestCode: Int = 0):
                ExtraFeaturesIntroductionDialogFragment {
            val df = create(feature, source, requestCode)
            df.show(fm, FRAGMENT_TAG)
            return df
        }
    }
}
