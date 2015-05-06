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

import org.mariotaku.simplerestapi.RestAPIFactory;
import org.mariotaku.simplerestapi.http.Authorization;
import org.mariotaku.twidere.api.twitter.auth.OAuthEndpoint;
import org.mariotaku.twidere.api.twitter.model.impl.StatusImpl;
import org.mariotaku.twidere.api.twitter.model.impl.TypeConverterMapper;
import org.mariotaku.twidere.api.twitter.model.impl.UserImpl;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;

/**
 * Created by mariotaku on 15/5/5.
 */
public class TwitterAPIFactory {

    static {
        TypeConverterMapper.register(Status.class, StatusImpl.class);
        TypeConverterMapper.register(User.class, UserImpl.class);
    }


    public static Twitter getInstance(Authorization authorization) {
        final OAuthEndpoint endpoint = new OAuthEndpoint("https://api.twitter.com/1.1/");
        final RestAPIFactory factory = new RestAPIFactory();
        factory.setClient(new OkHttpRestClient());
        factory.setConverter(new TwitterConverter());
        factory.setEndpoint(endpoint);
        factory.setAuthorization(authorization);
        return factory.build(Twitter.class);
    }

}
