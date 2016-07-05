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

package org.mariotaku.twidere.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.http.HttpResponseCode;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.microblog.library.twitter.model.UserList;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.SimpleParcelableUserListsAdapter;
import org.mariotaku.twidere.adapter.SimpleParcelableUsersAdapter;
import org.mariotaku.twidere.adapter.UserAutoCompleteAdapter;
import org.mariotaku.twidere.fragment.CreateUserListDialogFragment;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.UserListCreatedEvent;
import org.mariotaku.twidere.model.util.ParcelableUserListUtils;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.ParseUtils;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.DataStoreUtils.getAccountScreenName;

public class UserListSelectorActivity extends BaseActivity implements OnClickListener,
        OnItemClickListener {

    private AutoCompleteTextView mEditScreenName;
    private ListView mUserListsListView, mUsersListView;
    private SimpleParcelableUserListsAdapter mUserListsAdapter;
    private SimpleParcelableUsersAdapter mUsersAdapter;
    private View mUsersListContainer, mUserListsContainer, mCreateUserListContainer;

    private String mScreenName;

    private Runnable mResumeFragmentRunnable;
    private boolean mFragmentsResumed;
    private View mScreenNameConfirm, mCreateList;

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.screen_name_confirm: {
                final String screen_name = ParseUtils.parseString(mEditScreenName.getText());
                if (isEmpty(screen_name)) return;
                searchUser(screen_name);
                break;
            }
            case R.id.create_list: {
                final DialogFragment f = new CreateUserListDialogFragment();
                final Bundle args = new Bundle();
                args.putParcelable(EXTRA_ACCOUNT_KEY, getAccountKey());
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
        mScreenNameConfirm = findViewById(R.id.screen_name_confirm);
        mCreateList = findViewById(R.id.create_list);
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
        if (!intent.hasExtra(EXTRA_ACCOUNT_KEY)) {
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
        final UserAutoCompleteAdapter adapter = new UserAutoCompleteAdapter(this);
        adapter.setAccountKey(getAccountKey());
        mEditScreenName.setAdapter(adapter);
        mEditScreenName.setText(mScreenName);
        mUserListsListView.setAdapter(mUserListsAdapter = new SimpleParcelableUserListsAdapter(this));
        mUsersListView.setAdapter(mUsersAdapter = new SimpleParcelableUsersAdapter(this));
        mUserListsListView.setOnItemClickListener(this);
        mUsersListView.setOnItemClickListener(this);
        mScreenNameConfirm.setOnClickListener(this);
        mCreateList.setOnClickListener(this);
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
        bus.register(this);
    }

    @Override
    protected void onStop() {
        bus.unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onUserListCreated(UserListCreatedEvent event) {
        getUserLists(mScreenName);
    }

    private UserKey getAccountKey() {
        return getIntent().getParcelableExtra(EXTRA_ACCOUNT_KEY);
    }

    private void getUserLists(final String screenName) {
        if (screenName == null) return;
        mScreenName = screenName;
        final GetUserListsTask task = new GetUserListsTask(this, getAccountKey(), screenName);
        AsyncTaskUtils.executeTask(task);
    }

    private boolean isSelectingUser() {
        return INTENT_ACTION_SELECT_USER.equals(getIntent().getAction());
    }

    private void searchUser(final String name) {
        final SearchUsersTask task = new SearchUsersTask(this, getAccountKey(), name);
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
        private final UserKey mAccountKey;
        private final String mScreenName;

        GetUserListsTask(final UserListSelectorActivity activity, final UserKey accountKey,
                         final String screenName) {
            mActivity = activity;
            mAccountKey = accountKey;
            mScreenName = screenName;
        }

        @Override
        protected SingleResponse<List<ParcelableUserList>> doInBackground(final Object... params) {
            final MicroBlog twitter = MicroBlogAPIFactory.getInstance(mActivity, mAccountKey, false);
            if (twitter == null) return SingleResponse.Companion.getInstance();
            try {
                final ResponseList<UserList> lists = twitter.getUserLists(mScreenName, true);
                final List<ParcelableUserList> data = new ArrayList<>();
                boolean isMyAccount = mScreenName.equalsIgnoreCase(getAccountScreenName(mActivity,
                        mAccountKey));
                for (final UserList item : lists) {
                    final User user = item.getUser();
                    if (user != null && mScreenName.equalsIgnoreCase(user.getScreenName())) {
                        if (!isMyAccount && TextUtils.equals(user.getId(), mAccountKey.getId())) {
                            isMyAccount = true;
                        }
                        data.add(ParcelableUserListUtils.from(item, mAccountKey));
                    }
                }
                final SingleResponse<List<ParcelableUserList>> result = SingleResponse.Companion.getInstance(data);
                result.getExtras().putBoolean(EXTRA_IS_MY_ACCOUNT, isMyAccount);
                return result;
            } catch (final MicroBlogException e) {
                Log.w(LOGTAG, e);
                return SingleResponse.Companion.getInstance(e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<List<ParcelableUserList>> result) {
            mActivity.dismissDialogFragment(FRAGMENT_TAG_GET_USER_LISTS);
            if (result.getData() != null) {
                mActivity.setUserListsData(result.getData(), result.getExtras().getBoolean(EXTRA_IS_MY_ACCOUNT));
            } else if (result.getException() instanceof MicroBlogException) {
                final MicroBlogException te = (MicroBlogException) result.getException();
                if (te.getStatusCode() == HttpResponseCode.NOT_FOUND) {
                    mActivity.searchUser(mScreenName);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            final ProgressDialogFragment df = new ProgressDialogFragment();
            df.setCancelable(false);
            mActivity.showDialogFragment(df, FRAGMENT_TAG_GET_USER_LISTS);
        }

    }

    private static class SearchUsersTask extends AsyncTask<Object, Object, SingleResponse<List<ParcelableUser>>> {

        private static final String FRAGMENT_TAG_SEARCH_USERS = "search_users";
        private final UserListSelectorActivity mActivity;

        private final UserKey mAccountKey;
        private final String mName;

        SearchUsersTask(final UserListSelectorActivity activity, final UserKey accountKey,
                        final String name) {
            mActivity = activity;
            mAccountKey = accountKey;
            mName = name;
        }

        @Override
        protected SingleResponse<List<ParcelableUser>> doInBackground(final Object... params) {
            final MicroBlog twitter = MicroBlogAPIFactory.getInstance(mActivity, mAccountKey, false);
            if (twitter == null) return SingleResponse.Companion.getInstance();
            try {
                final Paging paging = new Paging();
                final ResponseList<User> lists = twitter.searchUsers(mName, paging);
                final List<ParcelableUser> data = new ArrayList<>();
                for (final User item : lists) {
                    data.add(ParcelableUserUtils.fromUser(item, mAccountKey));
                }
                return SingleResponse.Companion.getInstance(data);
            } catch (final MicroBlogException e) {
                Log.w(LOGTAG, e);
                return SingleResponse.Companion.getInstance(e);
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
            final ProgressDialogFragment df = new ProgressDialogFragment();
            df.setCancelable(false);
            mActivity.showDialogFragment(df, FRAGMENT_TAG_SEARCH_USERS);
        }

    }


}
