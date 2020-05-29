package org.mariotaku.twidere.view

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.layout_link_preview.view.*
import org.mariotaku.twidere.R

data class LinkPreviewData(
        val title: String?,
        val desc: String?,
        val img: String?
)


class LinkPreviewView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_link_preview, this)
    }

    fun displayData(value: String, result: LinkPreviewData) {
        link_preview_title.isVisible = true
        link_preview_link.isVisible = true
        link_preview_loader.isVisible = false
        link_preview_title.text = result.title
        link_preview_link.text = value
    }

    fun reset() {
        link_preview_title.isVisible = false
        link_preview_link.isVisible = false
        link_preview_loader.isVisible = true
        link_preview_title.text = ""
        link_preview_link.text = ""
    }
}