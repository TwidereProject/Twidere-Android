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
import android.os.Build
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import android.text.InputType
import android.text.Selection
import android.text.method.ArrowKeyMovementMethod
import android.text.method.MovementMethod
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.AdapterView
import com.bumptech.glide.Glide
import org.mariotaku.chameleon.view.ChameleonMultiAutoCompleteTextView
import org.mariotaku.ktextension.contains
import org.mariotaku.twidere.adapter.ComposeAutoCompleteAdapter
import org.mariotaku.twidere.extension.setupEmojiFactory
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.util.widget.StatusTextTokenizer


class ComposeEditText(
        context: Context,
        attrs: AttributeSet? = null
) : ChameleonMultiAutoCompleteTextView(context, attrs) {

    private var adapter: ComposeAutoCompleteAdapter? = null
    var imageInputListener: ((InputContentInfoCompat) -> Unit)? = null
    var account: AccountDetails? = null
        set(value) {
            field = value
            updateAccount()
        }

    init {
        setupEmojiFactory()
        setTokenizer(StatusTextTokenizer())
        onItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
            removeIMESuggestions()
        }
        // HACK: remove AUTO_COMPLETE flag to force IME show auto completion
        setRawInputType(inputType and InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE.inv())
    }

    override fun getDefaultMovementMethod(): MovementMethod {
        return ArrowKeyMovementMethod()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode && adapter == null) {
            adapter = ComposeAutoCompleteAdapter(context, Glide.with(context))
        }
        setAdapter(adapter)
        updateAccount()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        adapter?.closeCursor()
        adapter = null
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return try {
            super.onTextContextMenuItem(id)
        } catch (e: AbstractMethodError) {
            // http://crashes.to/s/69acd0ea0de
            true
        }catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            // workaround
            // https://github.com/TwidereProject/Twidere-Android/issues/1178
            setSelection(length() - 1, length() - 1)
            setSelection(length(), length())
            true
        }
    }

    override fun onCreateInputConnection(editorInfo: EditorInfo?): InputConnection? {
        if (editorInfo == null) return null
        val ic = super.onCreateInputConnection(editorInfo) ?: return null
        EditorInfoCompat.setContentMimeTypes(editorInfo, arrayOf("image/*"))

        val callback = InputConnectionCompat.OnCommitContentListener { inputContentInfo, flags, _ ->
            // read and display inputContentInfo asynchronously
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1
                    && InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION in flags) {
                try {
                    inputContentInfo.requestPermission()
                } catch (e: Exception) {
                    return@OnCommitContentListener false // return false if failed
                }

            }

            // read and display inputContentInfo asynchronously.
            // call inputContentInfo.releasePermission() as needed.
            imageInputListener?.invoke(inputContentInfo)
            return@OnCommitContentListener true
        }
        return InputConnectionCompat.createWrapper(ic, editorInfo, callback)
    }

    private fun updateAccount() {
        adapter?.account = account
    }

    private fun removeIMESuggestions() {
        val selectionEnd = selectionEnd
        val selectionStart = selectionStart
        Selection.removeSelection(text)
        setSelection(selectionStart, selectionEnd)
    }
}
