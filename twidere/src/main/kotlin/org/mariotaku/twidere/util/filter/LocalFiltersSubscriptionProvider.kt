package org.mariotaku.twidere.util.filter

import android.content.Context

/**
 * Created by mariotaku on 2017/1/9.
 */

abstract class LocalFiltersSubscriptionProvider(val context: Context) : FiltersSubscriptionProvider {
    companion object {
        fun forName(context: Context, name: String): FiltersSubscriptionProvider? {
            when (name) {
                "url" -> {
                    return UrlFiltersSubscriptionProvider(context)
                }
            }
            return null
        }
    }
}
