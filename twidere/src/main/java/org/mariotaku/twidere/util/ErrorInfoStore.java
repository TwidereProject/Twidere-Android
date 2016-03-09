package org.mariotaku.twidere.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.UserKey;


/**
 * Created by mariotaku on 16/1/31.
 */
public class ErrorInfoStore {

    public static final String KEY_DIRECT_MESSAGES = "direct_messages";
    public static final String KEY_INTERACTIONS = "interactions";
    public static final String KEY_HOME_TIMELINE = "home_timeline";
    public static final String KEY_ACTIVITIES_BY_FRIENDS = "activities_by_friends";

    public static final int CODE_NO_DM_PERMISSION = 1;
    public static final int CODE_NO_ACCESS_FOR_CREDENTIALS = 2;
    public static final int CODE_NETWORK_ERROR = 3;

    private final SharedPreferences mPreferences;

    public ErrorInfoStore(Application application) {
        mPreferences = application.getSharedPreferences("error_info", Context.MODE_PRIVATE);
    }

    public int get(String key) {
        return mPreferences.getInt(key, 0);
    }

    public int get(String key, String extraId) {
        return get(key + "_" + extraId);
    }

    public int get(String key, UserKey extraId) {
        final String host = extraId.getHost();
        if (host == null) {
            return get(key, extraId.getId());
        } else {
            return get(key + "_" + extraId.getId() + "_" + host);
        }
    }

    public void put(String key, int code) {
        mPreferences.edit().putInt(key, code).apply();
    }

    public void put(String key, String extraId, int code) {
        put(key + "_" + extraId, code);
    }

    public void put(String key, UserKey extraId, int code) {
        final String host = extraId.getHost();
        if (host == null) {
            put(key, extraId.getId(), code);
        } else {
            put(key + "_" + extraId.getId() + "_" + host, code);
        }
    }

    @Nullable
    public static DisplayErrorInfo getErrorInfo(@NonNull Context context, int code) {
        switch (code) {
            case CODE_NO_DM_PERMISSION: {
                return new DisplayErrorInfo(code, R.drawable.ic_info_error_generic,
                        context.getString(R.string.error_no_dm_permission));
            }
            case CODE_NO_ACCESS_FOR_CREDENTIALS: {
                return new DisplayErrorInfo(code, R.drawable.ic_info_error_generic,
                        context.getString(R.string.error_no_access_for_credentials));
            }
            case CODE_NETWORK_ERROR: {
                return new DisplayErrorInfo(code, R.drawable.ic_info_error_generic,
                        context.getString(R.string.network_error));
            }
        }
        return null;
    }

    public void remove(String key, String extraId) {
        remove(key + "_" + extraId);
    }

    public void remove(String key, UserKey extraId) {
        final String host = extraId.getHost();
        if (host == null) {
            remove(key, extraId.getId());
        } else {
            remove(key + "_" + extraId.getId() + "_" + host);
        }
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
