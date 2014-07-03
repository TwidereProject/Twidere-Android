package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;
import org.mariotaku.jsonserializer.JSONSerializer;
import org.mariotaku.twidere.util.MediaPreviewUtils;
import org.mariotaku.twidere.util.ParseUtils;

import twitter4j.EntitySupport;
import twitter4j.MediaEntity;
import twitter4j.URLEntity;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ParcelableMedia implements Parcelable, JSONParcelable {

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

	public final String url, media_url;
	public final int start, end, type;

	public ParcelableMedia(final JSONParcel in) {
		url = in.readString("url");
		media_url = in.readString("media_url");
		start = in.readInt("start");
		end = in.readInt("end");
		type = in.readInt("type");
	}

	public ParcelableMedia(final MediaEntity entity) {
		url = ParseUtils.parseString(entity.getMediaURL());
		media_url = ParseUtils.parseString(entity.getMediaURL());
		start = entity.getStart();
		end = entity.getEnd();
		type = TYPE_IMAGE;
	}

	public ParcelableMedia(final Parcel in) {
		url = in.readString();
		media_url = in.readString();
		start = in.readInt();
		end = in.readInt();
		type = in.readInt();
	}

	private ParcelableMedia(final String url, final String media_url, final int start, final int end, final int type) {
		this.url = url;
		this.media_url = media_url;
		this.start = start;
		this.end = end;
		this.type = type;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final JSONParcel out) {
		out.writeString("url", url);
		out.writeString("media_url", media_url);
		out.writeInt("start", start);
		out.writeInt("end", end);
		out.writeInt("type", type);
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeString(url);
		dest.writeString(media_url);
		dest.writeInt(start);
		dest.writeInt(end);
		dest.writeInt(type);
	}

	public static ParcelableMedia[] fromEntities(final EntitySupport entities) {
		final List<ParcelableMedia> list = new ArrayList<ParcelableMedia>();
		final MediaEntity[] medias = entities.getMediaEntities();
		if (medias != null) {
			for (final MediaEntity media : medias) {
				final URL media_url = media.getMediaURL();
				if (media_url != null) {
					list.add(new ParcelableMedia(media));
				}
			}
		}
		final URLEntity[] urls = entities.getURLEntities();
		if (urls != null) {
			for (final URLEntity url : urls) {
				final String expanded = ParseUtils.parseString(url.getExpandedURL());
				final String media_url = MediaPreviewUtils.getSupportedLink(expanded);
				if (expanded != null && media_url != null) {
					list.add(new ParcelableMedia(expanded, media_url, url.getStart(), url.getEnd(), TYPE_IMAGE));
				}
			}
		}
		if (list.isEmpty()) return null;
		return list.toArray(new ParcelableMedia[list.size()]);
	}

	public static ParcelableMedia[] fromJSONString(final String json) {
		if (TextUtils.isEmpty(json)) return null;
		try {
			return JSONSerializer.createArray(JSON_CREATOR, new JSONArray(json));
		} catch (final JSONException e) {
			return null;
		}
	}

	public static ParcelableMedia newImage(final String media_url, final String url) {
		return new ParcelableMedia(url, media_url, 0, 0, TYPE_IMAGE);
	}

}
