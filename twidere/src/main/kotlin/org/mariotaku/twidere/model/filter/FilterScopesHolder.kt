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

package org.mariotaku.twidere.model.filter

import android.os.Parcel
import android.os.Parcelable
import org.mariotaku.ktextension.contains

class FilterScopesHolder(val masks: Int, value: Int = 0) : Parcelable {

    var value: Int = 0
        get() = field and masks
        private set(v) {
            field = v and masks
        }

    constructor(parcel: Parcel) : this(parcel.readInt(), parcel.readInt())

    init {
        this.value = value
    }

    operator fun set(scope: Int, enabled: Boolean) {
        value = if (enabled) {
            value or scope
        } else {
            value and scope.inv()
        }
    }

    operator fun get(scope: Int): Boolean {
        return scope in value
    }

    operator fun contains(scope: Int): Boolean {
        return scope in masks
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(masks)
        parcel.writeInt(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun hasMask(mask: Int): Boolean = masks and mask != 0

    companion object CREATOR : Parcelable.Creator<FilterScopesHolder> {
        override fun createFromParcel(parcel: Parcel): FilterScopesHolder {
            return FilterScopesHolder(parcel)
        }

        override fun newArray(size: Int): Array<FilterScopesHolder?> {
            return arrayOfNulls(size)
        }
    }

}