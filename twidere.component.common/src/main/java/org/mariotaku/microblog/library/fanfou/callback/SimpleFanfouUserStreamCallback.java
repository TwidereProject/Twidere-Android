/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
