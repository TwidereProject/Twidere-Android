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

package org.mariotaku.twidere.extension.shortener.gist;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import org.mariotaku.restfu.RestAPIFactory;
import org.mariotaku.twidere.IStatusShortener;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.StatusShortenResult;

import java.lang.ref.WeakReference;

/**
 * Created by mariotaku on 15/6/4.
 */
public class GistStatusShortenerService extends Service {


    private final StatusShortenerStub mBinder = new StatusShortenerStub(this);

    @Override
    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    /**
     * @return Shortened tweet.
     */
    public StatusShortenResult shorten(final ParcelableStatusUpdate status, final String overrideStatusText) {
        final Github github = getGithubInstance();
        try {
            final String text = overrideStatusText != null ? overrideStatusText : status.text;
            final ParcelableAccount account = status.accounts[0];
            final NewGist newGist = new NewGist();
            final Gist response = github.createGist(newGist);
            if (response != null) return StatusShortenResult.getInstance(response.getHtmlUrl());
        } catch (final GithubException e) {
            final int errorCode = e.getErrorCode() != 0 ? e.getErrorCode() : -1;
            return StatusShortenResult.getInstance(errorCode, e.getMessage());
        }
        return StatusShortenResult.getInstance(-1, "Unknown error");
    }

    private Github getGithubInstance() {
        RestAPIFactory factory = new RestAPIFactory();
        return factory.build(Github.class);
    }

    /*
     * By making this a static class with a WeakReference to the Service, we
     * ensure that the Service can be GCd even when the system process still has
     * a remote reference to the stub.
     */
    private static final class StatusShortenerStub extends IStatusShortener.Stub {

        final WeakReference<GistStatusShortenerService> mService;

        public StatusShortenerStub(final GistStatusShortenerService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public StatusShortenResult shorten(final ParcelableStatusUpdate status, final String overrideStatusText)
                throws RemoteException {
            return mService.get().shorten(status, overrideStatusText);
        }

    }

}
