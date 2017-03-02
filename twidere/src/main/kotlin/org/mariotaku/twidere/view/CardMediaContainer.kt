/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.PreviewStyle
import org.mariotaku.twidere.extension.model.aspect_ratio
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import org.mariotaku.twidere.util.MediaLoadingHandler
import java.lang.ref.WeakReference

/**
 * Dynamic layout for media preview
 * Created by mariotaku on 14/12/17.
 */
class CardMediaContainer(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    private val horizontalSpacing: Int
    private val verticalSpacing: Int
    private var tempIndices: IntArray? = null

    var style: Int = PreviewStyle.NONE
        @PreviewStyle set @PreviewStyle get

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CardMediaContainer)
        horizontalSpacing = a.getDimensionPixelSize(R.styleable.CardMediaContainer_android_horizontalSpacing, 0)
        verticalSpacing = a.getDimensionPixelSize(R.styleable.CardMediaContainer_android_verticalSpacing, 0)
        a.recycle()
    }

    fun displayMedia(vararg imageRes: Int) {
        val k = imageRes.size
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val imageView = child.findViewById(R.id.mediaPreview) as ImageView
            val progress = child.findViewById(R.id.media_preview_progress)
            progress.visibility = View.GONE
            if (i < k) {
                imageView.setImageResource(imageRes[i])
                child.visibility = View.VISIBLE
            } else {
                imageView.setImageDrawable(null)
                child.visibility = View.GONE
            }
        }
    }

    fun displayMedia(requestManager: RequestManager, media: Array<ParcelableMedia>?, accountId: UserKey? = null,
            extraId: Long = -1, withCredentials: Boolean = false,
            mediaClickListener: OnMediaClickListener? = null, loadingHandler: MediaLoadingHandler? = null) {
        if (media == null || style == PreviewStyle.NONE) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.visibility = View.GONE
            }
            return
        }
        val clickListener = ImageGridClickListener(mediaClickListener, accountId, extraId)
        val mediaSize = media.size
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (mediaClickListener != null) {
                child.setOnClickListener(clickListener)
            }
            val imageView = child.findViewById(R.id.mediaPreview) as ImageView
            when (style) {
                PreviewStyle.REAL_SIZE, PreviewStyle.CROP -> {
                    imageView.scaleType = ScaleType.CENTER_CROP
                }
                PreviewStyle.SCALE -> {
                    imageView.scaleType = ScaleType.FIT_CENTER
                }
            }
            if (i < mediaSize) {
                val item = media[i]
                val video = item.type == ParcelableMedia.Type.VIDEO
                val url = item.preview_url ?: run {
                    if (video) return@run null
                    item.media_url
                }
                if (withCredentials) {
                    requestManager.load(url).into(imageView)
                    // TODO handle load progress w/ authentication
                    // loader.displayPreviewImageWithCredentials(imageView, url, accountId, loadingHandler, video)
                } else {
                    requestManager.load(url).into(imageView)
                    // TODO handle load progress
                    // loader.displayPreviewImage(imageView, url, loadingHandler, video)
                }
                if (imageView is MediaPreviewImageView) {
                    imageView.setHasPlayIcon(ParcelableMediaUtils.hasPlayIcon(item.type))
                }
                if (item.alt_text.isNullOrEmpty()) {
                    child.contentDescription = context.getString(R.string.media)
                } else {
                    child.contentDescription = item.alt_text
                }
                (child.layoutParams as MediaLayoutParams).media = item
                child.visibility = View.VISIBLE
            } else {
                // TODO cancel image load task
                child.visibility = View.GONE
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childIndices = createChildIndices()
        val childCount = getChildIndicesInLayout(this, childIndices)
        if (childCount > 0) {
            if (childCount == 1) {
                layout1Media(childIndices)
            } else if (childCount == 3) {
                layout3Media(horizontalSpacing, verticalSpacing, childIndices)
            } else {
                layoutGridMedia(childCount, 2, horizontalSpacing, verticalSpacing, childIndices)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = View.resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        val contentWidth = measuredWidth - paddingLeft - paddingRight
        var ratioMultiplier = 1f
        var contentHeight = -1
        if (layoutParams.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
            val measuredHeight = View.resolveSize(suggestedMinimumWidth, widthMeasureSpec)
            ratioMultiplier = if (contentWidth > 0) measuredHeight / (contentWidth * WIDTH_HEIGHT_RATIO) else 1f
            contentHeight = contentWidth
        }
        val childIndices = createChildIndices()
        val childCount = getChildIndicesInLayout(this, childIndices)
        var heightSum = 0
        if (childCount > 0) {
            if (childCount == 1) {
                heightSum = measure1Media(contentWidth, childIndices, ratioMultiplier)
            } else if (childCount == 2) {
                heightSum = measureGridMedia(childCount, 2, contentWidth, ratioMultiplier, horizontalSpacing,
                        verticalSpacing, childIndices)
            } else if (childCount == 3) {
                heightSum = measure3Media(contentWidth, horizontalSpacing, childIndices, ratioMultiplier)
            } else {
                heightSum = measureGridMedia(childCount, 2, contentWidth,
                        WIDTH_HEIGHT_RATIO * ratioMultiplier, horizontalSpacing, verticalSpacing, childIndices)
            }
            if (contentHeight > 0) {
                heightSum = contentHeight
            }
        } else if (contentHeight > 0) {
            heightSum = contentHeight
        }
        val height = heightSum + paddingTop + paddingBottom
        setMeasuredDimension(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
    }

    override fun checkLayoutParams(p: LayoutParams?): Boolean {
        return p is MediaLayoutParams
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MediaLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return MediaLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return MediaLayoutParams(p.width, p.height)
    }

    private fun measure1Media(contentWidth: Int, childIndices: IntArray, ratioMultiplier: Float): Int {
        val child = getChildAt(childIndices[0])
        var childHeight = Math.round(contentWidth.toFloat() * WIDTH_HEIGHT_RATIO * ratioMultiplier)
        if (style == PreviewStyle.REAL_SIZE) {
            val media = (child.layoutParams as? MediaLayoutParams)?.media
            if (media != null) {
                val aspectRatio = media.aspect_ratio
                if (!aspectRatio.isNaN()) {
                    childHeight = Math.round(contentWidth / aspectRatio.coerceIn(0.3, 20.0)).toInt()
                }
            }
        }
        val widthSpec = View.MeasureSpec.makeMeasureSpec(contentWidth, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(childHeight, View.MeasureSpec.EXACTLY)
        child.measure(widthSpec, heightSpec)
        return childHeight
    }

    private fun layout1Media(childIndices: IntArray) {
        val child = getChildAt(childIndices[0])
        val left = paddingLeft
        val top = paddingTop
        val right = left + child.measuredWidth
        val bottom = top + child.measuredHeight
        child.layout(left, top, right, bottom)
    }

    private fun measureGridMedia(childCount: Int, columnCount: Int, contentWidth: Int,
            widthHeightRatio: Float, horizontalSpacing: Int, verticalSpacing: Int,
            childIndices: IntArray): Int {
        val childWidth = (contentWidth - horizontalSpacing * (columnCount - 1)) / columnCount
        val childHeight = Math.round(childWidth * widthHeightRatio)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(childHeight, View.MeasureSpec.EXACTLY)
        for (i in 0 until childCount) {
            getChildAt(childIndices[i]).measure(widthSpec, heightSpec)
        }
        val rowsCount = Math.ceil(childCount / columnCount.toDouble()).toInt()
        return rowsCount * childHeight + (rowsCount - 1) * verticalSpacing
    }

    private fun layoutGridMedia(childCount: Int, columnCount: Int, horizontalSpacing: Int,
            verticalSpacing: Int, childIndices: IntArray) {
        val initialLeft = paddingLeft
        var left = initialLeft
        var top = paddingTop
        for (i in 0 until childCount) {
            val colIdx = i % columnCount
            val child = getChildAt(childIndices[i])
            child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
            if (colIdx == columnCount - 1) {
                // Last item in this row, set top of next row to last view bottom + verticalSpacing
                top = child.bottom + verticalSpacing
                // And reset left to initial left
                left = initialLeft
            } else {
                // The left of next item is right + horizontalSpacing of previous item
                left = child.right + horizontalSpacing
            }
        }
    }

    private fun measure3Media(contentWidth: Int, horizontalSpacing: Int, childIndices: IntArray,
            ratioMultiplier: Float): Int {
        val child0 = getChildAt(childIndices[0])
        val child1 = getChildAt(childIndices[1])
        val child2 = getChildAt(childIndices[2])
        val childWidth = (contentWidth - horizontalSpacing) / 2
        val childLeftHeightSpec = View.MeasureSpec.makeMeasureSpec(Math.round(childWidth * ratioMultiplier), View.MeasureSpec.EXACTLY)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY)
        child0.measure(widthSpec, childLeftHeightSpec)
        val childRightHeight = Math.round((childWidth - horizontalSpacing) / 2 * ratioMultiplier)
        val childRightHeightSpec = View.MeasureSpec.makeMeasureSpec(childRightHeight, View.MeasureSpec.EXACTLY)
        child1.measure(widthSpec, childRightHeightSpec)
        child2.measure(widthSpec, childRightHeightSpec)
        return Math.round(contentWidth.toFloat() * WIDTH_HEIGHT_RATIO * ratioMultiplier)
    }

    private fun layout3Media(horizontalSpacing: Int, verticalSpacing: Int, childIndices: IntArray) {
        val left = paddingLeft
        val top = paddingTop
        val child0 = getChildAt(childIndices[0])
        val child1 = getChildAt(childIndices[1])
        val child2 = getChildAt(childIndices[2])
        child0.layout(left, top, left + child0.measuredWidth, top + child0.measuredHeight)
        val rightColLeft = child0.right + horizontalSpacing
        child1.layout(rightColLeft, top, rightColLeft + child1.measuredWidth,
                top + child1.measuredHeight)
        val child2Top = child1.bottom + verticalSpacing
        child2.layout(rightColLeft, child2Top, rightColLeft + child2.measuredWidth,
                child2Top + child2.measuredHeight)
    }

    private fun createChildIndices(): IntArray {
        if (tempIndices == null || tempIndices!!.size < childCount) {
            tempIndices = IntArray(childCount)
        }
        return tempIndices!!
    }

    class MediaLayoutParams : ViewGroup.LayoutParams {

        var media: ParcelableMedia? = null

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
        constructor(width: Int, height: Int) : super(width, height)
    }

    interface OnMediaClickListener {
        fun onMediaClick(view: View, media: ParcelableMedia, accountKey: UserKey?, id: Long)
    }

    private class ImageGridClickListener(
            listener: OnMediaClickListener?,
            private val accountKey: UserKey?,
            private val extraId: Long
    ) : View.OnClickListener {

        private val weakListener = WeakReference<OnMediaClickListener>(listener)

        override fun onClick(v: View) {
            val listener = weakListener.get() ?: return
            val media = (v.layoutParams as? MediaLayoutParams)?.media ?: return
            listener.onMediaClick(v, media, accountKey, extraId)
        }

    }

    companion object {

        private const val WIDTH_HEIGHT_RATIO = 0.5f

        private fun getChildIndicesInLayout(viewGroup: ViewGroup, indices: IntArray): Int {
            val childCount = viewGroup.childCount
            var indicesCount = 0
            for (i in 0 until childCount) {
                if (viewGroup.getChildAt(i).visibility != View.GONE) {
                    indices[indicesCount++] = i
                }
            }
            return indicesCount
        }
    }

}
