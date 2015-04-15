/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import org.mariotaku.twidere.IMediaUploader;
import org.mariotaku.twidere.model.MediaUploadResult;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.UploaderMediaItem;

public final class MediaUploaderInterface extends AbsServiceInterface<IMediaUploader> implements IMediaUploader {
    protected MediaUploaderInterface(Context context, String shortenerName) {
        super(context, shortenerName);
    }

    public static MediaUploaderInterface getInstance(final Application application, final String uploaderName) {
        if (uploaderName == null) return null;
        final Intent intent = new Intent(INTENT_ACTION_EXTENSION_UPLOAD_MEDIA);
        final ComponentName component = ComponentName.unflattenFromString(uploaderName);
        intent.setComponent(component);
        if (application.getPackageManager().queryIntentServices(intent, 0).size() != 1) return null;
        return new MediaUploaderInterface(application, uploaderName);
    }

    @Override
    public MediaUploadResult upload(final ParcelableStatusUpdate status, final UploaderMediaItem[] media)
            throws RemoteException {
        final IMediaUploader iface = getInterface();
        if (iface == null) return null;
        try {
            return iface.upload(status, media);
        } catch (final RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected IMediaUploader onServiceConnected(ComponentName service, IBinder obj) {
        return IMediaUploader.Stub.asInterface(obj);
    }
}
