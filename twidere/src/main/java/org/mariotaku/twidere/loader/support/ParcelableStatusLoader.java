/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.loader.support;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import org.mariotaku.twidere.constant.IntentConstants;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SingleResponse;

import org.mariotaku.twidere.api.twitter.TwitterException;

import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT;
import static org.mariotaku.twidere.util.Utils.findStatus;

/**
 * Created by mariotaku on 14/12/5.
 */
public class ParcelableStatusLoader extends AsyncTaskLoader<SingleResponse<ParcelableStatus>> {

    private final boolean mOmitIntentExtra;
    private final Bundle mExtras;
    private final long mAccountId, mStatusId;

    public ParcelableStatusLoader(final Context context, final boolean omitIntentExtra, final Bundle extras,
                                  final long accountId, final long statusId) {
        super(context);
        mOmitIntentExtra = omitIntentExtra;
        mExtras = extras;
        mAccountId = accountId;
        mStatusId = statusId;
    }

    @Override
    public SingleResponse<ParcelableStatus> loadInBackground() {
        if (!mOmitIntentExtra && mExtras != null) {
            final ParcelableStatus cache = mExtras.getParcelable(IntentConstants.EXTRA_STATUS);
            if (cache != null) {
                final SingleResponse<ParcelableStatus> response = SingleResponse.getInstance(cache);
                final Bundle extras = response.getExtras();
                extras.putParcelable(EXTRA_ACCOUNT, ParcelableCredentials.getCredentials(getContext(), mAccountId));
                return response;
            }
        }
        try {
            final ParcelableStatus status = findStatus(getContext(), mAccountId, mStatusId);
            final ParcelableCredentials credentials = ParcelableAccount.getCredentials(getContext(), mAccountId);
            final SingleResponse<ParcelableStatus> response = SingleResponse.getInstance(status);
            final Bundle extras = response.getExtras();
            extras.putParcelable(EXTRA_ACCOUNT, credentials);
            return response;
        } catch (final TwitterException e) {
            return SingleResponse.getInstance(e);
        }
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

}
