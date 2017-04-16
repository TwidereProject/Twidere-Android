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
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Canvas
import android.graphics.PorterDuff.Mode
import android.graphics.Rect
import android.location.*
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.view.SupportMenuInflater
import android.support.v7.widget.ActionMenuView.OnMenuItemClickListener
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.*
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.twitter.Extractor
import com.twitter.Validator
import kotlinx.android.synthetic.main.activity_compose.*
import nl.komponents.kovenant.task
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.ktextension.*
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.pickncrop.library.MediaPickerActivity
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.BaseRecyclerViewAdapter
import org.mariotaku.twidere.adapter.MediaPreviewAdapter
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_SCREEN_NAME
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.extension.model.applyUpdateStatus
import org.mariotaku.twidere.extension.model.textLimit
import org.mariotaku.twidere.extension.model.unique_id_non_null
import org.mariotaku.twidere.extension.text.twitter.ReplyTextAndMentions
import org.mariotaku.twidere.extension.text.twitter.extractReplyTextAndMentions
import org.mariotaku.twidere.extension.withAppendedPath
import org.mariotaku.twidere.fragment.*
import org.mariotaku.twidere.fragment.PermissionRequestDialog.PermissionRequestCancelCallback
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.analyzer.PurchaseFinished
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtras
import org.mariotaku.twidere.model.schedule.ScheduleInfo
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableLocationUtils
import org.mariotaku.twidere.preference.ServicePickerPreference
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.service.LengthyOperationsService
import org.mariotaku.twidere.task.compose.AbsAddMediaTask
import org.mariotaku.twidere.task.compose.AbsDeleteMediaTask
import org.mariotaku.twidere.task.twitter.UpdateStatusTask
import org.mariotaku.twidere.text.MarkForDeleteSpan
import org.mariotaku.twidere.text.style.EmojiSpan
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.EditTextEnterHandler.EnterListener
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.view.ViewAnimator
import org.mariotaku.twidere.util.view.ViewProperties
import org.mariotaku.twidere.view.CheckableLinearLayout
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.ShapedImageView
import org.mariotaku.twidere.view.helper.SimpleItemTouchHelperCallback
import org.mariotaku.twidere.view.holder.compose.MediaPreviewViewHolder
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
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
    private lateinit var bottomMenuAnimator: ViewAnimator
    private val supportMenuInflater by lazy { SupportMenuInflater(this) }

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
    private var ignoreMentions: Boolean = false
    private var replyToSelf: Boolean = false
    private var scheduleInfo: ScheduleInfo? = null
        set(value) {
            field = value
            updateUpdateStatusIcon()
        }

    // Listeners
    private var locationListener: LocationListener? = null

    private val draftAction: String get() = draft?.action_type ?: when (intent.action) {
        INTENT_ACTION_REPLY -> Draft.Action.REPLY
        INTENT_ACTION_QUOTE -> Draft.Action.QUOTE
        else -> Draft.Action.UPDATE_STATUS
    }

    private val media: Array<ParcelableMediaUpdate>
        get() = mediaList.toTypedArray()

    private val mediaList: List<ParcelableMediaUpdate>
        get() = mediaPreviewAdapter.asList()

    private var isAccountSelectorVisible: Boolean
        get() = bottomMenuAnimator.currentChild == accountSelector
        set(visible) {
            bottomMenuAnimator.showView(if (visible) accountSelector else composeMenu, true)
            displaySelectedAccountsIcon()
        }

    private val hasMedia: Boolean
        get() = mediaPreviewAdapter.itemCount > 0

    private val isQuote: Boolean
        get() = INTENT_ACTION_QUOTE == intent.action

    private val isQuotingProtectedStatus: Boolean
        get() {
            val status = inReplyToStatus
            if (!isQuote || status == null) return false
            return status.user_is_protected && status.account_key != status.user_key
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeneralComponent.get(this).inject(this)
        nameFirst = preferences[nameFirstKey]
        setContentView(R.layout.activity_compose)

        bottomMenuAnimator = ViewAnimator()
        bottomMenuAnimator.setupViews()

        mediaPreviewAdapter = MediaPreviewAdapter(this, Glide.with(this))
        mediaPreviewAdapter.listener = object : MediaPreviewAdapter.Listener {
            override fun onEditClick(position: Int, holder: MediaPreviewViewHolder) {
                attachedMediaPreview.showContextMenuForChild(holder.itemView)
            }

            override fun onRemoveClick(position: Int, holder: MediaPreviewViewHolder) {
                mediaPreviewAdapter.remove(position)
                updateMediaState()
                setMenu()
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
        val defaultAccountKeys = accountDetails.mapToArray(AccountDetails::key)
        menuBar.setOnMenuItemClickListener(this)
        setupEditText()
        accountSelectorButton.setOnClickListener(this)
        replyLabel.setOnClickListener(this)

        hintLabel.text = HtmlSpanBuilder.fromHtml(getString(R.string.hint_status_reply_to_user_removed)).apply {
            val dialogSpan = getSpans(0, length, URLSpan::class.java).firstOrNull {
                "#dialog" == it.url
            }
            if (dialogSpan != null) {
                val spanStart = getSpanStart(dialogSpan)
                val spanEnd = getSpanEnd(dialogSpan)
                removeSpan(dialogSpan)
                setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        MessageDialogFragment.show(supportFragmentManager,
                                message = getString(R.string.message_status_reply_to_user_removed_explanation),
                                tag = "status_reply_to_user_removed_explanation")
                    }
                }, spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            }
        }
        hintLabel.movementMethod = LinkMovementMethod.getInstance()
        hintLabel.linksClickable = true

        accountSelector.layoutManager = FixedLinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
            reverseLayout = false
            stackFromEnd = false
        }
        accountsAdapter = AccountIconsAdapter(this).apply {
            setAccounts(accountDetails)
        }
        accountSelector.adapter = accountsAdapter


        attachedMediaPreview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        attachedMediaPreview.adapter = mediaPreviewAdapter
        registerForContextMenu(attachedMediaPreview)
        itemTouchHelper.attachToRecyclerView(attachedMediaPreview)
        attachedMediaPreview.addItemDecoration(PreviewGridItemDecoration(resources.getDimensionPixelSize(R.dimen.element_spacing_small)))

        if (savedInstanceState == null) {
            // The context was first created
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
            val notificationAccount = intent.getParcelableExtra<UserKey?>(EXTRA_NOTIFICATION_ACCOUNT)
            if (notificationId != -1) {
                twitterWrapper.clearNotificationAsync(notificationId, notificationAccount)
            }
            if (!handleIntent(intent)) {
                handleDefaultIntent(intent)
            }
            showLabelAndHint(intent)
            val selectedAccountKeys = accountsAdapter.selectedAccountKeys
            if (selectedAccountKeys.isNullOrEmpty()) {
                val idsInPrefs = kPreferences[composeAccountsKey]?.asList() ?: emptyList()
                val intersection = defaultAccountKeys.intersect(idsInPrefs)

                if (intersection.isEmpty()) {
                    accountsAdapter.selectedAccountKeys = defaultAccountKeys
                } else {
                    accountsAdapter.selectedAccountKeys = intersection.toTypedArray()
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
        val imageExtensionsIntent = Intent(INTENT_ACTION_EXTENSION_EDIT_IMAGE)
        val mediaMenuItem = menu.findItem(R.id.status_attachment)
        if (mediaMenuItem != null && mediaMenuItem.hasSubMenu()) {
            val subMenu = mediaMenuItem.subMenu
            MenuUtils.addIntentToMenu(this, subMenu, composeExtensionsIntent,
                    MENU_GROUP_COMPOSE_EXTENSION)
            MenuUtils.addIntentToMenu(this, subMenu, imageExtensionsIntent,
                    MENU_GROUP_IMAGE_EXTENSION)
        }
        updateViewStyle()
        bottomMenuAnimator.showView(composeMenu, false)
        textChanged = false

        resetButtonsStates()
    }

    override fun onDestroy() {
        if (shouldSkipDraft || !isFinishing) {
            super.onDestroy()
            return
        }
        if (intent.getBooleanExtra(EXTRA_SAVE_DRAFT, true) && hasComposingStatus()) {
            saveToDrafts()
            Toast.makeText(this, R.string.message_toast_status_saved_to_draft, Toast.LENGTH_SHORT).show()
        } else {
            discardTweet()
        }
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()

        imageUploaderUsed = !ServicePickerPreference.isNoneValue(kPreferences[mediaUploaderKey])
        statusShortenerUsed = !ServicePickerPreference.isNoneValue(kPreferences[statusShortenerKey])
        if (kPreferences[attachLocationKey]) {
            if (checkAnySelfPermissionsGranted(AndroidPermission.ACCESS_COARSE_LOCATION,
                    AndroidPermission.ACCESS_FINE_LOCATION)) {
                try {
                    startLocationUpdateIfEnabled()
                } catch (e: SecurityException) {
                }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_TAKE_PHOTO, REQUEST_PICK_MEDIA -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val src = MediaPickerActivity.getMediaUris(data)
                    TaskStarter.execute(AddMediaTask(this, src, false, false))
                    val extras = data.getBundleExtra(MediaPickerActivity.EXTRA_EXTRAS)
                    if (extras?.getBoolean(EXTRA_IS_POSSIBLY_SENSITIVE) ?: false) {
                        possiblySensitive = true
                    }
                }
            }
            REQUEST_EDIT_IMAGE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (data.data != null) {
                        setMenu()
                        updateTextCount()
                    }
                }
            }
            REQUEST_EXTENSION_COMPOSE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // The latter two is for compatibility
                    val text = data.getCharSequenceExtra(Intent.EXTRA_TEXT) ?:
                            data.getStringExtra(EXTRA_TEXT) ?:
                            data.getStringExtra(EXTRA_APPEND_TEXT)
                    val isReplaceMode = data.getBooleanExtra(EXTRA_IS_REPLACE_MODE,
                            data.getStringExtra(EXTRA_APPEND_TEXT) == null)
                    if (text != null) {
                        val editable = editText.editableText
                        if (editable == null || isReplaceMode) {
                            editText.setText(text)
                        } else {
                            editable.replace(editText.selectionStart, editText.selectionEnd, text)
                        }
                        setMenu()
                        updateTextCount()
                    }

                    val src = MediaPickerActivity.getMediaUris(data)?.takeIf(Array<Uri>::isNotEmpty) ?:
                            data.getParcelableExtra<Uri>(EXTRA_IMAGE_URI)?.let { arrayOf(it) }
                    if (src != null) {
                        TaskStarter.execute(AddMediaTask(this, src, false, false))
                    }
                }
            }
            REQUEST_PURCHASE_EXTRA_FEATURES -> {
                if (resultCode == Activity.RESULT_OK) {
                    Analyzer.log(PurchaseFinished.create(data!!))
                }
            }
            REQUEST_SET_SCHEDULE -> {
                if (resultCode == Activity.RESULT_OK) {
                    scheduleInfo = data?.getParcelableExtra(EXTRA_SCHEDULE_INFO)
                }
            }
            REQUEST_ADD_GIF -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val intent = ThemedMediaPickerActivity.withThemed(this@ComposeActivity)
                            .getMedia(data.data)
                            .extras(Bundle {
                                this[EXTRA_IS_POSSIBLY_SENSITIVE] = data.getBooleanExtra(EXTRA_IS_POSSIBLY_SENSITIVE, false)
                            })
                            .build()
                    startActivityForResult(intent, REQUEST_PICK_MEDIA)
                }
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArray(EXTRA_ACCOUNT_KEYS, accountsAdapter.selectedAccountKeys)
        outState.putParcelableArrayList(EXTRA_MEDIA, ArrayList(mediaList))
        outState.putBoolean(EXTRA_IS_POSSIBLY_SENSITIVE, possiblySensitive)
        outState.putParcelable(EXTRA_STATUS, inReplyToStatus)
        outState.putParcelable(EXTRA_USER, mentionUser)
        outState.putParcelable(EXTRA_DRAFT, draft)
        outState.putParcelable(EXTRA_SCHEDULE_INFO, scheduleInfo)
        outState.putBoolean(EXTRA_SHOULD_SAVE_ACCOUNTS, shouldSaveAccounts)
        outState.putString(EXTRA_ORIGINAL_TEXT, originalText)
        outState.putString(EXTRA_DRAFT_UNIQUE_ID, draftUniqueId)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore from previous saved state
        accountsAdapter.selectedAccountKeys = savedInstanceState
                .getNullableTypedArray(EXTRA_ACCOUNT_KEYS) ?: emptyArray()
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
        scheduleInfo = savedInstanceState.getParcelable(EXTRA_SCHEDULE_INFO)
        showLabelAndHint(intent)

        resetButtonsStates()
    }

    override fun onClick(view: View) {
        when (view) {
            updateStatus -> {
                confirmAndUpdateStatus()
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
                requestOrTakePhoto()
            }
            R.id.record_video -> {
                requestOrCaptureVideo()
            }
            R.id.add_media -> {
                requestOrPickMedia()
            }
            R.id.drafts -> {
                IntentUtils.openDrafts(this)
            }
            R.id.delete -> {
                TaskStarter.execute(DeleteMediaTask(this, media))
            }
            R.id.toggle_sensitive -> {
                if (!hasMedia) return true
                possiblySensitive = !possiblySensitive
                setMenu()
                updateTextCount()
            }
            R.id.schedule -> {
                val provider = statusScheduleProvider ?: return true
                startActivityForResult(provider.createSetScheduleIntent(), REQUEST_SET_SCHEDULE)
            }
            R.id.add_gif -> {
                val provider = gifShareProvider ?: return true
                startActivityForResult(provider.createGifSelectorIntent(), REQUEST_ADD_GIF)
            }
            else -> {
                when (item.groupId) {
                    R.id.location_option -> {
                        locationMenuItemSelected(item)
                    }
                    else -> {
                        extensionIntentItemSelected(item)
                    }
                }
            }
        }
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (isAccountSelectorVisible && !TwidereViewUtils.hitView(ev, accountSelectorButton)) {
                    val layoutManager = accountSelector.layoutManager
                    val clickedItem = (0 until layoutManager.childCount).any {
                        TwidereViewUtils.hitView(ev, layoutManager.getChildAt(it))
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
            val window = window
            if (!TwidereViewUtils.hitView(event, window.decorView)
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

    override fun onActionModeStarted(mode: ActionMode) {
        super.onActionModeStarted(mode)
        ThemeUtils.applyColorFilterToMenuIcon(mode.menu, ThemeUtils.getColorForeground(this), 0, 0,
                Mode.MULTIPLY)
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
                }
            }
            REQUEST_TAKE_PHOTO_PERMISSION -> {
                if (!checkAnySelfPermissionsGranted(AndroidPermission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.message_toast_compose_write_storage_no_permission, Toast.LENGTH_SHORT).show()
                }
                takePhoto()
            }
            REQUEST_CAPTURE_VIDEO_PERMISSION -> {
                if (!checkAnySelfPermissionsGranted(AndroidPermission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.message_toast_compose_write_storage_no_permission, Toast.LENGTH_SHORT).show()
                }
                captureVideo()
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
            }
        }
    }


    override fun onSetAltText(position: Int, altText: String?) {
        mediaPreviewAdapter.setAltText(position, altText)
    }

    private fun locationMenuItemSelected(item: MenuItem) {
        item.isChecked = true
        var attachLocationChecked = false
        var attachPreciseLocationChecked = false
        when (item.itemId) {
            R.id.location_precise -> {
                attachLocationChecked = true
                attachPreciseLocationChecked = true
                locationLabel.tag = null
            }
            R.id.location_coarse -> {
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

    private fun extensionIntentItemSelected(item: MenuItem) {
        val intent = item.intent ?: return
        try {
            val action = intent.action
            when (action) {
                INTENT_ACTION_EXTENSION_COMPOSE -> {
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
                }
                else -> startActivity(intent)
            }
        } catch (e: ActivityNotFoundException) {
            Analyzer.logException(e)
        }
    }

    private fun addMedia(media: List<ParcelableMediaUpdate>) {
        mediaPreviewAdapter.addAll(media)
        updateMediaState()
        setMenu()
    }

    private fun removeMedia(list: List<ParcelableMediaUpdate>) {
        mediaPreviewAdapter.removeAll(list)
    }

    private fun clearMedia() {
        mediaPreviewAdapter.clear()
        updateMediaState()
        setMenu()
    }

    private fun displaySelectedAccountsIcon() {
        val accounts = accountsAdapter.selectedAccounts
        val account = accounts.singleOrNull()

        val displayDoneIcon = isAccountSelectorVisible

        if (account != null) {
            accountsCount.setText(null)

            if (displayDoneIcon) {
                Glide.clear(accountProfileImage)
                accountProfileImage.setColorFilter(ThemeUtils.getColorFromAttribute(this,
                        android.R.attr.colorForeground))
                accountProfileImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
                accountProfileImage.setImageResource(R.drawable.ic_action_confirm)
            } else {
                accountProfileImage.clearColorFilter()
                accountProfileImage.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(this).loadProfileImage(this, account, accountProfileImage.style)
                        .into(accountProfileImage)
            }

            accountProfileImage.setBorderColor(account.color)
        } else {
            accountsCount.setText(accounts.size.toString())

            Glide.clear(accountProfileImage)
            if (displayDoneIcon) {
                accountProfileImage.setImageResource(R.drawable.ic_action_confirm)
                accountProfileImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
            } else {
                accountProfileImage.setImageDrawable(null)
            }

            accountProfileImage.setBorderColors(*Utils.getAccountColors(accounts))
        }

        if (displayDoneIcon) {
            accountsCount.visibility = View.GONE
        } else {
            accountsCount.visibility = View.VISIBLE
        }
    }

    private fun updateMediaState() {
        attachedMediaPreview.visibility = if (hasMedia) View.VISIBLE else View.GONE
    }

    private fun resetButtonsStates() {
        updateLocationState()
        updateAccountSelectionState()
        updateUpdateStatusIcon()
        updateMediaState()
        setMenu()
    }

    // MARK: Begin intent handling

    private fun handleIntent(intent: Intent): Boolean {
        shouldSaveAccounts = false
        mentionUser = intent.getParcelableExtra(EXTRA_USER)
        inReplyToStatus = intent.getParcelableExtra(EXTRA_STATUS)
        when (intent.action) {
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
                val screenNames: Array<String>? = intent.getStringArrayExtra(EXTRA_SCREEN_NAMES)
                val accountKey: UserKey? = intent.getParcelableExtra(EXTRA_ACCOUNT_KEYS)
                val inReplyToStatus: ParcelableStatus? = intent.getParcelableExtra(EXTRA_IN_REPLY_TO_STATUS)
                return handleReplyMultipleIntent(screenNames, accountKey, inReplyToStatus)
            }
            INTENT_ACTION_COMPOSE_TAKE_PHOTO -> {
                requestOrTakePhoto()
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
        accountsAdapter.selectedAccountKeys = arrayOf(user.account_key)
        return true
    }

    private fun handleQuoteIntent(status: ParcelableStatus?): Boolean {
        if (status == null) return false
        editText.setText(Utils.getQuoteStatus(this, status))
        editText.setSelection(0)
        accountsAdapter.selectedAccountKeys = arrayOf(status.account_key)
        showQuoteLabelAndHint(status)
        return true
    }

    private fun handleReplyIntent(status: ParcelableStatus?): Boolean {
        if (status == null) return false
        val am = AccountManager.get(this)
        val details = AccountUtils.getAccountDetails(am, status.account_key, false) ?: return false
        val accountUser = details.user
        val mentions = ArrayList<String>()
        if (accountUser.key != status.user_key) {
            editText.append("@${status.user_screen_name} ")
        }
        var selectionStart = editText.length()
        if (status.is_retweet && !TextUtils.isEmpty(status.retweeted_by_user_screen_name)) {
            mentions.add(status.retweeted_by_user_screen_name)
        }
        if (status.is_quote && !TextUtils.isEmpty(status.quoted_user_screen_name)) {
            mentions.add(status.quoted_user_screen_name)
        }
        if (status.mentions.isNotNullOrEmpty()) {
            status.mentions.filterNot {
                it.key == status.account_key || it.screen_name.isNullOrEmpty()
            }.mapTo(mentions) { it.screen_name }
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

        mentions.distinctBy { it.toLowerCase(Locale.US) }.filterNot {
            return@filterNot it.equals(status.user_screen_name, ignoreCase = true)
        }.forEach { editText.append("@$it ") }

        // For non-Twitter instances, put current user mention at last
        if (details.type != AccountType.TWITTER && accountUser.key == status.user_key) {
            selectionStart = editText.length()
            editText.append("@${status.user_screen_name} ")
        }

        val text = intent.getStringExtra(EXTRA_TEXT)
        if (text != null) {
            editText.append(text)
        } else {
            val selectionEnd = editText.length()
            editText.setSelection(selectionStart, selectionEnd)
        }
        accountsAdapter.selectedAccountKeys = arrayOf(status.account_key)
        showReplyLabelAndHint(status)
        return true
    }

    private fun handleEditDraftIntent(draft: Draft?): Boolean {
        if (draft == null) return false
        val extras = draft.action_extras as? UpdateStatusActionExtras
        val media = draft.media
        draftUniqueId = draft.unique_id_non_null
        recentLocation = draft.location
        accountsAdapter.selectedAccountKeys = draft.account_keys ?: emptyArray()

        editText.setText(extras?.editingText ?: draft.text)
        editText.setSelection(editText.length())

        if (media != null) {
            addMedia(Arrays.asList(*media))
        }

        if (extras != null) {
            possiblySensitive = extras.isPossiblySensitive
            inReplyToStatus = extras.inReplyToStatus
        }

        val tag = Uri.withAppendedPath(Drafts.CONTENT_URI, draft._id.toString()).toString()
        notificationManager.cancel(tag, NOTIFICATION_ID_DRAFTS)
        return true
    }

    private fun handleDefaultIntent(intent: Intent?): Boolean {
        if (intent == null) return false
        val action = intent.action
        val hasAccountKeys: Boolean
        if (intent.hasExtra(EXTRA_ACCOUNT_KEYS)) {
            val accountKeys = intent.getParcelableArrayExtra(EXTRA_ACCOUNT_KEYS).toTypedArray(UserKey.CREATOR)
            accountsAdapter.selectedAccountKeys = accountKeys
            hasAccountKeys = true
        } else if (intent.hasExtra(EXTRA_ACCOUNT_KEY)) {
            val accountKey = intent.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)
            accountsAdapter.selectedAccountKeys = arrayOf(accountKey)
            hasAccountKeys = true
        } else {
            hasAccountKeys = false
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
            shouldSaveAccounts = !hasAccountKeys
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

    // MARK: End intent handling

    // MARK: Begin label and hint handling

    private fun showLabelAndHint(intent: Intent): Boolean {
        when (intent.action) {
            INTENT_ACTION_REPLY -> {
                return showReplyLabelAndHint(intent.getParcelableExtra(EXTRA_STATUS))
            }
            INTENT_ACTION_QUOTE -> {
                return showQuoteLabelAndHint(intent.getParcelableExtra(EXTRA_STATUS))
            }
            INTENT_ACTION_EDIT_DRAFT -> {
                val draft: Draft? = intent.getParcelableExtra(EXTRA_DRAFT)
                when (draft?.action_type) {
                    Draft.Action.REPLY -> {
                        return showReplyLabelAndHint((draft.action_extras as? UpdateStatusActionExtras)?.inReplyToStatus)
                    }
                    Draft.Action.QUOTE -> {
                        return showQuoteLabelAndHint((draft.action_extras as? UpdateStatusActionExtras)?.inReplyToStatus)
                    }
                    else -> {
                        showDefaultLabelAndHint()
                        return false
                    }
                }
            }
            else -> {
                showDefaultLabelAndHint()
                return false
            }
        }
    }

    private fun showQuoteLabelAndHint(status: ParcelableStatus?): Boolean {
        if (status == null) {
            showDefaultLabelAndHint()
            return false
        }
        val replyToName = userColorNameManager.getDisplayName(status, nameFirst)
        replyLabel.text = getString(R.string.label_quote_name_text, replyToName, status.text_unescaped)
        replyLabel.visibility = View.VISIBLE
        editText.hint = getString(R.string.label_quote_name, replyToName)
        return true
    }

    private fun showReplyLabelAndHint(status: ParcelableStatus?): Boolean {
        if (status == null) {
            showDefaultLabelAndHint()
            return false
        }
        val replyToName = userColorNameManager.getDisplayName(status, nameFirst)
        replyLabel.text = getString(R.string.label_reply_name_text, replyToName, status.text_unescaped)
        replyLabel.visibility = View.VISIBLE
        editText.hint = getString(R.string.label_reply_name, replyToName)
        return true
    }

    private fun showDefaultLabelAndHint() {
        replyLabel.visibility = View.GONE
        editText.setHint(R.string.label_status_hint)
    }

    // MARK: End label and hint handling

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

    private fun handleReplyMultipleIntent(screenNames: Array<String>?, accountKey: UserKey?,
            inReplyToStatus: ParcelableStatus?): Boolean {
        if (screenNames == null || screenNames.isEmpty() || accountKey == null ||
                inReplyToStatus == null) return false
        val myScreenName = DataStoreUtils.getAccountScreenName(this, accountKey) ?: return false
        screenNames.filterNot { it.equals(myScreenName, ignoreCase = true) }
                .forEach { editText.append("@$it ") }
        editText.setSelection(editText.length())
        accountsAdapter.selectedAccountKeys = arrayOf(accountKey)
        this.inReplyToStatus = inReplyToStatus
        return true
    }

    private fun updateAccountSelectionState() {
        displaySelectedAccountsIcon()
        val accounts = accountsAdapter.selectedAccounts
        editText.accountKey = accounts.firstOrNull()?.key ?: Utils.getDefaultAccountKey(this)
        statusTextCount.maxLength = accounts.textLimit
        val singleAccount = accounts.singleOrNull()
        ignoreMentions = singleAccount?.type == AccountType.TWITTER
        replyToSelf = singleAccount?.let { it.key == inReplyToStatus?.user_key } ?: false
    }

    private fun requestOrTakePhoto() {
        if (checkAnySelfPermissionsGranted(AndroidPermission.WRITE_EXTERNAL_STORAGE)) {
            takePhoto()
            return
        }
        ActivityCompat.requestPermissions(this, arrayOf(AndroidPermission.WRITE_EXTERNAL_STORAGE),
                REQUEST_TAKE_PHOTO_PERMISSION)
    }

    private fun requestOrCaptureVideo() {
        if (checkAnySelfPermissionsGranted(AndroidPermission.WRITE_EXTERNAL_STORAGE)) {
            captureVideo()
            return
        }
        ActivityCompat.requestPermissions(this, arrayOf(AndroidPermission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CAPTURE_VIDEO_PERMISSION)
    }

    private fun requestOrPickMedia() {
        if (checkAnySelfPermissionsGranted(AndroidPermission.WRITE_EXTERNAL_STORAGE)) {
            pickMedia()
            return
        }
        ActivityCompat.requestPermissions(this, arrayOf(AndroidPermission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PICK_MEDIA_PERMISSION)
    }

    private fun takePhoto(): Boolean {
        val builder = ThemedMediaPickerActivity.withThemed(this)
        builder.takePhoto()
        startActivityForResult(builder.build(), REQUEST_TAKE_PHOTO)
        return true
    }

    private fun captureVideo(): Boolean {
        val builder = ThemedMediaPickerActivity.withThemed(this)
        builder.captureVideo()
        startActivityForResult(builder.build(), REQUEST_TAKE_PHOTO)
        return true
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
        preferences[composeAccountsKey] = accountsAdapter.selectedAccountKeys
    }

    private fun setMenu() {
        if (menuBar == null) return
        val menu = menuBar.menu
        val hasMedia = this.hasMedia

        /*
         * No media & Not reply: [Take photo][Add image][Attach location][Drafts]
         * Has media & Not reply: [Take photo][Media menu][Attach location][Drafts]
         * Is reply: [Media menu][View status][Attach location][Drafts]
         */
        menu.setItemAvailability(R.id.toggle_sensitive, hasMedia)
        menu.setItemAvailability(R.id.schedule, extraFeaturesService.isSupported(
                ExtraFeaturesService.FEATURE_SCHEDULE_STATUS))
        menu.setItemAvailability(R.id.add_gif, extraFeaturesService.isSupported(
                ExtraFeaturesService.FEATURE_SHARE_GIF))

        menu.setGroupAvailability(MENU_GROUP_IMAGE_EXTENSION, hasMedia)
        menu.setItemChecked(R.id.toggle_sensitive, hasMedia && possiblySensitive)

        val attachLocation = kPreferences[attachLocationKey]
        val attachPreciseLocation = kPreferences[attachPreciseLocationKey]

        if (!attachLocation) {
            menu.setItemChecked(R.id.location_off, true)
            menu.setMenuItemIcon(R.id.location_submenu, R.drawable.ic_action_location_off)
        } else if (attachPreciseLocation) {
            menu.setItemChecked(R.id.location_precise, true)
            menu.setMenuItemIcon(R.id.location_submenu, R.drawable.ic_action_location)
        } else {
            menu.setItemChecked(R.id.location_coarse, true)
            menu.setMenuItemIcon(R.id.location_submenu, R.drawable.ic_action_location)
        }

        ThemeUtils.wrapMenuIcon(menuBar, excludeGroups = MENU_GROUP_IMAGE_EXTENSION)
        ThemeUtils.resetCheatSheet(menuBar)
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

    private fun setRecentLocation(location: ParcelableLocation?) {
        if (location != null) {
            val attachPreciseLocation = kPreferences[attachPreciseLocationKey]
            if (attachPreciseLocation) {
                locationLabel.text = ParcelableLocationUtils.getHumanReadableString(location, 3)
            } else {
                if (locationLabel.tag == null || location != recentLocation) {
                    val task = DisplayPlaceNameTask()
                    task.params = location
                    task.callback = this
                    TaskStarter.execute(task)
                }
            }
        } else {
            locationLabel.setText(R.string.unknown_location)
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
            locationLabel.setText(R.string.getting_location)
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


    private fun hasComposingStatus(): Boolean {
        if (intent.action == INTENT_ACTION_EDIT_DRAFT) return true
        if (hasMedia) return true
        val text = editText.text?.toString().orEmpty()
        if (text == originalText) return false
        val replyTextAndMentions = getTwitterReplyTextAndMentions(text)
        if (replyTextAndMentions != null) {
            return replyTextAndMentions.replyText.isNotEmpty()
        }
        return text.isNotEmpty()
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
        } else if (scheduleInfo != null && !extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_SCHEDULE_STATUS)) {
            ExtraFeaturesIntroductionDialogFragment.show(supportFragmentManager,
                    feature = ExtraFeaturesService.FEATURE_SCHEDULE_STATUS,
                    requestCode = REQUEST_PURCHASE_EXTRA_FEATURES)
        } else {
            updateStatus()
        }
    }

    private fun updateStatus() {
        if (isFinishing || editText == null) return

        val update = try {
            getStatusUpdate()
        } catch(e: NoAccountException) {
            editText.error = getString(R.string.message_toast_no_account_selected)
            return
        } catch(e: NoContentException) {
            editText.error = getString(R.string.error_message_no_content)
            return
        } catch(e: StatusTooLongException) {
            editText.error = getString(R.string.error_message_status_too_long)
            editText.setSelection(e.exceededStartIndex, editText.length())
            return
        }

        LengthyOperationsService.updateStatusesAsync(this, update.draft_action, statuses = update,
                scheduleInfo = scheduleInfo)
        finishComposing()
    }

    private fun finishComposing() {
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
            showLabelAndHint(intent)
            setMenu()
            updateTextCount()
            shouldSkipDraft = false
        } else {
            setResult(Activity.RESULT_OK)
            shouldSkipDraft = true
            finish()
        }
    }

    private fun discardTweet() {
        val context = applicationContext
        val media = mediaList
        task { media.forEach { media -> Utils.deleteMedia(context, Uri.parse(media.uri)) } }
    }

    private fun getStatusUpdate(): ParcelableStatusUpdate {
        val accountKeys = accountsAdapter.selectedAccountKeys
        if (accountKeys.isEmpty()) throw NoAccountException()
        val update = ParcelableStatusUpdate()
        val media = this.media
        val text = editText.text?.toString().orEmpty()
        val accounts = AccountUtils.getAllAccountDetails(AccountManager.get(this), accountKeys, true)
        val maxLength = statusTextCount.maxLength
        val inReplyTo = inReplyToStatus
        val replyTextAndMentions = getTwitterReplyTextAndMentions(text, accounts)
        if (inReplyTo != null && replyTextAndMentions != null) {
            val (replyStartIndex, replyText, _, excludedMentions, replyToOriginalUser) =
                    replyTextAndMentions
            if (replyText.isEmpty() && media.isEmpty()) throw NoContentException()
            if (!statusShortenerUsed && validator.getTweetLength(replyText) > maxLength) {
                throw StatusTooLongException(replyStartIndex + replyText.offsetByCodePoints(0, maxLength))
            }
            update.text = replyText
            update.extended_reply_mode = true
            update.excluded_reply_user_ids = excludedMentions.mapToArray { it.key.id }
            val replyToSelf = accounts.singleOrNull()?.key == inReplyTo.user_key
            // Fix status to at least make mentioned user know what status it is
            if (!replyToOriginalUser && !replyToSelf) {
                update.attachment_url = LinkCreator.getStatusWebLink(inReplyTo).toString()
            }
        } else {
            if (text.isEmpty() && media.isEmpty()) throw NoContentException()
            if (!statusShortenerUsed && validator.getTweetLength(text) > maxLength) {
                throw StatusTooLongException(text.offsetByCodePoints(0, maxLength))
            }
            update.text = text
            update.extended_reply_mode = false
        }

        val attachLocation = kPreferences[attachLocationKey]
        val attachPreciseLocation = kPreferences[attachPreciseLocationKey]
        update.draft_action = draftAction
        update.accounts = accounts
        if (attachLocation) {
            update.location = recentLocation
            update.display_coordinates = attachPreciseLocation
        }
        update.media = media
        update.in_reply_to_status = inReplyTo
        update.is_possibly_sensitive = possiblySensitive
        update.draft_extras = update.updateStatusActionExtras().also {
            it.editingText = text
        }
        return update
    }

    private fun updateTextCount() {
        val editable = editText.editableText ?: return
        val text = editable.toString()
        val textAndMentions = getTwitterReplyTextAndMentions(text)
        if (textAndMentions == null) {
            hintLabel.visibility = View.GONE
            editable.clearSpans(MentionColorSpan::class.java)
            statusTextCount.textCount = validator.getTweetLength(text)
        } else if (textAndMentions.replyToOriginalUser || replyToSelf) {
            hintLabel.visibility = View.GONE
            val mentionColor = ThemeUtils.getTextColorSecondary(this)
            editable.clearSpans(MentionColorSpan::class.java)
            editable.setSpan(MentionColorSpan(mentionColor), 0, textAndMentions.replyStartIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            statusTextCount.textCount = validator.getTweetLength(textAndMentions.replyText)
        } else {
            hintLabel.visibility = View.VISIBLE
            editable.clearSpans(MentionColorSpan::class.java)
            statusTextCount.textCount = validator.getTweetLength(textAndMentions.replyText)
        }
    }

    private fun updateUpdateStatusIcon() {
        if (scheduleInfo != null) {
            updateStatusIcon.setImageResource(R.drawable.ic_action_time)
        } else {
            updateStatusIcon.setImageResource(R.drawable.ic_action_send)
        }
    }

    private fun updateLocationState() {
        if (kPreferences[attachLocationKey]) {
            locationLabel.visibility = View.VISIBLE
            if (recentLocation != null) {
                setRecentLocation(recentLocation)
            } else {
                locationLabel.setText(R.string.getting_location)
            }
        } else {
            locationLabel.visibility = View.GONE
        }
    }

    private fun getTwitterReplyTextAndMentions(text: String = editText.text?.toString().orEmpty(),
            accounts: Array<AccountDetails> = accountsAdapter.selectedAccounts): ReplyTextAndMentions? {
        val inReplyTo = inReplyToStatus ?: return null
        if (!ignoreMentions) return null
        val account = accounts.singleOrNull() ?: return null
        return extractor.extractReplyTextAndMentions(text, inReplyTo, account.key)
    }

    private fun saveToDrafts(): Uri? {
        val statusUpdate = try {
            getStatusUpdate()
        } catch(e: ComposeException) {
            return null
        }
        val draft = UpdateStatusTask.createDraft(draftAction) {
            applyUpdateStatus(statusUpdate)
        }
        val values = ObjectCursor.valuesCreatorFrom(Draft::class.java).create(draft)
        val draftUri = contentResolver.insert(Drafts.CONTENT_URI, values)
        displayNewDraftNotification(draftUri)
        return draftUri
    }

    private fun displayNewDraftNotification(draftUri: Uri) {
        val notificationUri = Drafts.CONTENT_URI_NOTIFICATIONS.withAppendedPath(draftUri.lastPathSegment)
        contentResolver.insert(notificationUri, null)
    }

    private fun ViewAnimator.setupViews() {
        fun AnimatorSet.setup() {
            interpolator = DecelerateInterpolator()
            duration = 250
        }

        addView(accountSelector) { view ->
            inAnimator = AnimatorSet().also { set ->
                set.playTogether(
                        ObjectAnimator.ofFloat(view, ViewProperties.TRANSLATION_X_RELATIVE, -1f, 0f),
                        ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
                )
                set.setup()
            }
            outAnimator = AnimatorSet().also { set ->
                set.playTogether(
                        ObjectAnimator.ofFloat(view, ViewProperties.TRANSLATION_X_RELATIVE, 0f, -1f),
                        ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
                )
                set.setup()
            }
        }
        addView(composeMenu) { view ->
            inAnimator = AnimatorSet().also { set ->
                set.playTogether(
                        ObjectAnimator.ofFloat(view, ViewProperties.TRANSLATION_X_RELATIVE, 1f, 0f),
                        ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
                )
                set.setup()
            }
            outAnimator = AnimatorSet().also { set ->
                set.playTogether(
                        ObjectAnimator.ofFloat(view, ViewProperties.TRANSLATION_X_RELATIVE, 0f, 1f),
                        ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
                )
                set.setup()
            }
        }
    }

    private fun updateViewStyle() {
        accountProfileImage.style = preferences[profileImageStyleKey]
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
        editTextContainer.touchDelegate = ComposeEditTextTouchDelegate(editTextContainer, editText)
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

    private class ComposeLocationListener(activity: ComposeActivity) : LocationListener {

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

    private class AccountIconViewHolder(val adapter: AccountIconsAdapter, itemView: View) : ViewHolder(itemView) {
        private val iconView = itemView.findViewById(android.R.id.icon) as ShapedImageView

        init {
            itemView.setOnClickListener {
                (itemView as CheckableLinearLayout).toggle()
                adapter.toggleSelection(layoutPosition)
            }
            itemView.setOnLongClickListener {
                (itemView as CheckableLinearLayout).toggle()
                adapter.setSelection(layoutPosition)
                return@setOnLongClickListener true
            }
        }

        fun showAccount(adapter: AccountIconsAdapter, account: AccountDetails, isSelected: Boolean) {
            itemView.alpha = if (isSelected) 1f else 0.33f
            val context = adapter.context
            adapter.requestManager.loadProfileImage(context, account, adapter.profileImageStyle).into(iconView)
            iconView.setBorderColor(account.color)
        }

    }

    private class AccountIconsAdapter(
            private val activity: ComposeActivity
    ) : BaseRecyclerViewAdapter<AccountIconViewHolder>(activity, Glide.with(activity)) {
        private val inflater: LayoutInflater = activity.layoutInflater
        private val selection: MutableMap<UserKey, Boolean> = HashMap()

        private var accounts: Array<AccountDetails>? = null

        init {
            setHasStableIds(true)
        }

        var selectedAccountKeys: Array<UserKey>
            get() {
                val accounts = accounts ?: return emptyArray()
                return accounts.filter { selection[it.key] ?: false }
                        .map { it.key }
                        .toTypedArray()
            }
            set(value) {
                selection.clear()
                for (accountKey in value) {
                    selection.put(accountKey, true)
                }
                notifyDataSetChanged()
            }

        val selectedAccounts: Array<AccountDetails>
            get() {
                val accounts = accounts ?: return emptyArray()
                return accounts.filter { selection[it.key] ?: false }.toTypedArray()
            }

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

        override fun getItemId(position: Int): Long {
            return accounts!![position].hashCode().toLong()
        }

        fun setAccounts(accounts: Array<AccountDetails>) {
            this.accounts = accounts
            notifyDataSetChanged()
        }

        fun toggleSelection(position: Int) {
            if (accounts == null || position < 0) return
            val account = accounts!![position]
            selection.put(account.key, true != selection[account.key])
            activity.updateAccountSelectionState()
            activity.setMenu()
            notifyDataSetChanged()
        }

        fun setSelection(position: Int) {
            if (accounts == null || position < 0) return
            val account = accounts!![position]
            selection.clear()
            selection.put(account.key, true != selection[account.key])
            activity.updateAccountSelectionState()
            activity.setMenu()
            notifyDataSetChanged()
        }
    }

    private class AddMediaTask(activity: ComposeActivity, sources: Array<Uri>, copySrc: Boolean,
            deleteSrc: Boolean) : AbsAddMediaTask<ComposeActivity>(activity, sources, copySrc, deleteSrc) {

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

    private class DeleteMediaTask(activity: ComposeActivity, val media: Array<ParcelableMediaUpdate>) :
            AbsDeleteMediaTask<ComposeActivity>(activity, media.mapToArray { Uri.parse(it.uri) }) {

        init {
            this.callback = activity
        }

        override fun beforeExecute() {
            callback?.setProgressVisible(true)
        }

        override fun afterExecute(callback: ComposeActivity?, result: BooleanArray?) {
            if (callback == null || result == null) return
            callback.setProgressVisible(false)
            callback.removeMedia(media.filterIndexed { i, _ -> result[i] })
            callback.setMenu()
            if (result.any { false }) {
                Toast.makeText(callback, R.string.message_toast_error_occurred, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private class DisplayPlaceNameTask : AbstractTask<ParcelableLocation, List<Address>, ComposeActivity>() {

        override fun doLongOperation(location: ParcelableLocation): List<Address>? {
            try {
                val activity = callback ?: throw IOException("Interrupted")
                val gcd = Geocoder(activity, Locale.getDefault())
                return gcd.getFromLocation(location.latitude, location.longitude, 1)
            } catch (e: IOException) {
                return null
            }

        }

        override fun beforeExecute() {
            val location = params
            val activity = callback ?: return
            val textView = activity.locationLabel ?: return

            val preferences = activity.preferences
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

        override fun afterExecute(activity: ComposeActivity?, addresses: List<Address>?) {
            if (activity == null) return
            val textView = activity.locationLabel ?: return
            val preferences = activity.preferences
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

    private class ComposeEditTextTouchDelegate(
            val parentView: View, val delegateView: View
    ) : TouchDelegate(Rect(), delegateView) {

        private var delegateTargeted: Boolean = false

        override fun onTouchEvent(event: MotionEvent): Boolean {
            var sendToDelegate = false
            var handled = false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (TwidereViewUtils.hitView(event, delegateView)) {
                        delegateTargeted = false
                        sendToDelegate = false
                    } else if (TwidereViewUtils.hitView(event, parentView)) {
                        delegateTargeted = true
                        sendToDelegate = true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE -> {
                    sendToDelegate = delegateTargeted
                }
                MotionEvent.ACTION_CANCEL -> {
                    sendToDelegate = delegateTargeted
                    delegateTargeted = false
                }
            }
            if (sendToDelegate) {
                handled = delegateView.dispatchTouchEvent(event)
            }
            return handled
        }
    }

    private class MentionColorSpan(color: Int) : ForegroundColorSpan(color)

    private open class ComposeException : Exception()

    private class StatusTooLongException(val exceededStartIndex: Int) : ComposeException()
    private class NoContentException : ComposeException()
    private class NoAccountException : ComposeException()

    companion object {

        // Constants
        private const val EXTRA_SHOULD_SAVE_ACCOUNTS = "should_save_accounts"
        private const val EXTRA_ORIGINAL_TEXT = "original_text"
        private const val EXTRA_DRAFT_UNIQUE_ID = "draft_unique_id"
        private const val DISCARD_STATUS_DIALOG_FRAGMENT_TAG = "discard_status"

        private const val REQUEST_ATTACH_LOCATION_PERMISSION = 301
        private const val REQUEST_PICK_MEDIA_PERMISSION = 302
        private const val REQUEST_TAKE_PHOTO_PERMISSION = 303
        private const val REQUEST_CAPTURE_VIDEO_PERMISSION = 304
        private const val REQUEST_SET_SCHEDULE = 305
        private const val REQUEST_ADD_GIF = 306

        private fun ParcelableStatusUpdate.updateStatusActionExtras() = UpdateStatusActionExtras().also {
            it.inReplyToStatus = in_reply_to_status
            it.isPossiblySensitive = is_possibly_sensitive
            it.displayCoordinates = display_coordinates
            it.excludedReplyUserIds = excluded_reply_user_ids
            it.isExtendedReplyMode = extended_reply_mode
        }

    }
}

