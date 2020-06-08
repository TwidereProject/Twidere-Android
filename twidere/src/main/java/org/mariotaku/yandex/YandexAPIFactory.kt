package org.mariotaku.yandex

import org.mariotaku.restfu.RestAPIFactory
import org.mariotaku.restfu.RestConverter
import org.mariotaku.restfu.RestMethod
import org.mariotaku.restfu.RestRequest
import org.mariotaku.restfu.http.Endpoint
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.restfu.http.ValueMap
import org.mariotaku.restfu.logansqaure.LoganSquareConverterFactory


class YandexAPIFactory(apiKey: String, endpoint: String) {
    private val factory: RestAPIFactory<YandexException> = RestAPIFactory<YandexException>()

    init {
        factory.setEndpoint(Endpoint(endpoint))
        factory.setExceptionFactory { cause, _, _ ->
            cause?.let { YandexException(it) }
                    ?: YandexException()
        }
        factory.setRestConverterFactory(LoganSquareConverterFactory())
        factory.setRestRequestFactory(object : RestRequest.DefaultFactory<YandexException?>() {
            override fun create(restMethod: RestMethod<YandexException?>,
                                factory: RestConverter.Factory<YandexException?>, valuePool: ValueMap?): RestRequest {
                val method = restMethod.method
                val path = restMethod.path
                val headers = restMethod.getHeaders(valuePool)
                val queries = restMethod.getQueries(valuePool)
                val params = restMethod.getParams(factory, valuePool)
                val rawValue = restMethod.rawValue
                val bodyType = restMethod.bodyType
                val extras = restMethod.extras
                queries.add("key", apiKey)
                return RestRequest(method.value, method.allowBody, path, headers, queries,
                        params, rawValue, bodyType, extras)
            }
        })
    }

    fun setHttpClient(restClient: RestHttpClient): YandexAPIFactory {
        factory.setHttpClient(restClient)
        return this
    }

    fun build(): YandexAPI {
        return factory.build(YandexAPI::class.java)
    }
}

