package org.mariotaku.twidere.view.controller.premium

import android.widget.Toast
import org.mariotaku.twidere.R
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.premium.ExtraFeaturesService

/**
 * Created by mariotaku on 2017/2/4.
 */

class GoogleFiltersImportViewController : AbsGoogleInAppItemViewController() {
    override val feature: String
        get() = ExtraFeaturesService.FEATURE_FILTERS_IMPORT
    override val summary: String
        get() = context.getString(R.string.extra_feature_description_filters_import)
    override val title: String
        get() = context.getString(R.string.extra_feature_title_filters_import)
    override val availableLabel: String
        get() = context.getString(R.string.action_import)

    override fun onAvailableButtonClick() {
        IntentUtils.openFilters(context, "users")
        Toast.makeText(context, R.string.message_toast_filters_import_hint, Toast.LENGTH_SHORT).show()
    }
}
