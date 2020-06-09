/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import androidx.core.view.WindowInsetsCompat
import androidx.customview.widget.ViewDragHelper
import androidx.appcompat.app.WindowDecorActionBar
import androidx.appcompat.app.decorToolbar
import android.view.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_media_viewer.*
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.ktextension.*
import org.mariotaku.mediaviewer.library.*
import org.mariotaku.mediaviewer.library.subsampleimageview.SubsampleImageViewerFragment.EXTRA_MEDIA_URI
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.iface.IControlBarActivity.ControlBarShowHideHelper
import org.mariotaku.twidere.annotation.CacheFileType
import org.mariotaku.twidere.extension.addSystemUiVisibility
import org.mariotaku.twidere.extension.dismissProgressDialog
import org.mariotaku.twidere.extension.removeSystemUiVisibility
import org.mariotaku.twidere.extension.showProgressDialog
import org.mariotaku.twidere.fragment.PermissionRequestDialog
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.fragment.iface.IBaseFragment
import org.mariotaku.twidere.fragment.media.*
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.provider.CacheProvider
import org.mariotaku.twidere.provider.ShareProvider
import org.mariotaku.twidere.task.SaveFileTask
import org.mariotaku.twidere.task.SaveMediaToGalleryTask
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.PermissionUtils
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.support.WindowSupport
import org.mariotaku.twidere.view.viewer.MediaSwipeCloseContainer
import java.io.File
import javax.inject.Inject
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.math.roundToInt
import android.Manifest.permission as AndroidPermissions

class MediaViewerActivity : BaseActivity(), IMediaViewerActivity, MediaSwipeCloseContainer.Listener,
        PermissionRequestDialog.PermissionRequestCancelCallback {
    @Inject
    internal lateinit var mediaFileCache: FileCache
    @Inject
    internal lateinit var mediaDownloader: MediaDownloader

    private var saveToStoragePosition = -1

    private var shareMediaPosition = -1
    private var wasBarShowing = 0
    private var hideOffsetNotSupported = false
    private lateinit var mediaViewerHelper: IMediaViewerActivity.Helper
    private lateinit var controlBarShowHideHelper: ControlBarShowHideHelper

    private val status: ParcelableStatus?
        get() = intent.getParcelableExtra<ParcelableStatus>(EXTRA_STATUS)

    private val initialMedia: ParcelableMedia?
        get() = intent.getParcelableExtra<ParcelableMedia>(EXTRA_CURRENT_MEDIA)

    private val media: Array<ParcelableMedia> by lazy {
        return@lazy intent.getNullableTypedArrayExtra<ParcelableMedia>(EXTRA_MEDIA) ?: emptyArray()
    }

    private val currentFragment: MediaViewerFragment?
        get() {
            val viewPager = findViewPager()
            val adapter = viewPager.adapter ?: return null
            val currentItem = viewPager.currentItem
            if (currentItem < 0 || currentItem >= adapter.count) return null
            return adapter.instantiateItem(viewPager, currentItem) as? MediaViewerFragment
        }

    private fun getCurrentCacheFileInfo(position: Int): SaveFileTask.FileInfo? {
        if (position == -1) return null
        val viewPager = findViewPager()
        val adapter = viewPager.adapter ?: return null
        val f = adapter.instantiateItem(viewPager, position) as? MediaViewerFragment ?:
                return null
        return f.cacheFileInfo()
    }


    override val shouldApplyWindowBackground: Boolean = false

    override val controlBarHeight: Int
        get() {
            return supportActionBar?.height ?: 0
        }

    override var controlBarOffset: Float
        get() {
            val actionBar = supportActionBar
            if (actionBar != null) {
                return 1 - actionBar.hideOffset / controlBarHeight.toFloat()
            }
            return 0f
        }
        @SuppressLint("RestrictedApi")
        set(offset) {
            val actionBar = supportActionBar
            if (actionBar != null && !hideOffsetNotSupported) {
                if (actionBar is WindowDecorActionBar) {
                    val toolbar = actionBar.decorToolbar.viewGroup
                    toolbar.alpha = offset
                    activityLayout.statusBarAlpha = offset
                }
                try {
                    actionBar.hideOffset = (controlBarHeight * (1f - offset)).roundToInt()
                } catch (e: UnsupportedOperationException) {
                    // Some device will throw this exception
                    hideOffsetNotSupported = true
                }
            }
            notifyControlBarOffsetChanged()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
        super.onCreate(savedInstanceState)
        GeneralComponent.get(this).inject(this)
        mediaViewerHelper = IMediaViewerActivity.Helper(this)
        controlBarShowHideHelper = ControlBarShowHideHelper(this)
        mediaViewerHelper.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.elevation = 0f
        swipeContainer.listener = this
        swipeContainer.backgroundAlpha = 1f
        WindowSupport.setStatusBarColor(window, Color.TRANSPARENT)
        activityLayout.statusBarColor = overrideTheme.colorToolbar
        ViewCompat.setOnApplyWindowInsetsListener(activityLayout) { view, insets ->
            val statusBarHeight = insets.systemWindowInsetTop - ThemeUtils.getActionBarHeight(this)
            activityLayout.statusBarHeight = statusBarHeight
            onApplyWindowInsets(view, insets)
        }
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            activityLayout.statusBarAlpha = if (View.SYSTEM_UI_FLAG_FULLSCREEN in visibility) 0f else 1f
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SHARE_MEDIA -> {
                ShareProvider.clearTempFiles(this)
            }
            REQUEST_SELECT_SAVE_MEDIA -> {
                if (resultCode != Activity.RESULT_OK || data == null) return
                val uri = data.data ?: return
                saveMediaToContentUri(uri)
            }
        }
    }


    override fun onContentChanged() {
        super.onContentChanged()
        mediaViewerHelper.onContentChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_media_viewer, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        val obj = currentFragment ?: return false
        if (obj.isDetached || obj.host == null) return false
        val running = obj.isMediaLoading
        val downloaded = obj.isMediaLoaded
        val supportedSaveTo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        menu.setItemAvailability(R.id.refresh, !running && !downloaded)
        menu.setItemAvailability(R.id.share, !running && downloaded)
        menu.setItemAvailability(R.id.save, !running && downloaded)
        menu.setItemAvailability(R.id.save_to, supportedSaveTo && !running && downloaded)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val viewPager = findViewPager()
        val adapter = viewPager.adapter ?: return false
        val currentItem = viewPager.currentItem
        if (currentItem < 0 || currentItem >= adapter.count) return false
        val obj = adapter.instantiateItem(viewPager, currentItem) as? MediaViewerFragment ?: return false
        when (item.itemId) {
            R.id.refresh -> {
                if (obj is CacheDownloadMediaViewerFragment) {
                    obj.startLoading(true)
                    obj.showProgress(true, 0f)
                    obj.setMediaViewVisible(false)
                }
                return true
            }
            R.id.share -> {
                val fileInfo = obj.cacheFileInfo()
                if (fileInfo != null) {
                    requestAndShareMedia(currentItem)
                } else {
                    val media = media[currentItem]
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_TEXT, media.url)
                    startActivity(Intent.createChooser(intent, getString(R.string.action_share)))
                }
                return true
            }
            R.id.save -> {
                requestAndSaveToStorage(currentItem)
                return true
            }
            R.id.save_to -> {
                openSaveToDocumentChooser()
                return true
            }
            R.id.open_in_browser -> {
                val media = media[currentItem]
                try {
                    val uri = Uri.parse(media.url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.addCategory(Intent.CATEGORY_BROWSABLE)
                    intent.`package` = IntentUtils.getDefaultBrowserPackage(this, uri, true)
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // TODO show error, or improve app url
                }

                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionCancelled(requestCode: Int) {
        when (requestCode) {
            REQUEST_PERMISSION_SHARE_MEDIA -> {
                shareMedia()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_SAVE_MEDIA -> {
                if (PermissionUtils.hasPermission(permissions, grantResults, AndroidPermissions.WRITE_EXTERNAL_STORAGE)) {
                    saveToStorage()
                } else {
                    Toast.makeText(this, R.string.message_toast_save_media_no_storage_permission, Toast.LENGTH_LONG).show()
                }
                return
            }
            REQUEST_PERMISSION_SHARE_MEDIA -> {
                if (!PermissionUtils.hasPermission(permissions, grantResults, AndroidPermissions.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.message_toast_share_media_no_storage_permission, Toast.LENGTH_LONG).show()
                }
                shareMedia()
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        currentFragment
        return super.onKeyUp(keyCode, event)
    }

    override fun toggleBar() {
        setBarVisibility(!isBarShowing)
    }

    override fun getInitialPosition(): Int {
        return media.indexOf(initialMedia)
    }

    override fun getLayoutRes(): Int {
        return R.layout.activity_media_viewer
    }

    override fun findViewPager(): ViewPager {
        return viewPager
    }

    override fun isBarShowing(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return FLAG_SYSTEM_UI_HIDE_BARS !in window.decorView.systemUiVisibility
        }
        return controlBarOffset >= 1
    }

    override fun setBarVisibility(visible: Boolean) {
        if (isBarShowing == visible) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (visible) {
                window.decorView.removeSystemUiVisibility(FLAG_SYSTEM_UI_HIDE_BARS)
            } else {
                window.decorView.addSystemUiVisibility(FLAG_SYSTEM_UI_HIDE_BARS)
            }
        } else {
            setControlBarVisibleAnimate(visible)
        }
    }

    override fun getDownloader(): MediaDownloader {
        return mediaDownloader
    }

    override fun getFileCache(): FileCache {
        return mediaFileCache
    }

    @SuppressLint("SwitchIntDef")
    override fun instantiateMediaFragment(position: Int): MediaViewerFragment {
        val media = media[position]
        val args = Bundle()
        val intent = intent
        args.putParcelable(EXTRA_ACCOUNT_KEY, intent.getParcelableExtra<Parcelable>(EXTRA_ACCOUNT_KEY))
        args.putParcelable(EXTRA_MEDIA, media)
        args.putParcelable(EXTRA_STATUS, intent.getParcelableExtra<Parcelable>(EXTRA_STATUS))
        when (media.type) {
            ParcelableMedia.Type.IMAGE -> {
                val mediaUrl = media.media_url ?: return Fragment.instantiate(this, ExternalBrowserPageFragment::class.java.name, args) as MediaViewerFragment
                args.putParcelable(EXTRA_MEDIA_URI, Uri.parse(mediaUrl))
                return if (mediaUrl.endsWith(".gif")) {
                    Fragment.instantiate(this, GifPageFragment::class.java.name, args) as MediaViewerFragment
                } else {
                    Fragment.instantiate(this, ImagePageFragment::class.java.name, args) as MediaViewerFragment
                }
            }
            ParcelableMedia.Type.ANIMATED_GIF, ParcelableMedia.Type.CARD_ANIMATED_GIF -> {
                args.putBoolean(VideoPageFragment.EXTRA_LOOP, true)
                args.putBoolean(VideoPageFragment.EXTRA_DISABLE_CONTROL, true)
                args.putBoolean(VideoPageFragment.EXTRA_DEFAULT_MUTE, true)
                return instantiateMediaViewerFragment(args)
            }
            ParcelableMedia.Type.VIDEO -> {
                return instantiateMediaViewerFragment(args)
            }
            ParcelableMedia.Type.EXTERNAL_PLAYER -> {
                return Fragment.instantiate(this, ExternalBrowserPageFragment::class.java.name, args) as MediaViewerFragment
            }
            else -> {
                return Fragment.instantiate(this, ExternalBrowserPageFragment::class.java.name, args) as MediaViewerFragment
            }
        }
    }

    override fun getMediaCount(): Int {
        return media.size
    }

    override fun getOverrideTheme(): Chameleon.Theme {
        val theme = super.getOverrideTheme()
        theme.colorToolbar = ContextCompat.getColor(this, R.color.ab_bg_color_media_viewer)
        theme.isToolbarColored = false
        return theme
    }

    override fun onSwipeCloseFinished() {
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onSwipeOffsetChanged(offset: Int) {
        val offsetFactor = 1 - (abs(offset).toFloat() / swipeContainer.height)
        swipeContainer.backgroundAlpha = offsetFactor
        val colorToolbar = overrideTheme.colorToolbar
        val alpha = (Color.alpha(colorToolbar) * offsetFactor).roundToInt().coerceIn(0..255)
        activityLayout.statusBarAlpha = alpha / 255f
    }

    override fun onSwipeStateChanged(state: Int) {
        supportActionBar?.let {
            val barShowing = controlBarOffset >= 1
            if (state == ViewDragHelper.STATE_IDLE) {
                if (wasBarShowing == 1 && !barShowing) {
                    setControlBarVisibleAnimate(true)
                }
                wasBarShowing = 0
            } else {
                if (wasBarShowing == 0) {
                    wasBarShowing = if (barShowing) 1 else -1
                }
                if (barShowing) {
                    setControlBarVisibleAnimate(false)
                }
            }
        }
    }

    override fun setControlBarVisibleAnimate(visible: Boolean, listener: ControlBarShowHideHelper.ControlBarAnimationListener?) {
        controlBarShowHideHelper.setControlBarVisibleAnimate(visible, listener)
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        val result = super.onApplyWindowInsets(v, insets)
        val adapter = viewPager.adapter ?: return insets
        if (adapter.count == 0) return insets
        val fragment = adapter.instantiateItem(viewPager, viewPager.currentItem)
        if (fragment is IBaseFragment<*>) {
            fragment.requestApplyInsets()
        }
        return result
    }

    private fun instantiateMediaViewerFragment(args: Bundle): MediaViewerFragment {
        return Fragment.instantiate(this, ExoPlayerPageFragment::class.java.name, args) as MediaViewerFragment
    }

    private fun processShareIntent(intent: Intent) {
        val status = status ?: return
        intent.putExtra(Intent.EXTRA_SUBJECT, IntentUtils.getStatusShareSubject(this, status))
        intent.putExtra(Intent.EXTRA_TEXT, IntentUtils.getStatusShareText(this, status))
    }

    private fun requestAndSaveToStorage(position: Int) {
        saveToStoragePosition = position
        if (checkAllSelfPermissionsGranted(AndroidPermissions.WRITE_EXTERNAL_STORAGE)) {
            saveToStorage()
        } else {
            val permissions: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                arrayOf(AndroidPermissions.WRITE_EXTERNAL_STORAGE, AndroidPermissions.READ_EXTERNAL_STORAGE)
            } else {
                arrayOf(AndroidPermissions.WRITE_EXTERNAL_STORAGE)
            }
            PermissionRequestDialog.show(supportFragmentManager, getString(R.string.message_permission_request_save_media),
                    permissions, REQUEST_PERMISSION_SAVE_MEDIA)
        }
    }

    private fun requestAndShareMedia(position: Int) {
        shareMediaPosition = position
        if (checkAllSelfPermissionsGranted(AndroidPermissions.WRITE_EXTERNAL_STORAGE)) {
            shareMedia()
        } else {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                arrayOf(AndroidPermissions.WRITE_EXTERNAL_STORAGE, AndroidPermissions.READ_EXTERNAL_STORAGE)
            } else {
                arrayOf(AndroidPermissions.WRITE_EXTERNAL_STORAGE)
            }
            PermissionRequestDialog.show(supportFragmentManager, getString(R.string.message_permission_request_share_media),
                    permissions, REQUEST_PERMISSION_SHARE_MEDIA)
        }
    }

    private fun shareMedia() {
        val fileInfo = getCurrentCacheFileInfo(shareMediaPosition) ?: return
        val destination = ShareProvider.getFilesDir(this) ?: return
        val task = SaveMediaTask(this, destination, fileInfo)
        task.execute()
    }

    private fun saveToStorage() {
        val fileInfo = getCurrentCacheFileInfo(saveToStoragePosition) ?: return
        val pubDir = when ((fileInfo as? CacheProvider.CacheFileTypeSupport)?.cacheFileType) {
            CacheFileType.VIDEO -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                } else {
                    getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                }
            }
            CacheFileType.IMAGE -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                } else {
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                }
            }
            else -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                } else {
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                }
            }
        }
        val saveDir = File(pubDir, "Twidere")
        val task = SaveMediaToGalleryTask(this, fileInfo, saveDir)
        task.execute()
    }

    private fun openSaveToDocumentChooser() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return
        val fileInfo = getCurrentCacheFileInfo(viewPager.currentItem) ?: return
        thread {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.type = fileInfo.mimeType ?: "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            val extension = fileInfo.fileExtension
            val saveFileName = if (extension != null) {
                "${fileInfo.fileName?.removeSuffix("_$extension")}.$extension"
            } else {
                fileInfo.fileName
            }
            intent.putExtra(Intent.EXTRA_TITLE, saveFileName)
            startActivityForResult(intent, REQUEST_SELECT_SAVE_MEDIA)
        }
    }

    private fun saveMediaToContentUri(data: Uri) {
        val fileInfo = getCurrentCacheFileInfo(viewPager.currentItem) ?: return
        val weakThis = weak()
        (showProgressDialog("save_media_to_progress") and task {
            val a = weakThis.get() ?: throw InterruptedException()
            fileInfo.inputStream().use { st ->
                a.contentResolver.openOutputStream(data)?.use {
                    st.copyTo(it)
                }
            }
        }).successUi {
            val a = weakThis.get() ?: return@successUi
            Toast.makeText(a, R.string.message_toast_media_saved, Toast.LENGTH_SHORT).show()
        }.alwaysUi {
            val a = weakThis.get() ?: return@alwaysUi
            a.dismissProgressDialog("save_media_to_progress")
        }
    }

    private fun MediaViewerFragment.cacheFileInfo(): SaveFileTask.FileInfo? {
        when (this) {
            is CacheDownloadMediaViewerFragment -> {
                val cacheUri = downloadResult?.cacheUri ?: return null
                val type = when (this) {
                    is ImagePageFragment -> CacheFileType.IMAGE
                    is VideoPageFragment -> CacheFileType.VIDEO
                    is GifPageFragment -> CacheFileType.IMAGE
                    else -> return null
                }
                return activity?.let {
                    CacheProvider.ContentUriFileInfo(it, cacheUri, type)
                }
            }
            is ExoPlayerPageFragment -> {
                return getRequestFileInfo()
            }
            else -> return null
        }
    }

    class SaveMediaTask(activity: MediaViewerActivity, destination: File, fileInfo: FileInfo) :
            SaveFileTask(activity, destination, fileInfo) {
        private val PROGRESS_FRAGMENT_TAG = "progress"

        override fun dismissProgress() {
            val activity = context as? MediaViewerActivity ?: return

            activity.executeAfterFragmentResumed {
                val fm = it.supportFragmentManager
                val fragment = fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG) as? DialogFragment
                fragment?.dismiss()
            }
        }

        override fun showProgress() {
            val activity = context as? MediaViewerActivity ?: return

            activity.executeAfterFragmentResumed {
                val fragment = ProgressDialogFragment()
                fragment.isCancelable = false
                fragment.show(it.supportFragmentManager, PROGRESS_FRAGMENT_TAG)
            }
        }

        override fun onFileSaved(savedFile: File, mimeType: String?) {
            val activity = context as? MediaViewerActivity ?: return

            val fileUri = ShareProvider.getUriForFile(activity, AUTHORITY_TWIDERE_SHARE,
                    savedFile)

            val intent = Intent(Intent.ACTION_SEND)
            intent.setDataAndType(fileUri, mimeType)
            intent.putExtra(Intent.EXTRA_STREAM, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
            }
            activity.processShareIntent(intent)
            activity.startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.action_share)),
                    REQUEST_SHARE_MEDIA)
        }

        override fun onFileSaveFailed() {
            val activity = context as? MediaViewerActivity ?: return
            Toast.makeText(activity, R.string.message_toast_error_occurred, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        private const val REQUEST_SHARE_MEDIA = 201
        private const val REQUEST_PERMISSION_SAVE_MEDIA = 202
        private const val REQUEST_PERMISSION_SHARE_MEDIA = 203
        private const val REQUEST_SELECT_SAVE_MEDIA = 204

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        const val FLAG_SYSTEM_UI_HIDE_BARS = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    interface Media
}

