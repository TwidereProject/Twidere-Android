package org.mariotaku.twidere.view.controller.premium

import org.mariotaku.twidere.R
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.premium.ExtraFeaturesService

/**
 * Created by mariotaku on 2017/2/4.
 */

class GoogleFiltersSubscriptionsViewController : AbsGoogleInAppItemViewController() {
    override val feature: String
        get() = ExtraFeaturesService.FEATURE_FILTERS_SUBSCRIPTION
    override val summary: String
        get() = context.getString(R.string.extra_feature_description_filters_subscription)
    override val title: String
        get() = context.getString(R.string.extra_feature_title_filters_subscription)
    override val availableLabel: String
        get() = context.getString(R.string.action_filter_subscriptions_card_manage)

    override fun onAvailableButtonClick() {
        IntentUtils.openFilters(context, "settings")
    }
}
