package android.support.v4.content

import android.content.Context

import org.mariotaku.twidere.util.Analyzer

/**
 * Created by mariotaku on 2017/2/7.
 */

abstract class FixedAsyncTaskLoader<D>(context: Context) : AsyncTaskLoader<D>(context) {

    internal override fun executePendingTask() {
        try {
            super.executePendingTask()
        } catch (e: IllegalStateException) {
            Analyzer.logException(IllegalStateException("IllegalStateException at ${this.javaClass}", e))
            // Ignore
        }

    }
}
