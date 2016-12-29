package org.mariotaku.twidere.extension.model

import android.content.ContentResolver
import android.net.Uri
import org.mariotaku.ktextension.convert
import org.mariotaku.ktextension.map
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.`FiltersData$BaseItemCursorIndices`
import org.mariotaku.twidere.model.`FiltersData$UserItemCursorIndices`
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.IOException
import java.util.*

/**
 * Created by mariotaku on 2016/12/28.
 */

fun FiltersData.read(cr: ContentResolver) {
    fun readBaseItems(uri: Uri): List<FiltersData.BaseItem>? {
        val c = cr.query(uri, Filters.COLUMNS, null, null, null) ?: return null
        @Suppress("ConvertTryFinallyToUseCall")
        try {
            return c.map(`FiltersData$BaseItemCursorIndices`(c))
        } finally {
            c.close()
        }
    }
    this.users = run {
        val c = cr.query(Filters.Users.CONTENT_URI, Filters.Users.COLUMNS, null, null, null) ?: return@run null
        @Suppress("ConvertTryFinallyToUseCall")
        try {
            return@run c.map(`FiltersData$UserItemCursorIndices`(c))
        } finally {
            c.close()
        }
    }
    this.keywords = readBaseItems(Filters.Keywords.CONTENT_URI)
    this.sources = readBaseItems(Filters.Sources.CONTENT_URI)
    this.links = readBaseItems(Filters.Links.CONTENT_URI)
}

private const val TAG_FILTERS = "filters"
private const val TAG_KEYWORD = "keyword"
private const val TAG_SOURCE = "source"
private const val TAG_LINK = "link"
private const val TAG_USER = "user"

private const val ATTR_SCREEN_NAME = "screenName"
private const val ATTR_NAME = "name"
private const val ATTR_KEY = "key"


@Throws(IOException::class)
fun FiltersData.serialize(serializer: XmlSerializer) {

    @Throws(IOException::class)
    fun FiltersData.BaseItem.serialize(name: String, writer: XmlSerializer) {
        writer.startTag(null, name)
        writer.text(value)
        writer.endTag(null, name)
    }

    serializer.startDocument("utf-8", true)
    serializer.startTag(null, TAG_FILTERS)
    this.users?.forEach { user ->
        serializer.startTag(null, TAG_USER)
        serializer.attribute(null, ATTR_KEY, user.userKey.toString())
        serializer.attribute(null, ATTR_NAME, user.name)
        serializer.attribute(null, ATTR_SCREEN_NAME, user.screenName)
        serializer.endTag(null, TAG_USER)
    }
    this.keywords?.forEach { it.serialize(TAG_KEYWORD, serializer) }
    this.sources?.forEach { it.serialize(TAG_SOURCE, serializer) }
    this.links?.forEach { it.serialize(TAG_LINK, serializer) }
    serializer.endTag(null, TAG_FILTERS)
    serializer.endDocument()
}

@Throws(IOException::class)
fun FiltersData.parse(parser: XmlPullParser) {
    fun parseUserItem(parser: XmlPullParser): FiltersData.UserItem? {
        val item = FiltersData.UserItem()
        item.name = parser.getAttributeValue(null, ATTR_NAME) ?: return null
        item.screenName = parser.getAttributeValue(null, ATTR_SCREEN_NAME) ?: return null
        item.userKey = parser.getAttributeValue(null, ATTR_KEY)?.convert(UserKey::valueOf) ?: return null
        return item
    }

    var event = parser.eventType
    var stack = Stack<Any?>()
    while (event != XmlPullParser.END_DOCUMENT) {
        when (event) {
            XmlPullParser.START_DOCUMENT -> {
                if (users == null) {
                    users = ArrayList()
                }
                if (keywords == null) {
                    keywords = ArrayList()
                }
                if (sources == null) {
                    sources = ArrayList()
                }
                if (links == null) {
                    links = ArrayList()
                }
            }
            XmlPullParser.START_TAG -> {
                stack.push(when (parser.name) {
                    TAG_USER -> parseUserItem(parser)
                    TAG_KEYWORD, TAG_SOURCE, TAG_LINK -> FiltersData.BaseItem()
                    else -> null
                })
            }
            XmlPullParser.END_TAG -> {
                val obj = stack.pop()
                when (parser.name) {
                    TAG_USER -> (obj as? FiltersData.UserItem).let { users.add(it) }
                    TAG_KEYWORD -> (obj as? FiltersData.BaseItem).let { keywords.add(it) }
                    TAG_SOURCE -> (obj as? FiltersData.BaseItem).let { sources.add(it) }
                    TAG_LINK -> (obj as? FiltersData.BaseItem).let { links.add(it) }
                }
            }
            XmlPullParser.TEXT -> {
                stack.push(run {
                    val obj = stack.pop()
                    when (obj) {
                        is FiltersData.BaseItem -> {
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