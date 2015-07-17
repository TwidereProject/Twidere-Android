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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ExtensionsAdapter;
import org.mariotaku.twidere.loader.ExtensionsListLoader;
import org.mariotaku.twidere.loader.ExtensionsListLoader.ExtensionInfo;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.PermissionsManager;

import java.util.List;

public class ExtensionsListFragment extends BaseListFragment implements Constants, LoaderCallbacks<List<ExtensionInfo>> {

    private ExtensionsAdapter mAdapter;
    private PackageManager mPackageManager;
    private PermissionsManager mPermissionsManager;

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPackageManager = getActivity().getPackageManager();
        mPermissionsManager = new PermissionsManager(getActivity());
        mAdapter = new ExtensionsAdapter(getActivity());
        setListAdapter(mAdapter);
        final ListView listView = getListView();
        listView.setOnCreateContextMenuListener(this);
        getLoaderManager().initLoader(0, null, this);
        setListShown(false);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public Loader<List<ExtensionInfo>> onCreateLoader(final int id, final Bundle args) {
        return new ExtensionsListLoader(getActivity(), mPackageManager);
    }

    @Override
    public void onLoadFinished(final Loader<List<ExtensionInfo>> loader, final List<ExtensionInfo> data) {
        mAdapter.setData(data);
        setListShown(true);
    }

    @Override
    public void onLoaderReset(final Loader<List<ExtensionInfo>> loader) {
        mAdapter.setData(null);
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        openSettings(mAdapter.getItem(position));
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        final MenuInflater inflater = new MenuInflater(v.getContext());
        inflater.inflate(R.menu.action_extension, menu);
        final AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
        final ExtensionInfo extensionInfo = mAdapter.getItem(adapterMenuInfo.position);
        if (extensionInfo.pname != null && extensionInfo.settings != null) {
            final Intent intent = new Intent(INTENT_ACTION_EXTENSION_SETTINGS);
            intent.setClassName(extensionInfo.pname, extensionInfo.settings);
            MenuUtils.setMenuItemAvailability(menu, R.id.settings, mPackageManager.queryIntentActivities(intent, 0).size() == 1);
        } else {
            MenuUtils.setMenuItemAvailability(menu, R.id.settings, false);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        final ExtensionInfo extensionInfo = mAdapter.getItem(adapterMenuInfo.position);
        switch (item.getItemId()) {
            case R.id.settings: {
                openSettings(extensionInfo);
                break;
            }
            case R.id.delete: {
                uninstallExtension(extensionInfo);
                break;
            }
            case R.id.revoke: {
                mPermissionsManager.revoke(extensionInfo.pname);
                mAdapter.notifyDataSetChanged();
                break;
            }
            default: {
                return false;
            }
        }
        return true;
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

    private boolean uninstallExtension(final ExtensionInfo info) {
        if (info == null) return false;
        final Uri packageUri = Uri.parse("package:" + info.pname);
        final Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        try {
            startActivity(uninstallIntent);
        } catch (final Exception e) {
            Log.w(LOGTAG, e);
            return false;
        }
        return true;
    }

}
