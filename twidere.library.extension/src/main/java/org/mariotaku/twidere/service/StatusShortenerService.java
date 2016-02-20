package org.mariotaku.twidere.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import org.mariotaku.twidere.IStatusShortener;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.StatusShortenResult;

import java.lang.ref.WeakReference;

/**
 * Created by mariotaku on 16/2/20.
 */
public abstract class StatusShortenerService extends Service {
    private final StatusShortenerStub mBinder = new StatusShortenerStub(this);

    public IBinder onBind(final Intent intent) {
        return mBinder;
    }

    protected abstract StatusShortenResult shorten(ParcelableStatusUpdate status,
                                                   ParcelableAccount currentAccount,
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
        public StatusShortenResult shorten(final ParcelableStatusUpdate status,
                                           final ParcelableAccount currentAccount,
                                           final String overrideStatusText)
                throws RemoteException {
            return mService.get().shorten(status, currentAccount, overrideStatusText);
        }

        @Override
        public boolean callback(StatusShortenResult result, ParcelableStatus status) throws RemoteException {
            return mService.get().callback(result, status);
        }

    }
}
