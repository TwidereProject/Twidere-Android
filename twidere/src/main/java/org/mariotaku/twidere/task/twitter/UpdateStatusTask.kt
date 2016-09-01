package org.mariotaku.twidere.task.twitter

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import android.text.TextUtils
import android.util.Pair
import com.nostra13.universalimageloader.core.assist.ImageSize
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.math.NumberUtils
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.fanfou.model.PhotoStatusUpdate
import org.mariotaku.microblog.library.twitter.TwitterUpload
import org.mariotaku.microblog.library.twitter.model.ErrorInfo
import org.mariotaku.microblog.library.twitter.model.MediaUploadResponse
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.microblog.library.twitter.model.StatusUpdate
import org.mariotaku.restfu.http.ContentType
import org.mariotaku.restfu.http.mime.Body
import org.mariotaku.restfu.http.mime.FileBody
import org.mariotaku.restfu.http.mime.SimpleBody
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtra
import org.mariotaku.twidere.model.util.ParcelableAccountUtils
import org.mariotaku.twidere.model.util.ParcelableLocationUtils
import org.mariotaku.twidere.model.util.ParcelableStatusUtils
import org.mariotaku.twidere.preference.ServicePickerPreference
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.util.io.ContentLengthInputStream
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by mariotaku on 16/5/22.
 */
class UpdateStatusTask(
        internal val context: Context,
        internal val stateCallback: UpdateStatusTask.StateCallback
) : AbstractTask<Pair<String, ParcelableStatusUpdate>, UpdateStatusTask.UpdateStatusResult, Context>(), Constants {

    @Inject
    lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var mediaLoader: MediaLoaderWrapper

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    override fun doLongOperation(params: Pair<String, ParcelableStatusUpdate>): UpdateStatusResult {
        val draftId = saveDraft(params.first, params.second)
        twitterWrapper.addSendingDraftId(draftId)
        try {
            val result = doUpdateStatus(params.second, draftId)
            deleteOrUpdateDraft(params.second, result, draftId)
            return result
        } catch (e: UpdateStatusException) {
            return UpdateStatusResult(e, draftId)
        } finally {
            twitterWrapper.removeSendingDraftId(draftId)
        }
    }

    override fun beforeExecute() {
        stateCallback.beforeExecute()
    }

    override fun afterExecute(handler: Context?, result: UpdateStatusResult) {
        stateCallback.afterExecute(handler, result)
    }

    @Throws(UpdateStatusException::class)
    private fun doUpdateStatus(update: ParcelableStatusUpdate, draftId: Long): UpdateStatusResult {
        val app = TwidereApplication.getInstance(context)
        val uploader = getMediaUploader(app)
        val shortener = getStatusShortener(app)

        val pendingUpdate = PendingStatusUpdate.from(update)


        uploadMedia(uploader, update, pendingUpdate)
        shortenStatus(shortener, update, pendingUpdate)

        val result: UpdateStatusResult
        try {
            result = requestUpdateStatus(update, pendingUpdate, draftId)
        } catch (e: IOException) {
            return UpdateStatusResult(UpdateStatusException(e), draftId)
        }

        mediaUploadCallback(uploader, pendingUpdate, result)
        statusShortenCallback(shortener, pendingUpdate, result)
        return result
    }

    private fun deleteOrUpdateDraft(update: ParcelableStatusUpdate, result: UpdateStatusResult, draftId: Long) {
        val where = Expression.equalsArgs(Drafts._ID).sql
        val whereArgs = arrayOf(draftId.toString())
        var hasError = false
        val failedAccounts = ArrayList<UserKey>()
        for (i in update.accounts.indices) {
            val exception = result.exceptions[i]
            if (exception != null && !isDuplicate(exception)) {
                hasError = true
                failedAccounts.add(update.accounts[i].account_key)
            }
        }
        val cr = context.contentResolver
        if (hasError) {
            val values = ContentValues()
            values.put(Drafts.ACCOUNT_KEYS, failedAccounts.joinToString(","))
            cr.update(Drafts.CONTENT_URI, values, where, whereArgs)
            // TODO show error message
        } else {
            cr.delete(Drafts.CONTENT_URI, where, whereArgs)
        }
    }

    @Throws(UploadException::class)
    private fun uploadMedia(uploader: MediaUploaderInterface?,
                            update: ParcelableStatusUpdate,
                            pendingUpdate: PendingStatusUpdate) {
        stateCallback.onStartUploadingMedia()
        if (uploader == null) {
            uploadMediaWithDefaultProvider(update, pendingUpdate)
        } else {
            uploadMediaWithExtension(uploader, update, pendingUpdate)
        }
    }

    @Throws(UploadException::class)
    private fun uploadMediaWithExtension(uploader: MediaUploaderInterface,
                                         update: ParcelableStatusUpdate,
                                         pending: PendingStatusUpdate) {
        val media: Array<UploaderMediaItem>
        try {
            media = UploaderMediaItem.getFromStatusUpdate(context, update)
        } catch (e: FileNotFoundException) {
            throw UploadException(e)
        }

        val sharedMedia = HashMap<UserKey, MediaUploadResult>()
        for (i in 0..pending.length - 1) {
            val account = update.accounts[i]
            // Skip upload if shared media found
            val accountKey = account.account_key
            var uploadResult: MediaUploadResult? = sharedMedia[accountKey]
            if (uploadResult == null) {
                uploadResult = uploader.upload(update, accountKey, media)
                if (uploadResult == null) {
                    // TODO error handling
                    continue
                }
                pending.mediaUploadResults[i] = uploadResult
                if (uploadResult.shared_owners != null) {
                    for (sharedOwner in uploadResult.shared_owners) {
                        sharedMedia.put(sharedOwner, uploadResult)
                    }
                }
            }
            // Override status text
            pending.overrideTexts[i] = Utils.getMediaUploadStatus(context,
                    uploadResult.media_uris, pending.overrideTexts[i])
        }
    }

    private fun shortenStatus(shortener: StatusShortenerInterface?,
                              update: ParcelableStatusUpdate,
                              pending: PendingStatusUpdate) {
        if (shortener == null) return
        stateCallback.onShorteningStatus()
        val sharedShortened = HashMap<UserKey, StatusShortenResult>()
        for (i in 0..pending.length - 1) {
            val account = update.accounts[i]
            // Skip upload if this shared media found
            val accountKey = account.account_key
            var shortenResult: StatusShortenResult? = sharedShortened[accountKey]
            if (shortenResult == null) {
                shortenResult = shortener.shorten(update, accountKey, pending.overrideTexts[i])
                if (shortenResult == null) {
                    // TODO error handling
                    continue
                }
                pending.statusShortenResults[i] = shortenResult
                if (shortenResult.shared_owners != null) {
                    for (sharedOwner in shortenResult.shared_owners) {
                        sharedShortened.put(sharedOwner, shortenResult)
                    }
                }
            }
            // Override status text
            pending.overrideTexts[i] = shortenResult.shortened
        }
    }

    @Throws(IOException::class)
    private fun requestUpdateStatus(statusUpdate: ParcelableStatusUpdate,
                                    pendingUpdate: PendingStatusUpdate,
                                    draftId: Long): UpdateStatusResult {

        stateCallback.onUpdatingStatus()

        val result = UpdateStatusResult(arrayOfNulls<ParcelableStatus>(pendingUpdate.length),
                arrayOfNulls<MicroBlogException>(pendingUpdate.length), draftId)

        for (i in 0 until pendingUpdate.length) {
            val account = statusUpdate.accounts[i]
            val microBlog = MicroBlogAPIFactory.getInstance(context, account.account_key, true)
            var body: Body? = null
            try {
                when (ParcelableAccountUtils.getAccountType(account)) {
                    ParcelableAccount.Type.FANFOU -> {
                        // Call uploadPhoto if media present
                        if (!ArrayUtils.isEmpty(statusUpdate.media)) {
                            // Fanfou only allow one photo
                            if (statusUpdate.media.size > 1) {
                                result.exceptions[i] = MicroBlogException(
                                        context.getString(R.string.error_too_many_photos_fanfou))
                            } else {
                                val sizeLimit = Point(2048, 1536)
                                body = getBodyFromMedia(context, mediaLoader,
                                        Uri.parse(statusUpdate.media[0].uri),
                                        sizeLimit, statusUpdate.media[0].type,
                                        ContentLengthInputStream.ReadListener { length, position ->
                                            stateCallback.onUploadingProgressChanged(-1, position, length)
                                        })
                                val photoUpdate = PhotoStatusUpdate(body,
                                        pendingUpdate.overrideTexts[i])
                                val requestResult = microBlog.uploadPhoto(photoUpdate)

                                result.statuses[i] = ParcelableStatusUtils.fromStatus(requestResult,
                                        account.account_key, false)
                            }
                        } else {
                            val requestResult = twitterUpdateStatus(microBlog,
                                    statusUpdate, pendingUpdate, pendingUpdate.overrideTexts[i], i)

                            result.statuses[i] = ParcelableStatusUtils.fromStatus(requestResult,
                                    account.account_key, false)
                        }
                    }
                    else -> {
                        val requestResult = twitterUpdateStatus(microBlog, statusUpdate,
                                pendingUpdate, pendingUpdate.overrideTexts[i], i)

                        result.statuses[i] = ParcelableStatusUtils.fromStatus(requestResult,
                                account.account_key, false)
                    }
                }
            } catch (e: MicroBlogException) {
                result.exceptions[i] = e
            } finally {
                Utils.closeSilently(body)
            }
        }
        return result
    }

    /**
     * Calling Twitter's upload method. This method sets multiple owner for bandwidth saving
     */
    @Throws(UploadException::class)
    private fun uploadMediaWithDefaultProvider(update: ParcelableStatusUpdate, pendingUpdate: PendingStatusUpdate) {
        // Return empty array if no media attached
        if (ArrayUtils.isEmpty(update.media)) return
        val ownersList = update.accounts.filter {
            ParcelableAccount.Type.TWITTER == ParcelableAccountUtils.getAccountType(it)
        }.map {
            it.account_key
        }
        val ownerIds = ownersList.map {
            it.id
        }.toTypedArray()
        for (i in 0..pendingUpdate.length - 1) {
            val account = update.accounts[i]
            val mediaIds: Array<String>?
            when (ParcelableAccountUtils.getAccountType(account)) {
                ParcelableAccount.Type.TWITTER -> {
                    val upload = MicroBlogAPIFactory.getInstance(context,
                            account.account_key, true, true, TwitterUpload::class.java)!!
                    if (pendingUpdate.sharedMediaIds != null) {
                        mediaIds = pendingUpdate.sharedMediaIds
                    } else {
                        mediaIds = uploadAllMediaShared(upload, update, ownerIds, true)
                        pendingUpdate.sharedMediaIds = mediaIds
                    }
                }
                ParcelableAccount.Type.FANFOU -> {
                    // Nope, fanfou uses photo uploading API
                    mediaIds = null
                }
                ParcelableAccount.Type.STATUSNET -> {
                    // TODO use their native API
                    val upload = MicroBlogAPIFactory.getInstance(context,
                            account.account_key, true, true, TwitterUpload::class.java)!!
                    mediaIds = uploadAllMediaShared(upload, update, ownerIds, false)
                }
                else -> {
                    mediaIds = null
                }
            }
            pendingUpdate.mediaIds[i] = mediaIds
        }
        pendingUpdate.sharedMediaOwners = ownersList.toTypedArray()
    }

    @Throws(MicroBlogException::class)
    private fun twitterUpdateStatus(microBlog: MicroBlog, statusUpdate: ParcelableStatusUpdate,
                                    pendingUpdate: PendingStatusUpdate, overrideText: String,
                                    index: Int): Status {
        val status = StatusUpdate(overrideText)
        if (statusUpdate.in_reply_to_status != null) {
            status.inReplyToStatusId(statusUpdate.in_reply_to_status.id)
        }
        if (statusUpdate.repost_status_id != null) {
            status.setRepostStatusId(statusUpdate.repost_status_id)
        }
        if (statusUpdate.attachment_url != null) {
            status.setAttachmentUrl(statusUpdate.attachment_url)
        }
        if (statusUpdate.location != null) {
            status.location(ParcelableLocationUtils.toGeoLocation(statusUpdate.location))
            status.displayCoordinates(statusUpdate.display_coordinates)
        }
        val mediaIds = pendingUpdate.mediaIds[index]
        if (mediaIds != null) {
            status.mediaIds(*mediaIds)
        }
        status.possiblySensitive(statusUpdate.is_possibly_sensitive)
        return microBlog.updateStatus(status)
    }

    private fun statusShortenCallback(shortener: StatusShortenerInterface?, pendingUpdate: PendingStatusUpdate, updateResult: UpdateStatusResult) {
        if (shortener == null) return
        for (i in 0..pendingUpdate.length - 1) {
            val shortenResult = pendingUpdate.statusShortenResults[i]
            val status = updateResult.statuses[i]
            if (shortenResult == null || status == null) continue
            shortener.callback(shortenResult, status)
        }
    }

    private fun mediaUploadCallback(uploader: MediaUploaderInterface?, pendingUpdate: PendingStatusUpdate, updateResult: UpdateStatusResult) {
        if (uploader == null) return
        for (i in 0..pendingUpdate.length - 1) {
            val uploadResult = pendingUpdate.mediaUploadResults[i]
            val status = updateResult.statuses[i]
            if (uploadResult == null || status == null) continue
            uploader.callback(uploadResult, status)
        }
    }

    @Throws(UploaderNotFoundException::class, UploadException::class, ShortenerNotFoundException::class, ShortenException::class)
    private fun getStatusShortener(app: TwidereApplication): StatusShortenerInterface? {
        val shortenerComponent = preferences.getString(KEY_STATUS_SHORTENER, null)
        if (ServicePickerPreference.isNoneValue(shortenerComponent)) return null

        val shortener = StatusShortenerInterface.getInstance(app, shortenerComponent) ?: throw ShortenerNotFoundException()
        try {
            shortener.checkService { metaData ->
                if (metaData == null) throw ExtensionVersionMismatchException()
                val extensionVersion = metaData.getString(METADATA_KEY_EXTENSION_VERSION_STATUS_SHORTENER)
                if (!TextUtils.equals(extensionVersion, context.getString(R.string.status_shortener_service_interface_version))) {
                    throw ExtensionVersionMismatchException()
                }
            }
        } catch (e: AbsServiceInterface.CheckServiceException) {
            if (e is ExtensionVersionMismatchException) {
                throw ShortenException(context.getString(R.string.shortener_version_incompatible))
            }
            throw ShortenException(e)
        }

        return shortener
    }

    @Throws(UploaderNotFoundException::class, UploadException::class)
    private fun getMediaUploader(app: TwidereApplication): MediaUploaderInterface? {
        val uploaderComponent = preferences.getString(KEY_MEDIA_UPLOADER, null)
        if (ServicePickerPreference.isNoneValue(uploaderComponent)) return null
        val uploader = MediaUploaderInterface.getInstance(app, uploaderComponent) ?: throw UploaderNotFoundException(context.getString(R.string.error_message_media_uploader_not_found))
        try {
            uploader.checkService { metaData ->
                if (metaData == null) throw ExtensionVersionMismatchException()
                val extensionVersion = metaData.getString(METADATA_KEY_EXTENSION_VERSION_MEDIA_UPLOADER)
                if (!TextUtils.equals(extensionVersion, context.getString(R.string.media_uploader_service_interface_version))) {
                    throw ExtensionVersionMismatchException()
                }
            }
        } catch (e: AbsServiceInterface.CheckServiceException) {
            if (e is ExtensionVersionMismatchException) {
                throw UploadException(context.getString(R.string.uploader_version_incompatible))
            }
            throw UploadException(e)
        }

        return uploader
    }

    @Throws(UploadException::class)
    private fun uploadAllMediaShared(upload: TwitterUpload, update: ParcelableStatusUpdate,
                                     ownerIds: Array<String>, chucked: Boolean): Array<String> {
        val mediaIds = update.media.mapIndexed { index, media ->
            val resp: MediaUploadResponse
            //noinspection TryWithIdenticalCatches
            var body: Body? = null
            try {
                val sizeLimit = Point(2048, 1536)
                body = getBodyFromMedia(context, mediaLoader, Uri.parse(media.uri), sizeLimit,
                        media.type, ContentLengthInputStream.ReadListener { length, position ->
                    stateCallback.onUploadingProgressChanged(index, position, length)
                })
                if (chucked) {
                    resp = uploadMediaChucked(upload, body, ownerIds)
                } else {
                    resp = upload.uploadMedia(body, ownerIds)
                }
            } catch (e: IOException) {
                throw UploadException(e)
            } catch (e: MicroBlogException) {
                throw UploadException(e)
            } finally {
                Utils.closeSilently(body)
            }
            resp.id
        }
        return mediaIds.toTypedArray()
    }


    @Throws(IOException::class, MicroBlogException::class)
    private fun uploadMediaChucked(upload: TwitterUpload, body: Body,
                                   ownerIds: Array<String>): MediaUploadResponse {
        val mediaType = body.contentType().contentType
        val length = body.length()
        val stream = body.stream()
        var response = upload.initUploadMedia(mediaType, length, ownerIds)
        val segments = if (length == 0L) 0 else (length / BULK_SIZE + 1).toInt()
        for (segmentIndex in 0..segments - 1) {
            val currentBulkSize = Math.min(BULK_SIZE.toLong(), length - segmentIndex * BULK_SIZE).toInt()
            val bulk = SimpleBody(ContentType.OCTET_STREAM, null, currentBulkSize.toLong(),
                    stream)
            upload.appendUploadMedia(response.id, segmentIndex, bulk)
        }
        response = upload.finalizeUploadMedia(response.id)
        var info: MediaUploadResponse.ProcessingInfo? = response.processingInfo
        while (info != null && shouldWaitForProcess(info)) {
            val checkAfterSecs = info.checkAfterSecs
            if (checkAfterSecs <= 0) {
                break
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(checkAfterSecs))
            } catch (e: InterruptedException) {
                break
            }

            response = upload.getUploadMediaStatus(response.id)
            info = response.processingInfo
        }
        if (info != null && MediaUploadResponse.ProcessingInfo.State.FAILED == info.state) {
            val exception = MicroBlogException()
            val errorInfo = info.error
            if (errorInfo != null) {
                exception.errors = arrayOf(errorInfo)
            }
            throw exception
        }
        return response
    }

    private fun isDuplicate(exception: Exception): Boolean {
        return exception is MicroBlogException && exception.errorCode == ErrorInfo.STATUS_IS_DUPLICATE
    }

    private fun shouldWaitForProcess(info: MediaUploadResponse.ProcessingInfo): Boolean {
        when (info.state) {
            MediaUploadResponse.ProcessingInfo.State.PENDING, MediaUploadResponse.ProcessingInfo.State.IN_PROGRESS -> return true
            else -> return false
        }
    }


    private fun saveDraft(@Draft.Action draftAction: String?, statusUpdate: ParcelableStatusUpdate): Long {
        val draft = Draft()
        draft.account_keys = ParcelableAccountUtils.getAccountKeys(statusUpdate.accounts)
        if (draftAction != null) {
            draft.action_type = draftAction
        } else {
            draft.action_type = Draft.Action.UPDATE_STATUS
        }
        draft.text = statusUpdate.text
        draft.location = statusUpdate.location
        draft.media = statusUpdate.media
        val extra = UpdateStatusActionExtra()
        extra.inReplyToStatus = statusUpdate.in_reply_to_status
        extra.setIsPossiblySensitive(statusUpdate.is_possibly_sensitive)
        extra.isRepostStatusId = statusUpdate.repost_status_id
        extra.displayCoordinates = statusUpdate.display_coordinates
        draft.action_extras = extra
        val resolver = context.contentResolver
        val draftUri = resolver.insert(Drafts.CONTENT_URI, DraftValuesCreator.create(draft)) ?: return -1
        return NumberUtils.toLong(draftUri.lastPathSegment, -1)
    }

    internal class PendingStatusUpdate(val length: Int, defaultText: String) {

        var sharedMediaIds: Array<String>? = null
        var sharedMediaOwners: Array<UserKey>? = null

        val overrideTexts: Array<String>
        val mediaIds: Array<Array<String>?>

        val mediaUploadResults: Array<MediaUploadResult?>
        val statusShortenResults: Array<StatusShortenResult?>

        init {
            overrideTexts = Array(length) { idx ->
                defaultText
            }
            mediaUploadResults = arrayOfNulls<MediaUploadResult>(length)
            statusShortenResults = arrayOfNulls<StatusShortenResult>(length)
            mediaIds = arrayOfNulls<Array<String>>(length)
        }

        companion object {

            fun from(statusUpdate: ParcelableStatusUpdate): PendingStatusUpdate {
                return PendingStatusUpdate(statusUpdate.accounts.size,
                        statusUpdate.text)
            }
        }
    }

    class UpdateStatusResult {
        val statuses: Array<ParcelableStatus?>
        val exceptions: Array<MicroBlogException?>

        val exception: UpdateStatusException?
        val draftId: Long

        val succeed: Boolean get() = !statuses.contains(null)

        constructor(statuses: Array<ParcelableStatus?>, exceptions: Array<MicroBlogException?>, draftId: Long) {
            this.statuses = statuses
            this.exceptions = exceptions
            this.exception = null
            this.draftId = draftId
        }

        constructor(exception: UpdateStatusException, draftId: Long) {
            this.exception = exception
            this.statuses = arrayOfNulls<ParcelableStatus>(0)
            this.exceptions = arrayOfNulls<MicroBlogException>(0)
            this.draftId = draftId
        }
    }


    open class UpdateStatusException : Exception {
        constructor() : super()

        constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable)

        constructor(throwable: Throwable) : super(throwable)

        constructor(message: String) : super(message)
    }

    class UploaderNotFoundException : UpdateStatusException {

        constructor() : super()

        constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable)

        constructor(throwable: Throwable) : super(throwable)

        constructor(message: String) : super(message)
    }

    class UploadException : UpdateStatusException {

        constructor() : super() {
        }

        constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable) {
        }

        constructor(throwable: Throwable) : super(throwable) {
        }

        constructor(message: String) : super(message) {
        }
    }

    class ExtensionVersionMismatchException : AbsServiceInterface.CheckServiceException()

    class ShortenerNotFoundException : UpdateStatusException()

    class ShortenException : UpdateStatusException {

        constructor() : super() {
        }

        constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable) {
        }

        constructor(throwable: Throwable) : super(throwable) {
        }

        constructor(message: String) : super(message) {
        }
    }

    interface StateCallback {
        @WorkerThread
        fun onStartUploadingMedia()

        @WorkerThread
        fun onUploadingProgressChanged(index: Int, current: Long, total: Long)

        @WorkerThread
        fun onShorteningStatus()

        @WorkerThread
        fun onUpdatingStatus()

        @UiThread
        fun afterExecute(handler: Context?, result: UpdateStatusResult)

        @UiThread
        fun beforeExecute()
    }

    companion object {

        private val BULK_SIZE = 256 * 1024// 128 Kib

        @Throws(IOException::class)
        fun getBodyFromMedia(context: Context, mediaLoader: MediaLoaderWrapper,
                             mediaUri: Uri, sizeLimit: Point? = null,
                             @ParcelableMedia.Type type: Int,
                             readListener: ContentLengthInputStream.ReadListener): FileBody {
            val resolver = context.contentResolver
            var mediaType = resolver.getType(mediaUri)
            val cis = run {
                if (type == ParcelableMedia.Type.IMAGE && sizeLimit != null) {
                    val length: Long
                    val o = BitmapFactory.Options()
                    o.inJustDecodeBounds = true
                    BitmapFactoryUtils.decodeUri(resolver, mediaUri, null, o)
                    if (o.outMimeType != null) {
                        mediaType = o.outMimeType
                    }
                    o.inSampleSize = Utils.calculateInSampleSize(o.outWidth, o.outHeight,
                            sizeLimit.x, sizeLimit.y)
                    o.inJustDecodeBounds = false
                    if (o.outWidth > 0 && o.outHeight > 0 && mediaType != "image/gif") {
                        val bitmap = mediaLoader.loadImageSync(mediaUri.toString(),
                                ImageSize(o.outWidth, o.outHeight).scaleDown(o.inSampleSize))

                        if (bitmap != null) {
                            val os = DirectByteArrayOutputStream()
                            when (mediaType) {
                                "image/png", "image/x-png", "image/webp", "image-x-webp" -> {
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, os)
                                }
                                else -> {
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, os)
                                }
                            }
                            length = os.size().toLong()
                            return@run ContentLengthInputStream(os.inputStream(true), length)
                        }
                    }
                }
                val st = resolver.openInputStream(mediaUri) ?: throw FileNotFoundException(mediaUri.toString())
                val length = st.available().toLong()
                return@run ContentLengthInputStream(st, length)
            }

            cis.setReadListener(readListener)
            val contentType: ContentType
            if (TextUtils.isEmpty(mediaType)) {
                contentType = ContentType.parse("application/octet-stream")
            } else {
                contentType = ContentType.parse(mediaType!!)
            }
            return FileBody(cis, "attachment", cis.length(), contentType)
        }

        internal class DirectByteArrayOutputStream : ByteArrayOutputStream {
            constructor() : super()
            constructor(size: Int) : super(size)

            fun inputStream(close: Boolean): InputStream {
                return DirectInputStream(this, close)
            }

            internal class DirectInputStream(
                    val os: DirectByteArrayOutputStream,
                    val close: Boolean
            ) : ByteArrayInputStream(os.buf, 0, os.count) {
                override fun close() {
                    if (close) {
                        os.close()
                    }
                    super.close()
                }
            }
        }


    }
}
