/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.jsonserializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public final class JSONParcel {

    private final JSONObject jsonObject;

    JSONParcel() {
        this(new JSONObject());
    }

    JSONParcel(final JSONObject json) {
        if (json == null) throw new NullPointerException();
        jsonObject = json;
    }

    public boolean contains(final String key) {
        return jsonObject.has(key);
    }

    public JSONObject getJSON() {
        return jsonObject;
    }

    public boolean isNull(String key) {
        return jsonObject.isNull(key);
    }

    public boolean readBoolean(final String key) {
        return jsonObject.optBoolean(key);
    }

    public boolean readBoolean(final String key, final boolean def) {
        return jsonObject.optBoolean(key, def);
    }

    public double readDouble(final String key) {
        return jsonObject.optDouble(key);
    }

    public double readDouble(final String key, final double def) {
        return jsonObject.optDouble(key, def);
    }

    public float readFloat(final String key, final float def) {
        return (float) readDouble(key, def);
    }

    public int readInt(final String key) {
        return jsonObject.optInt(key);
    }

    public int readInt(final String key, final int def) {
        return jsonObject.optInt(key, def);
    }

    public JSONObject readJSONObject(final String key) {
        return jsonObject.optJSONObject(key);
    }

    public JSONArray readJSONArray(final String key) {
        return jsonObject.optJSONArray(key);
    }

    public JSONArrayParcel readJSONArrayParcel(final String key) {
        if (jsonObject.isNull(key)) return null;
        return new JSONArrayParcel(readJSONArray(key));
    }

    public String[] readStringArray(final String key) {
        if (jsonObject.isNull(key)) return null;
        final JSONArray array = jsonObject.optJSONArray(key);
        final String[] stringArray = new String[array.length()];
        for (int i = 0, j = array.length(); i < j; i++) {
            try {
                stringArray[i] = array.getString(i);
            } catch (JSONException e) {
                return null;
            }
        }
        return stringArray;
    }

    public long readLong(final String key) {
        return jsonObject.optLong(key);
    }

    public long readLong(final String key, final long def) {
        return jsonObject.optLong(key, def);
    }

    public Object readObject(final String key) {
        return jsonObject.opt(key);
    }

    public <T extends JSONParcelable> T readParcelable(final String key, final JSONParcelable.Creator<T> creator) {
        return JSONSerializer.createObject(creator, jsonObject.optJSONObject(key));
    }

    public <T extends JSONParcelable> T[] readParcelableArray(final String key, final JSONParcelable.Creator<T> creator) {
        return JSONSerializer.createArray(creator, jsonObject.optJSONArray(key));
    }

    public String readString(final String key) {
        return readString(key, null);
    }

    public String readString(final String key, final String def) {
        return jsonObject.optString(key, def);
    }

    public void writeBoolean(final String key, final boolean value) {
        try {
            jsonObject.put(key, value);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeBooleanArray(final String key, final boolean[] value) {
        if (key == null) return;
        try {
            if (value == null) {
                jsonObject.put(key, JSONObject.NULL);
                return;
            }
            final JSONArray array = new JSONArray();
            for (final boolean item : value) {
                array.put(item);
            }
            jsonObject.put(key, array);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeDouble(final String key, final double value) {
        try {
            jsonObject.put(key, value);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeDoubleArray(final String key, final double[] value) {
        if (key == null) return;
        try {
            if (value == null) {
                jsonObject.put(key, JSONObject.NULL);
                return;
            }
            final JSONArray array = new JSONArray();
            for (final double item : value) {
                array.put(item);
            }
            jsonObject.put(key, array);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeFloat(final String key, final float value) {
        writeDouble(key, value);
    }

    public void writeFloatArray(final String key, final float[] value) {
        try {
            if (value == null) {
                jsonObject.put(key, JSONObject.NULL);
                return;
            }
            final JSONArray array = new JSONArray();
            for (final float item : value) {
                array.put(item);
            }
            jsonObject.put(key, array);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeInt(final String key, final int value) {
        try {
            jsonObject.put(key, value);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeIntArray(final String key, final int[] value) {
        if (key == null) return;
        try {
            if (value == null) {
                jsonObject.put(key, JSONObject.NULL);
                return;
            }
            final JSONArray array = new JSONArray();
            for (final int item : value) {
                array.put(item);
            }
            jsonObject.put(key, array);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeJSONArray(final String key, final JSONArray value) {
        try {
            jsonObject.put(key, value);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeJSONObject(final String key, final JSONObject value) {
        try {
            jsonObject.put(key, value);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeLong(final String key, final long value) {
        try {
            jsonObject.put(key, value);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeLongArray(final String key, final long[] value) {
        if (key == null) return;
        try {
            if (value == null) {
                jsonObject.put(key, JSONObject.NULL);
                return;
            }
            final JSONArray array = new JSONArray();
            for (final long item : value) {
                array.put(item);
            }
            jsonObject.put(key, array);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeObject(final String key, final Object value) {
        if (value instanceof JSONParcelable) {
            writeParcelable(key, (JSONParcelable) value);
            return;
        }
        try {
            jsonObject.put(key, value);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeObjectArray(final String key, final Object[] value) {
        if (key == null) return;
        try {
            if (value == null) {
                jsonObject.put(key, JSONObject.NULL);
                return;
            }
            final JSONArray array = new JSONArray();
            for (final Object item : value) {
                if (item instanceof JSONParcelable) {
                    final JSONObject json = JSONSerializer.toJSONObject((JSONParcelable) item);
                    array.put(json);
                } else {
                    array.put(item);
                }
            }
            jsonObject.put(key, array);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeObjectList(final String key, final List<Object> value) {
        if (key == null) return;
        writeObjectArray(key, value.toArray());
    }

    public <T extends JSONParcelable> void writeParcelable(final String key, final T value) {
        if (key == null) return;
        try {
            if (value == null) {
                jsonObject.put(key, JSONObject.NULL);
                return;
            }
            final JSONObject json = JSONSerializer.toJSONObject(value);
            jsonObject.put(key, json);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public <T extends JSONParcelable> void writeParcelableArray(final String key, final T[] value) {
        if (key == null) return;
        try {
            if (value == null) {
                jsonObject.put(key, JSONObject.NULL);
                return;
            }
            final JSONArray array = new JSONArray();
            for (final T item : value) {
                final JSONObject json = JSONSerializer.toJSONObject(item);
                array.put(json != null ? json : JSONObject.NULL);
            }
            jsonObject.put(key, array);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeString(final String key, final String value) {
        if (key == null) return;
        try {
            jsonObject.put(key, value);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeStringArray(final String key, final String[] value) {
        if (key == null) return;
        try {
            if (value == null) {
                jsonObject.put(key, JSONObject.NULL);
                return;
            }
            final JSONArray array = new JSONArray();
            for (final String item : value) {
                array.put(item);
            }
            jsonObject.put(key, array);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    public void writeValue(final String key, final Object value) {
        if (key == null) return;
        try {
            jsonObject.put(key, value);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }
}
