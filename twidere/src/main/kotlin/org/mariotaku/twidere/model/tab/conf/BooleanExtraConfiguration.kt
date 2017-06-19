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
import org.mariotaku.twidere.model.tab.StringHolder
import org.mariotaku.twidere.model.tab.TabConfiguration

/**
 * Created by mariotaku on 2016/12/5.
 */
open class BooleanExtraConfiguration(
        key: String,
        title: StringHolder,
        val defaultValue: BooleanHolder
) : TabConfiguration.ExtraConfiguration(key, title) {

    open var value: Boolean
        get() = checkBox.isChecked
        set(value) {
            checkBox.isChecked = value
        }

    private lateinit var checkBox: CheckBox

    constructor(key: String, title: StringHolder, def: Boolean) : this(key, title,
            BooleanHolder.constant(def))

    constructor(key: String, title: Int, def: Boolean) : this(key, StringHolder.resource(title),
            BooleanHolder.constant(def))

    override fun onCreateView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_extra_config_checkbox, parent, false)
    }

    override fun onViewCreated(context: Context, view: View, fragment: CustomTabsFragment.TabEditorDialogFragment) {
        super.onViewCreated(context, view, fragment)
        val titleView = view.findViewById<TextView>(android.R.id.title)
        val summaryView = view.findViewById<TextView>(android.R.id.summary)
        titleView.text = title.createString(context)

        val summary = this.summary
        if (summary != null) {
            summaryView.visibility = View.VISIBLE
            summaryView.text = summary.createString(context)
        } else {
            summaryView.visibility = View.GONE
        }

        checkBox = view.findViewById<CheckBox>(android.R.id.checkbox)
        checkBox.visibility = View.VISIBLE
        view.setOnClickListener { checkBox.toggle() }

        value = defaultValue.createBoolean(context)
    }
}
