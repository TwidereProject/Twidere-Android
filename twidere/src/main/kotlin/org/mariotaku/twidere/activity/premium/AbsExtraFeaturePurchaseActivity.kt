package org.mariotaku.twidere.activity.premium

import android.content.Context
import android.content.Intent
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.model.premium.PurchaseResult

/**
 * Created by mariotaku on 2017/1/8.
 */

abstract class AbsExtraFeaturePurchaseActivity : BaseActivity() {
    protected val requestingFeature: String? get() = intent.getStringExtra(EXTRA_REQUESTING_FEATURE)

    protected fun finishWithError(code: Int) {
        setResult(code)
        finish()
    }

    protected fun finishWithResult(result: PurchaseResult) {
        setResult(RESULT_OK, Intent().putExtra(EXTRA_PURCHASE_RESULT, result))
        finish()
    }

    companion object {

        const val RESULT_SERVICE_UNAVAILABLE = 1
        const val RESULT_INTERNAL_ERROR = 6
        const val RESULT_NOT_PURCHASED = 8

        const val EXTRA_PURCHASE_RESULT = "purchase_result"
        const val EXTRA_REQUESTING_FEATURE = "requesting_feature"

        const val ACTION_RESTORE_PURCHASE = "${IntentConstants.INTENT_PACKAGE_PREFIX}RESTORE_PURCHASE"

        @JvmStatic
        fun <T : AbsExtraFeaturePurchaseActivity> purchaseIntent(context: Context, cls: Class<T>, feature: String): Intent {
            val intent = Intent(context, cls)
            intent.putExtra(EXTRA_REQUESTING_FEATURE, feature)
            return intent
        }

        @JvmStatic
        fun <T : AbsExtraFeaturePurchaseActivity> restorePurchaseIntent(context: Context, cls: Class<T>, feature: String): Intent {
            val intent = Intent(context, cls)
            intent.action = ACTION_RESTORE_PURCHASE
            intent.putExtra(EXTRA_REQUESTING_FEATURE, feature)
            return intent
        }
    }
}
