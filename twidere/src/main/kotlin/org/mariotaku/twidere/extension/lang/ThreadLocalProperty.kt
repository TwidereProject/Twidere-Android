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

package org.mariotaku.twidere.extension.lang

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ThreadLocalDelegate<T>(initial: () -> T) {

    private var threadLocal: ThreadLocal<T> = PropertyThreadLocal(initial)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = threadLocal.get()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        threadLocal.set(value)
    }

    private class PropertyThreadLocal<T>(val initial: () -> T) : ThreadLocal<T>() {

        override fun initialValue(): T {
            return initial()
        }
    }
}

private class ThreadLocalProperty<T>(private val ref: ThreadLocal<T>) : ReadOnlyProperty<Any, T?> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T? = ref.get()
}

fun <T> threadLocal(obj: () -> T): ThreadLocalDelegate<T> = ThreadLocalDelegate(obj)

operator fun <T> ThreadLocal<T>.provideDelegate(thisRef: Any, prop: KProperty<*>): ReadOnlyProperty<Any, T?> =
        ThreadLocalProperty(this)