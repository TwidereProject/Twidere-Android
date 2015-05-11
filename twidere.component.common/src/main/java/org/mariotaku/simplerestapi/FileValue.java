package org.mariotaku.simplerestapi;

import org.mariotaku.simplerestapi.http.mime.BaseTypedData;
import org.mariotaku.simplerestapi.http.mime.TypedData;
import org.mariotaku.simplerestapi.param.File;

/**
 * Created by mariotaku on 15/2/6.
 */
public final class FileValue {
    private final File annotation;
    private final Object value;

    @Override
    public String toString() {
        return "FileValue{" +
                "annotation=" + annotation +
                ", value=" + value +
                '}';
    }

    public FileValue(File annotation, Object value) {
        this.annotation = annotation;
        this.value = value;
    }

    public TypedData body() {
        return BaseTypedData.wrap(value);
    }
}
