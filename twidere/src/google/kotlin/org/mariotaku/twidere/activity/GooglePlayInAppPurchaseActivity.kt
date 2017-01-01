package org.mariotaku.twidere.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.constant.IntentConstants.INTENT_PACKAGE_PREFIX
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import java.lang.ref.WeakReference

/**
 * Created by mariotaku on 2016/12/25.
 */

class GooglePlayInAppPurchaseActivity : BaseActivity(), BillingProcessor.IBillingHandler {

    private lateinit var billingProcessor: BillingProcessor

    private val productId: String get() = intent.getStringExtra(EXTRA_PRODUCT_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingProcessor = BillingProcessor(this, Constants.GOOGLE_PLAY_LICENCING_PUBKEY, this)
        if (!isFinishing && !BillingProcessor.isIabServiceAvailable(this)) {
            handleError()
        }
    }

    override fun onDestroy() {
        billingProcessor.release()
        super.onDestroy()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // MARK: Payment methods
    override fun onBillingError(code: Int, error: Throwable?) {
        handleError()
    }

    override fun onBillingInitialized() {
        if (intent.action == ACTION_RESTORE_PURCHASE) {
            performRestorePurchase()
        } else {
            billingProcessor.purchase(this, productId)
        }
    }

    override fun onProductPurchased(productId: String?, details: TransactionDetails?) {
        getProductDetailsAndFinish()
    }

    override fun onPurchaseHistoryRestored() {
        getProductDetailsAndFinish()
    }

    private fun handleError() {
        setResult(RESULT_CANCELED)
        finish()
    }

    private fun handlePurchased(details: TransactionDetails) {
        setResult(RESULT_OK)
        finish()
    }

    private fun performRestorePurchase() {
        val weakThis = WeakReference(this)
        val dfRef = WeakReference(ProgressDialogFragment.show(supportFragmentManager, "consume_purchase_progress"))
        task {
            val bp = weakThis.get()?.billingProcessor ?: throw IllegalStateException()
            bp.loadOwnedPurchasesFromGoogle()
            val details = bp.getPurchaseTransactionDetails(productId)
            return@task details ?: throw IllegalStateException()
        }.successUi { details ->
            weakThis.get()?.handlePurchased(details)
        }.failUi { error ->
            weakThis.get()?.handleError()
        }.alwaysUi {
            weakThis.get()?.executeAfterFragmentResumed {
                val fm = weakThis.get()?.supportFragmentManager
                val df = dfRef.get() ?: (fm?.findFragmentByTag("consume_purchase_progress") as? DialogFragment)
                df?.dismiss()
            }
        }
    }

    private fun getProductDetailsAndFinish() {
        val weakThis = WeakReference(this)
        val dfRef = WeakReference(ProgressDialogFragment.show(supportFragmentManager, "consume_purchase_progress"))
        task {
            val bp = weakThis.get()?.billingProcessor ?: throw IllegalStateException()
            bp.loadOwnedPurchasesFromGoogle()
            val result = bp.getPurchaseTransactionDetails(productId)
            return@task result ?: throw IllegalStateException()
        }.successUi { details ->
            weakThis.get()?.handlePurchased(details)
        }.failUi { error ->
            weakThis.get()?.handleError()
        }.alwaysUi {
            weakThis.get()?.executeAfterFragmentResumed {
                val fm = weakThis.get()?.supportFragmentManager
                val df = dfRef.get() ?: (fm?.findFragmentByTag("consume_purchase_progress") as? DialogFragment)
                df?.dismiss()
            }
        }
    }

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"
        const val ACTION_RESTORE_PURCHASE = "${INTENT_PACKAGE_PREFIX}RESTORE_PURCHASE"
    }
}
