/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import android.util.Pair;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.http.RestHttpRequest;
import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.RequestType;
import org.mariotaku.twidere.util.HtmlLinkExtractor.HtmlLink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mariotaku.twidere.api.twitter.model.MediaEntity;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.UrlEntity;

import static android.text.TextUtils.isEmpty;

public class MediaPreviewUtils {

    public static final String AVAILABLE_URL_SCHEME_PREFIX = "(https?:\\/\\/)?";
    public static final String AVAILABLE_IMAGE_SHUFFIX = "(png|jpeg|jpg|gif|bmp)";
    public static final String SINA_WEIBO_IMAGES_AVAILABLE_SIZES = "(woriginal|large|thumbnail|bmiddle|wap[\\d]+|mw[\\d]+)";
    public static final String GOOGLE_IMAGES_AVAILABLE_SIZES = "((([whs]\\d+|no)\\-?)+)";

    private static final String STRING_PATTERN_TWITTER_IMAGES_DOMAIN = "(p|pbs)\\.twimg\\.com";
    private static final String STRING_PATTERN_TWITTER_TON_DOMAIN = "(ton)\\.twitter\\.com";
    private static final String STRING_PATTERN_SINA_WEIBO_IMAGES_DOMAIN = "[\\w\\d]+\\.sinaimg\\.cn|[\\w\\d]+\\.sina\\.cn";
    private static final String STRING_PATTERN_LOCKERZ_DOMAIN = "lockerz\\.com";
    private static final String STRING_PATTERN_PLIXI_DOMAIN = "plixi\\.com";
    private static final String STRING_PATTERN_INSTAGRAM_DOMAIN = "instagr\\.am|instagram\\.com";
    private static final String STRING_PATTERN_TWITPIC_DOMAIN = "twitpic\\.com";
    private static final String STRING_PATTERN_IMGLY_DOMAIN = "img\\.ly";
    private static final String STRING_PATTERN_YFROG_DOMAIN = "yfrog\\.com";
    private static final String STRING_PATTERN_TWITGOO_DOMAIN = "twitgoo\\.com";
    private static final String STRING_PATTERN_MOBYPICTURE_DOMAIN = "moby\\.to";
    private static final String STRING_PATTERN_IMGUR_DOMAIN = "imgur\\.com|i\\.imgur\\.com";
    private static final String STRING_PATTERN_PHOTOZOU_DOMAIN = "photozou\\.jp";
    private static final String STRING_PATTERN_GOOGLE_IMAGES_DOMAIN = "(lh|gp|s)(\\d+)?\\.(ggpht|googleusercontent)\\.com";

    private static final String STRING_PATTERN_IMAGES_NO_SCHEME = "[^:\\/\\/].+?\\." + AVAILABLE_IMAGE_SHUFFIX;
    private static final String STRING_PATTERN_TWITTER_IMAGES_NO_SCHEME = STRING_PATTERN_TWITTER_IMAGES_DOMAIN
            + "(\\/media)?\\/([\\d\\w\\-_]+)\\." + AVAILABLE_IMAGE_SHUFFIX;
    private static final String STRING_PATTERN_SINA_WEIBO_IMAGES_NO_SCHEME = "("
            + STRING_PATTERN_SINA_WEIBO_IMAGES_DOMAIN + ")" + "\\/" + SINA_WEIBO_IMAGES_AVAILABLE_SIZES
            + "\\/(([\\d\\w]+)\\." + AVAILABLE_IMAGE_SHUFFIX + ")";
    private static final String STRING_PATTERN_LOCKERZ_NO_SCHEME = "(" + STRING_PATTERN_LOCKERZ_DOMAIN + ")"
            + "\\/s\\/(\\w+)\\/?";
    private static final String STRING_PATTERN_PLIXI_NO_SCHEME = "(" + STRING_PATTERN_PLIXI_DOMAIN + ")"
            + "\\/p\\/(\\w+)\\/?";
    private static final String STRING_PATTERN_INSTAGRAM_NO_SCHEME = "(" + STRING_PATTERN_INSTAGRAM_DOMAIN + ")"
            + "\\/p\\/([_\\-\\d\\w]+)\\/?";
    private static final String STRING_PATTERN_TWITPIC_NO_SCHEME = STRING_PATTERN_TWITPIC_DOMAIN + "\\/([\\d\\w]+)\\/?";
    private static final String STRING_PATTERN_IMGLY_NO_SCHEME = STRING_PATTERN_IMGLY_DOMAIN + "\\/([\\w\\d]+)\\/?";
    private static final String STRING_PATTERN_YFROG_NO_SCHEME = STRING_PATTERN_YFROG_DOMAIN + "\\/([\\w\\d]+)\\/?";
    private static final String STRING_PATTERN_TWITGOO_NO_SCHEME = STRING_PATTERN_TWITGOO_DOMAIN + "\\/([\\d\\w]+)\\/?";
    private static final String STRING_PATTERN_MOBYPICTURE_NO_SCHEME = STRING_PATTERN_MOBYPICTURE_DOMAIN
            + "\\/([\\d\\w]+)\\/?";
    private static final String STRING_PATTERN_IMGUR_NO_SCHEME = "(" + STRING_PATTERN_IMGUR_DOMAIN + ")"
            + "\\/([\\d\\w]+)((?-i)s|(?-i)l)?(\\." + AVAILABLE_IMAGE_SHUFFIX + ")?";
    private static final String STRING_PATTERN_PHOTOZOU_NO_SCHEME = STRING_PATTERN_PHOTOZOU_DOMAIN
            + "\\/photo\\/show\\/([\\d]+)\\/([\\d]+)\\/?";
    private static final String STRING_PATTERN_GOOGLE_IMAGES_NO_SCHEME = "(" + STRING_PATTERN_GOOGLE_IMAGES_DOMAIN
            + ")" + "((\\/[\\w\\d\\-\\_]+)+)\\/" + GOOGLE_IMAGES_AVAILABLE_SIZES + "\\/.+";
    private static final String STRING_PATTERN_GOOGLE_PROXY_IMAGES_NO_SCHEME = "("
            + STRING_PATTERN_GOOGLE_IMAGES_DOMAIN + ")" + "\\/proxy\\/([\\w\\d\\-\\_]+)="
            + GOOGLE_IMAGES_AVAILABLE_SIZES;

    private static final String STRING_PATTERN_IMAGES = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_IMAGES_NO_SCHEME;
    private static final String STRING_PATTERN_TWITTER_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
            + STRING_PATTERN_TWITTER_IMAGES_NO_SCHEME;
    private static final String STRING_PATTERN_SINA_WEIBO_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
            + STRING_PATTERN_SINA_WEIBO_IMAGES_NO_SCHEME;
    private static final String STRING_PATTERN_LOCKERZ = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_LOCKERZ_NO_SCHEME;
    private static final String STRING_PATTERN_PLIXI = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_PLIXI_NO_SCHEME;
    private static final String STRING_PATTERN_INSTAGRAM = AVAILABLE_URL_SCHEME_PREFIX
            + STRING_PATTERN_INSTAGRAM_NO_SCHEME;
    private static final String STRING_PATTERN_TWITPIC = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_TWITPIC_NO_SCHEME;
    private static final String STRING_PATTERN_IMGLY = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_IMGLY_NO_SCHEME;
    private static final String STRING_PATTERN_YFROG = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_YFROG_NO_SCHEME;
    private static final String STRING_PATTERN_TWITGOO = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_TWITGOO_NO_SCHEME;
    private static final String STRING_PATTERN_MOBYPICTURE = AVAILABLE_URL_SCHEME_PREFIX
            + STRING_PATTERN_MOBYPICTURE_NO_SCHEME;
    private static final String STRING_PATTERN_IMGUR = AVAILABLE_URL_SCHEME_PREFIX + STRING_PATTERN_IMGUR_NO_SCHEME;
    private static final String STRING_PATTERN_PHOTOZOU = AVAILABLE_URL_SCHEME_PREFIX
            + STRING_PATTERN_PHOTOZOU_NO_SCHEME;
    private static final String STRING_PATTERN_GOOGLE_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
            + STRING_PATTERN_GOOGLE_IMAGES_NO_SCHEME;
    private static final String STRING_PATTERN_GOOGLE_PROXY_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
            + STRING_PATTERN_GOOGLE_PROXY_IMAGES_NO_SCHEME;
    private static final String STRING_PATTERN_TWITTER_DM_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
            + STRING_PATTERN_TWITTER_IMAGES_NO_SCHEME;

    public static final Pattern PATTERN_TWITTER_IMAGES = Pattern.compile(STRING_PATTERN_TWITTER_IMAGES,
            Pattern.CASE_INSENSITIVE);
    public static final Pattern PATTERN_TWITTER_DM_IMAGES = Pattern.compile(STRING_PATTERN_TWITTER_DM_IMAGES,
            Pattern.CASE_INSENSITIVE);
    public static final Pattern PATTERN_SINA_WEIBO_IMAGES = Pattern.compile(STRING_PATTERN_SINA_WEIBO_IMAGES,
            Pattern.CASE_INSENSITIVE);
    public static final Pattern PATTERN_LOCKERZ = Pattern.compile(STRING_PATTERN_LOCKERZ, Pattern.CASE_INSENSITIVE);
    public static final Pattern PATTERN_PLIXI = Pattern.compile(STRING_PATTERN_PLIXI, Pattern.CASE_INSENSITIVE);

    public static final Pattern PATTERN_INSTAGRAM = Pattern.compile(STRING_PATTERN_INSTAGRAM, Pattern.CASE_INSENSITIVE);
    public static final int INSTAGRAM_GROUP_ID = 3;

    public static final Pattern PATTERN_TWITPIC = Pattern.compile(STRING_PATTERN_TWITPIC, Pattern.CASE_INSENSITIVE);
    public static final int TWITPIC_GROUP_ID = 2;

    public static final Pattern PATTERN_IMGLY = Pattern.compile(STRING_PATTERN_IMGLY, Pattern.CASE_INSENSITIVE);
    public static final int IMGLY_GROUP_ID = 2;

    public static final Pattern PATTERN_YFROG = Pattern.compile(STRING_PATTERN_YFROG, Pattern.CASE_INSENSITIVE);
    public static final int YFROG_GROUP_ID = 2;

    public static final Pattern PATTERN_TWITGOO = Pattern.compile(STRING_PATTERN_TWITGOO, Pattern.CASE_INSENSITIVE);
    public static final int TWITGOO_GROUP_ID = 2;

    public static final Pattern PATTERN_MOBYPICTURE = Pattern.compile(STRING_PATTERN_MOBYPICTURE,
            Pattern.CASE_INSENSITIVE);
    public static final int MOBYPICTURE_GROUP_ID = 2;

    public static final Pattern PATTERN_IMGUR = Pattern.compile(STRING_PATTERN_IMGUR, Pattern.CASE_INSENSITIVE);
    public static final int IMGUR_GROUP_ID = 3;

    public static final Pattern PATTERN_PHOTOZOU = Pattern.compile(STRING_PATTERN_PHOTOZOU, Pattern.CASE_INSENSITIVE);
    public static final int PHOTOZOU_GROUP_ID = 3;

    public static final Pattern PATTERN_GOOGLE_IMAGES = Pattern.compile(STRING_PATTERN_GOOGLE_IMAGES,
            Pattern.CASE_INSENSITIVE);
    public static final int GOOGLE_IMAGES_GROUP_SERVER = 2;
    public static final int GOOGLE_IMAGES_GROUP_ID = 6;

    public static final Pattern PATTERN_GOOGLE_PROXY_IMAGES = Pattern.compile(STRING_PATTERN_GOOGLE_PROXY_IMAGES,
            Pattern.CASE_INSENSITIVE);
    public static final int GOOGLE_PROXY_IMAGES_GROUP_SERVER = 2;
    public static final int GOOGLE_PROXY_IMAGES_GROUP_ID = 6;

    private static final Pattern[] SUPPORTED_PATTERNS = {PATTERN_TWITTER_IMAGES, PATTERN_INSTAGRAM,
            PATTERN_GOOGLE_IMAGES, PATTERN_GOOGLE_PROXY_IMAGES, PATTERN_SINA_WEIBO_IMAGES, PATTERN_TWITPIC,
            PATTERN_IMGUR, PATTERN_IMGLY, PATTERN_YFROG, PATTERN_LOCKERZ, PATTERN_PLIXI, PATTERN_TWITGOO,
            PATTERN_MOBYPICTURE, PATTERN_PHOTOZOU, PATTERN_TWITTER_DM_IMAGES};

    private static final String URL_PHOTOZOU_PHOTO_INFO = "https://api.photozou.jp/rest/photo_info.json";

    public static ParcelableMedia getAllAvailableImage(final String link, final boolean fullImage) {
        try {
            return getAllAvailableImage(link, fullImage, null);
        } catch (final IOException e) {
            throw new AssertionError("This should never happen");
        }
    }

    public static ParcelableMedia getAllAvailableImage(final String link, final boolean fullImage,
                                                       final RestHttpClient client) throws IOException {
        if (link == null) return null;
        Matcher m;
        m = PATTERN_TWITTER_IMAGES.matcher(link);
        if (m.matches()) return getTwitterImage(link, fullImage);
        m = PATTERN_INSTAGRAM.matcher(link);
        if (m.matches())
            return getInstagramImage(RegexUtils.matcherGroup(m, INSTAGRAM_GROUP_ID), link, fullImage);
        m = PATTERN_GOOGLE_IMAGES.matcher(link);
        if (m.matches())
            return getGoogleImage(RegexUtils.matcherGroup(m, GOOGLE_IMAGES_GROUP_SERVER), RegexUtils.matcherGroup(m, GOOGLE_IMAGES_GROUP_ID),
                    fullImage);
        m = PATTERN_GOOGLE_PROXY_IMAGES.matcher(link);
        if (m.matches())
            return getGoogleProxyImage(RegexUtils.matcherGroup(m, GOOGLE_PROXY_IMAGES_GROUP_SERVER),
                    RegexUtils.matcherGroup(m, GOOGLE_PROXY_IMAGES_GROUP_ID), fullImage);
        m = PATTERN_SINA_WEIBO_IMAGES.matcher(link);
        if (m.matches()) return getSinaWeiboImage(link, fullImage);
        m = PATTERN_TWITPIC.matcher(link);
        if (m.matches())
            return getTwitpicImage(RegexUtils.matcherGroup(m, TWITPIC_GROUP_ID), link, fullImage);
        m = PATTERN_IMGUR.matcher(link);
        if (m.matches())
            return getImgurImage(RegexUtils.matcherGroup(m, IMGUR_GROUP_ID), link, fullImage);
        m = PATTERN_IMGLY.matcher(link);
        if (m.matches())
            return getImglyImage(RegexUtils.matcherGroup(m, IMGLY_GROUP_ID), link, fullImage);
        m = PATTERN_YFROG.matcher(link);
        if (m.matches())
            return getYfrogImage(RegexUtils.matcherGroup(m, YFROG_GROUP_ID), link, fullImage);
        m = PATTERN_LOCKERZ.matcher(link);
        if (m.matches()) return getLockerzAndPlixiImage(link, fullImage);
        m = PATTERN_PLIXI.matcher(link);
        if (m.matches()) return getLockerzAndPlixiImage(link, fullImage);
        m = PATTERN_TWITGOO.matcher(link);
        if (m.matches())
            return getTwitgooImage(RegexUtils.matcherGroup(m, TWITGOO_GROUP_ID), link, fullImage);
        m = PATTERN_MOBYPICTURE.matcher(link);
        if (m.matches())
            return getMobyPictureImage(RegexUtils.matcherGroup(m, MOBYPICTURE_GROUP_ID), link, fullImage);
        m = PATTERN_PHOTOZOU.matcher(link);
        if (m.matches())
            return getPhotozouImage(client, RegexUtils.matcherGroup(m, PHOTOZOU_GROUP_ID), link, fullImage);
        return null;
    }

    public static ParcelableMedia[] getImagesInStatus(final String status_string, final boolean fullImage) {
        if (status_string == null) return new ParcelableMedia[0];
        final List<ParcelableMedia> images = new ArrayList<>();
        final HtmlLinkExtractor extractor = new HtmlLinkExtractor();
        for (final HtmlLink link : extractor.grabLinks(status_string)) {
            final ParcelableMedia spec = getAllAvailableImage(link.getLink(), fullImage);
            if (spec != null) {
                images.add(spec);
            }
        }
        return images.toArray(new ParcelableMedia[images.size()]);
    }

    public static String getSupportedFirstLink(final Status status) {
        if (status == null) return null;
        final MediaEntity[] mediaEntities = status.getMediaEntities();
        if (mediaEntities != null) {
            for (final MediaEntity mediaEntity : mediaEntities) {
                final String expanded = TwitterContentUtils.getMediaUrl(mediaEntity);
                if (getSupportedLink(expanded) != null) return expanded;
            }
        }
        final UrlEntity[] urlEntities = status.getUrlEntities();
        if (urlEntities != null) {
            for (final UrlEntity urlEntity : urlEntities) {
                final String expanded = urlEntity.getExpandedUrl();
                if (getSupportedLink(expanded) != null) return expanded;
            }
        }
        return null;
    }

    public static String getSupportedFirstLink(final String html) {
        if (html == null) return null;
        final HtmlLinkExtractor extractor = new HtmlLinkExtractor();
        for (final HtmlLink link : extractor.grabLinks(html)) {
            if (getSupportedLink(link.getLink()) != null) return link.getLink();
        }
        return null;
    }

    public static String getSupportedLink(final String link) {
        if (link == null) return null;
        for (final Pattern pattern : SUPPORTED_PATTERNS) {
            if (pattern.matcher(link).matches()) return link;
        }
        return null;
    }

    public static List<String> getSupportedLinksInStatus(final String statusString) {
        if (statusString == null) return Collections.emptyList();
        final List<String> links = new ArrayList<>();
        final HtmlLinkExtractor extractor = new HtmlLinkExtractor();
        for (final HtmlLink link : extractor.grabLinks(statusString)) {
            final String spec = getSupportedLink(link.getLink());
            if (spec != null) {
                links.add(spec);
            }
        }
        return links;
    }

    public static boolean isLinkSupported(final String link) {
        if (link == null) return false;
        for (final Pattern pattern : SUPPORTED_PATTERNS) {
            if (pattern.matcher(link).matches()) return true;
        }
        return false;
    }

    private static ParcelableMedia getGoogleImage(final String server, final String id, final boolean fullImage) {
        if (isEmpty(server) || isEmpty(id)) return null;
        final String full = "https://" + server + id + "/s0/full";
        final String preview = fullImage ? full : "https://" + server + id + "/s480/full";
        return ParcelableMedia.newImage(preview, full);
    }

    private static ParcelableMedia getGoogleProxyImage(final String server, final String id, final boolean fullImage) {
        if (isEmpty(server) || isEmpty(id)) return null;
        final String full = "https://" + server + "/proxy/" + id + "=s0";
        final String preview = fullImage ? full : "https://" + server + "/proxy/" + id + "=s480";
        return ParcelableMedia.newImage(preview, full);
    }

    private static ParcelableMedia getImglyImage(final String id, final String orig, final boolean fullImage) {
        if (isEmpty(id)) return null;
        final String preview = String.format("http://img.ly/show/%s/%s", fullImage ? "full" : "medium", id);
        return ParcelableMedia.newImage(preview, orig);
    }

    private static ParcelableMedia getImgurImage(final String id, final String orig, final boolean fullImage) {
        if (isEmpty(id)) return null;
        final String preview = fullImage ? String.format("http://i.imgur.com/%s.jpg", id) : String.format(
                "http://i.imgur.com/%sl.jpg", id);
        return ParcelableMedia.newImage(preview, orig);
    }

    private static ParcelableMedia getInstagramImage(final String id, final String orig, final boolean fullImage) {
        if (isEmpty(id)) return null;
        final String preview = String.format("https://instagram.com/p/%s/media/?size=%s", id, fullImage ? "l" : "m");
        return ParcelableMedia.newImage(preview, orig);
    }

    private static ParcelableMedia getLockerzAndPlixiImage(final String url, final boolean fullImage) {
        if (isEmpty(url)) return null;
        final String preview = String.format("https://api.plixi.com/api/tpapi.svc/imagefromurl?url=%s&size=%s", url,
                fullImage ? "big" : "small");
        return ParcelableMedia.newImage(preview, url);

    }

    private static ParcelableMedia getMobyPictureImage(final String id, final String orig, final boolean fullImage) {
        if (isEmpty(id)) return null;
        final String preview = String.format("http://moby.to/%s:%s", id, fullImage ? "full" : "thumb");
        return ParcelableMedia.newImage(preview, orig);
    }

    private static ParcelableMedia getPhotozouImage(final RestHttpClient client, final String id, final String orig,
                                                    final boolean fullImage) throws IOException {
        if (isEmpty(id)) return null;
        if (client != null) {
            final RestHttpRequest.Builder builder = new RestHttpRequest.Builder();
            builder.method(GET.METHOD);
            builder.url(Endpoint.constructUrl(URL_PHOTOZOU_PHOTO_INFO, Pair.create("photo_id", id)));
            builder.extra(RequestType.MEDIA);
            final RestHttpResponse response = client.execute(builder.build());
            final PhotoZouPhotoInfo info = LoganSquare.parse(response.getBody().stream(), PhotoZouPhotoInfo.class);
            if (info.info != null && info.info.photo != null) {
                final String key = fullImage ? info.info.photo.originalImageUrl : info.info.photo.imageUrl;
                return ParcelableMedia.newImage(key, orig);
            }
        }
        final String preview = String.format(Locale.US, "http://photozou.jp/p/img/%s", id);
        return ParcelableMedia.newImage(preview, orig);
    }

    private static ParcelableMedia getSinaWeiboImage(final String url, final boolean fullImage) {
        if (isEmpty(url)) return null;
        final String full = url.replaceAll("/" + SINA_WEIBO_IMAGES_AVAILABLE_SIZES + "/", "/woriginal/");
        final String preview = fullImage ? full : url.replaceAll("/" + SINA_WEIBO_IMAGES_AVAILABLE_SIZES + "/",
                "/bmiddle/");
        return ParcelableMedia.newImage(preview, full);
    }

    private static ParcelableMedia getTwitgooImage(final String id, final String orig, final boolean fullImage) {
        if (isEmpty(id)) return null;
        final String preview = String.format("http://twitgoo.com/show/%s/%s", fullImage ? "img" : "thumb", id);
        return ParcelableMedia.newImage(preview, orig);
    }

    private static ParcelableMedia getTwitpicImage(final String id, final String orig, final boolean fullImage) {
        if (isEmpty(id)) return null;
        final String preview = String.format("http://twitpic.com/show/%s/%s", fullImage ? "large" : "thumb", id);
        return ParcelableMedia.newImage(preview, orig);
    }

    private static ParcelableMedia getTwitterImage(final String url, final boolean fullImage) {
        if (isEmpty(url)) return null;
        final String full = (url + ":large").replaceFirst("https?://", "https://");
        final String preview = fullImage ? full : (url + ":medium").replaceFirst("https?://", "https://");
        return ParcelableMedia.newImage(preview, full);
    }

    private static ParcelableMedia getYfrogImage(final String id, final String orig, final boolean fullImage) {
        if (isEmpty(id)) return null;
        final String preview = String.format("http://yfrog.com/%s:%s", id, fullImage ? "medium" : "iphone");
        return ParcelableMedia.newImage(preview, orig);
    }

    @JsonObject
    public static final class PhotoZouPhotoInfo {

        @JsonField(name = "info")
        public Info info;

        @JsonObject
        public static final class Info {

            @JsonField(name = "photo")
            public Photo photo;

            @JsonObject
            public static final class Photo {
                @JsonField(name = "original_image_url")
                public String originalImageUrl;
                @JsonField(name = "image_url")
                public String imageUrl;
            }
        }

    }

}
