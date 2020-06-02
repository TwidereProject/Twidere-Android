package org.mariotaku.twidere.view

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.bumptech.glide.RequestManager
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.layout_link_preview.view.*
import org.mariotaku.twidere.R


data class LinkPreviewData(
        val title: String?,
        val desc: String? = null,
        val img: String? = null,
        val imgRes: Int? = null
)


class LinkPreviewView : MaterialCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_link_preview, this, true)
    }

    fun displayData(value: String, result: LinkPreviewData, requestManager: RequestManager) {
        link_preview_title.isVisible = true
        link_preview_link.isVisible = true
        link_preview_img.isVisible = result.img != null
        link_preview_loader.isVisible = false
        link_preview_title.text = result.title
        link_preview_link.text = Uri.parse(value).host
        if (result.img != null) {
            requestManager.load(result.img).into(link_preview_img)
        } else if (result.imgRes != null) {
            requestManager.load(result.imgRes).into(link_preview_img)
        }
    }

    fun reset() {
        link_preview_img.isVisible = false
        link_preview_title.isVisible = false
        link_preview_link.isVisible = false
        link_preview_loader.isVisible = true
        link_preview_title.text = ""
        link_preview_link.text = ""
    }
}