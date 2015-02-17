package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.util.MediaPreviewUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.SimpleValueSerializer;
import org.mariotaku.twidere.util.SimpleValueSerializer.Reader;
import org.mariotaku.twidere.util.SimpleValueSerializer.SerializationException;
import org.mariotaku.twidere.util.SimpleValueSerializer.SimpleValueSerializable;
import org.mariotaku.twidere.util.SimpleValueSerializer.Writer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import twitter4j.EntitySupport;
import twitter4j.ExtendedEntitySupport;
import twitter4j.MediaEntity;
import twitter4j.MediaEntity.Size;
import twitter4j.URLEntity;

@SuppressWarnings("unused")
public class ParcelableMedia implements Parcelable, JSONParcelable, SimpleValueSerializable {

    public static final int TYPE_IMAGE = 1;

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
        page_url = ParseUtils.parseString(entity.getMediaURL());
        media_url = ParseUtils.parseString(entity.getMediaURL());
        start = entity.getStart();
        end = entity.getEnd();
        type = TYPE_IMAGE;
        final Size size = entity.getSizes().get(Size.LARGE);
        width = size != null ? size.getWidth() : 0;
        height = size != null ? size.getHeight() : 0;
    }

    public ParcelableMedia(final Parcel in) {
        page_url = in.readString();
        media_url = in.readString();
        start = in.readInt();
        end = in.readInt();
        type = in.readInt();
        width = in.readInt();
        height = in.readInt();
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
                final URL mediaURL = media.getMediaURL();
                if (mediaURL != null) {
                    list.add(new ParcelableMedia(media));
                }
            }
        }
        final URLEntity[] urlEntities = entities.getURLEntities();
        if (urlEntities != null) {
            for (final URLEntity url : urlEntities) {
                final String expanded = ParseUtils.parseString(url.getExpandedURL());
                final String media_url = MediaPreviewUtils.getSupportedLink(expanded);
                if (expanded != null && media_url != null) {
                    list.add(new ParcelableMedia(media_url, expanded, url.getStart(), url.getEnd(), TYPE_IMAGE));
                }
            }
        }
        if (list.isEmpty()) return null;
        return list.toArray(new ParcelableMedia[list.size()]);
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
