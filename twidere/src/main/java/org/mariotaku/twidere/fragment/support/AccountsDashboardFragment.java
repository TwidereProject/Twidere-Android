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

package org.mariotaku.twidere.fragment.support;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.FiltersActivity;
import org.mariotaku.twidere.activity.SettingsActivity;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.activity.support.AccountsManagerActivity;
import org.mariotaku.twidere.activity.support.ComposeActivity;
import org.mariotaku.twidere.activity.support.DraftsActivity;
import org.mariotaku.twidere.activity.support.HomeActivity;
import org.mariotaku.twidere.activity.support.QuickSearchBarActivity;
import org.mariotaku.twidere.activity.support.UserProfileEditorActivity;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableAccount.Indices;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.CompareUtils;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.content.SupportFragmentReloadCursorObserver;
import org.mariotaku.twidere.view.ShapedImageView;

import java.util.ArrayList;

import static org.mariotaku.twidere.util.Utils.openUserFavorites;
import static org.mariotaku.twidere.util.Utils.openUserLists;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

public class AccountsDashboardFragment extends BaseSupportListFragment implements LoaderCallbacks<Cursor>,
        OnSharedPreferenceChangeListener, OnCheckedChangeListener, ImageLoadingListener, OnClickListener {

    private final SupportFragmentReloadCursorObserver mReloadContentObserver = new SupportFragmentReloadCursorObserver(
            this, 0, this);

    private ContentResolver mResolver;
    private SharedPreferences mPreferences;
    private MergeAdapter mAdapter;

    private AccountSelectorAdapter mAccountsAdapter;
    private AccountOptionsAdapter mAccountOptionsAdapter;
    private AppMenuAdapter mAppMenuAdapter;

    private TextView mAppMenuSectionView;
    private View mAccountSelectorView;
    private RecyclerView mAccountsSelector;
    private ImageView mAccountProfileBannerView;
    private ShapedImageView mAccountProfileImageView;
    private TextView mAccountProfileNameView, mAccountProfileScreenNameView;
    private Switch mAccountsToggle;
    private View mAccountProfileContainer;

    private Context mThemedContext;
    private ImageLoaderWrapper mImageLoader;

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_SETTINGS: {
                if (data == null) return;
                final FragmentActivity activity = getActivity();
                if (data.getBooleanExtra(EXTRA_RESTART_ACTIVITY, false) && activity instanceof IThemedActivity) {
                    ((IThemedActivity) activity).restart();
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
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final ParcelableAccount account = mAccountsAdapter.getSelectedAccount();
        if (account == null) return;
        final ContentValues values = new ContentValues();
        values.put(Accounts.IS_ACTIVATED, isChecked);
        final String where = Accounts.ACCOUNT_ID + " = " + account.account_id;
        mResolver.update(Accounts.CONTENT_URI, values, where, null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_container: {
                final ParcelableAccount account = mAccountsAdapter.getSelectedAccount();
                if (account == null) return;
                final FragmentActivity activity = getActivity();
                final Bundle activityOption = Utils.makeSceneTransitionOption(activity,
                        new Pair<View, String>(mAccountProfileImageView, UserFragment.TRANSITION_NAME_PROFILE_IMAGE));
                openUserProfile(activity, account.account_id, account.account_id,
                        account.screen_name, activityOption);
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
        if (data != null && data.getCount() > 0 && mAccountsAdapter.getSelectedAccountId() <= 0) {
            data.moveToFirst();
            mAccountsAdapter.setSelectedAccountId(data.getLong(data.getColumnIndex(Accounts.ACCOUNT_ID)));
        }
        mAccountsAdapter.changeCursor(data);
        updateAccountOptionsSeparatorLabel();
        updateDefaultAccountState();
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        mAccountsAdapter.changeCursor(null);
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        final ListAdapter adapter = mAdapter.getAdapter(position);
        final Object item = mAdapter.getItem(position);
        if (adapter instanceof AccountOptionsAdapter) {
            final ParcelableAccount account = mAccountsAdapter.getSelectedAccount();
            if (account == null || !(item instanceof OptionItem)) return;
            final OptionItem option = (OptionItem) item;
            switch (option.id) {
                case MENU_SEARCH: {
//                    final FragmentActivity a = getActivity();
//                    if (a instanceof HomeActivity) {
//                        ((HomeActivity) a).openSearchView(account);
//                    } else {
//                        getActivity().onSearchRequested();
//                    }
                    final Intent intent = new Intent(getActivity(), QuickSearchBarActivity.class);
                    intent.putExtra(EXTRA_ACCOUNT_ID, account.account_id);
                    startActivity(intent);
                    closeAccountsDrawer();
                    break;
                }
                case MENU_COMPOSE: {
                    final Intent composeIntent = new Intent(INTENT_ACTION_COMPOSE);
                    composeIntent.setClass(getActivity(), ComposeActivity.class);
                    composeIntent.putExtra(EXTRA_ACCOUNT_IDS, new long[]{account.account_id});
                    startActivity(composeIntent);
                    break;
                }
                case MENU_FAVORITES: {
                    openUserFavorites(getActivity(), account.account_id, account.account_id, account.screen_name);
                    break;
                }
                case MENU_LISTS: {
                    openUserLists(getActivity(), account.account_id, account.account_id, account.screen_name);
                    break;
                }
                case MENU_EDIT: {
                    final Bundle bundle = new Bundle();
                    bundle.putLong(EXTRA_ACCOUNT_ID, account.account_id);
                    final Intent intent = new Intent(INTENT_ACTION_EDIT_USER_PROFILE);
                    intent.setClass(getActivity(), UserProfileEditorActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                }
            }
        } else if (adapter instanceof AppMenuAdapter) {
            if (!(item instanceof OptionItem)) return;
            final OptionItem option = (OptionItem) item;
            switch (option.id) {
                case MENU_ACCOUNTS: {
                    final Intent intent = new Intent(getActivity(), AccountsManagerActivity.class);
                    startActivity(intent);
                    break;
                }
                case MENU_DRAFTS: {
                    final Intent intent = new Intent(INTENT_ACTION_DRAFTS);
                    intent.setClass(getActivity(), DraftsActivity.class);
                    startActivity(intent);
                    break;
                }
                case MENU_FILTERS: {
                    final Intent intent = new Intent(getActivity(), FiltersActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    break;
                }
                case MENU_SETTINGS: {
                    final Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivityForResult(intent, REQUEST_SETTINGS);
                    break;
                }
            }
            closeAccountsDrawer();
        }
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {

    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        view.setTag(imageUri);
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {

    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (KEY_DEFAULT_ACCOUNT_ID.equals(key)) {
            updateDefaultAccountState();
        }
    }

    public void setStatusBarHeight(int height) {
        mAccountProfileContainer.setPadding(0, height, 0, 0);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        // No-op
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mResolver = getContentResolver();
        final View view = getView();
        if (view == null) throw new AssertionError();
        final Context context = view.getContext();
        mImageLoader = TwidereApplication.getInstance(context).getImageLoaderWrapper();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final ListView listView = getListView();
        mAdapter = new MergeAdapter();
        mAccountsAdapter = new AccountSelectorAdapter(context, this);
        mAccountOptionsAdapter = new AccountOptionsAdapter(context);
        mAppMenuAdapter = new AppMenuAdapter(context);
        mAppMenuSectionView = newSectionView(context, R.string.more);
        mAccountSelectorView = inflater.inflate(R.layout.header_drawer_account_selector, listView, false);
        mAccountsSelector = (RecyclerView) mAccountSelectorView.findViewById(R.id.other_accounts_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setStackFromEnd(true);
        mAccountsSelector.setLayoutManager(layoutManager);
        mAccountsSelector.setAdapter(mAccountsAdapter);
        mAccountProfileContainer = mAccountSelectorView.findViewById(R.id.profile_container);
        mAccountProfileImageView = (ShapedImageView) mAccountSelectorView.findViewById(R.id.profile_image);
        mAccountProfileBannerView = (ImageView) mAccountSelectorView.findViewById(R.id.account_profile_banner);
        mAccountProfileNameView = (TextView) mAccountSelectorView.findViewById(R.id.name);
        mAccountProfileScreenNameView = (TextView) mAccountSelectorView.findViewById(R.id.screen_name);
        mAccountsToggle = (Switch) mAccountSelectorView.findViewById(R.id.toggle);

        mAccountProfileContainer.setOnClickListener(this);

        mAccountsToggle.setOnCheckedChangeListener(this);
        mAdapter.addView(mAccountSelectorView, false);
        mAdapter.addAdapter(mAccountOptionsAdapter);
        mAdapter.addView(mAppMenuSectionView, false);
        mAdapter.addAdapter(mAppMenuAdapter);
        setListAdapter(mAdapter);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return LayoutInflater.from(getThemedContext()).inflate(R.layout.fragment_accounts_drawer, container, false);
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

    private void closeAccountsDrawer() {
        final Activity activity = getActivity();
        if (activity instanceof HomeActivity) {
            ((HomeActivity) activity).closeAccountsDrawer();
        }
    }

    private Context getThemedContext() {
        if (mThemedContext != null) return mThemedContext;
        final Context context = getActivity();
        final int themeResource = ThemeUtils.getDrawerThemeResource(context);
        return mThemedContext = new ContextThemeWrapper(context, themeResource);
    }

    private static TextView newSectionView(final Context context, final int titleRes) {
        final TextView textView = new TextView(context, null, android.R.attr.listSeparatorTextViewStyle);
        if (titleRes != 0) {
            textView.setText(titleRes);
        }
        return textView;
    }

    private void onAccountSelected(ParcelableAccount account) {
        mAccountsAdapter.setSelectedAccountId(account.account_id);
        updateAccountOptionsSeparatorLabel();
    }

    private void updateAccountOptionsSeparatorLabel() {
        final ParcelableAccount account = mAccountsAdapter.getSelectedAccount();
        if (account == null) {
            return;
        }
        mAccountProfileNameView.setText(account.name);
        mAccountProfileScreenNameView.setText("@" + account.screen_name);
        mAccountsToggle.setChecked(account.is_activated);
        mImageLoader.displayProfileImage(mAccountProfileImageView, account.profile_image_url);
        mAccountProfileImageView.setBorderColors(account.color);
        final int bannerWidth = mAccountProfileBannerView.getWidth();
        final Resources res = getResources();
        final int defWidth = res.getDisplayMetrics().widthPixels;
        final int width = bannerWidth > 0 ? bannerWidth : defWidth;
        final String bannerUrl = Utils.getBestBannerUrl(account.profile_banner_url, width);
        final ImageView bannerView = mAccountProfileBannerView;
        if (bannerView.getDrawable() == null || !CompareUtils.objectEquals(bannerUrl, bannerView.getTag())) {
            mImageLoader.displayProfileBanner(mAccountProfileBannerView, bannerUrl, this);
        }
    }

    private void updateDefaultAccountState() {
    }

    private static final class AccountOptionsAdapter extends OptionItemsAdapter {

        private static final ArrayList<OptionItem> sOptions = new ArrayList<>();

        static {
            sOptions.add(new OptionItem(android.R.string.search_go, R.drawable.ic_action_search, MENU_SEARCH));
            sOptions.add(new OptionItem(R.string.compose, R.drawable.ic_action_status_compose, MENU_COMPOSE));
            sOptions.add(new OptionItem(R.string.favorites, R.drawable.ic_action_star, MENU_FAVORITES));
            sOptions.add(new OptionItem(R.string.lists, R.drawable.ic_action_list, MENU_LISTS));
        }

        public AccountOptionsAdapter(final Context context) {
            super(context);
            clear();
            addAll(sOptions);
        }

    }

    private static final class AppMenuAdapter extends OptionItemsAdapter {

        public AppMenuAdapter(final Context context) {
            super(context);
            add(new OptionItem(R.string.accounts, R.drawable.ic_action_accounts, MENU_ACCOUNTS));
            add(new OptionItem(R.string.drafts, R.drawable.ic_action_draft, MENU_DRAFTS));
            add(new OptionItem(R.string.filters, R.drawable.ic_action_speaker_muted, MENU_FILTERS));
            add(new OptionItem(R.string.settings, R.drawable.ic_action_settings, MENU_SETTINGS));
        }

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

        @Override
        public void onClick(View v) {
            adapter.dispatchItemSelected(getPosition());
        }
    }

    private static class AccountSelectorAdapter extends Adapter<AccountProfileImageViewHolder> {

        private final LayoutInflater mInflater;
        private final ImageLoaderWrapper mImageLoader;
        private final AccountsDashboardFragment mFragment;
        private Cursor mCursor;
        private Indices mIndices;
        private long mSelectedAccountId;
        private int mSelectedAccountIndex;

        AccountSelectorAdapter(Context context, AccountsDashboardFragment fragment) {
            mInflater = LayoutInflater.from(context);
            mImageLoader = TwidereApplication.getInstance(context).getImageLoaderWrapper();
            mFragment = fragment;
        }

        public void changeCursor(Cursor cursor) {
            mCursor = cursor;
            if (cursor != null) {
                mIndices = new Indices(cursor);
            }
            updateSelectedAccountIndex();
            notifyDataSetChanged();
        }

        private void updateSelectedAccountIndex() {
            final Cursor c = mCursor;
            final Indices i = mIndices;
            mSelectedAccountIndex = -1;
            if (c != null && i != null && c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    if (c.getLong(mIndices.account_id) == mSelectedAccountId) {
                        mSelectedAccountIndex = c.getPosition();
                        break;
                    }
                    c.moveToNext();
                }
            }
        }

        public ParcelableAccount getSelectedAccount() {
            final Cursor c = mCursor;
            final Indices i = mIndices;
            if (c != null && i != null && c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    if (c.getLong(mIndices.account_id) == mSelectedAccountId)
                        return new ParcelableAccount(c, mIndices);
                    c.moveToNext();
                }
            }
            return null;
        }

        public long getSelectedAccountId() {
            return mSelectedAccountId;
        }

        public void setSelectedAccountId(long accountId) {
            mSelectedAccountId = accountId;
            updateSelectedAccountIndex();
            notifyDataSetChanged();
        }

        @Override
        public AccountProfileImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = mInflater.inflate(R.layout.adapter_item_dashboard_account, parent, false);
            return new AccountProfileImageViewHolder(this, view);
        }

        @Override
        public void onBindViewHolder(AccountProfileImageViewHolder holder, int position) {
            final Cursor c = mCursor;
            if (mSelectedAccountIndex != -1 && position >= mSelectedAccountIndex) {
                c.moveToPosition(position + 1);
            } else {
                c.moveToPosition(position);
            }
            holder.itemView.setAlpha(c.getInt(mIndices.is_activated) == 1 ? 1 : 0.5f);
            mImageLoader.displayProfileImage(holder.icon, c.getString(mIndices.profile_image_url));
            holder.icon.setBorderColor(c.getInt(mIndices.color));
        }

        @Override
        public int getItemCount() {
            if (mCursor == null) return 0;
            return Math.max(mCursor.getCount() - 1, 0);
        }

        private void dispatchItemSelected(int position) {
            final Cursor c = mCursor;
            if (mSelectedAccountIndex != -1 && position >= mSelectedAccountIndex) {
                c.moveToPosition(position + 1);
            } else {
                c.moveToPosition(position);
            }
            mFragment.onAccountSelected(new ParcelableAccount(c, mIndices));
        }
    }

    private static class OptionItem {

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
            if (name != other.name) return false;
            return true;
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

    private static abstract class OptionItemsAdapter extends ArrayAdapter<OptionItem> {

        private final int mActionIconColor;

        public OptionItemsAdapter(final Context context) {
            super(context, R.layout.list_item_menu);
            mActionIconColor = ThemeUtils.getThemeForegroundColor(context);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);
            final OptionItem option = getItem(position);
            final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
            text1.setText(option.name);
            icon.setImageDrawable(icon.getResources().getDrawable(option.icon));
            icon.setColorFilter(mActionIconColor, Mode.SRC_ATOP);
            return view;
        }

    }
}