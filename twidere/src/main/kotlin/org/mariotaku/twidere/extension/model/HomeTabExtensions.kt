/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2018 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.extension.model

import org.mariotaku.twidere.model.HomeTab
import org.mariotaku.twidere.model.tab.argument.TabArguments
import org.mariotaku.twidere.model.tab.argument.TextQueryArguments
import org.mariotaku.twidere.model.tab.argument.UserArguments
import org.mariotaku.twidere.model.tab.argument.UserListArguments
import org.mariotaku.twidere.model.tab.extra.InteractionsTabExtras
import org.mariotaku.twidere.model.tab.extra.TimelineTabExtras
import org.mariotaku.twidere.model.tab.extra.TrendsTabExtras
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer

fun HomeTab.serialize(serializer: XmlSerializer) {
    serializer.startTag(null, "tab")
    serializer.attribute(null, "type", type)
    serializer.attribute(null, "position", position.toString())

    serializer.writeNonEmptyStringTag("name", name)
    serializer.writeNonEmptyStringTag("icon", icon)

    val arguments = this.arguments
    val extras = this.extras

    when (arguments) {
        is UserArguments -> serializer.serializeUserArguments(arguments)
        is UserListArguments -> serializer.serializeUserListArguments(arguments)
        is TextQueryArguments -> serializer.serializeTextQueryArguments(arguments)
        is TabArguments -> serializer.serializeBaseArguments(arguments)
    }

    when (extras) {
        is TimelineTabExtras -> serializer.serializeTimelineTabExtras(extras)
        is InteractionsTabExtras -> serializer.serializeInteractionsTabExtras(extras)
        is TrendsTabExtras -> serializer.serializeTrendsTabExtras(extras)
    }

    serializer.endTag(null, "tab")
}

fun HomeTab.parse(parser: XmlPullParser) {
    for (i in 0 until parser.attributeCount) {
        when (parser.getAttributeName(i)) {
            "type" -> type = parser.getAttributeValue(i)
        }
    }
}

private fun XmlSerializer.writeNonEmptyStringTag(name: String, value: String?) {
    if (value.isNullOrEmpty()) return
    startTag(null, name)
    text(value)
    endTag(null, name)
}

private fun XmlSerializer.writeBaseArgumentsFields(arguments: TabArguments) {
    val keys = arguments.accountKeys
    val id = arguments.accountId
    if (keys != null) {
        startTag(null, "accountKeys")
        keys.forEach {
            writeNonEmptyStringTag("item", it.toString())
        }
        endTag(null, "accountKeys")
    } else if (id != null) {
        startTag(null, "accountKeys")
        writeNonEmptyStringTag("item", id)
        endTag(null, "accountKeys")
    }
}

private fun XmlSerializer.serializeUserArguments(arguments: UserArguments) {
    startTag(null, "arguments")
    attribute(null, "type", "user")
    writeBaseArgumentsFields(arguments)
    writeNonEmptyStringTag("userKey", arguments.userKey?.toString() ?: arguments.userId)
    endTag(null, "arguments")
}

private fun XmlSerializer.serializeBaseArguments(arguments: TabArguments) {
    startTag(null, "arguments")
    writeBaseArgumentsFields(arguments)
    endTag(null, "arguments")
}

private fun XmlSerializer.serializeUserListArguments(arguments: UserListArguments) {
    startTag(null, "arguments")
    attribute(null, "type", "userList")
    writeBaseArgumentsFields(arguments)
    writeNonEmptyStringTag("listId", arguments.listId)
    endTag(null, "arguments")
}

private fun XmlSerializer.serializeTextQueryArguments(arguments: TextQueryArguments) {
    startTag(null, "arguments")
    attribute(null, "type", "textQuery")
    writeBaseArgumentsFields(arguments)
    writeNonEmptyStringTag("query", arguments.query)
    endTag(null, "arguments")
}

private fun XmlSerializer.serializeTimelineTabExtras(extras: TimelineTabExtras) {
    startTag(null, "extras")
    attribute(null, "type", "timeline")
    writeNonEmptyStringTag("style", extras.timelineStyle.toString())
    endTag(null, "extras")
}

private fun XmlSerializer.serializeInteractionsTabExtras(extras: InteractionsTabExtras) {
    startTag(null, "extras")
    attribute(null, "type", "interactions")
    writeNonEmptyStringTag("mentionsOnly", extras.isMentionsOnly.toString())
    writeNonEmptyStringTag("myFollowingOnly", extras.isMyFollowingOnly.toString())
    endTag(null, "extras")
}

private fun XmlSerializer.serializeTrendsTabExtras(extras: TrendsTabExtras) {
    startTag(null, "extras")
    attribute(null, "type", "trends")
    writeNonEmptyStringTag("placeName", extras.placeName)
    writeNonEmptyStringTag("woeId", extras.woeId.toString())
    endTag(null, "extras")
}