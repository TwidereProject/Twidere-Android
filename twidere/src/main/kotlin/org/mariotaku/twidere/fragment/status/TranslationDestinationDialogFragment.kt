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

package org.mariotaku.twidere.fragment.status

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AlertDialog
import kotlinx.android.parcel.Parcelize
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.getTypedArray
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.translationDestinationKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.fragment.BaseDialogFragment
import java.text.Collator
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class TranslationDestinationDialogFragment : BaseDialogFragment() {

    private val currentIndex = AtomicInteger(-1)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val languages = arguments?.getTypedArray<DisplayLanguage>(EXTRA_LANGUAGES)?.sortedArrayWith(LanguageComparator()) ?: emptyArray()
        val selectedLanguage = preferences[translationDestinationKey] ?: arguments?.getString(EXTRA_SELECTED_LANGUAGE)
        val selectedIndex = languages.indexOfFirst { selectedLanguage == it.code }
        builder.setTitle(R.string.title_translate_to)
        builder.setSingleChoiceItems(languages.mapToArray { it.name }, selectedIndex) { _, which ->
            currentIndex.set(which)
        }
        builder.setPositiveButton(android.R.string.ok) lambda@ { _, _ ->
            val idx = currentIndex.get()
            if (idx < 0) return@lambda
            preferences[translationDestinationKey] = languages[idx].code
            (targetFragment as? StatusFragment)?.reloadTranslation()
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.onShow {
            it.applyTheme()
            it.listView?.isFastScrollEnabled = true
        }
        return dialog
    }

    @Parcelize
    data class DisplayLanguage(val name: String, val code: String): Parcelable

    private class LanguageComparator : Comparator<DisplayLanguage> {

        private val collator = Collator.getInstance(Locale.getDefault())

        override fun compare(object1: DisplayLanguage, object2: DisplayLanguage): Int {
            return collator.compare(object1.name, object2.name)
        }

    }

    companion object {
        const val EXTRA_SELECTED_LANGUAGE = "selected_language"
        const val EXTRA_LANGUAGES = "languages"

        fun create(languages: List<DisplayLanguage>, selectedLanguage: String?): TranslationDestinationDialogFragment {
            val df = TranslationDestinationDialogFragment()
            df.arguments = Bundle {
                this[EXTRA_LANGUAGES] = languages.toTypedArray()
                this[EXTRA_SELECTED_LANGUAGE] = selectedLanguage
            }
            return df
        }
    }
}