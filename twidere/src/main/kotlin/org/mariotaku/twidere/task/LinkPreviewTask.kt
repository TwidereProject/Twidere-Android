package org.mariotaku.twidere.task

import android.content.Context
import androidx.collection.LruCache
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mariotaku.ktextension.toString
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.twidere.model.event.StatusListChangedEvent
import org.mariotaku.twidere.view.LinkPreviewData

class LinkPreviewTask(
        context: Context
) : BaseAbstractTask<String, LinkPreviewData, Any>(context) {

    override fun doLongOperation(url: String?): LinkPreviewData? {
        if (url == null) {
            return null
        }
        loadingList.add(url)
        val response = runCatching {
            restHttpClient.newCall(
                    HttpRequest
                            .Builder()
                            .url(url.replace("http:", "https:"))
                            .method("GET")
                            .build()
            ).execute()
        }.getOrNull()
        return response?.body?.stream()?.toString(charset = Charsets.UTF_8, close = true)?.let {
            Jsoup.parse(it)
        }?.let { doc ->
            val title = doc.getMeta("og:title") ?: doc.title()
            val desc = doc.getMeta("og:description")
            val img = doc.getMeta("og:image")
            LinkPreviewData(
                    title, desc, img
            )
        }?.also {
            cacheData.put(url, it)
            loadingList.remove(url)
        }
    }

    override fun afterExecute(callback: Any?, result: LinkPreviewData?) {
        bus.post(StatusListChangedEvent())
    }

    private fun Document.getMeta(name: String): String? {
        return this.head().getElementsByAttributeValue("property", name).firstOrNull { it.tagName() == "meta" }?.attributes()?.get("content")
    }

    companion object {
        private val loadingList = arrayListOf<String>()
        private val cacheData = LruCache<String, LinkPreviewData>(100)

        fun isInLoading(url: String): Boolean {
            return loadingList.contains(url)
        }

        fun getCached(url: String): LinkPreviewData? {
            return cacheData[url]
        }
    }
}