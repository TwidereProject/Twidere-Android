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

package org.mariotaku.twidere.activity

import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.graphics.PorterDuff.Mode
import android.location.*
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Parcelable
import android.provider.BaseColumns
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.view.SupportMenuInflater
import android.support.v7.widget.ActionMenuView.OnMenuItemClickListener
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.*
import android.text.style.ImageSpan
import android.text.style.MetricAffectingSpan
import android.text.style.SuggestionSpan
import android.text.style.UpdateAppearance
import android.util.Log
import android.view.*
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.twitter.Extractor
import com.twitter.Validator
import kotlinx.android.synthetic.main.activity_compose.*
import org.apache.commons.lang3.ArrayUtils
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.pickncrop.library.MediaPickerActivity
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.adapter.BaseRecyclerViewAdapter
import org.mariotaku.twidere.adapter.MediaPreviewAdapter
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_SCREEN_NAME
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.extension.model.getAccountUser
import org.mariotaku.twidere.extension.model.textLimit
import org.mariotaku.twidere.extension.model.unique_id_non_null
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.EditAltTextDialogFragment
import org.mariotaku.twidere.fragment.PermissionRequestDialog
import org.mariotaku.twidere.fragment.PermissionRequestDialog.PermissionRequestCancelCallback
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtras
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableLocationUtils
import org.mariotaku.twidere.preference.ServicePickerPreference
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.service.LengthyOperationsService
import org.mariotaku.twidere.task.compose.AbsAddMediaTask
import org.mariotaku.twidere.task.compose.AbsDeleteMediaTask
import org.mariotaku.twidere.text.MarkForDeleteSpan
import org.mariotaku.twidere.text.style.EmojiSpan
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.EditTextEnterHandler.EnterListener
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.view.CheckableLinearLayout
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.ShapedImageView
import org.mariotaku.twidere.view.helper.SimpleItemTouchHelperCallback
import org.mariotaku.twidere.view.holder.compose.MediaPreviewViewHolder
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject
import android.Manifest.permission as AndroidPermission

class ComposeActivity : BaseActivity(), OnMenuItemClickListener, OnClickListener, OnLongClickListener,
        ActionMode.Callback, PermissionRequestCancelCallback, EditAltTextDialogFragment.EditAltTextCallback {

    // Utility classes
    @Inject
    lateinit var extractor: Extractor
    @Inject
    lateinit var validator: Validator
    @Inject
    lateinit var locationManager: LocationManager

    private lateinit var itemTouchHelper: ItemTouchHelper
    private val supportMenuInflater by lazy { SupportMenuInflater(this) }
    private var currentTask: AsyncTask<Any, Any, *>? = null

    private val backTimeoutRunnable = Runnable { navigateBackPressed = false }

    // Adapters
    private lateinit var mediaPreviewAdapter: MediaPreviewAdapter
    private lateinit var accountsAdapter: AccountIconsAdapter

    // Data fields
    private var recentLocation: ParcelableLocation? = null
    private var inReplyToStatus: ParcelableStatus? = null
    private var mentionUser: ParcelableUser? = null
    private var originalText: String? = null
    private var possiblySensitive: Boolean = false
    private var shouldSaveAccounts: Boolean = false
    private var imageUploaderUsed: Boolean = false
    private var statusShortenerUsed: Boolean = false
    private var navigateBackPressed: Boolean = false
    private var textChanged: Boolean = false
    private var composeKeyMetaState: Int = 0
    private var draft: Draft? = null
    private var nameFirst: Boolean = false
    private var draftUniqueId: String? = null
    private var shouldSkipDraft: Boolean = false

    // Listeners
    private var locationListener: LocationListener? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            REQUEST_TAKE_PHOTO, REQUEST_PICK_MEDIA -> {
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    val src = MediaPickerActivity.getMediaUris(intent)
                    TaskStarter.execute(AddMediaTask(this, src, false, false))
                }
            }
            REQUEST_EDIT_IMAGE -> {
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    if (intent.data != null) {
                        setMenu()
                        updateTextCount()
                    }
                }
            }
            REQUEST_EXTENSION_COMPOSE -> {
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    val text = intent.getStringExtra(EXTRA_TEXT)
                    val append = intent.getStringExtra(EXTRA_APPEND_TEXT)
                    val imageUri = intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI)
                    if (text != null) {
                        editText.setText(text)
                    } else if (append != null) {
                        editText.append(append)
                    }
                    if (imageUri != null) {
                    }
                    setMenu()
                    updateTextCount()
                }
            }
        }

    }


    override fun onBackPressed() {
        if (currentTask?.status == AsyncTask.Status.RUNNING) return
        if (!shouldSkipDraft && hasComposingStatus()) {
            saveToDrafts()
            Toast.makeText(this, R.string.message_toast_status_saved_to_draft, Toast.LENGTH_SHORT).show()
            shouldSkipDraft = true
            finish()
        } else {
            shouldSkipDraft = true
            discardTweet()
        }
    }

    override fun onDestroy() {
        if (!shouldSkipDraft && hasComposingStatus() && isFinishing) {
            saveToDrafts()
            Toast.makeText(this, R.string.message_toast_status_saved_to_draft, Toast.LENGTH_SHORT).show()
        }
        super.onDestroy()
    }

    private fun discardTweet() {
        if (isFinishing || currentTask?.status == AsyncTask.Status.RUNNING) return
        currentTask = AsyncTaskUtils.executeTask(DiscardTweetTask(this))
    }

    private fun hasComposingStatus(): Boolean {
        val text = if (editText != null) ParseUtils.parseString(editText.text) else null
        val textChanged = text != null && !text.isEmpty() && text != originalText
        val isEditingDraft = INTENT_ACTION_EDIT_DRAFT == intent.action
        return textChanged || hasMedia() || isEditingDraft
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArray(EXTRA_ACCOUNT_KEYS, accountsAdapter.selectedAccountKeys)
        outState.putParcelableArrayList(EXTRA_MEDIA, ArrayList<Parcelable>(mediaList))
        outState.putBoolean(EXTRA_IS_POSSIBLY_SENSITIVE, possiblySensitive)
        outState.putParcelable(EXTRA_STATUS, inReplyToStatus)
        outState.putParcelable(EXTRA_USER, mentionUser)
        outState.putParcelable(EXTRA_DRAFT, draft)
        outState.putBoolean(EXTRA_SHOULD_SAVE_ACCOUNTS, shouldSaveAccounts)
        outState.putString(EXTRA_ORIGINAL_TEXT, originalText)
        outState.putString(EXTRA_DRAFT_UNIQUE_ID, draftUniqueId)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()

        imageUploaderUsed = !ServicePickerPreference.isNoneValue(kPreferences[mediaUploaderKey])
        statusShortenerUsed = !ServicePickerPreference.isNoneValue(kPreferences[statusShortenerKey])
        if (kPreferences[attachLocationKey]) {
            if (checkAnySelfPermissionsGranted(AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION)) {
                try {
                    startLocationUpdateIfEnabled()
                } catch (e: SecurityException) {
                    locationSwitch.checkedPosition = LOCATION_OPTIONS.indexOf(LOCATION_VALUE_NONE)
                }
            } else {
                locationSwitch.checkedPosition = LOCATION_OPTIONS.indexOf(LOCATION_VALUE_NONE)
            }
        }
        setMenu()
        updateTextCount()
        val textSize = preferences[textSizeKey]
        editText.textSize = textSize * 1.25f
    }

    override fun onStop() {
        saveAccountSelection()
        try {
            if (locationListener != null) {
                locationManager.removeUpdates(locationListener)
                locationListener = null
            }
        } catch (ignore: SecurityException) {
            // That should not happen
        }

        super.onStop()
    }

    override fun onClick(view: View) {
        when (view) {
            updateStatus -> {
                confirmAndUpdateStatus()
            }
            accountSelectorContainer -> {
                isAccountSelectorVisible = false
            }
            accountSelectorButton -> {
                isAccountSelectorVisible = !isAccountSelectorVisible
            }
            replyLabel -> {
                if (replyLabel.visibility != View.VISIBLE) return
                replyLabel.setSingleLine(replyLabel.lineCount > 1)
            }
        }
    }

    private var isAccountSelectorVisible: Boolean
        get() = accountSelectorContainer.visibility == View.VISIBLE
        set(visible) {
            accountSelectorContainer.visibility = if (visible) View.VISIBLE else View.GONE
        }

    private fun confirmAndUpdateStatus() {
        val matchResult = Regex("[DM] +([a-z0-9_]{1,20}) +[^ ]+").matchEntire(editText.text)
        if (matchResult != null) {
            val screenName = matchResult.groupValues[1]
            val df = DirectMessageConfirmFragment()
            df.arguments = Bundle {
                this[EXTRA_SCREEN_NAME] = screenName
            }
            df.show(supportFragmentManager, "send_direct_message_confirm")
        } else if (isQuotingProtectedStatus) {
            val df = RetweetProtectedStatusWarnFragment()
            df.show(supportFragmentManager,
                    "retweet_protected_status_warning_message")
        } else {
            updateStatus()
        }
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        val window = window
        val contentView = window.findViewById(android.R.id.content)
        contentView.setPadding(contentView.paddingLeft, 0,
                contentView.paddingRight, contentView.paddingBottom)
    }

    override fun onLongClick(v: View): Boolean {
        when (v) {
            updateStatus -> {
                Utils.showMenuItemToast(v, getString(R.string.action_send), true)
                return true
            }
        }
        return false
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.take_photo -> {
                requestOrOpenCamera()
            }
            R.id.add_image, R.id.add_image_sub_item -> {
                requestOrPickMedia()
            }
            R.id.drafts -> {
                IntentUtils.openDrafts(this)
            }
            R.id.delete -> {
                TaskStarter.execute(DeleteMediaTask(this, media))
            }
            R.id.toggle_sensitive -> {
                if (!hasMedia()) return false
                possiblySensitive = !possiblySensitive
                setMenu()
                updateTextCount()
            }
            else -> {
                val intent = item.intent
                if (intent != null) {
                    try {
                        val action = intent.action
                        if (INTENT_ACTION_EXTENSION_COMPOSE == action) {
                            val accountKeys = accountsAdapter.selectedAccountKeys
                            intent.putExtra(EXTRA_TEXT, ParseUtils.parseString(editText.text))
                            intent.putExtra(EXTRA_ACCOUNT_KEYS, accountKeys)
                            if (accountKeys.isNotEmpty()) {
                                val accountKey = accountKeys.first()
                                intent.putExtra(EXTRA_NAME, DataStoreUtils.getAccountName(this, accountKey))
                                intent.putExtra(EXTRA_SCREEN_NAME, DataStoreUtils.getAccountScreenName(this, accountKey))
                            }
                            inReplyToStatus?.let {
                                intent.putExtra(EXTRA_IN_REPLY_TO_ID, it.id)
                                intent.putExtra(EXTRA_IN_REPLY_TO_NAME, it.user_name)
                                intent.putExtra(EXTRA_IN_REPLY_TO_SCREEN_NAME, it.user_screen_name)
                            }
                            startActivityForResult(intent, REQUEST_EXTENSION_COMPOSE)
                        } else {
                            startActivity(intent)
                        }
                    } catch (e: ActivityNotFoundException) {
                        Log.w(LOGTAG, e)
                        return false
                    }

                }
            }
        }
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.rawX
                val y = ev.rawY
                if (isAccountSelectorVisible && !TwidereViewUtils.hitView(x, y, accountSelectorButton)) {
                    var clickedItem = false
                    val layoutManager = accountSelector.layoutManager
                    for (i in 0..layoutManager.childCount - 1) {
                        if (TwidereViewUtils.hitView(x, y, layoutManager.getChildAt(i))) {
                            clickedItem = true
                            break
                        }
                    }
                    if (!clickedItem) {
                        isAccountSelectorVisible = false
                        return true
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.rawX
            val y = event.rawY
            val window = window
            if (!TwidereViewUtils.hitView(x, y, window.decorView)
                    && window.peekDecorView() != null && !hasComposingStatus()) {
                onBackPressed()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun getMenuInflater(): MenuInflater {
        return supportMenuInflater
    }

    fun removeAllMedia(list: List<ParcelableMediaUpdate>) {
        mediaPreviewAdapter.removeAll(list)
    }

    fun saveToDrafts(): Uri {
        val text = editText.text.toString()
        val draft = Draft()
        draft.unique_id = this.draftUniqueId ?: UUID.randomUUID().toString()
        draft.action_type = getDraftAction(intent.action)
        draft.account_keys = accountsAdapter.selectedAccountKeys
        draft.text = text
        draft.media = media
        draft.location = recentLocation
        draft.timestamp = System.currentTimeMillis()
        draft.action_extras = UpdateStatusActionExtras().apply {
            this.inReplyToStatus = this@ComposeActivity.inReplyToStatus
            this.isPossiblySensitive = this@ComposeActivity.possiblySensitive
        }
        val values = ObjectCursor.valuesCreatorFrom(Draft::class.java).create(draft)
        val draftUri = contentResolver.insert(Drafts.CONTENT_URI, values)
        displayNewDraftNotification(text, draftUri)
        return draftUri
    }

    fun setSelectedAccounts(vararg accounts: AccountDetails) {
        if (accounts.size == 1) {
            accountsCount.setText(null)
            val account = accounts[0]
            val profileImageStyle = preferences[profileImageStyleKey]
            Glide.with(this).loadProfileImage(this, account, profileImageStyle).into(accountProfileImage)
            accountProfileImage.setBorderColor(account.color)
        } else {
            accountsCount.setText(accounts.size.toString())
            //TODO cancel image load
            accountProfileImage.setImageDrawable(null)
            accountProfileImage.setBorderColors(*Utils.getAccountColors(accounts))
        }
    }

    override fun onActionModeStarted(mode: ActionMode) {
        super.onActionModeStarted(mode)
        ThemeUtils.applyColorFilterToMenuIcon(mode.menu, ThemeUtils.getContrastActionBarItemColor(this),
                0, 0, Mode.MULTIPLY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeneralComponentHelper.build(this).inject(this)
        nameFirst = preferences[nameFirstKey]
        setContentView(R.layout.activity_compose)

        mediaPreviewAdapter = MediaPreviewAdapter(this, Glide.with(this))
        mediaPreviewAdapter.listener = object : MediaPreviewAdapter.Listener {
            override fun onEditClick(position: Int, holder: MediaPreviewViewHolder) {
                attachedMediaPreview.showContextMenuForChild(holder.itemView)
            }

            override fun onRemoveClick(position: Int, holder: MediaPreviewViewHolder) {
                mediaPreviewAdapter.remove(position)
                updateAttachedMediaView()
            }

            override fun onStartDrag(viewHolder: ViewHolder) {
                itemTouchHelper.startDrag(viewHolder)
            }

        }
        itemTouchHelper = ItemTouchHelper(AttachedMediaItemTouchHelperCallback(mediaPreviewAdapter.touchAdapter))

        setFinishOnTouchOutside(false)
        val am = AccountManager.get(this)
        val accounts = AccountUtils.getAccounts(am)
        if (accounts.isEmpty()) {
            Toast.makeText(this, R.string.message_toast_no_account, Toast.LENGTH_SHORT).show()
            shouldSkipDraft = true
            finish()
            return
        }
        val accountDetails = AccountUtils.getAllAccountDetails(am, accounts, true)
        val defaultAccountIds = accountDetails.map(AccountDetails::key).toTypedArray()
        menuBar.setOnMenuItemClickListener(this)
        setupEditText()
        accountSelectorContainer.setOnClickListener(this)
        accountSelectorButton.setOnClickListener(this)
        replyLabel.setOnClickListener(this)
        locationSwitch.max = LOCATION_OPTIONS.size
        val attachLocation = kPreferences[attachLocationKey]
        val attachPreciseLocation = kPreferences[attachPreciseLocationKey]
        if (attachLocation) {
            if (attachPreciseLocation) {
                locationSwitch.checkedPosition = LOCATION_OPTIONS.indexOf(LOCATION_VALUE_COORDINATE)
            } else {
                locationSwitch.checkedPosition = LOCATION_OPTIONS.indexOf(LOCATION_VALUE_PLACE)
            }
        } else {
            locationSwitch.checkedPosition = LOCATION_OPTIONS.indexOf(LOCATION_VALUE_NONE)
        }
        locationSwitch.setOnCheckedChangeListener {
            val value = LOCATION_OPTIONS[locationSwitch.checkedPosition]
            var attachLocationChecked = false
            var attachPreciseLocationChecked = false
            when (value) {
                LOCATION_VALUE_COORDINATE -> {
                    attachLocationChecked = true
                    attachPreciseLocationChecked = true
                    locationText.tag = null
                }
                LOCATION_VALUE_PLACE -> {
                    attachLocationChecked = true
                    attachPreciseLocationChecked = false
                }
            }
            kPreferences.edit {
                this[attachLocationKey] = attachLocationChecked
                this[attachPreciseLocationKey] = attachPreciseLocationChecked
            }
            if (attachLocationChecked) {
                requestOrUpdateLocation()
            } else if (locationListener != null) {
                try {
                    locationManager.removeUpdates(locationListener)
                    locationListener = null
                } catch (e: SecurityException) {
                    //Ignore
                }

            }
            updateLocationState()
            setMenu()
            updateTextCount()
        }

        accountSelector.layoutManager = FixedLinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.VERTICAL
            stackFromEnd = true
        }
        accountSelector.itemAnimator = DefaultItemAnimator()
        accountsAdapter = AccountIconsAdapter(this).apply {
            setAccounts(accountDetails)
        }
        accountSelector.adapter = accountsAdapter


        attachedMediaPreview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        attachedMediaPreview.adapter = mediaPreviewAdapter
        registerForContextMenu(attachedMediaPreview)
        itemTouchHelper.attachToRecyclerView(attachedMediaPreview)
        attachedMediaPreview.addItemDecoration(PreviewGridItemDecoration(resources.getDimensionPixelSize(R.dimen.element_spacing_small)))

        if (savedInstanceState != null) {
            // Restore from previous saved state
            val selected = savedInstanceState.getTypedArray(EXTRA_ACCOUNT_KEYS, UserKey.CREATOR)
            accountsAdapter.setSelectedAccountIds(*selected)
            possiblySensitive = savedInstanceState.getBoolean(EXTRA_IS_POSSIBLY_SENSITIVE)
            val mediaList = savedInstanceState.getParcelableArrayList<ParcelableMediaUpdate>(EXTRA_MEDIA)
            if (mediaList != null) {
                addMedia(mediaList)
            }
            inReplyToStatus = savedInstanceState.getParcelable(EXTRA_STATUS)
            mentionUser = savedInstanceState.getParcelable(EXTRA_USER)
            draft = savedInstanceState.getParcelable(EXTRA_DRAFT)
            shouldSaveAccounts = savedInstanceState.getBoolean(EXTRA_SHOULD_SAVE_ACCOUNTS)
            originalText = savedInstanceState.getString(EXTRA_ORIGINAL_TEXT)
            draftUniqueId = savedInstanceState.getString(EXTRA_DRAFT_UNIQUE_ID)
            setLabel(intent)
        } else {
            // The context was first created
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
            val notificationAccount = intent.getParcelableExtra<UserKey>(EXTRA_NOTIFICATION_ACCOUNT)
            if (notificationId != -1) {
                twitterWrapper.clearNotificationAsync(notificationId, notificationAccount)
            }
            if (!handleIntent(intent)) {
                handleDefaultIntent(intent)
            }
            setLabel(intent)
            val selectedAccountIds = accountsAdapter.selectedAccountKeys
            if (ArrayUtils.isEmpty(selectedAccountIds)) {
                val idsInPrefs: Array<UserKey> = kPreferences[composeAccountsKey] ?: emptyArray()
                val intersection: Array<UserKey> = defaultAccountIds.intersect(listOf(*idsInPrefs)).toTypedArray()

                if (intersection.isEmpty()) {
                    accountsAdapter.setSelectedAccountIds(*defaultAccountIds)
                } else {
                    accountsAdapter.setSelectedAccountIds(*intersection)
                }
            }
            originalText = ParseUtils.parseString(editText.text)
        }

        val menu = menuBar.menu
        supportMenuInflater.inflate(R.menu.menu_compose, menu)
        ThemeUtils.wrapMenuIcon(menuBar)

        updateStatus.setOnClickListener(this)
        updateStatus.setOnLongClickListener(this)
        val composeExtensionsIntent = Intent(INTENT_ACTION_EXTENSION_COMPOSE)
        MenuUtils.addIntentToMenu(this, menu, composeExtensionsIntent, MENU_GROUP_COMPOSE_EXTENSION)
        val imageExtensionsIntent = Intent(INTENT_ACTION_EXTENSION_EDIT_IMAGE)
        val mediaMenuItem = menu.findItem(R.id.media_menu)
        if (mediaMenuItem != null && mediaMenuItem.hasSubMenu()) {
            MenuUtils.addIntentToMenu(this, mediaMenuItem.subMenu, imageExtensionsIntent, MENU_GROUP_IMAGE_EXTENSION)
        }
        updateViewStyle()
        setMenu()
        updateLocationState()
        notifyAccountSelectionChanged()

        textChanged = false

        updateAttachedMediaView()
    }

    private fun updateViewStyle() {
        accountProfileImage.style = preferences[profileImageStyleKey]
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        if (KeyEvent.isModifierKey(keyCode)) {
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {
                composeKeyMetaState = composeKeyMetaState or KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode)
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                composeKeyMetaState = composeKeyMetaState and KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode).inv()
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        if (menuInfo !is ExtendedRecyclerView.ContextMenuInfo) return
        when (menuInfo.recyclerViewId) {
            R.id.attachedMediaPreview -> {
                menu.setHeaderTitle(R.string.edit_media)
                supportMenuInflater.inflate(R.menu.menu_attached_media_edit, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuInfo = item.menuInfo as? ExtendedRecyclerView.ContextMenuInfo ?: run {
            return super.onContextItemSelected(item)
        }
        when (menuInfo.recyclerViewId) {
            R.id.attachedMediaPreview -> {
                when (item.itemId) {
                    R.id.edit_description -> {
                        val position = menuInfo.position
                        val altText = mediaPreviewAdapter.getItem(position).alt_text
                        executeAfterFragmentResumed { activity ->
                            EditAltTextDialogFragment.show(activity.supportFragmentManager, position,
                                    altText)
                        }
                    }
                }
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun setupEditText() {
        val sendByEnter = preferences.getBoolean(KEY_QUICK_SEND)
        EditTextEnterHandler.attach(editText, ComposeEnterListener(this), sendByEnter)
        editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setMenu()
                updateTextCount()
                if (s is Spannable && count == 1 && before == 0) {
                    val imageSpans = s.getSpans(start, start + count, ImageSpan::class.java)
                    val imageSources = ArrayList<String>()
                    for (imageSpan in imageSpans) {
                        imageSources.add(imageSpan.source)
                        s.setSpan(MarkForDeleteSpan(), start, start + count,
                                Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                    }
                    if (!imageSources.isEmpty()) {
                        val intent = ThemedMediaPickerActivity.withThemed(this@ComposeActivity)
                                .getMedia(Uri.parse(imageSources[0]))
                                .build()
                        startActivityForResult(intent, REQUEST_PICK_MEDIA)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                textChanged = s.isEmpty()
                val deletes = s.getSpans(0, s.length, MarkForDeleteSpan::class.java)
                for (delete in deletes) {
                    s.delete(s.getSpanStart(delete), s.getSpanEnd(delete))
                    s.removeSpan(delete)
                }
                for (span in s.getSpans(0, s.length, UpdateAppearance::class.java)) {
                    trimSpans(s, span)
                }
            }

            private fun trimSpans(s: Editable, span: Any) {
                if (span is EmojiSpan) return
                if (span is SuggestionSpan) return
                if (span is MetricAffectingSpan) {
                    s.removeSpan(span)
                }
            }
        })
        editText.customSelectionActionModeCallback = this
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val action = handler.getKeyAction(KeyboardShortcutConstants.CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (KeyboardShortcutConstants.ACTION_NAVIGATION_BACK == action) {
            if (editText.length() == 0 && !textChanged) {
                if (!navigateBackPressed) {
                    Toast.makeText(this, getString(R.string.message_toast_press_again_to_close), Toast.LENGTH_SHORT).show()
                    editText.removeCallbacks(backTimeoutRunnable)
                    editText.postDelayed(backTimeoutRunnable, 2000)
                } else {
                    onBackPressed()
                }
                navigateBackPressed = true
            } else {
                textChanged = false
            }
            return true
        }
        return super.handleKeyboardShortcutSingle(handler, keyCode, event, metaState)
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int,
            repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        return super.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
    }

    private fun addMedia(media: List<ParcelableMediaUpdate>) {
        mediaPreviewAdapter.addAll(media)
        updateAttachedMediaView()
    }

    private fun clearMedia() {
        mediaPreviewAdapter.clear()
        updateAttachedMediaView()
    }

    private fun updateAttachedMediaView() {
        val hasMedia = hasMedia()
        attachedMediaPreview.visibility = if (hasMedia) View.VISIBLE else View.GONE
        if (hasMedia) {
            editText.minLines = resources.getInteger(R.integer.media_compose_min_lines)
        } else {
            editText.minLines = resources.getInteger(R.integer.default_compose_min_lines)
        }
        setMenu()
    }

    private fun displayNewDraftNotification(text: String, draftUri: Uri) {
        val values = ContentValues()
        values.put(BaseColumns._ID, draftUri.lastPathSegment)
        contentResolver.insert(Drafts.CONTENT_URI_NOTIFICATIONS, values)
    }

    private val media: Array<ParcelableMediaUpdate>
        get() = mediaList.toTypedArray()

    private val mediaList: List<ParcelableMediaUpdate>
        get() = mediaPreviewAdapter.asList()

    private fun handleDefaultIntent(intent: Intent?): Boolean {
        if (intent == null) return false
        val action = intent.action
        val hasAccountIds: Boolean
        if (intent.hasExtra(EXTRA_ACCOUNT_KEYS)) {
            val accountKeys = intent.getParcelableArrayExtra(EXTRA_ACCOUNT_KEYS).toTypedArray(UserKey.CREATOR)
            accountsAdapter.setSelectedAccountIds(*accountKeys)
            hasAccountIds = true
        } else if (intent.hasExtra(EXTRA_ACCOUNT_KEY)) {
            val accountKey = intent.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)
            accountsAdapter.setSelectedAccountIds(accountKey)
            hasAccountIds = true
        } else {
            hasAccountIds = false
        }
        if (Intent.ACTION_SEND == action) {
            shouldSaveAccounts = false
            val stream = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (stream != null) {
                val src = arrayOf(stream)
                TaskStarter.execute(AddMediaTask(this, src, true, false))
            }
        } else if (Intent.ACTION_SEND_MULTIPLE == action) {
            shouldSaveAccounts = false
            val extraStream = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            if (extraStream != null) {
                val src = extraStream.toTypedArray()
                TaskStarter.execute(AddMediaTask(this, src, true, false))
            }
        } else {
            shouldSaveAccounts = !hasAccountIds
            val data = intent.data
            if (data != null) {
                val src = arrayOf(data)
                TaskStarter.execute(AddMediaTask(this, src, true, false))
            }
        }
        val extraSubject = intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT)
        val extraText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
        editText.setText(Utils.getShareStatus(this, extraSubject, extraText))
        val selectionEnd = editText.length()
        editText.setSelection(selectionEnd)
        return true
    }

    private fun handleEditDraftIntent(draft: Draft?): Boolean {
        if (draft == null) return false
        draftUniqueId = draft.unique_id_non_null
        editText.setText(draft.text)
        val selectionEnd = editText.length()
        editText.setSelection(selectionEnd)
        accountsAdapter.setSelectedAccountIds(*draft.account_keys ?: emptyArray())
        if (draft.media != null) {
            addMedia(Arrays.asList(*draft.media))
        }
        recentLocation = draft.location
        (draft.action_extras as? UpdateStatusActionExtras)?.let {
            possiblySensitive = it.isPossiblySensitive
            inReplyToStatus = it.inReplyToStatus
        }
        val tag = Uri.withAppendedPath(Drafts.CONTENT_URI, draft._id.toString()).toString()
        notificationManager.cancel(tag, TwidereConstants.NOTIFICATION_ID_DRAFTS)
        return true
    }

    private fun setLabel(intent: Intent): Boolean {
        val action = intent.action
        if (action == null) {
            hideLabel()
            return false
        }
        when (action) {
            INTENT_ACTION_REPLY -> {
                showReplyLabel(intent.getParcelableExtra<ParcelableStatus>(EXTRA_STATUS))
                return true
            }
            INTENT_ACTION_QUOTE -> {
                showQuoteLabel(intent.getParcelableExtra<ParcelableStatus>(EXTRA_STATUS))
                return true
            }
            INTENT_ACTION_EDIT_DRAFT -> {
                val draft = intent.getParcelableExtra<Draft>(EXTRA_DRAFT)
                if (draft == null) {
                    hideLabel()
                    return false
                }
                if (draft.action_type == null) {
                    draft.action_type = Draft.Action.UPDATE_STATUS
                }
                when (draft.action_type) {
                    Draft.Action.REPLY -> {
                        if (draft.action_extras is UpdateStatusActionExtras) {
                            showReplyLabel((draft.action_extras as UpdateStatusActionExtras).inReplyToStatus)
                        } else {
                            hideLabel()
                            return false
                        }
                    }
                    Draft.Action.QUOTE -> {
                        if (draft.action_extras is UpdateStatusActionExtras) {
                            showQuoteLabel((draft.action_extras as UpdateStatusActionExtras).inReplyToStatus)
                        } else {
                            hideLabel()
                            return false
                        }
                    }
                    else -> {
                        hideLabel()
                        return false
                    }
                }
                return true
            }
        }
        hideLabel()
        return false
    }

    private fun handleIntent(intent: Intent): Boolean {
        val action = intent.action ?: return false
        shouldSaveAccounts = false
        mentionUser = intent.getParcelableExtra(EXTRA_USER)
        inReplyToStatus = intent.getParcelableExtra(EXTRA_STATUS)
        when (action) {
            INTENT_ACTION_REPLY -> {
                return handleReplyIntent(inReplyToStatus)
            }
            INTENT_ACTION_QUOTE -> {
                return handleQuoteIntent(inReplyToStatus)
            }
            INTENT_ACTION_EDIT_DRAFT -> {
                draft = intent.getParcelableExtra(EXTRA_DRAFT)
                return handleEditDraftIntent(draft)
            }
            INTENT_ACTION_MENTION -> {
                return handleMentionIntent(mentionUser)
            }
            INTENT_ACTION_REPLY_MULTIPLE -> {
                val screenNames = intent.getStringArrayExtra(EXTRA_SCREEN_NAMES)
                val accountKey = intent.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEYS)
                val inReplyToStatus = intent.getParcelableExtra<ParcelableStatus>(EXTRA_IN_REPLY_TO_STATUS)
                return handleReplyMultipleIntent(screenNames, accountKey, inReplyToStatus)
            }
            INTENT_ACTION_COMPOSE_TAKE_PHOTO -> {
                requestOrOpenCamera()
                return true
            }
            INTENT_ACTION_COMPOSE_PICK_IMAGE -> {
                requestOrPickMedia()
                return true
            }
        }
        // Unknown action or no intent extras
        return false
    }

    private fun handleMentionIntent(user: ParcelableUser?): Boolean {
        if (user == null || user.key == null) return false
        val accountScreenName = DataStoreUtils.getAccountScreenName(this, user.account_key)
        if (TextUtils.isEmpty(accountScreenName)) return false
        editText.setText(String.format("@%s ", user.screen_name))
        val selection_end = editText.length()
        editText.setSelection(selection_end)
        accountsAdapter.setSelectedAccountIds(user.account_key)
        return true
    }

    private fun handleQuoteIntent(status: ParcelableStatus?): Boolean {
        if (status == null) return false
        editText.setText(Utils.getQuoteStatus(this, status))
        editText.setSelection(0)
        accountsAdapter.setSelectedAccountIds(status.account_key)
        showQuoteLabel(status)
        return true
    }

    private fun showQuoteLabel(status: ParcelableStatus?) {
        if (status == null) {
            hideLabel()
            return
        }
        val replyToName = userColorNameManager.getDisplayName(status, nameFirst)
        replyLabel.text = getString(R.string.quote_name_text, replyToName, status.text_unescaped)
        replyLabel.visibility = View.VISIBLE
        replyLabelDivider.visibility = View.VISIBLE
    }

    private fun showReplyLabel(status: ParcelableStatus?) {
        if (status == null) {
            hideLabel()
            return
        }
        val replyToName = userColorNameManager.getDisplayName(status, nameFirst)
        replyLabel.text = getString(R.string.reply_to_name_text, replyToName, status.text_unescaped)
        replyLabel.visibility = View.VISIBLE
        replyLabelDivider.visibility = View.VISIBLE
    }

    private fun hideLabel() {
        replyLabel.visibility = View.GONE
        replyLabelDivider.visibility = View.GONE
    }

    private fun handleReplyIntent(status: ParcelableStatus?): Boolean {
        if (status == null) return false
        val am = AccountManager.get(this)
        val accountUser = AccountUtils.findByAccountKey(am, status.account_key)?.getAccountUser(am) ?: return false
        var selectionStart = 0
        val mentions = TreeSet(String.CASE_INSENSITIVE_ORDER)
        // If replying status from current user, just exclude it's screen name from selection.
        if (accountUser.key != status.user_key) {
            editText.append("@${status.user_screen_name} ")
            selectionStart = editText.length()
        }
        if (status.is_retweet && !TextUtils.isEmpty(status.retweeted_by_user_screen_name)) {
            mentions.add(status.retweeted_by_user_screen_name)
        }
        if (status.is_quote && !TextUtils.isEmpty(status.quoted_user_screen_name)) {
            mentions.add(status.quoted_user_screen_name)
        }
        if (!ArrayUtils.isEmpty(status.mentions)) {
            status.mentions
                    .filterNot { it.key == status.account_key || it.screen_name.isNullOrEmpty() }
                    .mapTo(mentions) { it.screen_name }
            mentions.addAll(extractor.extractMentionedScreennames(status.quoted_text_plain))
        } else if (USER_TYPE_FANFOU_COM == status.account_key.host) {
            addFanfouHtmlToMentions(status.text_unescaped, status.spans, mentions)
            if (status.is_quote) {
                addFanfouHtmlToMentions(status.quoted_text_unescaped, status.quoted_spans, mentions)
            }
        } else {
            mentions.addAll(extractor.extractMentionedScreennames(status.text_plain))
            if (status.is_quote) {
                mentions.addAll(extractor.extractMentionedScreennames(status.quoted_text_plain))
            }
        }

        mentions.filterNot {
            it.equals(status.user_screen_name, ignoreCase = true)
                    || it.equals(accountUser.screen_name, ignoreCase = true)
        }.forEach { editText.append("@$it ") }

        // Put current user mention at last
        if (accountUser.key == status.user_key) {
            selectionStart = editText.length()
            editText.append("@${status.user_screen_name} ")
        }

        val selectionEnd = editText.length()
        editText.setSelection(selectionStart, selectionEnd)
        accountsAdapter.setSelectedAccountIds(status.account_key)
        showReplyLabel(status)
        return true
    }

    private fun addFanfouHtmlToMentions(text: String, spans: Array<SpanItem>?, mentions: MutableCollection<String>) {
        if (spans == null) return
        for (span in spans) {
            val start = span.start
            val end = span.end
            if (start <= 0 || end > text.length || start > end) continue
            val ch = text[start - 1]
            if (ch == '@' || ch == '\uff20') {
                mentions.add(text.substring(start, end))
            }
        }
    }

    private fun handleReplyMultipleIntent(screenNames: Array<String>?, accountId: UserKey?,
            inReplyToStatus: ParcelableStatus): Boolean {
        if (screenNames == null || screenNames.isEmpty() || accountId == null) return false
        val myScreenName = DataStoreUtils.getAccountScreenName(this, accountId)
        if (TextUtils.isEmpty(myScreenName)) return false
        screenNames.filterNot { it.equals(myScreenName, ignoreCase = true) }
                .forEach { editText.append("@$it ") }
        editText.setSelection(editText.length())
        accountsAdapter.setSelectedAccountIds(accountId)
        this.inReplyToStatus = inReplyToStatus
        return true
    }

    private fun hasMedia(): Boolean {
        return mediaPreviewAdapter.itemCount > 0
    }

    private val isQuote: Boolean
        get() = INTENT_ACTION_QUOTE == intent.action

    private val isQuotingProtectedStatus: Boolean
        get() {
            val status = inReplyToStatus
            if (!isQuote || status == null) return false
            return status.user_is_protected && status.account_key != status.user_key
        }

    private fun noReplyContent(text: String?): Boolean {
        if (text == null) return true
        val action = intent.action
        val is_reply = INTENT_ACTION_REPLY == action || INTENT_ACTION_REPLY_MULTIPLE == action
        return is_reply && text == originalText
    }

    private fun notifyAccountSelectionChanged() {
        val accounts = accountsAdapter.selectedAccounts
        setSelectedAccounts(*accounts)
        if (ArrayUtils.isEmpty(accounts)) {
            editText.accountKey = Utils.getDefaultAccountKey(this)
        } else {
            editText.accountKey = accounts[0].key
        }
        statusTextCount.maxLength = accounts.textLimit
        setMenu()
    }

    private fun requestOrOpenCamera() {
        if (checkAnySelfPermissionsGranted(AndroidPermission.WRITE_EXTERNAL_STORAGE)) {
            openCamera()
            return
        }
        ActivityCompat.requestPermissions(this, arrayOf(AndroidPermission.WRITE_EXTERNAL_STORAGE),
                REQUEST_OPEN_CAMERA_PERMISSION)
    }

    private fun openCamera(): Boolean {
        val builder = ThemedMediaPickerActivity.withThemed(this)
        if (intent.action == INTENT_ACTION_COMPOSE_TAKE_PHOTO) {
            builder.takePhoto()
        } else {
            builder.pickSources(arrayOf(MediaPickerActivity.SOURCE_CAMERA, MediaPickerActivity.SOURCE_CAMCORDER))
            builder.containsVideo(true)
            builder.videoOnly(false)
        }
        startActivityForResult(builder.build(), REQUEST_TAKE_PHOTO)
        return true
    }

    private fun requestOrPickMedia() {
        if (checkAnySelfPermissionsGranted(AndroidPermission.WRITE_EXTERNAL_STORAGE)) {
            pickMedia()
            return
        }
        ActivityCompat.requestPermissions(this, arrayOf(AndroidPermission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PICK_MEDIA_PERMISSION)
    }

    private fun pickMedia(): Boolean {
        val intent = ThemedMediaPickerActivity.withThemed(this)
                .pickMedia()
                .containsVideo(true)
                .videoOnly(false)
                .allowMultiple(true)
                .build()
        startActivityForResult(intent, REQUEST_PICK_MEDIA)
        return true
    }

    private fun saveAccountSelection() {
        if (!shouldSaveAccounts) return
        val editor = preferences.edit()

        editor.putString(KEY_COMPOSE_ACCOUNTS, accountsAdapter.selectedAccountKeys.joinToString(","))
        editor.apply()
    }

    private fun setMenu() {
        if (menuBar == null) return
        val menu = menuBar.menu
        val hasMedia = hasMedia()

        /*
         * No media & Not reply: [Take photo][Add image][Attach location][Drafts]
         * Has media & Not reply: [Take photo][Media menu][Attach location][Drafts]
         * Is reply: [Media menu][View status][Attach location][Drafts]
         */
        MenuUtils.setItemAvailability(menu, R.id.add_image, !hasMedia)
        MenuUtils.setItemAvailability(menu, R.id.media_menu, hasMedia)
        MenuUtils.setItemAvailability(menu, R.id.toggle_sensitive, hasMedia)
        MenuUtils.setItemAvailability(menu, R.id.schedule, scheduleSupported)

        menu.setGroupEnabled(MENU_GROUP_IMAGE_EXTENSION, hasMedia)
        menu.setGroupVisible(MENU_GROUP_IMAGE_EXTENSION, hasMedia)
        menu.setItemChecked(R.id.toggle_sensitive, hasMedia && possiblySensitive)
        ThemeUtils.resetCheatSheet(menuBar)
        //        mMenuBar.show();
    }

    private val scheduleSupported: Boolean
        get() {
            val accounts = accountsAdapter.selectedAccounts
            if (ArrayUtils.isEmpty(accounts)) return false
            return false
        }

    private fun setProgressVisible(visible: Boolean) {
        if (isFinishing) return
        executeAfterFragmentResumed { activity ->
            val composeActivity = activity as ComposeActivity
            val fm = composeActivity.supportFragmentManager
            val f = fm.findFragmentByTag(DISCARD_STATUS_DIALOG_FRAGMENT_TAG)
            if (!visible && f is DialogFragment) {
                f.dismiss()
            } else if (visible) {
                val df = ProgressDialogFragment()
                df.show(fm, DISCARD_STATUS_DIALOG_FRAGMENT_TAG)
                df.isCancelable = false
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_ATTACH_LOCATION_PERMISSION -> {
                if (checkAnySelfPermissionsGranted(AndroidPermission.ACCESS_FINE_LOCATION, AndroidPermission.ACCESS_COARSE_LOCATION)) {
                    try {
                        startLocationUpdateIfEnabled()
                    } catch (e: SecurityException) {
                        // That should not happen
                    }
                } else {
                    Toast.makeText(this, R.string.message_toast_cannot_get_location, Toast.LENGTH_SHORT).show()
                    kPreferences.edit {
                        this[attachLocationKey] = false
                        this[attachPreciseLocationKey] = false
                    }
                    locationSwitch.checkedPosition = LOCATION_OPTIONS.indexOf(LOCATION_VALUE_NONE)
                }
            }
            REQUEST_OPEN_CAMERA_PERMISSION -> {
                if (!checkAnySelfPermissionsGranted(AndroidPermission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.message_toast_compose_write_storage_no_permission, Toast.LENGTH_SHORT).show()
                }
                openCamera()
            }
            REQUEST_PICK_MEDIA_PERMISSION -> {
                if (!checkAnySelfPermissionsGranted(AndroidPermission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.message_toast_compose_write_storage_no_permission, Toast.LENGTH_SHORT).show()
                }
                pickMedia()
            }
        }
    }

    override fun onPermissionRequestCancelled(requestCode: Int) {
        when (requestCode) {
            REQUEST_ATTACH_LOCATION_PERMISSION -> {
                locationSwitch.checkedPosition = LOCATION_OPTIONS.indexOf(LOCATION_VALUE_NONE)
            }
        }
    }

    private fun setRecentLocation(location: ParcelableLocation?) {
        if (location != null) {
            val attachPreciseLocation = kPreferences[attachPreciseLocationKey]
            if (attachPreciseLocation) {
                locationText.text = ParcelableLocationUtils.getHumanReadableString(location, 3)
            } else {
                if (locationText.tag == null || location != recentLocation) {
                    val task = DisplayPlaceNameTask(this)
                    task.params = location
                    task.callback = locationText
                    TaskStarter.execute(task)
                }
            }
        } else {
            locationText.setText(R.string.unknown_location)
        }
        recentLocation = location
    }

    /**
     * The Location Manager manages location providers. This code searches for
     * the best provider of data (GPS, WiFi/cell phone tower lookup, some other
     * mechanism) and finds the last known location.
     */
    @Throws(SecurityException::class)
    private fun startLocationUpdateIfEnabled(): Boolean {
        if (locationListener != null) return true
        val attachLocation = kPreferences[attachLocationKey]
        if (!attachLocation) {
            return false
        }
        val attachPreciseLocation = kPreferences[attachPreciseLocationKey]
        val criteria = Criteria()
        if (attachPreciseLocation) {
            criteria.accuracy = Criteria.ACCURACY_FINE
        } else {
            criteria.accuracy = Criteria.ACCURACY_COARSE
        }
        val provider = locationManager.getBestProvider(criteria, true)
        if (provider != null) {
            locationText.setText(R.string.getting_location)
            locationListener = ComposeLocationListener(this)
            locationManager.requestLocationUpdates(provider, 0, 0f, locationListener)
            val location = Utils.getCachedLocation(this)
            if (location != null) {
                locationListener?.onLocationChanged(location)
            }
        } else {
            Toast.makeText(this, R.string.message_toast_cannot_get_location, Toast.LENGTH_SHORT).show()
        }
        return provider != null
    }

    private fun requestOrUpdateLocation() {
        if (checkAnySelfPermissionsGranted(AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION)) {
            try {
                startLocationUpdateIfEnabled()
            } catch (e: SecurityException) {
                Toast.makeText(this, R.string.message_toast_cannot_get_location, Toast.LENGTH_SHORT).show()
            }
        } else {
            val permissions = arrayOf(AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION)
            PermissionRequestDialog.show(supportFragmentManager, getString(R.string.message_permission_request_compose_location),
                    permissions, REQUEST_ATTACH_LOCATION_PERMISSION)
        }
    }

    private fun updateLocationState() {
        val attachLocation = kPreferences[attachLocationKey]
        locationIcon.isActivated = attachLocation
        if (!attachLocation) {
            locationText.setText(R.string.no_location)
        } else if (recentLocation != null) {
            setRecentLocation(recentLocation)
        } else {
            locationText.setText(R.string.getting_location)
        }
    }

    private fun updateStatus() {
        if (isFinishing || editText == null) return
        val hasMedia = hasMedia()
        val text = editText.text.toString()
        val tweetLength = validator.getTweetLength(text)
        val maxLength = statusTextCount.maxLength
        if (accountsAdapter.isSelectionEmpty) {
            editText.error = getString(R.string.message_toast_no_account_selected)
            return
        } else if (!hasMedia && (TextUtils.isEmpty(text) || noReplyContent(text))) {
            editText.error = getString(R.string.error_message_no_content)
            return
        } else if (maxLength > 0 && !statusShortenerUsed && tweetLength > maxLength) {
            editText.error = getString(R.string.error_message_status_too_long)
            val textLength = editText.length()
            editText.setSelection(textLength - (tweetLength - maxLength), textLength)
            return
        }
        val attachLocation = kPreferences[attachLocationKey]
        val attachPreciseLocation = kPreferences[attachPreciseLocationKey]
        val accountKeys = accountsAdapter.selectedAccountKeys
        val isPossiblySensitive = hasMedia && possiblySensitive
        val update = ParcelableStatusUpdate()
        @Draft.Action val action = draft?.action_type ?: getDraftAction(intent.action)
        update.accounts = AccountUtils.getAllAccountDetails(AccountManager.get(this), accountKeys, true)
        update.text = text
        if (attachLocation) {
            update.location = recentLocation
            update.display_coordinates = attachPreciseLocation
        }
        update.media = media
        update.in_reply_to_status = inReplyToStatus
        update.is_possibly_sensitive = isPossiblySensitive
        update.attachment_url = (draft?.action_extras as? UpdateStatusActionExtras)?.attachmentUrl
        LengthyOperationsService.updateStatusesAsync(this, action, update)
        if (preferences[noCloseAfterTweetSentKey] && inReplyToStatus == null) {
            possiblySensitive = false
            shouldSaveAccounts = true
            inReplyToStatus = null
            mentionUser = null
            draft = null
            originalText = null
            editText.text = null
            clearMedia()
            val intent = Intent(INTENT_ACTION_COMPOSE)
            setIntent(intent)
            handleIntent(intent)
            setLabel(intent)
            setMenu()
            updateTextCount()
            shouldSkipDraft = false
        } else {
            setResult(Activity.RESULT_OK)
            shouldSkipDraft = true
            finish()
        }
    }

    private fun updateTextCount() {
        val text = editText.text?.toString() ?: return
        statusTextCount.textCount = validator.getTweetLength(text)
    }

    internal class ComposeLocationListener(activity: ComposeActivity) : LocationListener {

        private val activityRef = WeakReference(activity)

        override fun onLocationChanged(location: Location) {
            val activity = activityRef.get() ?: return
            activity.setRecentLocation(ParcelableLocationUtils.fromLocation(location))
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

        }

        override fun onProviderEnabled(provider: String) {
        }

        override fun onProviderDisabled(provider: String) {
        }

    }

    internal class AccountIconViewHolder(val adapter: AccountIconsAdapter, itemView: View) : ViewHolder(itemView), OnClickListener {
        private val iconView = itemView.findViewById(android.R.id.icon) as ShapedImageView
        private val nameView = itemView.findViewById(android.R.id.text1) as TextView

        init {
            itemView.setOnClickListener(this)
        }

        fun showAccount(adapter: AccountIconsAdapter, account: AccountDetails, isSelected: Boolean) {
            itemView.alpha = if (isSelected) 1f else 0.33f
            (itemView as CheckableLinearLayout).isChecked = isSelected
            val context = adapter.context
            adapter.requestManager.loadProfileImage(context, account, adapter.profileImageStyle).into(iconView)
            iconView.setBorderColor(account.color)
            nameView.text = if (adapter.isNameFirst) account.user.name else "@" + account.user.screen_name
        }

        override fun onClick(v: View) {
            (itemView as CheckableLinearLayout).toggle()
            adapter.toggleSelection(layoutPosition)
        }


    }

    internal class AccountIconsAdapter(
            private val activity: ComposeActivity
    ) : BaseRecyclerViewAdapter<AccountIconViewHolder>(activity, Glide.with(activity)) {
        private val inflater: LayoutInflater = activity.layoutInflater
        private val selection: MutableMap<UserKey, Boolean> = HashMap()
        val isNameFirst: Boolean = preferences[nameFirstKey]

        private var accounts: Array<AccountDetails>? = null

        init {
            setHasStableIds(true)
        }

        override fun getItemId(position: Int): Long {
            return accounts!![position].hashCode().toLong()
        }

        val selectedAccountKeys: Array<UserKey>
            get() {
                val accounts = accounts ?: return emptyArray()
                return accounts.filter { selection[it.key] ?: false }
                        .map { it.key }
                        .toTypedArray()
            }

        fun setSelectedAccountIds(vararg accountKeys: UserKey) {
            selection.clear()
            for (accountKey in accountKeys) {
                selection.put(accountKey, true)
            }
            notifyDataSetChanged()
        }

        val selectedAccounts: Array<AccountDetails>
            get() {
                val accounts = accounts ?: return emptyArray()
                return accounts.filter { selection[it.key] ?: false }.toTypedArray()
            }

        val isSelectionEmpty: Boolean
            get() = selectedAccountKeys.isEmpty()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountIconViewHolder {
            val view = inflater.inflate(R.layout.adapter_item_compose_account, parent, false)
            return AccountIconViewHolder(this, view)
        }

        override fun onBindViewHolder(holder: AccountIconViewHolder, position: Int) {
            val account = accounts!![position]
            val isSelected = selection[account.key] ?: false
            holder.showAccount(this, account, isSelected)
        }

        override fun getItemCount(): Int {
            return if (accounts != null) accounts!!.size else 0
        }

        fun setAccounts(accounts: Array<AccountDetails>) {
            this.accounts = accounts
            notifyDataSetChanged()
        }

        internal fun toggleSelection(position: Int) {
            if (accounts == null || position < 0) return
            val account = accounts!![position]
            selection.put(account.key, true != selection[account.key])
            activity.notifyAccountSelectionChanged()
            notifyDataSetChanged()
        }
    }

    internal class AddMediaTask(
            activity: ComposeActivity,
            sources: Array<Uri>,
            copySrc: Boolean,
            deleteSrc: Boolean
    ) : AbsAddMediaTask<ComposeActivity>(activity, sources, copySrc, deleteSrc) {

        init {
            callback = activity
        }

        override fun afterExecute(activity: ComposeActivity?, result: List<ParcelableMediaUpdate>?) {
            if (activity == null || result == null) return
            activity.setProgressVisible(false)
            activity.addMedia(result)
            activity.setMenu()
            activity.updateTextCount()
        }

        override fun beforeExecute() {
            val activity = this.callback ?: return
            activity.setProgressVisible(true)
        }

    }

    internal class DeleteMediaTask(
            activity: ComposeActivity,
            val media: Array<ParcelableMediaUpdate>
    ) : AbsDeleteMediaTask<ComposeActivity>(activity, media.map { Uri.parse(it.uri) }.toTypedArray()) {

        init {
            this.callback = activity
        }

        override fun beforeExecute() {
            callback?.setProgressVisible(true)
        }

        override fun afterExecute(callback: ComposeActivity?, result: BooleanArray?) {
            if (callback == null || result == null) return
            callback.setProgressVisible(false)
            callback.removeAllMedia(media.filterIndexed { i, media -> result[i] })
            callback.setMenu()
            if (result.any { false }) {
                Toast.makeText(callback, R.string.message_toast_error_occurred, Toast.LENGTH_SHORT).show()
            }
        }
    }

    internal class DiscardTweetTask(activity: ComposeActivity) : AsyncTask<Any, Any, Unit>() {

        val activityRef: WeakReference<ComposeActivity>
        private val media: List<ParcelableMediaUpdate>

        init {
            this.activityRef = WeakReference(activity)
            this.media = activity.mediaList
        }

        override fun doInBackground(vararg params: Any) {
            val activity = activityRef.get() ?: return
            media.map { Uri.parse(it.uri) }.forEach { uri ->
                Utils.deleteMedia(activity, uri)
            }
        }

        override fun onPostExecute(result: Unit) {
            val activity = activityRef.get() ?: return
            activity.setProgressVisible(false)
            activity.shouldSkipDraft = true
            activity.finish()
        }

        override fun onPreExecute() {
            val activity = activityRef.get() ?: return
            activity.setProgressVisible(true)
        }
    }

    internal class DisplayPlaceNameTask(
            private val context: ComposeActivity
    ) : AbstractTask<ParcelableLocation, List<Address>, TextView>() {

        override fun doLongOperation(location: ParcelableLocation): List<Address>? {
            val gcd = Geocoder(context, Locale.getDefault())
            try {
                return gcd.getFromLocation(location.latitude, location.longitude, 1)
            } catch (e: IOException) {
                return null
            }

        }

        override fun beforeExecute() {
            val location = params
            val textView = callback ?: return

            val preferences = context.preferences
            val attachLocation = preferences[attachLocationKey]
            val attachPreciseLocation = preferences[attachPreciseLocationKey]
            if (attachLocation) {
                if (attachPreciseLocation) {
                    textView.text = ParcelableLocationUtils.getHumanReadableString(location, 3)
                    textView.tag = location
                } else {
                    val tag = textView.tag
                    if (tag is Address) {
                        textView.text = tag.locality
                    } else if (tag is NoAddress) {
                        textView.setText(R.string.label_location_your_coarse_location)
                    } else {
                        textView.setText(R.string.getting_location)
                    }
                }
            } else {
                textView.setText(R.string.no_location)
            }
        }

        override fun afterExecute(textView: TextView?, addresses: List<Address>?) {
            textView!!
            val preferences = context.preferences
            val attachLocation = preferences[attachLocationKey]
            val attachPreciseLocation = preferences[attachPreciseLocationKey]
            if (attachLocation) {
                if (attachPreciseLocation) {
                    val location = params
                    textView.text = ParcelableLocationUtils.getHumanReadableString(location, 3)
                    textView.tag = location
                } else if (addresses == null || addresses.isEmpty()) {
                    val tag = textView.tag
                    if (tag is Address) {
                        textView.text = tag.locality
                    } else {
                        textView.setText(R.string.label_location_your_coarse_location)
                        textView.tag = NoAddress()
                    }
                } else {
                    val address = addresses[0]
                    textView.tag = address
                    textView.text = address.locality
                }
            } else {
                textView.setText(R.string.no_location)
            }
        }

        internal class NoAddress
    }

    override fun onSetAltText(position: Int, altText: String?) {
        mediaPreviewAdapter.setAltText(position, altText)
    }

    class RetweetProtectedStatusWarnFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

        override fun onClick(dialog: DialogInterface, which: Int) {
            val activity = activity
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    if (activity is ComposeActivity) {
                        activity.updateStatus()
                    }
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context = activity
            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.quote_protected_status_warning_message)
            builder.setPositiveButton(R.string.send_anyway, this)
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.setOnShowListener {
                it as AlertDialog
                it.applyTheme()
            }
            return dialog
        }
    }

    class DirectMessageConfirmFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

        private val screenName: String get() = arguments.getString(EXTRA_SCREEN_NAME)

        override fun onClick(dialog: DialogInterface, which: Int) {
            val activity = activity
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    if (activity is ComposeActivity) {
                        activity.updateStatus()
                    }
                }
                DialogInterface.BUTTON_NEUTRAL -> {
                    if (activity is ComposeActivity) {
                        // Insert a ZWSP into status text
                        activity.editText.text.insert(1, "\u200b")
                        activity.updateStatus()
                    }
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context = activity
            val builder = AlertDialog.Builder(context)
            builder.setMessage(getString(R.string.message_format_compose_message_convert_to_status,
                    "@$screenName"))
            builder.setPositiveButton(R.string.action_send, this)
            builder.setNeutralButton(R.string.action_compose_message_convert_to_status, this)
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.setOnShowListener {
                it as AlertDialog
                it.applyTheme()
            }
            return dialog
        }
    }

    class AttachedMediaItemTouchHelperCallback(adapter: SimpleItemTouchHelperCallback.ItemTouchHelperAdapter) : SimpleItemTouchHelperCallback(adapter) {

        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return false
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
            // Set movement flags based on the layout manager
            val dragFlags = ItemTouchHelper.START or ItemTouchHelper.END
            val swipeFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                // Fade out the view as it is swiped out of the parent's bounds
                val alpha = ALPHA_FULL - Math.abs(dY) / viewHolder.itemView.height.toFloat()
                viewHolder.itemView.alpha = alpha
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            } else {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        override fun getSwipeThreshold(viewHolder: ViewHolder?): Float {
            return 0.75f
        }

        override fun clearView(recyclerView: RecyclerView?, viewHolder: ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            viewHolder.itemView.alpha = ALPHA_FULL
        }

        companion object {
            val ALPHA_FULL = 1.0f
        }
    }

    private class ComposeEnterListener(private val activity: ComposeActivity?) : EnterListener {

        override fun shouldCallListener(): Boolean {
            return activity != null && activity.composeKeyMetaState == 0
        }

        override fun onHitEnter(): Boolean {
            if (activity == null) return false
            activity.confirmAndUpdateStatus()
            return true
        }
    }

    companion object {

        // Constants
        private const val EXTRA_SHOULD_SAVE_ACCOUNTS = "should_save_accounts"
        private const val EXTRA_ORIGINAL_TEXT = "original_text"
        private const val EXTRA_DRAFT_UNIQUE_ID = "draft_unique_id"
        private const val DISCARD_STATUS_DIALOG_FRAGMENT_TAG = "discard_status"

        val LOCATION_VALUE_PLACE = "place"
        val LOCATION_VALUE_COORDINATE = "coordinate"
        val LOCATION_VALUE_NONE = "none"

        private val LOCATION_OPTIONS = arrayOf(LOCATION_VALUE_NONE, LOCATION_VALUE_PLACE, LOCATION_VALUE_COORDINATE)

        private const val REQUEST_ATTACH_LOCATION_PERMISSION = 301
        private const val REQUEST_PICK_MEDIA_PERMISSION = 302
        private const val REQUEST_OPEN_CAMERA_PERMISSION = 303

        internal fun getDraftAction(intentAction: String?): String {
            if (intentAction == null) {
                return Draft.Action.UPDATE_STATUS
            }
            when (intentAction) {
                INTENT_ACTION_REPLY -> {
                    return Draft.Action.REPLY
                }
                INTENT_ACTION_QUOTE -> {
                    return Draft.Action.QUOTE
                }
            }
            return Draft.Action.UPDATE_STATUS
        }
    }
}
