package org.mariotaku.twidere.util.sync

import android.content.Context
import android.util.Xml
import org.mariotaku.ktextension.contentEquals
import org.mariotaku.ktextension.removeAll
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.util.*

abstract class FileBasedKeyValueSyncAction<DownloadSession : Closeable, UploadSession : Closeable>(
        val context: Context
) : SingleFileBasedDataSyncAction<MutableMap<String, String>, File, DownloadSession, UploadSession>(), ISyncAction {

    private val TAG_ITEMS = "items"
    private val TAG_ITEM = "item"

    private val ATTR_KEY = "key"

    protected abstract val snapshotFileName: String

    final override fun MutableMap<String, String>.addAllData(data: MutableMap<String, String>): Boolean {
        this.putAll(data)
        return true
    }

    final override fun MutableMap<String, String>.removeAllData(data: MutableMap<String, String>): Boolean {
        this.removeAll(data.keys)
        return true
    }

    final override fun MutableMap<String, String>.minus(data: MutableMap<String, String>): MutableMap<String, String> {
        val diff = HashMap<String, String>()
        for ((k, v) in this) {
            val dv = data[k]
            if (v != dv) {
                diff[k] = v
            }
        }
        return diff
    }

    final override fun File.loadSnapshot(): HashMap<String, String> {
        return reader().use {
            val snapshot = HashMap<String, String>()
            val parser = Xml.newPullParser()
            parser.setInput(it)
            snapshot.parse(parser)
            return@use snapshot
        }
    }

    final override fun File.saveSnapshot(data: MutableMap<String, String>) {
        writer().use {
            val serializer = Xml.newSerializer()
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            serializer.setOutput(it)
            data.serialize(serializer)
        }
    }

    final override var File.snapshotLastModified: Long
        get() = this.lastModified()
        set(value) {
            this.setLastModified(value)
        }

    final override fun newData(): MutableMap<String, String> = HashMap()

    final override fun newSnapshotStore(): File {
        val syncDataDir: File = context.syncDataDir.mkdirIfNotExists() ?: throw IOException()
        return File(syncDataDir, snapshotFileName)
    }

    final override fun MutableMap<String, String>.isDataEmpty(): Boolean = this.isEmpty()

    override fun MutableMap<String, String>.dataContentEquals(localData: MutableMap<String, String>): Boolean {
        return this.entries.contentEquals(localData.entries)
    }

    protected fun Map<String, String>.serialize(serializer: XmlSerializer) {
        serializer.startDocument("utf-8", true)
        serializer.startTag(null, TAG_ITEMS)
        for ((k, v) in this) {
            serializer.startTag(null, TAG_ITEM)
            serializer.attribute(null, ATTR_KEY, k)
            serializer.text(v)
            serializer.endTag(null, TAG_ITEM)
        }
        serializer.endTag(null, TAG_ITEMS)
        serializer.endDocument()
    }

    protected fun MutableMap<String, String>.parse(parser: XmlPullParser) {
        val stack = Stack<Any?>()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    stack.push(when (parser.name) {
                        TAG_ITEM -> DataEntry(parser.getAttributeValue(null, ATTR_KEY))
                        else -> null
                    })
                }
                XmlPullParser.END_TAG -> {
                    val obj = stack.pop()
                    when (parser.name) {
                        TAG_ITEM -> (obj as? DataEntry)?.let { put(it.key, it.value) }
                    }
                }
                XmlPullParser.TEXT -> {
                    stack.push(run {
                        val obj = stack.pop()
                        when (obj) {
                            is DataEntry -> {
                                obj.value = parser.text ?: return@run null
                            }
                        }
                        return@run obj
                    })
                }
            }
            event = parser.next()
        }
    }

    private data class DataEntry(val key: String) {
        lateinit var value: String
    }

}
