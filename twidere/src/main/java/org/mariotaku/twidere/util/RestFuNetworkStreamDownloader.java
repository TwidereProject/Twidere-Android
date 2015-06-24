/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.content.Context;
import android.net.Uri;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.restfu.http.RestHttpRequest;
import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.restfu.http.mime.TypedData;
import org.mariotaku.twidere.activity.support.ThemedImagePickerActivity;
import org.mariotaku.twidere.model.RequestType;

import java.io.IOException;

/**
 * Created by mariotaku on 15/6/17.
 */
public class RestFuNetworkStreamDownloader extends ThemedImagePickerActivity.NetworkStreamDownloader {

    public RestFuNetworkStreamDownloader(Context context) {
        super(context);
    }

    public DownloadResult get(Uri uri) throws IOException {
        final RestHttpClient client = TwitterAPIFactory.getDefaultHttpClient(getContext());
        final RestHttpRequest.Builder builder = new RestHttpRequest.Builder();
        builder.method(GET.METHOD);
        builder.url(uri.toString());
        builder.extra(RequestType.MEDIA);
        final RestHttpResponse response = client.execute(builder.build());
        if (response.isSuccessful()) {
            final TypedData body = response.getBody();
            final ContentType contentType = body.contentType();
            return DownloadResult.get(body.stream(), contentType != null ? contentType.getContentType() : "image/*");
        } else {
            throw new IOException("Unable to get " + uri);
        }
    }

}
