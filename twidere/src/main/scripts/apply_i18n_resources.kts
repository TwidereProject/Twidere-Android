#!/usr/bin/env kotlinc -script
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
import java.io.File
import java.util.*

val valuesRegex = Regex("values-([a-z]+)(-r([A-Z]+))?")

val File.resourceLocale: Locale?
    get() {
        val mr = valuesRegex.matchEntire(name) ?: return null
        return Locale(mr.groupValues[1], mr.groupValues[3])
    }

val resDir = File("res-localized")
val keepRegionLocales = File(".keep_region_locales").readLines().map {
    val segs = it.split("-", limit = 2)
    if (segs.size == 1) {
        Locale(it)
    } else {
        Locale(segs[0], segs[1])
    }
}

// Rename aliased languages
resDir.listFiles { file ->
    if (!file.isDirectory) return@listFiles false
    val resourceLocale = file.resourceLocale ?: return@listFiles false
    if (resourceLocale.country == null) return@listFiles false
    return@listFiles resourceLocale !in keepRegionLocales
}.forEach { file ->
    file.renameTo(File(file.parentFile, "values-${file.resourceLocale!!.language}"))
}