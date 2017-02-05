package org.mariotaku.twidere.util.filter

import android.content.Context
import android.net.Uri
import com.bluelinelabs.logansquare.LoganSquare
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import org.mariotaku.ktextension.convert
import org.mariotaku.restfu.annotation.method.GET
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.restfu.http.MultiValueMap
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.restfu.http.mime.Body
import org.mariotaku.twidere.extension.model.parse
import org.mariotaku.twidere.extension.newPullParser
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.util.ETagCache
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import java.io.IOException
import javax.inject.Inject

/**
 * Created by mariotaku on 2017/1/9.
 */

class UrlFiltersSubscriptionProvider(context: Context, val arguments: Arguments) : LocalFiltersSubscriptionProvider(context) {
    @Inject
    internal lateinit var restHttpClient: RestHttpClient
    @Inject
    internal lateinit var etagCache: ETagCache
    private var filters: FiltersData? = null

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    @Throws(IOException::class)
    override fun fetchFilters(): Boolean {
        val builder = HttpRequest.Builder()
        builder.method(GET.METHOD)
        builder.url(arguments.url)
        val headers = MultiValueMap<String>()
        etagCache[arguments.url]?.let { etag ->
            headers.add("If-None-Match", etag)
        }

        builder.headers(headers)
        val request = builder.build()
        restHttpClient.newCall(request).execute().use { response ->
            if (response.status != 200) {
                return false
            }
            val etag = response.getHeader("ETag")
            this.filters = response.body?.convert { body ->
                when (response.body.contentType()?.contentType) {
                    "application/json" -> {
                        return@convert body.toJsonFilters()
                    }
                    "application/xml", "text/xml" -> {
                        return@convert body.toXmlFilters()
                    }
                    else -> {
                        // Infer from extension
                        val uri = Uri.parse(arguments.url)
                        when (uri.lastPathSegment?.substringAfterLast('.')) {
                            "xml" -> return@convert body.toXmlFilters()
                            "json" -> return@convert body.toJsonFilters()
                        }
                        return@convert null
                    }
                }
            }
            etagCache[arguments.url] = etag
            return true
        }
    }

    override fun firstAdded(): Boolean {
        etagCache[arguments.url] = null
        return true
    }

    override fun deleteLocalData(): Boolean {
        etagCache[arguments.url] = null
        return true
    }

    override fun getUsers(): List<FiltersData.UserItem>? {
        return filters?.users
    }

    override fun getKeywords(): List<FiltersData.BaseItem>? {
        return filters?.keywords
    }

    override fun getSources(): List<FiltersData.BaseItem>? {
        return filters?.sources
    }

    override fun getLinks(): List<FiltersData.BaseItem>? {
        return filters?.links
    }

    private fun Body.toJsonFilters(): FiltersData? {
        return LoganSquare.parse(stream(), FiltersData::class.java)
    }

    private fun Body.toXmlFilters(): FiltersData? {
        return FiltersData().apply {
            this.parse(stream().newPullParser(Charsets.UTF_8))
        }
    }

    @JsonObject
    class Arguments {
        @JsonField(name = arrayOf("url"))
        lateinit var url: String
    }
}
