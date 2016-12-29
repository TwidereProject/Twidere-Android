package org.mariotaku.twidere.util.premium

import android.content.Context
import android.content.Intent
import org.mariotaku.twidere.R

/**
 * Created by mariotaku on 2016/12/25.
 */

class DummyExtraFeaturesChecker : ExtraFeaturesChecker() {

    override val introductionLayout: Int = R.layout.card_item_extra_features_purchase_introduction
    override val statusLayout: Int = throw UnsupportedOperationException()

    override fun isSupported(): Boolean = false

    override fun isEnabled(): Boolean = false

    override fun createPurchaseIntent(context: Context): Intent = throw UnsupportedOperationException()

    override fun createRestorePurchaseIntent(context: Context): Intent? = null

}
