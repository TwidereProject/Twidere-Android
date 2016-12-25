package org.mariotaku.twidere.util.premium

import android.content.Context
import com.anjlab.android.iab.v3.BillingProcessor
import org.mariotaku.twidere.Constants.GOOGLE_PLAY_LICENCING_PUBKEY

/**
 * Created by mariotaku on 2016/12/25.
 */

class GooglePlayExtraFeaturesChecker() : ExtraFeaturesChecker() {
    private lateinit var bp: BillingProcessor

    override fun init(context: Context) {
        super.init(context)
        bp = BillingProcessor(context, GOOGLE_PLAY_LICENCING_PUBKEY, null)
    }

    override fun release() {
        bp.release()
    }

    override fun isSupported(): Boolean = true

    override fun isEnabled(): Boolean {
        val details = bp.getPurchaseTransactionDetails("android.test.purchased") ?: return false
        return bp.isValidTransactionDetails(details)
    }

}
