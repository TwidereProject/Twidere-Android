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

package org.mariotaku.twidere.activity.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.SimpleParcelableUserListsAdapter;
import org.mariotaku.twidere.adapter.SimpleParcelableUsersAdapter;
import org.mariotaku.twidere.adapter.UserHashtagAutoCompleteAdapter;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.http.HttpResponseCode;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserList;
import org.mariotaku.twidere.fragment.support.CreateUserListDialogFragment;
import org.mariotaku.twidere.fragment.support.SupportProgressDialogFragment;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.TwitterAPIFactory;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.ParseUtils.parseString;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;

public class UserListSelectorActivity extends BaseSupportDialogActivity implements OnClickListener, OnItemClickListener {

    private AutoCompleteTextView mEditScreenName;
    private ListView mUserListsListView, mUsersListView;
    private SimpleParcelableUserListsAdapter mUserListsAdapter;
    private SimpleParcelableUsersAdapter mUsersAdapter;
    private View mUsersListContainer, mUserListsContainer, mCreateUserListContainer;

    private String mScreenName;

    private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (BROADCAST_USER_LIST_CREATED.equals(action)) {
                getUserLists(mScreenName);
            }
        }
    };
    private Runnable mResumeFragmentRunnable;
    private boolean mFragmentsResumed;

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.screen_name_confirm: {
                final String screen_name = parseString(mEditScreenName.getText());
                if (isEmpty(screen_name)) return;
                searchUser(screen_name);
                break;
            }
            case R.id.create_list: {
                final DialogFragment f = new CreateUserListDialogFragment();
                final Bundle args = new Bundle();
                args.putLong(EXTRA_ACCOUNT_ID, getAccountId());
                f.setArguments(args);
                f.show(getSupportFragmentManager(), null);
                break;
            }
        }
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mUsersListContainer = findViewById(R.id.users_list_container);
        mUserListsContainer = findViewById(R.id.user_lists_container);
        mEditScreenName = (AutoCompleteTextView) findViewById(R.id.edit_screen_name);
        mUserListsListView = (ListView) findViewById(R.id.user_lists_list);
        mUsersListView = (ListView) findViewById(R.id.users_list);
        mCreateUserListContainer = findViewById(R.id.create_list_container);
    }

    @Override
    public void onItemClick(final AdapterView<?> view, final View child, final int position, final long id) {
        final int view_id = view.getId();
        final ListView list = (ListView) view;
        if (view_id == R.id.users_list) {
            final ParcelableUser user = mUsersAdapter.getItem(position - list.getHeaderViewsCount());
            if (user == null) return;
            if (isSelectingUser()) {
                final Intent data = new Intent();
                data.setExtrasClassLoader(getClassLoader());
                data.putExtra(EXTRA_USER, user);
                setResult(RESULT_OK, data);
                finish();
            } else {
                getUserLists(user.screen_name);
            }
        } else if (view_id == R.id.user_lists_list) {
            final Intent data = new Intent();
            data.putExtra(EXTRA_USER_LIST, mUserListsAdapter.getItem(position - list.getHeaderViewsCount()));
            setResult(RESULT_OK, data);
            finish();
        }
    }

    public void setUsersData(final List<ParcelableUser> data) {
        mUsersAdapter.setData(data, true);
        mUsersListContainer.setVisibility(View.VISIBLE);
        mUserListsContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        if (!intent.hasExtra(EXTRA_ACCOUNT_ID)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_user_list_selector);
        if (savedInstanceState == null) {
            mScreenName = intent.getStringExtra(EXTRA_SCREEN_NAME);
        } else {
            mScreenName = savedInstanceState.getString(EXTRA_SCREEN_NAME);
        }

        final boolean selecting_user = isSelectingUser();
        setTitle(selecting_user ? R.string.select_user : R.string.select_user_list);
        if (!isEmpty(mScreenName)) {
            if (selecting_user) {
                searchUser(mScreenName);
            } else {
                getUserLists(mScreenName);
            }
        }
        final UserHashtagAutoCompleteAdapter adapter = new UserHashtagAutoCompleteAdapter(this);
        adapter.setAccountId(getAccountId());
        mEditScreenName.setAdapter(adapter);
        mEditScreenName.setText(mScreenName);
        mUserListsListView.setAdapter(mUserListsAdapter = new SimpleParcelableUserListsAdapter(this));
        mUsersListView.setAdapter(mUsersAdapter = new SimpleParcelableUsersAdapter(this));
        mUserListsListView.setOnItemClickListener(this);
        mUsersListView.setOnItemClickListener(this);
        if (selecting_user) {
            mUsersListContainer.setVisibility(View.VISIBLE);
            mUserListsContainer.setVisibility(View.GONE);
        } else {
            mUsersListContainer.setVisibility(isEmpty(mScreenName) ? View.VISIBLE : View.GONE);
            mUserListsContainer.setVisibility(isEmpty(mScreenName) ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_SCREEN_NAME, mScreenName);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(BROADCAST_USER_LIST_CREATED);
        registerReceiver(mStatusReceiver, filter);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mStatusReceiver);
        super.onStop();
    }

    private long getAccountId() {
        return getIntent().getLongExtra(EXTRA_ACCOUNT_ID, -1);
    }

    private void getUserLists(final String screenName) {
        if (screenName == null) return;
        mScreenName = screenName;
        final GetUserListsTask task = new GetUserListsTask(this, getAccountId(), screenName);
        AsyncTaskUtils.executeTask(task);
    }

    private boolean isSelectingUser() {
        return INTENT_ACTION_SELECT_USER.equals(getIntent().getAction());
    }

    private void searchUser(final String name) {
        final SearchUsersTask task = new SearchUsersTask(this, getAccountId(), name);
        AsyncTaskUtils.executeTask(task);
    }

    private void setUserListsData(final List<ParcelableUserList> data, final boolean isMyAccount) {
        mUserListsAdapter.setData(data, true);
        mUsersListContainer.setVisibility(View.GONE);
        mUserListsContainer.setVisibility(View.VISIBLE);
        mCreateUserListContainer.setVisibility(isMyAccount ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (!mFragmentsResumed && mResumeFragmentRunnable != null) {
            mResumeFragmentRunnable.run();
        }
        mFragmentsResumed = true;
    }

    @Override
    protected void onPause() {
        mFragmentsResumed = false;
        super.onPause();
    }

    private void dismissDialogFragment(final String tag) {
        mResumeFragmentRunnable = new Runnable() {
            @Override
            public void run() {
                final FragmentManager fm = getSupportFragmentManager();
                final Fragment f = fm.findFragmentByTag(tag);
                if (f instanceof DialogFragment) {
                    ((DialogFragment) f).dismiss();
                }
                mResumeFragmentRunnable = null;
            }
        };
        if (mFragmentsResumed) {
            mResumeFragmentRunnable.run();
        }
    }

    private void showDialogFragment(final DialogFragment df, final String tag) {
        mResumeFragmentRunnable = new Runnable() {
            @Override
            public void run() {
                df.show(getSupportFragmentManager(), tag);
                mResumeFragmentRunnable = null;
            }
        };
        if (mFragmentsResumed) {
            mResumeFragmentRunnable.run();
        }
    }

    private static class GetUserListsTask extends AsyncTask<Object, Object, SingleResponse<List<ParcelableUserList>>> {

        private static final String FRAGMENT_TAG_GET_USER_LISTS = "get_user_lists";

        private final UserListSelectorActivity mActivity;
        private final long mAccountId;
        private final String mScreenName;

        GetUserListsTask(final UserListSelectorActivity activity, final long accountId, final String screenName) {
            mActivity = activity;
            mAccountId = accountId;
            mScreenName = screenName;
        }

        @Override
        protected SingleResponse<List<ParcelableUserList>> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mActivity, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final ResponseList<UserList> lists = twitter.getUserLists(mScreenName, true);
                final List<ParcelableUserList> data = new ArrayList<>();
                boolean is_my_account = mScreenName.equalsIgnoreCase(getAccountScreenName(mActivity, mAccountId));
                for (final UserList item : lists) {
                    final User user = item.getUser();
                    if (user != null && mScreenName.equalsIgnoreCase(user.getScreenName())) {
                        if (!is_my_account && user.getId() == mAccountId) {
                            is_my_account = true;
                        }
                        data.add(new ParcelableUserList(item, mAccountId));
                    }
                }
                final SingleResponse<List<ParcelableUserList>> result = SingleResponse.getInstance(data);
                result.getExtras().putBoolean(EXTRA_IS_MY_ACCOUNT, is_my_account);
                return result;
            } catch (final TwitterException e) {
                e.printStackTrace();
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<List<ParcelableUserList>> result) {
            mActivity.dismissDialogFragment(FRAGMENT_TAG_GET_USER_LISTS);
            if (result.getData() != null) {
                mActivity.setUserListsData(result.getData(), result.getExtras().getBoolean(EXTRA_IS_MY_ACCOUNT));
            } else if (result.getException() instanceof TwitterException) {
                final TwitterException te = (TwitterException) result.getException();
                if (te.getStatusCode() == HttpResponseCode.NOT_FOUND) {
                    mActivity.searchUser(mScreenName);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            final SupportProgressDialogFragment df = new SupportProgressDialogFragment();
            df.setCancelable(false);
            mActivity.showDialogFragment(df, FRAGMENT_TAG_GET_USER_LISTS);
        }

    }

    private static class SearchUsersTask extends AsyncTask<Object, Object, SingleResponse<List<ParcelableUser>>> {

        private static final String FRAGMENT_TAG_SEARCH_USERS = "search_users";
        private final UserListSelectorActivity mActivity;

        private final long mAccountId;
        private final String mName;

        SearchUsersTask(final UserListSelectorActivity activity, final long accountId, final String name) {
            mActivity = activity;
            mAccountId = accountId;
            mName = name;
        }

        @Override
        protected SingleResponse<List<ParcelableUser>> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mActivity, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final Paging paging = new Paging();
                final ResponseList<User> lists = twitter.searchUsers(mName, paging);
                final List<ParcelableUser> data = new ArrayList<>();
                for (final User item : lists) {
                    data.add(new ParcelableUser(item, mAccountId));
                }
                return SingleResponse.getInstance(data);
            } catch (final TwitterException e) {
                e.printStackTrace();
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<List<ParcelableUser>> result) {
            mActivity.dismissDialogFragment(FRAGMENT_TAG_SEARCH_USERS);
            if (result.getData() != null) {
                mActivity.setUsersData(result.getData());
            }
        }

        @Override
        protected void onPreExecute() {
            final SupportProgressDialogFragment df = new SupportProgressDialogFragment();
            df.setCancelable(false);
            mActivity.showDialogFragment(df, FRAGMENT_TAG_SEARCH_USERS);
        }

    }


}
