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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.hasRunningLoadersSafe
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_media_viewer.*
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.ktextension.checkAllSelfPermissionsGranted
import org.mariotaku.ktextension.toTypedArray
import org.mariotaku.mediaviewer.library.*
import org.mariotaku.mediaviewer.library.subsampleimageview.SubsampleImageViewerFragment.EXTRA_MEDIA_URI
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.iface.IExtendedActivity
import org.mariotaku.twidere.fragment.*
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.provider.CacheProvider
import org.mariotaku.twidere.provider.ShareProvider
import org.mariotaku.twidere.task.SaveFileTask
import org.mariotaku.twidere.task.SaveMediaToGalleryTask
import org.mariotaku.twidere.util.AsyncTaskUtils
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.MenuUtils
import org.mariotaku.twidere.util.PermissionUtils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import java.io.File
import javax.inject.Inject
import android.Manifest.permission as AndroidPermissions

class MediaViewerActivity : BaseActivity(), IExtendedActivity, IMediaViewerActivity {

    @Inject
    lateinit var mFileCache: FileCache
    @Inject
    lateinit var mMediaDownloader: MediaDownloader

    private var saveToStoragePosition = -1
    private var shareMediaPosition = -1

    private lateinit var mediaViewerHelper: IMediaViewerActivity.Helper

    private val status: ParcelableStatus?
        get() = intent.getParcelableExtra<ParcelableStatus>(EXTRA_STATUS)

    private val initialMedia: ParcelableMedia?
        get() = intent.getParcelableExtra<ParcelableMedia>(EXTRA_CURRENT_MEDIA)

    private val media: Array<ParcelableMedia> by lazy {
        intent.getParcelableArrayExtra(EXTRA_MEDIA).toTypedArray(ParcelableMedia.CREATOR)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeneralComponentHelper.build(this).inject(this)
        mediaViewerHelper = IMediaViewerActivity.Helper(this)
        mediaViewerHelper.onCreate(savedInstanceState)
        val actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SHARE_MEDIA -> {
                ShareProvider.clearTempFiles(this)
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
        val viewPager = findViewPager()
        val adapter = viewPager.adapter
        val currentItem = viewPager.currentItem
        if (currentItem < 0 || currentItem >= adapter.count) return false
        val obj = adapter.instantiateItem(viewPager, currentItem) as? MediaViewerFragment ?: return false
        if (obj is CacheDownloadMediaViewerFragment) {
            val running = obj.loaderManager.hasRunningLoadersSafe()
            val downloaded = obj.hasDownloadedData()
            MenuUtils.setItemAvailability(menu, R.id.refresh, !running && !downloaded)
            MenuUtils.setItemAvailability(menu, R.id.share, !running && downloaded)
            MenuUtils.setItemAvailability(menu, R.id.save, !running && downloaded)
        } else {
            MenuUtils.setItemAvailability(menu, R.id.refresh, false)
            MenuUtils.setItemAvailability(menu, R.id.share, true)
            MenuUtils.setItemAvailability(menu, R.id.save, false)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val viewPager = findViewPager()
        val adapter = viewPager.adapter
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
                if (obj is CacheDownloadMediaViewerFragment) {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_SAVE_MEDIA -> {
                if (PermissionUtils.hasPermission(permissions, grantResults, AndroidPermissions.WRITE_EXTERNAL_STORAGE)) {
                    saveToStorage()
                } else {
                    Toast.makeText(this, R.string.save_media_no_storage_permission_message, Toast.LENGTH_LONG).show()
                }
                return
            }
            REQUEST_PERMISSION_SHARE_MEDIA -> {
                if (!PermissionUtils.hasPermission(permissions, grantResults, AndroidPermissions.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, R.string.share_media_no_storage_permission_message, Toast.LENGTH_LONG).show()
                }
                shareMedia()
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
        val actionBar = supportActionBar
        return actionBar != null && actionBar.isShowing
    }

    override fun setBarVisibility(visible: Boolean) {
        val actionBar = supportActionBar ?: return
        if (visible) {
            actionBar.show()
        } else {
            actionBar.hide()
        }
    }

    override fun getDownloader(): MediaDownloader {
        return mMediaDownloader
    }

    override fun getFileCache(): FileCache {
        return mFileCache
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
                if (mediaUrl.endsWith(".gif")) {
                    return Fragment.instantiate(this, GifPageFragment::class.java.name, args) as MediaViewerFragment
                } else {
                    return Fragment.instantiate(this, ImagePageFragment::class.java.name, args) as MediaViewerFragment
                }
            }
            ParcelableMedia.Type.ANIMATED_GIF, ParcelableMedia.Type.CARD_ANIMATED_GIF -> {
                args.putBoolean(VideoPageFragment.EXTRA_LOOP, true)
                return Fragment.instantiate(this, VideoPageFragment::class.java.name, args) as MediaViewerFragment
            }
            ParcelableMedia.Type.VIDEO -> {
                return Fragment.instantiate(this,
                        VideoPageFragment::class.java.name, args) as MediaViewerFragment
            }
            ParcelableMedia.Type.EXTERNAL_PLAYER -> {
                return Fragment.instantiate(this, ExternalBrowserPageFragment::class.java.name, args) as MediaViewerFragment
            }
        }
        throw UnsupportedOperationException(media.toString())
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
            val permissions: Array<String>
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                permissions = arrayOf(AndroidPermissions.WRITE_EXTERNAL_STORAGE, AndroidPermissions.READ_EXTERNAL_STORAGE)
            } else {
                permissions = arrayOf(AndroidPermissions.WRITE_EXTERNAL_STORAGE)
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
            val permissions: Array<String>
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                permissions = arrayOf(AndroidPermissions.WRITE_EXTERNAL_STORAGE, AndroidPermissions.READ_EXTERNAL_STORAGE)
            } else {
                permissions = arrayOf(AndroidPermissions.WRITE_EXTERNAL_STORAGE)
            }
            PermissionRequestDialog.show(supportFragmentManager, getString(R.string.message_permission_request_share_media),
                    permissions, REQUEST_PERMISSION_SHARE_MEDIA)
        }
    }

    private fun shareMedia() {
        if (shareMediaPosition == -1) return
        val viewPager = findViewPager()
        val adapter = viewPager.adapter
        val f = adapter.instantiateItem(viewPager, shareMediaPosition) as? CacheDownloadMediaViewerFragment ?: return
        val cacheUri = f.downloadResult?.cacheUri ?: return
        val destination = ShareProvider.getFilesDir(this) ?: return
        val type: String
        when (f) {
            is VideoPageFragment -> type = CacheProvider.Type.VIDEO
            is ImagePageFragment -> type = CacheProvider.Type.IMAGE
            is GifPageFragment -> type = CacheProvider.Type.IMAGE
            else -> throw UnsupportedOperationException("Unsupported fragment $f")
        }
        val task = object : SaveFileTask(this@MediaViewerActivity, cacheUri, destination,
                CacheProvider.CacheFileTypeCallback(this@MediaViewerActivity, type)) {
            private val PROGRESS_FRAGMENT_TAG = "progress"

            override fun dismissProgress() {
                val activity = context as IExtendedActivity
                activity.executeAfterFragmentResumed { activity ->
                    val fm = (activity as FragmentActivity).supportFragmentManager
                    val fragment = fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG) as? DialogFragment
                    fragment?.dismiss()
                }
            }

            override fun showProgress() {
                val activity = context as IExtendedActivity
                activity.executeAfterFragmentResumed { activity ->
                    val fragment = ProgressDialogFragment()
                    fragment.isCancelable = false
                    fragment.show((activity as FragmentActivity).supportFragmentManager, PROGRESS_FRAGMENT_TAG)
                }
            }

            override fun onFileSaved(savedFile: File, mimeType: String?) {
                val activity = context as MediaViewerActivity

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
                startActivityForResult(Intent.createChooser(intent, activity.getString(R.string.action_share)),
                        REQUEST_SHARE_MEDIA)
            }

            override fun onFileSaveFailed() {
                val activity = context as MediaViewerActivity
                Toast.makeText(activity, R.string.error_occurred, Toast.LENGTH_SHORT).show()
            }
        }
        AsyncTaskUtils.executeTask(task)
    }

    private fun saveToStorage() {
        if (saveToStoragePosition == -1) return
        val viewPager = findViewPager()
        val adapter = viewPager.adapter
        val f = adapter.instantiateItem(viewPager, saveToStoragePosition) as? CacheDownloadMediaViewerFragment ?: return
        val cacheUri = f.downloadResult?.cacheUri ?: return
        val task: SaveFileTask = when (f) {
            is ImagePageFragment -> SaveMediaToGalleryTask.create(this, cacheUri, CacheProvider.Type.IMAGE)
            is VideoPageFragment -> SaveMediaToGalleryTask.create(this, cacheUri, CacheProvider.Type.VIDEO)
            is GifPageFragment -> SaveMediaToGalleryTask.create(this, cacheUri, CacheProvider.Type.IMAGE)
            else -> throw UnsupportedOperationException()
        }
        AsyncTaskUtils.executeTask(task)
    }

    companion object {

        private val REQUEST_SHARE_MEDIA = 201
        private val REQUEST_PERMISSION_SAVE_MEDIA = 202
        private val REQUEST_PERMISSION_SHARE_MEDIA = 203
    }
}
