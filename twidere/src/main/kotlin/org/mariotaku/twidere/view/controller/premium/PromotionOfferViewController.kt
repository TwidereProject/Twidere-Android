/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.view.controller.premium

import android.content.Intent
import android.view.View
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.PremiumDashboardActivity
import org.mariotaku.twidere.constant.promotionsEnabledKey
import org.mariotaku.twidere.util.premium.ExtraFeaturesService

class PromotionOfferViewController : PremiumDashboardActivity.ExtraFeatureViewController() {

    override fun onCreate() {
        super.onCreate()
        titleView.setText(R.string.title_promotions_reward)
        messageView.text = context.getString(R.string.message_promotions_reward)
        button1.setText(R.string.action_purchase_features_pack)
        if (preferences[promotionsEnabledKey]) {
            button2.setText(R.string.action_disable)
        } else {
            button2.setText(R.string.action_enable_promotions)
        }

        button1.visibility = View.VISIBLE
        button2.visibility = View.VISIBLE

        button1.setOnClickListener {
            val purchaseIntent = extraFeaturesService.createPurchaseIntent(context,
                    ExtraFeaturesService.FEATURE_FEATURES_PACK) ?: return@setOnClickListener
            activity.startActivityForControllerResult(purchaseIntent, position,
                    REQUEST_PURCHASE_FEATURES_PACK)
        }
        button2.setOnClickListener {
            togglePromotions()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PURCHASE_FEATURES_PACK -> {
                activity.recreate()
            }
        }
    }

    override fun onViewCreated(view: View) {
        if (preferences[promotionsEnabledKey] || !extraFeaturesService.isPurchased(ExtraFeaturesService.FEATURE_FEATURES_PACK)) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    private fun togglePromotions() {
        preferences[promotionsEnabledKey] = !preferences[promotionsEnabledKey]
        ChameleonUtils.getActivity(context)?.recreate()
    }

    companion object {
        private const val REQUEST_PURCHASE_FEATURES_PACK = 101
    }
}