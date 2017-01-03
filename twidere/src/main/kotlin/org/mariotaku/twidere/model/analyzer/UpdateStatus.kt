package org.mariotaku.twidere.model.analyzer

import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.util.Analyzer

/**
 * Created by mariotaku on 2016/12/28.
 */

data class UpdateStatus(
        @AccountType override val accountType: String? = null,
        @Draft.Action val actionType: String?,
        @ParcelableMedia.Type val mediaType: Int,
        val hasLocation: Boolean,
        val preciseLocation: Boolean,
        val success: Boolean
) : Analyzer.Event {

    private val locationType: String get() = if (!hasLocation) {
        "none"
    } else if (preciseLocation) {
        "coordinate"
    } else {
        "place"
    }

    override val name: String
        get() = "Tweet"

    override fun forEachValues(action: (String, String?) -> Unit) {
        action("Status Type", actionTypeString(actionType))
        action("Media Type", mediaTypeString(mediaType))
        action("Location Type", locationType)
        action("Success", success.toString())
    }

    fun actionTypeString(@Draft.Action action: String?): String {
        return when (action) {
            Draft.Action.QUOTE -> "quote"
            Draft.Action.REPLY -> "reply"
            else -> "tweet"
        }
    }

    fun mediaTypeString(@ParcelableMedia.Type type: Int): String {
        return when (type) {
            ParcelableMedia.Type.IMAGE -> "image"
            ParcelableMedia.Type.VIDEO -> "video"
            ParcelableMedia.Type.ANIMATED_GIF -> "gif"
            ParcelableMedia.Type.CARD_ANIMATED_GIF -> "gif"
            ParcelableMedia.Type.EXTERNAL_PLAYER -> "external"
            ParcelableMedia.Type.VARIABLE_TYPE -> "variable"
            else -> "unknown"
        }
    }
}
