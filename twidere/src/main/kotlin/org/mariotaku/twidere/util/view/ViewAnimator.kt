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

package org.mariotaku.twidere.util.view

import android.animation.Animator
import android.view.View

/**
 * Created by mariotaku on 2017/3/28.
 */

class ViewAnimator {

    private val managedViews = HashMap<View, Config>()
    private var currentInAnimator: Animator? = null
    private var currentOutAnimator: Animator? = null

    var currentChild: View? = null
        private set

    fun addView(view: View, config: Config.(View) -> Unit) {
        managedViews[view] = Config().apply { config(view) }
    }

    fun showView(view: View?, animate: Boolean) {
        currentInAnimator?.cancel()
        currentOutAnimator?.cancel()

        val oldView = currentChild

        currentOutAnimator = managedViews[oldView]?.outAnimator?.also { animator ->
            animator.addListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animation: Animator) {
                    oldView?.visibility = View.GONE
                    animator.removeListener(this)
                }

                override fun onAnimationCancel(animation: Animator) {
                    animator.removeListener(this)
                }
            })
            if (animate) {
                animator.start()
            } else {
                animator.end()
            }
        }
        currentInAnimator = managedViews[view]?.inAnimator?.also { animator ->
            animator.addListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animation: Animator) {
                    animator.removeListener(this)
                }

                override fun onAnimationCancel(animation: Animator) {
                    animator.removeListener(this)
                }

                override fun onAnimationStart(animation: Animator) {
                    view?.visibility = View.VISIBLE
                }
            })
            if (animate) {
                animator.start()
            } else {
                animator.end()
            }
        }

        currentChild = view
    }

    class Config {

        var inAnimator: Animator? = null
        var outAnimator: Animator? = null

    }

    internal abstract class SimpleAnimatorListener : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator) {
        }

        override fun onAnimationEnd(animation: Animator) {
        }

        override fun onAnimationCancel(animation: Animator) {
        }

        override fun onAnimationStart(animation: Animator) {
        }

    }
}
