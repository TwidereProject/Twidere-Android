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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;

import com.squareup.otto.Subscribe;

import org.mariotaku.twidere.adapter.ParcelableUsersAdapter;
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition;
import org.mariotaku.twidere.adapter.iface.IUsersAdapter;
import org.mariotaku.twidere.adapter.iface.IUsersAdapter.UserAdapterListener;
import org.mariotaku.twidere.loader.iface.IExtendedLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.FriendshipTaskEvent;
import org.mariotaku.twidere.model.util.UserKeyUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.LinkCreator;
import org.mariotaku.twidere.util.ParcelUtils;
import org.mariotaku.twidere.util.RecyclerViewNavigationHelper;
import org.mariotaku.twidere.view.holder.UserViewHolder;

import java.util.List;

public abstract class ParcelableUsersFragment extends AbsContentListRecyclerViewFragment<ParcelableUsersAdapter>
        implements LoaderCallbacks<List<ParcelableUser>>, UserAdapterListener, KeyboardShortcutCallback,
        IUsersAdapter.FollowClickListener {

    @NonNull
    private final Object mUsersBusCallback;

    private RecyclerViewNavigationHelper mNavigationHelper;

    protected ParcelableUsersFragment() {
        mUsersBusCallback = createMessageBusCallback();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ParcelableUsersAdapter adapter = getAdapter();
        final RecyclerView recyclerView = getRecyclerView();
        final LinearLayoutManager layoutManager = getLayoutManager();
        adapter.setUserAdapterListener(this);

        mNavigationHelper = new RecyclerViewNavigationHelper(recyclerView, layoutManager, adapter,
                this);
        final Bundle loaderArgs = new Bundle(getArguments());
        loaderArgs.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().initLoader(0, loaderArgs, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(mUsersBusCallback);
    }

    @Override
    public void onStop() {
        mBus.unregister(mUsersBusCallback);
        super.onStop();
    }

    @Override
    public boolean isRefreshing() {
        if (getContext() == null || isDetached()) return false;
        final LoaderManager lm = getLoaderManager();
        return lm.hasRunningLoaders();
    }

    @NonNull
    @Override
    protected ParcelableUsersAdapter onCreateAdapter(Context context, boolean compact) {
        final ParcelableUsersAdapter adapter = new ParcelableUsersAdapter(context);
        adapter.setFollowClickListener(this);
        return adapter;
    }

    @NonNull
    @Override
    public ParcelableUsersAdapter getAdapter() {
        return super.getAdapter();
    }

    @Override
    public void onLoadFinished(Loader<List<ParcelableUser>> loader, List<ParcelableUser> data) {
        final ParcelableUsersAdapter adapter = getAdapter();
        adapter.setData(data);
        if (!(loader instanceof IExtendedLoader) || ((IExtendedLoader) loader).isFromUser()) {
            adapter.setLoadMoreSupportedPosition(hasMoreData(data) ? IndicatorPosition.END : IndicatorPosition.NONE);
            setRefreshEnabled(true);
        }
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
        showContent();
        setRefreshEnabled(true);
        setRefreshing(false);
        setLoadMoreIndicatorPosition(IndicatorPosition.NONE);
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        return mNavigationHelper.handleKeyboardShortcutSingle(handler, keyCode, event, metaState);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event, int metaState) {
        return mNavigationHelper.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        return mNavigationHelper.isKeyboardShortcutHandled(handler, keyCode, event, metaState);
    }

    @Override
    public final Loader<List<ParcelableUser>> onCreateLoader(int id, Bundle args) {
        final boolean fromUser = args.getBoolean(EXTRA_FROM_USER);
        args.remove(EXTRA_FROM_USER);
        return onCreateUsersLoader(getActivity(), args, fromUser);
    }

    @Override
    public void onLoaderReset(Loader<List<ParcelableUser>> loader) {
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
    }

    @Override
    public void onUserClick(UserViewHolder holder, int position) {
        final ParcelableUser user = getAdapter().getUser(position);
        final FragmentActivity activity = getActivity();
        if (UserKeyUtils.isSameHost(user.account_key, user.key)) {
            IntentUtils.openUserProfile(activity, user, null, true, getUserReferral());
        } else if (user.extras != null && user.extras.statusnet_profile_url != null) {
            final Uri uri = Uri.parse(user.extras.statusnet_profile_url);
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else {
            final Uri uri = LinkCreator.getTwitterUserLink(user.screen_name);
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    @Override
    public void onFollowClicked(UserViewHolder holder, int position) {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        final ParcelableUsersAdapter adapter = getAdapter();
        final ParcelableUser user = adapter.getUser(position);
        if (user == null || twitter == null) return;
        if (twitter.isUpdatingRelationship(user.account_key, user.key)) return;
        if (user.is_following) {
            DestroyFriendshipDialogFragment.show(getFragmentManager(), user);
        } else {
            twitter.createFriendshipAsync(user.account_key, user.key);
        }
    }

    @UserFragment.Referral
    protected String getUserReferral() {
        return null;
    }

    @Override
    public boolean onUserLongClick(UserViewHolder holder, int position) {
        return true;
    }

    protected abstract Loader<List<ParcelableUser>> onCreateUsersLoader(final Context context,
                                                                        @NonNull final Bundle args,
                                                                        final boolean fromUser);

    @Override
    protected void setupRecyclerView(Context context, boolean compact) {
        super.setupRecyclerView(context, true);
    }

    private int findPosition(ParcelableUsersAdapter adapter, UserKey accountKey, UserKey userKey) {
        return adapter.findPosition(accountKey, userKey);
    }

    protected boolean shouldRemoveUser(int position, FriendshipTaskEvent event) {
        return false;
    }

    protected boolean hasMoreData(List<ParcelableUser> data) {
        return data == null || !data.isEmpty();
    }

    protected void removeUsers(String... ids) {
        //TODO remove from adapter
    }

    public final List<ParcelableUser> getData() {
        return getAdapter().getData();
    }

    @NonNull
    protected Object createMessageBusCallback() {
        return new UsersBusCallback();
    }

    protected class UsersBusCallback {

        @Subscribe
        public void onFriendshipTaskEvent(FriendshipTaskEvent event) {
            final ParcelableUsersAdapter adapter = getAdapter();
            final int position = findPosition(adapter, event.getAccountKey(), event.getUserKey());
            final List<ParcelableUser> data = adapter.getData();
            if (position < 0 || position >= data.size()) return;
            if (shouldRemoveUser(position, event)) {
                data.remove(position);
                adapter.notifyItemRemoved(position);
            } else {
                ParcelableUser adapterUser = data.get(position);
                ParcelableUser eventUser = event.getUser();
                if (eventUser != null) {
                    if (adapterUser.account_key.equals(eventUser.account_key)) {
                        ParcelableUser clone = ParcelUtils.clone(eventUser);
                        clone.position = adapterUser.position;
                        data.set(position, clone);
                    }
                }
                adapter.notifyItemChanged(position);
            }
        }

    }
}
