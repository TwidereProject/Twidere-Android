package org.mariotaku.twidere.model.analyzer

import org.mariotaku.twidere.util.Analyzer

/**
 * Created by mariotaku on 2017/1/7.
 */

data class PurchaseIntroduction(val productName: String, val source: String?) : Analyzer.Event {
    override val name: String = "Purchase Introduction"
    override val accountType: String? = null
    override fun forEachValues(action: (String, String?) -> Unit) {
        action("Product Name", productName)
        if (source != null) {
            action("Source", source)
        }
    }
}
