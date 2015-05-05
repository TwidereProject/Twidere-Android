package org.mariotaku.simplerestapi;

import org.mariotaku.simplerestapi.http.RestResponse;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by mariotaku on 15/2/6.
 */
public interface Converter {

    public Object convert(RestResponse response, Type type) throws IOException;

}
