/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.fragment

import android.accounts.AccountManager
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.view.ViewCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Property
import android.view.*
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_messages_conversation.*
import kotlinx.android.synthetic.main.layout_actionbar_message_user_picker.view.*
import me.uucky.colorpicker.internal.EffectViewHelper
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.OrderBy
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.activity.ThemedMediaPickerActivity
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter
import org.mariotaku.twidere.adapter.MessageConversationAdapter
import org.mariotaku.twidere.adapter.SimpleParcelableUsersAdapter
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.ACTION_NAVIGATION_BACK
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.CONTEXT_TAG_NAVIGATION
import org.mariotaku.twidere.constant.SharedPreferenceConstants
import org.mariotaku.twidere.loader.CacheUserSearchLoader
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableDirectMessage
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.TaskStateChangedEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Conversation
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.ConversationEntries
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.EditTextEnterHandler.EnterListener
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.TakeAllKeyboardShortcut
import org.mariotaku.twidere.util.Utils.buildDirectMessageConversationUri
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.view.ExtendedRecyclerView
import java.util.*
import javax.inject.Inject

class MessagesConversationFragment : BaseFragment(), LoaderCallbacks<Cursor?>, OnClickListener,
        OnItemSelectedListener, KeyboardShortcutCallback, TakeAllKeyboardShortcut {

    // Callbacks and listeners
    private val searchLoadersCallback = object : LoaderCallbacks<List<ParcelableUser>> {
        override fun onCreateLoader(id: Int, args: Bundle): Loader<List<ParcelableUser>> {
            usersSearchList.visibility = View.GONE
            usersSearchEmpty.visibility = View.GONE
            usersSearchProgress.visibility = View.VISIBLE
            val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
            val query = args.getString(EXTRA_QUERY)
            val fromCache = args.getBoolean(EXTRA_FROM_CACHE)
            val fromUser = args.getBoolean(EXTRA_FROM_USER, false)
            return CacheUserSearchLoader(context, accountKey, query, fromCache, fromUser)
        }

        override fun onLoadFinished(loader: Loader<List<ParcelableUser>>, data: List<ParcelableUser>?) {
            usersSearchList.visibility = View.VISIBLE
            usersSearchProgress.visibility = View.GONE
            usersSearchEmpty.visibility = if (data == null || data.isEmpty()) View.GONE else View.VISIBLE
            usersSearchAdapter!!.setData(data, true)
            updateEmptyText()
        }

        override fun onLoaderReset(loader: Loader<List<ParcelableUser>>) {

        }
    }
    private var scrollListener: PanelShowHideListener? = null

    private var messageDrafts: SharedPreferences? = null

    // Adapters
    private var adapter: MessageConversationAdapter? = null
    private var usersSearchAdapter: SimpleParcelableUsersAdapter? = null

    // Data fields
    private var searchUsersLoaderInitialized: Boolean = false
    private var navigateBackPressed: Boolean = false
    private var loaderInitialized: Boolean = false
    private var imageUri: String? = null
    private var account: AccountDetails? = null
    private var recipient: ParcelableUser? = null
    private var textChanged: Boolean = false
    private var queryTextChanged: Boolean = false

    private val backTimeoutRunnable = Runnable { navigateBackPressed = false }

    private val actionBarCustomView: View
        get() = (activity as AppCompatActivity).supportActionBar!!.customView

    @Subscribe
    fun notifyTaskStateChanged(event: TaskStateChangedEvent) {
        updateRefreshState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PICK_MEDIA -> {
                if (resultCode == Activity.RESULT_OK && data!!.dataString != null) {
                    imageUri = data.dataString
                    updateAddImageButton()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_messages_conversation, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = activity as BaseActivity
        messageDrafts = activity.getSharedPreferences(MESSAGE_DRAFTS_PREFERENCES_NAME, Context.MODE_PRIVATE)

        setHasOptionsMenu(true)
        val actionBar = activity.supportActionBar!!
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_CUSTOM)
        actionBar.setCustomView(R.layout.layout_actionbar_message_user_picker)
        val am = AccountManager.get(context)
        val accounts = AccountUtils.getAllAccountDetails(am, AccountUtils.getAccounts(am), true).asList()
        val accountsSpinnerAdapter = AccountsSpinnerAdapter(
                actionBar.themedContext, R.layout.spinner_item_account_icon)
        accountsSpinnerAdapter.setDropDownViewResource(R.layout.list_item_simple_user)
        accountsSpinnerAdapter.addAll(accounts)
        actionBarCustomView.accountSpinner.adapter = accountsSpinnerAdapter
        actionBarCustomView.accountSpinner.onItemSelectedListener = this
        actionBarCustomView.queryButton.setOnClickListener(this)
        adapter = MessageConversationAdapter(activity)
        val layoutManager = FixedLinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        val useOutline = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP

        val effectHelper: EffectViewHelper
        if (useOutline) {
            val elevation = resources.getDimension(R.dimen.element_spacing_normal)
            val property = PanelElevationProperty(elevation)
            effectHelper = EffectViewHelper(inputPanel, property, 100)
        } else {
            effectHelper = EffectViewHelper(inputPanelShadowCompat, View.ALPHA, 100)
        }
        scrollListener = PanelShowHideListener(effectHelper)

        inputPanelShadowCompat.visibility = if (useOutline) View.GONE else View.VISIBLE
        ViewCompat.setAlpha(inputPanelShadowCompat, 0f)

        usersSearchAdapter = SimpleParcelableUsersAdapter(activity)
        usersSearchList.adapter = usersSearchAdapter
        usersSearchList.emptyView = usersSearchEmpty
        usersSearchList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val account = actionBarCustomView.accountSpinner.selectedItem as AccountDetails
            showConversation(account, usersSearchAdapter!!.getItem(position))
            updateRecipientInfo()
        }

        setupEditQuery()
        setupEditText()

        sendMessage.setOnClickListener(this)
        addImage.setOnClickListener(this)
        sendMessage.isEnabled = false
        if (savedInstanceState != null) {
            val account: AccountDetails? = savedInstanceState.getParcelable(EXTRA_ACCOUNT)
            val recipient: ParcelableUser? = savedInstanceState.getParcelable(EXTRA_USER)
            showConversation(account, recipient)
            editText.setText(savedInstanceState.getString(EXTRA_TEXT))
            imageUri = savedInstanceState.getString(EXTRA_IMAGE_URI)
        } else {
            val args = arguments
            val account: AccountDetails?
            val recipient: ParcelableUser?
            if (args != null) {
                if (args.containsKey(EXTRA_ACCOUNT)) {
                    account = args.getParcelable(EXTRA_ACCOUNT)
                    recipient = args.getParcelable<ParcelableUser>(EXTRA_USER)
                } else if (args.containsKey(EXTRA_ACCOUNT_KEY) && args.containsKey(EXTRA_RECIPIENT_ID)) {
                    val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
                    if (accountKey == null) {
                        getActivity().finish()
                        return
                    }
                    val accountPos = accountsSpinnerAdapter.findPositionByKey(accountKey)
                    if (accountPos >= 0) {
                        actionBarCustomView.accountSpinner.setSelection(accountPos)
                    }
                    val userId = args.getString(EXTRA_RECIPIENT_ID)
                    if (accountPos >= 0) {
                        account = accountsSpinnerAdapter.getItem(accountPos)
                    } else {
                        account = AccountUtils.getAccountDetails(AccountManager.get(activity), accountKey, true)
                    }
                    if (userId != null) {
                        recipient = Utils.getUserForConversation(activity, accountKey, userId)
                    } else {
                        recipient = null
                    }
                } else {
                    account = null
                    recipient = null
                }
                showConversation(account, recipient)
                if (account != null && recipient != null) {
                    val key = getDraftsTextKey(account.key, recipient.key)
                    editText.setText(messageDrafts!!.getString(key, null))
                }
            }
        }
        editText.setSelection(editText.length())
        val isValid = account != null && recipient != null
        conversationContainer.visibility = if (isValid) View.VISIBLE else View.GONE
        recipientSelectorContainer.visibility = if (isValid) View.GONE else View.VISIBLE

        usersSearchList.visibility = View.GONE
        usersSearchProgress.visibility = View.GONE

        registerForContextMenu(recyclerView)

        queryTextChanged = false
        textChanged = false
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
        updateEmptyText()
        recyclerView.addOnScrollListener(scrollListener)
        scrollListener!!.reset()
    }

    override fun onResume() {
        super.onResume()
        updateAddImageButton()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (editText != null) {
            outState.putCharSequence(EXTRA_TEXT, editText.text)
        }
        outState.putParcelable(EXTRA_ACCOUNT, account)
        outState.putParcelable(EXTRA_USER, recipient)
        outState.putString(EXTRA_IMAGE_URI, imageUri)
    }

    override fun onStop() {
        recyclerView.removeOnScrollListener(scrollListener)
        bus.unregister(this)

        val account = account
        val recipient = recipient
        if (account != null && recipient != null) {
            val key = getDraftsTextKey(account.key, recipient.key)
            val editor = messageDrafts!!.edit()
            val text = ParseUtils.parseString(editText.text)
            if (TextUtils.isEmpty(text)) {
                editor.remove(key)
            } else {
                editor.putString(key, text)
            }
            editor.apply()
        }
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_direct_messages_conversation, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        MenuUtils.setItemAvailability(menu, R.id.delete_all, recipient != null && Utils.isOfficialCredentials(activity, account!!))
        updateRecipientInfo()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.delete_all -> {
                val account = account
                if (account == null || recipient == null) return true
                val args = Bundle()
                args.putParcelable(EXTRA_ACCOUNT, account)
                args.putParcelable(EXTRA_USER, recipient)
                val df = DeleteConversationConfirmDialogFragment()
                df.arguments = args
                df.show(fragmentManager, "delete_conversation_confirm")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
        val accountId = args?.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val recipientId = args?.getString(EXTRA_RECIPIENT_ID)
        val cols = DirectMessages.COLUMNS
        val isValid = accountId != null && recipientId != null
        conversationContainer.visibility = if (isValid) View.VISIBLE else View.GONE
        recipientSelectorContainer.visibility = if (isValid) View.GONE else View.VISIBLE
        if (!isValid) {
            return CursorLoader(activity, TwidereDataStore.CONTENT_URI_NULL, cols, null, null, null)
        }
        val uri = buildDirectMessageConversationUri(accountId, recipientId, null)
        return CursorLoader(activity, uri, cols, null, null, Conversation.DEFAULT_SORT_ORDER)
    }

    override fun onClick(view: View) {
        when (view) {
            sendMessage -> {
                sendDirectMessage()
            }
            addImage -> {
                val intent = ThemedMediaPickerActivity.withThemed(activity).build()
                startActivityForResult(intent, REQUEST_PICK_MEDIA)
            }
            actionBarCustomView.queryButton -> {
                val account = actionBarCustomView.accountSpinner.selectedItem as AccountDetails
                searchUsers(account.key, ParseUtils.parseString(actionBarCustomView.editUserQuery.text), false)
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        val account = actionBarCustomView.accountSpinner.selectedItem as AccountDetails?
        if (account != null) {
            this.account = account
            updateRecipientInfo()
            updateAccount()
        }
    }

    override fun onNothingSelected(view: AdapterView<*>) {

    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {
        adapter!!.setCursor(null)
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, cursor: Cursor?) {
        adapter!!.setCursor(cursor)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (menuInfo == null) return
        val inflater = MenuInflater(context)
        val contextMenuInfo = menuInfo as ExtendedRecyclerView.ContextMenuInfo?
        val message = adapter!!.getDirectMessage(contextMenuInfo!!.position)!!
        menu.setHeaderTitle(message.text_unescaped)
        inflater.inflate(R.menu.action_direct_message, menu)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        if (!userVisibleHint) return false
        val menuInfo = item!!.menuInfo
        val contextMenuInfo = menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val message = adapter!!.getDirectMessage(contextMenuInfo.position) ?: return false
        when (item.itemId) {
            R.id.copy -> {
                ClipboardUtils.setText(context, message.text_plain)
                return true
            }
            R.id.delete -> {
                val args = Bundle()
                args.putParcelable(EXTRA_ACCOUNT, account)
                args.putParcelable(EXTRA_MESSAGE, message)
                val df = DeleteMessageConfirmDialogFragment()
                df.arguments = args
                df.show(fragmentManager, "delete_message_confirm")
                return true
            }
        }
        return false
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (ACTION_NAVIGATION_BACK == action) {
            val showingConversation = isShowingConversation
            val editText = if (showingConversation) editText else actionBarCustomView.editUserQuery
            val textChanged = if (showingConversation) textChanged else queryTextChanged
            if (editText.length() == 0 && !textChanged) {
                val activity = activity
                if (!navigateBackPressed) {
                    Toast.makeText(activity, R.string.message_toast_press_again_to_close, Toast.LENGTH_SHORT).show()
                    editText.removeCallbacks(backTimeoutRunnable)
                    editText.postDelayed(backTimeoutRunnable, 2000)
                    navigateBackPressed = true
                } else {
                    activity.onBackPressed()
                }
            } else {
                queryTextChanged = false
                this.textChanged = false
            }
            return true
        }
        return false
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        return ACTION_NAVIGATION_BACK == action
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int, repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        return false
    }


    fun showConversation(account: AccountDetails?, recipient: ParcelableUser?) {
        this.account = account
        this.recipient = recipient
        if (account == null || recipient == null) return
        val lm = loaderManager
        val args = Bundle()
        args.putParcelable(EXTRA_ACCOUNT_KEY, account.key)
        args.putString(EXTRA_RECIPIENT_ID, recipient.key.id)
        if (loaderInitialized) {
            lm.restartLoader(0, args, this)
        } else {
            loaderInitialized = true
            lm.initLoader(0, args, this)
        }
        AsyncTaskUtils.executeTask(SetReadStateTask(activity, account, recipient))
        updateActionBar()
        updateRecipientInfo()
        updateAccount()
        editText.requestFocus()
    }

    private fun updateAccount() {
        if (account == null) return
        if (Utils.isOfficialCredentials(context, account!!)) {
            addImage.visibility = View.VISIBLE
        } else {
            addImage.visibility = View.GONE
        }
    }

    val isShowingConversation: Boolean
        get() = conversationContainer.visibility == View.VISIBLE

    private fun getDraftsTextKey(accountKey: UserKey, userId: UserKey): String {
        return String.format(Locale.ROOT, "text_%s_to_%s", accountKey, userId)
    }

    private fun searchUsers(accountKey: UserKey, query: String, fromCache: Boolean) {
        val args = Bundle()
        args.putParcelable(EXTRA_ACCOUNT_KEY, accountKey)
        args.putString(EXTRA_QUERY, query)
        args.putBoolean(EXTRA_FROM_CACHE, fromCache)
        val lm = loaderManager
        if (searchUsersLoaderInitialized) {
            lm.restartLoader(LOADER_ID_SEARCH_USERS, args, searchLoadersCallback)
        } else {
            searchUsersLoaderInitialized = true
            lm.initLoader(LOADER_ID_SEARCH_USERS, args, searchLoadersCallback)
        }
    }

    //    @Override
    //    public void onRefreshFromEnd() {
    //        new TwidereAsyncTask<Object, Object, long[][]>() {
    //
    //            @Override
    //            protected long[][] doInBackground(final Object... params) {
    //                final long[][] result = new long[2][];
    //                result[0] = getActivatedAccountIds(getActivity());
    //                result[1] = getNewestMessageIdsFromDatabase(getActivity(), DirectMessages.Inbox.CONTENT_URI);
    //                return result;
    //            }
    //
    //            @Override
    //            protected void onPostExecute(final long[][] result) {
    //                final AsyncTwitterWrapper twitter = getTwitterWrapper();
    //                if (twitter == null) return;
    //                twitter.getReceivedDirectMessagesAsync(result[0], null, result[1]);
    //                twitter.getSentDirectMessagesAsync(result[0], null, null);
    //            }
    //
    //        }.executeTask();
    //    }
    //
    //    @Override
    //    public void onRefresh() {
    //        loadMoreMessages();
    //    }

    private fun sendDirectMessage() {
        val account = account ?: return
        val recipient = recipient ?: return
        val message = editText.text.toString()
        if (TextUtils.isEmpty(message)) {
            editText.error = getString(R.string.error_message_no_content)
        } else {
            twitterWrapper.sendDirectMessageAsync(account.key, recipient.key.id,
                    message, imageUri)
            editText.text = null
            imageUri = null
            updateAddImageButton()
        }
    }

    private fun setupEditQuery() {
        val queryEnterHandler = EditTextEnterHandler.attach(actionBarCustomView.editUserQuery, object : EnterListener {
            override fun shouldCallListener(): Boolean {
                val activity = activity as? BaseActivity ?: return false
                return activity.keyMetaState == 0
            }

            override fun onHitEnter(): Boolean {
                val activity = activity as? BaseActivity ?: return false
                if (activity.keyMetaState != 0) return false
                val account = actionBarCustomView.accountSpinner.selectedItem as AccountDetails
                editText.accountKey = account.key
                searchUsers(account.key, ParseUtils.parseString(actionBarCustomView.editUserQuery.text), false)
                return true
            }
        }, true)
        queryEnterHandler.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                val account = (actionBarCustomView.accountSpinner.selectedItem ?: return) as AccountDetails
                editText.accountKey = account.key
                searchUsers(account.key, ParseUtils.parseString(s), true)
            }
        })
        actionBarCustomView.editUserQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                queryTextChanged = s.isEmpty()
            }
        })
    }

    private fun setupEditText() {
        EditTextEnterHandler.attach(editText, object : EnterListener {
            override fun shouldCallListener(): Boolean {
                return true
            }

            override fun onHitEnter(): Boolean {
                sendDirectMessage()
                return true
            }
        }, preferences.getBoolean(SharedPreferenceConstants.KEY_QUICK_SEND, false))
        editText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                textChanged = s.isEmpty()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s == null) return
                sendMessage.isEnabled = validator.isValidDirectMessage(s.toString())
            }
        })
    }

    private fun updateActionBar() {
        val activity = activity as BaseActivity
        val actionBar = activity.supportActionBar ?: return
        actionBar.setDisplayOptions(if (recipient != null) ActionBar.DISPLAY_SHOW_TITLE else ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_TITLE or ActionBar.DISPLAY_SHOW_CUSTOM)
    }

    private fun updateAddImageButton() {
        addImage.isActivated = imageUri != null
    }

    //    @Override
    //    public boolean scrollToStart() {
    //        if (mAdapter == null || mAdapter.isEmpty()) return false;
    //        setSelection(mAdapter.getCount() - 1);
    //        return true;
    //    }

    private fun updateEmptyText() {
        val noQuery = actionBarCustomView.editUserQuery!!.length() <= 0
        if (noQuery) {
            usersSearchEmptyText!!.setText(R.string.type_name_to_search)
        } else {
            usersSearchEmptyText!!.setText(R.string.no_user_found)
        }
    }

    private fun updateRecipientInfo() {
        val activity = activity ?: return
        if (recipient != null) {
            activity.title = userColorNameManager.getDisplayName(recipient,
                    preferences.getBoolean(KEY_NAME_FIRST))
        } else {
            activity.setTitle(R.string.direct_messages)
        }
    }

    //    @Override
    //    protected void onReachedTop() {
    //        if (!mLoadMoreAutomatically) return;
    //        loadMoreMessages();
    //    }

    private fun updateRefreshState() {
        //        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        //        if (twitter == null || !getUserVisibleHint()) return;
        //        final boolean refreshing = twitter.isReceivedDirectMessagesRefreshing()
        //                || twitter.isSentDirectMessagesRefreshing();
        //        setRefreshing(refreshing);
    }

    //    private void loadMoreMessages() {
    //        if (isRefreshing()) return;
    //        new TwidereAsyncTask<Object, Object, long[][]>() {
    //
    //            @Override
    //            protected long[][] doInBackground(final Object... params) {
    //                final long[][] result = new long[3][];
    //                result[0] = getActivatedAccountIds(getActivity());
    //                result[1] = getOldestMessageIdsFromDatabase(getActivity(), DirectMessages.Inbox.CONTENT_URI);
    //                result[2] = getOldestMessageIdsFromDatabase(getActivity(), DirectMessages.Outbox.CONTENT_URI);
    //                return result;
    //            }
    //
    //            @Override
    //            protected void onPostExecute(final long[][] result) {
    //                final AsyncTwitterWrapper twitter = getTwitterWrapper();
    //                if (twitter == null) return;
    //                twitter.getReceivedDirectMessagesAsync(result[0], result[1], null);
    //                twitter.getSentDirectMessagesAsync(result[0], result[2], null);
    //            }
    //
    //        }.executeTask();
    //    }

    class DeleteConversationConfirmDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity)
            builder.setMessage(R.string.delete_conversation_confirm_message)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setNegativeButton(android.R.string.cancel, null)
            return builder.create()
        }


        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val args = arguments
                    val account: AccountDetails? = args.getParcelable(EXTRA_ACCOUNT)
                    val user: ParcelableUser? = args.getParcelable(EXTRA_USER)
                    val twitter = twitterWrapper
                    if (account == null || user == null) return
                    twitter.destroyMessageConversationAsync(account.key, user.key.id)
                }
            }
        }
    }


    class DeleteMessageConfirmDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity)
            builder.setMessage(R.string.delete_message_confirm_message)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setNegativeButton(android.R.string.cancel, null)
            return builder.create()
        }


        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val args = arguments
                    val account: AccountDetails? = args.getParcelable(EXTRA_ACCOUNT)
                    val message: ParcelableDirectMessage? = args.getParcelable(EXTRA_MESSAGE)
                    val twitter = twitterWrapper
                    if (account == null || message == null) return
                    twitter.destroyDirectMessageAsync(account.key, message.id)
                }
            }
        }
    }

    class SetReadStateTask(private val context: Context, private val account: AccountDetails, private val recipient: ParcelableUser) : AsyncTask<Any, Any, Cursor>() {

        @Inject
        lateinit var readStateManager: ReadStateManager

        init {
            GeneralComponentHelper.build(context).inject(this)
        }

        override fun doInBackground(vararg params: Any): Cursor {
            val resolver = context.contentResolver
            val projection = arrayOf(ConversationEntries.MESSAGE_ID)
            val selection = Expression.and(
                    Expression.equalsArgs(ConversationEntries.ACCOUNT_KEY),
                    Expression.equalsArgs(ConversationEntries.CONVERSATION_ID)).sql
            val selectionArgs = arrayOf(account.key.toString(), recipient.key.toString())
            val orderBy = OrderBy(ConversationEntries.MESSAGE_ID, false).sql
            return resolver.query(ConversationEntries.CONTENT_URI, projection, selection,
                    selectionArgs, orderBy)
        }

        override fun onPostExecute(cursor: Cursor) {
            if (cursor.moveToFirst()) {
                val messageIdIdx = cursor.getColumnIndex(ConversationEntries.MESSAGE_ID)
                val key = "${account.key}-${recipient.key}"
                readStateManager.setPosition(CustomTabType.DIRECT_MESSAGES, key, cursor.getLong(messageIdIdx), false)
            }
            cursor.close()
        }
    }

    internal class PanelShowHideListener(private val effectHelper: EffectViewHelper) : RecyclerView.OnScrollListener() {
        private var showElevation: Boolean = false

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val showElevation = layoutManager.findLastCompletelyVisibleItemPosition() < layoutManager.itemCount - 1
            if (showElevation != showElevation) {
                effectHelper.setState(showElevation)
            }
            this.showElevation = showElevation
        }

        fun reset() {
            effectHelper.resetState(showElevation)
        }
    }

    internal class PanelElevationProperty(private val elevation: Float) : Property<View, Float>(java.lang.Float.TYPE, null) {

        override fun set(obj: View, value: Float?) {
            ViewCompat.setTranslationZ(obj, elevation * value!!)
        }

        override fun get(obj: View): Float? {
            return ViewCompat.getTranslationZ(obj) / elevation
        }
    }

    companion object {

        // Constants
        private val LOADER_ID_SEARCH_USERS = 1

    }
}
