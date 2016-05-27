/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.support.annotation.Nullable;
import android.util.Log;

import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.constant.IntentConstants;
import org.mariotaku.twidere.util.ServiceUtils.ServiceToken;

public abstract class AbsServiceInterface<I extends IInterface> implements IInterface {

    private final Context mContext;
    private final String mShortenerName;
    @Nullable
    private final Bundle mMetaData;
    private I mIInterface;

    private ServiceToken mToken;

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(final ComponentName service, final IBinder obj) {
            mIInterface = AbsServiceInterface.this.onServiceConnected(service, obj);
        }

        @Override
        public void onServiceDisconnected(final ComponentName service) {
            mIInterface = null;
        }
    };

    protected abstract I onServiceConnected(ComponentName service, IBinder obj);

    protected AbsServiceInterface(final Context context, final String componentName, @Nullable final Bundle metaData) {
        mContext = context;
        mShortenerName = componentName;
        mMetaData = metaData;
    }

    public final I getInterface() {
        return mIInterface;
    }

    @Override
    public final IBinder asBinder() {
        // Useless here
        return mIInterface.asBinder();
    }

    public final void unbindService() {
        ServiceUtils.unbindFromService(mToken);
    }

    public final void waitForService() {
        final Intent intent = new Intent(IntentConstants.INTENT_ACTION_EXTENSION_SHORTEN_STATUS);
        final ComponentName component = ComponentName.unflattenFromString(mShortenerName);
        intent.setComponent(component);
        mToken = ServiceUtils.bindToService(mContext, intent, mConnection);
        while (mIInterface == null) {
            try {
                Thread.sleep(100L);
            } catch (final InterruptedException e) {
                Log.w(TwidereConstants.LOGTAG, e);
            }
        }
    }

    public final void checkService(CheckServiceAction action) throws CheckServiceException {
        action.check(mMetaData);
    }

    public interface CheckServiceAction {
        void check(@Nullable Bundle metaData) throws CheckServiceException;
    }

    public static class CheckServiceException extends Exception {

    }
}
