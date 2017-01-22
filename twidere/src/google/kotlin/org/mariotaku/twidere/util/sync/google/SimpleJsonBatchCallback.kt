package org.mariotaku.twidere.util.sync.google

import com.google.api.client.googleapis.batch.json.JsonBatchCallback
import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.HttpResponseException

import java.io.IOException

/**
 * Created by mariotaku on 1/22/17.
 */

internal open class SimpleJsonBatchCallback<T> : JsonBatchCallback<T>() {
    @Throws(IOException::class)
    override fun onFailure(error: GoogleJsonError, headers: HttpHeaders) {
    }

    @Throws(IOException::class)
    override fun onSuccess(result: T, headers: HttpHeaders) {
    }
}
