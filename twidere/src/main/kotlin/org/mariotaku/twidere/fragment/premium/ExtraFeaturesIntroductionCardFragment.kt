package org.mariotaku.twidere.fragment.premium

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_extra_features_introduction.*
import kotlinx.android.synthetic.main.layout_extra_features_introduction.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.PURCHASE_RESPONSE_NOT_PURCHASED
import org.mariotaku.twidere.fragment.BaseSupportFragment
import org.mariotaku.twidere.util.premium.ExtraFeaturesService

/**
 * Created by mariotaku on 2016/12/25.
 */

class ExtraFeaturesIntroductionCardFragment : BaseSupportFragment() {

    lateinit var extraFeaturesService: ExtraFeaturesService

    private val REQUEST_PURCHASE: Int = 301
    private val REQUEST_RESTORE_PURCHASE: Int = 302

    // MARK: Fragment lifecycle
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        extraFeaturesService = ExtraFeaturesService.newInstance(context)
        purchaseButton.setOnClickListener {
            startActivityForResult(extraFeaturesService.createPurchaseIntent(context), REQUEST_PURCHASE)
        }
        val restorePurchaseIntent = extraFeaturesService.createRestorePurchaseIntent(context)
        if (restorePurchaseIntent != null) {
            restorePurchaseHint.visibility = View.VISIBLE
            restorePurchaseButton.visibility = View.VISIBLE
            restorePurchaseButton.setOnClickListener {
                startActivityForResult(restorePurchaseIntent, REQUEST_RESTORE_PURCHASE)
            }
        } else {
            restorePurchaseHint.visibility = View.GONE
            restorePurchaseButton.visibility = View.GONE
            restorePurchaseButton.setOnClickListener(null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PURCHASE -> {
                activity?.recreate()
            }
            REQUEST_RESTORE_PURCHASE -> {
                if (requestCode == Activity.RESULT_OK) {
                    activity?.recreate()
                } else if (resultCode == PURCHASE_RESPONSE_NOT_PURCHASED) {
                    Toast.makeText(context, R.string.message_extra_features_not_purchased, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_extra_features_introduction, container, false)
    }

}
