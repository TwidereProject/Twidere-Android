package org.mariotaku.twidere.task

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.text.Spannable
import android.text.Spanned
import org.mariotaku.twidere.model.SpanItem
import org.mariotaku.twidere.text.style.EmojiSpan
import java.net.URL


class EmojiSpanTask(
        private val spans: Array<SpanItem>,
        private val spannable: Spannable
) : AsyncTask<Any, Any, Any>() {

    private var mOnImageDownloadedListener: OnImageDownloadedListener? = null

    override fun doInBackground(vararg params: Any?) {
        spans.map { span ->
            if (span.type == SpanItem.SpanType.EMOJI) {
                val url = URL(span.link)
                val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                spannable.setSpan(EmojiSpan(BitmapDrawable(bmp)), span.start, span.end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    // Interface the task will use to communicate with your activity method.
    interface OnImageDownloadedListener {
        fun onImageDownloaded(spannable: Spannable)  // No need for context.
    }

    override fun onPostExecute(result: Any?) {
        mOnImageDownloadedListener?.onImageDownloaded(spannable)
    }

    // Setter.
    fun setOnImageDownloadedListener(listener: OnImageDownloadedListener) {
        mOnImageDownloadedListener = listener
    }

}
