package org.mariotaku.twidere.preference.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.SwitchPreferenceCompat
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.sync.SyncPreferences
import javax.inject.Inject

/**
 * Created by mariotaku on 2017/1/6.
 */

class SyncItemPreference(
        context: Context,
        attrs: AttributeSet
) : SwitchPreferenceCompat(context, attrs) {
    @Inject
    lateinit var syncPreferences: SyncPreferences
    @Inject
    lateinit var preferences: SharedPreferences

    val syncType: String

    init {
        GeneralComponent.get(context).inject(this)
        val a = context.obtainStyledAttributes(attrs, R.styleable.SyncItemPreference)
        syncType = a.getString(R.styleable.SyncItemPreference_syncType)!!
        key = SyncPreferences.getSyncEnabledKey(syncType)
        a.recycle()
    }

    override fun syncSummaryView(view: View?) {
        if (view !is TextView) return
        if (summary != null || summaryOn != null || summaryOff != null) {
            return super.syncSummaryView(view)
        }
        view.visibility = View.VISIBLE
        val lastSynced = syncPreferences.getLastSynced(syncType)
        if (lastSynced > 0) {
            view.text = context.getString(R.string.message_sync_last_synced_time,
                    Utils.formatToLongTimeString(context, lastSynced))
        } else {
            view.text = null
        }
    }

}
