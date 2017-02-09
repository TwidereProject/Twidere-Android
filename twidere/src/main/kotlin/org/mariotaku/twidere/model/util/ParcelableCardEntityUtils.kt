package org.mariotaku.twidere.model.util


import android.support.v4.util.ArrayMap
import org.apache.commons.lang3.math.NumberUtils
import org.mariotaku.microblog.library.twitter.model.CardEntity
import org.mariotaku.microblog.library.twitter.util.ThreadLocalSimpleDateFormat
import org.mariotaku.twidere.model.ParcelableCardEntity
import org.mariotaku.twidere.model.UserKey
import java.text.DateFormat
import java.text.ParseException
import java.util.*

/**
 * Created by mariotaku on 16/2/24.
 */
object ParcelableCardEntityUtils {

    internal val sISOFormat: DateFormat = ThreadLocalSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
            Locale.ENGLISH)

    init {
        sISOFormat.isLenient = true
        sISOFormat.timeZone = TimeZone.getTimeZone("UTC")
    }

    fun fromCardEntity(card: CardEntity?, accountKey: UserKey?): ParcelableCardEntity? {
        if (card == null) return null
        val obj = ParcelableCardEntity()
        obj.name = card.name
        obj.url = card.url
        obj.users = ParcelableUserUtils.fromUsers(card.users, accountKey)
        obj.account_key = accountKey
        obj.values = from(card.bindingValues)
        return obj
    }

    fun from(bindingValues: Map<String, CardEntity.BindingValue>?): Map<String, ParcelableCardEntity.ParcelableBindingValue>? {
        if (bindingValues == null) return null
        val map = ArrayMap<String, ParcelableCardEntity.ParcelableBindingValue>()
        for ((key, value) in bindingValues) {
            map.put(key, ParcelableCardEntity.ParcelableBindingValue(value))
        }
        return map
    }

    fun getAsBoolean(obj: ParcelableCardEntity, key: String, def: Boolean): Boolean {
        val value = obj.getValue(key) ?: return def
        return java.lang.Boolean.parseBoolean(value.value)
    }

    fun getAsString(obj: ParcelableCardEntity, key: String, def: String?): String? {
        return obj.getValue(key)?.value ?: return def
    }

    fun getString(obj: ParcelableCardEntity, key: String): String? {
        val value = obj.getValue(key)
        if (value == null || CardEntity.BindingValue.TYPE_STRING != value.type) return null
        return getAsString(obj, key, null)
    }

    fun getAsInteger(obj: ParcelableCardEntity, key: String, def: Int): Int {
        val value = obj.getValue(key) ?: return def
        return NumberUtils.toInt(value.value, def)
    }

    fun getAsLong(obj: ParcelableCardEntity, key: String, def: Long): Long {
        val value = obj.getValue(key) ?: return def
        return NumberUtils.toLong(value.value, def)
    }

    fun getAsDate(obj: ParcelableCardEntity, key: String, def: Date): Date {
        val value = obj.getValue(key) ?: return def
        try {
            return sISOFormat.parse(value.value)
        } catch (e: ParseException) {
            return def
        }

    }

}
