package org.mariotaku.twidere.extension.model

import android.content.ContentResolver
import android.net.Uri
import org.mariotaku.ktextension.addAllEnhanced
import org.mariotaku.ktextension.isNullOrEmpty
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.extension.queryAll
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.IOException
import java.util.*

/**
 * Created by mariotaku on 2016/12/28.
 */

fun FiltersData.read(cr: ContentResolver, loadSubscription: Boolean = false) {
    fun readBaseItems(uri: Uri): List<FiltersData.BaseItem>? {
        val where = if (loadSubscription) null else Expression.lesserThan(Filters.SOURCE, 0).sql
        return cr.queryAll(uri, Filters.COLUMNS, where, null, null,
                FiltersData.BaseItem::class.java)
    }
    this.users = run {
        val where = if (loadSubscription) null else Expression.lesserThan(Filters.Users.SOURCE, 0).sql
        return@run cr.queryAll(Filters.Users.CONTENT_URI, Filters.Users.COLUMNS, where, null,
                null, FiltersData.UserItem::class.java)
    }
    this.keywords = readBaseItems(Filters.Keywords.CONTENT_URI)
    this.sources = readBaseItems(Filters.Sources.CONTENT_URI)
    this.links = readBaseItems(Filters.Links.CONTENT_URI)
}

fun FiltersData.write(cr: ContentResolver, deleteOld: Boolean = true) {
    val baseCreator = ObjectCursor.valuesCreatorFrom(FiltersData.BaseItem::class.java)
    val userCreator = ObjectCursor.valuesCreatorFrom(FiltersData.UserItem::class.java)
    if (users != null) {
        if (deleteOld) {
            cr.delete(Filters.Users.CONTENT_URI, null, null)
        }
        ContentResolverUtils.bulkInsert(cr, Filters.Users.CONTENT_URI, users.map(userCreator::create))
    }
    if (keywords != null) {
        if (deleteOld) {
            cr.delete(Filters.Keywords.CONTENT_URI, null, null)
        }
        ContentResolverUtils.bulkInsert(cr, Filters.Keywords.CONTENT_URI, keywords.map(baseCreator::create))
    }
    if (sources != null) {
        if (deleteOld) {
            cr.delete(Filters.Sources.CONTENT_URI, null, null)
        }
        ContentResolverUtils.bulkInsert(cr, Filters.Sources.CONTENT_URI, sources.map(baseCreator::create))
    }
    if (links != null) {
        if (deleteOld) {
            cr.delete(Filters.Links.CONTENT_URI, null, null)
        }
        ContentResolverUtils.bulkInsert(cr, Filters.Links.CONTENT_URI, links.map(baseCreator::create))
    }
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
        item.userKey = parser.getAttributeValue(null, ATTR_KEY)?.let(UserKey::valueOf) ?: return null
        return item
    }

    val stack = Stack<Any?>()
    var event = parser.eventType
    while (event != XmlPullParser.END_DOCUMENT) {
        when (event) {
            XmlPullParser.START_DOCUMENT -> {
                initFields()
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
                    TAG_USER -> (obj as? FiltersData.UserItem)?.let { users.add(it) }
                    TAG_KEYWORD -> (obj as? FiltersData.BaseItem)?.let { keywords.add(it) }
                    TAG_SOURCE -> (obj as? FiltersData.BaseItem)?.let { sources.add(it) }
                    TAG_LINK -> (obj as? FiltersData.BaseItem)?.let { links.add(it) }
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

fun FiltersData.addAll(data: FiltersData, ignoreDuplicates: Boolean = false): Boolean {
    var changed = false
    initFields()
    if (data.users != null) {
        changed = changed or this.users.addAllEnhanced(collection = data.users, ignoreDuplicates = ignoreDuplicates)
    }
    if (data.keywords != null) {
        changed = changed or this.keywords.addAllEnhanced(collection = data.keywords, ignoreDuplicates = ignoreDuplicates)
    }
    if (data.sources != null) {
        changed = changed or this.sources.addAllEnhanced(collection = data.sources, ignoreDuplicates = ignoreDuplicates)
    }
    if (data.links != null) {
        changed = changed or this.links.addAllEnhanced(collection = data.links, ignoreDuplicates = ignoreDuplicates)
    }
    return changed
}

fun FiltersData.removeAll(data: FiltersData): Boolean {
    var changed = false
    changed = changed or (data.users?.let { this.users?.removeAll(it) } ?: false)
    changed = changed or (data.keywords?.let { this.keywords?.removeAll(it) } ?: false)
    changed = changed or (data.sources?.let { this.sources?.removeAll(it) } ?: false)
    changed = changed or (data.links?.let { this.links?.removeAll(it) } ?: false)
    return changed
}

fun FiltersData.isEmpty(): Boolean {
    return users.isNullOrEmpty() && keywords.isNullOrEmpty() && sources.isNullOrEmpty()
            && links.isNullOrEmpty()
}

fun FiltersData.initFields() {
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
