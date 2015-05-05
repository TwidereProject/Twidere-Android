package org.mariotaku.simplerestapi.http;

import java.io.IOException;

/**
 * Created by mariotaku on 15/2/7.
 */
public interface RestHttpClient {

    RestResponse execute(RestRequest request) throws IOException;

}
