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

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageDecoder
import org.mariotaku.ktextension.nextPowerOf2
import org.mariotaku.mediaviewer.library.CacheDownloadLoader
import org.mariotaku.mediaviewer.library.subsampleimageview.SubsampleImageViewerFragment
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.MediaViewerActivity
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.UriUtils
import org.mariotaku.twidere.util.media.MediaExtra
import java.io.IOException
import java.lang.ref.WeakReference
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class ImagePageFragment : SubsampleImageViewerFragment() {


    private val media: ParcelableMedia?
        get() = arguments?.getParcelable<ParcelableMedia?>(EXTRA_MEDIA)

    private val accountKey: UserKey?
        get() = arguments?.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)

    private val sizedResultCreator: CacheDownloadLoader.ResultCreator by lazy {
        return@lazy SizedResultCreator(requireContext())
    }

    private var mediaLoadState: Int = 0

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            activity?.invalidateOptionsMenu()
        }
    }

    override fun getDownloadUri(): Uri? {
        return media?.media_url?.let(Uri::parse)
    }

    override fun getDownloadExtra(): Any? {
        val mediaExtra = MediaExtra()
        mediaExtra.accountKey = accountKey
        mediaExtra.fallbackUrl = media?.preview_url
        mediaExtra.isSkipUrlReplacing = mediaExtra.fallbackUrl != downloadUri?.toString()
        return mediaExtra
    }

    override fun hasDownloadedData(): Boolean {
        return super.hasDownloadedData() && mediaLoadState != State.ERROR
    }

    override fun onMediaLoadStateChange(@State state: Int) {
        mediaLoadState = state
        if (userVisibleHint) {
            activity?.invalidateOptionsMenu()
        }
    }

    override fun setupImageView(imageView: SubsamplingScaleImageView) {
        imageView.maxScale = resources.displayMetrics.density
        imageView.setBitmapDecoderClass(PreviewBitmapDecoder::class.java)
        imageView.setExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        imageView.setOnClickListener {
            val activity = activity as? MediaViewerActivity ?: return@setOnClickListener
            activity.toggleBar()
        }
    }

    override fun getImageSource(data: CacheDownloadLoader.Result): ImageSource {
        if (BuildConfig.DEBUG && data.cacheUri == null) { error("Assertion failed") }
        if (data !is SizedResult) {
            return super.getImageSource(data)
        }
        val imageSource = ImageSource.uri(data.cacheUri!!)
        imageSource.tilingEnabled()
        imageSource.dimensions(data.width, data.height)
        return imageSource
    }

    override fun getPreviewImageSource(data: CacheDownloadLoader.Result): ImageSource? {
        if (data !is SizedResult) return null
        if (BuildConfig.DEBUG && data.cacheUri == null) { error("Assertion failed") }
        return ImageSource.uri(UriUtils.appendQueryParameters(data.cacheUri, QUERY_PARAM_PREVIEW, true))
    }

    override fun getResultCreator(): CacheDownloadLoader.ResultCreator? {
        return sizedResultCreator
    }

    internal class SizedResult(cacheUri: Uri, val width: Int, val height: Int) : CacheDownloadLoader.Result(cacheUri, null)

    internal class SizedResultCreator(context: Context) : CacheDownloadLoader.ResultCreator {

        private val weakContext = WeakReference(context)

        override fun create(uri: Uri): CacheDownloadLoader.Result {
            val context = weakContext.get() ?: return CacheDownloadLoader.Result.getInstance(InterruptedException())
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            try {
                decodeBitmap(context.contentResolver, uri, o)
            } catch (e: IOException) {
                return CacheDownloadLoader.Result.getInstance(uri)
            }

            if (o.outWidth > 0 && o.outHeight > 0) {
                return SizedResult(uri, o.outWidth, o.outHeight)
            }
            return CacheDownloadLoader.Result.getInstance(uri)
        }

    }

    class PreviewBitmapDecoder : SkiaImageDecoder() {
        @Throws(Exception::class)
        override fun decode(context: Context, uri: Uri): Bitmap {
            if (AUTHORITY_TWIDERE_CACHE == uri.authority && uri.getBooleanQueryParameter(QUERY_PARAM_PREVIEW, false)) {
                val o = BitmapFactory.Options()
                o.inJustDecodeBounds = true
                o.inPreferredConfig = Bitmap.Config.RGB_565
                val cr = context.contentResolver
                decodeBitmap(cr, uri, o)
                val dm = context.resources.displayMetrics
                val targetSize = min(1024, max(dm.widthPixels, dm.heightPixels))
                val sizeRatio = ceil(max(o.outHeight, o.outWidth) / targetSize.toDouble())
                o.inSampleSize = max(1.0, sizeRatio).toInt().nextPowerOf2
                o.inJustDecodeBounds = false
                return decodeBitmap(cr, uri, o) ?: throw IOException()
            }
            return super.decode(context, uri)
        }

    }

    companion object {

        @Throws(IOException::class)
        internal fun decodeBitmap(cr: ContentResolver, uri: Uri, o: BitmapFactory.Options): Bitmap? {
            cr.openInputStream(uri).use {
                return BitmapFactory.decodeStream(it, null, o)
            }
        }

    }
}