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

import android.accounts.AccountManager
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ReplacementSpan
import android.view.*
import androidx.annotation.WorkerThread
import androidx.loader.app.LoaderManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_messages_conversation_new.*
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.SelectableUsersAdapter
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.extension.queryOne
import org.mariotaku.twidere.extension.text.appendCompat
import org.mariotaku.twidere.fragment.BaseFragment
import org.mariotaku.twidere.loader.CacheUserSearchLoader
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.ParcelableMessageConversation.ConversationType
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.task.twitter.message.SendMessageTask
import org.mariotaku.twidere.text.MarkForDeleteSpan
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.view.SimpleTextWatcher
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

/**
 * Created by mariotaku on 2017/2/15.
 */
class MessageNewConversationFragment : BaseFragment(), LoaderCallbacks<List<ParcelableUser>?> {

    private val accountKey: UserKey by lazy { arguments?.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)!! }
    private val account: AccountDetails by lazy {
        AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)!!
    }

    private var selectedRecipients: List<ParcelableUser>
        get() {
            val text = editParticipants.editableText ?: return emptyList()
            return text.getSpans(0, text.length, ParticipantSpan::class.java).map(ParticipantSpan::user)
        }
        set(value) {
            val roundRadius = resources.getDimension(R.dimen.element_spacing_xsmall)
            val spanPadding = resources.getDimension(R.dimen.element_spacing_xsmall)
            val nameFirst = preferences[nameFirstKey]
            editParticipants.text = SpannableStringBuilder().apply {
                value.forEach { user ->
                    val displayName = userColorNameManager.getDisplayName(user, nameFirst)
                    val span = ParticipantSpan(user, displayName, roundRadius, spanPadding)
                    appendCompat(user.screen_name, span, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    append(" ")
                }
            }
        }

    private var loaderInitialized: Boolean = false
    private var performSearchRequestRunnable: Runnable? = null

    private lateinit var usersAdapter: SelectableUsersAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = context ?: return
        setHasOptionsMenu(true)
        usersAdapter = SelectableUsersAdapter(context, requestManager)
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

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s !is Spannable) return
                s.getSpans(0, s.length, PendingQuerySpan::class.java).forEach { span ->
                    s.removeSpan(span)
                }
                // Processing deletion
                if (count < before) {
                    val spans = s.getSpans(start, start, ParticipantSpan::class.java)
                    if (spans.isNotEmpty()) {
                        spans.forEach { span ->
                            val deleteStart = s.getSpanStart(span)
                            val deleteEnd = s.getSpanEnd(span)
                            s.removeSpan(span)
                            s.setSpan(MarkForDeleteSpan(), deleteStart, deleteEnd,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            updateCheckState()
                        }
                        return
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
                    s.setSpan(PendingQuerySpan(), spaceNextStart, start + count, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    searchUser(s.substring(spaceNextStart), true)
                }
            }
        })
        val nameFirst = preferences[nameFirstKey]
        val roundRadius = resources.getDimension(R.dimen.element_spacing_xsmall)
        val spanPadding = resources.getDimension(R.dimen.element_spacing_xsmall)
        usersAdapter.itemCheckedListener = itemChecked@ { pos, checked ->
            val text: Editable = editParticipants.editableText ?: return@itemChecked false
            val user = usersAdapter.getUser(pos)
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
                text.appendCompat(user.screen_name, span, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                text.append(" ")
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
            return@itemChecked true
        }

        if (savedInstanceState == null) {
            arguments?.let {
                val users = it.getNullableTypedArray<ParcelableUser>(EXTRA_USERS)
                if (users != null && users.isNotEmpty()) {
                    selectedRecipients = users.toList()
                    editParticipants.setSelection(editParticipants.length())
                    if (it.getBoolean(EXTRA_OPEN_CONVERSATION)) {
                        createOrOpenConversation()
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_messages_conversation_new, container, false)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ParcelableUser>?> {
        val query = args!!.getString(EXTRA_QUERY)!!
        val fromCache = args.getBoolean(EXTRA_FROM_CACHE)
        val fromUser = args.getBoolean(EXTRA_FROM_USER)
        return CacheUserSearchLoader(requireContext(), accountKey, query, !fromCache, true, fromUser)
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableUser>?>) {
        usersAdapter.data = null
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableUser>?>, data: List<ParcelableUser>?) {
        usersAdapter.data = data
        updateCheckState()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_messages_conversation_new, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.setItemAvailability(R.id.create_conversation, selectedRecipients.isNotEmpty())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.create_conversation -> {
                createOrOpenConversation()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createOrOpenConversation() {
        val account = this.account
        val context = context ?: return
        val activity = activity ?: return
        val selected = this.selectedRecipients
        if (selected.isEmpty()) return
        val maxParticipants = if (account.isOfficial(context)) {
            defaultFeatures.twitterDirectMessageMaxParticipants
        } else {
            1
        }
        if (selected.size > maxParticipants) {
            editParticipants.error = getString(R.string.error_message_message_too_many_participants)
            return
        }
        val conversation = ParcelableMessageConversation()
        conversation.account_color = account.color
        conversation.account_key = account.key
        conversation.id = "${SendMessageTask.TEMP_CONVERSATION_ID_PREFIX}${System.currentTimeMillis()}"
        conversation.local_timestamp = System.currentTimeMillis()
        conversation.conversation_type = if (selected.size > 1) {
            ConversationType.GROUP
        } else {
            ConversationType.ONE_TO_ONE
        }
        conversation.participants = (selected + account.user).toTypedArray()
        conversation.is_temp = true

        if (conversation.conversation_type == ConversationType.ONE_TO_ONE) {
            val participantKeys = conversation.participants.map(ParcelableUser::key)
            val existingConversation = findMessageConversation(context, accountKey, participantKeys)
            if (existingConversation != null) {
                activity.startActivity(IntentUtils.messageConversation(accountKey, existingConversation.id))
                activity.finish()
                return
            }
        }

        val values = ObjectCursor.valuesCreatorFrom(ParcelableMessageConversation::class.java).create(conversation)
        context.contentResolver.insert(Conversations.CONTENT_URI, values)
        activity.startActivity(IntentUtils.messageConversation(accountKey, conversation.id))
        activity.finish()
    }

    private fun updateCheckState() {
        val selected = selectedRecipients
        usersAdapter.clearCheckState()
        usersAdapter.clearLockedState()
        usersAdapter.setLockedState(accountKey, true)
        selected.forEach { user ->
            usersAdapter.setCheckState(user.key, true)
        }
        usersAdapter.notifyDataSetChanged()
        activity?.invalidateOptionsMenu()
    }


    private fun searchUser(query: String, fromType: Boolean) {
        if (TextUtils.isEmpty(query)) {
            return
        }
        val args = Bundle {
            this[EXTRA_ACCOUNT_KEY] = accountKey
            this[EXTRA_QUERY] = query
            this[EXTRA_FROM_CACHE] = fromType
        }
        if (loaderInitialized) {
            LoaderManager.getInstance(this).initLoader(0, args, this)
            loaderInitialized = true
        } else {
            LoaderManager.getInstance(this).restartLoader(0, args, this)
        }
        if (performSearchRequestRunnable != null) {
            editParticipants.removeCallbacks(performSearchRequestRunnable)
        }
        if (fromType) {
            performSearchRequestRunnable = PerformSearchRequestRunnable(query, this)
            editParticipants.postDelayed(performSearchRequestRunnable, 1000L)
        }
    }


    @WorkerThread
    fun findMessageConversation(context: Context, accountKey: UserKey,
            participantKeys: Collection<UserKey>): ParcelableMessageConversation? {
        val resolver = context.contentResolver
        val where = Expression.and(Expression.equalsArgs(Conversations.ACCOUNT_KEY),
                Expression.equalsArgs(Conversations.PARTICIPANT_KEYS)).sql
        val whereArgs = arrayOf(accountKey.toString(), participantKeys.sorted().joinToString(","))
        return resolver.queryOne(Conversations.CONTENT_URI, Conversations.COLUMNS, where, whereArgs,
                null, ParcelableMessageConversation::class.java)
    }

    internal class PerformSearchRequestRunnable(val query: String, fragment: MessageNewConversationFragment) : Runnable {
        val fragmentRef = WeakReference(fragment)
        override fun run() {
            val fragment = fragmentRef.get() ?: return
            fragment.searchUser(query, false)
        }

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
            return (nameWidth + padding * 2).roundToInt()
        }

    }

}