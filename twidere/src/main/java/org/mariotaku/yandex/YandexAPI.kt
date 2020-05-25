package org.mariotaku.yandex

import org.mariotaku.restfu.annotation.method.POST
import org.mariotaku.restfu.annotation.param.Query
import org.mariotaku.yandex.model.YandexTranslateResult

interface YandexAPI {
    @POST("/api/v1.5/tr.json/translate")
    fun search(@Query("text") text: String,
               @Query("lang") lang: String): YandexTranslateResult
}