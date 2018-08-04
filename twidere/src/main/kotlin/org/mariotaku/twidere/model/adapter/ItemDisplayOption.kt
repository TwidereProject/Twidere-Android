package org.mariotaku.twidere.model.adapter

import org.mariotaku.twidere.annotation.ImageShapeStyle

data class ItemDisplayOption(
        var profileImageEnabled: Boolean,
        @ImageShapeStyle
        var profileImageStyle: Int,
        var textSize: Float,
        var showAbsoluteTime: Boolean
)