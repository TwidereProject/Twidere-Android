package org.mariotaku.twidere.view.constraint

import android.annotation.SuppressLint
import android.content.Context
import android.support.constraint.ConstraintHelper
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import org.mariotaku.twidere.annotation.PreviewStyle
import org.mariotaku.twidere.extension.model.aspectRatio
import org.mariotaku.twidere.extension.setVisible
import org.mariotaku.twidere.extension.view.resetValues
import org.mariotaku.twidere.model.ParcelableMedia

class MediaContainerHelper(context: Context, attrs: AttributeSet?) : ConstraintHelper(context, attrs) {
    val referencedCount: Int
        get() = mCount

    fun hideAll() {
        for (i in 0 until mCount) {
            getReferencedViewAt(i).setVisible(false)
        }
    }

    @SuppressLint("SwitchIntDef")
    fun layout1(@PreviewStyle style: Int, item: ParcelableMedia) {
        for (i in 0 until mCount) {
            getReferencedViewAt(i).setVisible(i <= 0)
        }
        val child = getReferencedViewAt(0)

        val lp = child.layoutParams as ConstraintLayout.LayoutParams
        lp.resetValues()
        lp.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        lp.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        lp.matchConstraintPercentWidth = 1f
        when (style) {
            PreviewStyle.ACTUAL_SIZE -> {
                val ratio = item.aspectRatio
                if (ratio.isNaN()) {
                    lp.dimensionRatio = "W,1:2"
                } else {
                    lp.dimensionRatio = "W,1:$ratio"
                }
            }
            else -> {
                lp.dimensionRatio = "W,1:2"
            }
        }
        lp.horizontalBias = .5f
    }

    fun layout3() {
        for (i in 0 until mCount) {
            getReferencedViewAt(i).setVisible(i <= 2)
        }
        val child0 = getReferencedViewAt(0)
        val child1 = getReferencedViewAt(1)
        val child2 = getReferencedViewAt(2)

        (child0.layoutParams as ConstraintLayout.LayoutParams).also {
            it.resetValues()
            it.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            it.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            it.matchConstraintPercentWidth = .5f
            it.dimensionRatio = "W,1:1"
            it.horizontalBias = 0f
        }

        (child1.layoutParams as ConstraintLayout.LayoutParams).also {
            it.resetValues()
            it.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            it.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            it.matchConstraintPercentWidth = .5f
            it.dimensionRatio = "W,1:2"
            it.horizontalBias = 1f
        }
        (child2.layoutParams as ConstraintLayout.LayoutParams).also {
            it.resetValues()
            it.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            it.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            it.topToBottom = child1.id
            it.matchConstraintPercentWidth = .5f
            it.dimensionRatio = "W,1:2"
            it.horizontalBias = 1f
        }
    }

    fun layoutGrid(numColumns: Int, count: Int, dimensionRatio: String) {
        for (i in count until mCount) {
            getReferencedViewAt(i).setVisible(false)
        }

        val widthPercent = 1f / numColumns

        for (i in 0 until count.coerceAtMost(mCount)) {
            val view = getReferencedViewAt(i)
            view.setVisible(true)

            val row = i / numColumns
            val column = i % numColumns

            val lp = view.layoutParams as ConstraintLayout.LayoutParams
            lp.resetValues()
            lp.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            lp.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            if (row == 0) {
                lp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            } else {
                lp.topToBottom = mIds[i - numColumns]
            }
            lp.matchConstraintPercentWidth = widthPercent
            lp.dimensionRatio = dimensionRatio
            if (numColumns > 1) {
                lp.horizontalBias = column / (numColumns - 1).toFloat()
            } else {
                lp.horizontalBias = 0f
            }
        }
    }

    fun getReferencedViewAt(index: Int): View {
        return (parent as ViewGroup).findViewById(mIds[index])
    }
}

