package org.mariotaku.twidere.activity.iface

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.util.Property
import android.view.animation.DecelerateInterpolator

/**
 * Created by mariotaku on 14/10/21.
 */
interface IControlBarActivity {

    /**
     * 0: invisible, 1: visible
     */
    var controlBarOffset: Float
        get() = 0f
        set(value) {}

    val controlBarHeight: Int get() = 0

    fun setControlBarVisibleAnimate(visible: Boolean, listener: ControlBarShowHideHelper.ControlBarAnimationListener? = null) {}

    fun registerControlBarOffsetListener(listener: ControlBarOffsetListener) {}

    fun unregisterControlBarOffsetListener(listener: ControlBarOffsetListener) {}

    fun notifyControlBarOffsetChanged() {}

    interface ControlBarOffsetListener {
        fun onControlBarOffsetChanged(activity: IControlBarActivity, offset: Float)
    }

    class ControlBarShowHideHelper(private val activity: IControlBarActivity) {
        private var controlAnimationDirection: Int = 0
        private var currentControlAnimation: ObjectAnimator? = null

        private object ControlBarOffsetProperty : Property<IControlBarActivity, Float>(Float::class.java, null) {

            override fun set(obj: IControlBarActivity, value: Float) {
                obj.controlBarOffset = (value)
            }

            override fun get(obj: IControlBarActivity): Float {
                return obj.controlBarOffset
            }

        }

        interface ControlBarAnimationListener {
            fun onControlBarVisibleAnimationStart(visible: Boolean) {}
            fun onControlBarVisibleAnimationFinish(visible: Boolean) {}
        }

        fun setControlBarVisibleAnimate(visible: Boolean, listener: ControlBarAnimationListener? = null) {
            val newDirection = if (visible) 1 else -1
            if (controlAnimationDirection == newDirection) return
            if (currentControlAnimation != null && controlAnimationDirection != 0) {
                currentControlAnimation!!.cancel()
                currentControlAnimation = null
                controlAnimationDirection = newDirection
            }
            val animator: ObjectAnimator
            val offset = activity.controlBarOffset
            animator = if (visible) {
                if (offset >= 1) return
                ObjectAnimator.ofFloat(activity, ControlBarOffsetProperty, offset, 1f)
            } else {
                if (offset <= 0) return
                ObjectAnimator.ofFloat(activity, ControlBarOffsetProperty, offset, 0f)
            }
            animator.interpolator = DecelerateInterpolator()
            animator.addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    listener?.onControlBarVisibleAnimationStart(visible)
                }

                override fun onAnimationEnd(animation: Animator) {
                    controlAnimationDirection = 0
                    currentControlAnimation = null
                    listener?.onControlBarVisibleAnimationFinish(visible)
                }

                override fun onAnimationCancel(animation: Animator) {
                    controlAnimationDirection = 0
                    currentControlAnimation = null
                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            animator.duration = DURATION
            animator.start()
            currentControlAnimation = animator
            controlAnimationDirection = newDirection
        }

        companion object {

            private const val DURATION = 200L
        }
    }
}
