package org.mariotaku.twidere.model.util

import android.text.TextUtils
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.math.NumberUtils
import org.mariotaku.ktextension.addAllTo
import org.mariotaku.ktextension.isNullOrEmpty
import org.mariotaku.microblog.library.twitter.model.*
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.InternalTwitterContentUtils
import org.mariotaku.twidere.util.media.preview.PreviewMediaExtractor

/**
 * Created by mariotaku on 16/2/13.
 */
object ParcelableMediaUtils {

    fun fromEntities(entities: EntitySupport): Array<ParcelableMedia> {
        val list = ArrayList<ParcelableMedia>()
        val mediaEntities: Array<MediaEntity>?
        if (entities is ExtendedEntitySupport) {
            val extendedMediaEntities = entities.extendedMediaEntities
            mediaEntities = extendedMediaEntities ?: entities.mediaEntities
        } else {
            mediaEntities = entities.mediaEntities
        }
        if (mediaEntities != null) {
            for (media in mediaEntities) {
                val mediaURL = InternalTwitterContentUtils.getMediaUrl(media)
                if (mediaURL != null) {
                    list.add(ParcelableMediaUtils.fromMediaEntity(media))
                }
            }
        }
        val urlEntities = entities.urlEntities
        if (urlEntities != null) {
            for (url in urlEntities) {
                val expanded = url.expandedUrl
                val media = PreviewMediaExtractor.fromLink(expanded)
                if (media != null) {
                    list.add(media)
                }
            }
        }
        return list.toTypedArray()
    }

    fun fromMediaEntity(entity: MediaEntity): ParcelableMedia {
        val media = ParcelableMedia()
        val mediaUrl = InternalTwitterContentUtils.getMediaUrl(entity)
        media.url = mediaUrl
        media.media_url = mediaUrl
        media.preview_url = mediaUrl
        media.page_url = entity.expandedUrl
        media.type = ParcelableMediaUtils.getTypeInt(entity.type)
        media.alt_text = entity.altText
        val size = entity.sizes[MediaEntity.ScaleType.LARGE]
        if (size != null) {
            media.width = size.width
            media.height = size.height
        } else {
            media.width = 0
            media.height = 0
        }
        media.video_info = ParcelableMedia.VideoInfo.fromMediaEntityInfo(entity.videoInfo)
        return media
    }

    fun fromStatus(status: Status, accountKey: UserKey, accountType: String): Array<ParcelableMedia>? {
        return fromEntities(status) + fromAttachments(status) + fromCard(status.card,
                status.urlEntities, status.mediaEntities, status.extendedMediaEntities, accountKey,
                accountType) + fromPhoto(status)
    }

    private fun fromPhoto(status: Status): Array<ParcelableMedia> {
        val photo = status.photo ?: return emptyArray()
        val media = ParcelableMedia()
        media.type = ParcelableMedia.Type.IMAGE
        media.url = photo.url
        media.page_url = photo.url
        media.media_url = photo.largeUrl
        media.preview_url = photo.imageUrl
        return arrayOf(media)
    }

    private fun fromAttachments(status: Status): Array<ParcelableMedia> {
        val attachments = status.attachments ?: return emptyArray()
        val temp = arrayOfNulls<ParcelableMedia>(attachments.size)
        val externalUrl = status.externalUrl
        var i = 0
        for (attachment in attachments) {
            val mimeType = attachment.mimetype ?: continue
            val media = ParcelableMedia()

            if (mimeType.startsWith("image/")) {
                media.type = ParcelableMedia.Type.IMAGE
            } else if (mimeType.startsWith("video/")) {
                media.type = ParcelableMedia.Type.VIDEO
            } else {
                // https://github.com/TwidereProject/Twidere-Android/issues/729
                // Skip unsupported attachment
                continue
            }
            media.width = attachment.width
            media.height = attachment.height
            media.url = if (TextUtils.isEmpty(externalUrl)) attachment.url else externalUrl
            media.page_url = if (TextUtils.isEmpty(externalUrl)) attachment.url else externalUrl
            media.media_url = attachment.url
            media.preview_url = attachment.largeThumbUrl
            temp[i++] = media
        }
        return ArrayUtils.subarray<ParcelableMedia>(temp, 0, i)
    }

    private fun fromCard(card: CardEntity?, urlEntities: Array<UrlEntity>?,
            mediaEntities: Array<MediaEntity>?, extendedMediaEntities: Array<MediaEntity>?,
            accountKey: UserKey, accountType: String): Array<ParcelableMedia> {
        if (card == null) return emptyArray()
        val name = card.name
        if ("animated_gif" == name || "player" == name) {
            val media = ParcelableMedia()
            val playerStreamUrl = card.getBindingValue("player_stream_url")
            media.card = ParcelableCardEntityUtils.fromCardEntity(card, accountKey,
                    accountType)
            val appUrlResolved = card.getBindingValue("app_url_resolved") as CardEntity.StringValue
            media.url = if (checkUrl(appUrlResolved)) appUrlResolved.value else card.url
            if ("animated_gif" == name) {
                media.media_url = (playerStreamUrl as CardEntity.StringValue).value
                media.type = ParcelableMedia.Type.CARD_ANIMATED_GIF
            } else if (playerStreamUrl is CardEntity.StringValue) {
                media.media_url = playerStreamUrl.value
                media.type = ParcelableMedia.Type.VIDEO
            } else {
                val playerUrl = card.getBindingValue("player_url") as? CardEntity.StringValue
                if (playerUrl != null) {
                    media.media_url = playerUrl.value
                }
                media.type = ParcelableMedia.Type.EXTERNAL_PLAYER
            }
            val playerImage = card.getBindingValue("player_image")
            if (playerImage is CardEntity.ImageValue) {
                media.preview_url = playerImage.url
                media.width = playerImage.width
                media.height = playerImage.height
            }
            val playerWidth = card.getBindingValue("player_width")
            val playerHeight = card.getBindingValue("player_height")
            if (playerWidth is CardEntity.StringValue && playerHeight is CardEntity.StringValue) {
                media.width = NumberUtils.toInt(playerWidth.value, -1)
                media.height = NumberUtils.toInt(playerHeight.value, -1)
            }
            writeLinkInfo(media, urlEntities, mediaEntities, extendedMediaEntities)
            return arrayOf(media)
        } else if ("summary_large_image" == name) {
            val photoImageFullSize = card.getBindingValue("photo_image_full_size") as? CardEntity.ImageValue ?: return emptyArray()

            val media = ParcelableMedia()
            media.url = card.url
            media.card = ParcelableCardEntityUtils.fromCardEntity(card, accountKey,
                    accountType)
            media.type = ParcelableMedia.Type.IMAGE
            media.media_url = photoImageFullSize.url
            media.width = photoImageFullSize.width
            media.height = photoImageFullSize.height
            media.open_browser = true
            val summaryPhotoImage = card.getBindingValue("summary_photo_image")
            if (summaryPhotoImage is CardEntity.ImageValue) {
                media.preview_url = summaryPhotoImage.url
            }
            return arrayOf(media)
        }
        return emptyArray()
    }

    private fun writeLinkInfo(media: ParcelableMedia, vararg entities: Array<out UrlEntity>?) {
        entities.forEach { array ->
            if (array == null) return@forEach
            for (entity in array) {
                if (entity.url == media.url) {
                    media.page_url = entity.expandedUrl
                    if (media.page_url == null) {
                        media.page_url = media.url
                    }
                    break
                }
            }
        }
    }

    private fun checkUrl(value: CardEntity.StringValue?): Boolean {
        if (value == null) return false
        val valueString = value.value
        return valueString != null && (valueString.startsWith("http://") || valueString.startsWith("https://"))
    }

    fun getTypeInt(type: String): Int {
        when (type) {
            MediaEntity.Type.PHOTO -> return ParcelableMedia.Type.IMAGE
            MediaEntity.Type.VIDEO -> return ParcelableMedia.Type.VIDEO
            MediaEntity.Type.ANIMATED_GIF -> return ParcelableMedia.Type.ANIMATED_GIF
        }
        return ParcelableMedia.Type.UNKNOWN
    }

    fun image(url: String): ParcelableMedia {
        val media = ParcelableMedia()
        media.type = ParcelableMedia.Type.IMAGE
        media.url = url
        media.media_url = url
        media.preview_url = url
        return media
    }

    fun hasPlayIcon(@ParcelableMedia.Type type: Int): Boolean {
        when (type) {
            ParcelableMedia.Type.VIDEO, ParcelableMedia.Type.ANIMATED_GIF,
            ParcelableMedia.Type.CARD_ANIMATED_GIF, ParcelableMedia.Type.EXTERNAL_PLAYER -> return true
            else -> return false
        }
    }

    fun findByUrl(media: Array<ParcelableMedia>?, url: String?): ParcelableMedia? {
        if (media == null || url == null) return null
        for (item in media) {
            if (url == item.url) return item
        }
        return null
    }

    fun getPrimaryMedia(status: ParcelableStatus): Array<ParcelableMedia>? {
        if (status.is_quote && status.media.isNullOrEmpty()) {
            return status.quoted_media
        } else {
            return status.media
        }
    }

    fun getAllMedia(status: ParcelableStatus): Array<ParcelableMedia> {
        val result = ArrayList<ParcelableMedia>()
        status.media?.addAllTo(result)
        status.quoted_media?.addAllTo(result)
        return result.toTypedArray()
    }
}
