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

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.DataExportActivity;
import org.mariotaku.twidere.activity.support.DataImportActivity;
import org.mariotaku.twidere.graphic.EmptyDrawable;
import org.mariotaku.twidere.util.CompareUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.holder.ViewListHolder;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BasePreferenceActivity {

    private static long HEADER_ID_RESTORE_ICON = 1001;

    private SharedPreferences mPreferences;
    private PackageManager mPackageManager;

    private HeaderAdapter mAdapter;

    private int mCurrentThemeColor, mThemeBackgroundAlpha;
    private boolean mCompactCards;

    private String mTheme;
    private String mThemeFontFamily;
    private String mThemeBackground;

    private boolean mShouldNotifyChange;

    public static void setShouldNotifyChange(Activity activity) {
        if (!(activity instanceof SettingsActivity)) return;
        ((SettingsActivity) activity).setShouldNotifyChange(true);
    }

    private void setShouldNotifyChange(boolean notify) {
        mShouldNotifyChange = notify;
    }

    @Override
    public void finish() {
        if (shouldNotifyThemeChange()) {
            final Intent data = new Intent();
            data.putExtra(EXTRA_RESTART_ACTIVITY, true);
            setResult(RESULT_OK, data);
        }
        super.finish();
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

    public HeaderAdapter getHeaderAdapter() {
        if (mAdapter != null) return mAdapter;
        return mAdapter = new HeaderAdapter(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null && data.getBooleanExtra(EXTRA_RESTART_ACTIVITY, false)) {
            setShouldNotifyChange(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBuildHeaders(final List<Header> target) {
        loadHeadersFromResource(R.xml.settings_headers, target);
        final HeaderAdapter adapter = getHeaderAdapter();
        adapter.clear();
        adapter.addAll(target);
        final ComponentName main = new ComponentName(this, MainActivity.class);
        if (mPackageManager.getComponentEnabledSetting(main) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            final Header restoreIconHeader = new Header();
            restoreIconHeader.titleRes = R.string.want_old_icon_back;
            restoreIconHeader.title = getString(restoreIconHeader.titleRes);
            restoreIconHeader.id = HEADER_ID_RESTORE_ICON;
            restoreIconHeader.intent = getIntent();
            adapter.add(restoreIconHeader);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if (getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT) != null) return false;
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public void onHeaderClick(@NonNull final Header header, final int position) {
        if (header.id == HEADER_ID_RESTORE_ICON) {
            final ComponentName main = new ComponentName(this, MainActivity.class);
            final ComponentName main2 = new ComponentName(this, MainHondaJOJOActivity.class);
            mPackageManager.setComponentEnabledSetting(main, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            mPackageManager.setComponentEnabledSetting(main2, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            Toast.makeText(this, R.string.icon_restored_message, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        super.onHeaderClick(header, position);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case MENU_HOME: {
                onBackPressed();
                return true;
            }
            case MENU_IMPORT_SETTINGS: {
                final Intent intent = new Intent(this, DataImportActivity.class);
                startActivity(intent);
                return true;
            }
            case MENU_EXPORT_SETTINGS: {
                final Intent intent = new Intent(this, DataExportActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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
    public void switchToHeader(@NonNull final Header header) {
        if (header.fragment == null && header.intent == null) return;
        super.switchToHeader(header);
    }

    @Override
    public void switchToHeader(final String fragmentName, final Bundle args) {
        if (fragmentName == null) return;
        super.switchToHeader(fragmentName, args);
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
    protected void onCreate(final Bundle savedInstanceState) {
        mPackageManager = getPackageManager();
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mCompactCards = mPreferences.getBoolean(KEY_COMPACT_CARDS, false);
        mTheme = mPreferences.getString(KEY_THEME, DEFAULT_THEME);
        mThemeBackground = mPreferences.getString(KEY_THEME_BACKGROUND, DEFAULT_THEME_BACKGROUND);
        mThemeBackgroundAlpha = mPreferences.getInt(KEY_THEME_BACKGROUND_ALPHA, DEFAULT_THEME_BACKGROUND_ALPHA);
        mThemeFontFamily = mPreferences.getString(KEY_THEME_FONT_FAMILY, DEFAULT_THEME_FONT_FAMILY);
        mCurrentThemeColor = ThemeUtils.getUserAccentColor(this);
        super.onCreate(savedInstanceState);
        setIntent(getIntent().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState != null) {
            invalidateHeaders();
        }
        final ListView listView = getListView();
        if (listView != null) {
            listView.setDivider(new EmptyDrawable());
            listView.setDividerHeight(0);
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

    private boolean shouldNotifyThemeChange() {
        if (mShouldNotifyChange) return true;
        return !CompareUtils.objectEquals(mTheme, mPreferences.getString(KEY_THEME, DEFAULT_THEME))
                || !CompareUtils.objectEquals(mThemeFontFamily, mPreferences.getString(KEY_THEME_FONT_FAMILY, DEFAULT_THEME_FONT_FAMILY))
                || !CompareUtils.objectEquals(mThemeBackground, mPreferences.getString(KEY_THEME_BACKGROUND, DEFAULT_THEME_BACKGROUND))
                || mCurrentThemeColor != ThemeUtils.getUserAccentColor(this)
                || mThemeBackgroundAlpha != mPreferences.getInt(KEY_THEME_BACKGROUND_ALPHA, DEFAULT_THEME_BACKGROUND_ALPHA)
                || mCompactCards != mPreferences.getBoolean(KEY_COMPACT_CARDS, false);
    }

    private static class HeaderAdapter extends BaseAdapter {

        static final int HEADER_TYPE_NORMAL = 0;
        static final int HEADER_TYPE_CATEGORY = 1;
        static final int HEADER_TYPE_SPACE = 2;

        private final Resources mResources;
        private final int mActionIconColor;
        private final ArrayList<Header> mHeaders;
        private final LayoutInflater mInflater;
        private int mCategoriesCount;
        private boolean mFirstItemIsCategory;

        public HeaderAdapter(final Context context) {
            mInflater = LayoutInflater.from(context);
            mHeaders = new ArrayList<>();
            mResources = context.getResources();
            mActionIconColor = ThemeUtils.getThemeForegroundColor(context);
        }

        public void add(Header header) {
            mHeaders.add(header);
            notifyDataSetChanged();
        }

        public void addAll(List<Header> headers) {
            mHeaders.addAll(headers);
            notifyDataSetChanged();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public void notifyDataSetChanged() {
            updateCategoriesInfo();
            super.notifyDataSetChanged();
        }

        private void updateCategoriesInfo() {
            mFirstItemIsCategory = !mHeaders.isEmpty()
                    && getHeaderType(mHeaders.get(0)) == HEADER_TYPE_CATEGORY;
            mCategoriesCount = getCategoriesCount(0, mHeaders.size());
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

        public void clear() {
            mHeaders.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(final int position) {
            final Header header = getItem(position);
            return getHeaderType(header);
        }

        @Override
        public int getCount() {
            return mHeaders.size() + mCategoriesCount + (mFirstItemIsCategory ? 0 : 1);
        }

        @Override
        public Header getItem(final int position) {
            if (position == getCount() - 1) return new Header();
            final int realPosition = mFirstItemIsCategory ? position + 1 : position;
            int categoriesCount = 0;
            int i;
            for (i = 0; i + categoriesCount < realPosition; i++) {
                if (getHeaderType(mHeaders.get(i)) == HEADER_TYPE_CATEGORY) {
                    categoriesCount++;
                }
            }
            if (i + categoriesCount == realPosition && getHeaderType(mHeaders.get(i)) == HEADER_TYPE_CATEGORY) {
                return new Header();
            }
            return mHeaders.get(realPosition - categoriesCount);
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
                case HEADER_TYPE_SPACE: {
                    break;
                }
                default: {
                    bindHeader(view, position, header);
                    break;
                }
            }
            return view;
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

            if (position > 0 && position <= getCount() - 1) {
                final boolean prevCategory = getItemViewType(position - 1) == HEADER_TYPE_CATEGORY;
                holder.content.setShowDividers(prevCategory ? LinearLayout.SHOW_DIVIDER_NONE : LinearLayout.SHOW_DIVIDER_END);
            } else {
                holder.content.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
            }
        }

        private View inflateItemView(int viewType, ViewGroup parent) {
            final int layoutRes;
            switch (viewType) {
                case HEADER_TYPE_CATEGORY: {
                    layoutRes = R.layout.list_item_preference_header_category;
                    break;
                }
                case HEADER_TYPE_SPACE: {
                    layoutRes = R.layout.list_item_preference_header_space;
                    break;
                }
                default: {
                    layoutRes = R.layout.list_item_preference_header_item;
                    break;
                }
            }
            return mInflater.inflate(layoutRes, parent, false);
        }

        @Override
        public boolean isEnabled(final int position) {
            return getItemViewType(position) == HEADER_TYPE_NORMAL;
        }

        private static int getHeaderType(final Header header) {
            if (header.fragment != null || header.intent != null)
                return HEADER_TYPE_NORMAL;
            else if (header.title != null || header.titleRes != 0)
                return HEADER_TYPE_CATEGORY;
            else
                return HEADER_TYPE_SPACE;

        }

        private static class HeaderViewHolder extends ViewListHolder {
            private final TextView title, summary;
            private final ImageView icon;
            private final LinearLayout content;

            HeaderViewHolder(final View view) {
                super(view);
                title = (TextView) findViewById(android.R.id.title);
                summary = (TextView) findViewById(android.R.id.summary);
                icon = (ImageView) findViewById(android.R.id.icon);
                content = (LinearLayout) findViewById(android.R.id.content);
            }
        }

    }

}
