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

package org.mariotaku.twidere.adapter.support;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTrojan;
import android.view.ViewGroup;

public abstract class SupportFixedFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

	public SupportFixedFragmentStatePagerAdapter(final FragmentManager fm) {
		super(fm);
	}

	@Override
	public Object instantiateItem(final ViewGroup container, final int position) {
		final Fragment f = (Fragment) super.instantiateItem(container, position);
		final Bundle savedFragmentState = f != null ? FragmentTrojan.getSavedFragmentState(f) : null;
		if (savedFragmentState != null && f != null) {
			savedFragmentState.setClassLoader(f.getClass().getClassLoader());
		}
		return f;
	}

}
