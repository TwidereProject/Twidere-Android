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

package org.mariotaku.twidere.extension.model

import org.mariotaku.ktextension.mapToArray
import org.mariotaku.ktextension.toIntOr
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.microblog.library.twitter.model.CardEntity
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.model.ParcelableCardEntity
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableCardEntityUtils
import java.text.ParseException
import java.util.*

fun CardEntity.toParcelable(accountKey: UserKey, accountType: String): ParcelableCardEntity? {
    val obj = ParcelableCardEntity()
    obj.name = name
    obj.url = url
    obj.users = users?.mapToArray {
        it.toParcelable(accountKey, accountType)
    }
    obj.account_key = accountKey
    obj.values = bindingValues?.mapValues { entry ->
        ParcelableCardEntity.ParcelableBindingValue(entry.value)
    }
    return obj
}

fun ParcelableCardEntity.getAsBoolean(key: String, def: Boolean): Boolean {
    val value = getValue(key) ?: return def
    return value.value!!.toBoolean()
}

fun ParcelableCardEntity.getAsString(key: String, def: String?): String? {
    return getValue(key)?.value ?: return def
}

fun ParcelableCardEntity.getString(key: String): String? {
    val value = getValue(key) ?: return null
    if (CardEntity.BindingValue.TYPE_STRING != value.type) return null
    return value.value
}

fun ParcelableCardEntity.getAsInteger(key: String, def: Int): Int {
    val value = getValue(key) ?: return def
    return value.value.toIntOr(def)
}

fun ParcelableCardEntity.getAsLong(key: String, def: Long): Long {
    val value = getValue(key) ?: return def
    return value.value.toLongOr(def)
}

fun ParcelableCardEntity.getAsDate(key: String, def: Date): Date {
    val value = getValue(key) ?: return def
    return try {
        ParcelableCardEntityUtils.sISOFormat.parse(value.value)
    } catch (e: ParseException) {
        def
    }
}
