package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;
import org.mariotaku.twidere.util.MediaPreviewUtils;
import org.mariotaku.twidere.util.SimpleValueSerializer;
import org.mariotaku.twidere.util.SimpleValueSerializer.Reader;
import org.mariotaku.twidere.util.SimpleValueSerializer.SerializationException;
import org.mariotaku.twidere.util.SimpleValueSerializer.SimpleValueSerializable;
import org.mariotaku.twidere.util.SimpleValueSerializer.Writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import twitter4j.EntitySupport;
import twitter4j.ExtendedEntitySupport;
import twitter4j.MediaEntity;
import twitter4j.MediaEntity.Size;
import twitter4j.MediaEntity.Type;
import twitter4j.URLEntity;

@SuppressWarnings("unused")
public class ParcelableMedia implements Parcelable, JSONParcelable, SimpleValueSerializable {

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;

    public static final Parcelable.Creator<ParcelableMedia> CREATOR = new Parcelable.Creator<ParcelableMedia>() {
        @Override
        public ParcelableMedia createFromParcel(final Parcel in) {
            return new ParcelableMedia(in);
        }

        @Override
        public ParcelableMedia[] newArray(final int size) {
            return new ParcelableMedia[size];
        }
    };

    public static final JSONParcelable.Creator<ParcelableMedia> JSON_CREATOR = new JSONParcelable.Creator<ParcelableMedia>() {
        @Override
        public ParcelableMedia createFromParcel(final JSONParcel in) {
            return new ParcelableMedia(in);
        }

        @Override
        public ParcelableMedia[] newArray(final int size) {
            return new ParcelableMedia[size];
        }
    };

    public static final SimpleValueSerializer.Creator<ParcelableMedia> SIMPLE_CREATOR = new SimpleValueSerializer.Creator<ParcelableMedia>() {
        @Override
        public ParcelableMedia create(final SimpleValueSerializer.Reader reader) throws SerializationException {
            return new ParcelableMedia(reader);
        }

        @Override
        public ParcelableMedia[] newArray(final int size) {
            return new ParcelableMedia[size];
        }
    };

    @NonNull
    public String media_url;

    @Nullable
    public String page_url;
    public int start, end, type;
    public int width, height;

    public VideoInfo video_info;


    public ParcelableMedia() {

    }

    public ParcelableMedia(final JSONParcel in) {
        media_url = in.readString("media_url");
        page_url = in.readString("page_url");
        start = in.readInt("start");
        end = in.readInt("end");
        type = in.readInt("type");
        width = in.readInt("width");
        height = in.readInt("height");
    }

    public ParcelableMedia(final MediaEntity entity) {
        page_url = entity.getMediaURL();
        media_url = entity.getMediaURL();
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
        type = update.type;
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

    private static int getTypeInt(Type type) {
        switch (type) {
            case PHOTO:
                return TYPE_IMAGE;
            case VIDEO:
                return TYPE_VIDEO;
        }
        return TYPE_UNKNOWN;
    }

    public ParcelableMedia(final Parcel in) {
        page_url = in.readString();
        media_url = in.readString();
        start = in.readInt();
        end = in.readInt();
        type = in.readInt();
        width = in.readInt();
        height = in.readInt();
        video_info = in.readParcelable(VideoInfo.class.getClassLoader());
    }

    private ParcelableMedia(@NonNull final String media_url, @Nullable final String page_url,
                            final int start, final int end, final int type) {
        this.page_url = page_url;
        this.media_url = media_url;
        this.start = start;
        this.end = end;
        this.type = type;
        this.width = 0;
        this.height = 0;
    }

    public ParcelableMedia(Reader reader) throws SerializationException {
        while (reader.hasKeyValue()) {
            switch (reader.nextKey()) {
                case "media_url": {
                    media_url = reader.nextString();
                    break;
                }
                case "page_url": {
                    page_url = reader.nextString();
                    break;
                }
                case "start": {
                    start = reader.nextInt();
                    break;
                }
                case "end": {
                    end = reader.nextInt();
                    break;
                }
                case "type": {
                    type = reader.nextInt();
                    break;
                }
                case "width": {
                    width = reader.nextInt();
                    break;
                }
                case "height": {
                    height = reader.nextInt();
                    break;
                }
                default: {
                    reader.skipValue();
                    break;
                }
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableMedia that = (ParcelableMedia) o;

        if (end != that.end) return false;
        if (start != that.start) return false;
        if (type != that.type) return false;
        if (!media_url.equals(that.media_url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = media_url.hashCode();
        result = 31 * result + start;
        result = 31 * result + end;
        result = 31 * result + type;
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
    public void write(Writer writer) {
        writer.write("media_url", media_url);
        writer.write("page_url", page_url);
        writer.write("start", String.valueOf(start));
        writer.write("end", String.valueOf(end));
        writer.write("type", String.valueOf(type));
        writer.write("width", String.valueOf(width));
        writer.write("height", String.valueOf(height));
    }

    @Override
    public void writeToParcel(final JSONParcel out) {
        out.writeString("media_url", media_url);
        out.writeString("page_url", page_url);
        out.writeInt("start", start);
        out.writeInt("end", end);
        out.writeInt("type", type);
        out.writeInt("width", width);
        out.writeInt("height", height);
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(page_url);
        dest.writeString(media_url);
        dest.writeInt(start);
        dest.writeInt(end);
        dest.writeInt(type);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeParcelable(video_info, flags);
    }

    public static ParcelableMedia[] fromEntities(final EntitySupport entities) {
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
                final String mediaURL = media.getMediaURL();
                if (mediaURL != null) {
                    list.add(new ParcelableMedia(media));
                }
            }
        }
        final URLEntity[] urlEntities = entities.getURLEntities();
        if (urlEntities != null) {
            for (final URLEntity url : urlEntities) {
                final String expanded = url.getExpandedURL();
                final String media_url = MediaPreviewUtils.getSupportedLink(expanded);
                if (expanded != null && media_url != null) {
                    list.add(new ParcelableMedia(media_url, expanded, url.getStart(), url.getEnd(), TYPE_IMAGE));
                }
            }
        }
        if (list.isEmpty()) return null;
        return list.toArray(new ParcelableMedia[list.size()]);
    }


    public static class VideoInfo implements Parcelable {

        public Variant[] variants;
        public long[] aspect_ratio;
        public long duration;

        public VideoInfo(Reader source) {

        }

        public VideoInfo(MediaEntity.VideoInfo videoInfo) {
            variants = Variant.fromMediaEntityVariants(videoInfo.getVariants());
            aspect_ratio = videoInfo.getAspectRatio();
            duration = videoInfo.getDuration();
        }

        public static VideoInfo fromMediaEntityInfo(MediaEntity.VideoInfo videoInfo) {
            if (videoInfo == null) return null;
            return new VideoInfo(videoInfo);
        }

        @Override
        public String toString() {
            return "VideoInfo{" +
                    "variants=" + Arrays.toString(variants) +
                    ", aspect_ratio=" + Arrays.toString(aspect_ratio) +
                    ", duration=" + duration +
                    '}';
        }

        public static class Variant implements Parcelable {
            public Variant(MediaEntity.VideoInfo.Variant entityVariant) {
                content_type = entityVariant.getContentType();
                url = entityVariant.getUrl();
                bitrate = entityVariant.getBitrate();
            }

            @Override
            public String toString() {
                return "Variant{" +
                        "content_type='" + content_type + '\'' +
                        ", url='" + url + '\'' +
                        ", bitrate=" + bitrate +
                        '}';
            }

            public final String content_type;
            public final String url;
            public final long bitrate;

            public Variant(JSONParcel source) {
                content_type = source.readString("content_type");
                url = source.readString("url");
                bitrate = source.readLong("bitrate");
            }

            @Override
            public int describeContents() {
                return 0;
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
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this.content_type);
                dest.writeString(this.url);
                dest.writeLong(this.bitrate);
            }


            private Variant(Parcel in) {
                this.content_type = in.readString();
                this.url = in.readString();
                this.bitrate = in.readLong();
            }

            public static final Parcelable.Creator<Variant> CREATOR = new Parcelable.Creator<Variant>() {
                public Variant createFromParcel(Parcel source) {
                    return new Variant(source);
                }

                public Variant[] newArray(int size) {
                    return new Variant[size];
                }
            };

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeTypedArray(variants, flags);
            dest.writeLongArray(aspect_ratio);
            dest.writeLong(duration);
        }


        private VideoInfo(Parcel in) {
            variants = in.createTypedArray(Variant.CREATOR);
            aspect_ratio = in.createLongArray();
            duration = in.readLong();
        }

        public static final Parcelable.Creator<VideoInfo> CREATOR = new Parcelable.Creator<VideoInfo>() {
            public VideoInfo createFromParcel(Parcel source) {
                return new VideoInfo(source);
            }

            public VideoInfo[] newArray(int size) {
                return new VideoInfo[size];
            }
        };
    }

    public static ParcelableMedia newImage(final String media_url, final String url) {
        return new ParcelableMedia(media_url, url, 0, 0, TYPE_IMAGE);
    }

    public static class MediaSize {

        public static final int LARGE = 1;
        public static final int MEDIUM = 2;
        public static final int SMALL = 3;
        public static final int THUMB = 4;


    }

}
