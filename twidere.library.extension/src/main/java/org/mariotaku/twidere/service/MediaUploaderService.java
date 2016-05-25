package org.mariotaku.twidere.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import org.mariotaku.twidere.IMediaUploader;
import org.mariotaku.twidere.model.MediaUploadResult;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.UploaderMediaItem;
import org.mariotaku.commons.logansquare.LoganSquareMapperFinder;
import org.mariotaku.twidere.model.UserKey;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Abstract media uploader service
 * <p/>
 * Created by mariotaku on 16/2/27.
 */
public abstract class MediaUploaderService extends Service {

    private final MediaUploaderStub mBinder = new MediaUploaderStub(this);

    @Override
    public final IBinder onBind(final Intent intent) {
        return mBinder;
    }

    protected abstract MediaUploadResult upload(ParcelableStatusUpdate status,
                                                UserKey currentAccount, UploaderMediaItem[] media);

    protected abstract boolean callback(MediaUploadResult result, ParcelableStatus status);

    /*
     * By making this a static class with a WeakReference to the Service, we
     * ensure that the Service can be GCd even when the system process still has
     * a remote reference to the stub.
     */
    private static final class MediaUploaderStub extends IMediaUploader.Stub {

        final WeakReference<MediaUploaderService> mService;

        public MediaUploaderStub(final MediaUploaderService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public String upload(String statusJson, String currentAccount, String mediaJson) throws RemoteException {
            try {
                final ParcelableStatusUpdate statusUpdate = LoganSquareMapperFinder.mapperFor(ParcelableStatusUpdate.class)
                        .parse(statusJson);
                final List<UploaderMediaItem> media = LoganSquareMapperFinder.mapperFor(UploaderMediaItem.class)
                        .parseList(mediaJson);
                final MediaUploadResult shorten = mService.get().upload(statusUpdate,
                        UserKey.valueOf(currentAccount),
                        media.toArray(new UploaderMediaItem[media.size()]));
                return LoganSquareMapperFinder.mapperFor(MediaUploadResult.class).serialize(shorten);
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
                final MediaUploadResult result = LoganSquareMapperFinder.mapperFor(MediaUploadResult.class)
                        .parse(resultJson);
                final ParcelableStatus status = LoganSquareMapperFinder.mapperFor(ParcelableStatus.class)
                        .parse(statusJson);
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
