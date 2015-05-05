package org.mariotaku.simplerestapi.http;

/**
 * Created by mariotaku on 15/2/6.
 */
public interface ValueMap {

    public boolean has(String key);

    public Object get(String key);

}
