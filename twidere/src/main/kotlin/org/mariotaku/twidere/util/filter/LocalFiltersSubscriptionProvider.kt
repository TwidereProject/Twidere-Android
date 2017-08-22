package org.mariotaku.twidere.util.filter

import android.content.Context
import org.mariotaku.twidere.model.filter.UrlFiltersSubscriptionProviderArguments
import org.mariotaku.twidere.util.JsonSerializer
import java.io.IOException

/**
 * Created by mariotaku on 2017/1/9.
 */

abstract class LocalFiltersSubscriptionProvider(val context: Context) : FiltersSubscriptionProvider {
    companion object {
        fun forName(context: Context, name: String, arguments: String?): FiltersSubscriptionProvider? {
            when (name) {
                "url" -> {
                    if (arguments == null) return null
                    val argsObj = try {
                        JsonSerializer.parse(arguments, UrlFiltersSubscriptionProviderArguments::class.java)
                    } catch (e: IOException) {
                        return null
                    }
                    return UrlFiltersSubscriptionProvider(context, argsObj)
                }
            }
            return null
        }
    }
}
