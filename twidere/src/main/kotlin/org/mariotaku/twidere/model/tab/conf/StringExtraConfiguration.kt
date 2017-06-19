package org.mariotaku.twidere.model.tab.conf

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

import org.mariotaku.twidere.R
import org.mariotaku.twidere.fragment.CustomTabsFragment
import org.mariotaku.twidere.model.tab.StringHolder
import org.mariotaku.twidere.model.tab.TabConfiguration

/**
 * Created by mariotaku on 2016/12/5.
 */
class StringExtraConfiguration(key: String, title: StringHolder, private val def: String?) :
        TabConfiguration.ExtraConfiguration(key, title) {
    var maxLines: Int = 0

    var value: String?
        get() = editText.text?.toString()
        set(value) = editText.setText(value)

    private lateinit var editText: EditText

    constructor(key: String, titleRes: Int, def: String?) : this(key,
            StringHolder.resource(titleRes), def)

    override fun onCreateView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_extra_config_text, parent, false)
    }

    override fun onViewCreated(context: Context, view: View, fragment: CustomTabsFragment.TabEditorDialogFragment) {
        super.onViewCreated(context, view, fragment)
        editText = view.findViewById<EditText>(R.id.editText)
        editText.hint = title.createString(context)
        editText.setText(def)
    }

    fun maxLines(maxLines: Int): StringExtraConfiguration {
        this.maxLines = maxLines
        return this
    }
}
