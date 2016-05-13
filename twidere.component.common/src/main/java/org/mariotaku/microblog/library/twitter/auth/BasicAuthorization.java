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

package org.mariotaku.microblog.library.twitter.auth;

import android.util.Base64;

import org.mariotaku.restfu.RestRequest;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;

/**
 * Created by mariotaku on 15/4/19.
 */
public final class BasicAuthorization implements Authorization {

    private final String user;
    private final String password;

    public BasicAuthorization(String user, String password) {
        this.user = user;
        this.password = password;
    }

    @Override
    public String getHeader(Endpoint endpoint, RestRequest info) {
        if (!hasAuthorization()) return null;
        return "Basic " + Base64.encodeToString((user + ":" + password).getBytes(), Base64.NO_WRAP);
    }

    @Override
    public boolean hasAuthorization() {
        return user != null && password != null;
    }
}
