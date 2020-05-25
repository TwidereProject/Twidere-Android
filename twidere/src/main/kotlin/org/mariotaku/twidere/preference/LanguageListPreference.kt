package org.mariotaku.twidere.preference

import android.content.Context
import android.util.AttributeSet
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import java.util.*

class LanguageListPreference(context: Context, attrs: AttributeSet? = null) : EntrySummaryListPreference(context, attrs) {
    init {
        val locales = BuildConfig.TRANSLATION_ARRAY.map { Locale(it.split('-')[0], it.split('-')[1]) }.let {
            it + Locale.US
        }.sortedBy {
            it.getDisplayName(it)
        }
        entries = arrayListOf(context.resources.getString(R.string.system_default)).apply {
            addAll(locales.map { it.getDisplayName(it) })
        }.toTypedArray()
        entryValues = arrayListOf("").apply {
            addAll(locales.map { it.toString() })
        }.toTypedArray()
    }
}