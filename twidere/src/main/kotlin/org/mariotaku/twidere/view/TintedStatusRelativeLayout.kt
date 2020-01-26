/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.core.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.Chameleon.Theme.LightStatusBarMode
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.chameleon.ChameleonView
import org.mariotaku.chameleon.internal.SupportMethods
import org.mariotaku.twidere.R
import androidx.core.view.OnApplyWindowInsetsListener as OnApplyWindowInsetsListenerCompat

class TintedStatusRelativeLayout(context: Context, attrs: AttributeSet? = null) :
        ExtendedRelativeLayout(context, attrs), TintedStatusLayout, ChameleonView,
        ChameleonView.StatusBarThemeable {

    override var setPaddingEnabled: Boolean = false

    override var statusBarColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            updatePaint()
            invalidate()
        }

    override var statusBarAlpha: Float = 1f
        set(value) {
            field = value
            updatePaint()
            invalidate()
        }

    override var statusBarHeight: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    private val statusBarPaint: Paint

    var applyWindowInsetsListener: OnApplyWindowInsetsListenerCompat? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.TintedStatusLayout)
        setPaddingEnabled = a.getBoolean(R.styleable.TintedStatusLayout_setPadding, false)
        a.recycle()
        statusBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        setWillNotDraw(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            ViewCompat.setOnApplyWindowInsetsListener(this) lambda@ { view, insets ->
                val consumed = applyWindowInsetsListener?.onApplyWindowInsets(view, insets) ?: insets
                if (!consumed.isConsumed) {
                    val top = consumed.systemWindowInsetTop
                    val left = consumed.systemWindowInsetLeft
                    val right = consumed.systemWindowInsetRight
                    val bottom = consumed.systemWindowInsetBottom
                    if (setPaddingEnabled) {
                        setPadding(left, top, right, bottom)
                    }
                    statusBarHeight = top
                    return@lambda consumed
                } else {
                    return@lambda consumed.consumeSystemWindowInsets()
                }
            }
        }
    }


    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), statusBarHeight.toFloat(), statusBarPaint)
    }

    override fun isPostApplyTheme(): Boolean {
        return false
    }

    override fun createAppearance(context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): Appearance? {
        val appearance = Appearance()
        appearance.statusBarColor = theme.statusBarColor
        appearance.lightStatusBarMode = theme.lightStatusBarMode
        return appearance
    }

    override fun applyAppearance(appearance: ChameleonView.Appearance) {
        val a = appearance as Appearance
        val statusBarColor = a.statusBarColor
        this.statusBarColor = statusBarColor
        val activity = ChameleonUtils.getActivity(context)
        if (activity != null) {
            val window = activity.window
            SupportMethods.setStatusBarColor(window, Color.TRANSPARENT)
            ChameleonUtils.applyLightStatusBar(window, statusBarColor, a.lightStatusBarMode)
        }
    }

    override fun isStatusBarColorHandled(): Boolean {
        return true
    }

    private fun updatePaint() {
        statusBarPaint.color = statusBarColor
        statusBarPaint.alpha = (Color.alpha(statusBarColor) * statusBarAlpha).toInt()
    }

    class Appearance : ChameleonView.Appearance {
        var statusBarColor: Int = 0
        @LightStatusBarMode
        @get:LightStatusBarMode
        var lightStatusBarMode: Int = 0
    }

}
