package org.mariotaku.twidere.util.filter

import android.content.Context
import org.mariotaku.twidere.util.JsonSerializer

/**
 * Created by mariotaku on 2017/1/9.
 */

abstract class LocalFiltersSubscriptionProvider(val context: Context) : FiltersSubscriptionProvider {
    companion object {
        fun forName(context: Context, name: String, arguments: String?): FiltersSubscriptionProvider? {
            when (name) {
                "url" -> {
                    if (arguments == null) return null
                    val argsObj = JsonSerializer.parse(arguments,
                            UrlFiltersSubscriptionProvider.Arguments::class.java) ?: return null
                    return UrlFiltersSubscriptionProvider(context, argsObj)
                }
            }
            return null
        }
    }
}
