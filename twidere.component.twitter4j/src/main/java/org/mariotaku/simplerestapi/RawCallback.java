package org.mariotaku.simplerestapi;

import org.mariotaku.simplerestapi.http.RestResponse;

import java.io.IOException;

/**
 * Created by mariotaku on 15/2/7.
 */
public interface RawCallback extends ErrorCallback {
    void result(RestResponse result) throws IOException;

}
