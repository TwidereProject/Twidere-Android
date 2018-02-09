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
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.PreviewStyle
import org.mariotaku.twidere.extension.model.aspect_ratio
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.media.AuthenticatedUri
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import java.lang.ref.WeakReference

/**
 * Dynamic layout for media preview
 * Created by mariotaku on 14/12/17.
 */
class CardMediaContainer(context: Context, attrs: AttributeSet? = null) : ViewGroup(context, attrs) {

    private val horizontalSpacing: Int
    private val verticalSpacing: Int
    private var childIndices: IntArray = IntArray(0)
    private var videoViewIds: IntArray = IntArray(0)

    var style: Int = PreviewStyle.NONE
        @PreviewStyle set
        @PreviewStyle get

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
            if (child !is ImageView) {
                child.visibility = View.GONE
            } else if (i < k) {
                child.setImageResource(imageRes[i])
                child.visibility = View.VISIBLE
            } else {
                child.setImageDrawable(null)
                child.visibility = View.GONE
            }
        }
    }

    fun displayMedia(requestManager: RequestManager, media: Array<ParcelableMedia>?, accountKey: UserKey? = null,
            extraId: Long = -1, withCredentials: Boolean = false,
            mediaClickListener: OnMediaClickListener? = null) {
        if (media == null || style == PreviewStyle.NONE) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                child.visibility = View.GONE
            }
            return
        }
        val clickListener = MediaItemViewClickListener(mediaClickListener, accountKey, extraId)
        var displayChildIndex = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as MediaLayoutParams
            if (mediaClickListener != null) {
                child.setOnClickListener(clickListener)
            }
            if (!lp.isMediaItemView) continue
            (child as ImageView).displayImage(displayChildIndex, media, requestManager, accountKey,
                    withCredentials)
            displayChildIndex++
        }
    }

    private fun ImageView.displayImage(displayChildIndex: Int, media: Array<ParcelableMedia>,
            requestManager: RequestManager, accountKey: UserKey?, withCredentials: Boolean) {
        this.scaleType = when (style) {
            PreviewStyle.ACTUAL_SIZE, PreviewStyle.CROP -> ScaleType.CENTER_CROP
            PreviewStyle.SCALE -> ScaleType.FIT_CENTER
            PreviewStyle.NONE -> ScaleType.CENTER
            else -> ScaleType.CENTER
        }
        val lp = this.layoutParams as MediaLayoutParams
        if (displayChildIndex < media.size) {
            val item = media[displayChildIndex]
            val video = item.type == ParcelableMedia.Type.VIDEO
            val url = item.preview_url ?: this@CardMediaContainer.run {
                if (video) return@run null
                item.media_url
            }
            val request = if (withCredentials) {
                requestManager.load(AuthenticatedUri(Uri.parse(url), accountKey)).asBitmap()
            } else {
                requestManager.load(url).asBitmap()
            }
            when (style) {
                PreviewStyle.ACTUAL_SIZE -> {
                    request.fitCenter()
                }
                PreviewStyle.CROP -> {
                    request.centerCrop()
                }
                PreviewStyle.SCALE -> {
                    request.fitCenter()
                }
                PreviewStyle.NONE -> {
                    // Ignore
                }
            }
            request.into(this)
            if (this is MediaPreviewImageView) {
                setHasPlayIcon(ParcelableMediaUtils.hasPlayIcon(item.type))
            }
            if (item.alt_text.isNullOrEmpty()) {
                this.contentDescription = context.getString(R.string.media)
            } else {
                this.contentDescription = item.alt_text
            }
            (this.layoutParams as MediaLayoutParams).media = item
            this.visibility = View.VISIBLE
            findViewById<View>(lp.videoViewId)?.visibility = View.VISIBLE
        } else {
            Glide.clear(this)
            this.visibility = View.GONE
            findViewById<View>(lp.videoViewId)?.visibility = View.GONE
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childCount = rebuildChildInfo()
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
        val childCount = rebuildChildInfo()
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
        if (style == PreviewStyle.ACTUAL_SIZE) {
            val media = (child.layoutParams as MediaLayoutParams).media
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
        findViewById<View>(videoViewIds[0])?.measure(widthSpec, heightSpec)
        return childHeight
    }

    private fun layout1Media(childIndices: IntArray) {
        val child = getChildAt(childIndices[0])
        val left = paddingLeft
        val top = paddingTop
        val right = left + child.measuredWidth
        val bottom = top + child.measuredHeight
        child.layout(left, top, right, bottom)
        findViewById<View>(videoViewIds[0])?.layout(left, top, right, bottom)
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
            findViewById<View>(videoViewIds[i])?.measure(widthSpec, heightSpec)
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
            findViewById<View>(videoViewIds[i])?.layout(left, top, left + child.measuredWidth,
                    top + child.measuredHeight)
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

        findViewById<View>(videoViewIds[0])?.measure(widthSpec, childLeftHeightSpec)
        findViewById<View>(videoViewIds[1])?.measure(widthSpec, childRightHeightSpec)
        findViewById<View>(videoViewIds[2])?.measure(widthSpec, childRightHeightSpec)
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

        findViewById<View>(videoViewIds[0])?.layout(left, top, left + child0.measuredWidth,
                top + child0.measuredHeight)
        findViewById<View>(videoViewIds[1])?.layout(rightColLeft, top,
                rightColLeft + child1.measuredWidth, top + child1.measuredHeight)
        findViewById<View>(videoViewIds[2])?.layout(rightColLeft, child2Top,
                rightColLeft + child2.measuredWidth, child2Top + child2.measuredHeight)
    }

    private fun rebuildChildInfo(): Int {
        val childCount = this.childCount
        if (childIndices.size < childCount) {
            childIndices = IntArray(childCount)
            videoViewIds = IntArray(childCount)
        }
        var indicesCount = 0
        for (childIndex in 0 until childCount) {
            val child = getChildAt(childIndex)
            val lp = child.layoutParams as MediaLayoutParams
            if (lp.isMediaItemView && child.visibility != View.GONE) {
                childIndices[indicesCount] = childIndex
                videoViewIds[indicesCount] = lp.videoViewId
                indicesCount++
            }
        }
        return indicesCount
    }

    class MediaLayoutParams : ViewGroup.LayoutParams {

        val isMediaItemView: Boolean
        val videoViewId: Int
        var media: ParcelableMedia? = null

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.MediaLayoutParams)
            isMediaItemView = a.getBoolean(R.styleable.MediaLayoutParams_layout_isMediaItemView, false)
            videoViewId = a.getResourceId(R.styleable.MediaLayoutParams_layout_videoViewId, 0)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height) {
            videoViewId = 0
            isMediaItemView = true
        }
    }

    interface OnMediaClickListener {
        fun onMediaClick(view: View, current: ParcelableMedia, accountKey: UserKey?, id: Long)
    }

    private class MediaItemViewClickListener(
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

    }

}
