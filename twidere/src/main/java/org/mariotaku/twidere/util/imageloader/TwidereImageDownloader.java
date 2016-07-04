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

package org.mariotaku.twidere.util.imageloader;

import android.content.Context;
import android.text.TextUtils;

import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import org.mariotaku.mediaviewer.library.CacheDownloadLoader;
import org.mariotaku.mediaviewer.library.MediaDownloader;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class TwidereImageDownloader extends BaseImageDownloader {

    private final MediaDownloader mMediaDownloader;
    private final String mTwitterProfileImageSize;

    public TwidereImageDownloader(final Context context, MediaDownloader downloader) {
        super(context);
        mMediaDownloader = downloader;
        mTwitterProfileImageSize = context.getString(R.string.profile_image_size);
    }

    @Override
    protected InputStream getStreamFromNetwork(String uriString, final Object extras) throws IOException {
        if (uriString == null) return null;
        try {
            if (isTwitterProfileImage(uriString)) {
                uriString = Utils.getTwitterProfileImageOfSize(uriString, mTwitterProfileImageSize);
            }
            return getStreamFromNetworkInternal(uriString, extras);
        } catch (final FileNotFoundException e) {
            if (isTwitterProfileImage(uriString) && !uriString.contains("_normal.")) {
                return getStreamFromNetworkInternal(Utils.getNormalTwitterProfileImage(uriString), extras);
            }
            throw new IOException(String.format(Locale.US, "Error downloading image %s", uriString));
        }
    }

    private ContentLengthInputStream getStreamFromNetworkInternal(final String uriString, final Object extras) throws IOException {
        CacheDownloadLoader.DownloadResult result = mMediaDownloader.get(uriString, extras);
        return new ContentLengthInputStream(result.getStream(), (int) result.getLength());
    }

    private boolean isTwitterProfileImage(final String uriString) {
        return !TextUtils.isEmpty(uriString) && TwidereLinkify.PATTERN_TWITTER_PROFILE_IMAGES.matcher(uriString).matches();
    }

}
