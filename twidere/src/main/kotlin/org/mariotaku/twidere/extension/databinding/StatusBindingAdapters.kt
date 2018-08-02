package org.mariotaku.twidere.extension.databinding

import android.databinding.BindingAdapter
import org.mariotaku.twidere.util.UnitConvertUtils
import org.mariotaku.twidere.view.LabeledImageButton

@BindingAdapter("properCount", "pluralRes", "zeroRes", requireAll = true)
fun LabeledImageButton.displayProperCount(count: Long, pluralRes: Int, zeroRes: Int) {
    if (count > 0) {
        val properCount = UnitConvertUtils.calculateProperCount(count)
        text = properCount
        contentDescription = context.resources.getQuantityString(pluralRes,
                count.toInt(), properCount)
    } else {
        text = null
        contentDescription = context.getString(zeroRes)
    }
}