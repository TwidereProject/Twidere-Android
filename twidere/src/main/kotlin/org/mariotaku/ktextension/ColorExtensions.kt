package org.mariotaku.ktextension

import androidx.annotation.ColorInt
import java.util.*

/**
 * Created by mariotaku on 2017/1/2.
 */

fun toHexColor(@ColorInt color: Int, format: HexColorFormat = HexColorFormat.ARGB) = format.transform(color)

enum class HexColorFormat(val transform: (Int) -> String) {
    ARGB({ "#%08X".format(Locale.ROOT, it) }),
    RGB({ "#%06X".format(Locale.ROOT, 0xFFFFFF and it) })
}