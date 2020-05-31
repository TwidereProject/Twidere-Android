package org.mariotaku.twidere.util

import androidx.recyclerview.widget.RecyclerView
import android.view.View

import org.mariotaku.twidere.util.ContentScrollHandler.ContentListSupport
import org.mariotaku.twidere.util.ContentScrollHandler.ViewCallback

/**
 * Created by mariotaku on 16/3/1.
 */
class RecyclerViewScrollHandler<A>(contentListSupport: ContentListSupport<A>, viewCallback: ViewCallback?) : RecyclerView.OnScrollListener() {

    internal val scrollHandler: ContentScrollHandler<A> = ContentScrollHandler(contentListSupport, viewCallback)
    private var oldState = RecyclerView.SCROLL_STATE_IDLE

    var touchSlop: Int
        get() = scrollHandler.touchSlop
        set(value) {
            scrollHandler.touchSlop = value
        }

    var reversed: Boolean
        get() = scrollHandler.reversed
        set(value) {
            scrollHandler.reversed = value
        }

    val touchListener: View.OnTouchListener
        get() = scrollHandler.touchListener

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        scrollHandler.handleScrollStateChanged(newState, RecyclerView.SCROLL_STATE_IDLE)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val scrollState = recyclerView.scrollState
        scrollHandler.handleScroll(dy, scrollState, oldState, RecyclerView.SCROLL_STATE_IDLE)
        oldState = scrollState
    }

    class RecyclerViewCallback(private val recyclerView: RecyclerView) : ViewCallback {

        override val computingLayout: Boolean
            get() = recyclerView.isComputingLayout

        override fun post(runnable: Runnable) {
            recyclerView.post(runnable)
        }
    }
}
