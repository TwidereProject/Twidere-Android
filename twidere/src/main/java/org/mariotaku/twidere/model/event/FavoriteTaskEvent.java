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

package org.mariotaku.twidere.model.event;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;

/**
 * Created by mariotaku on 14/12/10.
 */
public class FavoriteTaskEvent {

    private int action;
    private UserKey mAccountKey;
    private String statusId;

    @Nullable
    private ParcelableStatus status;
    private boolean finished;
    private boolean succeeded;

    public FavoriteTaskEvent(@Action final int action, final UserKey accountKey, final String statusId) {
        this.action = action;
        this.mAccountKey = accountKey;
        this.statusId = statusId;
    }

    public int getAction() {
        return action;
    }

    public UserKey getAccountKey() {
        return mAccountKey;
    }

    public String getStatusId() {
        return statusId;
    }

    @Nullable
    public ParcelableStatus getStatus() {
        return status;
    }

    public void setStatus(@Nullable ParcelableStatus status) {
        this.status = status;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    @IntDef({Action.CREATE, Action.DESTROY})
    public @interface Action {
        int CREATE = 1;
        int DESTROY = 2;
    }
}
