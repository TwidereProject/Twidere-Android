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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.ActionMenuView.OnMenuItemClickListener;
import android.support.v7.widget.FixedLinearLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ComposeActivity;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.activity.QuickSearchBarActivity;
import org.mariotaku.twidere.activity.SettingsActivity;
import org.mariotaku.twidere.annotation.CustomTabType;
import org.mariotaku.twidere.menu.support.AccountToggleProvider;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableAccountUtils;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.CompareUtils;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.IntentUtils;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MenuUtils;
import org.mariotaku.twidere.util.TransitionUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.content.SupportFragmentReloadCursorObserver;
import org.mariotaku.twidere.view.ShapedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountsDashboardFragment extends BaseSupportFragment implements LoaderCallbacks<Cursor>,
        OnSharedPreferenceChangeListener, OnClickListener, KeyboardShortcutCallback,
        NavigationView.OnNavigationItemSelectedListener {

    private final Rect mSystemWindowsInsets = new Rect();
    private ContentResolver mResolver;

    private AccountSelectorAdapter mAccountsAdapter;

    private NavigationView mNavigationView;
    private View mAccountSelectorView;
    private RecyclerView mAccountsSelector;
    private ViewSwitcher mAccountProfileBannerView;
    private ImageView mFloatingProfileImageSnapshotView;
    private ShapedImageView mAccountProfileImageView;
    private TextView mAccountProfileNameView, mAccountProfileScreenNameView;
    private ActionMenuView mAccountsToggleMenu;
    private View mAccountProfileContainer;
    private View mNoAccountContainer;

    private AccountToggleProvider mAccountActionProvider;
    private final SupportFragmentReloadCursorObserver mReloadContentObserver = new SupportFragmentReloadCursorObserver(
            this, 0, this) {
        @Override
        public void onChange(boolean selfChange, @Nullable Uri uri) {
            final ContentResolver cr = getContentResolver();
            if (cr == null) return;
            final Cursor c = cr.query(Accounts.CONTENT_URI, Accounts.COLUMNS, null, null, Accounts.SORT_POSITION);
            try {
                updateAccountProviderData(c);
            } finally {
                Utils.closeSilently(c);
            }
            super.onChange(selfChange, uri);
        }
    };
    private boolean mSwitchAccountAnimationPlaying;
    private boolean mUseStarsForLikes;
    private boolean mLoaderInitialized;

    @NonNull
    public UserKey[] getActivatedAccountIds() {
        if (mAccountActionProvider != null) {
            return mAccountActionProvider.getActivatedAccountIds();
        }
        return DataStoreUtils.getActivatedAccountKeys(getActivity());
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull final KeyboardShortcutsHandler handler,
                                                final int keyCode, @NonNull final KeyEvent event, int metaState) {
        return false;
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        return ACTION_NAVIGATION_PREVIOUS.equals(action) || ACTION_NAVIGATION_NEXT.equals(action);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull final KeyboardShortcutsHandler handler,
                                                final int keyCode, final int repeatCount,
                                                @NonNull final KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (action == null) return false;
        final int offset;
        switch (action) {
            case ACTION_NAVIGATION_PREVIOUS: {
                offset = -1;
                break;
            }
            case ACTION_NAVIGATION_NEXT: {
                offset = 1;
                break;
            }
            default: {
                return false;
            }
        }
//        final int selectedItem = mNavigationView.getSelectedItemPosition();
//        final int count = mNavigationView.getCount();
//        int resultPosition;
//        if (!mNavigationView.isFocused() || selectedItem == ListView.INVALID_POSITION) {
//            resultPosition = firstVisiblePosition;
//        } else {
//            resultPosition = selectedItem + offset;
//            while (resultPosition >= 0 && resultPosition < count && !mAdapter.isEnabled(resultPosition)) {
//                resultPosition += offset;
//            }
//        }
//        final View focusedChild = mNavigationView.getFocusedChild();
//        if (focusedChild == null) {
//            mNavigationView.requestChildFocus(mNavigationView.getChildAt(0), null);
//        }
//        if (resultPosition >= 0 && resultPosition < count) {
//            mNavigationView.setSelection(resultPosition);
//        }
        return true;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_SETTINGS: {
                if (data == null) return;
                final FragmentActivity activity = getActivity();
                if (data.getBooleanExtra(EXTRA_CHANGED, false)) {
                    activity.recreate();
                }
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDefaultAccountState();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_container: {
                final ParcelableAccount account = mAccountsAdapter.getSelectedAccount();
                if (account == null) return;
                final FragmentActivity activity = getActivity();
                if (account.account_user != null) {
                    IntentUtils.openUserProfile(activity, account.account_user, null,
                            mPreferences.getBoolean(KEY_NEW_DOCUMENT_API),
                            UserFragment.Referral.SELF_PROFILE);
                } else {
                    IntentUtils.openUserProfile(activity, account.account_key,
                            account.account_key, account.screen_name, null,
                            mPreferences.getBoolean(KEY_NEW_DOCUMENT_API),
                            UserFragment.Referral.SELF_PROFILE);
                }
                break;
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        return new CursorLoader(getActivity(), Accounts.CONTENT_URI, Accounts.COLUMNS, null, null, Accounts.SORT_POSITION);
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        updateAccountProviderData(data);
    }

    private void updateAccountProviderData(@Nullable final Cursor cursor) {
        if (cursor == null) return;
        final Menu menu = mAccountsToggleMenu.getMenu();
        mAccountActionProvider = (AccountToggleProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.select_account));
        final ParcelableAccount[] accounts = ParcelableAccountUtils.getAccounts(cursor);
        if (accounts.length > 0) {
            mNoAccountContainer.setVisibility(View.GONE);
            mAccountProfileContainer.setVisibility(View.VISIBLE);
        } else {
            mNoAccountContainer.setVisibility(View.VISIBLE);
            mAccountProfileContainer.setVisibility(View.INVISIBLE);
        }
        UserKey defaultId = null;
        for (ParcelableAccount account : accounts) {
            if (account.is_activated) {
                defaultId = account.account_key;
                break;
            }
        }
        mUseStarsForLikes = mPreferences.getBoolean(KEY_I_WANT_MY_STARS_BACK);

        mAccountsAdapter.setAccounts(accounts);
        UserKey accountKey = UserKey.valueOf(mPreferences.getString(KEY_DEFAULT_ACCOUNT_KEY, null));
        if (accountKey == null) {
            accountKey = defaultId;
        }
        ParcelableAccount selectedAccount = null;
        for (ParcelableAccount account : accounts) {
            if (account.account_key.maybeEquals(accountKey)) {
                selectedAccount = account;
                break;
            }
        }
        mAccountsAdapter.setSelectedAccount(selectedAccount);

        if (mAccountActionProvider != null) {
            mAccountActionProvider.setExclusive(false);
            mAccountActionProvider.setAccounts(accounts);
        }
        updateAccountActions();
        ParcelableAccount currentAccount = mAccountsAdapter.getSelectedAccount();
        if (currentAccount != null) {
            displayAccountBanner(currentAccount);
            displayCurrentAccount(null);
        }
        updateDefaultAccountState();
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
    }


    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (KEY_DEFAULT_ACCOUNT_KEY.equals(key)) {
            updateDefaultAccountState();
        }
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        mSystemWindowsInsets.set(insets);
        updateSystemWindowsInsets();
    }

    private void updateSystemWindowsInsets() {
        if (mAccountProfileContainer == null) return;
        final Rect insets = mSystemWindowsInsets;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mResolver = getContentResolver();
        final View view = getView();
        assert view != null;
        final Context context = view.getContext();
        final LayoutInflater inflater = getLayoutInflater(savedInstanceState);
        mAccountsAdapter = new AccountSelectorAdapter(inflater, this);
        final LinearLayoutManager layoutManager = new FixedLinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.setStackFromEnd(true);
        mAccountsSelector.setLayoutManager(layoutManager);
        mAccountsSelector.setAdapter(mAccountsAdapter);
        mAccountsSelector.setItemAnimator(null);
        final SupportMenuInflater menuInflater = new SupportMenuInflater(context);
        menuInflater.inflate(R.menu.action_dashboard_timeline_toggle, mAccountsToggleMenu.getMenu());
        mAccountsToggleMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getGroupId() != AccountToggleProvider.MENU_GROUP) {
                    switch (item.getItemId()) {
                        case R.id.compose: {
                            final ParcelableAccount account = mAccountsAdapter.getSelectedAccount();
                            if (account == null) return true;
                            final Intent composeIntent = new Intent(INTENT_ACTION_COMPOSE);
                            composeIntent.setClass(getActivity(), ComposeActivity.class);
                            composeIntent.putExtra(EXTRA_ACCOUNT_KEY, account.account_key);
                            startActivity(composeIntent);
                            return true;
                        }
                    }
                    return false;
                }
                final ParcelableAccount[] accounts = mAccountActionProvider.getAccounts();
                final ParcelableAccount account = accounts[item.getOrder()];
                final ContentValues values = new ContentValues();
                final boolean newActivated = !account.is_activated;
                mAccountActionProvider.setAccountActivated(account.account_key, newActivated);
                values.put(Accounts.IS_ACTIVATED, newActivated);
                final String where = Expression.equalsArgs(Accounts.ACCOUNT_KEY).getSQL();
                final String[] whereArgs = {account.account_key.toString()};
                mResolver.update(Accounts.CONTENT_URI, values, where, whereArgs);
                return true;
            }
        });

        mAccountProfileContainer.setOnClickListener(this);

        mAccountProfileBannerView.setInAnimation(getContext(), android.R.anim.fade_in);
        mAccountProfileBannerView.setOutAnimation(getContext(), android.R.anim.fade_out);
        mAccountProfileBannerView.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                return inflater.inflate(R.layout.layout_account_dashboard_profile_image,
                        mAccountProfileBannerView, false);
            }
        });

        mNavigationView.setNavigationItemSelectedListener(this);
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        loadAccounts();

        updateSystemWindowsInsets();
    }

    public void loadAccounts() {
        if (!mLoaderInitialized) {
            mLoaderInitialized = true;
            getLoaderManager().initLoader(0, null, this);
        } else {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accounts_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNavigationView = (NavigationView) view.findViewById(R.id.navigation_view);
        mAccountSelectorView = mNavigationView.getHeaderView(0);
        mAccountsSelector = (RecyclerView) mAccountSelectorView.findViewById(R.id.other_accounts_list);
        mAccountProfileContainer = mAccountSelectorView.findViewById(R.id.profile_container);
        mNoAccountContainer = mAccountSelectorView.findViewById(R.id.no_account_container);
        mAccountProfileImageView = (ShapedImageView) mAccountSelectorView.findViewById(R.id.profile_image);
        mAccountProfileBannerView = (ViewSwitcher) mAccountSelectorView.findViewById(R.id.account_profile_banner);
        mFloatingProfileImageSnapshotView = (ImageView) mAccountSelectorView.findViewById(R.id.floating_profile_image_snapshot);
        mAccountProfileNameView = (TextView) mAccountSelectorView.findViewById(R.id.name);
        mAccountProfileScreenNameView = (TextView) mAccountSelectorView.findViewById(R.id.screen_name);
        mAccountsToggleMenu = (ActionMenuView) mAccountSelectorView.findViewById(R.id.account_dashboard_menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        final ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(Accounts.CONTENT_URI, true, mReloadContentObserver);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onStop() {
        final ContentResolver resolver = getContentResolver();
        resolver.unregisterContentObserver(mReloadContentObserver);
        super.onStop();
    }

    void updateAccountActions() {
        final HomeActivity activity = (HomeActivity) getActivity();
        if (activity == null) return;
        final List<SupportTabSpec> tabs = activity.getTabs();
        final ParcelableAccount account = mAccountsAdapter.getSelectedAccount();
        if (account == null) return;
        boolean hasDmTab = false, hasInteractionsTab = false;
        for (SupportTabSpec tab : tabs) {
            if (tab.type == null) continue;
            switch (tab.type) {
                case CustomTabType.DIRECT_MESSAGES: {
                    if (!hasDmTab) {
                        hasDmTab = hasAccountInTab(tab, account.account_key, account.is_activated);
                    }
                    break;
                }
                case CustomTabType.NOTIFICATIONS_TIMELINE: {
                    if (!hasInteractionsTab) {
                        hasInteractionsTab = hasAccountInTab(tab, account.account_key, account.is_activated);
                    }
                    break;
                }
            }
        }
        final Menu menu = mNavigationView.getMenu();
        MenuUtils.setMenuItemAvailability(menu, R.id.interactions, !hasInteractionsTab);
        MenuUtils.setMenuItemAvailability(menu, R.id.messages, !hasDmTab);

        if (mUseStarsForLikes) {
            MenuUtils.setMenuItemTitle(menu, R.id.favorites, R.string.favorites);
            MenuUtils.setMenuItemIcon(menu, R.id.favorites, R.drawable.ic_action_star);
        } else {
            MenuUtils.setMenuItemTitle(menu, R.id.favorites, R.string.likes);
            MenuUtils.setMenuItemIcon(menu, R.id.favorites, R.drawable.ic_action_heart);
        }
        boolean hasLists = false, hasGroups = false, hasPublicTimeline = false;
        switch (ParcelableAccountUtils.getAccountType(account)) {
            case ParcelableAccount.Type.TWITTER: {
                hasLists = true;
                break;
            }
            case ParcelableAccount.Type.STATUSNET: {
                hasGroups = true;
                break;
            }
            case ParcelableAccount.Type.FANFOU: {
                hasPublicTimeline = true;
                break;
            }
        }
        MenuUtils.setMenuItemAvailability(menu, R.id.groups, hasGroups);
        MenuUtils.setMenuItemAvailability(menu, R.id.lists, hasLists);
        MenuUtils.setMenuItemAvailability(menu, R.id.public_timeline, hasPublicTimeline);
    }

    private boolean hasAccountInTab(SupportTabSpec tab, UserKey accountId, boolean isActivated) {
        if (tab.args == null) return false;
        final UserKey[] accountKeys = Utils.getAccountKeys(getContext(), tab.args);
        if (accountKeys == null) return isActivated;
        return ArrayUtils.contains(accountKeys, accountId);
    }

    private void closeAccountsDrawer() {
        final Activity activity = getActivity();
        if (activity instanceof HomeActivity) {
            ((HomeActivity) activity).closeAccountsDrawer();
        }
    }

    private void getLocationOnScreen(View view, RectF rectF) {
        final int[] location = new int[2];
        view.getLocationOnScreen(location);
        rectF.set(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
    }

    private void onAccountSelected(AccountProfileImageViewHolder holder, @NonNull final ParcelableAccount account) {
        if (mSwitchAccountAnimationPlaying) return;
        final ImageView snapshotView = mFloatingProfileImageSnapshotView;
        final ShapedImageView profileImageView = mAccountProfileImageView;
        final ShapedImageView clickedImageView = holder.getIconView();

        // Reset snapshot view position
        snapshotView.setPivotX(0);
        snapshotView.setPivotY(0);
        snapshotView.setTranslationX(0);
        snapshotView.setTranslationY(0);

        final Matrix matrix = new Matrix();
        final RectF sourceBounds = new RectF(), destBounds = new RectF(), snapshotBounds = new RectF();
        getLocationOnScreen(clickedImageView, sourceBounds);
        getLocationOnScreen(profileImageView, destBounds);
        getLocationOnScreen(snapshotView, snapshotBounds);
        final float finalScale = destBounds.width() / sourceBounds.width();
        final Bitmap snapshotBitmap = TransitionUtils.createViewBitmap(clickedImageView, matrix,
                new RectF(0, 0, sourceBounds.width(), sourceBounds.height()));
        final ViewGroup.LayoutParams lp = snapshotView.getLayoutParams();
        lp.width = clickedImageView.getWidth();
        lp.height = clickedImageView.getHeight();
        snapshotView.setLayoutParams(lp);
        // Copied from MaterialNavigationDrawer: https://github.com/madcyph3r/AdvancedMaterialDrawer/
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(snapshotView, View.TRANSLATION_X,
                sourceBounds.left - snapshotBounds.left, destBounds.left - snapshotBounds.left))
                .with(ObjectAnimator.ofFloat(snapshotView, View.TRANSLATION_Y,
                        sourceBounds.top - snapshotBounds.top, destBounds.top - snapshotBounds.top))
                .with(ObjectAnimator.ofFloat(snapshotView, View.SCALE_X, 1, finalScale))
                .with(ObjectAnimator.ofFloat(snapshotView, View.SCALE_Y, 1, finalScale))
                .with(ObjectAnimator.ofFloat(profileImageView, View.ALPHA, 1, 0))
                .with(ObjectAnimator.ofFloat(clickedImageView, View.SCALE_X, 0, 1))
                .with(ObjectAnimator.ofFloat(clickedImageView, View.SCALE_Y, 0, 1));
        final long animationTransition = 400;
        set.setDuration(animationTransition);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListener() {

            private Drawable clickedDrawable;
            private int[] clickedColors;

            @Override
            public void onAnimationStart(Animator animation) {
                snapshotView.setVisibility(View.VISIBLE);
                snapshotView.setImageBitmap(snapshotBitmap);
                final Drawable profileDrawable = profileImageView.getDrawable();
                clickedDrawable = clickedImageView.getDrawable();
                clickedColors = clickedImageView.getBorderColors();
                final ParcelableAccount oldSelectedAccount = mAccountsAdapter.getSelectedAccount();
                if (oldSelectedAccount == null) return;
                mMediaLoader.displayDashboardProfileImage(clickedImageView,
                        oldSelectedAccount, profileDrawable);
                clickedImageView.setBorderColors(profileImageView.getBorderColors());

                displayAccountBanner(account);

                mSwitchAccountAnimationPlaying = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                finishAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                finishAnimation();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

            private void finishAnimation() {
                final Editor editor = mPreferences.edit();
                editor.putString(KEY_DEFAULT_ACCOUNT_KEY, account.account_key.toString());
                editor.apply();
                mAccountsAdapter.setSelectedAccount(account);
                updateAccountActions();
                displayCurrentAccount(clickedDrawable);
                snapshotView.setVisibility(View.INVISIBLE);
                snapshotView.setImageDrawable(null);
                profileImageView.setImageDrawable(clickedDrawable);
                profileImageView.setBorderColors(clickedColors);
                profileImageView.setAlpha(1f);
                clickedImageView.setScaleX(1);
                clickedImageView.setScaleY(1);
                clickedImageView.setAlpha(1f);
                mSwitchAccountAnimationPlaying = false;
            }
        });
        set.start();

    }

    protected void displayAccountBanner(@NonNull ParcelableAccount account) {
        final int bannerWidth = mAccountProfileBannerView.getWidth();
        final Resources res = getResources();
        final int defWidth = res.getDisplayMetrics().widthPixels;
        final int width = bannerWidth > 0 ? bannerWidth : defWidth;
        final ImageView bannerView = (ImageView) mAccountProfileBannerView.getNextView();
        if (bannerView.getDrawable() == null || !CompareUtils.objectEquals(account, bannerView.getTag())) {
            mMediaLoader.displayProfileBanner(bannerView, account, width);
            bannerView.setTag(account);
        } else {
            mMediaLoader.cancelDisplayTask(bannerView);
        }
    }

    private void displayCurrentAccount(Drawable profileImageSnapshot) {
        final ParcelableAccount account = mAccountsAdapter.getSelectedAccount();
        if (account == null) {
            return;
        }
        mAccountProfileNameView.setText(account.name);
        mAccountProfileScreenNameView.setText(String.format("@%s", account.screen_name));
        mMediaLoader.displayDashboardProfileImage(mAccountProfileImageView, account,
                profileImageSnapshot);
        mAccountProfileImageView.setBorderColors(account.color);
        mAccountProfileBannerView.showNext();
    }

    private void updateDefaultAccountState() {
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        final ParcelableAccount account = mAccountsAdapter.getSelectedAccount();
        if (account == null) return false;
        switch (item.getItemId()) {
            case R.id.search: {
                final Intent intent = new Intent(getActivity(), QuickSearchBarActivity.class);
                intent.putExtra(EXTRA_ACCOUNT_KEY, account.account_key);
                startActivity(intent);
                closeAccountsDrawer();
                break;
            }
            case R.id.compose: {
                final Intent composeIntent = new Intent(INTENT_ACTION_COMPOSE);
                composeIntent.setClass(getActivity(), ComposeActivity.class);
                composeIntent.putExtra(EXTRA_ACCOUNT_KEY, account.account_key);
                startActivity(composeIntent);
                break;
            }
            case R.id.favorites: {
                IntentUtils.openUserFavorites(getActivity(), account.account_key,
                        account.account_key, account.screen_name);
                break;
            }
            case R.id.lists: {
                IntentUtils.openUserLists(getActivity(), account.account_key,
                        account.account_key, account.screen_name);
                break;
            }
            case R.id.groups: {
                IntentUtils.openUserGroups(getActivity(), account.account_key,
                        account.account_key, account.screen_name);
                break;
            }
            case R.id.public_timeline: {
                IntentUtils.openPublicTimeline(getActivity(), account.account_key);
                break;
            }
            case R.id.messages: {
                IntentUtils.openDirectMessages(getActivity(), account.account_key);
                break;
            }
            case R.id.interactions: {
                IntentUtils.openInteractions(getActivity(), account.account_key);
                break;
            }
            case R.id.edit: {
                IntentUtils.openProfileEditor(getActivity(), account.account_key);
                break;
            }
            case R.id.accounts: {
                IntentUtils.openAccountsManager(getActivity());
                closeAccountsDrawer();
                break;
            }
            case R.id.drafts: {
                IntentUtils.openDrafts(getActivity());
                closeAccountsDrawer();
                break;
            }
            case R.id.filters: {
                IntentUtils.openFilters(getActivity());
                closeAccountsDrawer();
                break;
            }
            case R.id.settings: {
                final Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(intent, REQUEST_SETTINGS);
                closeAccountsDrawer();
                break;
            }
        }
        return false;
    }

    public void setStatusBarHeight(int height) {
        final int top = Utils.getInsetsTopWithoutActionBarHeight(getActivity(), height);
        mAccountProfileContainer.setPadding(0, top, 0, 0);
    }

    static class AccountProfileImageViewHolder extends ViewHolder implements OnClickListener {

        private final AccountSelectorAdapter adapter;
        private final ShapedImageView icon;

        public AccountProfileImageViewHolder(AccountSelectorAdapter adapter, View itemView) {
            super(itemView);
            this.adapter = adapter;
            itemView.setOnClickListener(this);
            icon = (ShapedImageView) itemView.findViewById(android.R.id.icon);
        }

        public ShapedImageView getIconView() {
            return icon;
        }

        @Override
        public void onClick(View v) {
            adapter.dispatchItemSelected(this);
        }
    }

    private static class AccountSelectorAdapter extends Adapter<AccountProfileImageViewHolder> {

        private final LayoutInflater mInflater;
        private final MediaLoaderWrapper mImageLoader;
        private final AccountsDashboardFragment mFragment;
        private ParcelableAccount[] mInternalAccounts;

        AccountSelectorAdapter(LayoutInflater inflater, AccountsDashboardFragment fragment) {
            mInflater = inflater;
            mImageLoader = fragment.mMediaLoader;
            mFragment = fragment;
            setHasStableIds(true);
        }

        private static int indexOfAccount(List<ParcelableAccount> accounts, UserKey accountId) {
            for (int i = 0, j = accounts.size(); i < j; i++) {
                if (accounts.get(i).account_key.equals(accountId)) return i;
            }
            return -1;
        }

        public ParcelableAccount getAdapterAccount(int adapterPosition) {
            if (mInternalAccounts == null || mInternalAccounts.length < 1) {
                return null;
            }
            return mInternalAccounts[adapterPosition + 1];
        }

        @Nullable
        public ParcelableAccount getSelectedAccount() {
            if (mInternalAccounts == null || mInternalAccounts.length == 0) {
                return null;
            }
            return mInternalAccounts[0];
        }

        @Nullable
        public UserKey getSelectedAccountKey() {
            final ParcelableAccount selectedAccount = getSelectedAccount();
            if (selectedAccount == null) return null;
            return selectedAccount.account_key;
        }

        public void setSelectedAccount(@Nullable ParcelableAccount account) {
            final ParcelableAccount selectedAccount = getSelectedAccount();
            if (selectedAccount == null || account == null) return;
            swap(account, selectedAccount);
        }

        @Override
        public AccountProfileImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = mInflater.inflate(R.layout.adapter_item_dashboard_account, parent, false);
            return new AccountProfileImageViewHolder(this, view);
        }

        @Override
        public void onBindViewHolder(AccountProfileImageViewHolder holder, int position) {
            final ParcelableAccount account = getAdapterAccount(position);
            mImageLoader.displayDashboardProfileImage(holder.icon, account, null);
            holder.icon.setBorderColor(account.color);
        }

        @Override
        public long getItemId(int position) {
            return getAdapterAccount(position).hashCode();
        }

        @Override
        public int getItemCount() {
            if (mInternalAccounts == null || mInternalAccounts.length == 0) return 0;
            return mInternalAccounts.length - 1;
        }

        public void setAccounts(ParcelableAccount[] accounts) {
            if (accounts != null) {
                final ParcelableAccount[] previousAccounts = mInternalAccounts;
                mInternalAccounts = new ParcelableAccount[accounts.length];
                int tempIdx = 0;
                final List<ParcelableAccount> tempList = new ArrayList<>();
                Collections.addAll(tempList, accounts);
                if (previousAccounts != null) {
                    for (ParcelableAccount previousAccount : previousAccounts) {
                        final int idx = indexOfAccount(tempList, previousAccount.account_key);
                        if (idx >= 0) {
                            mInternalAccounts[tempIdx++] = tempList.remove(idx);
                        }
                    }
                }
                for (ParcelableAccount account : tempList) {
                    mInternalAccounts[tempIdx++] = account;
                }
            } else {
                mInternalAccounts = null;
            }
            notifyDataSetChanged();
        }

        private void dispatchItemSelected(AccountProfileImageViewHolder holder) {
            mFragment.onAccountSelected(holder, getAdapterAccount(holder.getAdapterPosition()));
        }

        public ParcelableAccount[] getAccounts() {
            return mInternalAccounts;
        }

        private void swap(@NonNull ParcelableAccount from, @NonNull ParcelableAccount to) {
            int fromIdx = -1, toIdx = -1;
            for (int i = 0, j = mInternalAccounts.length; i < j; i++) {
                final ParcelableAccount account = mInternalAccounts[i];
                if (from.id == account.id) {
                    fromIdx = i;
                }
                if (to.id == account.id) {
                    toIdx = i;
                }
            }
            if (fromIdx < 0 || toIdx < 0) return;
            final ParcelableAccount temp = mInternalAccounts[toIdx];
            mInternalAccounts[toIdx] = mInternalAccounts[fromIdx];
            mInternalAccounts[fromIdx] = temp;
            notifyDataSetChanged();
        }
    }


    public static class OptionItem {

        private final int name, icon, id;

        OptionItem(final int name, final int icon, final int id) {
            this.name = name;
            this.icon = icon;
            this.id = id;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (!(obj instanceof OptionItem)) return false;
            final OptionItem other = (OptionItem) obj;
            if (icon != other.icon) return false;
            if (id != other.id) return false;
            return name == other.name;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + icon;
            result = prime * result + id;
            result = prime * result + name;
            return result;
        }

        @Override
        public String toString() {
            return "AccountOption{name=" + name + ", icon=" + icon + ", id=" + id + "}";
        }

    }

}