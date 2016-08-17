package org.mariotaku.twidere.model.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.microblog.library.fanfou.model.Photo;
import org.mariotaku.microblog.library.gnusocial.model.Attachment;
import org.mariotaku.microblog.library.twitter.model.CardEntity;
import org.mariotaku.microblog.library.twitter.model.EntitySupport;
import org.mariotaku.microblog.library.twitter.model.ExtendedEntitySupport;
import org.mariotaku.microblog.library.twitter.model.MediaEntity;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.UrlEntity;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableMediaUpdate;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.TwidereArrayUtils;
import org.mariotaku.twidere.util.media.preview.PreviewMediaExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 16/2/13.
 */
public class ParcelableMediaUtils {
    private ParcelableMediaUtils() {
    }

    @NonNull
    public static ParcelableMedia[] fromEntities(@Nullable final EntitySupport entities) {
        if (entities == null) return new ParcelableMedia[0];
        final List<ParcelableMedia> list = new ArrayList<>();
        final MediaEntity[] mediaEntities;
        if (entities instanceof ExtendedEntitySupport) {
            final ExtendedEntitySupport extendedEntities = (ExtendedEntitySupport) entities;
            final MediaEntity[] extendedMediaEntities = extendedEntities.getExtendedMediaEntities();
            mediaEntities = extendedMediaEntities != null ? extendedMediaEntities : entities.getMediaEntities();
        } else {
            mediaEntities = entities.getMediaEntities();
        }
        if (mediaEntities != null) {
            for (final MediaEntity media : mediaEntities) {
                final String mediaURL = InternalTwitterContentUtils.getMediaUrl(media);
                if (mediaURL != null) {
                    list.add(ParcelableMediaUtils.fromMediaEntity(media));
                }
            }
        }
        final UrlEntity[] urlEntities = entities.getUrlEntities();
        if (urlEntities != null) {
            for (final UrlEntity url : urlEntities) {
                final String expanded = url.getExpandedUrl();
                final ParcelableMedia media = PreviewMediaExtractor.fromLink(expanded);
                if (media != null) {
                    media.start = url.getStart();
                    media.end = url.getEnd();
                    list.add(media);
                }
            }
        }
        return list.toArray(new ParcelableMedia[list.size()]);
    }

    private static ParcelableMedia fromMediaEntity(MediaEntity entity) {
        final ParcelableMedia media = new ParcelableMedia();
        final String mediaUrl = InternalTwitterContentUtils.getMediaUrl(entity);
        media.url = mediaUrl;
        media.media_url = mediaUrl;
        media.preview_url = mediaUrl;
        media.page_url = entity.getExpandedUrl();
        media.start = entity.getStart();
        media.end = entity.getEnd();
        media.type = ParcelableMediaUtils.getTypeInt(entity.getType());
        media.alt_text = entity.getAltText();
        final MediaEntity.Size size = entity.getSizes().get(MediaEntity.ScaleType.LARGE);
        if (size != null) {
            media.width = size.getWidth();
            media.height = size.getHeight();
        } else {
            media.width = 0;
            media.height = 0;
        }
        media.video_info = ParcelableMedia.VideoInfo.fromMediaEntityInfo(entity.getVideoInfo());
        return media;
    }

    @Nullable
    public static ParcelableMedia[] fromMediaUpdates(@Nullable final ParcelableMediaUpdate[] mediaUpdates) {
        if (mediaUpdates == null) return null;
        final ParcelableMedia[] media = new ParcelableMedia[mediaUpdates.length];
        for (int i = 0, j = mediaUpdates.length; i < j; i++) {
            final ParcelableMediaUpdate mediaUpdate = mediaUpdates[i];
            media[i] = new ParcelableMedia(mediaUpdate);
        }
        return media;
    }

    @Nullable
    public static ParcelableMedia[] fromStatus(@NonNull final Status status) {
        final ParcelableMedia[] fromEntities = fromEntities(status);
        final ParcelableMedia[] fromAttachments = fromAttachments(status);
        final ParcelableMedia[] fromCard = fromCard(status.getCard(), status.getUrlEntities(),
                status.getMediaEntities(), status.getExtendedMediaEntities());
        final ParcelableMedia[] fromPhoto = fromPhoto(status);
        final ParcelableMedia[] merged = new ParcelableMedia[fromCard.length +
                fromAttachments.length + fromEntities.length + fromPhoto.length];
        TwidereArrayUtils.mergeArray(merged, fromEntities, fromAttachments, fromCard, fromPhoto);
        return merged;
    }

    @NonNull
    private static ParcelableMedia[] fromPhoto(Status status) {
        Photo photo = status.getPhoto();
        if (photo == null) return new ParcelableMedia[0];
        final ParcelableMedia media = new ParcelableMedia();
        media.type = ParcelableMedia.Type.IMAGE;
        media.url = photo.getUrl();
        media.page_url = photo.getUrl();
        media.media_url = photo.getLargeUrl();
        media.preview_url = photo.getImageUrl();
        return new ParcelableMedia[]{media};
    }

    @NonNull
    private static ParcelableMedia[] fromAttachments(@NonNull Status status) {
        final Attachment[] attachments = status.getAttachments();
        if (attachments == null) return new ParcelableMedia[0];
        final ParcelableMedia[] temp = new ParcelableMedia[attachments.length];
        final String externalUrl = status.getExternalUrl();
        int i = 0;
        for (Attachment attachment : attachments) {
            final String mimetype = attachment.getMimetype();
            if (mimetype != null && mimetype.startsWith("image/")) {
                ParcelableMedia media = new ParcelableMedia();
                media.type = ParcelableMedia.Type.IMAGE;
                media.width = attachment.getWidth();
                media.height = attachment.getHeight();
                media.url = TextUtils.isEmpty(externalUrl) ? attachment.getUrl() : externalUrl;
                media.page_url = TextUtils.isEmpty(externalUrl) ? attachment.getUrl() : externalUrl;
                media.media_url = attachment.getUrl();
                media.preview_url = attachment.getLargeThumbUrl();
                temp[i++] = media;
            }
        }
        return ArrayUtils.subarray(temp, 0, i);
    }

    @NonNull
    private static ParcelableMedia[] fromCard(@Nullable CardEntity card,
                                              @Nullable UrlEntity[] urlEntities,
                                              @Nullable MediaEntity[] mediaEntities,
                                              @Nullable MediaEntity[] extendedMediaEntities) {
        if (card == null) return new ParcelableMedia[0];
        final String name = card.getName();
        if ("animated_gif".equals(name) || "player".equals(name)) {
            final ParcelableMedia media = new ParcelableMedia();
            final CardEntity.BindingValue playerStreamUrl = card.getBindingValue("player_stream_url");
            media.card = ParcelableCardEntityUtils.fromCardEntity(card, null);
            CardEntity.StringValue appUrlResolved = (CardEntity.StringValue) card.getBindingValue("app_url_resolved");
            media.url = checkUrl(appUrlResolved) ? appUrlResolved.getValue() : card.getUrl();
            if ("animated_gif".equals(name)) {
                media.media_url = ((CardEntity.StringValue) playerStreamUrl).getValue();
                media.type = ParcelableMedia.Type.CARD_ANIMATED_GIF;
            } else if (playerStreamUrl instanceof CardEntity.StringValue) {
                media.media_url = ((CardEntity.StringValue) playerStreamUrl).getValue();
                media.type = ParcelableMedia.Type.VIDEO;
            } else {
                CardEntity.StringValue playerUrl = (CardEntity.StringValue) card.getBindingValue("player_url");
                if (playerUrl != null) {
                    media.media_url = playerUrl.getValue();
                }
                media.type = ParcelableMedia.Type.EXTERNAL_PLAYER;
            }
            final CardEntity.BindingValue playerImage = card.getBindingValue("player_image");
            if (playerImage instanceof CardEntity.ImageValue) {
                media.preview_url = ((CardEntity.ImageValue) playerImage).getUrl();
                media.width = ((CardEntity.ImageValue) playerImage).getWidth();
                media.height = ((CardEntity.ImageValue) playerImage).getHeight();
            }
            final CardEntity.BindingValue playerWidth = card.getBindingValue("player_width");
            final CardEntity.BindingValue playerHeight = card.getBindingValue("player_height");
            if (playerWidth instanceof CardEntity.StringValue && playerHeight instanceof CardEntity.StringValue) {
                media.width = NumberUtils.toInt(((CardEntity.StringValue) playerWidth).getValue(), -1);
                media.height = NumberUtils.toInt(((CardEntity.StringValue) playerHeight).getValue(), -1);
            }
            writeLinkInfo(media, urlEntities, mediaEntities, extendedMediaEntities);
            return new ParcelableMedia[]{media};
        } else if ("summary_large_image".equals(name)) {
            final CardEntity.BindingValue photoImageFullSize = card.getBindingValue("photo_image_full_size");
            if (!(photoImageFullSize instanceof CardEntity.ImageValue))
                return new ParcelableMedia[0];

            final ParcelableMedia media = new ParcelableMedia();
            media.url = card.getUrl();
            media.card = ParcelableCardEntityUtils.fromCardEntity(card, null);
            media.type = ParcelableMedia.Type.IMAGE;
            media.media_url = ((CardEntity.ImageValue) photoImageFullSize).getUrl();
            media.width = ((CardEntity.ImageValue) photoImageFullSize).getWidth();
            media.height = ((CardEntity.ImageValue) photoImageFullSize).getHeight();
            media.open_browser = true;
            final CardEntity.BindingValue summaryPhotoImage = card.getBindingValue("summary_photo_image");
            if (summaryPhotoImage instanceof CardEntity.ImageValue) {
                media.preview_url = ((CardEntity.ImageValue) summaryPhotoImage).getUrl();
            }
            if (urlEntities != null) {
                for (UrlEntity entity : urlEntities) {
                    if (entity.getUrl().equals(media.url)) {
                        media.start = entity.getStart();
                        media.end = entity.getEnd();
                        break;
                    }
                }
            }
            return new ParcelableMedia[]{media};
        }
        return new ParcelableMedia[0];
    }

    private static void writeLinkInfo(ParcelableMedia media, UrlEntity[]... entities) {
        if (entities == null) return;
        for (UrlEntity[] array : entities) {
            if (array == null) continue;
            for (UrlEntity entity : array) {
                if (entity.getUrl().equals(media.url)) {
                    media.page_url = entity.getExpandedUrl();
                    if (media.page_url == null) {
                        media.page_url = media.url;
                    }
                    media.start = entity.getStart();
                    media.end = entity.getEnd();
                    break;
                }
            }
        }
    }

    private static boolean checkUrl(CardEntity.StringValue value) {
        if (value == null) return false;
        final String valueString = value.getValue();
        return valueString != null && (valueString.startsWith("http://")
                || valueString.startsWith("https://"));
    }

    public static int getTypeInt(String type) {
        switch (type) {
            case MediaEntity.Type.PHOTO:
                return ParcelableMedia.Type.IMAGE;
            case MediaEntity.Type.VIDEO:
                return ParcelableMedia.Type.VIDEO;
            case MediaEntity.Type.ANIMATED_GIF:
                return ParcelableMedia.Type.ANIMATED_GIF;
        }
        return ParcelableMedia.Type.UNKNOWN;
    }

    public static ParcelableMedia image(final String url) {
        ParcelableMedia media = new ParcelableMedia();
        media.type = ParcelableMedia.Type.IMAGE;
        media.url = url;
        media.media_url = url;
        media.preview_url = url;
        return media;
    }

    public static boolean hasPlayIcon(@ParcelableMedia.Type int type) {
        switch (type) {
            case ParcelableMedia.Type.VIDEO:
            case ParcelableMedia.Type.ANIMATED_GIF:
            case ParcelableMedia.Type.CARD_ANIMATED_GIF:
            case ParcelableMedia.Type.EXTERNAL_PLAYER:
                return true;
        }
        return false;
    }

    public static ParcelableMedia findByUrl(@Nullable ParcelableMedia[] media, @Nullable String url) {
        if (media == null || url == null) return null;
        for (ParcelableMedia item : media) {
            if (url.equals(item.url)) return item;
        }
        return null;
    }

    @Nullable
    public static ParcelableMedia[] getPrimaryMedia(ParcelableStatus status) {
        if (status.is_quote && ArrayUtils.isEmpty(status.media)) {
            return status.quoted_media;
        } else {
            return status.media;
        }
    }

    public static ParcelableMedia[] getAllMedia(ParcelableStatus status) {
        ParcelableMedia[] result = new ParcelableMedia[TwidereArrayUtils.arraysLength(status.media,
                status.quoted_media)];
        TwidereArrayUtils.mergeArray(result, status.media, status.quoted_media);
        return result;
    }
}
