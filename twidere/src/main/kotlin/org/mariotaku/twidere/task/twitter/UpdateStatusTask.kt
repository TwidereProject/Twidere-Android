package org.mariotaku.twidere.task.twitter

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import android.support.media.ExifInterface
import android.text.TextUtils
import android.webkit.MimeTypeMap
import net.ypresto.androidtranscoder.MediaTranscoder
import net.ypresto.androidtranscoder.format.MediaFormatStrategyPresets
import org.mariotaku.ktextension.*
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.fanfou.model.PhotoStatusUpdate
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.mastodon.model.Attachment
import org.mariotaku.microblog.library.twitter.TwitterUpload
import org.mariotaku.microblog.library.twitter.model.ErrorInfo
import org.mariotaku.microblog.library.twitter.model.MediaUploadResponse
import org.mariotaku.microblog.library.twitter.model.NewMediaMetadata
import org.mariotaku.microblog.library.twitter.model.StatusUpdate
import org.mariotaku.restfu.http.ContentType
import org.mariotaku.restfu.http.mime.Body
import org.mariotaku.restfu.http.mime.FileBody
import org.mariotaku.restfu.http.mime.SimpleBody
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.alias.MastodonStatusUpdate
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.extension.calculateInSampleSize
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.account.AccountExtras
import org.mariotaku.twidere.model.analyzer.UpdateStatus
import org.mariotaku.twidere.model.schedule.ScheduleInfo
import org.mariotaku.twidere.model.util.ParcelableLocationUtils
import org.mariotaku.twidere.preference.ComponentPickerPreference
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.task.BaseAbstractTask
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.io.ContentLengthInputStream
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.text.StatusTextValidator
import java.io.Closeable
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Update status
 *
 * Created by mariotaku on 16/5/22.
 */
class UpdateStatusTask(
        context: Context,
        internal val stateCallback: UpdateStatusTask.StateCallback
) : BaseAbstractTask<Pair<ParcelableStatusUpdate, ScheduleInfo?>, UpdateStatusTask.UpdateStatusResult, Any?>(context) {

    override fun doLongOperation(params: Pair<ParcelableStatusUpdate, ScheduleInfo?>): UpdateStatusResult {
        val (update, info) = params
        val draftId = saveDraft(context, update.draft_action ?: Draft.Action.UPDATE_STATUS) {
            applyUpdateStatus(update)
        }
        microBlogWrapper.addSendingDraftId(draftId)
        try {
            val result = doUpdateStatus(update, info, draftId)
            deleteOrUpdateDraft(update, result, draftId)
            return result
        } catch (e: UpdateStatusException) {
            return UpdateStatusResult(e, draftId)
        } finally {
            microBlogWrapper.removeSendingDraftId(draftId)
        }
    }

    override fun beforeExecute() {
        stateCallback.beforeExecute()
    }

    override fun afterExecute(callback: Any?, results: UpdateStatusResult) {
        stateCallback.afterExecute(results)
        logUpdateStatus(params.first, results)
    }

    private fun logUpdateStatus(statusUpdate: ParcelableStatusUpdate, result: UpdateStatusResult) {
        val mediaType = statusUpdate.media?.firstOrNull()?.type ?: ParcelableMedia.Type.UNKNOWN
        val hasLocation = statusUpdate.location != null
        val preciseLocation = statusUpdate.display_coordinates
        Analyzer.log(UpdateStatus(result.accountTypes.firstOrNull(), statusUpdate.draft_action,
                mediaType, hasLocation, preciseLocation, result.succeed,
                result.exceptions.firstOrNull() ?: result.exception))
    }

    @Throws(UpdateStatusException::class)
    private fun doUpdateStatus(update: ParcelableStatusUpdate, info: ScheduleInfo?, draftId: Long):
            UpdateStatusResult {
        val app = TwidereApplication.getInstance(context)
        val uploader = getMediaUploader(app)
        val shortener = getStatusShortener(app)

        val pendingUpdate = PendingStatusUpdate(update)

        val result: UpdateStatusResult
        try {
            uploadMedia(uploader, update, info, pendingUpdate)
            shortenStatus(shortener, update, pendingUpdate)

            if (info != null) {
                result = requestScheduleStatus(update, pendingUpdate, info, draftId)
            } else {
                result = requestUpdateStatus(update, pendingUpdate, draftId)
            }

            mediaUploadCallback(uploader, pendingUpdate, result)
            statusShortenCallback(shortener, pendingUpdate, result)

            // Cleanup
            pendingUpdate.deleteOnSuccess.forEach { item -> item.delete(context) }
        } catch (e: UploadException) {
            e.deleteAlways?.forEach { it.delete(context) }
            throw e
        } finally {
            // Cleanup
            pendingUpdate.deleteAlways.forEach { item -> item.delete(context) }
            uploader?.unbindService()
            shortener?.unbindService()
        }
        return result
    }

    private fun deleteOrUpdateDraft(update: ParcelableStatusUpdate, result: UpdateStatusResult,
            draftId: Long) {
        val where = Expression.equals(Drafts._ID, draftId).sql
        var hasError = false
        val failedAccounts = ArrayList<UserKey>()
        for (i in update.accounts.indices) {
            val exception = result.exceptions[i]
            if (exception != null && !isDuplicate(exception)) {
                hasError = true
                failedAccounts.add(update.accounts[i].key)
            }
        }
        val cr = context.contentResolver
        if (hasError) {
            val values = ContentValues()
            values.put(Drafts.ACCOUNT_KEYS, failedAccounts.joinToString(","))
            cr.update(Drafts.CONTENT_URI, values, where, null)
            // TODO show error message
        } else {
            cr.delete(Drafts.CONTENT_URI, where, null)
        }
    }

    @Throws(UploadException::class)
    private fun uploadMedia(uploader: MediaUploaderInterface?,
            update: ParcelableStatusUpdate,
            info: ScheduleInfo?,
            pendingUpdate: PendingStatusUpdate) {
        stateCallback.onStartUploadingMedia()
        if (uploader != null) {
            uploadMediaWithExtension(uploader, update, pendingUpdate)
        } else if (info == null) {
            uploadMediaWithDefaultProvider(update, pendingUpdate)
        }
    }

    @Throws(UploadException::class)
    private fun uploadMediaWithExtension(uploader: MediaUploaderInterface,
            update: ParcelableStatusUpdate,
            pending: PendingStatusUpdate) {
        uploader.waitForService()
        val media: Array<UploaderMediaItem>
        try {
            media = UploaderMediaItem.getFromStatusUpdate(context, update)
        } catch (e: FileNotFoundException) {
            throw UploadException(e)
        }

        val sharedMedia = HashMap<UserKey, MediaUploadResult>()
        for (i in 0 until pending.length) {
            val account = update.accounts[i]
            // Skip upload if shared media found
            val accountKey = account.key
            var uploadResult: MediaUploadResult? = sharedMedia[accountKey]
            if (uploadResult == null) {
                uploadResult = uploader.upload(update, accountKey, media) ?: run {
                    throw UploadException()
                }
                if (uploadResult.media_uris == null) {
                    throw UploadException(uploadResult.error_message ?: "Unknown error")
                }
                pending.mediaUploadResults[i] = uploadResult
                if (uploadResult.shared_owners != null) {
                    for (sharedOwner in uploadResult.shared_owners) {
                        sharedMedia.put(sharedOwner, uploadResult)
                    }
                }
            }
            // Override status text
            pending.overrideTexts[i] = "${pending.overrideTexts[i]} ${uploadResult.media_uris.joinToString(" ")}"
        }
    }

    @Throws(UpdateStatusException::class)
    private fun shortenStatus(shortener: StatusShortenerInterface?,
            update: ParcelableStatusUpdate,
            pending: PendingStatusUpdate) {
        if (shortener == null) return
        stateCallback.onShorteningStatus()
        val sharedShortened = HashMap<UserKey, StatusShortenResult>()
        for (i in 0 until pending.length) {
            val account = update.accounts[i]
            val text = pending.overrideTexts[i]
            val textLimit = account.textLimit
            val ignoreMentions = account.type == AccountType.TWITTER
            if (textLimit >= 0 && StatusTextValidator.calculateLength(account.type, account.key,
                    update.summary, text, ignoreMentions, update.in_reply_to_status) <= textLimit) {
                continue
            }
            shortener.waitForService()
            // Skip upload if this shared media found
            val accountKey = account.key
            var shortenResult: StatusShortenResult? = sharedShortened[accountKey]
            if (shortenResult == null) {
                shortenResult = shortener.shorten(update, accountKey, text) ?: run {
                    throw ShortenException()
                }
                if (shortenResult.shortened == null) {
                    throw ShortenException(shortenResult.error_message ?: "Unknown error")
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

    @Throws(UpdateStatusException::class)
    private fun requestScheduleStatus(
            statusUpdate: ParcelableStatusUpdate,
            pendingUpdate: PendingStatusUpdate,
            scheduleInfo: ScheduleInfo,
            draftId: Long
    ): UpdateStatusResult {

        stateCallback.onUpdatingStatus()
        if (!extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_SCHEDULE_STATUS)) {
            throw SchedulerNotFoundException(context.getString(R.string.error_message_scheduler_not_available))
        }

        val controller = scheduleProvider ?: run {
            throw SchedulerNotFoundException(context.getString(R.string.error_message_scheduler_not_available))
        }

        controller.scheduleStatus(statusUpdate, pendingUpdate, scheduleInfo)

        return UpdateStatusResult(pendingUpdate.length, draftId)
    }

    @Throws(UpdateStatusException::class)
    private fun requestUpdateStatus(
            statusUpdate: ParcelableStatusUpdate,
            pendingUpdate: PendingStatusUpdate,
            draftId: Long
    ): UpdateStatusResult {

        stateCallback.onUpdatingStatus()

        val result = UpdateStatusResult(pendingUpdate.length, draftId)

        for (i in 0 until pendingUpdate.length) {
            val account = statusUpdate.accounts[i]
            result.accountTypes[i] = account.type

            try {
                val status = when (account.type) {
                    AccountType.FANFOU -> {
                        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                        // Call uploadPhoto if media present
                        if (statusUpdate.media.isNotNullOrEmpty()) {
                            // Fanfou only allow one photo
                            fanfouUpdateStatusWithPhoto(microBlog, statusUpdate, pendingUpdate,
                                    account.getMediaSizeLimit(), i)
                        } else {
                            twitterUpdateStatus(microBlog, statusUpdate, pendingUpdate, i)
                        }
                    }
                    AccountType.MASTODON -> {
                        val mastodon = account.newMicroBlogInstance(context, Mastodon::class.java)
                        mastodonUpdateStatus(mastodon, statusUpdate, pendingUpdate, i)
                    }
                    else -> {
                        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
                        twitterUpdateStatus(microBlog, statusUpdate, pendingUpdate, i)
                    }
                }
                result.statuses[i] = status
            } catch (e: MicroBlogException) {
                result.exceptions[i] = e
            }
        }
        return result
    }

    @Throws(MicroBlogException::class, UploadException::class)
    private fun fanfouUpdateStatusWithPhoto(microBlog: MicroBlog, statusUpdate: ParcelableStatusUpdate,
            pendingUpdate: PendingStatusUpdate, sizeLimit: SizeLimit?, updateIndex: Int): ParcelableStatus {
        if (statusUpdate.media.size > 1) {
            throw MicroBlogException(context.getString(R.string.error_too_many_photos_fanfou))
        }
        val media = statusUpdate.media.first()
        try {
            val details = statusUpdate.accounts[updateIndex]
            return getBodyFromMedia(context, media, sizeLimit, false, ContentLengthInputStream.ReadListener { length, position ->
                stateCallback.onUploadingProgressChanged(-1, position, length)
            }).use { (body) ->
                val photoUpdate = PhotoStatusUpdate(body, pendingUpdate.overrideTexts[updateIndex])
                return@use microBlog.uploadPhoto(photoUpdate)
            }.toParcelable(details)
        } catch (e: IOException) {
            throw UploadException(e)
        }
    }

    /**
     * Calling Twitter's upload method. This method sets multiple owner for bandwidth saving
     */
    @Throws(UploadException::class)
    private fun uploadMediaWithDefaultProvider(update: ParcelableStatusUpdate, pendingUpdate: PendingStatusUpdate) {
        // Return empty array if no media attached
        if (update.media.isNullOrEmpty()) return
        val ownersList = update.accounts.filter {
            AccountType.TWITTER == it.type
        }.map(AccountDetails::key)
        val ownerIds = ownersList.map {
            it.id
        }.toTypedArray()
        for (i in 0 until pendingUpdate.length) {
            val account = update.accounts[i]
            val mediaIds: Array<String>?
            when (account.type) {
                AccountType.TWITTER -> {
                    val upload = account.newMicroBlogInstance(context, cls = TwitterUpload::class.java)
                    if (pendingUpdate.sharedMediaIds != null) {
                        mediaIds = pendingUpdate.sharedMediaIds
                    } else {
                        val (ids, deleteOnSuccess, deleteAlways) = uploadMicroBlogMediaShared(context,
                                upload, account, update.media, null, ownerIds, true, stateCallback)
                        mediaIds = ids
                        deleteOnSuccess.addAllTo(pendingUpdate.deleteOnSuccess)
                        deleteAlways.addAllTo(pendingUpdate.deleteAlways)
                        pendingUpdate.sharedMediaIds = mediaIds
                    }
                }
                AccountType.FANFOU -> {
                    // Nope, fanfou uses photo uploading API
                    mediaIds = null
                }
                AccountType.STATUSNET -> {
                    // TODO use their native API
                    val upload = account.newMicroBlogInstance(context, cls = TwitterUpload::class.java)
                    val (ids, deleteOnSuccess, deleteAlways) = uploadMicroBlogMediaShared(context,
                            upload, account, update.media, null, ownerIds, false, stateCallback)
                    mediaIds = ids
                    deleteOnSuccess.addAllTo(pendingUpdate.deleteOnSuccess)
                    deleteAlways.addAllTo(pendingUpdate.deleteAlways)
                }
                AccountType.MASTODON -> {
                    val mastodon = account.newMicroBlogInstance(context, cls = Mastodon::class.java)
                    val (ids, deleteOnSuccess, deleteAlways) = uploadMastodonMedia(context,
                            mastodon, account, update.media, false, stateCallback)
                    mediaIds = ids
                    deleteOnSuccess.addAllTo(pendingUpdate.deleteOnSuccess)
                    deleteAlways.addAllTo(pendingUpdate.deleteAlways)
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
            pendingUpdate: PendingStatusUpdate, index: Int): ParcelableStatus {
        val overrideText = pendingUpdate.overrideTexts[index]
        val status = StatusUpdate(overrideText)
        val inReplyToStatus = statusUpdate.in_reply_to_status

        val details = statusUpdate.accounts[index]
        if (statusUpdate.draft_action == Draft.Action.REPLY && inReplyToStatus != null) {
            status.inReplyToStatusId(inReplyToStatus.id)

            if (details.type == AccountType.TWITTER && statusUpdate.extended_reply_mode) {
                status.autoPopulateReplyMetadata(true)
                if (statusUpdate.excluded_reply_user_ids.isNotNullOrEmpty()) {
                    status.excludeReplyUserIds(statusUpdate.excluded_reply_user_ids)
                }
            }
        }
        if (statusUpdate.repost_status_id != null) {
            status.repostStatusId(statusUpdate.repost_status_id)
        }
        if (statusUpdate.attachment_url != null) {
            status.attachmentUrl(statusUpdate.attachment_url)
        }
        if (statusUpdate.location != null) {
            status.location(ParcelableLocationUtils.toGeoLocation(statusUpdate.location))
            status.displayCoordinates(statusUpdate.display_coordinates)
        }
        val mediaIds = pendingUpdate.mediaIds[index]
        if (mediaIds != null) {
            status.mediaIds(mediaIds)
        }
        if (statusUpdate.is_possibly_sensitive) {
            status.possiblySensitive(statusUpdate.is_possibly_sensitive)
        }
        return microBlog.updateStatus(status).toParcelable(details)
    }

    @Throws(MicroBlogException::class)
    private fun mastodonUpdateStatus(mastodon: Mastodon, statusUpdate: ParcelableStatusUpdate,
            pendingUpdate: PendingStatusUpdate, index: Int): ParcelableStatus {
        val overrideText = pendingUpdate.overrideTexts[index]
        val status = MastodonStatusUpdate(overrideText)
        val inReplyToStatus = statusUpdate.in_reply_to_status

        val details = statusUpdate.accounts[index]
        if (statusUpdate.draft_action == Draft.Action.REPLY && inReplyToStatus != null) {
            status.inReplyToId(inReplyToStatus.originalId)
        }
        val mediaIds = pendingUpdate.mediaIds[index]
        if (mediaIds != null) {
            status.mediaIds(mediaIds)
        }
        if (statusUpdate.is_possibly_sensitive) {
            status.sensitive(statusUpdate.is_possibly_sensitive)
        }
        if (statusUpdate.summary != null) {
            status.spoilerText(statusUpdate.summary)
        }
        if (statusUpdate.visibility != null) {
            status.visibility(statusUpdate.visibility)
        }
        return mastodon.postStatus(status).toParcelable(details)
    }

    private fun statusShortenCallback(shortener: StatusShortenerInterface?,
            pendingUpdate: PendingStatusUpdate, updateResult: UpdateStatusResult) {
        if (shortener == null || !shortener.waitForService()) return
        for (i in 0 until pendingUpdate.length) {
            val shortenResult = pendingUpdate.statusShortenResults[i]
            val status = updateResult.statuses[i]
            if (shortenResult == null || status == null) continue
            shortener.callback(shortenResult, status)
        }
    }

    private fun mediaUploadCallback(uploader: MediaUploaderInterface?,
            pendingUpdate: PendingStatusUpdate, updateResult: UpdateStatusResult) {
        if (uploader == null || !uploader.waitForService()) return
        for (i in 0 until pendingUpdate.length) {
            val uploadResult = pendingUpdate.mediaUploadResults[i]
            val status = updateResult.statuses[i]
            if (uploadResult == null || status == null) continue
            uploader.callback(uploadResult, status)
        }
    }

    @Throws(UploaderNotFoundException::class, UploadException::class, ShortenerNotFoundException::class, ShortenException::class)
    private fun getStatusShortener(app: TwidereApplication): StatusShortenerInterface? {
        val shortenerComponent = preferences.getString(KEY_STATUS_SHORTENER, null)
        if (ComponentPickerPreference.isNoneValue(shortenerComponent)) return null

        val shortener = StatusShortenerInterface.getInstance(app, shortenerComponent) ?: throw ShortenerNotFoundException()
        try {
            shortener.checkService { metaData ->
                if (metaData == null) throw ExtensionVersionMismatchException()
                val extensionVersion = metaData.getString(METADATA_KEY_EXTENSION_VERSION_STATUS_SHORTENER)
                if (!TextUtils.equals(extensionVersion, context.getString(R.string.status_shortener_service_interface_version))) {
                    throw ExtensionVersionMismatchException()
                }
            }
        } catch (e: ExtensionVersionMismatchException) {
            throw ShortenException(context.getString(R.string.shortener_version_incompatible))
        } catch (e: AbsServiceInterface.CheckServiceException) {
            throw ShortenException(e)
        }
        return shortener
    }

    @Throws(UploaderNotFoundException::class, UploadException::class)
    private fun getMediaUploader(app: TwidereApplication): MediaUploaderInterface? {
        val uploaderComponent = preferences.getString(KEY_MEDIA_UPLOADER, null)
        if (ComponentPickerPreference.isNoneValue(uploaderComponent)) return null
        val uploader = MediaUploaderInterface.getInstance(app, uploaderComponent) ?:
                throw UploaderNotFoundException(context.getString(R.string.error_message_media_uploader_not_found))
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
                throw UploadException(context.getString(R.string.uploader_version_incompatible), e)
            }
            throw UploadException(e)
        }

        return uploader
    }

    private fun isDuplicate(exception: Exception): Boolean {
        return exception is MicroBlogException && exception.errorCode == ErrorInfo.STATUS_IS_DUPLICATE
    }

    class PendingStatusUpdate internal constructor(val length: Int, defaultText: String) {

        internal constructor(statusUpdate: ParcelableStatusUpdate) : this(statusUpdate.accounts.size,
                statusUpdate.text)

        var sharedMediaIds: Array<String>? = null
        var sharedMediaOwners: Array<UserKey>? = null

        val overrideTexts: Array<String> = Array(length) { defaultText }
        val mediaIds: Array<Array<String>?> = arrayOfNulls(length)

        val mediaUploadResults: Array<MediaUploadResult?> = arrayOfNulls(length)
        val statusShortenResults: Array<StatusShortenResult?> = arrayOfNulls(length)

        val deleteOnSuccess: ArrayList<MediaDeletionItem> = arrayListOf()
        val deleteAlways: ArrayList<MediaDeletionItem> = arrayListOf()

    }

    class UpdateStatusResult {
        val statuses: Array<ParcelableStatus?>
        val exceptions: Array<MicroBlogException?>
        val accountTypes: Array<String?>

        val exception: UpdateStatusException?
        val draftId: Long

        val succeed: Boolean get() = exception == null && exceptions.none { it != null }

        constructor(count: Int, draftId: Long) {
            this.statuses = arrayOfNulls(count)
            this.exceptions = arrayOfNulls(count)
            this.accountTypes = arrayOfNulls(count)
            this.exception = null
            this.draftId = draftId
        }

        constructor(exception: UpdateStatusException, draftId: Long) {
            this.exception = exception
            this.statuses = arrayOfNulls(0)
            this.exceptions = arrayOfNulls(0)
            this.accountTypes = arrayOfNulls(0)
            this.draftId = draftId
        }
    }


    open class UpdateStatusException : Exception {
        protected constructor() : super()

        protected constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable)

        protected constructor(throwable: Throwable) : super(throwable)

        protected constructor(message: String) : super(message)
    }

    class UploaderNotFoundException(message: String) : UpdateStatusException(message)
    class SchedulerNotFoundException(message: String) : UpdateStatusException(message)

    class UploadException : UpdateStatusException {

        var deleteAlways: List<MediaDeletionItem>? = null

        constructor() : super()

        constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable)

        constructor(throwable: Throwable) : super(throwable)

        constructor(message: String) : super(message)
    }


    class ExtensionVersionMismatchException : AbsServiceInterface.CheckServiceException()

    class ShortenerNotFoundException : UpdateStatusException()

    class ShortenException : UpdateStatusException {

        constructor() : super()

        constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable)

        constructor(throwable: Throwable) : super(throwable)

        constructor(message: String) : super(message)
    }

    interface StateCallback : UploadCallback {

        @WorkerThread
        fun onShorteningStatus()

        @WorkerThread
        fun onUpdatingStatus()

        @UiThread
        fun afterExecute(result: UpdateStatusResult)

        @UiThread
        fun beforeExecute()
    }

    interface UploadCallback {
        @WorkerThread
        fun onStartUploadingMedia()

        @WorkerThread
        fun onUploadingProgressChanged(index: Int, current: Long, total: Long)

    }

    data class SizeLimit(
            val image: AccountExtras.ImageLimit,
            val video: AccountExtras.VideoLimit
    )

    data class MediaStreamBody(
            val body: Body,
            val geometry: Point?,
            val deleteOnSuccess: List<MediaDeletionItem>?,
            val deleteAlways: List<MediaDeletionItem>?
    ) : Closeable {
        override fun close() {
            body.close()
        }
    }

    interface MediaDeletionItem {
        fun delete(context: Context): Boolean
    }

    data class UriMediaDeletionItem(val uri: Uri) : MediaDeletionItem {
        override fun delete(context: Context): Boolean {
            return Utils.deleteMedia(context, uri)
        }
    }

    data class FileMediaDeletionItem(val file: File) : MediaDeletionItem {
        override fun delete(context: Context): Boolean {
            return file.delete()
        }

    }

    data class SharedMediaUploadResult(
            val ids: Array<String>,
            val deleteOnSuccess: List<MediaDeletionItem>,
            val deleteAlways: List<MediaDeletionItem>
    )

    companion object {

        private val BULK_SIZE = 512 * 1024// 512 Kib

        @Throws(UploadException::class)
        fun uploadMicroBlogMediaShared(context: Context, upload: TwitterUpload,
                account: AccountDetails, media: Array<ParcelableMediaUpdate>,
                mediaCategory: String? = null, ownerIds: Array<String>?, chucked: Boolean,
                callback: UploadCallback?): SharedMediaUploadResult {
            val deleteOnSuccess = ArrayList<MediaDeletionItem>()
            val deleteAlways = ArrayList<MediaDeletionItem>()
            val mediaIds = media.mapIndexedToArray { index, media ->
                val resp: MediaUploadResponse
                //noinspection TryWithIdenticalCatches
                var body: MediaStreamBody? = null
                try {
                    val sizeLimit = account.getMediaSizeLimit(mediaCategory)
                    body = getBodyFromMedia(context, media, sizeLimit, chucked,
                            ContentLengthInputStream.ReadListener { length, position ->
                                callback?.onUploadingProgressChanged(index, position, length)
                            })
                    resp = if (chucked) {
                        uploadMediaChucked(upload, body.body, mediaCategory, ownerIds)
                    } else {
                        upload.uploadMedia(body.body, ownerIds)
                    }
                } catch (e: IOException) {
                    throw UploadException(e).apply {
                        this.deleteAlways = deleteAlways
                    }
                } catch (e: MicroBlogException) {
                    throw UploadException(e).apply {
                        this.deleteAlways = deleteAlways
                    }
                } finally {
                    body?.close()
                }
                body?.deleteOnSuccess?.addAllTo(deleteOnSuccess)
                body?.deleteAlways?.addAllTo(deleteAlways)
                if (media.alt_text?.isNotEmpty() == true) {
                    try {
                        upload.createMetadata(NewMediaMetadata(resp.id, media.alt_text))
                    } catch (e: MicroBlogException) {
                        // Ignore
                    }
                }
                return@mapIndexedToArray resp.id
            }
            return SharedMediaUploadResult(mediaIds, deleteOnSuccess, deleteAlways)
        }

        @Throws(UploadException::class)
        fun uploadMastodonMedia(context: Context, mastodon: Mastodon,
                account: AccountDetails, media: Array<ParcelableMediaUpdate>,
                chucked: Boolean, callback: UploadCallback?): SharedMediaUploadResult {
            val deleteOnSuccess = ArrayList<MediaDeletionItem>()
            val deleteAlways = ArrayList<MediaDeletionItem>()
            val mediaIds = media.mapIndexedToArray { index, media ->
                val resp: Attachment
                //noinspection TryWithIdenticalCatches
                var body: MediaStreamBody? = null
                try {
                    val sizeLimit = account.getMediaSizeLimit()
                    body = getBodyFromMedia(context, media, sizeLimit, chucked,
                            ContentLengthInputStream.ReadListener { length, position ->
                                callback?.onUploadingProgressChanged(index, position, length)
                            })
                    resp = mastodon.uploadMediaAttachment(body.body)
                } catch (e: IOException) {
                    throw UploadException(e).apply {
                        this.deleteAlways = deleteAlways
                    }
                } catch (e: MicroBlogException) {
                    throw UploadException(e).apply {
                        this.deleteAlways = deleteAlways
                    }
                } finally {
                    body?.close()
                }
                body?.deleteOnSuccess?.addAllTo(deleteOnSuccess)
                body?.deleteAlways?.addAllTo(deleteAlways)
                return@mapIndexedToArray resp.id
            }
            return SharedMediaUploadResult(mediaIds, deleteOnSuccess, deleteAlways)
        }

        @Throws(IOException::class)
        fun getBodyFromMedia(context: Context, media: ParcelableMediaUpdate, sizeLimit: SizeLimit? = null,
                chucked: Boolean, readListener: ContentLengthInputStream.ReadListener): MediaStreamBody {
            return getBodyFromMedia(context, Uri.parse(media.uri), media.type, media.delete_always,
                    media.delete_on_success, sizeLimit, chucked, readListener)
        }

        @Throws(IOException::class)
        fun getBodyFromMedia(context: Context, mediaUri: Uri, @ParcelableMedia.Type type: Int,
                isDeleteAlways: Boolean, isDeleteOnSuccess: Boolean, sizeLimit: SizeLimit? = null,
                chucked: Boolean, readListener: ContentLengthInputStream.ReadListener?): MediaStreamBody {
            val deleteOnSuccessList: MutableList<MediaDeletionItem> = mutableListOf()
            val deleteAlwaysList: MutableList<MediaDeletionItem> = mutableListOf()

            val deletionItem = UriMediaDeletionItem(mediaUri)
            if (isDeleteAlways) {
                deleteAlwaysList.add(deletionItem)
            } else if (isDeleteOnSuccess) {
                deleteOnSuccessList.add(deletionItem)
            }
            try {
                val resolver = context.contentResolver
                val mediaType = resolver.getType(mediaUri) ?: run {
                    return@run mediaUri.lastPathSegment?.substringAfterLast(".")?.let { ext ->
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                    }
                }

                val data = when (type) {
                    ParcelableMedia.Type.IMAGE -> imageStream(context, resolver, mediaUri, mediaType,
                            sizeLimit?.image, chucked)
                    ParcelableMedia.Type.VIDEO -> videoStream(context, resolver, mediaUri, mediaType,
                            sizeLimit?.video, chucked)
                    else -> null
                }

                val cis = data?.stream ?: run {
                    val st = resolver.openInputStream(mediaUri) ?: throw FileNotFoundException(mediaUri.toString())
                    val length = st.available().toLong()
                    return@run ContentLengthInputStream(st, length)
                }
                cis.setReadListener(readListener)
                val mimeType = data?.type ?: mediaType ?: "application/octet-stream"
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ".bin"
                val fileName = mediaUri.lastPathSegment?.substringBeforeLast(".") ?: "attachment"
                val body = FileBody(cis, "$fileName.$extension", cis.length(), ContentType.parse(mimeType))

                data?.deleteOnSuccess?.addAllTo(deleteOnSuccessList)
                data?.deleteAlways?.addAllTo(deleteAlwaysList)
                return MediaStreamBody(body, data?.geometry, deleteOnSuccessList, deleteAlwaysList)
            } finally {
                if (isDeleteAlways) {
                    deleteAlwaysList.forEach { it.delete(context) }
                }
            }
        }


        @Throws(IOException::class, MicroBlogException::class)
        private fun uploadMediaChucked(upload: TwitterUpload, body: Body,
                mediaCategory: String? = null, ownerIds: Array<String>?): MediaUploadResponse {
            val mediaType = body.contentType().contentType
            val length = body.length()
            val stream = body.stream()
            var response = upload.initUploadMedia(mediaType, length, mediaCategory, ownerIds)
            val segments = if (length == 0L) 0 else (length / BULK_SIZE + 1).toInt()
            for (segmentIndex in 0 until segments) {
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

        private fun shouldWaitForProcess(info: MediaUploadResponse.ProcessingInfo): Boolean {
            when (info.state) {
                MediaUploadResponse.ProcessingInfo.State.PENDING, MediaUploadResponse.ProcessingInfo.State.IN_PROGRESS -> return true
                else -> return false
            }
        }

        private fun imageStream(
                context: Context,
                resolver: ContentResolver,
                mediaUri: Uri,
                defaultType: String?,
                imageLimit: AccountExtras.ImageLimit?,
                chucked: Boolean
        ): MediaStreamData? {
            var mediaType = defaultType
            val o = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            var imageSize = -1L
            resolver.openInputStream(mediaUri).use {
                imageSize = it.available().toLong()
                BitmapFactory.decodeStream(it, null, o)
            }
            BitmapFactoryUtils.decodeUri(resolver, mediaUri, opts = o)
            // Try to use decoded media type
            if (o.outMimeType != null) {
                mediaType = o.outMimeType
            }
            // Skip if media is GIF
            if (o.outWidth <= 0 || o.outHeight <= 0 || mediaType == "image/gif") {
                return null
            }

            if (imageLimit == null || (imageLimit.checkGeomentry(o.outWidth, o.outHeight)
                    && imageLimit.checkSize(imageSize, chucked))) return null
            o.inSampleSize = o.calculateInSampleSize(imageLimit.maxWidth, imageLimit.maxHeight)
            o.inJustDecodeBounds = false
            // Do actual image decoding
            val bitmap = try {
                context.contentResolver.openInputStream(mediaUri).use {
                    BitmapFactory.decodeStream(it, null, o)
                } ?: return null
            } catch (oome: OutOfMemoryError) {
                Analyzer.logException(OutOfMemoryError("OOM Image size: $imageSize, width: ${o.outWidth}, height: ${o.outHeight}, type: ${o.outMimeType}"))
                return null
            }

            val size = Point(bitmap.width, bitmap.height)
            val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mediaType)
            val tempFile = File.createTempFile("twidere__scaled_image_", ".$ext", context.cacheDir)

            when (mediaType) {
                "image/png", "image/x-png", "image/webp", "image-x-webp" -> {
                    tempFile.outputStream().use { os ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                    }
                }
                "image/jpeg" -> {
                    val origExif = context.contentResolver.openInputStream(mediaUri)
                            .use(::ExifInterface)
                    tempFile.outputStream().use { os ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, os)
                        os.flush()
                    }
                    val orientation = origExif.getAttribute(ExifInterface.TAG_ORIENTATION)
                    if (orientation != null) {
                        ExifInterface(tempFile.absolutePath).apply {
                            setAttribute(ExifInterface.TAG_ORIENTATION, orientation)
                            saveAttributes()
                        }
                    }
                }
                else -> {
                    tempFile.outputStream().use { os ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, os)
                    }
                }
            }
            return MediaStreamData(ContentLengthInputStream(tempFile), mediaType, size,
                    null, listOf(FileMediaDeletionItem(tempFile)))
        }

        private fun videoStream(
                context: Context,
                resolver: ContentResolver,
                mediaUri: Uri,
                defaultType: String?,
                videoLimit: AccountExtras.VideoLimit?,
                chucked: Boolean
        ): MediaStreamData? {

            // No limit, upload original
            if (videoLimit == null) {
                return null
            }

            if (!videoLimit.isSupported) {
                throw IOException(context.getString(R.string.error_message_video_upload_not_supported))
            }

            var mediaType = defaultType
            val geometry = Point()
            var duration = -1L
            var framerate = -1.0
            var size = -1L
            // TODO only transcode video if needed, use `MediaMetadataRetriever`
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, mediaUri)
                val extractedMimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                if (extractedMimeType != null) {
                    mediaType = extractedMimeType
                }
                geometry.x = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toIntOr(-1)
                geometry.y = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toIntOr(-1)
                duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLongOr(-1)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    framerate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE).toDoubleOr(-1.0)
                }

                size = resolver.openFileDescriptor(mediaUri, "r").use { it.statSize }
            } catch (e: Exception) {
                DebugLog.w(LOGTAG, "Unable to retrieve video info", e)
            } finally {
                retriever.releaseSafe()
            }

            if (geometry.x > 0 && geometry.y > 0 && videoLimit.checkGeometry(geometry.x, geometry.y)
                    && framerate > 0 && videoLimit.checkFrameRate(framerate)
                    && size > 0 && videoLimit.checkSize(size, chucked)) {
                // Size valid, upload directly
                DebugLog.d(LOGTAG, "Upload video directly")
                return null
            }

            if (!videoLimit.checkMinDuration(duration, chucked)) {
                throw UploadException(context.getString(R.string.message_video_too_short))
            }

            if (!videoLimit.checkMaxDuration(duration, chucked)) {
                throw UploadException(context.getString(R.string.message_video_too_long))
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                // Go get a new phone
                return null
            }
            DebugLog.d(LOGTAG, "Transcoding video")

            val ext = mediaUri.lastPathSegment.substringAfterLast(".")
            val strategy = MediaFormatStrategyPresets.createAndroid720pStrategy()
            val listener = object : MediaTranscoder.Listener {
                override fun onTranscodeFailed(exception: Exception?) {
                }

                override fun onTranscodeCompleted() {
                }

                override fun onTranscodeProgress(progress: Double) {
                }

                override fun onTranscodeCanceled() {
                }

            }
            val pfd = resolver.openFileDescriptor(mediaUri, "r")
            val tempFile = File.createTempFile("twidere__encoded_video_", ".$ext", context.cacheDir)
            val future = MediaTranscoder.getInstance().transcodeVideo(pfd.fileDescriptor,
                    tempFile.absolutePath, strategy, listener)
            try {
                future.get()
            } catch (e: Exception) {
                DebugLog.w(LOGTAG, "Error transcoding video, try upload directly", e)
                tempFile.delete()
                return null
            }
            return MediaStreamData(ContentLengthInputStream(tempFile.inputStream(), tempFile.length()),
                    mediaType, geometry, null, listOf(FileMediaDeletionItem(tempFile)))
        }

        internal class MediaStreamData(
                val stream: ContentLengthInputStream?,
                val type: String?,
                val geometry: Point?,
                val deleteOnSuccess: List<MediaDeletionItem>?,
                val deleteAlways: List<MediaDeletionItem>?
        )

        inline fun createDraft(@Draft.Action action: String, config: Draft.() -> Unit): Draft {
            val draft = Draft()
            draft.action_type = action
            draft.timestamp = System.currentTimeMillis()
            config(draft)
            return draft
        }

        fun saveDraft(context: Context, @Draft.Action action: String, config: Draft.() -> Unit): Long {
            val draft = createDraft(action, config)
            val resolver = context.contentResolver
            val creator = ObjectCursor.valuesCreatorFrom(Draft::class.java)
            val draftUri = resolver.insert(Drafts.CONTENT_URI, creator.create(draft)) ?: return -1
            return draftUri.lastPathSegment.toLongOr(-1)
        }


        fun deleteDraft(context: Context, id: Long) {
            val where = Expression.equals(Drafts._ID, id).sql
            context.contentResolver.delete(Drafts.CONTENT_URI, where, null)
        }

        fun AccountExtras.ImageLimit.checkGeomentry(width: Int, height: Int): Boolean {
            if (this.maxWidth <= 0 || this.maxHeight <= 0) return true
            return (width <= this.maxWidth && height <= this.maxHeight) || (height <= this.maxWidth
                    && width <= this.maxHeight)
        }

    }

}

