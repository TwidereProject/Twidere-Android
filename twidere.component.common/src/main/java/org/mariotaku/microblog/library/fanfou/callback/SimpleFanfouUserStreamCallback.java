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

package org.mariotaku.microblog.library.fanfou.callback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.User;

import java.io.IOException;
import java.util.Date;

/**
 * Created by mariotaku on 2017/3/11.
 */

public abstract class SimpleFanfouUserStreamCallback extends FanfouUserStreamCallback {
    @Override
    protected boolean onConnected() {
        return false;
    }

    @Override
    protected boolean onDisconnect(final int code, final String reason) {
        return false;
    }

    @Override
    protected boolean onException(@NonNull final Throwable ex) {
        return false;
    }

    @Override
    protected boolean onStatusCreation(@NonNull final Date createdAt, @NonNull final User source,
            @Nullable final User target, @NonNull final Status status) {
        return false;
    }

    @Override
    protected void onUnhandledEvent(@NonNull final String event, @NonNull final String json)
            throws IOException {

    }
}
