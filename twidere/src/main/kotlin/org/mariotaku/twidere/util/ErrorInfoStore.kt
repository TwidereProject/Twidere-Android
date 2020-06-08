package org.mariotaku.twidere.util

import android.content.Context
import androidx.annotation.DrawableRes
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.UserKey


/**
 * Created by mariotaku on 16/1/31.
 */
class ErrorInfoStore(application: Context) {

    private val preferences = application.getSharedPreferences("error_info", Context.MODE_PRIVATE)

    operator fun get(key: String): Int {
        return preferences.getInt(key, 0)
    }

    operator fun get(key: String, extraId: String): Int {
        return get(key + "_" + extraId)
    }

    operator fun get(key: String, extraId: UserKey): Int {
        val host = extraId.host
        return if (host == null) {
            get(key, extraId.id)
        } else {
            get(key + "_" + extraId.id + "_" + host)
        }
    }

    operator fun set(key: String, code: Int) {
        preferences.edit().putInt(key, code).apply()
    }

    operator fun set(key: String, extraId: String, code: Int) {
        set(key + "_" + extraId, code)
    }

    operator fun set(key: String, extraId: UserKey, code: Int) {
        val host = extraId.host
        if (host == null) {
            set(key, extraId.id, code)
        } else {
            set(key + "_" + extraId.id + "_" + host, code)
        }
    }

    fun remove(key: String, extraId: String) {
        remove(key + "_" + extraId)
    }

    fun remove(key: String, extraId: UserKey) {
        val host = extraId.host
        if (host == null) {
            remove(key, extraId.id)
        } else {
            remove(key + "_" + extraId.id + "_" + host)
        }
    }

    fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    class DisplayErrorInfo(code: Int, @DrawableRes icon: Int, message: String) {
        var code: Int = 0
            internal set
        var icon: Int = 0
            internal set
        var message: String
            internal set

        init {
            this.code = code
            this.icon = icon
            this.message = message
        }
    }

    companion object {

        const val KEY_DIRECT_MESSAGES = "direct_messages"
        const val KEY_INTERACTIONS = "interactions"
        const val KEY_HOME_TIMELINE = "home_timeline"
        const val KEY_ACTIVITIES_BY_FRIENDS = "activities_by_friends"

        const val CODE_NO_DM_PERMISSION = 1
        const val CODE_NO_ACCESS_FOR_CREDENTIALS = 2
        const val CODE_NETWORK_ERROR = 3
        const val CODE_TIMESTAMP_ERROR = 4

        fun getErrorInfo(context: Context, code: Int): DisplayErrorInfo? {
            when (code) {
                CODE_NO_DM_PERMISSION -> {
                    return DisplayErrorInfo(code, R.drawable.ic_info_error_generic,
                            context.getString(R.string.error_no_dm_permission))
                }
                CODE_NO_ACCESS_FOR_CREDENTIALS -> {
                    return DisplayErrorInfo(code, R.drawable.ic_info_error_generic,
                            context.getString(R.string.error_no_access_for_credentials))
                }
                CODE_NETWORK_ERROR -> {
                    return DisplayErrorInfo(code, R.drawable.ic_info_error_generic,
                            context.getString(R.string.message_toast_network_error))
                }
                CODE_TIMESTAMP_ERROR -> {
                    return DisplayErrorInfo(code, R.drawable.ic_info_error_generic,
                            context.getString(R.string.error_info_oauth_timestamp_error))
                }
            }
            return null
        }
    }
}
