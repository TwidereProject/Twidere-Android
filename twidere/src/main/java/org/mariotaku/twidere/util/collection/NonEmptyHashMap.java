package org.mariotaku.twidere.util.collection;

import java.util.HashMap;

/**
 * Created by mariotaku on 2016/12/7.
 */

public class NonEmptyHashMap<K, V> extends HashMap<K, V> {
    @Override
    public V put(K key, V value) {
        if (value == null) {
            remove(key);
            return null;
        }
        return super.put(key, value);
    }

}
