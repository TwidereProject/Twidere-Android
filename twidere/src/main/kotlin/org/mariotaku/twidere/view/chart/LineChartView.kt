package org.mariotaku.twidere.view.chart

import android.content.Context
import android.databinding.BindingMethod
import android.databinding.BindingMethods
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import org.mariotaku.twidere.R

@BindingMethods(
        BindingMethod(type = LineChartView::class, attribute = "chartValues", method = "setValues"),
        BindingMethod(type = LineChartView::class, attribute = "chartValuesCount", method = "setValuesCount")
)
class LineChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    var values: FloatArray? = null
        set(value) {
            field = value
            updatePath()
            invalidate()
        }

    var valuesCount: Int = 0
        set(value) {
            field = value
            updatePath()
            invalidate()
        }

    private var chartLineColor: Int = Color.LTGRAY
        set(value) {
            field = value
            linePaint.color = value
        }
    private var chartLineSize: Float = 1f
        set(value) {
            field = value
            linePaint.strokeWidth = value
        }

    private var chartBaseline: Float

    private var chartBaselineColor: Int = Color.LTGRAY
        set(value) {
            field = value
            baselinePaint.color = value
        }
    private var chartBaselineSize: Float = 1f
        set(value) {
            field = value
            baselinePaint.strokeWidth = value
        }
    private var chartEndpointColor: Int = Color.LTGRAY
        set(value) {
            field = value
            endpointPaint.color = value
        }

    private var chartEndpointSize: Float = 2f
        set(value) {
            field = value
            endpointPaint.strokeWidth = value
            invalidate()
        }

    private val linePaint = createPaint()
    private val baselinePaint = createPaint()
    private val endpointPaint = createPaint()

    private val path: Path = Path()

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LineChartView)
        chartBaseline = a.getDimension(R.styleable.LineChartView_chartBaseline, 0f)
        chartBaselineColor = a.getColor(R.styleable.LineChartView_chartBaselineColor, Color.LTGRAY)
        chartBaselineSize = a.getDimension(R.styleable.LineChartView_chartBaselineSize, 1f)
        chartLineColor = a.getColor(R.styleable.LineChartView_chartLineColor, Color.LTGRAY)
        chartLineSize = a.getDimension(R.styleable.LineChartView_chartLineSize, 1f)
        chartEndpointColor = a.getColor(R.styleable.LineChartView_chartEndpointColor, Color.DKGRAY)
        chartEndpointSize = a.getDimension(R.styleable.LineChartView_chartEndpointSize, 2f)
        a.recycle()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, linePaint)
        canvas.drawLine(0f, height - chartBaseline, width.toFloat(),
                height - chartBaseline, baselinePaint)

        val values = this.values
        if (values != null && values.isNotEmpty()) {
            val x = width * (values.lastIndex / (valuesCount - 1f))
            val y = (height - chartBaseline) * (1 - values.last())
            canvas.drawPoint(x, y, endpointPaint)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        updatePath()
    }

    private fun updatePath() {
        if (width <= 0 || height <= 0) return
        path.reset()
        if (valuesCount <= 1) return
        val values = this.values ?: return
        values.forEachIndexed { index, value ->
            val x = width * (index / (valuesCount - 1f))
            val y = (height - chartBaseline) * (1 - value)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
    }

    companion object {
        private fun createPaint() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
    }
}