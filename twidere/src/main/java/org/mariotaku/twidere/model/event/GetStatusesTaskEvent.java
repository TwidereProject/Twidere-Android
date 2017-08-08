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

package org.mariotaku.twidere.model.event;

import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by mariotaku on 14/12/10.
 */
public class GetStatusesTaskEvent {

    @NonNull
    public final Uri uri;
    public final boolean running;
    public final Exception exception;

    public GetStatusesTaskEvent(@NonNull Uri uri, boolean running, Exception exception) {
        this.uri = uri;
        this.running = running;
        this.exception = exception;
    }
}
