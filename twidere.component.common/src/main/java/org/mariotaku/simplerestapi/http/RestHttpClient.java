package org.mariotaku.simplerestapi.http;

import android.support.annotation.NonNull;

import java.io.IOException;

/**
 * Created by mariotaku on 15/2/7.
 */
public interface RestHttpClient {

    @NonNull
    RestHttpResponse execute(RestHttpRequest request) throws IOException;

}
