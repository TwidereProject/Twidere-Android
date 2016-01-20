package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.math.NumberUtils;
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

    @Nullable
    public static ParcelableMedia[] fromEntities(@Nullable final EntitySupport entities) {
        if (entities == null) return null;
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
        if (list.isEmpty()) return null;
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
    public static ParcelableMedia[] fromStatus(final Status status) {
        final ParcelableMedia[] fromEntities = fromEntities(status);
        final ParcelableMedia[] fromCard = fromCard(status.getCard(), status.getUrlEntities());
        if (fromEntities == null) {
            return fromCard;
        } else if (fromCard == null) {
            return fromEntities;
        }
        final ParcelableMedia[] merged = new ParcelableMedia[fromCard.length + fromEntities.length];
        TwidereArrayUtils.mergeArray(merged, fromEntities, fromCard);
        return merged;
    }

    @Nullable
    private static ParcelableMedia[] fromCard(@Nullable CardEntity card, @Nullable UrlEntity[] entities) {
        if (card == null) return null;
        final String name = card.getName();
        if ("animated_gif".equals(name) || "player".equals(name)) {
            final ParcelableMedia media = new ParcelableMedia();
            final BindingValue playerStreamUrl = card.getBindingValue("player_stream_url");
            media.card = ParcelableCardEntity.fromCardEntity(card, -1);
            media.url = card.getUrl();
            if ("animated_gif".equals(name)) {
                media.media_url = ((StringValue) playerStreamUrl).getValue();
                media.type = Type.TYPE_CARD_ANIMATED_GIF;
            } else if (playerStreamUrl instanceof StringValue) {
                media.media_url = ((StringValue) playerStreamUrl).getValue();
                media.type = Type.TYPE_VIDEO;
            } else {
                media.type = Type.TYPE_EXTERNAL_PLAYER;
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
                return null;

            final ParcelableMedia media = new ParcelableMedia();
            media.url = card.getUrl();
            media.card = ParcelableCardEntity.fromCardEntity(card, -1);
            media.type = Type.TYPE_IMAGE;
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
        return null;
    }

    private static int getTypeInt(String type) {
        switch (type) {
            case MediaEntity.Type.PHOTO:
                return Type.TYPE_IMAGE;
            case MediaEntity.Type.VIDEO:
                return Type.TYPE_VIDEO;
            case MediaEntity.Type.ANIMATED_GIF:
                return Type.TYPE_ANIMATED_GIF;
        }
        return Type.TYPE_UNKNOWN;
    }


    public static ParcelableMedia image(final String url) {
        ParcelableMedia media = new ParcelableMedia();
        media.type = Type.TYPE_VARIABLE_TYPE;
        media.url = url;
        media.media_url = url;
        media.preview_url = url;
        return media;
    }

    public static ParcelableMedia variableType(@NonNull String link) {
        ParcelableMedia media = new ParcelableMedia();
        media.type = Type.TYPE_VARIABLE_TYPE;
        media.url = link;
        return media;
    }

    @OnJsonParseComplete
    void onParseComplete() {
        if (this.page_url != null) {
            this.url = this.page_url;
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("media_url", media_url)
                .append("page_url", url)
                .append("preview_url", preview_url)
                .append("start", start)
                .append("end", end)
                .append("type", type)
                .append("width", width)
                .append("height", height)
                .append("video_info", video_info)
                .append("card", card)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ParcelableMedia media = (ParcelableMedia) o;

        return new EqualsBuilder()
                .append(start, media.start)
                .append(end, media.end)
                .append(type, media.type)
                .append(width, media.width)
                .append(height, media.height)
                .append(media_url, media.media_url)
                .append(url, media.url)
                .append(preview_url, media.preview_url)
                .append(video_info, media.video_info)
                .append(card, media.card)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(media_url)
                .append(url)
                .append(preview_url)
                .append(start)
                .append(end)
                .append(type)
                .append(width)
                .append(height)
                .append(video_info)
                .append(card)
                .toHashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableMediaParcelablePlease.writeToParcel(this, dest, flags);
    }

    @IntDef({Type.TYPE_UNKNOWN, Type.TYPE_IMAGE, Type.TYPE_VIDEO, Type.TYPE_ANIMATED_GIF,
            Type.TYPE_CARD_ANIMATED_GIF, Type.TYPE_EXTERNAL_PLAYER, Type.TYPE_VARIABLE_TYPE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {

        @Type
        int TYPE_UNKNOWN = 0;
        @Type
        int TYPE_IMAGE = 1;
        @Type
        int TYPE_VIDEO = 2;
        @Type
        int TYPE_ANIMATED_GIF = 3;
        @Type
        int TYPE_CARD_ANIMATED_GIF = 4;
        @Type
        int TYPE_EXTERNAL_PLAYER = 5;
        @Type
        int TYPE_VARIABLE_TYPE = 6;
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
        public String toString() {
            return new ToStringBuilder(this)
                    .append("variants", variants)
                    .append("duration", duration)
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            VideoInfo videoInfo = (VideoInfo) o;

            return new EqualsBuilder()
                    .append(duration, videoInfo.duration)
                    .append(variants, videoInfo.variants)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(variants)
                    .append(duration)
                    .toHashCode();
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

                return new EqualsBuilder()
                        .append(bitrate, variant.bitrate)
                        .append(content_type, variant.content_type)
                        .append(url, variant.url)
                        .isEquals();
            }

            @Override
            public int hashCode() {
                return new HashCodeBuilder(17, 37)
                        .append(content_type)
                        .append(url)
                        .append(bitrate)
                        .toHashCode();
            }

            @Override
            public String toString() {
                return new ToStringBuilder(this)
                        .append("content_type", content_type)
                        .append("url", url)
                        .append("bitrate", bitrate)
                        .toString();
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
