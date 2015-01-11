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

package org.mariotaku.twidere.extension.streaming.util;

import android.content.Context;

import twitter4j.http.HostAddressResolver;
import twitter4j.http.HostAddressResolverFactory;
import twitter4j.http.HttpClientConfiguration;

/**
 * Created by mariotaku on 15/1/11.
 */
public class TwidereHostAddressResolverFactory implements HostAddressResolverFactory {
    private final Context context;

    public TwidereHostAddressResolverFactory(Context context) {
        this.context = context;
    }

    @Override
    public HostAddressResolver getInstance(HttpClientConfiguration conf) {
        return new TwidereHostAddressResolver(context);
    }
}
