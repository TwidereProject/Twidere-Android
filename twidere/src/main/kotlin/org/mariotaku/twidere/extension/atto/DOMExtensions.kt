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

package org.mariotaku.twidere.extension.atto

import org.attoparser.dom.Document
import org.attoparser.dom.Element

/**
 * Created by mariotaku on 2017/4/29.
 */

fun Document.firstElementOrNull(match: (Element) -> Boolean): Element? {
    getChildrenOfType(Element::class.java).forEach { child ->
        val element = child.firstElementOrNull(match)
        if (element != null) return element
    }
    return null
}

fun Element.firstElementOrNull(match: (Element) -> Boolean): Element? {
    if (match(this)) return this
    getChildrenOfType(Element::class.java).forEach { child ->
        val element = child.firstElementOrNull(match)
        if (element != null) return element
    }
    return null
}

fun Element.filter(match: (Element) -> Boolean): List<Element> {
    return filterTo(ArrayList(), match)
}

fun <C : MutableCollection<Element>> Element.filterTo(to: C, match: (Element) -> Boolean): C {
    if (match(this)) {
        to.add(this)
    }
    getChildrenOfType(Element::class.java).forEach { child ->
        child.filterTo(to, match)
    }
    return to
}