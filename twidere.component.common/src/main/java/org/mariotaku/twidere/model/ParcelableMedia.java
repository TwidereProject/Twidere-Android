package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.twidere.api.twitter.model.CardEntity;
import org.mariotaku.twidere.api.twitter.model.CardEntity.BindingValue;
import org.mariotaku.twidere.api.twitter.model.CardEntity.ImageValue;
import org.mariotaku.twidere.api.twitter.model.CardEntity.StringValue;
import org.mariotaku.twidere.api.twitter.model.EntitySupport;
import org.mariotaku.twidere.api.twitter.model.ExtendedEntitySupport;
import org.mariotaku.twidere.api.twitter.model.MediaEntity;
import org.mariotaku.twidere.api.twitter.model.MediaEntity.Size;
import org.mariotaku.twidere.api.twitter.model.MediaEntity.Type;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.UrlEntity;
import org.mariotaku.twidere.util.MediaPreviewUtils;
import org.mariotaku.twidere.util.TwidereArrayUtils;
import org.mariotaku.twidere.util.TwitterContentUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonObject
@ParcelablePlease
public class ParcelableMedia implements Parcelable {

    @MediaType
    public static final int TYPE_UNKNOWN = 0;
    @MediaType
    public static final int TYPE_IMAGE = 1;
    @MediaType
    public static final int TYPE_VIDEO = 2;
    @MediaType
    public static final int TYPE_ANIMATED_GIF = 3;
    @MediaType
    public static final int TYPE_CARD_ANIMATED_GIF = 4;
    @MediaType
    public static final int TYPE_EXTERNAL_PLAYER = 5;
    @JsonField(name = "media_url")
    @ParcelableThisPlease
    public String media_url;
    @Nullable
    @JsonField(name = "page_url")
    @ParcelableThisPlease
    public String page_url;
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
    @MediaType
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
    @ParcelableNoThanks
    public ParcelableCardEntity card;

    public ParcelableMedia() {

    }


    public ParcelableMedia(final MediaEntity entity) {
        page_url = TwitterContentUtils.getMediaUrl(entity);
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
        page_url = update.uri;
        preview_url = update.uri;
        type = update.type;
    }

    private ParcelableMedia(@NonNull final String media_url, @Nullable final String page_url,
                            @Nullable final String preview_url, final int start, final int end,
                            final int type) {
        this.page_url = page_url;
        this.media_url = media_url;
        this.preview_url = preview_url;
        this.start = start;
        this.end = end;
        this.type = type;
        this.width = 0;
        this.height = 0;
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
                final String media_url = MediaPreviewUtils.getSupportedLink(expanded);
                if (expanded != null && media_url != null) {
                    final ParcelableMedia media = new ParcelableMedia();
                    media.type = TYPE_IMAGE;
                    media.page_url = expanded;
                    media.media_url = media_url;
                    media.preview_url = media_url;
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

    public static ParcelableMedia newImage(final String media_url, final String url) {
        return new ParcelableMedia(media_url, url, media_url, 0, 0, TYPE_IMAGE);
    }

    @Nullable
    private static ParcelableMedia[] fromCard(@Nullable CardEntity card, @Nullable UrlEntity[] entities) {
        if (card == null) return null;
        final String name = card.getName();
        if ("animated_gif".equals(name) || "player".equals(name)) {
            final ParcelableMedia media = new ParcelableMedia();
            final BindingValue playerStreamUrl = card.getBindingValue("player_stream_url");
            media.card = ParcelableCardEntity.fromCardEntity(card, -1);
            if ("animated_gif".equals(name)) {
                media.media_url = ((StringValue) playerStreamUrl).getValue();
                media.type = ParcelableMedia.TYPE_CARD_ANIMATED_GIF;
            } else if (playerStreamUrl instanceof StringValue) {
                media.media_url = ((StringValue) playerStreamUrl).getValue();
                media.type = ParcelableMedia.TYPE_VIDEO;
            } else {
                media.type = ParcelableMedia.TYPE_EXTERNAL_PLAYER;
            }
            media.page_url = card.getUrl();
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
                    if (entity.getUrl().equals(media.page_url)) {
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
            media.card = ParcelableCardEntity.fromCardEntity(card, -1);
            media.type = ParcelableMedia.TYPE_IMAGE;
            media.media_url = ((ImageValue) photoImageFullSize).getUrl();
            media.width = ((ImageValue) photoImageFullSize).getWidth();
            media.height = ((ImageValue) photoImageFullSize).getHeight();
            media.page_url = card.getUrl();
            final BindingValue summaryPhotoImage = card.getBindingValue("summary_photo_image");
            if (summaryPhotoImage instanceof ImageValue) {
                media.preview_url = ((ImageValue) summaryPhotoImage).getUrl();
            }
            if (entities != null) {
                for (UrlEntity entity : entities) {
                    if (entity.getUrl().equals(media.page_url)) {
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

    private static int getTypeInt(Type type) {
        switch (type) {
            case PHOTO:
                return TYPE_IMAGE;
            case VIDEO:
                return TYPE_VIDEO;
            case ANIMATED_GIF:
                return TYPE_ANIMATED_GIF;
        }
        return TYPE_UNKNOWN;
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
        if (media_url != null ? !media_url.equals(media.media_url) : media.media_url != null)
            return false;
        if (page_url != null ? !page_url.equals(media.page_url) : media.page_url != null)
            return false;
        if (preview_url != null ? !preview_url.equals(media.preview_url) : media.preview_url != null)
            return false;
        if (video_info != null ? !video_info.equals(media.video_info) : media.video_info != null)
            return false;
        return !(card != null ? !card.equals(media.card) : media.card != null);

    }

    @Override
    public int hashCode() {
        int result = media_url != null ? media_url.hashCode() : 0;
        result = 31 * result + (page_url != null ? page_url.hashCode() : 0);
        result = 31 * result + (preview_url != null ? preview_url.hashCode() : 0);
        result = 31 * result + start;
        result = 31 * result + end;
        result = 31 * result + type;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + (video_info != null ? video_info.hashCode() : 0);
        result = 31 * result + (card != null ? card.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ParcelableMedia{" +
                "media_url='" + media_url + '\'' +
                ", page_url='" + page_url + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", type=" + type +
                ", width=" + width +
                ", height=" + height +
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

    @IntDef({TYPE_UNKNOWN, TYPE_IMAGE, TYPE_VIDEO, TYPE_ANIMATED_GIF, TYPE_CARD_ANIMATED_GIF,
            TYPE_EXTERNAL_PLAYER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MediaType {

    }

    @JsonObject
    public static class VideoInfo implements Parcelable {

        public static final Parcelable.Creator<VideoInfo> CREATOR = new Parcelable.Creator<VideoInfo>() {
            @Override
            public VideoInfo createFromParcel(Parcel source) {
                return new VideoInfo(source);
            }

            @Override
            public VideoInfo[] newArray(int size) {
                return new VideoInfo[size];
            }
        };
        @JsonField(name = "variants")
        public Variant[] variants;
        @JsonField(name = "duration")
        public long duration;

        public VideoInfo() {

        }

        public VideoInfo(MediaEntity.VideoInfo videoInfo) {
            variants = Variant.fromMediaEntityVariants(videoInfo.getVariants());
            duration = videoInfo.getDuration();
        }

        private VideoInfo(Parcel in) {
            variants = in.createTypedArray(Variant.CREATOR);
            duration = in.readLong();
        }

        public static VideoInfo fromMediaEntityInfo(MediaEntity.VideoInfo videoInfo) {
            if (videoInfo == null) return null;
            return new VideoInfo(videoInfo);
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
            dest.writeTypedArray(variants, flags);
            dest.writeLong(duration);
        }

        @JsonObject
        public static class Variant implements Parcelable {
            public static final Parcelable.Creator<Variant> CREATOR = new Parcelable.Creator<Variant>() {
                @Override
                public Variant createFromParcel(Parcel source) {
                    return new Variant(source);
                }

                @Override
                public Variant[] newArray(int size) {
                    return new Variant[size];
                }
            };
            @JsonField(name = "content_type")
            public String content_type;
            @JsonField(name = "url")
            public String url;
            @JsonField(name = "bitrate")
            public long bitrate;

            public Variant() {
            }

            public Variant(MediaEntity.VideoInfo.Variant entityVariant) {
                content_type = entityVariant.getContentType();
                url = entityVariant.getUrl();
                bitrate = entityVariant.getBitrate();
            }

            private Variant(Parcel in) {
                this.content_type = in.readString();
                this.url = in.readString();
                this.bitrate = in.readLong();
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
                dest.writeString(this.content_type);
                dest.writeString(this.url);
                dest.writeLong(this.bitrate);
            }

        }

    }
}
