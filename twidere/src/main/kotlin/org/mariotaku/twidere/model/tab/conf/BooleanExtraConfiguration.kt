package org.mariotaku.twidere.model.tab.conf

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView

import org.mariotaku.twidere.R
import org.mariotaku.twidere.fragment.CustomTabsFragment
import org.mariotaku.twidere.model.tab.BooleanHolder
import org.mariotaku.twidere.model.tab.TabConfiguration

/**
 * Created by mariotaku on 2016/12/5.
 */
open class BooleanExtraConfiguration(
        key: String,
        val defaultValue: BooleanHolder
) : TabConfiguration.ExtraConfiguration(key) {

    open var value: Boolean
        get() = checkBox.isChecked
        set(value) {
            checkBox.isChecked = value
        }

    private lateinit var checkBox: CheckBox

    constructor(key: String, def: Boolean) : this(key, BooleanHolder.constant(def))

    override fun onCreateView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_extra_config_checkbox, parent, false)
    }

    override fun onViewCreated(context: Context, view: View, fragment: CustomTabsFragment.TabEditorDialogFragment) {
        super.onViewCreated(context, view, fragment)
        val titleView = view.findViewById(android.R.id.title) as TextView
        val summaryView = view.findViewById(android.R.id.summary) as TextView
        titleView.text = title.createString(context)

        val summary = this.summary
        if (summary != null) {
            summaryView.visibility = View.VISIBLE
            summaryView.text = summary.createString(context)
        } else {
            summaryView.visibility = View.GONE
        }

        checkBox = view.findViewById(android.R.id.checkbox) as CheckBox
        checkBox.visibility = View.VISIBLE
        checkBox.isChecked = defaultValue.createBoolean(context)
        view.setOnClickListener { checkBox.toggle() }
    }
}
