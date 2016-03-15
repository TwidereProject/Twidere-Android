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

package org.mariotaku.twidere.fragment.iface;

import android.graphics.Rect;
import android.os.Bundle;

import java.util.LinkedList;
import java.util.Queue;

public interface IBaseFragment {
    Bundle getExtraConfiguration();

    int getTabPosition();

    void requestFitSystemWindows();

    interface SystemWindowsInsetsCallback {
        boolean getSystemWindowsInsets(Rect insets);
    }

    void executeAfterFragmentResumed(Action action);

    interface Action {
        void execute(IBaseFragment fragment);
    }

    class ActionHelper {

        private final IBaseFragment mFragment;

        private boolean mFragmentResumed;
        private Queue<Action> mActionQueue = new LinkedList<>();

        public ActionHelper(IBaseFragment fragment) {
            mFragment = fragment;
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
                action.execute(mFragment);
            }
        }

        public void executeAfterFragmentResumed(Action action) {
            mActionQueue.add(action);
            executePending();
        }
    }
}
