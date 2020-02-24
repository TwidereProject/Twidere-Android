package org.mariotaku.twidere.preference

import android.content.Context
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.widget.TextView
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.twidere.util.ThemeUtils

/**
 * Created by mariotaku on 2017/2/5.
 */

open class TintedPreferenceCategory(context: Context, attrs: AttributeSet? = null) : PreferenceCategory(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val theme = Chameleon.getOverrideTheme(context, ChameleonUtils.getActivity(context))
        val textView = holder.findViewById(android.R.id.title) as? TextView
        textView?.setTextColor(ThemeUtils.getOptimalAccentColor(theme.colorAccent,
                theme.colorForeground))
    }

}
