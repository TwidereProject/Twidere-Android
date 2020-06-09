package androidx.loader.content

import android.content.Context
import android.os.AsyncTask
import org.mariotaku.twidere.extension.set
import org.mariotaku.twidere.util.Analyzer

/**
 * Created by mariotaku on 2017/2/7.
 */

abstract class FixedAsyncTaskLoader<D>(context: Context) : AsyncTaskLoader<D>(context) {

    init {
        try {
            val executorField = javaClass.getDeclaredField("mExecutor")
            this[executorField] = AsyncTask.SERIAL_EXECUTOR
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun executePendingTask() {
        try {
            super.executePendingTask()
        } catch (e: IllegalStateException) {
            Analyzer.logException(IllegalStateException("IllegalStateException at ${this.javaClass}", e))
            // Ignore
        }

    }
}
