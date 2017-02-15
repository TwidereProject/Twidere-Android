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

package org.mariotaku.twidere.fragment.message

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.Spannable
import android.text.TextUtils
import android.text.style.ReplacementSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_messages_conversation_new.*
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.SelectableUsersAdapter
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.fragment.BaseFragment
import org.mariotaku.twidere.loader.CacheUserSearchLoader
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.text.MarkForDeleteSpan
import org.mariotaku.twidere.util.view.SimpleTextWatcher

/**
 * Created by mariotaku on 2017/2/15.
 */
class MessageNewConversationFragment : BaseFragment(), LoaderCallbacks<List<ParcelableUser>?> {

    private val accountKey: UserKey get() = arguments.getParcelable(EXTRA_ACCOUNT_KEY)
    private var loaderInitialized: Boolean = false

    private lateinit var usersAdapter: SelectableUsersAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        usersAdapter = SelectableUsersAdapter(context)
        recyclerView.adapter = usersAdapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        editParticipants.addTextChangedListener(object : SimpleTextWatcher {
            override fun afterTextChanged(s: Editable) {
                s.getSpans(0, s.length, MarkForDeleteSpan::class.java).forEach { span ->
                    val deleteStart = s.getSpanStart(span)
                    val deleteEnd = s.getSpanEnd(span)
                    s.removeSpan(span)
                    s.delete(deleteStart, deleteEnd)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                super.beforeTextChanged(s, start, count, after)
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s !is Spannable) return
                s.getSpans(0, s.length, PendingQuerySpan::class.java).forEach { span ->
                    s.removeSpan(span)
                }
                // Processing deletion
                if (count < before) {
                    val spans = s.getSpans(start, start, ParticipantSpan::class.java)
                    spans.forEach { span ->
                        val deleteStart = s.getSpanStart(span)
                        val deleteEnd = s.getSpanEnd(span)
                        s.removeSpan(span)
                        s.setSpan(MarkForDeleteSpan(), deleteStart, deleteEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                val spaceNextStart = run {
                    val spaceIdx = s.indexOfLast(Char::isWhitespace)
                    if (spaceIdx < 0) return@run 0
                    return@run spaceIdx + 1
                }
                // Skip if last char is space
                if (spaceNextStart > s.lastIndex) return
                if (s.getSpans(start, start + count, ParticipantSpan::class.java).isEmpty()) {
                    s.setSpan(PendingQuerySpan(), spaceNextStart, spaceNextStart + count, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    searchUser(s.substring(spaceNextStart), true)
                }
            }
        })
        val nameFirst = preferences[nameFirstKey]
        val roundRadius = resources.getDimension(R.dimen.element_spacing_xsmall)
        val spanPadding = resources.getDimension(R.dimen.element_spacing_xsmall)
        usersAdapter.itemCheckedListener = itemChecked@ { pos, checked ->
            val text: Editable = editParticipants.editableText ?: return@itemChecked
            val user = usersAdapter.getUser(pos) ?: return@itemChecked
            if (checked) {
                text.getSpans(0, text.length, PendingQuerySpan::class.java).forEach { pending ->
                    val start = text.getSpanStart(pending)
                    val end = text.getSpanEnd(pending)
                    text.removeSpan(pending)
                    if (start < 0 || end < 0 || end < start) return@forEach
                    text.delete(start, end)
                }
                val displayName = userColorNameManager.getDisplayName(user, nameFirst)
                val span = ParticipantSpan(user, displayName, roundRadius, spanPadding)
                val start = text.length
                text.append(user.screen_name)
                val end = text.length
                text.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.append(' ')
            } else {
                text.getSpans(0, text.length, ParticipantSpan::class.java).forEach { span ->
                    if (user != span.user) {
                        return@forEach
                    }
                    val start = text.getSpanStart(span)
                    var end = text.getSpanEnd(span)
                    text.removeSpan(span)
                    // Also remove last whitespace
                    if (end <= text.lastIndex && text[end].isWhitespace()) {
                        end += 1
                    }
                    text.delete(start, end)
                }
            }
            editParticipants.clearComposingText()
            updateCheckState()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_messages_conversation_new, container, false)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<ParcelableUser>?> {
        val query = args.getString(EXTRA_QUERY)
        val fromCache = args.getBoolean(EXTRA_FROM_CACHE)
        val fromUser = args.getBoolean(EXTRA_FROM_USER)
        return CacheUserSearchLoader(context, accountKey, query, fromCache, fromUser)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableUser>?>) {
        usersAdapter.data = null
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableUser>?>, data: List<ParcelableUser>?) {
        usersAdapter.data = data
        updateCheckState()
    }

    private fun updateCheckState() {
        val selected = selectedRecipients
        usersAdapter.clearCheckState()
        selected.forEach { user ->
            usersAdapter.setCheckState(user.key, true)
        }
        usersAdapter.notifyDataSetChanged()
    }

    private fun searchUser(query: String, fromCache: Boolean) {
        if (TextUtils.isEmpty(query)) {
            return
        }
        val args = Bundle {
            this[EXTRA_ACCOUNT_KEY] = accountKey
            this[EXTRA_QUERY] = query
            this[EXTRA_FROM_CACHE] = fromCache
        }
        if (loaderInitialized) {
            loaderManager.initLoader(0, args, this)
            loaderInitialized = true
        } else {
            loaderManager.restartLoader(0, args, this)
        }
    }

    private val selectedRecipients: List<ParcelableUser>
        get() {
            val text = editParticipants.editableText ?: return emptyList()
            return text.getSpans(0, text.length, ParticipantSpan::class.java).map(ParticipantSpan::user)
        }

    class PendingQuerySpan

    class ParticipantSpan(
            val user: ParcelableUser,
            val displayName: String,
            val roundRadius: Float,
            val padding: Float
    ) : ReplacementSpan() {

        private var backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var backgroundBounds = RectF()
        private var nameWidth: Float = 0f

        init {
            backgroundPaint.color = 0x20808080
        }

        override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            backgroundBounds.set(x, top.toFloat() + padding / 2, x + nameWidth + padding * 2, bottom - padding / 2)
            canvas.drawRoundRect(backgroundBounds, roundRadius, roundRadius, backgroundPaint)
            val textSizeBackup = paint.textSize
            paint.textSize = textSizeBackup - padding
            canvas.drawText(displayName, x + padding, y - padding / 2, paint)
            paint.textSize = textSizeBackup
        }

        override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            val textSizeBackup = paint.textSize
            paint.textSize = textSizeBackup - padding
            nameWidth = paint.measureText(displayName)
            paint.textSize = textSizeBackup
            return Math.round(nameWidth + padding * 2)
        }

    }
}