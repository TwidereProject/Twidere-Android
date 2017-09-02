package org.mariotaku.twidere.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

/**
 * Created by mariotaku on 2017/1/25.
 */

open class ContainerView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private var attachedToWindow: Boolean = false

    var viewController: ViewController? = null
        set(value) {
            field?.let { oldVc ->
                if (oldVc.attached) {
                    oldVc.attached = false
                    oldVc.onDetached()
                }
                val view = oldVc.view
                oldVc.onDestroyView(view)
                this.removeView(view)
                oldVc.onDestroy()
            }
            field = value
            if (value != null) {
                if (value.attached) {
                    throw IllegalStateException("ViewController has already attached")
                }
                value.context = context
                val view = value.onCreateView(this)
                this.addView(view)
                value.view = view
                value.onCreate()
                value.onViewCreated(view)
                if (attachedToWindow) {
                    value.attached = true
                    value.onAttached()
                }
            }
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachedToWindow = true
        viewController?.let { vc ->
            if (!vc.windowAttached) {
                vc.windowAttached = true
                vc.onWindowAttached()
            }
        }
    }

    override fun onDetachedFromWindow() {
        attachedToWindow = false
        viewController?.let { vc ->
            if (vc.attached) {
                vc.windowAttached = false
                vc.onWindowDetached()
            }
        }
        super.onDetachedFromWindow()
    }


    abstract class ViewController {

        lateinit var context: Context
            internal set
        lateinit var view: View
            internal set
        var attached: Boolean = false
            internal set
        var windowAttached: Boolean = false
            internal set

        open fun onCreate() {}
        open fun onDestroy() {}

        open fun onAttached() {}
        open fun onDetached() {}

        open fun onWindowAttached() {}
        open fun onWindowDetached() {}

        abstract fun onCreateView(parent: ContainerView): View

        open fun onDestroyView(view: View) {}

        open fun onViewCreated(view: View) {}
    }

}
