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

package org.mariotaku.twidere.activity.iface;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by mariotaku on 15/12/28.
 */
public interface IExtendedActivity {

    void executeAfterFragmentResumed(Action action);

    interface Action {
        void execute(IExtendedActivity activity);
    }

    class ActionHelper {

        private final IExtendedActivity mActivity;

        private boolean mFragmentResumed;
        private Queue<Action> mActionQueue = new LinkedList<>();

        public ActionHelper(IExtendedActivity activity) {
            mActivity = activity;
        }

        public void dispatchOnPause() {
            mFragmentResumed = false;
        }

        public void dispatchOnResumeFragments() {
            mFragmentResumed = true;
            executePending();
        }


        private void executePending() {
            if (!mFragmentResumed) return;
            Action action;
            while ((action = mActionQueue.poll()) != null) {
                action.execute(mActivity);
            }
        }

        public void executeAfterFragmentResumed(Action action) {
            mActionQueue.add(action);
            executePending();
        }
    }
}
