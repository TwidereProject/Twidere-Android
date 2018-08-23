package org.mariotaku.twidere.util.emoji

import org.mariotaku.microblog.library.mastodon.model.Emoji

fun addEmojisHtml(text: String, emojis: Array<Emoji>): String {
    var finalText = text
    emojis.map { emoji ->
        val emojiUrl = emoji.url ?: emoji.staticUrl
        finalText = finalText.replace(":${emoji.shortcode}:", "<emoji src=\"$emojiUrl\">:${emoji.shortcode}:</emoji>")
    }
    return finalText
}
