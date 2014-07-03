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

package org.mariotaku.twidere.fragment;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import org.mariotaku.menucomponent.widget.PopupMenu;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ExtensionsAdapter;
import org.mariotaku.twidere.loader.ExtensionsListLoader;
import org.mariotaku.twidere.loader.ExtensionsListLoader.ExtensionInfo;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.util.PermissionsManager;

import java.util.List;

public class ExtensionsListFragment extends BaseListFragment implements Constants,
		LoaderCallbacks<List<ExtensionInfo>>, OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener,
		Panes.Right {

	private ExtensionsAdapter mAdapter;
	private PackageManager mPackageManager;
	private PermissionsManager mPermissionsManager;
	private ExtensionInfo mSelectedExtension;
	private ListView mListView;
	private PopupMenu mPopupMenu;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPackageManager = getActivity().getPackageManager();
		mPermissionsManager = new PermissionsManager(getActivity());
		mAdapter = new ExtensionsAdapter(getActivity());
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, null, this);
		setListShown(false);
	}

	@Override
	public Loader<List<ExtensionInfo>> onCreateLoader(final int id, final Bundle args) {
		return new ExtensionsListLoader(getActivity(), mPackageManager);
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		openSettings(mAdapter.getItem(position));
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		mSelectedExtension = mAdapter.getItem(position);
		if (mSelectedExtension == null) return false;
		mPopupMenu = PopupMenu.getInstance(getActivity(), view);
		mPopupMenu.inflate(R.menu.action_extension);
		final Menu menu = mPopupMenu.getMenu();
		final MenuItem settings = menu.findItem(MENU_SETTINGS);
		final Intent intent = mSelectedExtension.pname != null && mSelectedExtension.settings != null ? new Intent(
				INTENT_ACTION_EXTENSION_SETTINGS) : null;
		if (intent != null) {
			intent.setClassName(mSelectedExtension.pname, mSelectedExtension.settings);
		}
		settings.setVisible(intent != null && mPackageManager.queryIntentActivities(intent, 0).size() == 1);
		mPopupMenu.setOnMenuItemClickListener(this);
		mPopupMenu.show();
		return true;
	}

	@Override
	public void onLoaderReset(final Loader<List<ExtensionInfo>> loader) {
		mAdapter.setData(null);
	}

	@Override
	public void onLoadFinished(final Loader<List<ExtensionInfo>> loader, final List<ExtensionInfo> data) {
		mAdapter.setData(data);
		setListShown(true);
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		if (mSelectedExtension == null) return false;
		switch (item.getItemId()) {
			case MENU_SETTINGS: {
				openSettings(mSelectedExtension);
				break;
			}
			case MENU_DELETE: {
				final Uri packageUri = Uri.parse("package:" + mSelectedExtension.pname);
				final Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
				startActivity(uninstallIntent);
				break;
			}
			case MENU_REVOKE: {
				mPermissionsManager.revoke(mSelectedExtension.pname);
				mAdapter.notifyDataSetChanged();
				break;
			}
		}
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onStop() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}

	private boolean openSettings(final ExtensionInfo info) {
		if (info == null || info.settings == null) return false;
		final Intent intent = new Intent(INTENT_ACTION_EXTENSIONS);
		intent.setClassName(info.pname, info.settings);
		try {
			startActivity(intent);
		} catch (final Exception e) {
			Log.w(LOGTAG, e);
			return false;
		}
		return true;
	}

}
