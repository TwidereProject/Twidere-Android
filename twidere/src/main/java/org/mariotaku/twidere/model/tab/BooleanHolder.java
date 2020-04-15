package org.mariotaku.twidere.model.tab;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.BoolRes;
import androidx.annotation.NonNull;

/**
 * Created by mariotaku on 2016/11/28.
 */

public abstract class BooleanHolder {

    public abstract boolean createBoolean(Context context);

    @NonNull
    public static BooleanHolder resource(@BoolRes int resourceId) {
        return new Resource(resourceId);
    }

    @NonNull
    public static BooleanHolder constant(boolean value) {
        return new Constant(value);
    }

    private static class Constant extends BooleanHolder implements Parcelable {

        private final boolean constant;

        private Constant(boolean constant) {
            this.constant = constant;
        }

        @Override
        public boolean createBoolean(Context context) {
            return false;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (constant ? 1 : 0));
        }

        public static final Creator<Constant> CREATOR = new Creator<Constant>() {
            public Constant createFromParcel(Parcel source) {
                return new Constant(source.readByte() == 1);
            }

            public Constant[] newArray(int size) {
                return new Constant[size];
            }
        };
    }

    private static class Resource extends BooleanHolder implements Parcelable {

        @BoolRes
        private final int resourceId;

        Resource(@BoolRes int resourceId) {
            this.resourceId = resourceId;
        }

        @Override
        public boolean createBoolean(Context context) {
            return context.getResources().getBoolean(resourceId);
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
