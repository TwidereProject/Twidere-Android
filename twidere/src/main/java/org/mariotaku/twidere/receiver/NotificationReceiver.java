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

package org.mariotaku.twidere.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.StringLongPair;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.ApplicationModule;

/**
 * Created by mariotaku on 15/4/4.
 */
public class NotificationReceiver extends BroadcastReceiver implements Constants {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action == null) return;
        switch (action) {
            case BROADCAST_NOTIFICATION_DELETED: {
                final Uri uri = intent.getData();
                if (uri == null) return;
                final String tag = getPositionTag(uri.getLastPathSegment());
                if (tag == null) return;
                final long accountId = ParseUtils.parseLong(uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID), -1);
                final ReadStateManager manager = ApplicationModule.get(context).getReadStateManager();
                final String paramReadPosition, paramReadPositions;
                if (!TextUtils.isEmpty(paramReadPosition = uri.getQueryParameter(QUERY_PARAM_READ_POSITION))) {
                    manager.setPosition(Utils.getReadPositionTagWithAccounts(tag, accountId),
                            ParseUtils.parseLong(paramReadPosition, -1));
                } else if (!TextUtils.isEmpty(paramReadPositions = uri.getQueryParameter(QUERY_PARAM_READ_POSITIONS))) {
                    try {
                        final StringLongPair[] pairs = StringLongPair.valuesOf(paramReadPositions);
                        for (StringLongPair pair : pairs) {
                            manager.setPosition(tag, pair.getKey(), pair.getValue());
                        }
                    } catch (NumberFormatException ignore) {

                    }
                }
                break;
            }
        }
    }

    private static String getPositionTag(@NonNull String type) {
        switch (type) {
            case AUTHORITY_HOME: {
                return TAB_TYPE_HOME_TIMELINE;
            }
            case AUTHORITY_MENTIONS: {
                return TAB_TYPE_MENTIONS_TIMELINE;
            }
            case AUTHORITY_DIRECT_MESSAGES: {
                return TAB_TYPE_DIRECT_MESSAGES;
            }
        }
        return null;
    }
}
