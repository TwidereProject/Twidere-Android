package org.mariotaku.twidere.model.analyzer

import android.app.Activity
import android.content.Intent
import org.mariotaku.twidere.constant.*
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

        internal fun getFailReason(resultCode: Int): String {
            return when (resultCode) {
                Activity.RESULT_CANCELED -> "cancelled"
                RESULT_SERVICE_UNAVAILABLE -> "service unavailable"
                RESULT_INTERNAL_ERROR -> "internal error"
                RESULT_NOT_PURCHASED -> "not purchased"
                else -> "unknown"
            }
        }

        fun create(name: String, data: Intent?): PurchaseFinished {
            val result = PurchaseFinished(name)
            if (data != null) {
                result.price = data.getDoubleExtra(EXTRA_PRICE, Double.NaN)
                result.currency = data.getStringExtra(EXTRA_CURRENCY)
            }
            return result
        }
    }
}
