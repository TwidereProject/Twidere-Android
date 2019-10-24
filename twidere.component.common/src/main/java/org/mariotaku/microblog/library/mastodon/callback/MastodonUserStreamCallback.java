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

package org.mariotaku.microblog.library.mastodon.callback;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.mastodon.model.Notification;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.util.CRLFLineReader;
import org.mariotaku.restfu.callback.RawCallback;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.twidere.util.JsonSerializer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Created by mariotaku on 15/5/26.
 */
@SuppressWarnings({"WeakerAccess"})
public abstract class MastodonUserStreamCallback implements RawCallback<MicroBlogException> {

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
        final CRLFLineReader reader = new CRLFLineReader(new InputStreamReader(response.getBody().stream(), StandardCharsets.UTF_8));
        try {
            String event = null;
            for (String line; (line = reader.readLine()) != null && !disconnected; ) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                if (!connected) {
                    onConnected();
                    connected = true;
                }
                int valueIndex = line.indexOf(":");
                if (TextUtils.isEmpty(line) || valueIndex <= 0) {
                    event = null;
                    continue;
                }
                String name = line.substring(0, valueIndex);
                valueIndex++;
                while (line.charAt(valueIndex) == ' ') {
                    valueIndex++;
                }
                String value = line.substring(valueIndex);
                switch (name) {
                    case "event": {
                        event = value;
                        break;
                    }
                    case "data": {
                        if (event != null) {
                            if (!handleEvent(event, value)) {
                                onUnhandledEvent(event, value);
                            }
                        }
                        event = null;
                        break;
                    }
                    default: {
                        event = null;
                        break;
                    }
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

    private boolean handleEvent(final String event, final String payload) throws IOException {
        switch (event) {
            case "update": {
                return onUpdate(JsonSerializer.parse(payload, Status.class));
            }
            case "notification": {
                return onNotification(JsonSerializer.parse(payload, Notification.class));
            }
            case "delete": {
                return onDelete(payload);
            }
        }
        return false;
    }

    protected abstract boolean onConnected();

    protected abstract boolean onException(@NonNull Throwable ex);

    protected abstract boolean onUpdate(@NonNull Status status);

    protected abstract boolean onNotification(@NonNull Notification notification);

    protected abstract boolean onDelete(@NonNull String id);

    protected abstract void onUnhandledEvent(@NonNull String event, @NonNull String payload)
            throws IOException;
}
