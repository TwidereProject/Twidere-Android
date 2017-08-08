package org.mariotaku.twidere.util.premium

import android.content.Context
import android.content.Intent
import android.support.annotation.CallSuper
import org.mariotaku.twidere.R
import org.mariotaku.twidere.view.ContainerView
import java.util.*

/**
 * Created by mariotaku on 2016/12/25.
 */

abstract class ExtraFeaturesService {
    protected lateinit var context: Context

    abstract fun getDashboardControllers(): List<Class<out ContainerView.ViewController>>

    @CallSuper
    protected open fun init(context: Context) {
        this.context = context
    }

    open fun appStarted() {

    }

    open fun release() {
    }

    /**
     * @param feature Checking feature, `null` for checking service itself supported
     */
    abstract fun isSupported(feature: String? = null): Boolean

    abstract fun isEnabled(feature: String): Boolean

    /**
     * For debug purpose only, this will remove purchased product
     */
    abstract fun destroyPurchase(): Boolean

    abstract fun createPurchaseIntent(context: Context, feature: String): Intent?

    abstract fun createRestorePurchaseIntent(context: Context, feature: String): Intent?

    data class Introduction(val icon: Int, val description: String)

    companion object {
        const val FEATURE_FEATURES_PACK = "features_pack"
        const val FEATURE_FILTERS_IMPORT = "import_filters"
        const val FEATURE_FILTERS_SUBSCRIPTION = "filters_subscriptions"
        const val FEATURE_SYNC_DATA = "sync_data"
        const val FEATURE_SCHEDULE_STATUS = "schedule_status"
        const val FEATURE_SHARE_GIF = "share_gif"

        fun newInstance(context: Context): ExtraFeaturesService {
            val instance = ServiceLoader.load(ExtraFeaturesService::class.java).firstOrNull() ?: run {
                return@run DummyExtraFeaturesService()
            }
            instance.init(context)
            return instance
        }

        fun getIntroduction(context: Context, feature: String): Introduction {
            return when (feature) {
                FEATURE_FEATURES_PACK -> Introduction(R.drawable.ic_action_infinity, "")
                FEATURE_FILTERS_IMPORT -> Introduction(R.drawable.ic_action_speaker_muted,
                        context.getString(R.string.extra_feature_description_filters_import))
                FEATURE_SYNC_DATA -> Introduction(R.drawable.ic_action_refresh,
                        context.getString(R.string.extra_feature_description_sync_data))
                FEATURE_FILTERS_SUBSCRIPTION -> Introduction(R.drawable.ic_action_speaker_muted,
                        context.getString(R.string.extra_feature_description_filters_subscription))
                FEATURE_SCHEDULE_STATUS -> Introduction(R.drawable.ic_action_time,
                        context.getString(R.string.extra_feature_description_schedule_status))
                FEATURE_SHARE_GIF -> Introduction(R.drawable.ic_action_gif,
                        context.getString(R.string.extra_feature_description_share_gif))
                else -> throw UnsupportedOperationException(feature)
            }
        }

    }
}
