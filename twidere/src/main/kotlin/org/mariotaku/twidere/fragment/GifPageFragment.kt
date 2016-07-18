package org.mariotaku.twidere.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.MediaDownloadEvent
import kotlinx.android.synthetic.main.layout_media_viewer_gif.*
import org.mariotaku.mediaviewer.library.CacheDownloadLoader
import org.mariotaku.mediaviewer.library.CacheDownloadMediaViewerFragment
import org.mariotaku.mediaviewer.library.subsampleimageview.SubsampleImageViewerFragment
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.TwidereConstants.EXTRA_MEDIA
import org.mariotaku.twidere.activity.MediaViewerActivity
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.UserKey
import pl.droidsonroids.gif.InputSource

class GifPageFragment : CacheDownloadMediaViewerFragment() {

    private var mediaDownloadEvent: MediaDownloadEvent? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        gifView.setOnClickListener { (activity as MediaViewerActivity).toggleBar() }
        startLoading(false)
    }

    override fun getDownloadUri(): Uri? {
        return arguments.getParcelable<Uri>(SubsampleImageViewerFragment.EXTRA_MEDIA_URI)
    }

    override fun getDownloadExtra(): Any? {
        return null
    }

    override fun displayMedia(result: CacheDownloadLoader.Result) {
        val context = context ?: return
        if (result.cacheUri != null) {
            gifView.setInputSource(InputSource.UriSource(context.contentResolver, result.cacheUri!!))
        } else {
            gifView.setInputSource(null)
        }
    }

    override fun isAbleToLoad(): Boolean {
        return downloadUri != null
    }

    override fun onCreateMediaView(inflater: LayoutInflater, parent: ViewGroup, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.layout_media_viewer_gif, parent, false)
    }

    override fun recycleMedia() {
        gifView?.setInputSource(null)
    }

    private val media: ParcelableMedia
        get() = arguments.getParcelable<ParcelableMedia>(EXTRA_MEDIA)

    private val accountKey: UserKey
        get() = arguments.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)

    override fun onDownloadRequested(nonce: Long) {
        super.onDownloadRequested(nonce)
        val context = context
        if (context != null) {
            mediaDownloadEvent = MediaDownloadEvent.create(context, media, nonce)
        } else {
            mediaDownloadEvent = null
        }
    }

    override fun onDownloadStart(total: Long, nonce: Long) {
        super.onDownloadStart(total, nonce)
        if (mediaDownloadEvent != null && mediaDownloadEvent!!.nonce == nonce) {
            mediaDownloadEvent!!.setOpenedTime(System.currentTimeMillis())
            mediaDownloadEvent!!.setSize(total)
        }
    }

    override fun onDownloadFinished(nonce: Long) {
        super.onDownloadFinished(nonce)
        if (mediaDownloadEvent != null && mediaDownloadEvent!!.nonce == nonce) {
            mediaDownloadEvent!!.markEnd()
            HotMobiLogger.getInstance(context).log<MediaDownloadEvent>(accountKey, mediaDownloadEvent)
            mediaDownloadEvent = null
        }
    }
}