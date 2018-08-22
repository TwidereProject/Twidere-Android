package org.mariotaku.twidere.util.emoji

import org.mariotaku.microblog.library.mastodon.model.Emoji

fun addEmojisHtml(text: String, emojis: Array<Emoji>): String {
    var finalText = text
    emojis.map { emoji ->
        finalText = finalText.replace(":${emoji.shortcode}:", "<emoji src=\"${emoji.url}\">:${emoji.shortcode}:</emoji>")
    }
    return finalText
}
