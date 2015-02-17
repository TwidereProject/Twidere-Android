/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.api.twitter;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.client.Header;
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedOutput;

/**
 * Created by mariotaku on 15/2/4.
 */
public class APIFactory {

    public static Twitter newTwitter(@NonNull final Authorization authorization) {
        final RestAdapter.Builder adapterBuilder = new RestAdapter.Builder();
        adapterBuilder.setClient(new OkClient() {

            @Override
            public Response execute(Request request) throws IOException {
                final String method = request.getMethod();
                final String url = request.getUrl();
                final List<Header> headers = new ArrayList<>(request.getHeaders());
                if (authorization.hasAuthorization()) {
                    headers.add(new Header("Authorization", authorization.getHeader(request)));
                }
                final TypedOutput body = request.getBody();
                final Request wrapped = new Request(method, url, headers, body);
                return super.execute(wrapped);
            }
        });
        final RestAdapter adapter = adapterBuilder.build();
        return adapter.create(Twitter.class);
    }

}
