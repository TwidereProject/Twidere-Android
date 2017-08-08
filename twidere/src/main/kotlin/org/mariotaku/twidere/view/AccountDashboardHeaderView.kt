package org.mariotaku.twidere.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import org.mariotaku.twidere.R

/**
 * Created by mariotaku on 2016/12/16.
 */
class AccountDashboardHeaderView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private var sizeMeasurementId: Int

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.AccountDashboardHeaderView)
        sizeMeasurementId = a.getResourceId(R.styleable.AccountDashboardHeaderView_sizeMeasurementId, 0)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val children: List<View> = (0 until childCount).map { getChildAt(it) }
        val sizeMeasurementView = children.find { it.id == sizeMeasurementId }!!
        measureChild(sizeMeasurementView, widthMeasureSpec, heightMeasureSpec)
        children.forEach { child ->
            if (child.id != sizeMeasurementId) {
                measureChild(child, MeasureSpec.makeMeasureSpec(sizeMeasurementView.measuredWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(sizeMeasurementView.measuredHeight, MeasureSpec.EXACTLY))
            }
        }
        setMeasuredDimension(sizeMeasurementView.measuredWidth, sizeMeasurementView.measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        (0 until childCount).forEach {
            getChildAt(it).layout(0, 0, r - l, b - t)
        }
    }

}