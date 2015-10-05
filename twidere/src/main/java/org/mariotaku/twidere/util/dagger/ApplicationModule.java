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

package org.mariotaku.twidere.util.dagger;

import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.ActivityStack;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ReadStateManager;

import dagger.Module;
import dagger.Provides;

/**
 * Created by mariotaku on 15/10/5.
 */
@Module
public class ApplicationModule {

    private final ActivityStack activityStack;
    private final AsyncTwitterWrapper asyncTwitterWrapper;
    private final ReadStateManager readStateManager;

    public ApplicationModule(TwidereApplication application) {
        activityStack = new ActivityStack();
        asyncTwitterWrapper = new AsyncTwitterWrapper(application);
        readStateManager = new ReadStateManager(application);
    }

    @Provides
    ActivityStack provideActivityStack() {
        return activityStack;
    }

    @Provides
    AsyncTwitterWrapper provideAsyncTwitterWrapper() {
        return asyncTwitterWrapper;
    }

    @Provides
    ReadStateManager provideReadStateManager() {
        return readStateManager;
    }

    public ActivityStack getActivityStack() {
        return activityStack;
    }

    public AsyncTwitterWrapper getAsyncTwitterWrapper() {
        return asyncTwitterWrapper;
    }

    public ReadStateManager getReadStateManager() {
        return readStateManager;
    }
}
