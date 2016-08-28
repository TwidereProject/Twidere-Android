/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.view

import android.content.Context
import android.content.res.ColorStateList
import android.text.InputType
import android.text.Selection
import android.text.method.ArrowKeyMovementMethod
import android.text.method.MovementMethod
import android.util.AttributeSet
import android.widget.AdapterView
import com.afollestad.appthemeengine.inflation.ATEMultiAutoCompleteTextView
import org.mariotaku.twidere.adapter.ComposeAutoCompleteAdapter
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.EmojiSupportUtils
import org.mariotaku.twidere.util.widget.StatusTextTokenizer
import org.mariotaku.twidere.view.iface.IThemeBackgroundTintView

class ComposeEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ATEMultiAutoCompleteTextView(context, attrs), IThemeBackgroundTintView {

    private var adapter: ComposeAutoCompleteAdapter? = null
    var accountKey: UserKey? = null
        set(value) {
            field = value
            updateAccountKey()
        }

    init {
        EmojiSupportUtils.initForTextView(this)
        setTokenizer(StatusTextTokenizer())
        onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            removeIMESuggestions()
        }
        // HACK: remove AUTO_COMPLETE flag to force IME show auto completion
        setRawInputType(inputType and InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE.inv())
    }

    override fun setBackgroundTintColor(color: ColorStateList) {
        supportBackgroundTintList = color
    }

    override fun getDefaultMovementMethod(): MovementMethod {
        return ArrowKeyMovementMethod.getInstance()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode && adapter == null) {
            adapter = ComposeAutoCompleteAdapter(context)
        }
        setAdapter<ComposeAutoCompleteAdapter>(adapter)
        updateAccountKey()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        adapter?.closeCursor()
        adapter = null
    }

    private fun updateAccountKey() {
        adapter?.accountKey = accountKey
    }

    private fun removeIMESuggestions() {
        val selectionEnd = selectionEnd
        val selectionStart = selectionStart
        Selection.removeSelection(text)
        setSelection(selectionStart, selectionEnd)
    }
}
