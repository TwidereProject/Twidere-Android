/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.databinding.BindingMethod
import android.databinding.BindingMethods
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import org.mariotaku.twidere.extension.loadProfileImage

@BindingMethods(
        BindingMethod(type = ProfileImageView::class, attribute = "profileImage", method = "setProfileImage")
)
class ProfileImageView(context: Context, attrs: AttributeSet? = null) : ShapedImageView(context, attrs) {

    var profileImage: String? = null
        set(value) {
            field = value
            if (value == null) {
                Glide.with(this).clear(this)
            } else {
                Glide.with(this).loadProfileImage(value, style, cornerRadius, cornerRadiusRatio).into(this)
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        val lp = layoutParams
        if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT && lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.onMeasure(makeSpec(heightMeasureSpec, View.MeasureSpec.EXACTLY), makeSpec(heightMeasureSpec, View.MeasureSpec.EXACTLY))
        } else if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT && lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            super.onMeasure(makeSpec(widthMeasureSpec, View.MeasureSpec.EXACTLY), makeSpec(widthMeasureSpec, View.MeasureSpec.EXACTLY))
        } else {
            if (width > height) {
                super.onMeasure(makeSpec(heightMeasureSpec, View.MeasureSpec.EXACTLY), makeSpec(heightMeasureSpec, View.MeasureSpec.EXACTLY))
            } else {
                super.onMeasure(makeSpec(widthMeasureSpec, View.MeasureSpec.EXACTLY), makeSpec(widthMeasureSpec, View.MeasureSpec.EXACTLY))
            }
        }
    }

    private fun makeSpec(spec: Int, mode: Int): Int {
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(spec), mode)
    }
}
