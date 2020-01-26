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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;

import org.mariotaku.twidere.IMediaUploader;
import org.mariotaku.twidere.model.MediaUploadResult;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.UploaderMediaItem;
import org.mariotaku.twidere.model.UserKey;

import java.util.List;

import static org.mariotaku.twidere.constant.IntentConstants.INTENT_ACTION_EXTENSION_UPLOAD_MEDIA;

public final class MediaUploaderInterface extends AbsServiceInterface<IMediaUploader> {

    private MediaUploaderInterface(Context context, String uploaderName, Bundle metaData) {
        super(context, uploaderName, metaData);
    }

    public MediaUploadResult upload(final ParcelableStatusUpdate status,
            final UserKey currentAccountKey,
            final UploaderMediaItem[] media) {
        final IMediaUploader iface = getInterface();
        if (iface == null) return MediaUploadResult.error(1, "Uploader not ready");
        try {
            final String statusJson = JsonSerializer.serialize(status, ParcelableStatusUpdate.class);
            final String mediaJson = JsonSerializer.serialize(media, UploaderMediaItem.class);
            return JsonSerializer.parse(iface.upload(statusJson, currentAccountKey.toString(),
                    mediaJson), MediaUploadResult.class);
        } catch (final Exception e) {
            return MediaUploadResult.error(2, e.getMessage());
        }
    }


    public boolean callback(MediaUploadResult uploadResult, ParcelableStatus status) {
        final IMediaUploader iface = getInterface();
        if (iface == null) return false;
        try {
            final String resultJson = JsonSerializer.serialize(uploadResult, MediaUploadResult.class);
            final String statusJson = JsonSerializer.serialize(status, ParcelableStatus.class);
            return iface.callback(resultJson, statusJson);
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    protected IMediaUploader onServiceConnected(ComponentName service, IBinder obj) {
        return IMediaUploader.Stub.asInterface(obj);
    }

    @Nullable
    public static MediaUploaderInterface getInstance(final Application application, final String uploaderName) {
        if (uploaderName == null) return null;
        final Intent intent = new Intent(INTENT_ACTION_EXTENSION_UPLOAD_MEDIA);
        final ComponentName component = ComponentName.unflattenFromString(uploaderName);
        intent.setComponent(component);
        final PackageManager pm = application.getPackageManager();
        final List<ResolveInfo> services = pm.queryIntentServices(intent, PackageManager.GET_META_DATA);
        if (services.size() != 1) return null;
        return new MediaUploaderInterface(application, uploaderName, services.get(0).serviceInfo.metaData);
    }
}
