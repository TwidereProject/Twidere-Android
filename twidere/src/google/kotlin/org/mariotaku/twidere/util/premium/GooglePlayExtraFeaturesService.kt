package org.mariotaku.twidere.util.premium

import android.content.Context
import android.content.Intent
import com.anjlab.android.iab.v3.BillingProcessor
import nl.komponents.kovenant.task
import org.mariotaku.twidere.Constants.GOOGLE_PLAY_LICENCING_PUBKEY
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.GooglePlayInAppPurchaseActivity
import org.mariotaku.twidere.activity.premium.AbsExtraFeaturePurchaseActivity

/**
 * Created by mariotaku on 2016/12/25.
 */

class GooglePlayExtraFeaturesService : ExtraFeaturesService() {

    private lateinit var bp: BillingProcessor

    override fun getDashboardLayouts() = intArrayOf(R.layout.card_item_extra_features_sync_status)

    override fun init(context: Context) {
        super.init(context)
        bp = BillingProcessor(context, GOOGLE_PLAY_LICENCING_PUBKEY, null)
    }

    override fun appStarted() {
        task {
            bp.loadOwnedPurchasesFromGoogle()
        }
    }

    override fun release() {
        bp.release()
    }

    override fun isSupported(): Boolean = BillingProcessor.isIabServiceAvailable(context)

    override fun isEnabled(feature: String): Boolean {
        if (bp.hasValidTransaction(PRODUCT_ID_EXTRA_FEATURES_PACK)) return true
        val productId = getProductId(feature)
        return bp.hasValidTransaction(productId)
    }

    override fun destroyPurchase(): Boolean {
        bp.consumePurchase(PRODUCT_ID_EXTRA_FEATURES_PACK)
        bp.consumePurchase(PRODUCT_ID_DATA_SYNC)
        bp.consumePurchase(PRODUCT_ID_FILTERS_IMPORT)
        bp.consumePurchase(PRODUCT_ID_FILTERS_SUBSCRIPTION)
        bp.consumePurchase(PRODUCT_ID_SCHEDULE_STATUS)
        return true
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
        private const val PRODUCT_ID_EXTRA_FEATURES_PACK = "twidere.extra.features"
        private const val PRODUCT_ID_DATA_SYNC = "twidere.extra.feature.data_sync"
        private const val PRODUCT_ID_FILTERS_IMPORT = "twidere.extra.feature.filter_import"
        private const val PRODUCT_ID_FILTERS_SUBSCRIPTION = "twidere.extra.feature.filter_subscription"
        private const val PRODUCT_ID_SCHEDULE_STATUS = "twidere.extra.feature.schedule_status"

        @JvmStatic
        fun getProductId(feature: String): String {
            return when (feature) {
                FEATURE_FEATURES_PACK -> PRODUCT_ID_EXTRA_FEATURES_PACK
                FEATURE_SYNC_DATA -> PRODUCT_ID_DATA_SYNC
                FEATURE_FILTERS_IMPORT -> PRODUCT_ID_FILTERS_IMPORT
                FEATURE_FILTERS_SUBSCRIPTION -> PRODUCT_ID_FILTERS_SUBSCRIPTION
                FEATURE_SCHEDULE_STATUS -> PRODUCT_ID_SCHEDULE_STATUS
                else -> throw UnsupportedOperationException(feature)
            }
        }
    }
}
