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

package org.mariotaku.twidere.model;

import static org.mariotaku.twidere.util.CompareUtils.bundleEquals;
import static org.mariotaku.twidere.util.CompareUtils.classEquals;
import static org.mariotaku.twidere.util.CompareUtils.objectEquals;

import android.app.Fragment;
import android.os.Bundle;

public class TabSpec {

	public final CharSequence name;
	public final Object icon;
	public final Class<? extends Fragment> cls;
	public final Bundle args;
	public final int position;

	public TabSpec(final CharSequence name, final Object icon, final Class<? extends Fragment> cls, final Bundle args,
			final int position) {
		if (cls == null) throw new IllegalArgumentException("Fragment cannot be null!");
		if (name == null && icon == null)
			throw new IllegalArgumentException("You must specify a name or icon for this tab!");
		this.name = name;
		this.icon = icon;
		this.cls = cls;
		this.args = args;
		this.position = position;

	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof TabSpec)) return false;
		final TabSpec spec = (TabSpec) o;
		return objectEquals(name, spec.name) && objectEquals(icon, spec.icon) && classEquals(cls, spec.cls)
				&& bundleEquals(args, spec.args) && position == spec.position;
	}

	@Override
	public String toString() {
		return "TabSpec{name=" + name + ", icon=" + icon + ", cls=" + cls + ", args=" + args + ", position=" + position
				+ "}";
	}

}
