package org.mariotaku.simplerestapi;

import org.mariotaku.simplerestapi.http.RestHttpResponse;

import java.lang.reflect.Type;

/**
 * Created by mariotaku on 15/2/6.
 */
public interface Converter {

    Object convert(RestHttpResponse response, Type type) throws Exception;

}
