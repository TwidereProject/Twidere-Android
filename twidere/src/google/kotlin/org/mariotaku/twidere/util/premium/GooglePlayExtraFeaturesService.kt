package org.mariotaku.twidere.util.premium

import android.content.Context
import android.content.Intent
import com.anjlab.android.iab.v3.BillingProcessor
import org.mariotaku.twidere.Constants.GOOGLE_PLAY_LICENCING_PUBKEY
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.GooglePlayInAppPurchaseActivity
import org.mariotaku.twidere.activity.premium.AbsExtraFeaturePurchaseActivity

/**
 * Created by mariotaku on 2016/12/25.
 */

class GooglePlayExtraFeaturesService() : ExtraFeaturesService() {
    private val PRODUCT_ID_EXTRA_FEATURES_PACK = "twidere.extra.features"

    private lateinit var bp: BillingProcessor

    override fun getDashboardLayouts() = intArrayOf(R.layout.card_item_extra_features_sync_status)

    override fun init(context: Context) {
        super.init(context)
        bp = BillingProcessor(context, GOOGLE_PLAY_LICENCING_PUBKEY, null)
    }

    override fun release() {
        bp.release()
    }

    override fun isSupported(): Boolean = true

    override fun isEnabled(feature: String): Boolean {
        if (bp.hasValidTransaction(PRODUCT_ID_EXTRA_FEATURES_PACK)) return true
        val productId = getProductId(feature)
        return bp.hasValidTransaction(productId)
    }

    override fun destroyPurchase(): Boolean {
        return bp.consumePurchase(PRODUCT_ID_EXTRA_FEATURES_PACK)
    }

    override fun createPurchaseIntent(context: Context, feature: String): Intent? {
        return AbsExtraFeaturePurchaseActivity.purchaseIntent(context,
                GooglePlayInAppPurchaseActivity::class.java, feature)
    }

    override fun createRestorePurchaseIntent(context: Context, feature: String): Intent? {
        return AbsExtraFeaturePurchaseActivity.restorePurchaseIntent(context,
                GooglePlayInAppPurchaseActivity::class.java, feature)
    }

    private fun BillingProcessor.hasValidTransaction(productId: String): Boolean {
        val details = getPurchaseTransactionDetails(productId) ?: return false
        return isValidTransactionDetails(details)
    }

    companion object {
        @JvmStatic
        fun getProductId(feature: String): String {
            return when (feature) {
                FEATURE_FEATURES_PACK -> "twidere.extra.features"
                FEATURE_SYNC_DATA -> "twidere.extra.feature.data_sync"
                FEATURE_FILTERS_IMPORT -> "twidere.extra.feature.filter_import"
                FEATURE_FILTERS_SUBSCRIPTION -> "twidere.extra.feature.filter_subscription"
                FEATURE_SCHEDULE_STATUS -> "twidere.extra.feature.schedule_status"
                else -> throw UnsupportedOperationException(feature)
            }
        }
    }
}
