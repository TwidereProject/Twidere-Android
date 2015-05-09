/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.simplerestapi.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by mariotaku on 15/5/8.
 */
public class SimpleValueMap implements ValueMap {

    private final Map<String, Object> internalMap = new HashMap<>();

    @Override
    public boolean has(String key) {
        return internalMap.containsKey(key);
    }

    @Override
    public Object get(String key) {
        return internalMap.get(key);
    }

    protected void put(String key, Object value) {
        internalMap.put(key, value);
    }

    protected void remove(String key) {
        internalMap.remove(key);
    }

    @Override
    public String[] keys() {
        final Set<String> keySet = internalMap.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    protected void copyValue(ValueMap from, String key) {
        if (from.has(key)) {
            put(key, from.get(key));
        }
    }
}
