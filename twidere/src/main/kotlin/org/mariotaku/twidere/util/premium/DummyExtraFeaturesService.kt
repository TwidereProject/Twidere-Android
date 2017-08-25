package org.mariotaku.twidere.util.premium

import android.content.Context
import android.content.Intent
import org.mariotaku.twidere.view.ContainerView

/**
 * Created by mariotaku on 2016/12/25.
 */

class DummyExtraFeaturesService : ExtraFeaturesService() {
    override fun getDashboardControllers() = emptyList<Class<ContainerView.ViewController>>()

    override fun isSupported(feature: String?): Boolean = false

    override fun isEnabled(feature: String): Boolean = false

    override fun isPurchased(feature: String): Boolean = false

    override fun destroyPurchase(): Boolean = false

    override fun createPurchaseIntent(context: Context, feature: String): Intent? = null

    override fun createRestorePurchaseIntent(context: Context, feature: String): Intent? = null

}
