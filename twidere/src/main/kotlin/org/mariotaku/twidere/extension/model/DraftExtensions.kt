package org.mariotaku.twidere.extension.model

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import org.apache.james.mime4j.dom.Header
import org.apache.james.mime4j.dom.MessageServiceFactory
import org.apache.james.mime4j.dom.address.Mailbox
import org.apache.james.mime4j.dom.field.*
import org.apache.james.mime4j.message.*
import org.apache.james.mime4j.parser.MimeStreamParser
import org.apache.james.mime4j.storage.Storage
import org.apache.james.mime4j.storage.StorageBodyFactory
import org.apache.james.mime4j.storage.TempFileStorageProvider
import org.apache.james.mime4j.stream.BodyDescriptor
import org.apache.james.mime4j.stream.MimeConfig
import org.apache.james.mime4j.stream.RawField
import org.apache.james.mime4j.util.MimeUtil
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.ktextension.toString
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.mime4j.getBooleanParameter
import org.mariotaku.twidere.extension.mime4j.getIntParameter
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.Draft.Action
import org.mariotaku.twidere.model.draft.SendDirectMessageActionExtras
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtras
import org.mariotaku.twidere.util.JsonSerializer
import org.mariotaku.twidere.util.collection.NonEmptyHashMap
import org.mariotaku.twidere.util.sync.mkdirIfNotExists
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList


val Draft.filename: String get() = "$unique_id_non_null.eml"

val Draft.unique_id_non_null: String
    get() = unique_id ?: UUID.nameUUIDFromBytes(("$_id:$timestamp").toByteArray()).toString()

fun Draft.writeMimeMessageTo(context: Context, st: OutputStream) {
    val cacheDir = File(context.cacheDir, "mime_cache").apply { mkdirIfNotExists() }
    val bodyFactory = StorageBodyFactory(TempFileStorageProvider("draft_media_", null,
            cacheDir), null)
    val storageProvider = bodyFactory.storageProvider
    val contentResolver = context.contentResolver

    val factory = MessageServiceFactory.newInstance()
    val builder = factory.newMessageBuilder()
    val writer = factory.newMessageWriter()

    val message = builder.newMessage() as AbstractMessage

    message.date = Date(this.timestamp)
    message.subject = this.getActionName(context)
    message.setFrom(this.account_keys?.map { Mailbox(it.id, it.host) })
    message.setTo(message.from)
    if (message.header == null) {
        message.header = HeaderImpl()
    }

    this.location?.let { location ->
        message.header.addField(RawField("X-GeoLocation", location.toString()))
    }
    this.action_type?.let { type ->
        message.header.addField(RawField("X-Action-Type", type))
    }

    val multipart = MultipartImpl("mixed")
    multipart.addBodyPart(BodyPart().apply {
        setText(bodyFactory.textBody(this@writeMimeMessageTo.text, Charsets.UTF_8.name()))
    })

    this.action_extras?.let { extras ->
        multipart.addBodyPart(BodyPart().apply {
            setText(bodyFactory.textBody(JsonSerializer.serialize(extras)), "json")
            this.filename = "twidere.action.extras.json"
        })
    }

    val storageList = ArrayList<Storage>()
    try {
        this.media?.forEach { mediaItem ->
            multipart.addBodyPart(BodyPart().apply {
                val uri = Uri.parse(mediaItem.uri)
                val mimeType = mediaItem.getMimeType(contentResolver) ?: "application/octet-stream"
                val parameters = NonEmptyHashMap<String, String?>()
                parameters["alt_text"] = mediaItem.alt_text
                parameters["media_type"] = mediaItem.type.toString()
                parameters["delete_on_success"] = mediaItem.delete_on_success.toString()
                parameters["delete_always"] = mediaItem.delete_always.toString()
                val storage = contentResolver.openInputStream(uri).use { storageProvider.store(it) }
                this.filename = uri.lastPathSegment
                this.contentTransferEncoding = MimeUtil.ENC_BASE64
                this.setBody(bodyFactory.binaryBody(storage), mimeType, parameters)
                storageList.add(storage)
            })
        }

        message.setMultipart(multipart)
        writer.writeMessage(message, st)
        st.flush()
    } finally {
        storageList.forEach(Storage::delete)
    }
}

fun Draft.readMimeMessageFrom(context: Context, st: InputStream): Boolean {
    val config = MimeConfig()
    val parser = MimeStreamParser(config)
    parser.isContentDecoding = true
    val handler = DraftContentHandler(context, this)
    parser.setContentHandler(handler)
    parser.parse(st)
    return !handler.malformedData
}

fun Draft.getActionName(context: Context): String? {
    if (TextUtils.isEmpty(action_type)) return context.getString(R.string.update_status)
    when (action_type) {
        Action.UPDATE_STATUS, Action.UPDATE_STATUS_COMPAT_1,
        Action.UPDATE_STATUS_COMPAT_2 -> {
            return context.getString(R.string.update_status)
        }
        Action.REPLY -> {
            return context.getString(R.string.action_reply)
        }
        Action.QUOTE -> {
            return context.getString(R.string.action_quote)
        }
        Action.FAVORITE -> {
            return context.getString(R.string.action_favorite)
        }
        Action.RETWEET -> {
            return context.getString(R.string.action_retweet)
        }
        Action.SEND_DIRECT_MESSAGE, Action.SEND_DIRECT_MESSAGE_COMPAT -> {
            return context.getString(R.string.send_direct_message)
        }
    }
    return null
}

fun Draft.applyUpdateStatus(statusUpdate: ParcelableStatusUpdate) {
    this.unique_id = statusUpdate.draft_unique_id ?: UUID.randomUUID().toString()
    this.account_keys = statusUpdate.accounts.mapToArray { it.key }
    this.text = statusUpdate.text
    this.location = statusUpdate.location
    this.media = statusUpdate.media
    this.timestamp = System.currentTimeMillis()
    this.action_extras = statusUpdate.draft_extras
}

fun draftActionTypeString(@Draft.Action action: String?): String {
    return when (action) {
        Action.QUOTE -> "quote"
        Action.REPLY -> "reply"
        else -> "tweet"
    }
}

private class DraftContentHandler(private val context: Context, private val draft: Draft) : SimpleContentHandler() {
    private val processingStack = Stack<SimpleContentHandler>()
    private val mediaList: MutableList<ParcelableMediaUpdate> = ArrayList()

    internal var malformedData: Boolean = false
    override fun headers(header: Header) {
        if (processingStack.isEmpty()) {
            draft.timestamp = header.getField("Date")?.let {
                (it as DateTimeField).date.time
            } ?: 0
            draft.account_keys = header.getField("From")?.let { field ->
                when (field) {
                    is MailboxField -> {
                        return@let arrayOf(field.mailbox.let { UserKey(it.localPart, it.domain) })
                    }
                    is MailboxListField -> {
                        return@let field.mailboxList.mapToArray { UserKey(it.localPart, it.domain) }
                    }
                    else -> {
                        return@let null
                    }
                }
            }
            draft.location = header.getField("X-GeoLocation")?.body?.let(ParcelableLocation::valueOf)
            draft.action_type = header.getField("X-Action-Type")?.body
        } else {
            processingStack.peek().headers(header)
        }
    }

    override fun startMultipart(bd: BodyDescriptor) {
    }

    override fun preamble(`is`: InputStream?) {
        processingStack.peek().preamble(`is`)
    }

    override fun startBodyPart() {
        processingStack.push(BodyPartHandler(context, draft))
    }

    override fun body(bd: BodyDescriptor?, `is`: InputStream?) {
        if (processingStack.isEmpty()) {
            malformedData = true
            return
        }
        processingStack.peek().body(bd, `is`)
    }

    override fun endBodyPart() {
        val handler = processingStack.pop() as BodyPartHandler
        handler.media?.let {
            mediaList.add(it)
        }
    }

    override fun epilogue(`is`: InputStream?) {
        processingStack.peek().epilogue(`is`)
    }

    override fun endMultipart() {
        draft.media = mediaList.toTypedArray()
    }
}


private class BodyPartHandler(private val context: Context, private val draft: Draft) : SimpleContentHandler() {
    internal lateinit var header: Header
    internal var media: ParcelableMediaUpdate? = null

    override fun headers(header: Header) {
        this.header = header
    }

    override fun body(bd: BodyDescriptor, st: InputStream) {
        body(header, bd, st)
    }

    fun body(header: Header, bd: BodyDescriptor, st: InputStream) {
        val contentDisposition = header.getField("Content-Disposition") as? ContentDispositionField
        if (contentDisposition != null && contentDisposition.isAttachment) {
            when (contentDisposition.filename) {
                "twidere.action.extras.json" -> {
                    draft.action_extras = when (draft.action_type) {
                        "0", "1", Action.UPDATE_STATUS, Action.REPLY, Action.QUOTE -> {
                            JsonSerializer.parse(st, UpdateStatusActionExtras::class.java)
                        }
                        "2", Action.SEND_DIRECT_MESSAGE -> {
                            JsonSerializer.parse(st, SendDirectMessageActionExtras::class.java)
                        }
                        else -> {
                            null
                        }
                    }
                }
                else -> {
                    val contentType = header.getField("Content-Type") as? ContentTypeField
                    val filename = contentDisposition.filename ?: return
                    val mediaFile = File(context.filesDir, filename)
                    media = ParcelableMediaUpdate().apply {
                        if (contentType != null) {
                            this.type = contentType.getIntParameter("media_type",
                                    ParcelableMedia.Type.UNKNOWN)
                            this.alt_text = contentType.getParameter("alt_text")
                            this.delete_on_success = contentType.getBooleanParameter("delete_on_success")
                            this.delete_always = contentType.getBooleanParameter("delete_always")
                        } else {
                            this.type = ParcelableMedia.Type.UNKNOWN
                        }
                        FileOutputStream(mediaFile).use {
                            st.copyTo(it)
                            it.flush()
                        }
                        this.uri = Uri.fromFile(mediaFile).toString()
                    }
                }
            }
        } else if (bd.mimeType == "text/plain" && draft.text == null) {
            draft.text = st.toString(Charset.forName(bd.charset))
        }
    }
}
