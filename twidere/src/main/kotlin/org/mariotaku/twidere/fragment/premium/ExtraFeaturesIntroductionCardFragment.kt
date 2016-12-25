package org.mariotaku.twidere.fragment.premium

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_extra_features_introduction.*
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.INTENT_ACTION_IN_APP_PURCHASE
import org.mariotaku.twidere.fragment.BaseSupportFragment

/**
 * Created by mariotaku on 2016/12/25.
 */

class ExtraFeaturesIntroductionCardFragment : BaseSupportFragment() {

    // MARK: Fragment lifecycle
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        purchaseButton.setOnClickListener {
            startActivity(Intent(INTENT_ACTION_IN_APP_PURCHASE).setPackage(BuildConfig.APPLICATION_ID))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_extra_features_introduction, container, false)
    }

}
