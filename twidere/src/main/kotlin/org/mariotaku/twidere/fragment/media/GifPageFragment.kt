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

package org.mariotaku.twidere.fragment.media

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private val media: ParcelableMedia
        get() = arguments?.getParcelable(EXTRA_MEDIA)!!

    private val accountKey: UserKey
        get() = arguments?.getParcelable(EXTRA_ACCOUNT_KEY)!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        gifView.setOnClickListener { (activity as MediaViewerActivity).toggleBar() }
        startLoading(false)
    }

    override fun getDownloadUri(): Uri? {
        return arguments?.getParcelable(SubsampleImageViewerFragment.EXTRA_MEDIA_URI)
    }

    override fun getDownloadExtra(): Any? {
        return null
    }

    override fun displayMedia(result: CacheDownloadLoader.Result) {
        val context = context ?: return
        val cacheUri = result.cacheUri
        if (cacheUri != null) {
            gifView.setInputSource(InputSource.UriSource(context.contentResolver, cacheUri))
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

    override fun releaseMediaResources() {
        gifView?.setInputSource(null)
    }

}