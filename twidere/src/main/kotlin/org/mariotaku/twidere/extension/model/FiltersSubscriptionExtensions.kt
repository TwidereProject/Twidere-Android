package org.mariotaku.twidere.extension.model

import android.content.ComponentName
import android.content.Context
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.FiltersSubscription
import org.mariotaku.twidere.model.filter.UrlFiltersSubscriptionProviderArguments
import org.mariotaku.twidere.util.JsonSerializer
import org.mariotaku.twidere.util.filter.FiltersSubscriptionProvider
import org.mariotaku.twidere.util.filter.LocalFiltersSubscriptionProvider

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

fun FiltersSubscription.getComponentLabel(context: Context): CharSequence {
    val component = this.component ?: return context.getString(R.string.title_filters_subscription_invalid)
    if (component.startsWith(":")) {
        when (component.substringAfter(":")) {
            "url" -> return context.getString(R.string.title_filters_subscription_url)
        }
        return context.getString(R.string.title_filters_subscription_invalid)
    }
    val cn = ComponentName.unflattenFromString(component) ?:
            return context.getString(R.string.title_filters_subscription_invalid)
    val pm = context.packageManager
    return pm.getServiceInfo(cn, 0).loadLabel(pm)
}

fun FiltersSubscription.setupUrl(url: String) {
    this.component = ":url"
    this.arguments = JsonSerializer.serialize(UrlFiltersSubscriptionProviderArguments().apply {
        this.url = url
    })
}