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

package org.mariotaku.twidere.util.dagger.component;

import org.mariotaku.twidere.activity.BasePreferenceActivity;
import org.mariotaku.twidere.activity.BaseThemedActivity;
import org.mariotaku.twidere.fragment.BaseFragment;
import org.mariotaku.twidere.fragment.support.BaseSupportFragment;
import org.mariotaku.twidere.util.MultiSelectEventHandler;
import org.mariotaku.twidere.util.dagger.ApplicationModule;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import dagger.Component;

/**
 * Created by mariotaku on 15/10/5.
 */
@Component(modules = ApplicationModule.class)
public interface GeneralComponent {
    void inject(StatusViewHolder.DummyStatusHolderAdapter object);

    void inject(BaseFragment object);

    void inject(BaseSupportFragment object);

    void inject(MultiSelectEventHandler object);

    void inject(BasePreferenceActivity object);

    void inject(BaseThemedActivity object);
}
