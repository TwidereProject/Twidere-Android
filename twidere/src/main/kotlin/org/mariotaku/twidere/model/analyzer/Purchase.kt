package org.mariotaku.twidere.model.analyzer

import android.app.Activity
import android.content.Intent
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.util.Analyzer

/**
 * Created by mariotaku on 2017/1/7.
 */

data class Purchase(val productName: String) : Analyzer.Event {
    override val name: String = "Purchase"
    override var accountType: String? = null
    var resultCode: Int = Activity.RESULT_OK
    var price: Double = Double.NaN
    var currency: String? = null

    override fun forEachValues(action: (String, String?) -> Unit) {
        if (resultCode != Activity.RESULT_OK) {
            action("Fail reason", getFailReason(resultCode))
        }
    }

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

        fun fromActivityResult(name: String, resultCode: Int, data: Intent?): Purchase {
            val result = Purchase(name)
            result.resultCode = resultCode
            if (data != null) {
                result.price = data.getDoubleExtra(EXTRA_PRICE, Double.NaN)
                result.currency = data.getStringExtra(EXTRA_CURRENCY)
            }
            return result
        }
    }
}
