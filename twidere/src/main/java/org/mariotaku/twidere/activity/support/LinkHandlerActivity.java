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

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.createFragmentForIntent;
import static org.mariotaku.twidere.util.Utils.matchLinkId;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.iface.IBasePullToRefreshFragment;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.util.FlymeUtils;
import org.mariotaku.twidere.util.MultiSelectEventHandler;

public class LinkHandlerActivity extends BaseSupportActivity implements OnClickListener, OnLongClickListener {

	private MultiSelectEventHandler mMultiSelectHandler;

	private ActionBar mActionBar;

	private boolean mFinishOnly;

	private View mGoTopView;
	private TextView mTitleView, mSubtitleView;

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.go_top: {
				final Fragment fragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
				if (fragment instanceof RefreshScrollTopInterface) {
					((RefreshScrollTopInterface) fragment).scrollToStart();
				} else if (fragment instanceof ListFragment) {
					((ListFragment) fragment).setSelection(0);
				}
				break;
			}
		}
	}

	@Override
	public boolean onLongClick(final View v) {
		switch (v.getId()) {
			case R.id.go_top: {
				final Fragment fragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
				if (fragment instanceof RefreshScrollTopInterface) {
					((RefreshScrollTopInterface) fragment).triggerRefresh();
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				if (mFinishOnly) {
					finish();
				} else {
					navigateUpFromSameTask();
				}
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	public void setSubtitle(final CharSequence subtitle) {
		mSubtitleView.setVisibility(isEmpty(subtitle) ? View.GONE : View.VISIBLE);
		mSubtitleView.setText(subtitle);
	}

	@Override
	protected IBasePullToRefreshFragment getCurrentPullToRefreshFragment() {
		final Fragment fragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
		if (fragment instanceof IBasePullToRefreshFragment)
			return (IBasePullToRefreshFragment) fragment;
		else if (fragment instanceof SupportFragmentCallback) {
			final Fragment curr = ((SupportFragmentCallback) fragment).getCurrentVisibleFragment();
			if (curr instanceof IBasePullToRefreshFragment) return (IBasePullToRefreshFragment) curr;
		}
		return null;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		mMultiSelectHandler = new MultiSelectEventHandler(this);
		mMultiSelectHandler.dispatchOnCreate();
		final Intent intent = getIntent();
		final Uri data = intent.getData();
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setUiOptions(getWindow(), data);
		super.onCreate(savedInstanceState);
		mActionBar = getActionBar();
		mActionBar.setDisplayShowTitleEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(false);
		mActionBar.setCustomView(R.layout.link_handler_actionbar);
		final View view = mActionBar.getCustomView();
		mGoTopView = view.findViewById(R.id.go_top);
		mTitleView = (TextView) view.findViewById(R.id.actionbar_title);
		mSubtitleView = (TextView) view.findViewById(R.id.actionbar_subtitle);
		mGoTopView.setOnClickListener(this);
		mGoTopView.setOnLongClickListener(this);
		setProgressBarIndeterminateVisibility(false);
		if (data == null || !showFragment(data)) {
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		mMultiSelectHandler.dispatchOnStart();
	}

	@Override
	protected void onStop() {
		mMultiSelectHandler.dispatchOnStop();
		super.onStop();
	}

	@Override
	protected void onTitleChanged(final CharSequence title, final int color) {
		super.onTitleChanged(title, color);
		mTitleView.setText(title);
	}

	private void setUiOptions(final Window window, final Uri data) {
		if (FlymeUtils.hasSmartBar()) {
			window.setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
		} else {
			window.setUiOptions(0);
		}
	}

	private boolean showFragment(final Uri uri) {
		final Intent intent = getIntent();
		intent.setExtrasClassLoader(getClassLoader());
		final Fragment fragment = createFragmentForIntent(this, intent);
		if (uri == null || fragment == null) return false;
		switch (matchLinkId(uri)) {
			case LINK_ID_STATUS: {
				setTitle(R.string.status);
				break;
			}
			case LINK_ID_USER: {
				setTitle(R.string.user);
				break;
			}
			case LINK_ID_USER_TIMELINE: {
				setTitle(R.string.statuses);
				break;
			}
			case LINK_ID_USER_FAVORITES: {
				setTitle(R.string.favorites);
				break;
			}
			case LINK_ID_USER_FOLLOWERS: {
				setTitle(R.string.followers);
				break;
			}
			case LINK_ID_USER_FRIENDS: {
				setTitle(R.string.action_following);
				break;
			}
			case LINK_ID_USER_BLOCKS: {
				setTitle(R.string.blocked_users);
				break;
			}
			case LINK_ID_MUTES_USERS: {
				setTitle(R.string.twitter_muted_users);
				break;
			}
			case LINK_ID_DIRECT_MESSAGES_CONVERSATION: {
				setTitle(R.string.direct_messages);
				break;
			}
			case LINK_ID_USER_LIST: {
				setTitle(R.string.user_list);
				break;
			}
			case LINK_ID_USER_LISTS: {
				setTitle(R.string.user_lists);
				break;
			}
			case LINK_ID_USER_LIST_TIMELINE: {
				setTitle(R.string.list_timeline);
				break;
			}
			case LINK_ID_USER_LIST_MEMBERS: {
				setTitle(R.string.list_members);
				break;
			}
			case LINK_ID_USER_LIST_SUBSCRIBERS: {
				setTitle(R.string.list_subscribers);
				break;
			}
			case LINK_ID_USER_LIST_MEMBERSHIPS: {
				setTitle(R.string.lists_following_user);
				break;
			}
			case LINK_ID_SAVED_SEARCHES: {
				setTitle(R.string.saved_searches);
				break;
			}
			case LINK_ID_USER_MENTIONS: {
				setTitle(R.string.user_mentions);
				break;
			}
			case LINK_ID_INCOMING_FRIENDSHIPS: {
				setTitle(R.string.incoming_friendships);
				break;
			}
			case LINK_ID_USERS: {
				setTitle(R.string.users);
				break;
			}
			case LINK_ID_STATUSES: {
				setTitle(R.string.statuses);
				break;
			}
			case LINK_ID_STATUS_RETWEETERS: {
				setTitle(R.string.users_retweeted_this);
				break;
			}
			case LINK_ID_STATUS_FAVORITERS: {
				setTitle(R.string.users_retweeted_this);
				break;
			}
			case LINK_ID_STATUS_REPLIES: {
				setTitle(R.string.view_replies);
				break;
			}
			case LINK_ID_SEARCH: {
				setTitle(android.R.string.search_go);
				setSubtitle(uri.getQueryParameter(QUERY_PARAM_QUERY));
				break;
			}
			default: {
				return false;
			}
		}
		mFinishOnly = Boolean.parseBoolean(uri.getQueryParameter(QUERY_PARAM_FINISH_ONLY));
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(android.R.id.content, fragment);
		ft.commit();
		return true;
	}

}
