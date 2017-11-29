/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.model.tab.conf

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import org.mariotaku.ktextension.mapToArray

import org.mariotaku.twidere.R
import org.mariotaku.twidere.fragment.CustomTabsFragment
import org.mariotaku.twidere.model.tab.StringHolder
import org.mariotaku.twidere.model.tab.TabConfiguration

class IntSpinnerExtraConfiguration(
        key: String,
        title: StringHolder,
        private val entries: Array<StringHolder>,
        private val values: IntArray,
        private val def: Int
) : TabConfiguration.ExtraConfiguration(key, title) {
    var maxLines: Int = 0

    var value: Int
        get() = values[spinner.selectedItemPosition]
        set(value) = spinner.setSelection(values.indexOf(value))

    private lateinit var spinner: Spinner

    constructor(key: String, titleRes: Int, entriesRes: IntArray, values: IntArray, def: Int) :
            this(key, StringHolder.resource(titleRes), entriesRes.mapToArray { StringHolder.resource(it) }, values, def)

    override fun onCreateView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_extra_config_spinner, parent, false)
    }

    override fun onViewCreated(context: Context, view: View, fragment: CustomTabsFragment.TabEditorDialogFragment) {
        super.onViewCreated(context, view, fragment)
        spinner = view.findViewById(R.id.spinner)
        spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, entries.mapToArray {
            it.createString(context)
        }).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        value = def
    }

}
