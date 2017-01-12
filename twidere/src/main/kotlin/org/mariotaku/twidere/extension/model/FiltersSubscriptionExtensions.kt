package org.mariotaku.twidere.extension.model

import android.content.Context
import org.mariotaku.twidere.model.FiltersSubscription
import org.mariotaku.twidere.util.JsonSerializer
import org.mariotaku.twidere.util.filter.FiltersSubscriptionProvider
import org.mariotaku.twidere.util.filter.LocalFiltersSubscriptionProvider
import org.mariotaku.twidere.util.filter.UrlFiltersSubscriptionProvider

/**
 * Created by mariotaku on 2017/1/9.
 */

fun FiltersSubscription.instantiateComponent(context: Context): FiltersSubscriptionProvider? {
    val component = this.component ?: return null
    if (component.startsWith(":")) {
        // Load builtin service
        return LocalFiltersSubscriptionProvider.forName(context, component.substringAfter(":"), arguments)
    }
    return null
}


fun FiltersSubscription.setupUrl(url: String) {
    this.component = ":url"
    this.arguments = JsonSerializer.serialize(UrlFiltersSubscriptionProvider.Arguments().apply {
        this.url = url
    })
}