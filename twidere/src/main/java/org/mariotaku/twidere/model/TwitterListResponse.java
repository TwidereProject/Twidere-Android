/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model;

import java.util.List;

/**
 * Created by mariotaku on 2017/4/20.
 */
public class TwitterListResponse<Data> extends ListResponse<Data> {

    public final UserKey accountKey;
    public final String maxId;
    public final String sinceId;

    public TwitterListResponse(final UserKey accountKey,
            final Exception exception) {
        this(accountKey, null, null, null, exception);
    }

    public TwitterListResponse(final UserKey accountKey, final String maxId,
            final String sinceId, final List<Data> list) {
        this(accountKey, maxId, sinceId, list, null);
    }

    TwitterListResponse(final UserKey accountKey, final String maxId, final String sinceId,
            final List<Data> list, final Exception exception) {
        super(list, exception);
        this.accountKey = accountKey;
        this.maxId = maxId;
        this.sinceId = sinceId;
    }

}
