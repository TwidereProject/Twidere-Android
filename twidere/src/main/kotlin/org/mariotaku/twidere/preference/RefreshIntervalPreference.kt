package org.mariotaku.twidere.preference

import android.app.job.JobInfo
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.preference.PreferenceManager
import android.util.AttributeSet
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.twidere.constant.autoRefreshCompatibilityModeKey
import java.util.concurrent.TimeUnit

/**
 * Created by mariotaku on 2017/2/8.
 */
class RefreshIntervalPreference(
        context: Context, attrs: AttributeSet? = null
) : EntrySummaryListPreference(context, attrs) {

    private val entriesBackup = entries
    private val valuesBackup = entryValues

    private val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == autoRefreshCompatibilityModeKey.key) {
            updateEntries()
        }
    }

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager) {
        super.onAttachedToHierarchy(preferenceManager)
        sharedPreferences.registerOnSharedPreferenceChangeListener(changeListener)
        updateEntries()
    }

    override fun onDetached() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(changeListener)
        super.onDetached()
    }

    private fun updateEntries() {
        var index: Int = -1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !sharedPreferences[autoRefreshCompatibilityModeKey]) {
            index = valuesBackup.indexOfFirst {
                val intervalMinutes = it.toString().toLongOr(-1L)
                if (intervalMinutes < 0) return@indexOfFirst false
                return@indexOfFirst TimeUnit.MINUTES.toMillis(intervalMinutes) >= JobInfo.getMinPeriodMillis()
            }

        }

        if (index >= 0) {
            entryValues = valuesBackup.sliceArray(index..valuesBackup.lastIndex)
            entries = entriesBackup.sliceArray(index..entriesBackup.lastIndex)
        } else {
            entryValues = valuesBackup
            entries = entriesBackup
        }
        val valueMinutes = value.toLongOr(-1)
        val minValue = entryValues.firstOrNull()?.toString().toLongOr(-1)
        if (valueMinutes in 1 until minValue) {
            value = minValue.toString()
        }
    }
}