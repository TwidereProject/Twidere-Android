package org.mariotaku.twidere.util.view

import android.annotation.SuppressLint
import android.support.constraint.ConstraintLayout
import org.mariotaku.twidere.annotation.PreviewStyle
import org.mariotaku.twidere.extension.model.aspectRatio
import org.mariotaku.twidere.extension.setVisible
import org.mariotaku.twidere.model.ParcelableMedia

object MediaAttachmentLayoutGenerator {

    @SuppressLint("SwitchIntDef")
    fun layout1(view: ConstraintLayout, @PreviewStyle style: Int, item: ParcelableMedia) {
        for (i in 1 until view.childCount) {
            view.getChildAt(i).setVisible(i <= 0)
        }
        val child = view.getChildAt(0)

        val lp = child.layoutParams as ConstraintLayout.LayoutParams
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

    fun layout2(view: ConstraintLayout) {
        for (i in 0 until view.childCount) {
            view.getChildAt(i).setVisible(i <= 1)
        }
        val child0 = view.getChildAt(0)
        val child1 = view.getChildAt(1)

        (child0.layoutParams as ConstraintLayout.LayoutParams).also {
            it.reset()
            it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            it.matchConstraintPercentWidth = .5f
            it.dimensionRatio = "W,1:1"
            it.horizontalBias = 0f
        }

        (child1.layoutParams as ConstraintLayout.LayoutParams).also {
            it.reset()
            it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            it.matchConstraintPercentWidth = .5f
            it.dimensionRatio = "W,1:1"
            it.horizontalBias = 1f
        }
    }

    fun layout3(view: ConstraintLayout) {
        for (i in 3 until view.childCount) {
            view.getChildAt(i).setVisible(i <= 2)
        }
        val child0 = view.getChildAt(0)
        val child1 = view.getChildAt(1)
        val child2 = view.getChildAt(2)

        (child0.layoutParams as ConstraintLayout.LayoutParams).also {
            it.reset()
            it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            it.matchConstraintPercentWidth = .5f
            it.dimensionRatio = "W,1:1"
            it.horizontalBias = 0f
        }

        (child1.layoutParams as ConstraintLayout.LayoutParams).also {
            it.reset()
            it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            it.matchConstraintPercentWidth = .5f
            it.dimensionRatio = "W,1:2"
            it.horizontalBias = 1f
        }
        (child2.layoutParams as ConstraintLayout.LayoutParams).also {
            it.reset()
            it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            it.topToBottom = child1.id
            it.matchConstraintPercentWidth = .5f
            it.dimensionRatio = "W,1:2"
            it.horizontalBias = 1f
        }
    }

    fun layout4(view: ConstraintLayout) {
        for (i in 4 until view.childCount) {
            view.getChildAt(i).setVisible(i <= 3)
        }
        val child0 = view.getChildAt(0)
        val child1 = view.getChildAt(1)
        val child2 = view.getChildAt(2)
        val child3 = view.getChildAt(3)

        (child0.layoutParams as ConstraintLayout.LayoutParams).also {
            it.reset()
            it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            it.matchConstraintPercentWidth = .5f
            it.dimensionRatio = "W,1:2"
            it.horizontalBias = 0f
        }

        (child1.layoutParams as ConstraintLayout.LayoutParams).also {
            it.reset()
            it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            it.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            it.matchConstraintPercentWidth = .5f
            it.dimensionRatio = "W,1:2"
            it.horizontalBias = 1f
        }
        (child2.layoutParams as ConstraintLayout.LayoutParams).also {
            it.reset()
            it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            it.topToBottom = child0.id
            it.matchConstraintPercentWidth = .5f
            it.dimensionRatio = "W,1:2"
            it.horizontalBias = 0f
        }
        (child3.layoutParams as ConstraintLayout.LayoutParams).also {
            it.reset()
            it.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            it.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            it.topToBottom = child1.id
            it.matchConstraintPercentWidth = .5f
            it.dimensionRatio = "W,1:2"
            it.horizontalBias = 1f
        }
    }

}