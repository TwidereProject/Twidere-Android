package org.mariotaku.twidere.task

import android.content.Context
import androidx.collection.LruCache
import org.attoparser.config.ParseConfiguration
import org.attoparser.dom.DOMMarkupParser
import org.attoparser.dom.Document
import org.mariotaku.ktextension.toString
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.twidere.extension.atto.firstElementOrNull
import org.mariotaku.twidere.view.LinkPreviewData

class LinkPreviewTask(
        context: Context
) : BaseAbstractTask<String, LinkPreviewData, Any>(context) {

    override fun doLongOperation(url: String?): LinkPreviewData? {
        if (url == null) {
            return null
        }
        loadingList.add(url)
        val response = restHttpClient.newCall(
                HttpRequest
                        .Builder()
                        .url(url.replace("http:", "https:"))
                        .method("GET")
                        .build()
        ).execute()
        //TODO: exception handling
        return response.body.stream().toString(charset = Charsets.UTF_8, close = true).let {
            val parser = DOMMarkupParser(ParseConfiguration.htmlConfiguration())
            parser.parse(it)
        }?.let { doc ->
            val title = doc.getMeta("og:title")
            val desc = doc.getMeta("og:description")
            val img = doc.getMeta("og:image")
            LinkPreviewData(
                    title, desc, img
            )
        }?.also {
            cacheData.put(url, it)
            loadingList.remove(url)
            //TODO: send the result back to bus
        }
    }

    private fun Document.getMeta(name: String): String? {
        return firstElementOrNull {
            it.elementNameMatches("meta") &&
                    it.hasAttribute("property") &&
                    it.getAttributeValue("property") == name
        }?.getAttributeValue("content")
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