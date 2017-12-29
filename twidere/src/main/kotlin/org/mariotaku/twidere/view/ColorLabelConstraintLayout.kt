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
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet

import org.mariotaku.twidere.view.iface.IColorLabelView

class ColorLabelConstraintLayout(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs), IColorLabelView {

    private val helper = IColorLabelView.Helper(this, context, attrs, 0)

    override fun drawBackground(color: Int) {
        helper.drawBackground(color)
    }

    override fun drawBottom(vararg colors: Int) {
        helper.drawBottom(colors)
    }

    override fun drawEnd(vararg colors: Int) {
        helper.drawEnd(colors)
    }

    override fun drawLabel(start: IntArray, end: IntArray, top: IntArray, bottom: IntArray, background: Int) {
        helper.drawLabel(start, end, top, bottom, background)
    }

    override fun drawStart(vararg colors: Int) {
        helper.drawStart(colors)
    }

    override fun drawTop(vararg colors: Int) {
        helper.drawTop(colors)
    }

    override fun isPaddingIgnored(): Boolean {
        return helper.isPaddingIgnored
    }

    override fun setIgnorePadding(ignorePadding: Boolean) {
        helper.setIgnorePadding(ignorePadding)
    }

    override fun dispatchDraw(canvas: Canvas) {
        helper.dispatchDrawBackground(canvas)
        super.dispatchDraw(canvas)
        helper.dispatchDrawLabels(canvas)
    }

}
