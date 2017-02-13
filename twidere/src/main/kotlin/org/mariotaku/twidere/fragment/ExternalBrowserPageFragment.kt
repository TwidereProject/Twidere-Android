package org.mariotaku.twidere.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.layout_media_viewer_browser_fragment.*
import kotlinx.android.synthetic.main.layout_media_viewer_texture_video_view.*
import org.mariotaku.mediaviewer.library.MediaViewerFragment
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.EXTRA_MEDIA
import org.mariotaku.twidere.model.ParcelableMedia

class ExternalBrowserPageFragment : MediaViewerFragment() {

    override fun onCreateMediaView(inflater: LayoutInflater, parent: ViewGroup,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.layout_media_viewer_browser_fragment, parent, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.loadsImagesAutomatically = true
        val media = arguments.getParcelable<ParcelableMedia>(EXTRA_MEDIA) ?: throw NullPointerException()
        webView.loadUrl(if (TextUtils.isEmpty(media.media_url)) media.url else media.media_url)
        videoContainer.setAspectRatioSource(VideoPageFragment.MediaAspectRatioSource(media, this))
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }


    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        webView?.destroy()
        super.onDestroy()
    }

    override fun recycleMedia() {

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            activity?.supportInvalidateOptionsMenu()
        }
    }
}