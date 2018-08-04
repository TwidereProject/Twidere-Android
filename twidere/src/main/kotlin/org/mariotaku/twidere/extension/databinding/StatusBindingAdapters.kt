package org.mariotaku.twidere.extension.databinding

import android.databinding.BindingAdapter
import android.widget.ImageButton
import org.mariotaku.microblog.library.annotation.mastodon.StatusVisibility
import org.mariotaku.twidere.R
import org.mariotaku.twidere.singleton.BidiFormatterSingleton
import org.mariotaku.twidere.util.UnitConvertUtils
import org.mariotaku.twidere.view.LabeledImageButton
import org.mariotaku.twidere.view.NameView

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

@BindingAdapter("name", "screenName", requireAll = false)
fun NameView.displayName(name: String?, screenName: String?) {
    this.name = name
    this.screenName = screenName

    this.updateText(BidiFormatterSingleton.get())
}

@BindingAdapter("statusVisibility")
fun ImageButton.displayVisibilityIcon(visibility: String?) {
    when (visibility) {
        StatusVisibility.PRIVATE -> {
            setImageResource(R.drawable.ic_action_lock)
        }
        StatusVisibility.DIRECT -> {
            setImageResource(R.drawable.ic_action_message)
        }
        else -> {
            setImageResource(R.drawable.ic_action_retweet)
        }
    }
}