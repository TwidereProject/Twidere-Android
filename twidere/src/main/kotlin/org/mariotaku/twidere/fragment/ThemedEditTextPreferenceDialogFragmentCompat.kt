/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package org.mariotaku.twidere.fragment

import android.content.Context
import android.os.Bundle
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.view.View
import android.widget.EditText

import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.view.ChameleonTextView

class ThemedEditTextPreferenceDialogFragmentCompat : ThemedPreferenceDialogFragmentCompat() {

    private lateinit var editText: EditText

    override fun needInputMethod(): Boolean {
        return true
    }

    override fun onCreateDialogView(context: Context): View {
        val view = super.onCreateDialogView(context)
        val theme = Chameleon.getOverrideTheme(context, context)
        editText = view.findViewById<EditText>(android.R.id.edit)
        val appearance = ChameleonTextView.Appearance.create(editText, context, null, theme)
        appearance.backgroundTintColor = theme.colorAccent
        ChameleonTextView.Appearance.apply(editText, appearance)
        return view
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        val preference = preference as EditTextPreference
        editText.setText(preference.text)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val value = editText.text.toString()
            val preference = preference as EditTextPreference
            if (preference.callChangeListener(value)) {
                preference.text = value
            }
        }

    }

    companion object {

        fun newInstance(key: String): ThemedEditTextPreferenceDialogFragmentCompat {
            val fragment = ThemedEditTextPreferenceDialogFragmentCompat()
            val args = Bundle()
            args.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = args
            return fragment
        }
    }
}