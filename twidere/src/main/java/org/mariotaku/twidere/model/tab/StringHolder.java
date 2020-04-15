package org.mariotaku.twidere.model.tab;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * Created by mariotaku on 2016/11/28.
 */

public abstract class StringHolder {

    public abstract String createString(Context context);

    @NonNull
    public static StringHolder resource(@StringRes int resourceId) {
        return new Resource(resourceId);
    }

    private static class Resource extends StringHolder implements Parcelable {

        @StringRes
        private final int resourceId;

        Resource(@StringRes int resourceId) {
            this.resourceId = resourceId;
        }

        @Override
        public String createString(Context context) {
            return context.getString(resourceId);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(resourceId);
        }

        public static final Creator<Resource> CREATOR = new Creator<Resource>() {
            public Resource createFromParcel(Parcel source) {
                return new Resource(source.readInt());
            }

            public Resource[] newArray(int size) {
                return new Resource[size];
            }
        };
    }
}
