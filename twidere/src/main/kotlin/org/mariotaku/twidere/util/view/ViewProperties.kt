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

package org.mariotaku.twidere.util.view

import android.annotation.TargetApi
import android.os.Build
import android.util.FloatProperty
import android.util.Property
import android.view.View

object ViewProperties {
    val TRANSLATION_X_RELATIVE: Property<View, Float> = @TargetApi(Build.VERSION_CODES.N)
    object : FloatProperty<View>("xRelative") {
        override fun setValue(obj: View, value: Float) {
            obj.translationX = value * obj.width
        }

        override fun get(obj: View): Float {
            return obj.translationX / obj.width
        }

    }

}