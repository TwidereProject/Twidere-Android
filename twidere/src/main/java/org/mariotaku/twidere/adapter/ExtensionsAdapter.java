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

package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.loader.ExtensionsListLoader.ExtensionInfo;
import org.mariotaku.twidere.util.PermissionsManager;
import org.mariotaku.twidere.view.holder.CheckableTwoLineWithIconViewHolder;

import java.util.List;

public class ExtensionsAdapter extends ArrayAdapter<ExtensionInfo> implements Constants {

	private final PermissionsManager mPermissionsManager;

	public ExtensionsAdapter(final Context context) {
		super(context, R.layout.list_item_two_line_checked);
		mPermissionsManager = new PermissionsManager(context);
	}

	@Override
	public long getItemId(final int position) {
		return getItem(position).hashCode();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);

		final CheckableTwoLineWithIconViewHolder holder;
		final Object tag = view.getTag();
		if (tag instanceof CheckableTwoLineWithIconViewHolder) {
			holder = (CheckableTwoLineWithIconViewHolder) tag;
		} else {
			holder = new CheckableTwoLineWithIconViewHolder(view);
			view.setTag(holder);
		}

		final ExtensionInfo info = getItem(position);
		final boolean permissionValid = PermissionsManager.isPermissionValid(info.permissions);
		holder.checkbox.setVisibility(permissionValid ? View.VISIBLE : View.GONE);
		if (permissionValid) {
			holder.checkbox.setChecked(mPermissionsManager.checkPermission(info.pname, info.permissions));
		}
		holder.text1.setText(info.label);
		holder.text2.setVisibility(TextUtils.isEmpty(info.description) ? View.GONE : View.VISIBLE);
		holder.text2.setText(info.description);
		holder.icon.setImageDrawable(info.icon);
		return view;
	}

	public void setData(final List<ExtensionInfo> data) {
		clear();
		if (data != null) {
			addAll(data);
		}
		notifyDataSetChanged();
	}

}
