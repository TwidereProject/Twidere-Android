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

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_home_content.view.*
import kotlinx.android.synthetic.main.fragment_messages_conversation_info.*
import kotlinx.android.synthetic.main.layout_toolbar_message_conversation_title.*
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.useCursor
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_CONVERSATION_ID
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.extension.model.displayAvatarTo
import org.mariotaku.twidere.extension.model.getConversationName
import org.mariotaku.twidere.fragment.BaseFragment
import org.mariotaku.twidere.fragment.iface.IToolBarSupportFragment
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.ParcelableMessageConversationCursorIndices
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations

/**
 * Created by mariotaku on 2017/2/15.
 */

class MessageConversationInfoFragment : BaseFragment(), IToolBarSupportFragment,
        LoaderManager.LoaderCallbacks<ParcelableMessageConversation?> {

    private val accountKey: UserKey get() = arguments.getParcelable(EXTRA_ACCOUNT_KEY)
    private val conversationId: String get() = arguments.getString(EXTRA_CONVERSATION_ID)

    override val controlBarHeight: Int get() = toolbar.measuredHeight
    override var controlBarOffset: Float = 0f

    override val toolbar: Toolbar
        get() = toolbarLayout.toolbar

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = this.activity

        if (activity is AppCompatActivity) {
            activity.supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        val theme = Chameleon.getOverrideTheme(context, activity)

        val profileImageStyle = preferences[profileImageStyleKey]
        appBarConversationAvatar.style = profileImageStyle
        conversationAvatar.style = profileImageStyle

        val avatarBackground = ChameleonUtils.getColorDependent(theme.colorToolbar)
        appBarConversationAvatar.setBackgroundColor(avatarBackground)
        appBarConversationName.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))
        
        conversationAvatar.setBackgroundColor(avatarBackground)
        conversationName.setTextColor(ChameleonUtils.getColorDependent(theme.colorToolbar))

        loaderManager.initLoader(0, null, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_messages_conversation_info, container, false)
    }

    override fun setupWindow(activity: FragmentActivity): Boolean {
        return false
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ParcelableMessageConversation?> {
        return ConversationInfoLoader(context, accountKey, conversationId)
    }

    override fun onLoaderReset(loader: Loader<ParcelableMessageConversation?>?) {
    }

    override fun onLoadFinished(loader: Loader<ParcelableMessageConversation?>?, data: ParcelableMessageConversation?) {
        if (data == null) {
            activity?.finish()
            return
        }
        val name = data.getConversationName(context, userColorNameManager, preferences[nameFirstKey]).first
        data.displayAvatarTo(mediaLoader, conversationAvatar)
        data.displayAvatarTo(mediaLoader, appBarConversationAvatar)
        appBarConversationName.text = name
        conversationName.text = name
    }

    class ConversationInfoLoader(
            context: Context,
            val accountKey: UserKey,
            val conversationId: String) : AsyncTaskLoader<ParcelableMessageConversation?>(context) {
        override fun loadInBackground(): ParcelableMessageConversation? {
            val where = Expression.and(Expression.equalsArgs(Conversations.ACCOUNT_KEY),
                    Expression.equalsArgs(Conversations.CONVERSATION_ID)).sql
            val whereArgs = arrayOf(accountKey.toString(), conversationId)
            context.contentResolver.query(Conversations.CONTENT_URI, Conversations.COLUMNS, where,
                    whereArgs, null).useCursor { cur ->
                if (cur.moveToFirst()) {
                    return ParcelableMessageConversationCursorIndices.fromCursor(cur)
                }
            }
            return null
        }

        override fun onStartLoading() {
            forceLoad()
        }
    }
}
