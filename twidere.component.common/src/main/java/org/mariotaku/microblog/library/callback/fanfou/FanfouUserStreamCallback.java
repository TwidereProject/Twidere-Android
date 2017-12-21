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

package org.mariotaku.microblog.library.callback.fanfou;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.model.fanfou.FanfouStreamObject;
import org.mariotaku.microblog.library.model.microblog.Status;
import org.mariotaku.microblog.library.model.microblog.User;
import org.mariotaku.microblog.library.util.CRLFLineReader;
import org.mariotaku.restfu.callback.RawCallback;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.twidere.util.JsonSerializer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Created by mariotaku on 15/5/26.
 */
@SuppressWarnings({"WeakerAccess"})
public abstract class FanfouUserStreamCallback implements RawCallback<MicroBlogException> {

    private boolean connected;

    private boolean disconnected;

    @Override
    public final void result(@NonNull final HttpResponse response) throws MicroBlogException, IOException {
        if (!response.isSuccessful()) {
            final MicroBlogException cause = new MicroBlogException();
            cause.setHttpResponse(response);
            onException(cause);
            return;
        }
        final CRLFLineReader reader = new CRLFLineReader(new InputStreamReader(response.getBody().stream(), "UTF-8"));
        try {
            for (String line; (line = reader.readLine()) != null && !disconnected; ) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                if (!connected) {
                    onConnected();
                    connected = true;
                }
                if (TextUtils.isEmpty(line)) continue;
                FanfouStreamObject object = JsonSerializer.parse(line, FanfouStreamObject.class);
                if (!handleEvent(object, line)) {
                    onUnhandledEvent(object.getEvent(), line);
                }
            }
        } catch (IOException e) {
            onException(e);
        } finally {
            reader.close();
        }
    }

    @Override
    public final void error(@NonNull final MicroBlogException cause) {
        onException(cause);
    }

    public final void disconnect() {
        disconnected = true;
    }

    private boolean handleEvent(final FanfouStreamObject object, final String json) throws IOException {
        switch (object.getEvent()) {
            case "message.create": {
                return onStatusCreation(object.getCreatedAt(), object.getSource(),
                        object.getTarget(), object.getObject(Status.class));
            }
        }
        return false;
    }

    protected abstract boolean onConnected();

    protected abstract boolean onDisconnect(int code, String reason);

    protected abstract boolean onException(@NonNull Throwable ex);

    protected abstract boolean onStatusCreation(@NonNull Date createdAt, @NonNull User source,
            @Nullable User target, @NonNull Status object);

    protected abstract void onUnhandledEvent(@NonNull String event, @NonNull String json)
            throws IOException;
}
