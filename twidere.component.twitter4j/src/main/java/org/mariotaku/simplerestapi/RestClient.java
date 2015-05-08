package org.mariotaku.simplerestapi;

import org.mariotaku.simplerestapi.http.Authorization;
import org.mariotaku.simplerestapi.http.Endpoint;
import org.mariotaku.simplerestapi.http.RestHttpClient;

/**
 * Created by mariotaku on 15/4/19.
 */
public interface RestClient {
    Endpoint getEndpoint();

    RestHttpClient getRestClient();

    Converter getConverter();

    Authorization getAuthorization();

}
