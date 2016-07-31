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

package org.mariotaku.twidere.util

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.style.URLSpan
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.webkit.URLUtil
import android.widget.TextView

import org.mariotaku.twidere.R

/**
 * Created by mariotaku on 15/5/10.
 */
class StatusActionModeCallback(private val textView: TextView, private val context: Context) : ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.action_status_text_selection, menu)
        mode.setTitle(android.R.string.selectTextMode)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        val start = textView.selectionStart
        val end = textView.selectionEnd
        val string = SpannableString.valueOf(textView.text)
        val spans = string.getSpans(start, end, URLSpan::class.java)
        val selectingLink = spans.size == 1 && URLUtil.isValidUrl(spans[0].url)
        MenuUtils.setItemAvailability(menu, R.id.copy_url, selectingLink)
        MenuUtils.setItemAvailability(menu, R.id.share_url, selectingLink)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.copy_url -> {
                val start = textView.selectionStart
                val end = textView.selectionEnd
                val string = SpannableString.valueOf(textView.text)
                val spans = string.getSpans(start, end, URLSpan::class.java)
                if (spans.size != 1) return true
                ClipboardUtils.setText(context, spans[0].url)
                mode.finish()
                return true
            }
            R.id.share_url -> {
                val start = textView.selectionStart
                val end = textView.selectionEnd
                val string = SpannableString.valueOf(textView.text)
                val spans = string.getSpans(start, end, URLSpan::class.java)
                if (spans.size != 1) return true
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, spans[0].url)
                context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_link)))
                mode.finish()
                return true
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {

    }
}
