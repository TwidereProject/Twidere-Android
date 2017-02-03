package org.mariotaku.twidere.view.controller.premium

import android.view.View
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.REQUEST_PURCHASE_EXTRA_FEATURES
import org.mariotaku.twidere.activity.PremiumDashboardActivity
import org.mariotaku.twidere.fragment.ExtraFeaturesIntroductionDialogFragment

/**
 * Created by mariotaku on 2017/2/4.
 */

abstract class AbsGoogleInAppItemViewController : PremiumDashboardActivity.ExtraFeatureViewController() {
    abstract val title: String
    abstract val summary: String
    abstract val feature: String
    abstract val availableLabel: String
    override fun onCreate() {
        super.onCreate()
        titleView.text = title
        messageView.text = summary

        button1.setText(R.string.action_purchase)
        button2.text = availableLabel

        button1.setOnClickListener {
            ExtraFeaturesIntroductionDialogFragment.show(activity.supportFragmentManager,
                    feature = this.feature, requestCode = REQUEST_PURCHASE_EXTRA_FEATURES)
        }
        button2.setOnClickListener {
            onAvailableButtonClick()
        }

        updateEnabledState()
    }

    override fun onResume() {
        super.onResume()
        updateEnabledState()
    }

    abstract fun onAvailableButtonClick()

    private fun updateEnabledState() {
        if (extraFeaturesService.isEnabled(feature)) {
            button1.visibility = View.GONE
            button2.visibility = View.VISIBLE
        } else {
            button1.visibility = View.VISIBLE
            button2.visibility = View.GONE
        }
    }
}
