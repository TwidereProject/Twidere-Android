package org.mariotaku.yandex.model

import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
data class YandexTranslateResult(
        @JsonField(name = ["code"])
        var code: Int? = null,
        @JsonField(name = ["lang"])
        var lang: String? = null,
        @JsonField(name = ["text"])
        var text: List<String>? = null
)