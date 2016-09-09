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

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.PorterDuff.Mode
import android.graphics.Rect
import android.location.*
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Parcelable
import android.provider.BaseColumns
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.view.SupportMenuInflater
import android.support.v7.widget.*
import android.support.v7.widget.ActionMenuView.OnMenuItemClickListener
import android.support.v7.widget.RecyclerView.*
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.*
import android.text.style.ImageSpan
import android.text.style.SuggestionSpan
import android.text.style.UpdateAppearance
import android.util.Log
import android.view.*
import android.view.ActionMode.Callback
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.afollestad.appthemeengine.Config
import com.afollestad.appthemeengine.customizers.ATEToolbarCustomizer
import com.afollestad.appthemeengine.util.ATEUtil
import com.twitter.Extractor
import kotlinx.android.synthetic.main.activity_compose.*
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.ObjectUtils
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.commons.io.StreamUtils
import org.mariotaku.ktextension.setItemChecked
import org.mariotaku.ktextension.toTypedArray
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ArrayRecyclerAdapter
import org.mariotaku.twidere.adapter.BaseRecyclerViewAdapter
import org.mariotaku.twidere.constant.KeyboardShortcutConstants
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtra
import org.mariotaku.twidere.model.util.ParcelableAccountUtils
import org.mariotaku.twidere.model.util.ParcelableLocationUtils
import org.mariotaku.twidere.preference.ServicePickerPreference
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.service.BackgroundOperationService
import org.mariotaku.twidere.text.MarkForDeleteSpan
import org.mariotaku.twidere.text.style.EmojiSpan
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.EditTextEnterHandler.EnterListener
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.view.CheckableLinearLayout
import org.mariotaku.twidere.view.ExtendedRecyclerView
import org.mariotaku.twidere.view.ShapedImageView
import org.mariotaku.twidere.view.helper.SimpleItemTouchHelperCallback
import java.io.*
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

class ComposeActivity : BaseActivity(), OnMenuItemClickListener, OnClickListener, OnLongClickListener, Callback, ATEToolbarCustomizer {

    // Utility classes
    @Inject
    lateinit var extractor: Extractor
    @Inject
    lateinit var validator: TwidereValidator
    @Inject
    lateinit var defaultFeatures: DefaultFeatures

    private var locationManager: LocationManager? = null
    private var mTask: AsyncTask<Any, Any, *>? = null
    private val supportMenuInflater by lazy { SupportMenuInflater(this) }
    private var itemTouchHelper: ItemTouchHelper? = null

    private val backTimeoutRunnable = Runnable { navigateBackPressed = false }

    // Adapters
    private var mediaPreviewAdapter: MediaPreviewAdapter? = null
    private var accountsAdapter: AccountIconsAdapter? = null

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

    // Listeners
    private var mLocationListener: LocationListener? = null
    private var mNameFirst: Boolean = false

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        when (requestCode) {
            REQUEST_TAKE_PHOTO, REQUEST_PICK_IMAGE, REQUEST_OPEN_DOCUMENT -> {
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    val src = arrayOf(intent.data)
                    val dst = arrayOf(createTempImageUri(0))
                    mTask = AsyncTaskUtils.executeTask(AddMediaTask(this, src, dst,
                            ParcelableMedia.Type.IMAGE, true))
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
        if (mTask != null && mTask!!.status == AsyncTask.Status.RUNNING) return
        if (hasComposingStatus()) {
            saveToDrafts()
            Toast.makeText(this, R.string.status_saved_to_draft, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            discardTweet()
        }
    }

    protected fun discardTweet() {
        if (isFinishing || mTask != null && mTask!!.status == AsyncTask.Status.RUNNING) return
        mTask = AsyncTaskUtils.executeTask(DiscardTweetTask(this))
    }

    protected fun hasComposingStatus(): Boolean {
        val text = if (editText != null) ParseUtils.parseString(editText.text) else null
        val textChanged = text != null && !text.isEmpty() && text != originalText
        val isEditingDraft = INTENT_ACTION_EDIT_DRAFT == intent.action
        return textChanged || hasMedia() || isEditingDraft
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArray(EXTRA_ACCOUNT_KEYS, accountsAdapter!!.selectedAccountKeys)
        outState.putParcelableArrayList(EXTRA_MEDIA, ArrayList<Parcelable>(mediaList))
        outState.putBoolean(EXTRA_IS_POSSIBLY_SENSITIVE, possiblySensitive)
        outState.putParcelable(EXTRA_STATUS, inReplyToStatus)
        outState.putParcelable(EXTRA_USER, mentionUser)
        outState.putParcelable(EXTRA_DRAFT, draft)
        outState.putBoolean(EXTRA_SHOULD_SAVE_ACCOUNTS, shouldSaveAccounts)
        outState.putString(EXTRA_ORIGINAL_TEXT, originalText)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        imageUploaderUsed = !ServicePickerPreference.isNoneValue(preferences.getString(KEY_MEDIA_UPLOADER, null))
        statusShortenerUsed = !ServicePickerPreference.isNoneValue(preferences.getString(KEY_STATUS_SHORTENER, null))
        if (preferences.getBoolean(KEY_ATTACH_LOCATION)) {
            requestOrUpdateLocation()
        }
        setMenu()
        updateTextCount()
        val textSize = preferences.getInt(KEY_TEXT_SIZE, Utils.getDefaultTextSize(this))
        editText.textSize = textSize * 1.25f
    }

    override fun onStop() {
        saveAccountSelection()
        try {
            if (mLocationListener != null) {
                locationManager!!.removeUpdates(mLocationListener)
                mLocationListener = null
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
        if (isQuotingProtectedStatus) {
            RetweetProtectedStatusWarnFragment().show(supportFragmentManager,
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
                Utils.showMenuItemToast(v, getString(R.string.send), true)
                return true
            }
        }
        return false
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.take_photo -> {
                takePhoto()
            }
            R.id.add_image, R.id.add_image_sub_item -> {
                pickImage()
            }
            R.id.drafts -> {
                IntentUtils.openDrafts(this)
            }
            R.id.delete -> {
                AsyncTaskUtils.executeTask(DeleteMediaTask(this))
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
                            val accountKeys = accountsAdapter!!.selectedAccountKeys
                            intent.putExtra(EXTRA_TEXT, ParseUtils.parseString(editText.text))
                            intent.putExtra(EXTRA_ACCOUNT_KEYS, accountKeys)
                            if (accountKeys.size > 0) {
                                val accountKey = accountKeys[0]
                                intent.putExtra(EXTRA_NAME, DataStoreUtils.getAccountName(this, accountKey))
                                intent.putExtra(EXTRA_SCREEN_NAME, DataStoreUtils.getAccountScreenName(this, accountKey))
                            }
                            if (inReplyToStatus != null) {
                                intent.putExtra(EXTRA_IN_REPLY_TO_ID, inReplyToStatus!!.id)
                                intent.putExtra(EXTRA_IN_REPLY_TO_NAME, inReplyToStatus!!.user_name)
                                intent.putExtra(EXTRA_IN_REPLY_TO_SCREEN_NAME, inReplyToStatus!!.user_screen_name)
                            }
                            startActivityForResult(intent, REQUEST_EXTENSION_COMPOSE)
                        } else if (INTENT_ACTION_EXTENSION_EDIT_IMAGE == action) {
                            // final ComponentName cmp = intent.getComponent();
                            // if (cmp == null || !hasMedia()) return false;
                            // final String name = new
                            // File(mMediaUri.getPath()).getName();
                            // final Uri data =
                            // Uri.withAppendedPath(CacheFiles.CONTENT_URI,
                            // Uri.encode(name));
                            // intent.setData(data);
                            // grantUriPermission(cmp.getPackageName(), data,
                            // Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            // startActivityForResult(intent,
                            // REQUEST_EDIT_IMAGE);
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
        mediaPreviewAdapter!!.removeAll(list)
    }

    fun saveToDrafts() {
        val text = editText.text.toString()
        val draft = Draft()

        draft.action_type = getDraftAction(intent.action)
        draft.account_keys = accountsAdapter!!.selectedAccountKeys
        draft.text = text
        val extra = UpdateStatusActionExtra()
        extra.inReplyToStatus = inReplyToStatus
        extra.setIsPossiblySensitive(possiblySensitive)
        draft.action_extras = extra
        draft.media = media
        draft.location = recentLocation
        val values = DraftValuesCreator.create(draft)
        val draftUri = contentResolver.insert(Drafts.CONTENT_URI, values)
        displayNewDraftNotification(text, draftUri)
    }

    fun setSelectedAccounts(vararg accounts: ParcelableAccount) {
        if (accounts.size == 1) {
            accountsCount.setText(null)
            val account = accounts[0]
            mediaLoader.displayProfileImage(accountProfileImage, account)
            accountProfileImage.setBorderColor(account.color)
        } else {
            accountsCount.setText(accounts.size.toString())
            mediaLoader.cancelDisplayTask(accountProfileImage)
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
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mNameFirst = preferences.getBoolean(KEY_NAME_FIRST)
        setContentView(R.layout.activity_compose)
        setFinishOnTouchOutside(false)
        val accounts = DataStoreUtils.getCredentialsArray(this, false, false)
        if (accounts.size <= 0) {
            val intent = Intent(INTENT_ACTION_TWITTER_LOGIN)
            intent.setClass(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        val defaultAccountIds = ParcelableAccountUtils.getAccountKeys(accounts)
        menuBar.setOnMenuItemClickListener(this)
        setupEditText()
        accountSelectorContainer.setOnClickListener(this)
        accountSelectorButton.setOnClickListener(this)
        replyLabel.setOnClickListener(this)
        locationSwitch.max = LOCATION_OPTIONS.size
        val attachLocation = preferences.getBoolean(KEY_ATTACH_LOCATION)
        val attachPreciseLocation = preferences.getBoolean(KEY_ATTACH_PRECISE_LOCATION)
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
                    locationText!!.tag = null
                }
                LOCATION_VALUE_PLACE -> {
                    attachLocationChecked = true
                    attachPreciseLocationChecked = false
                }
            }
            val editor = preferences.edit()
            editor.putBoolean(KEY_ATTACH_LOCATION, attachLocationChecked)
            editor.putBoolean(KEY_ATTACH_PRECISE_LOCATION, attachPreciseLocationChecked)
            editor.apply()
            if (attachLocationChecked) {
                requestOrUpdateLocation()
            } else if (mLocationListener != null) {
                try {
                    locationManager!!.removeUpdates(mLocationListener)
                    mLocationListener = null
                } catch (e: SecurityException) {
                    //Ignore
                }

            }
            updateLocationState()
            setMenu()
            updateTextCount()
        }

        val linearLayoutManager = FixedLinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        linearLayoutManager.stackFromEnd = true
        accountSelector.layoutManager = linearLayoutManager
        accountSelector.itemAnimator = DefaultItemAnimator()
        accountsAdapter = AccountIconsAdapter(this)
        accountSelector.adapter = accountsAdapter
        accountsAdapter!!.setAccounts(accounts)


        val adapter = MediaPreviewAdapter(this, PreviewGridOnStartDragListener(this))
        mediaPreviewAdapter = adapter
        itemTouchHelper = ItemTouchHelper(AttachedMediaItemTouchHelperCallback(adapter))
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        attachedMediaPreview.layoutManager = layoutManager
        attachedMediaPreview.adapter = mediaPreviewAdapter
        registerForContextMenu(attachedMediaPreview)
        itemTouchHelper!!.attachToRecyclerView(attachedMediaPreview)
        val previewGridSpacing = resources.getDimensionPixelSize(R.dimen.element_spacing_small)
        attachedMediaPreview.addItemDecoration(PreviewGridItemDecoration(previewGridSpacing))

        val intent = intent

        if (savedInstanceState != null) {
            // Restore from previous saved state
            val selected = savedInstanceState.getParcelableArray(EXTRA_ACCOUNT_KEYS).toTypedArray(UserKey.CREATOR)
            accountsAdapter!!.setSelectedAccountIds(*selected)
            possiblySensitive = savedInstanceState.getBoolean(EXTRA_IS_POSSIBLY_SENSITIVE)
            val mediaList = savedInstanceState.getParcelableArrayList<ParcelableMediaUpdate>(EXTRA_MEDIA)
            if (mediaList != null) {
                addMedia(mediaList)
            }
            inReplyToStatus = savedInstanceState.getParcelable<ParcelableStatus>(EXTRA_STATUS)
            mentionUser = savedInstanceState.getParcelable<ParcelableUser>(EXTRA_USER)
            draft = savedInstanceState.getParcelable<Draft>(EXTRA_DRAFT)
            shouldSaveAccounts = savedInstanceState.getBoolean(EXTRA_SHOULD_SAVE_ACCOUNTS)
            originalText = savedInstanceState.getString(EXTRA_ORIGINAL_TEXT)
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
            val selectedAccountIds = accountsAdapter!!.selectedAccountKeys
            if (ArrayUtils.isEmpty(selectedAccountIds)) {
                val idsInPrefs: Array<UserKey> = UserKey.arrayOf(preferences.getString(KEY_COMPOSE_ACCOUNTS, null)) ?: emptyArray()
                val intersection: Array<UserKey> = defaultAccountIds.intersect(listOf(*idsInPrefs)).toTypedArray()

                if (intersection.isEmpty()) {
                    accountsAdapter!!.setSelectedAccountIds(*defaultAccountIds)
                } else {
                    accountsAdapter!!.setSelectedAccountIds(*intersection)
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
        setMenu()
        updateLocationState()
        notifyAccountSelectionChanged()

        textChanged = false

        updateAttachedMediaView()
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
        if (v === attachedMediaPreview) {
            menu.setHeaderTitle(R.string.edit_media)
            supportMenuInflater.inflate(R.menu.menu_attached_media_edit, menu)
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuInfo = item.menuInfo
        if (menuInfo is ExtendedRecyclerView.ContextMenuInfo) {
            when (menuInfo.recyclerViewId) {
                R.id.attachedMediaPreview -> {
                    val position = menuInfo.position
                    val mediaUpdate = mediaPreviewAdapter!!.getItem(position)
                    val args = Bundle()
                    args.putString(EXTRA_TEXT, mediaUpdate.alt_text)
                    args.putInt(EXTRA_POSITION, position)
                    val df = EditAltTextDialogFragment()
                    df.arguments = args
                    df.show(supportFragmentManager, "edit_alt_text")
                    return true
                }
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
                        val intent = ThemedImagePickerActivity.withThemed(this@ComposeActivity).getImage(Uri.parse(imageSources[0])).build()
                        startActivityForResult(intent, REQUEST_PICK_IMAGE)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                textChanged = s.length == 0
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
                s.removeSpan(span)
            }
        })
        editText.customSelectionActionModeCallback = this
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val action = handler.getKeyAction(KeyboardShortcutConstants.CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (KeyboardShortcutConstants.ACTION_NAVIGATION_BACK == action) {
            if (editText.length() == 0 && !textChanged) {
                if (!navigateBackPressed) {
                    Toast.makeText(this, getString(R.string.press_again_to_close), Toast.LENGTH_SHORT).show()
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

    private fun addMedia(media: ParcelableMediaUpdate) {
        mediaPreviewAdapter!!.add(media)
        updateAttachedMediaView()
    }

    private fun addMedia(media: List<ParcelableMediaUpdate>) {
        mediaPreviewAdapter!!.addAll(media)
        updateAttachedMediaView()
    }

    private fun clearMedia() {
        mediaPreviewAdapter!!.clear()
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

    private fun createTempImageUri(extraNum: Int): Uri {
        val file = File(cacheDir, "tmp_image_${System.currentTimeMillis()}_$extraNum")
        return Uri.fromFile(file)
    }

    private fun displayNewDraftNotification(text: String, draftUri: Uri) {
        val values = ContentValues()
        values.put(BaseColumns._ID, draftUri.lastPathSegment)
        contentResolver.insert(Drafts.CONTENT_URI_NOTIFICATIONS, values)
    }

    private val media: Array<ParcelableMediaUpdate>
        get() = mediaList.toTypedArray()

    private val mediaList: List<ParcelableMediaUpdate>
        get() = mediaPreviewAdapter!!.asList

    private fun handleDefaultIntent(intent: Intent?): Boolean {
        if (intent == null) return false
        val action = intent.action
        val hasAccountIds: Boolean
        if (intent.hasExtra(EXTRA_ACCOUNT_KEYS)) {
            val accountKeys = intent.getParcelableArrayExtra(EXTRA_ACCOUNT_KEYS).toTypedArray(UserKey.CREATOR)
            accountsAdapter!!.setSelectedAccountIds(*accountKeys)
            hasAccountIds = true
        } else if (intent.hasExtra(EXTRA_ACCOUNT_KEY)) {
            val accountKey = intent.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)
            accountsAdapter!!.setSelectedAccountIds(accountKey)
            hasAccountIds = true
        } else {
            hasAccountIds = false
        }
        if (Intent.ACTION_SEND == action) {
            shouldSaveAccounts = false
            val stream = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (stream != null) {
                val src = arrayOf(stream)
                val dst = arrayOf(createTempImageUri(0))
                AsyncTaskUtils.executeTask(AddMediaTask(this, src, dst, getMediaType(intent.type,
                        ParcelableMedia.Type.IMAGE), false))
            }
        } else if (Intent.ACTION_SEND_MULTIPLE == action) {
            shouldSaveAccounts = false
            val extraStream = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            if (extraStream != null) {
                val src = extraStream.toTypedArray()
                val dst = extraStream.mapIndexed { i, uri -> createTempImageUri(i) }.toTypedArray()
                AsyncTaskUtils.executeTask(AddMediaTask(this, src, dst, getMediaType(intent.type,
                        ParcelableMedia.Type.IMAGE), false))
            }
        } else {
            shouldSaveAccounts = !hasAccountIds
            val data = intent.data
            if (data != null) {
                val src = arrayOf(data)
                val dst = arrayOf(createTempImageUri(0))
                AsyncTaskUtils.executeTask(AddMediaTask(this, src, dst, getMediaType(intent.type,
                        ParcelableMedia.Type.IMAGE), false))
            }
        }
        val extraSubject = intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT)
        val extraText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
        editText.setText(Utils.getShareStatus(this, extraSubject, extraText))
        val selectionEnd = editText.length()
        editText.setSelection(selectionEnd)
        return true
    }

    @ParcelableMedia.Type
    private fun getMediaType(type: String, @ParcelableMedia.Type defType: Int): Int {
        return defType
    }

    private fun handleEditDraftIntent(draft: Draft?): Boolean {
        if (draft == null) return false
        editText.setText(draft.text)
        val selectionEnd = editText.length()
        editText.setSelection(selectionEnd)
        accountsAdapter!!.setSelectedAccountIds(*draft.account_keys ?: emptyArray())
        if (draft.media != null) {
            addMedia(Arrays.asList(*draft.media))
        }
        recentLocation = draft.location
        if (draft.action_extras is UpdateStatusActionExtra) {
            val extra = draft.action_extras as UpdateStatusActionExtra?
            possiblySensitive = extra!!.isPossiblySensitive
            inReplyToStatus = extra.inReplyToStatus
        }
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
                        if (draft.action_extras is UpdateStatusActionExtra) {
                            showReplyLabel((draft.action_extras as UpdateStatusActionExtra).inReplyToStatus)
                        } else {
                            hideLabel()
                            return false
                        }
                    }
                    Draft.Action.QUOTE -> {
                        if (draft.action_extras is UpdateStatusActionExtra) {
                            showQuoteLabel((draft.action_extras as UpdateStatusActionExtra).inReplyToStatus)
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
        mentionUser = intent.getParcelableExtra<ParcelableUser>(EXTRA_USER)
        inReplyToStatus = intent.getParcelableExtra<ParcelableStatus>(EXTRA_STATUS)
        when (action) {
            INTENT_ACTION_REPLY -> {
                return handleReplyIntent(inReplyToStatus)
            }
            INTENT_ACTION_QUOTE -> {
                return handleQuoteIntent(inReplyToStatus)
            }
            INTENT_ACTION_EDIT_DRAFT -> {
                draft = intent.getParcelableExtra<Draft>(EXTRA_DRAFT)
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
                return takePhoto()
            }
            INTENT_ACTION_COMPOSE_PICK_IMAGE -> {
                return pickImage()
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
        accountsAdapter!!.setSelectedAccountIds(user.account_key)
        return true
    }

    private fun handleQuoteIntent(status: ParcelableStatus?): Boolean {
        if (status == null) return false
        editText.setText(Utils.getQuoteStatus(this, status))
        editText.setSelection(0)
        accountsAdapter!!.setSelectedAccountIds(status.account_key)
        showQuoteLabel(status)
        return true
    }

    private fun showQuoteLabel(status: ParcelableStatus?) {
        if (status == null) {
            hideLabel()
            return
        }
        val replyToName = userColorNameManager.getDisplayName(status, mNameFirst)
        replyLabel.text = getString(R.string.quote_name_text, replyToName, status.text_unescaped)
        replyLabel.visibility = View.VISIBLE
        replyLabelDivider!!.visibility = View.VISIBLE
    }

    private fun showReplyLabel(status: ParcelableStatus?) {
        if (status == null) {
            hideLabel()
            return
        }
        val replyToName = userColorNameManager.getDisplayName(status, mNameFirst)
        replyLabel.text = getString(R.string.reply_to_name_text, replyToName, status.text_unescaped)
        replyLabel.visibility = View.VISIBLE
        replyLabelDivider!!.visibility = View.VISIBLE
    }

    private fun hideLabel() {
        replyLabel.visibility = View.GONE
        replyLabelDivider!!.visibility = View.GONE
    }

    private fun handleReplyIntent(status: ParcelableStatus?): Boolean {
        if (status == null) return false
        val account = ParcelableAccountUtils.getAccount(this, status.account_key) ?: return false
        var selectionStart = 0
        val mentions = TreeSet(String.CASE_INSENSITIVE_ORDER)
        editText.append("@" + status.user_screen_name + " ")
        // If replying status from current user, just exclude it's screen name from selection.
        if (status.account_key != status.user_key) {
            selectionStart = editText.length()
        }
        if (status.is_retweet && !TextUtils.isEmpty(status.retweeted_by_user_screen_name)) {
            mentions.add(status.retweeted_by_user_screen_name)
        }
        if (status.is_quote && !TextUtils.isEmpty(status.quoted_user_screen_name)) {
            mentions.add(status.quoted_user_screen_name)
        }
        if (!ArrayUtils.isEmpty(status.mentions)) {
            for (mention in status.mentions) {
                if (mention.key == status.account_key || TextUtils.isEmpty(mention.screen_name)) {
                    continue
                }
                mentions.add(mention.screen_name)
            }
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

        for (mention in mentions) {
            if (mention.equals(status.user_screen_name, ignoreCase = true) || mention.equals(account.screen_name, ignoreCase = true)) {
                continue
            }
            editText.append("@$mention ")
        }
        val selectionEnd = editText.length()
        editText.setSelection(selectionStart, selectionEnd)
        accountsAdapter!!.setSelectedAccountIds(status.account_key)
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
        if (screenNames == null || screenNames.size == 0 || accountId == null) return false
        val myScreenName = DataStoreUtils.getAccountScreenName(this, accountId)
        if (TextUtils.isEmpty(myScreenName)) return false
        for (screenName in screenNames) {
            if (screenName.equals(myScreenName, ignoreCase = true)) {
                continue
            }
            editText.append("@$screenName ")
        }
        editText.setSelection(editText.length())
        accountsAdapter!!.setSelectedAccountIds(accountId)
        this.inReplyToStatus = inReplyToStatus
        return true
    }

    private fun hasMedia(): Boolean {
        return mediaPreviewAdapter!!.itemCount > 0
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
        val accounts = accountsAdapter!!.selectedAccounts
        setSelectedAccounts(*accounts)
        if (ArrayUtils.isEmpty(accounts)) {
            editText.accountKey = Utils.getDefaultAccountKey(this)
        } else {
            editText.accountKey = accounts[0].account_key
        }
        statusTextCount.maxLength = TwidereValidator.getTextLimit(accounts)
        setMenu()
    }

    private fun pickImage(): Boolean {
        val intent = ThemedImagePickerActivity.withThemed(this).pickImage().build()
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
        return true
    }

    private fun saveAccountSelection() {
        if (!shouldSaveAccounts) return
        val editor = preferences.edit()

        editor.putString(KEY_COMPOSE_ACCOUNTS, accountsAdapter!!.selectedAccountKeys.joinToString(","))
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
            val accounts = accountsAdapter!!.selectedAccounts
            if (ArrayUtils.isEmpty(accounts)) return false
            for (account in accounts) {
                if (TwitterContentUtils.getOfficialKeyType(this, account.consumer_key, account.consumer_secret) != ConsumerKeyType.TWEETDECK) {
                    return false
                }
            }
            return true
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionUtils.getPermission(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || PermissionUtils.getPermission(permissions, grantResults, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                startLocationUpdateIfEnabled()
            } catch (e: SecurityException) {
                // That should not happen
            }

        } else {
            Toast.makeText(this, R.string.cannot_get_location, Toast.LENGTH_SHORT).show()
            val editor = preferences.edit()
            editor.putBoolean(KEY_ATTACH_LOCATION, false)
            editor.putBoolean(KEY_ATTACH_PRECISE_LOCATION, false)
            editor.apply()
            locationSwitch.checkedPosition = ArrayUtils.indexOf(LOCATION_OPTIONS,
                    LOCATION_VALUE_NONE)
        }
    }

    private fun setRecentLocation(location: ParcelableLocation?) {
        if (location != null) {
            val attachPreciseLocation = preferences.getBoolean(KEY_ATTACH_PRECISE_LOCATION)
            if (attachPreciseLocation) {
                locationText!!.text = ParcelableLocationUtils.getHumanReadableString(location, 3)
            } else {
                if (locationText!!.tag == null || location != recentLocation) {
                    val task = DisplayPlaceNameTask(this)
                    task.params = location
                    task.callback = locationText
                    TaskStarter.execute(task)
                }
            }
        } else {
            locationText!!.setText(R.string.unknown_location)
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
        if (mLocationListener != null) return true
        val attachLocation = preferences.getBoolean(KEY_ATTACH_LOCATION)
        if (!attachLocation) {
            return false
        }
        val attachPreciseLocation = preferences.getBoolean(KEY_ATTACH_PRECISE_LOCATION)
        val criteria = Criteria()
        if (attachPreciseLocation) {
            criteria.accuracy = Criteria.ACCURACY_FINE
        } else {
            criteria.accuracy = Criteria.ACCURACY_COARSE
        }
        val provider = locationManager!!.getBestProvider(criteria, true)
        if (provider != null) {
            locationText!!.setText(R.string.getting_location)
            mLocationListener = ComposeLocationListener(this)
            locationManager!!.requestLocationUpdates(provider, 0, 0f, mLocationListener)
            val location = Utils.getCachedLocation(this)
            if (location != null) {
                mLocationListener!!.onLocationChanged(location)
            }
        } else {
            Toast.makeText(this, R.string.cannot_get_location, Toast.LENGTH_SHORT).show()
        }
        return provider != null
    }

    private fun takePhoto(): Boolean {
        val intent = ThemedImagePickerActivity.withThemed(this).takePhoto().build()
        startActivityForResult(intent, REQUEST_TAKE_PHOTO)
        return true
    }

    private fun requestOrUpdateLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                startLocationUpdateIfEnabled()
            } catch (e: SecurityException) {
                Toast.makeText(this, R.string.cannot_get_location, Toast.LENGTH_SHORT).show()
            }

        } else {
            val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, REQUEST_REQUEST_PERMISSIONS)
        }
    }

    private fun updateLocationState() {
        val attachLocation = preferences.getBoolean(KEY_ATTACH_LOCATION)
        locationIcon!!.isActivated = attachLocation
        if (!attachLocation) {
            locationText!!.setText(R.string.no_location)
        } else if (recentLocation != null) {
            setRecentLocation(recentLocation)
        } else {
            locationText!!.setText(R.string.getting_location)
        }
    }

    private fun updateStatus() {
        if (isFinishing) return
        val hasMedia = hasMedia()
        val text = if (editText != null) ParseUtils.parseString(editText.text) else null
        val tweetLength = validator.getTweetLength(text)
        val maxLength = statusTextCount.maxLength
        if (accountsAdapter!!.isSelectionEmpty) {
            editText.error = getString(R.string.no_account_selected)
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
        val attachLocation = preferences.getBoolean(KEY_ATTACH_LOCATION)
        val attachPreciseLocation = preferences.getBoolean(KEY_ATTACH_PRECISE_LOCATION)
        val accountKeys = accountsAdapter!!.selectedAccountKeys
        val isPossiblySensitive = hasMedia && possiblySensitive
        val update = ParcelableStatusUpdate()
        @Draft.Action val action: String
        if (draft != null) {
            action = draft!!.action_type
        } else {
            action = getDraftAction(intent.action)
        }
        update.accounts = ParcelableAccountUtils.getAccounts(this, *accountKeys)
        update.text = text
        if (attachLocation) {
            update.location = recentLocation
            update.display_coordinates = attachPreciseLocation
        }
        update.media = media
        update.in_reply_to_status = inReplyToStatus
        update.is_possibly_sensitive = isPossiblySensitive
        BackgroundOperationService.updateStatusesAsync(this, action, update)
        if (preferences.getBoolean(KEY_NO_CLOSE_AFTER_TWEET_SENT, false) && inReplyToStatus == null) {
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
        } else {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun updateTextCount() {
        var text = editText.text?.toString() ?: return
        if (defaultFeatures.isMediaLinkCountsInStatus && media.isNotEmpty()) {
            text += " https://twitter.com/example/status/12345678901234567890/photos/1"
        }
        statusTextCount.textCount = validator.getTweetLength(text)
    }

    override fun getLightToolbarMode(toolbar: Toolbar?): Int {
        return Config.LIGHT_TOOLBAR_AUTO
    }

    override fun getToolbarColor(toolbar: Toolbar?): Int {
        return ATEUtil.resolveColor(this, android.R.attr.panelColorBackground)
    }

    internal class ComposeLocationListener(activity: ComposeActivity) : LocationListener {

        val activityRef: WeakReference<ComposeActivity>

        init {
            activityRef = WeakReference(activity)
        }

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
        val iconView: ShapedImageView
        val nameView: TextView

        init {
            iconView = itemView.findViewById(android.R.id.icon) as ShapedImageView
            nameView = itemView.findViewById(android.R.id.text1) as TextView
            itemView.setOnClickListener(this)
        }

        fun showAccount(adapter: AccountIconsAdapter, account: ParcelableAccount, isSelected: Boolean) {
            itemView.alpha = if (isSelected) 1f else 0.33f
            (itemView as CheckableLinearLayout).isChecked = isSelected
            val loader = adapter.imageLoader
            if (ObjectUtils.notEqual(account, iconView.tag) || iconView.drawable == null) {
                iconView.tag = account
                loader.displayProfileImage(iconView, account)
            }
            iconView.setBorderColor(account.color)
            nameView.text = if (adapter.isNameFirst) account.name else "@" + account.screen_name
        }

        override fun onClick(v: View) {
            (itemView as CheckableLinearLayout).toggle()
            adapter.toggleSelection(layoutPosition)
        }


    }

    internal class AccountIconsAdapter(private val activity: ComposeActivity) : BaseRecyclerViewAdapter<AccountIconViewHolder>(activity) {
        private val mInflater: LayoutInflater
        private val selection: MutableMap<UserKey, Boolean>
        val isNameFirst: Boolean

        private var accounts: Array<ParcelableCredentials>? = null

        init {
            setHasStableIds(true)
            mInflater = activity.layoutInflater
            selection = HashMap<UserKey, Boolean>()
            isNameFirst = preferences.getBoolean(KEY_NAME_FIRST)
        }

        val imageLoader: MediaLoaderWrapper
            get() = mediaLoader

        override fun getItemId(position: Int): Long {
            return accounts!![position].hashCode().toLong()
        }

        val selectedAccountKeys: Array<UserKey>
            get() {
                val accounts = accounts ?: return emptyArray()
                return accounts.filter { selection[it.account_key] ?: false }
                        .map { it.account_key!! }
                        .toTypedArray()
            }

        fun setSelectedAccountIds(vararg accountKeys: UserKey) {
            selection.clear()
            for (accountKey in accountKeys) {
                selection.put(accountKey, true)
            }
            notifyDataSetChanged()
        }

        val selectedAccounts: Array<ParcelableCredentials>
            get() {
                val accounts = accounts ?: return emptyArray()
                return accounts.filter { selection[it.account_key] ?: false }.toTypedArray()
            }

        val isSelectionEmpty: Boolean
            get() = selectedAccountKeys.size == 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountIconViewHolder {
            val view = mInflater.inflate(R.layout.adapter_item_compose_account, parent, false)
            return AccountIconViewHolder(this, view)
        }

        override fun onBindViewHolder(holder: AccountIconViewHolder, position: Int) {
            val account = accounts!![position]
            val isSelected = selection[account.account_key] ?: false
            holder.showAccount(this, account, isSelected)
        }

        override fun getItemCount(): Int {
            return if (accounts != null) accounts!!.size else 0
        }

        fun setAccounts(accounts: Array<ParcelableCredentials>) {
            this.accounts = accounts
            notifyDataSetChanged()
        }

        internal fun toggleSelection(position: Int) {
            if (accounts == null || position < 0) return
            val account = accounts!![position]
            selection.put(account.account_key, java.lang.Boolean.TRUE != selection[account.account_key])
            activity.notifyAccountSelectionChanged()
            notifyDataSetChanged()
        }
    }

    internal class AddMediaTask(activity: ComposeActivity, val sources: Array<Uri>, val mDestinations: Array<Uri>, val mMediaType: Int,
                                val mDeleteSrc: Boolean) : AsyncTask<Any, Any, BooleanArray>() {

        val mActivityRef: WeakReference<ComposeActivity>

        init {
            mActivityRef = WeakReference(activity)
        }

        override fun doInBackground(vararg params: Any): BooleanArray {
            val activity = mActivityRef.get() ?: return BooleanArray(0)
            val result = BooleanArray(sources.size)
            for (i in 0 until sources.size) {
                val source = sources[i]
                val destination = mDestinations[i]
                var st: InputStream? = null
                var os: OutputStream? = null
                try {
                    val resolver = activity.contentResolver
                    st = resolver.openInputStream(source)
                    os = resolver.openOutputStream(destination)
                    if (st == null || os == null) throw FileNotFoundException()
                    StreamUtils.copy(st, os, null, null)
                    if (ContentResolver.SCHEME_FILE == source.scheme && mDeleteSrc) {
                        val file = File(source.path)
                        if (!file.delete()) {
                            Log.d(LOGTAG, String.format("Unable to delete %s", file))
                        }
                    }
                    result[i] = true
                } catch (e: IOException) {
                    if (BuildConfig.DEBUG) {
                        Log.w(LOGTAG, e)
                    }
                    result[i] = false
                } finally {
                    Utils.closeSilently(os)
                    Utils.closeSilently(st)
                }

            }
            return result
        }

        override fun onPostExecute(result: BooleanArray) {
            val activity = mActivityRef.get() ?: return
            activity.setProgressVisible(false)
            for (destination in mDestinations) {
                activity.addMedia(ParcelableMediaUpdate(destination.toString(), mMediaType))
            }
            activity.setMenu()
            activity.updateTextCount()
        }

        override fun onPreExecute() {
            val activity = mActivityRef.get() ?: return
            activity.setProgressVisible(true)
        }
    }

    internal class DeleteMediaTask(activity: ComposeActivity, vararg val media: ParcelableMediaUpdate) : AsyncTask<Any, Any, Boolean>() {

        val activity: WeakReference<ComposeActivity>

        init {
            this.activity = WeakReference(activity)
        }

        override fun doInBackground(vararg params: Any): Boolean {
            try {
                media.forEach {
                    val uri = Uri.parse(it.uri)
                    if (ContentResolver.SCHEME_FILE == uri.scheme) {
                        val file = File(uri.path)
                        if (!file.delete()) {
                            Log.d(LOGTAG, String.format("Unable to delete %s", file))
                        }
                    }
                }
            } catch (e: Exception) {
                return false
            }

            return true
        }

        override fun onPostExecute(result: Boolean) {
            val activity = activity.get() ?: return
            activity.setProgressVisible(false)
            activity.removeAllMedia(Arrays.asList(*media))
            activity.setMenu()
            if (!result) {
                Toast.makeText(activity, R.string.error_occurred, Toast.LENGTH_SHORT).show()
            }
        }

        override fun onPreExecute() {
            val activity = activity.get() ?: return
            activity.setProgressVisible(true)
        }
    }

    internal class DiscardTweetTask(activity: ComposeActivity) : AsyncTask<Any, Any, Unit>() {

        val activityRef: WeakReference<ComposeActivity>
        private val media: List<ParcelableMediaUpdate>

        init {
            this.activityRef = WeakReference(activity)
            this.media = activity.mediaList
        }

        override fun doInBackground(vararg params: Any): Unit {
            for (item in media) {
                val uri = Uri.parse(item.uri)
                if (ContentResolver.SCHEME_FILE == uri.scheme) {
                    val file = File(uri.path)
                    if (!file.delete()) {
                        Log.d(LOGTAG, String.format("Unable to delete %s", file))
                    }
                }
            }
            return Unit
        }

        override fun onPostExecute(result: Unit) {
            val activity = activityRef.get() ?: return
            activity.setProgressVisible(false)
            activity.finish()
        }

        override fun onPreExecute() {
            val activity = activityRef.get() ?: return
            activity.setProgressVisible(true)
        }
    }

    internal class DisplayPlaceNameTask(private val context: ComposeActivity) : AbstractTask<ParcelableLocation, List<Address>, TextView>() {

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
            val attachLocation = preferences.getBoolean(KEY_ATTACH_LOCATION)
            val attachPreciseLocation = preferences.getBoolean(KEY_ATTACH_PRECISE_LOCATION)
            if (attachLocation) {
                if (attachPreciseLocation) {
                    textView.text = ParcelableLocationUtils.getHumanReadableString(location, 3)
                    textView.tag = location
                } else {
                    val tag = textView.tag
                    if (tag is Address) {
                        textView.text = tag.locality
                    } else if (tag is NoAddress) {
                        textView.setText(R.string.your_coarse_location)
                    } else {
                        textView.setText(R.string.getting_location)
                    }
                }
            } else {
                textView.setText(R.string.no_location)
            }
        }

        override fun afterExecute(textView: TextView?, addresses: List<Address>?) {
            val preferences = context.preferences
            val attachLocation = preferences.getBoolean(KEY_ATTACH_LOCATION)
            val attachPreciseLocation = preferences.getBoolean(KEY_ATTACH_PRECISE_LOCATION)
            if (attachLocation) {
                if (attachPreciseLocation) {
                    val location = params
                    textView!!.text = ParcelableLocationUtils.getHumanReadableString(location, 3)
                    textView.tag = location
                } else if (addresses == null || addresses.isEmpty()) {
                    val tag = textView!!.tag
                    if (tag is Address) {
                        textView.text = tag.locality
                    } else {
                        textView.setText(R.string.your_coarse_location)
                        textView.tag = NoAddress()
                    }
                } else {
                    val address = addresses[0]
                    textView!!.tag = address
                    textView.text = address.locality
                }
            } else {
                textView!!.setText(R.string.no_location)
            }
        }

        internal class NoAddress
    }

    internal class MediaPreviewAdapter(val mActivity: ComposeActivity, val mDragStartListener: SimpleItemTouchHelperCallback.OnStartDragListener) : ArrayRecyclerAdapter<ParcelableMediaUpdate, MediaPreviewViewHolder>(mActivity), SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {
        val mInflater: LayoutInflater

        init {
            setHasStableIds(true)
            mInflater = LayoutInflater.from(mActivity)
        }

        fun onStartDrag(viewHolder: ViewHolder) {
            mDragStartListener.onStartDrag(viewHolder)
        }

        val asList: List<ParcelableMediaUpdate>
            get() = Collections.unmodifiableList(data)

        override fun getItemId(position: Int): Long {
            return getItem(position).hashCode().toLong()
        }

        override fun onBindViewHolder(holder: MediaPreviewViewHolder, position: Int, item: ParcelableMediaUpdate) {
            val media = getItem(position)
            holder.displayMedia(this, media)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaPreviewViewHolder {
            val view = mInflater.inflate(R.layout.grid_item_media_editor, parent, false)
            return MediaPreviewViewHolder(view)
        }

        override fun onViewAttachedToWindow(holder: MediaPreviewViewHolder?) {
            super.onViewAttachedToWindow(holder)
            holder!!.adapter = this
        }

        override fun onViewDetachedFromWindow(holder: MediaPreviewViewHolder?) {
            holder!!.adapter = null
            super.onViewDetachedFromWindow(holder)
        }

        override fun remove(position: Int): Boolean {
            val result = super.remove(position)
            if (result) {
                (context as ComposeActivity).updateAttachedMediaView()
            }
            return result
        }

        override fun onItemDismiss(position: Int) {
            // No-op
        }

        override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
            Collections.swap(data, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            return true
        }

        fun setAltText(position: Int, altText: String?) {
            data[position].alt_text = altText
            notifyDataSetChanged()
        }
    }

    internal class MediaPreviewViewHolder(itemView: View) : ViewHolder(itemView), OnLongClickListener, OnClickListener {

        val imageView: ImageView
        val removeView: View
        val editView: View
        var adapter: MediaPreviewAdapter? = null

        init {
            imageView = itemView.findViewById(R.id.image) as ImageView
            removeView = itemView.findViewById(R.id.remove)
            editView = itemView.findViewById(R.id.edit)

            itemView.setOnLongClickListener(this)
            itemView.setOnClickListener(this)
            removeView.setOnClickListener(this)
            editView.setOnClickListener(this)
        }

        fun displayMedia(adapter: MediaPreviewAdapter, media: ParcelableMediaUpdate) {
            adapter.mediaLoader.displayPreviewImage(media.uri, imageView)
        }

        override fun onLongClick(v: View): Boolean {
            if (adapter == null) return false
            adapter!!.onStartDrag(this)
            return false
        }

        override fun onClick(v: View) {
            if (adapter == null) return
            when (v.id) {
                R.id.remove -> {
                    adapter!!.remove(layoutPosition)
                }
                R.id.edit -> {
                    itemView.parent.showContextMenuForChild(itemView)
                }
            }
        }
    }

    class EditAltTextDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.edit_description)
            builder.setView(R.layout.dialog_compose_edit_alt_text)
            builder.setNegativeButton(android.R.string.cancel, null)
            builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                val editText = (dialog as Dialog).findViewById(R.id.edit_text) as EditText
                (activity as ComposeActivity).setMediaAltText(arguments.getInt(EXTRA_POSITION),
                        ParseUtils.parseString(editText.text))
            }
            builder.setNeutralButton(R.string.clear) { dialogInterface, i -> (activity as ComposeActivity).setMediaAltText(arguments.getInt(EXTRA_POSITION), null) }
            val dialog = builder.create()
            dialog.setOnShowListener { dialog ->
                val materialDialog = dialog as Dialog
                val editText = materialDialog.findViewById(R.id.edit_text) as EditText
                editText.setText(arguments.getString(EXTRA_TEXT))
            }
            return dialog
        }
    }

    private fun setMediaAltText(position: Int, altText: String?) {
        mediaPreviewAdapter!!.setAltText(position, altText)
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
            return builder.create()
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

    private class PreviewGridItemDecoration(private val previewGridSpacing: Int) : ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State?) {
            outRect.left = previewGridSpacing
            outRect.right = previewGridSpacing
        }
    }

    private class PreviewGridOnStartDragListener(private val activity: ComposeActivity) : SimpleItemTouchHelperCallback.OnStartDragListener {

        override fun onStartDrag(viewHolder: ViewHolder) {
            val helper = activity.itemTouchHelper ?: return
            helper.startDrag(viewHolder)
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
        private val EXTRA_SHOULD_SAVE_ACCOUNTS = "should_save_accounts"
        private val EXTRA_ORIGINAL_TEXT = "original_text"
        private val DISCARD_STATUS_DIALOG_FRAGMENT_TAG = "discard_status"

        val LOCATION_VALUE_PLACE = "place"
        val LOCATION_VALUE_COORDINATE = "coordinate"
        val LOCATION_VALUE_NONE = "none"

        private val LOCATION_OPTIONS = arrayOf(LOCATION_VALUE_NONE, LOCATION_VALUE_PLACE, LOCATION_VALUE_COORDINATE)

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
