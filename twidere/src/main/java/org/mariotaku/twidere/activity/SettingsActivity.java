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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.ThemedAppCompatDelegateFactory;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.DataExportActivity;
import org.mariotaku.twidere.activity.support.DataImportActivity;
import org.mariotaku.twidere.graphic.EmptyDrawable;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereActionModeForChildListener;
import org.mariotaku.twidere.util.support.ViewSupport;
import org.mariotaku.twidere.util.support.view.ViewOutlineProviderCompat;
import org.mariotaku.twidere.view.TintedStatusNativeActionModeAwareLayout;
import org.mariotaku.twidere.view.holder.ViewListHolder;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BasePreferenceActivity {

    private static final long HEADER_ID_RESTORE_ICON = 1001;
    private static final int RESULT_SETTINGS_CHANGED = 10;

    private HeaderAdapter mAdapter;

    private boolean mShouldNotifyChange;
    private TwidereActionModeForChildListener mTwidereActionModeForChildListener;
    private ThemedAppCompatDelegateFactory.ThemedAppCompatDelegate mDelegate;

    public static void setShouldNotifyChange(Activity activity) {
        if (!(activity instanceof SettingsActivity)) return;
        ((SettingsActivity) activity).setShouldNotifyChange(true);
    }

    public HeaderAdapter getHeaderAdapter() {
        if (mAdapter != null) return mAdapter;
        return mAdapter = new HeaderAdapter(this);
    }

    @Override
    public int getThemeColor() {
        return ThemeUtils.getUserAccentColor(this);
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getNoActionBarThemeResource(this);
    }

    @Override
    public void onBuildHeaders(final List<Header> target) {
        loadHeadersFromResource(R.xml.settings_headers, target);
        final HeaderAdapter adapter = getHeaderAdapter();
        adapter.clear();
        adapter.addAll(target);
        final ComponentName main = new ComponentName(this, MainActivity.class);
        final PackageManager pm = getPackageManager();
        if (pm.getComponentEnabledSetting(main) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            final Header restoreIconHeader = new Header();
            restoreIconHeader.titleRes = R.string.want_old_icon_back;
            restoreIconHeader.title = getString(restoreIconHeader.titleRes);
            restoreIconHeader.id = HEADER_ID_RESTORE_ICON;
            restoreIconHeader.intent = getIntent();
            adapter.add(restoreIconHeader);
        }
    }

    @Override
    protected boolean isValidFragment(final String fragmentName) {
        final Class<?> cls;
        try {
            cls = Class.forName(fragmentName);
        } catch (final ClassNotFoundException e) {
            return false;
        }
        return Fragment.class.isAssignableFrom(cls);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_SETTINGS_CHANGED && data != null && data.getBooleanExtra(EXTRA_CHANGED, false)) {
            setShouldNotifyChange(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onHeaderClick(@NonNull final Header header, final int position) {
        if (header.id == HEADER_ID_RESTORE_ICON) {
            final ComponentName main = new ComponentName(this, MainActivity.class);
            final ComponentName main2 = new ComponentName(this, MainHondaJOJOActivity.class);
            final PackageManager pm = getPackageManager();
            pm.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            pm.setComponentEnabledSetting(main2, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            Toast.makeText(this, R.string.icon_restored_message, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        super.onHeaderClick(header, position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startWithFragment(String fragmentName, Bundle args,
                                  Fragment resultTo, int resultRequestCode, int titleRes, int shortTitleRes) {
        Intent intent = onBuildStartFragmentIntent(fragmentName, args, titleRes, shortTitleRes);
        if (resultTo == null) {
            startActivityForResult(intent, resultRequestCode);
        } else {
            resultTo.startActivityForResult(intent, resultRequestCode);
        }
    }

    @Override
    public void switchToHeader(final String fragmentName, final Bundle args) {
        if (fragmentName == null) return;
        super.switchToHeader(fragmentName, args);
    }

    @Override
    public void switchToHeader(@NonNull final Header header) {
        if (header.fragment == null && header.intent == null) return;
        super.switchToHeader(header);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (!isTopSettings()) return false;
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    private boolean isTopSettings() {
        return getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT) == null;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.import_settings: {
                final Intent intent = new Intent(this, DataImportActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.export_settings: {
                final Intent intent = new Intent(this, DataExportActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        if (shouldNotifyChange()) {
            final Intent data = new Intent();
            data.putExtra(EXTRA_CHANGED, true);
            setResult(isTopSettings() ? RESULT_OK : RESULT_SETTINGS_CHANGED, data);
        }
        super.finish();
    }

    private void finishNoRestart() {
        super.finish();
    }

    @Override
    public void setListAdapter(final ListAdapter adapter) {
        if (adapter == null) {
            super.setListAdapter(null);
        } else {
            super.setListAdapter(getHeaderAdapter());
        }
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (ACTION_NAVIGATION_BACK.equals(action)) {
            navigateUp();
            return true;
        }
        return super.handleKeyboardShortcutSingle(handler, keyCode, event, metaState);
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        return ACTION_NAVIGATION_BACK.equals(action);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event, int metaState) {
        return super.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
//        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);

        mTwidereActionModeForChildListener = new TwidereActionModeForChildListener(this, this, false);
        final TintedStatusNativeActionModeAwareLayout layout = (TintedStatusNativeActionModeAwareLayout) findViewById(R.id.main_content);
        layout.setActionModeForChildListener(mTwidereActionModeForChildListener);

        ThemeUtils.setCompatContentViewOverlay(this, new EmptyDrawable());
        final View actionBarContainer = findViewById(R.id.twidere_action_bar_container);
        ViewCompat.setElevation(actionBarContainer, ThemeUtils.getSupportActionBarElevation(this));
        ViewSupport.setOutlineProvider(actionBarContainer, ViewOutlineProviderCompat.BACKGROUND);
        final View windowOverlay = findViewById(R.id.window_overlay);
        ViewSupport.setBackground(windowOverlay, ThemeUtils.getNormalWindowContentOverlay(this, getCurrentThemeResourceId()));
        setIntent(getIntent().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));


        final String backgroundOption = getCurrentThemeBackgroundOption();
        final boolean isTransparent = ThemeUtils.isTransparentBackground(backgroundOption);
        final int actionBarAlpha = isTransparent ? ThemeUtils.getActionBarAlpha(ThemeUtils.getUserThemeBackgroundAlpha(this)) : 0xFF;

        actionBarContainer.setAlpha(actionBarAlpha / 255f);
        windowOverlay.setAlpha(actionBarAlpha / 255f);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateUp();
            }
        });

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState != null) {
            invalidateHeaders();
        }
        final ListView listView = getListView();
        if (listView != null) {
            listView.setChoiceMode(isMultiPane() ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
            final LayoutParams lp = listView.getLayoutParams();
            if (lp instanceof MarginLayoutParams) {
                final MarginLayoutParams mlp = (MarginLayoutParams) lp;
                mlp.leftMargin = 0;
                mlp.topMargin = 0;
                mlp.rightMargin = 0;
                mlp.bottomMargin = 0;
                listView.setLayoutParams(mlp);
            }
            final ViewParent listParent = listView.getParent();
            if (listParent instanceof ViewGroup) {
                ((ViewGroup) listParent).setPadding(0, 0, 0, 0);
            }
        }
    }

    @Override
    public AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = ThemedAppCompatDelegateFactory.create(this, this);
        }
        return mDelegate;
    }

    private void setShouldNotifyChange(boolean notify) {
        mShouldNotifyChange = notify;
    }

    private boolean shouldNotifyChange() {
        return mShouldNotifyChange;
    }

    private void navigateUp() {
        if (mTwidereActionModeForChildListener.finishExisting()) {
            return;
        }
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (isTopSettings() && shouldNotifyChange()) {
            final RestartConfirmDialogFragment df = new RestartConfirmDialogFragment();
            df.show(getFragmentManager().beginTransaction(), "restart_confirm");
            return;
        }
        super.onBackPressed();
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(final ActionMode.Callback callback) {
        return null;
    }

    public static class RestartConfirmDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.app_restart_confirm);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(R.string.dont_restart, this);
            return builder.create();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            final SettingsActivity activity = (SettingsActivity) getActivity();
            if (activity == null) return;
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    activity.finish();
                    break;
                }
                case DialogInterface.BUTTON_NEGATIVE: {
                    activity.finishNoRestart();
                    break;
                }
            }
        }
    }

    private static class HeaderAdapter extends BaseAdapter {

        static final int HEADER_TYPE_NORMAL = 0;
        static final int HEADER_TYPE_CATEGORY = 1;

        private final Resources mResources;
        private final int mActionIconColor;
        private final ArrayList<Header> mHeaders;
        private final LayoutInflater mInflater;

        public HeaderAdapter(final Context context) {
            mInflater = LayoutInflater.from(context);
            mHeaders = new ArrayList<>();
            mResources = context.getResources();
            mActionIconColor = ThemeUtils.getThemeForegroundColor(context);
        }

        private static int getHeaderType(final Header header) {
            if (header.fragment != null || header.intent != null)
                return HEADER_TYPE_NORMAL;
            else return HEADER_TYPE_CATEGORY;

        }

        public void add(Header header) {
            mHeaders.add(header);
            notifyDataSetChanged();
        }

        public void addAll(List<Header> headers) {
            mHeaders.addAll(headers);
            notifyDataSetChanged();
        }

        public void clear() {
            mHeaders.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mHeaders.size();
        }

        @Override
        public Header getItem(final int position) {
            return mHeaders.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final Header header = getItem(position);
            final int viewType = getHeaderType(header);
            final View view = convertView != null ? convertView : inflateItemView(viewType, parent);
            switch (viewType) {
                case HEADER_TYPE_CATEGORY: {
                    bindCategoryHeader(view, position, header);
                    break;
                }
                default: {
                    bindHeader(view, position, header);
                    break;
                }
            }
            return view;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(final int position) {
            return getItemViewType(position) == HEADER_TYPE_NORMAL;
        }

        @Override
        public int getItemViewType(final int position) {
            final Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        private void bindCategoryHeader(View view, int position, Header header) {
            final TextView title = (TextView) view.findViewById(android.R.id.title);
            if (!TextUtils.isEmpty(header.title)) {
                title.setText(header.title);
            } else {
                title.setText(header.titleRes);
            }
        }

        private void bindHeader(View view, int position, Header header) {
            final HeaderViewHolder holder;
            final Object tag = view.getTag();
            if (tag instanceof HeaderViewHolder) {
                holder = (HeaderViewHolder) tag;
            } else {
                holder = new HeaderViewHolder(view);
                view.setTag(holder);
            }
            final CharSequence title = header.getTitle(mResources);
            holder.title.setText(title);
            final CharSequence summary = header.getSummary(mResources);
            if (!TextUtils.isEmpty(summary)) {
                holder.summary.setVisibility(View.VISIBLE);
                holder.summary.setText(summary);
            } else {
                holder.summary.setVisibility(View.GONE);
            }
            if (header.iconRes != 0) {
                holder.icon.setImageResource(header.iconRes);
            } else {
                holder.icon.setImageDrawable(null);
            }
            holder.icon.setColorFilter(mActionIconColor, Mode.SRC_ATOP);

        }

        private int getCategoriesCount(final int start, final int end) {
            int categoriesCount = 0;
            for (int i = start; i < end; i++) {
                if (getHeaderType(mHeaders.get(i)) == HEADER_TYPE_CATEGORY) {
                    categoriesCount++;
                }
            }
            return categoriesCount;
        }

        private View inflateItemView(int viewType, ViewGroup parent) {
            final int layoutRes;
            switch (viewType) {
                case HEADER_TYPE_CATEGORY: {
                    layoutRes = R.layout.list_item_preference_header_category;
                    break;
                }
                default: {
                    layoutRes = R.layout.list_item_preference_header_item;
                    break;
                }
            }
            return mInflater.inflate(layoutRes, parent, false);
        }

        private static class HeaderViewHolder extends ViewListHolder {
            private final TextView title, summary;
            private final ImageView icon;

            HeaderViewHolder(final View view) {
                super(view);
                title = (TextView) findViewById(android.R.id.title);
                summary = (TextView) findViewById(android.R.id.summary);
                icon = (ImageView) findViewById(android.R.id.icon);
            }
        }

    }

}
