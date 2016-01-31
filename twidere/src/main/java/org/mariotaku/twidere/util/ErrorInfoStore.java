package org.mariotaku.twidere.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.twidere.R;


/**
 * Created by mariotaku on 16/1/31.
 */
public class ErrorInfoStore {

    public static final String KEY_DIRECT_MESSAGES = "direct_messages";

    public static final int CODE_NO_DM_PERMISSION = 1;

    private final SharedPreferences mPreferences;

    public ErrorInfoStore(Application application) {
        mPreferences = application.getSharedPreferences("error_info", Context.MODE_PRIVATE);
    }

    public int get(String key) {
        return mPreferences.getInt(key, 0);
    }

    public int get(String key, long extraId) {
        return get(key + "_" + extraId);
    }

    public void put(String key, int code) {
        mPreferences.edit().putInt(key, code).apply();
    }

    public void put(String key, long extraId, int code) {
        put(key + "_" + extraId, code);
    }

    @Nullable
    public static DisplayErrorInfo getErrorInfo(@NonNull Context context, int code) {
        switch (code) {
            case CODE_NO_DM_PERMISSION: {
                return new DisplayErrorInfo(code, R.drawable.ic_info_error_generic,
                        context.getString(R.string.error_no_dm_permission));
            }
        }
        return null;
    }

    public void remove(String key, long extraId) {
        remove(key + "_" + extraId);
    }

    public void remove(String key) {
        mPreferences.edit().remove(key).apply();
    }

    public static class DisplayErrorInfo {
        int code;
        int icon;
        String message;

        public DisplayErrorInfo(int code, int icon, String message) {
            this.code = code;
            this.icon = icon;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public int getIcon() {
            return icon;
        }

        public String getMessage() {
            return message;
        }
    }
}
