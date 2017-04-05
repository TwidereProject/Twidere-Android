/*
 *         Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mariotaku.twidere.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import org.mariotaku.twidere.IStatusShortener;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.StatusShortenResult;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.JsonSerializer;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Abstract status shortener service
 * <p/>
 * Created by mariotaku on 16/2/20.
 */
public abstract class StatusShortenerService extends Service {
    private final StatusShortenerStub mBinder = new StatusShortenerStub(this);

    @Override
    public final IBinder onBind(final Intent intent) {
        return mBinder;
    }

    protected abstract StatusShortenResult shorten(ParcelableStatusUpdate status,
            UserKey currentAccountKey,
            String overrideStatusText);

    protected abstract boolean callback(StatusShortenResult result, ParcelableStatus status);

    /*
     * By making this a static class with a WeakReference to the Service, we
     * ensure that the Service can be GCd even when the system process still has
     * a remote reference to the stub.
     */
    private static final class StatusShortenerStub extends IStatusShortener.Stub {

        final WeakReference<StatusShortenerService> mService;

        public StatusShortenerStub(final StatusShortenerService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public String shorten(final String statusJson, final String currentAccountIdStr,
                final String overrideStatusText)
                throws RemoteException {
            try {
                final ParcelableStatusUpdate statusUpdate = JsonSerializer.parse(statusJson,
                        ParcelableStatusUpdate.class);
                final UserKey currentAccountId = UserKey.valueOf(currentAccountIdStr);
                final StatusShortenResult shorten = mService.get().shorten(statusUpdate, currentAccountId,
                        overrideStatusText);
                return JsonSerializer.serialize(shorten, StatusShortenResult.class);
            } catch (IOException e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    throw new RemoteException(e.getMessage());
                } else {
                    throw new RemoteException();
                }
            }
        }

        @Override
        public boolean callback(String resultJson, String statusJson) throws RemoteException {
            try {
                final StatusShortenResult result = JsonSerializer.parse(resultJson, StatusShortenResult.class);
                final ParcelableStatus status = JsonSerializer.parse(statusJson, ParcelableStatus.class);
                return mService.get().callback(result, status);
            } catch (IOException e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    throw new RemoteException(e.getMessage());
                } else {
                    throw new RemoteException();
                }
            }
        }

    }
}
