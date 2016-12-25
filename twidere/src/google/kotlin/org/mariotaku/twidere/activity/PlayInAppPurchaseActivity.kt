package org.mariotaku.twidere.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.TwidereConstants.EXTRA_DATA
import org.mariotaku.twidere.model.premium.GooglePurchaseResult

/**
 * Created by mariotaku on 2016/12/25.
 */

class PlayInAppPurchaseActivity : Activity(), BillingProcessor.IBillingHandler {

    lateinit var billingProcessor: BillingProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingProcessor = BillingProcessor(this, Constants.GOOGLE_PLAY_LICENCING_PUBKEY, this)
        if (!isFinishing && BillingProcessor.isIabServiceAvailable(this)) {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    override fun onDestroy() {
        billingProcessor.release()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // MARK: Payment methods
    override fun onBillingError(code: Int, error: Throwable?) {
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onBillingInitialized() {
        billingProcessor.purchase(this, "android.test.purchased")
    }

    override fun onProductPurchased(productId: String?, details: TransactionDetails?) {
        billingProcessor.getPurchaseTransactionDetails("android.test.purchased")
        val data = Intent()
        val purchaseResult = GooglePurchaseResult()
        details?.purchaseInfo?.purchaseData?.let { purchaseData ->

        }
        data.putExtra(EXTRA_DATA, purchaseResult)
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onPurchaseHistoryRestored() {
    }

}
