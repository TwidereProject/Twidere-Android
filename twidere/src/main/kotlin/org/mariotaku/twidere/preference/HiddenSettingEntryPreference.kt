package org.mariotaku.twidere.preference

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import android.util.AttributeSet
import org.mariotaku.twidere.constant.IntentConstants.INTENT_ACTION_HIDDEN_SETTINGS_ENTRY

/**
 * Created by mariotaku on 2016/12/12.
 */
class HiddenSettingEntryPreference(
        context: Context,
        attrs: AttributeSet? = null
) : TintedPreferenceCategory(context, attrs) {

    @SuppressLint("RestrictedApi")
    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?, id: Long) {
        super.onAttachedToHierarchy(preferenceManager, id)
        removeAll()
        val entryIntent = Intent(INTENT_ACTION_HIDDEN_SETTINGS_ENTRY)
        entryIntent.`package` = context.packageName
        context.packageManager.queryIntentActivities(entryIntent, 0).forEach { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo
            addPreference(Preference(context).apply {
                title = activityInfo.loadLabel(context.packageManager)
                intent = Intent(INTENT_ACTION_HIDDEN_SETTINGS_ENTRY).setClassName(context, activityInfo.name)
            })
        }
    }
}