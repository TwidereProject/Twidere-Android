package org.mariotaku.ktextension

import android.media.MediaMetadataRetriever

/**
 * Created by mariotaku on 2017/1/24.
 */
fun MediaMetadataRetriever.releaseSafe() {
    try {
        release()
    } catch (e: Exception) {
        // Ignore any exceptions
    }
}
