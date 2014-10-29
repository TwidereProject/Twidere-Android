package org.mariotaku.twidere.fragment.support;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.SignInActivity;
import org.mariotaku.twidere.adapter.AccountsAdapter;
import org.mariotaku.twidere.menu.TwidereMenuInflater;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.Utils;

/**
 * Created by mariotaku on 14/10/26.
 */
public class AccountsManagerFragment extends BaseSupportListFragment implements LoaderCallbacks<Cursor>, DropListener {

    private AccountsAdapter mAdapter;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_ACCOUNT: {
                final Intent intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
                intent.setClass(getActivity(), SignInActivity.class);
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, TwidereMenuInflater inflater) {
        inflater.inflate(R.menu.menu_accounts_manager, menu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        final FragmentActivity activity = getActivity();
        mAdapter = new AccountsAdapter(activity);
        Utils.configBaseAdapter(activity, mAdapter);
        setListAdapter(mAdapter);
        final DragSortListView listView = (DragSortListView) getListView();
        listView.setDropListener(this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(android.R.layout.list_content, null, false);
        final ListView originalList = (ListView) view.findViewById(android.R.id.list);
        final ViewGroup listContainer = (ViewGroup) originalList.getParent();
        listContainer.removeView(originalList);
        inflater.inflate(R.layout.fragment_custom_tabs, listContainer, true);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Uri uri = Accounts.CONTENT_URI;
        return new CursorLoader(getActivity(), uri, Accounts.COLUMNS, null, null, Accounts.SORT_POSITION);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void drop(int from, int to) {

    }

}
