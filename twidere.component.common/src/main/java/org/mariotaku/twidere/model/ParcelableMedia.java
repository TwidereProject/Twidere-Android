package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.twidere.api.gnusocial.model.Attachment;
import org.mariotaku.twidere.api.twitter.model.CardEntity;
import org.mariotaku.twidere.api.twitter.model.CardEntity.BindingValue;
import org.mariotaku.twidere.api.twitter.model.CardEntity.ImageValue;
import org.mariotaku.twidere.api.twitter.model.CardEntity.StringValue;
import org.mariotaku.twidere.api.twitter.model.EntitySupport;
import org.mariotaku.twidere.api.twitter.model.ExtendedEntitySupport;
import org.mariotaku.twidere.api.twitter.model.MediaEntity;
import org.mariotaku.twidere.api.twitter.model.MediaEntity.Size;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.UrlEntity;
import org.mariotaku.twidere.util.TwidereArrayUtils;
import org.mariotaku.twidere.util.TwitterContentUtils;
import org.mariotaku.twidere.util.media.preview.PreviewMediaExtractor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonObject
@ParcelablePlease
public class ParcelableMedia implements Parcelable {
    @SuppressWarnings("NullableProblems")
    @NonNull
    @JsonField(name = "url")
    @ParcelableThisPlease
    public String url;
    @Nullable
    @JsonField(name = "media_url")
    @ParcelableThisPlease
    public String media_url;
    @Nullable
    @JsonField(name = "preview_url")
    @ParcelableThisPlease
    public String preview_url;
    @JsonField(name = "start")
    @ParcelableThisPlease
    public int start;
    @JsonField(name = "end")
    @ParcelableThisPlease
    public int end;
    @Type
    @JsonField(name = "type")
    @ParcelableThisPlease
    public int type;
    @JsonField(name = "width")
    @ParcelableThisPlease
    public int width;
    @JsonField(name = "height")
    @ParcelableThisPlease
    public int height;
    @JsonField(name = "video_info")
    @ParcelableThisPlease
    public VideoInfo video_info;
    @ParcelableNoThanks
    public ParcelableCardEntity card;
    @JsonField(name = "page_url")
    String page_url;
    public static final Creator<ParcelableMedia> CREATOR = new Creator<ParcelableMedia>() {
        public ParcelableMedia createFromParcel(Parcel source) {
            ParcelableMedia target = new ParcelableMedia();
            ParcelableMediaParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableMedia[] newArray(int size) {
            return new ParcelableMedia[size];
        }
    };

    public ParcelableMedia() {

    }


    public ParcelableMedia(final MediaEntity entity) {
        url = TwitterContentUtils.getMediaUrl(entity);
        media_url = TwitterContentUtils.getMediaUrl(entity);
        preview_url = TwitterContentUtils.getMediaUrl(entity);
        start = entity.getStart();
        end = entity.getEnd();
        type = getTypeInt(entity.getType());
        final Size size = entity.getSizes().get(Size.LARGE);
        width = size != null ? size.getWidth() : 0;
        height = size != null ? size.getHeight() : 0;
        video_info = VideoInfo.fromMediaEntityInfo(entity.getVideoInfo());
    }

    public ParcelableMedia(ParcelableMediaUpdate update) {
        media_url = update.uri;
        url = update.uri;
        preview_url = update.uri;
        type = update.type;
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
                final String mediaURL = TwitterContentUtils.getMediaUrl(media);
                if (mediaURL != null) {
                    list.add(new ParcelableMedia(media));
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
        final ParcelableMedia[] fromCard = fromCard(status.getCard(), status.getUrlEntities());
        final ParcelableMedia[] merged = new ParcelableMedia[fromCard.length +
                fromAttachments.length + fromEntities.length];
        TwidereArrayUtils.mergeArray(merged, fromEntities, fromAttachments, fromCard);
        return merged;
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
                media.type = Type.IMAGE;
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
    private static ParcelableMedia[] fromCard(@Nullable CardEntity card, @Nullable UrlEntity[] entities) {
        if (card == null) return new ParcelableMedia[0];
        final String name = card.getName();
        if ("animated_gif".equals(name) || "player".equals(name)) {
            final ParcelableMedia media = new ParcelableMedia();
            final BindingValue playerStreamUrl = card.getBindingValue("player_stream_url");
            media.card = ParcelableCardEntity.fromCardEntity(card, -1);
            StringValue appUrlResolved = (StringValue) card.getBindingValue("app_url_resolved");
            media.url = checkUrl(appUrlResolved) ? appUrlResolved.getValue() : card.getUrl();
            if ("animated_gif".equals(name)) {
                media.media_url = ((StringValue) playerStreamUrl).getValue();
                media.type = Type.CARD_ANIMATED_GIF;
            } else if (playerStreamUrl instanceof StringValue) {
                media.media_url = ((StringValue) playerStreamUrl).getValue();
                media.type = Type.VIDEO;
            } else {
                StringValue playerUrl = (StringValue) card.getBindingValue("player_url");
                if (playerUrl != null) {
                    media.media_url = playerUrl.getValue();
                }
                media.type = Type.EXTERNAL_PLAYER;
            }
            final BindingValue playerImage = card.getBindingValue("player_image");
            if (playerImage instanceof ImageValue) {
                media.preview_url = ((ImageValue) playerImage).getUrl();
                media.width = ((ImageValue) playerImage).getWidth();
                media.height = ((ImageValue) playerImage).getHeight();
            }
            final BindingValue playerWidth = card.getBindingValue("player_width");
            final BindingValue playerHeight = card.getBindingValue("player_height");
            if (playerWidth instanceof StringValue && playerHeight instanceof StringValue) {
                media.width = NumberUtils.toInt(((StringValue) playerWidth).getValue(), -1);
                media.height = NumberUtils.toInt(((StringValue) playerHeight).getValue(), -1);
            }
            if (entities != null) {
                for (UrlEntity entity : entities) {
                    if (entity.getUrl().equals(media.url)) {
                        media.start = entity.getStart();
                        media.end = entity.getEnd();
                        break;
                    }
                }
            }
            return new ParcelableMedia[]{media};
        } else if ("summary_large_image".equals(name)) {
            final BindingValue photoImageFullSize = card.getBindingValue("photo_image_full_size");
            if (!(photoImageFullSize instanceof ImageValue))
                return new ParcelableMedia[0];

            final ParcelableMedia media = new ParcelableMedia();
            media.url = card.getUrl();
            media.card = ParcelableCardEntity.fromCardEntity(card, -1);
            media.type = Type.IMAGE;
            media.media_url = ((ImageValue) photoImageFullSize).getUrl();
            media.width = ((ImageValue) photoImageFullSize).getWidth();
            media.height = ((ImageValue) photoImageFullSize).getHeight();
            final BindingValue summaryPhotoImage = card.getBindingValue("summary_photo_image");
            if (summaryPhotoImage instanceof ImageValue) {
                media.preview_url = ((ImageValue) summaryPhotoImage).getUrl();
            }
            if (entities != null) {
                for (UrlEntity entity : entities) {
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

    private static boolean checkUrl(StringValue value) {
        if (value == null) return false;
        final String valueString = value.getValue();
        return valueString != null && (valueString.startsWith("http://")
                || valueString.startsWith("https://"));
    }

    private static int getTypeInt(String type) {
        switch (type) {
            case MediaEntity.Type.PHOTO:
                return Type.IMAGE;
            case MediaEntity.Type.VIDEO:
                return Type.VIDEO;
            case MediaEntity.Type.ANIMATED_GIF:
                return Type.ANIMATED_GIF;
        }
        return Type.UNKNOWN;
    }


    public static ParcelableMedia image(final String url) {
        ParcelableMedia media = new ParcelableMedia();
        media.type = Type.IMAGE;
        media.url = url;
        media.media_url = url;
        media.preview_url = url;
        return media;
    }

    @OnJsonParseComplete
    void onParseComplete() {
        if (this.page_url != null) {
            this.url = this.page_url;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableMedia media = (ParcelableMedia) o;

        if (start != media.start) return false;
        if (end != media.end) return false;
        if (type != media.type) return false;
        if (width != media.width) return false;
        if (height != media.height) return false;
        if (!url.equals(media.url)) return false;
        if (media_url != null ? !media_url.equals(media.media_url) : media.media_url != null)
            return false;
        if (preview_url != null ? !preview_url.equals(media.preview_url) : media.preview_url != null)
            return false;
        if (video_info != null ? !video_info.equals(media.video_info) : media.video_info != null)
            return false;
        if (card != null ? !card.equals(media.card) : media.card != null) return false;
        return !(page_url != null ? !page_url.equals(media.page_url) : media.page_url != null);

    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + (media_url != null ? media_url.hashCode() : 0);
        result = 31 * result + (preview_url != null ? preview_url.hashCode() : 0);
        result = 31 * result + start;
        result = 31 * result + end;
        result = 31 * result + type;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + (video_info != null ? video_info.hashCode() : 0);
        result = 31 * result + (card != null ? card.hashCode() : 0);
        result = 31 * result + (page_url != null ? page_url.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ParcelableMedia{" +
                "url='" + url + '\'' +
                ", media_url='" + media_url + '\'' +
                ", preview_url='" + preview_url + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", type=" + type +
                ", width=" + width +
                ", height=" + height +
                ", video_info=" + video_info +
                ", card=" + card +
                ", page_url='" + page_url + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableMediaParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static boolean hasPlayIcon(@Type int type) {
        switch (type) {
            case Type.VIDEO:
            case Type.ANIMATED_GIF:
            case Type.CARD_ANIMATED_GIF:
            case Type.EXTERNAL_PLAYER:
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

    @IntDef({Type.UNKNOWN, Type.IMAGE, Type.VIDEO, Type.ANIMATED_GIF, Type.CARD_ANIMATED_GIF,
            Type.EXTERNAL_PLAYER, Type.VARIABLE_TYPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {

        @Type
        int UNKNOWN = 0;
        @Type
        int IMAGE = 1;
        @Type
        int VIDEO = 2;
        @Type
        int ANIMATED_GIF = 3;
        @Type
        int CARD_ANIMATED_GIF = 4;
        @Type
        int EXTERNAL_PLAYER = 5;
        @Type
        int VARIABLE_TYPE = 6;
    }

    @ParcelablePlease
    @JsonObject
    public static class VideoInfo implements Parcelable {

        @ParcelableThisPlease
        @JsonField(name = "variants")
        public Variant[] variants;
        @ParcelableThisPlease
        @JsonField(name = "duration")
        public long duration;
        public static final Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
            public VideoInfo createFromParcel(Parcel source) {
                VideoInfo target = new VideoInfo();
                ParcelableMedia$VideoInfoParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public VideoInfo[] newArray(int size) {
                return new VideoInfo[size];
            }
        };

        public VideoInfo() {

        }

        public VideoInfo(MediaEntity.VideoInfo videoInfo) {
            variants = Variant.fromMediaEntityVariants(videoInfo.getVariants());
            duration = videoInfo.getDuration();
        }

        public static VideoInfo fromMediaEntityInfo(MediaEntity.VideoInfo videoInfo) {
            if (videoInfo == null) return null;
            return new VideoInfo(videoInfo);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VideoInfo videoInfo = (VideoInfo) o;

            if (duration != videoInfo.duration) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(variants, videoInfo.variants);

        }

        @Override
        public int hashCode() {
            int result = variants != null ? Arrays.hashCode(variants) : 0;
            result = 31 * result + (int) (duration ^ (duration >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "VideoInfo{" +
                    "variants=" + Arrays.toString(variants) +
                    ", duration=" + duration +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            ParcelableMedia$VideoInfoParcelablePlease.writeToParcel(this, dest, flags);
        }

        @ParcelablePlease
        @JsonObject
        public static class Variant implements Parcelable {
            @ParcelableThisPlease
            @JsonField(name = "content_type")
            public String content_type;
            @ParcelableThisPlease
            @JsonField(name = "url")
            public String url;
            @ParcelableThisPlease
            @JsonField(name = "bitrate")
            public long bitrate;
            public static final Creator<Variant> CREATOR = new Creator<Variant>() {
                public Variant createFromParcel(Parcel source) {
                    Variant target = new Variant();
                    ParcelableMedia$VideoInfo$VariantParcelablePlease.readFromParcel(target, source);
                    return target;
                }

                public Variant[] newArray(int size) {
                    return new Variant[size];
                }
            };

            public Variant() {
            }

            public Variant(MediaEntity.VideoInfo.Variant entityVariant) {
                content_type = entityVariant.getContentType();
                url = entityVariant.getUrl();
                bitrate = entityVariant.getBitrate();
            }

            public static Variant[] fromMediaEntityVariants(MediaEntity.VideoInfo.Variant[] entityVariants) {
                if (entityVariants == null) return null;
                final Variant[] variants = new Variant[entityVariants.length];
                for (int i = 0, j = entityVariants.length; i < j; i++) {
                    variants[i] = new Variant(entityVariants[i]);
                }
                return variants;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Variant variant = (Variant) o;

                if (bitrate != variant.bitrate) return false;
                if (content_type != null ? !content_type.equals(variant.content_type) : variant.content_type != null)
                    return false;
                return !(url != null ? !url.equals(variant.url) : variant.url != null);

            }

            @Override
            public int hashCode() {
                int result = content_type != null ? content_type.hashCode() : 0;
                result = 31 * result + (url != null ? url.hashCode() : 0);
                result = 31 * result + (int) (bitrate ^ (bitrate >>> 32));
                return result;
            }

            @Override
            public String toString() {
                return "Variant{" +
                        "content_type='" + content_type + '\'' +
                        ", url='" + url + '\'' +
                        ", bitrate=" + bitrate +
                        '}';
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                ParcelableMedia$VideoInfo$VariantParcelablePlease.writeToParcel(this, dest, flags);
            }
        }
    }
}
