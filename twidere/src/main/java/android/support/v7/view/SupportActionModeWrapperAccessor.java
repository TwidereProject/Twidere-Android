package android.support.v7.view;

import android.support.annotation.Nullable;

/**
 * Created by mariotaku on 16/3/1.
 */
public class SupportActionModeWrapperAccessor {

    @Nullable
    public static android.support.v7.view.ActionMode getWrappedObject(android.view.ActionMode mode) {
        if (mode instanceof SupportActionModeWrapper) {
            return ((SupportActionModeWrapper) mode).mWrappedObject;
        }
        return null;
    }

}
