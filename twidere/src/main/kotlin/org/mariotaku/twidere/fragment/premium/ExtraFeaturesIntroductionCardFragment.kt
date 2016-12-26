package org.mariotaku.twidere.fragment.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_extra_features_introduction.*
import kotlinx.android.synthetic.main.layout_extra_features_introduction.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.fragment.BaseSupportFragment
import org.mariotaku.twidere.util.premium.ExtraFeaturesChecker

/**
 * Created by mariotaku on 2016/12/25.
 */

class ExtraFeaturesIntroductionCardFragment : BaseSupportFragment() {

    lateinit var extraFeaturesChecker: ExtraFeaturesChecker

    // MARK: Fragment lifecycle
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        extraFeaturesChecker = ExtraFeaturesChecker.newInstance(context)
        purchaseButton.setOnClickListener {
            startActivity(extraFeaturesChecker.createPurchaseIntent(context))
        }
        val restorePurchaseIntent = extraFeaturesChecker.createRestorePurchaseIntent(context)
        if (restorePurchaseIntent != null) {
            restorePurchaseHint.visibility = View.VISIBLE
            restorePurchaseButton.visibility = View.VISIBLE
            restorePurchaseButton.setOnClickListener {
                startActivity(restorePurchaseIntent)
            }
        } else {
            restorePurchaseHint.visibility = View.GONE
            restorePurchaseButton.visibility = View.GONE
            restorePurchaseButton.setOnClickListener(null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_extra_features_introduction, container, false)
    }

}
