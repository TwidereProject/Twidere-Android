package org.mariotaku.twidere.task

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import org.mariotaku.twidere.text.style.EmojiSpan
import java.net.URL


class EmojiSpanTask(
        private val emojiurl: String
) : AsyncTask<Any, Any, Any>() {

    private var mOnImageDownloadedListener: OnImageDownloadedListener? = null
    private var retEmojiSpan: EmojiSpan? = null

    override fun doInBackground(vararg params: Any?) {
        retEmojiSpan = EmojiSpan(BitmapDrawable(BitmapFactory.decodeStream(URL(emojiurl).openConnection().getInputStream())))
    }

    // Interface the task will use to communicate with your activity method.
    interface OnImageDownloadedListener {
        fun onImageDownloaded(retEmojiSpan: EmojiSpan?)  // No need for context.
    }

    override fun onPostExecute(result: Any?) {
        mOnImageDownloadedListener?.onImageDownloaded(retEmojiSpan)
    }

    // Setter.
    fun setOnImageDownloadedListener(listener: OnImageDownloadedListener) {
        mOnImageDownloadedListener = listener
    }

}
