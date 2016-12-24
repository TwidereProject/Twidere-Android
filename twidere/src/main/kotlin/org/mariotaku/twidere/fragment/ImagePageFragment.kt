package org.mariotaku.twidere.fragment

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageDecoder
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.MediaDownloadEvent
import org.mariotaku.mediaviewer.library.CacheDownloadLoader
import org.mariotaku.mediaviewer.library.subsampleimageview.SubsampleImageViewerFragment
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.TwidereMathUtils
import org.mariotaku.twidere.util.UriUtils
import org.mariotaku.twidere.util.media.MediaExtra
import java.io.IOException

class ImagePageFragment : SubsampleImageViewerFragment() {

    private var mediaLoadState: Int = 0
    private var mediaDownloadEvent: MediaDownloadEvent? = null
    private val sizedResultCreator: CacheDownloadLoader.ResultCreator by lazy { SizedResultCreator(context) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        val activity = activity
        if (isVisibleToUser && activity != null) {
            activity.supportInvalidateOptionsMenu()
        }
    }

    override fun getDownloadExtra(): Any? {
        val mediaExtra = MediaExtra()
        mediaExtra.accountKey = accountKey
        val origDownloadUri = super.getDownloadUri()
        val downloadUri = downloadUri
        if (origDownloadUri != null && downloadUri != null) {
            val fallbackUrl = origDownloadUri.toString()
            mediaExtra.fallbackUrl = fallbackUrl
            mediaExtra.isSkipUrlReplacing = fallbackUrl != downloadUri.toString()
        }
        return mediaExtra
    }

    override fun hasDownloadedData(): Boolean {
        return super.hasDownloadedData() && mediaLoadState != State.ERROR
    }

    override fun onMediaLoadStateChange(@State state: Int) {
        mediaLoadState = state
        val activity = activity
        if (userVisibleHint && activity != null) {
            activity.supportInvalidateOptionsMenu()
        }
    }

    override fun setupImageView(imageView: SubsamplingScaleImageView) {
        imageView.maxScale = resources.displayMetrics.density
        imageView.setBitmapDecoderClass(PreviewBitmapDecoder::class.java)
        imageView.setParallelLoadingEnabled(true)
    }

    override fun getImageSource(data: CacheDownloadLoader.Result): ImageSource {
        assert(data.cacheUri != null)
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
        assert(data.cacheUri != null)
        return ImageSource.uri(UriUtils.appendQueryParameters(data.cacheUri, QUERY_PARAM_PREVIEW, true))
    }

    override fun getResultCreator(): CacheDownloadLoader.ResultCreator? {
        return sizedResultCreator
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
        if (mediaDownloadEvent?.nonce == nonce) {
            mediaDownloadEvent?.setOpenedTime(System.currentTimeMillis())
            mediaDownloadEvent?.setSize(total)
        }
    }

    override fun onDownloadFinished(nonce: Long) {
        super.onDownloadFinished(nonce)
        if (mediaDownloadEvent?.nonce == nonce) {
            mediaDownloadEvent?.markEnd()
            HotMobiLogger.getInstance(context).log(accountKey, mediaDownloadEvent!!)
            mediaDownloadEvent = null
        }
    }

    internal class SizedResult(cacheUri: Uri, val width: Int, val height: Int) : CacheDownloadLoader.Result(cacheUri, null)

    internal class SizedResultCreator(private val context: Context) : CacheDownloadLoader.ResultCreator {

        override fun create(uri: Uri): CacheDownloadLoader.Result {
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
                val targetSize = Math.min(1024, Math.max(dm.widthPixels, dm.heightPixels))
                val sizeRatio = Math.ceil(Math.max(o.outHeight, o.outWidth) / targetSize.toDouble())
                o.inSampleSize = TwidereMathUtils.nextPowerOf2(Math.max(1.0, sizeRatio).toInt())
                o.inJustDecodeBounds = false
                val bitmap = decodeBitmap(cr, uri, o) ?: throw IOException()
                return bitmap
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