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

package androidx.core.os

import android.annotation.SuppressLint
import java.util.*

@SuppressLint("RestrictedApi")
object LocaleHelperAccessor {
    fun forLanguageTag(str: String): Locale {
        when {
            str.contains("-") -> {
                val args = str.split("-").dropLastWhile { it.isEmpty() }.toTypedArray()
                when {
                    args.size > 2 -> {
                        return Locale(args[0], args[1], args[2])
                    }
                    args.size > 1 -> {
                        return Locale(args[0], args[1])
                    }
                    args.size == 1 -> {
                        return Locale(args[0])
                    }
                }
            }
            str.contains("_") -> {
                val args = str.split("_").dropLastWhile { it.isEmpty() }.toTypedArray()
                when {
                    args.size > 2 -> {
                        return Locale(args[0], args[1], args[2])
                    }
                    args.size > 1 -> {
                        return Locale(args[0], args[1])
                    }
                    args.size == 1 -> {
                        return Locale(args[0])
                    }
                }
            }
            else -> {
                return Locale(str)
            }
        }

        throw IllegalArgumentException("Can not parse language tag: [$str]")
    }
}
