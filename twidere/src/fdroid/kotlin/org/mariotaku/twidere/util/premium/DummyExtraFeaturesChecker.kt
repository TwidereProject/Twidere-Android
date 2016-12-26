package org.mariotaku.twidere.util.premium

import android.content.Context
import android.content.Intent

/**
 * Created by mariotaku on 2016/12/25.
 */

class DummyExtraFeaturesChecker() : ExtraFeaturesChecker() {
    override fun isSupported(): Boolean = false

    override fun isEnabled(): Boolean = false

    override fun createPurchaseIntent(context: Context): Intent = throw UnsupportedOperationException()

    override fun createRestorePurchaseIntent(context: Context): Intent? = null

}
