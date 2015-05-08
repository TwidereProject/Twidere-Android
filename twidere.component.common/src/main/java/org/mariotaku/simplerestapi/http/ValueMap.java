package org.mariotaku.simplerestapi.http;

/**
 * Created by mariotaku on 15/2/6.
 */
public interface ValueMap {

    boolean has(String key);

    Object get(String key);

    String[] keys();

}
