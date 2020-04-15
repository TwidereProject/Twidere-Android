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

import org.mariotaku.twidere.IStatusShortener;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.StatusShortenResult;
import org.mariotaku.twidere.model.UserKey;

import java.util.List;

import static org.mariotaku.twidere.constant.IntentConstants.INTENT_ACTION_EXTENSION_SHORTEN_STATUS;

public final class StatusShortenerInterface extends AbsServiceInterface<IStatusShortener> {

    private StatusShortenerInterface(Context context, String shortenerName, Bundle metaData) {
        super(context, shortenerName, metaData);
    }

    @Override
    protected IStatusShortener onServiceConnected(ComponentName service, IBinder obj) {
        return IStatusShortener.Stub.asInterface(obj);
    }

    public StatusShortenResult shorten(final ParcelableStatusUpdate status,
            final UserKey currentAccountKey, final String overrideStatusText) {
        final IStatusShortener iface = getInterface();
        if (iface == null) return StatusShortenResult.error(1, "Shortener not ready");
        try {
            final String statusJson = JsonSerializer.serialize(status, ParcelableStatusUpdate.class);
            final String resultJson = iface.shorten(statusJson, currentAccountKey.toString(),
                    overrideStatusText);
            return JsonSerializer.parse(resultJson, StatusShortenResult.class);
        } catch (final Exception e) {
            return StatusShortenResult.error(2, e.getMessage());
        }
    }

    public boolean callback(StatusShortenResult result, ParcelableStatus status) {
        final IStatusShortener iface = getInterface();
        if (iface == null) return false;
        try {
            final String resultJson = JsonSerializer.serialize(result, StatusShortenResult.class);
            final String statusJson = JsonSerializer.serialize(status, ParcelableStatus.class);
            return iface.callback(resultJson, statusJson);
        } catch (final Exception e) {
            return false;
        }
    }

    @Nullable
    public static StatusShortenerInterface getInstance(final Application application, final String shortenerName) {
        if (shortenerName == null) return null;
        final Intent intent = new Intent(INTENT_ACTION_EXTENSION_SHORTEN_STATUS);
        final ComponentName component = ComponentName.unflattenFromString(shortenerName);
        intent.setComponent(component);
        final PackageManager pm = application.getPackageManager();
        final List<ResolveInfo> services = pm.queryIntentServices(intent, PackageManager.GET_META_DATA);
        if (services.size() != 1) return null;
        return new StatusShortenerInterface(application, shortenerName, services.get(0).serviceInfo.metaData);
    }

}
