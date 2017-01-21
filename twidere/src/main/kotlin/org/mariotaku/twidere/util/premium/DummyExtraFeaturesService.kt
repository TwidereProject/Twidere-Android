package org.mariotaku.twidere.util.premium

import android.content.Context
import android.content.Intent

/**
 * Created by mariotaku on 2016/12/25.
 */

class DummyExtraFeaturesService : ExtraFeaturesService() {

    override fun getDashboardLayouts() = intArrayOf()

    override fun isSupported(): Boolean = false

    override fun isEnabled(feature: String): Boolean = false

    override fun destroyPurchase(): Boolean = false

    override fun createPurchaseIntent(context: Context, feature: String): Intent? = null

    override fun createRestorePurchaseIntent(context: Context, feature: String): Intent? = null

}
