package org.mariotaku.twidere.model

import android.content.Context
import org.mariotaku.twidere.R

data class ModelCreationConfig(
        val profileImageSize: String,
        val summaryThumbnailSize: Int
) {
    companion object {
        val DEFAULT = ModelCreationConfig("normal", 100)

        fun obtain(context: Context): ModelCreationConfig {
            return ModelCreationConfig(context.getString(R.string.profile_image_size),
                    context.resources.getDimensionPixelSize(R.dimen.summary_thumbnail_size))
        }
    }
}