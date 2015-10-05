/*
 *                 Twidere - Twitter client for Android
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


import android.app.Activity;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.SessionEvent;

/**
 * Created by mariotaku on 15/10/5.
 */
public class ActivityTracker {

    private final IntList mInternalStack = new ArrayIntList();
    private SessionEvent mSessionEvent;

    public void dispatchStart(Activity activity) {
        mInternalStack.add(System.identityHashCode(activity));
        // BEGIN HotMobi
        if (mSessionEvent == null) {
            mSessionEvent = SessionEvent.create(activity);
        }
        // END HotMobi
    }

    public void dispatchStop(Activity activity) {

        final int hashCode = System.identityHashCode(activity);

        // BEGIN HotMobi
        final SessionEvent event = mSessionEvent;
        if (event != null && !isSwitchingInSameTask(hashCode)) {
            event.dumpPreferences(activity);
            event.markEnd();
            HotMobiLogger.getInstance(activity).log(event);
            mSessionEvent = null;
        }
        // END HotMobi

        mInternalStack.removeElement(hashCode);
    }

    private boolean isSwitchingInSameTask(int hashCode) {
        return mInternalStack.lastIndexOf(hashCode) < mInternalStack.size() - 1;
    }

    public int size() {
        return mInternalStack.size();
    }

    public boolean isEmpty() {
        return mInternalStack.isEmpty();
    }

}
