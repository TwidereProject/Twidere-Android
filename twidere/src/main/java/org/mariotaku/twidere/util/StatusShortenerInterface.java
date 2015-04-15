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

import org.mariotaku.twidere.IStatusShortener;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.StatusShortenResult;

public final class StatusShortenerInterface extends AbsServiceInterface<IStatusShortener> implements IStatusShortener {

    protected StatusShortenerInterface(Context context, String shortenerName) {
        super(context, shortenerName);
    }

    @Override
    protected IStatusShortener onServiceConnected(ComponentName service, IBinder obj) {
        return IStatusShortener.Stub.asInterface(obj);
    }

    @Override
    public StatusShortenResult shorten(final ParcelableStatusUpdate status, final String overrideStatusText)
            throws RemoteException {
        final IStatusShortener iface = getInterface();
        if (iface == null) return null;
        try {
            return iface.shorten(status, overrideStatusText);
        } catch (final RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static StatusShortenerInterface getInstance(final Application application, final String shortener_name) {
        if (shortener_name == null) return null;
        final Intent intent = new Intent(INTENT_ACTION_EXTENSION_SHORTEN_STATUS);
        final ComponentName component = ComponentName.unflattenFromString(shortener_name);
        intent.setComponent(component);
        if (application.getPackageManager().queryIntentServices(intent, 0).size() != 1) return null;
        return new StatusShortenerInterface(application, shortener_name);
    }

}
