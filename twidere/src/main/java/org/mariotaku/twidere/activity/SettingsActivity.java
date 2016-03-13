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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback;
import android.support.v7.preference.PreferenceFragmentCompat.OnPreferenceStartFragmentCallback;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.BaseAppCompatActivity;
import org.mariotaku.twidere.fragment.CustomTabsFragment;
import org.mariotaku.twidere.fragment.ExtensionsListFragment;
import org.mariotaku.twidere.fragment.SettingsDetailsFragment;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.fragment.support.SupportBrowserFragment;
import org.mariotaku.twidere.preference.iface.IDialogPreference;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereActionModeForChildListener;
import org.mariotaku.twidere.view.TintedStatusNativeActionModeAwareLayout;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends BaseAppCompatActivity implements OnItemClickListener,
        OnPreferenceStartFragmentCallback, OnPreferenceDisplayDialogCallback {

    private static final int RESULT_SETTINGS_CHANGED = 10;

    private ListView mEntriesListView;
    private SlidingPaneLayout mSlidingPaneLayout;

    private boolean mShouldNotifyChange;
    private EntriesAdapter mEntriesAdapter;
    private TintedStatusNativeActionModeAwareLayout mMainContent;
    private View mDetailFragmentContainer;
    private TwidereActionModeForChildListener mTwidereActionModeForChildListener;

    public static void setShouldNotifyChange(Activity activity) {
        if (!(activity instanceof SettingsActivity)) return;
        ((SettingsActivity) activity).setShouldNotifyChange(true);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mEntriesListView = (ListView) findViewById(R.id.entries_list);
        mSlidingPaneLayout = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
        mMainContent = (TintedStatusNativeActionModeAwareLayout) findViewById(R.id.main_content);
        mDetailFragmentContainer = findViewById(R.id.detail_fragment_container);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final int backgroundAlpha = getCurrentThemeBackgroundAlpha();
        final int alpha = ThemeUtils.getActionBarAlpha(getThemeBackgroundOption(), backgroundAlpha);
        final int statusBarColor = ThemeUtils.getActionBarColor(this, getCurrentThemeColor(), getThemeBackgroundOption());
        mMainContent.setDrawColor(true);
        mMainContent.setColor(statusBarColor, alpha);

        mTwidereActionModeForChildListener = new TwidereActionModeForChildListener(this, this, false);
        mMainContent.setActionModeForChildListener(mTwidereActionModeForChildListener);
        mDetailFragmentContainer.setBackgroundColor((backgroundAlpha << 24 | 0xFFFFFF) & ThemeUtils.getThemeBackgroundColor(this));

        mSlidingPaneLayout.setShadowResourceLeft(R.drawable.sliding_pane_shadow_left);
        mSlidingPaneLayout.setShadowResourceRight(R.drawable.sliding_pane_shadow_right);
        mSlidingPaneLayout.setSliderFadeColor(0);
        mEntriesAdapter = new EntriesAdapter(this);
        initEntries();
        mEntriesListView.setAdapter(mEntriesAdapter);
        mEntriesListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mEntriesListView.setOnItemClickListener(this);
    }

    private void initEntries() {
        mEntriesAdapter.addHeader(getString(R.string.appearance));
        mEntriesAdapter.addPreference(R.drawable.ic_action_color_palette, getString(R.string.theme),
                R.xml.preferences_theme);
        mEntriesAdapter.addPreference(R.drawable.ic_action_card, getString(R.string.cards),
                R.xml.preferences_cards);

        mEntriesAdapter.addHeader(getString(R.string.function));
        mEntriesAdapter.addPreference(R.drawable.ic_action_tab, getString(R.string.tabs),
                CustomTabsFragment.class);
        mEntriesAdapter.addPreference(R.drawable.ic_action_extension, getString(R.string.extensions),
                ExtensionsListFragment.class);
        mEntriesAdapter.addPreference(R.drawable.ic_action_refresh, getString(R.string.refresh),
                R.xml.preferences_refresh);
        mEntriesAdapter.addPreference(R.drawable.ic_action_notification, getString(R.string.notifications),
                R.xml.preferences_notifications);
        mEntriesAdapter.addPreference(R.drawable.ic_action_server, getString(R.string.network),
                R.xml.preferences_network);
        mEntriesAdapter.addPreference(R.drawable.ic_action_status_compose, getString(R.string.compose),
                R.xml.preferences_compose);
        mEntriesAdapter.addPreference(R.drawable.ic_action_twidere_square, getString(R.string.content),
                R.xml.preferences_content);
        mEntriesAdapter.addPreference(R.drawable.ic_action_storage, getString(R.string.storage),
                R.xml.preferences_storage);
        mEntriesAdapter.addPreference(R.drawable.ic_action_more_horizontal, getString(R.string.other_settings),
                R.xml.preferences_other);

        mEntriesAdapter.addHeader(getString(R.string.about));
        mEntriesAdapter.addPreference(R.drawable.ic_action_info, getString(R.string.about),
                R.xml.preferences_about);
        final Bundle browserArgs = new Bundle();
        browserArgs.putString(EXTRA_URI, "file:///android_asset/gpl-3.0-standalone.html");
        mEntriesAdapter.addPreference(R.drawable.ic_action_open_source, getString(R.string.open_source_license),
                SupportBrowserFragment.class, browserArgs);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_SETTINGS_CHANGED && data != null && data.getBooleanExtra(EXTRA_CHANGED, false)) {
            setShouldNotifyChange(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private boolean isTopSettings() {
        return Boolean.parseBoolean("true");
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
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        final String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (ACTION_NAVIGATION_BACK.equals(action)) {
            onBackPressed();
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


    private void setShouldNotifyChange(boolean notify) {
        mShouldNotifyChange = notify;
    }

    private boolean shouldNotifyChange() {
        return mShouldNotifyChange;
    }


    @Override
    public void onBackPressed() {
        if (mTwidereActionModeForChildListener.finishExisting()) {
            return;
        }
        if (isTopSettings() && shouldNotifyChange()) {
            final RestartConfirmDialogFragment df = new RestartConfirmDialogFragment();
            df.show(getSupportFragmentManager(), "restart_confirm");
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat fragment, Preference preference) {
        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        final Fragment f = Fragment.instantiate(this, preference.getFragment(), preference.getExtras());
        ft.replace(R.id.detail_fragment_container, f);
        ft.addToBackStack(String.valueOf(preference.getTitle()));
        ft.commit();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Entry entry = mEntriesAdapter.getItem(position);
        if (!(entry instanceof PreferenceEntry)) return;
        final PreferenceEntry pe = (PreferenceEntry) entry;
        final FragmentManager fm = getSupportFragmentManager();
        final FragmentTransaction ft = fm.beginTransaction();
        if (pe.preference != 0) {
            final Bundle args = new Bundle();
            args.putInt(EXTRA_RESID, pe.preference);
            final Fragment f = Fragment.instantiate(this, SettingsDetailsFragment.class.getName(),
                    args);
            ft.replace(R.id.detail_fragment_container, f);
        } else if (pe.fragment != null) {
            ft.replace(R.id.detail_fragment_container, Fragment.instantiate(this, pe.fragment,
                    pe.args));
        }
        ft.setBreadCrumbTitle(pe.title);
        ft.commit();
        mSlidingPaneLayout.closePane();
    }

    @Override
    public boolean onPreferenceDisplayDialog(PreferenceFragmentCompat fragment, Preference preference) {
        if (preference instanceof IDialogPreference) {
            ((IDialogPreference) preference).displayDialog(fragment);
            return true;
        }
        return false;
    }

    static class EntriesAdapter extends BaseAdapter {

        static final int VIEW_TYPE_PREFERENCE_ENTRY = 0;
        static final int VIEW_TYPE_HEADER_ENTRY = 1;

        private final Context mContext;
        private final LayoutInflater mInflater;
        private final List<Entry> mEntries;

        public EntriesAdapter(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mEntries = new ArrayList<>();
        }

        public void addPreference(@DrawableRes int icon, String title, @XmlRes int preference) {
            mEntries.add(new PreferenceEntry(icon, title, preference, null, null));
            notifyDataSetChanged();
        }

        public void addPreference(@DrawableRes int icon, String title, Class<? extends Fragment> cls) {
            addPreference(icon, title, cls, null);
        }


        public void addPreference(@DrawableRes int icon, String title, Class<? extends Fragment> cls,
                                  @Nullable Bundle args) {
            mEntries.add(new PreferenceEntry(icon, title, 0, cls.getName(), args));
            notifyDataSetChanged();
        }

        public void addHeader(String title) {
            mEntries.add(new HeaderEntry(title));
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mEntries.size();
        }

        @Override
        public Entry getItem(int position) {
            return mEntries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public boolean isEnabled(int position) {
            return getItemViewType(position) == VIEW_TYPE_PREFERENCE_ENTRY;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            final Entry entry = getItem(position);
            if (entry instanceof PreferenceEntry) {
                return VIEW_TYPE_PREFERENCE_ENTRY;
            } else if (entry instanceof HeaderEntry) {
                return VIEW_TYPE_HEADER_ENTRY;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int viewType = getItemViewType(position);
            final Entry entry = getItem(position);
            final View view;
            if (convertView != null) {
                view = convertView;
            } else {
                switch (viewType) {
                    case VIEW_TYPE_PREFERENCE_ENTRY: {
                        view = mInflater.inflate(R.layout.list_item_preference_header_item, parent, false);
                        break;
                    }
                    case VIEW_TYPE_HEADER_ENTRY: {
                        view = mInflater.inflate(R.layout.list_item_preference_header_category, parent, false);
                        break;
                    }
                    default: {
                        throw new UnsupportedOperationException();
                    }
                }
            }
            entry.bind(view);
            return view;
        }
    }

    static abstract class Entry {
        public abstract void bind(View view);

    }

    static class PreferenceEntry extends Entry {
        private final int icon;
        private final String title;
        private final int preference;
        private final String fragment;

        private final Bundle args;

        public PreferenceEntry(int icon, String title, int preference, String fragment, Bundle args) {
            this.icon = icon;
            this.title = title;
            this.preference = preference;
            this.fragment = fragment;
            this.args = args;
        }

        @Override
        public void bind(View view) {
            ((ImageView) view.findViewById(android.R.id.icon)).setImageResource(icon);
            ((TextView) view.findViewById(android.R.id.title)).setText(title);
        }

    }

    static class HeaderEntry extends Entry {

        private final String title;

        public HeaderEntry(String title) {
            this.title = title;
        }

        @Override
        public void bind(View view) {
            ((TextView) view.findViewById(android.R.id.title)).setText(title);
        }
    }

    public static class RestartConfirmDialogFragment extends BaseSupportDialogFragment implements DialogInterface.OnClickListener {
        @NonNull
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


}
