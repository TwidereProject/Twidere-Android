package org.mariotaku.twidere.extension.view

import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintLayout.LayoutParams.UNSET

fun ConstraintLayout.LayoutParams.resetValues() {
    guideBegin = UNSET
    guideEnd = UNSET
    guidePercent = UNSET.toFloat()
    leftToLeft = UNSET
    leftToRight = UNSET
    rightToLeft = UNSET
    rightToRight = UNSET
    topToTop = UNSET
    topToBottom = UNSET
    bottomToTop = UNSET
    bottomToBottom = UNSET
    baselineToBaseline = UNSET
    circleConstraint = UNSET
    circleRadius = 0
    circleAngle = 0.0f
    startToEnd = UNSET
    startToStart = UNSET
    endToStart = UNSET
    endToEnd = UNSET
    goneLeftMargin = UNSET
    goneTopMargin = UNSET
    goneRightMargin = UNSET
    goneBottomMargin = UNSET
    goneStartMargin = UNSET
    goneEndMargin = UNSET
    horizontalBias = 0.5f
    verticalBias = 0.5f
    dimensionRatio = null
}