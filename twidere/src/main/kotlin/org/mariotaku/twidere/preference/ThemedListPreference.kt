package org.mariotaku.twidere.preference

import android.content.Context
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import android.util.AttributeSet

import org.mariotaku.twidere.fragment.ThemedListPreferenceDialogFragmentCompat
import org.mariotaku.twidere.preference.iface.IDialogPreference

/**
 * Created by mariotaku on 16/3/15.
 */
open class ThemedListPreference(context: Context, attrs: AttributeSet? = null) :
        ListPreference(context, attrs), IDialogPreference {

    override fun displayDialog(fragment: PreferenceFragmentCompat) {
        val df = ThemedListPreferenceDialogFragmentCompat.newInstance(key)
        df.setTargetFragment(fragment, 0)
        fragment.parentFragmentManager.let { df.show(it, key) }
    }
}
