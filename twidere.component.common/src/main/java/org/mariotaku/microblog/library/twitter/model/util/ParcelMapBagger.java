package org.mariotaku.microblog.library.twitter.model.util;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.ParcelBagger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mariotaku on 16/4/28.
 */
public abstract class ParcelMapBagger<T extends Parcelable> implements ParcelBagger<Map<String, T>> {
    private final Class<T> cls;

    protected ParcelMapBagger(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public final void write(Map<String, T> value, Parcel out, int flags) {
        if (value == null) {
            out.writeInt(-1);
        } else {
            int size = value.size();
            out.writeInt(size);
            for (Map.Entry<String, T> entry : value.entrySet()) {
                out.writeString(entry.getKey());
                out.writeParcelable(entry.getValue(), flags);
            }
        }
    }

    @Override
    public final Map<String, T> read(Parcel in) {
        int size = in.readInt();
        if (size < 0) return null;
        HashMap<String, T> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            final String key = in.readString();
            final T value = in.readParcelable(cls.getClassLoader());
            map.put(key, value);
        }
        return map;
    }
}
