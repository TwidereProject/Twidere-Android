package org.mariotaku.twidere.extension.model

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.bluelinelabs.logansquare.LoganSquare
import com.nostra13.universalimageloader.utils.IoUtils
import org.apache.james.mime4j.dom.Header
import org.apache.james.mime4j.dom.MessageServiceFactory
import org.apache.james.mime4j.dom.address.Mailbox
import org.apache.james.mime4j.dom.field.*
import org.apache.james.mime4j.message.*
import org.apache.james.mime4j.parser.MimeStreamParser
import org.apache.james.mime4j.storage.StorageBodyFactory
import org.apache.james.mime4j.stream.BodyDescriptor
import org.apache.james.mime4j.stream.MimeConfig
import org.apache.james.mime4j.stream.RawField
import org.apache.james.mime4j.util.MimeUtil
import org.mariotaku.ktextension.convert
import org.mariotaku.ktextension.toInt
import org.mariotaku.ktextension.toString
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.Draft.Action
import org.mariotaku.twidere.model.draft.SendDirectMessageActionExtras
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtras
import org.mariotaku.twidere.util.collection.NonEmptyHashMap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*


/**
 * Created by mariotaku on 2016/12/7.
 */
fun Draft.writeMimeMessageTo(context: Context, st: OutputStream) {
    val bodyFactory = StorageBodyFactory()
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
            setText(bodyFactory.textBody(LoganSquare.serialize(extras)), "json")
            this.filename = "twidere.action.extras.json"
        })
    }
    this.media?.forEach { mediaItem ->
        multipart.addBodyPart(BodyPart().apply {
            val uri = Uri.parse(mediaItem.uri)
            val mimeType = mediaItem.getMimeType(contentResolver) ?: "application/octet-stream"
            val parameters = NonEmptyHashMap<String, String?>()
            parameters["alt_text"] = mediaItem.alt_text
            parameters["media_type"] = mediaItem.type.toString()
            val storage = contentResolver.openInputStream(uri).use { storageProvider.store(it) }
            this.filename = uri.lastPathSegment
            this.contentTransferEncoding = MimeUtil.ENC_BASE64
            this.setBody(bodyFactory.binaryBody(storage), mimeType, parameters)
        })
    }

    message.setMultipart(multipart)
    writer.writeMessage(message, st)
    st.flush()
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
        Draft.Action.UPDATE_STATUS, Draft.Action.UPDATE_STATUS_COMPAT_1,
        Draft.Action.UPDATE_STATUS_COMPAT_2 -> {
            return context.getString(R.string.update_status)
        }
        Draft.Action.REPLY -> {
            return context.getString(R.string.action_reply)
        }
        Draft.Action.QUOTE -> {
            return context.getString(R.string.action_quote)
        }
        Draft.Action.FAVORITE -> {
            return context.getString(R.string.action_favorite)
        }
        Draft.Action.RETWEET -> {
            return context.getString(R.string.action_retweet)
        }
        Draft.Action.SEND_DIRECT_MESSAGE, Draft.Action.SEND_DIRECT_MESSAGE_COMPAT -> {
            return context.getString(R.string.send_direct_message)
        }
    }
    return null
}

val Draft.filename: String get() = "$unique_id_non_null.eml"

val Draft.unique_id_non_null: String
    get() = unique_id ?: UUID.nameUUIDFromBytes(("$_id:$timestamp").toByteArray()).toString()

private class DraftContentHandler(private val context: Context, private val draft: Draft) : SimpleContentHandler() {
    private val processingStack = Stack<SimpleContentHandler>()
    private val mediaList: MutableList<ParcelableMediaUpdate> = ArrayList()

    internal var malformedData: Boolean = false
    override fun headers(header: Header) {
        if (processingStack.isEmpty()) {
            draft.timestamp = header.getField("Date")?.convert {
                (it as DateTimeField).date.time
            } ?: 0
            draft.account_keys = header.getField("From")?.convert { field ->
                when (field) {
                    is MailboxField -> {
                        return@convert arrayOf(field.mailbox.convert { UserKey(it.localPart, it.domain) })
                    }
                    is MailboxListField -> {
                        return@convert field.mailboxList.map { UserKey(it.localPart, it.domain) }.toTypedArray()
                    }
                    else -> {
                        return@convert null
                    }
                }
            }
            draft.location = header.getField("X-GeoLocation")?.body?.convert(ParcelableLocation::valueOf)
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
                            LoganSquare.parse(st, UpdateStatusActionExtras::class.java)
                        }
                        "2", Action.SEND_DIRECT_MESSAGE -> {
                            LoganSquare.parse(st, SendDirectMessageActionExtras::class.java)
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
                        bd.transferEncoding
                        this.type = contentType?.getParameter("media_type").toInt(ParcelableMedia.Type.UNKNOWN)
                        this.alt_text = contentType?.getParameter("alt_text")
                        FileOutputStream(mediaFile).use {
                            IoUtils.copyStream(st, it, null)
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


fun draftActionTypeString(@Draft.Action action: String?): String {
    return when (action) {
        Draft.Action.QUOTE -> "quote"
        Draft.Action.REPLY -> "reply"
        else -> "tweet"
    }
}
