package org.mariotaku.twidere.model.analyzer

import android.content.Intent
import org.mariotaku.twidere.activity.premium.AbsExtraFeaturePurchaseActivity
import org.mariotaku.twidere.util.Analyzer

/**
 * Created by mariotaku on 2017/1/7.
 */

data class PurchaseFinished(val productName: String) : Analyzer.Event {
    override val name: String = "Purchase Finished"
    override var accountType: String? = null
    var price: Double = Double.NaN
    var currency: String? = null

    companion object {
        const val NAME_EXTRA_FEATURES = "Enhanced Features"

        fun create(data: Intent): PurchaseFinished {
            val purchaseResult: AbsExtraFeaturePurchaseActivity.PurchaseResult
                    = data.getParcelableExtra(AbsExtraFeaturePurchaseActivity.EXTRA_PURCHASE_RESULT)
            val result = PurchaseFinished(purchaseResult.feature)
            result.price = purchaseResult.price
            result.currency = purchaseResult.currency
            return result
        }
    }
}
