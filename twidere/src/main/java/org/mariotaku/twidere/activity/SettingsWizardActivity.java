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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.DataImportActivity;
import org.mariotaku.twidere.activity.support.HomeActivity;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.fragment.BaseDialogFragment;
import org.mariotaku.twidere.fragment.BaseFragment;
import org.mariotaku.twidere.fragment.BasePreferenceFragment;
import org.mariotaku.twidere.fragment.CustomTabsFragment;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;
import org.mariotaku.twidere.fragment.support.DirectMessagesFragment;
import org.mariotaku.twidere.fragment.support.HomeTimelineFragment;
import org.mariotaku.twidere.fragment.support.MentionsTimelineFragment;
import org.mariotaku.twidere.model.CustomTabConfiguration;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.preference.WizardPageHeaderPreference;
import org.mariotaku.twidere.preference.WizardPageNavPreference;
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.CustomTabUtils;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.LinePageIndicator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mariotaku.twidere.util.CompareUtils.classEquals;

public class SettingsWizardActivity extends Activity implements Constants {

    public static final String WIZARD_PREFERENCE_KEY_NEXT_PAGE = "next_page";
    public static final String WIZARD_PREFERENCE_KEY_USE_DEFAULTS = "use_defaults";
    public static final String WIZARD_PREFERENCE_KEY_EDIT_CUSTOM_TABS = "edit_custom_tabs";
    public static final String WIZARD_PREFERENCE_KEY_IMPORT_SETTINGS = "import_settings";

    private static final int REQUEST_IMPORT_SETTINGS = 201;

    private ViewPager mViewPager;

    private LinePageIndicator mIndicator;
    private TabsAdapter mAdapter;

    private AbsInitialSettingsTask mTask;

    public void applyInitialSettings() {
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) return;
        mTask = new InitialSettingsTask(this);
        AsyncTaskUtils.executeTask(mTask);
    }

    public void applyInitialTabSettings() {
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) return;
        mTask = new InitialTabSettingsTask(this);
        AsyncTaskUtils.executeTask(mTask);
    }

    public void exitWizard() {
        final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_SETTINGS_WIZARD_COMPLETED, true).apply();
        final Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(EXTRA_OPEN_ACCOUNTS_DRAWER, true);
        startActivity(intent);
        finish();
    }

    public void gotoFinishPage() {
        if (mViewPager == null || mAdapter == null) return;
        final int last = mAdapter.getCount() - 1;
        mViewPager.setCurrentItem(Math.max(last, 0));
    }

    public void gotoLastPage() {
        if (mViewPager == null || mAdapter == null) return;
        gotoPage(getPageCount() - 2);
    }

    public void gotoNextPage() {
        if (mViewPager == null || mAdapter == null) return;
        final int current = mViewPager.getCurrentItem();
        mViewPager.setCurrentItem(MathUtils.clamp(current + 1, mAdapter.getCount() - 1, 0));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mIndicator = (LinePageIndicator) findViewById(R.id.indicator);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_IMPORT_SETTINGS: {
                if (resultCode == RESULT_OK) {
                    gotoLastPage();
                } else {
                    gotoNextPage();
                }
                break;
            }
            default: {
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_wizard);
        mAdapter = new TabsAdapter(this, getFragmentManager(), null);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setEnabled(false);
        mIndicator.setViewPager(mViewPager);
        initPages();
    }

    private void initPages() {
        mAdapter.addTab(WizardPageWelcomeFragment.class, null, getString(R.string.wizard_page_welcome_title), null, 0);
        mAdapter.addTab(WizardPageThemeFragment.class, null, getString(R.string.theme), null, 0);
        mAdapter.addTab(WizardPageTabsFragment.class, null, getString(R.string.tabs), null, 0);
        mAdapter.addTab(WizardPageCardsFragment.class, null, getString(R.string.cards), null, 0);
        mAdapter.addTab(WizardPageUsageStatisticsFragment.class, null, getString(R.string.usage_statistics), null, 0);
        mAdapter.addTab(WizardPageHintsFragment.class, null, getString(R.string.hints), null, 0);
        mAdapter.addTab(WizardPageFinishedFragment.class, null, getString(R.string.wizard_page_finished_title), null, 0);
    }

    private void openImportSettingsDialog() {
        final Intent intent = new Intent(this, DataImportActivity.class);
        startActivityForResult(intent, REQUEST_IMPORT_SETTINGS);
    }

    public static abstract class BaseWizardPageFragment extends BasePreferenceFragment implements
            OnPreferenceClickListener {

        public void gotoFinishPage() {
            final Activity a = getActivity();
            if (a instanceof SettingsWizardActivity) {
                ((SettingsWizardActivity) a).gotoFinishPage();
            }
        }

        public void gotoLastPage() {
            final Activity a = getActivity();
            if (a instanceof SettingsWizardActivity) {
                ((SettingsWizardActivity) a).gotoLastPage();
            }
        }

        public void gotoNextPage() {
            final Activity a = getActivity();
            if (a instanceof SettingsWizardActivity) {
                ((SettingsWizardActivity) a).gotoNextPage();
            }
        }

        @Override
        public void onActivityCreated(final Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            addPreferencesFromResource(getPreferenceResource());
            final Context context = getActivity();
            final Preference wizardHeader = new WizardPageHeaderPreference(context);
            wizardHeader.setTitle(getHeaderTitle());
            wizardHeader.setSummary(getHeaderSummary());
            wizardHeader.setOrder(0);
            final PreferenceScreen screen = getPreferenceScreen();
            screen.addPreference(wizardHeader);
            final int nextPageTitle = getNextPageTitle();
            if (nextPageTitle != 0) {
                final Preference nextPage = new WizardPageNavPreference(context);
                nextPage.setOrder(999);
                nextPage.setKey(WIZARD_PREFERENCE_KEY_NEXT_PAGE);
                nextPage.setTitle(nextPageTitle);
                nextPage.setOnPreferenceClickListener(this);
                screen.addPreference(nextPage);
            }
        }

        @Override
        public boolean onPreferenceClick(final Preference preference) {
            if (WIZARD_PREFERENCE_KEY_NEXT_PAGE.equals(preference.getKey())) {
                gotoNextPage();
            }
            return true;
        }

        protected abstract int getHeaderSummary();

        protected abstract int getHeaderTitle();

        protected int getNextPageTitle() {
            return R.string.next_step;
        }

        protected abstract int getPreferenceResource();

    }

    public static class WizardPageCardsFragment extends BaseWizardPageFragment {

        @Override
        protected int getHeaderSummary() {
            return R.string.wizard_page_cards_text;
        }

        @Override
        protected int getHeaderTitle() {
            return R.string.cards;
        }

        @Override
        protected int getPreferenceResource() {
            return R.xml.preferences_cards;
        }
    }

    public static class WizardPageFinishedFragment extends BaseFragment implements OnClickListener {

        @Override
        public void onClick(final View v) {
            final Activity a = getActivity();
            if (a instanceof SettingsWizardActivity) {
                ((SettingsWizardActivity) a).exitWizard();
            }
        }

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                                 final Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.fragment_wizard_page_finished, container, false);
            view.findViewById(R.id.exit_wizard).setOnClickListener(this);
            return view;
        }

    }

    public static class WizardPageUsageStatisticsFragment extends BaseWizardPageFragment {


        @Override
        protected int getHeaderSummary() {
            return R.string.wizard_page_usage_statistics_text;
        }

        @Override
        protected int getHeaderTitle() {
            return R.string.usage_statistics;
        }

        @Override
        protected int getNextPageTitle() {
            return R.string.next_step;
        }

        @Override
        protected int getPreferenceResource() {
            return R.xml.settings_wizard_page_usage_statistics;
        }
    }

    public static class WizardPageHintsFragment extends BaseWizardPageFragment {

        @Override
        protected int getHeaderSummary() {
            return R.string.wizard_page_hints_text;
        }

        @Override
        protected int getHeaderTitle() {
            return R.string.hints;
        }

        @Override
        protected int getNextPageTitle() {
            return R.string.finish;
        }

        @Override
        protected int getPreferenceResource() {
            return R.xml.settings_wizard_page_hints;
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            super.gotoNextPage();
        }

        @Override
        public void gotoNextPage() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, REQUEST_REQUEST_PERMISSIONS);
            } else {
                // Try getting location, some custom rom will popup requirement dialog
                Utils.getCachedLocation(getActivity());
                super.gotoNextPage();
            }
        }
    }

    public static class WizardPageTabsFragment extends BaseWizardPageFragment {

        private static final int REQUEST_CUSTOM_TABS = 1;

        public void applyInitialTabSettings() {
            final Activity a = getActivity();
            if (a instanceof SettingsWizardActivity) {
                ((SettingsWizardActivity) a).applyInitialTabSettings();
            }
        }

        @Override
        public void onActivityCreated(final Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            findPreference(WIZARD_PREFERENCE_KEY_EDIT_CUSTOM_TABS).setOnPreferenceClickListener(this);
            findPreference(WIZARD_PREFERENCE_KEY_USE_DEFAULTS).setOnPreferenceClickListener(this);
        }

        @Override
        public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            switch (requestCode) {
                case REQUEST_CUSTOM_TABS:
                    if (resultCode != RESULT_OK) {
                        new TabsUnchangedDialogFragment().show(getFragmentManager(), "tabs_unchanged");
                    } else {
                        gotoNextPage();
                    }
                    break;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public boolean onPreferenceClick(final Preference preference) {
            final String key = preference.getKey();
            if (WIZARD_PREFERENCE_KEY_EDIT_CUSTOM_TABS.equals(key)) {
                final Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, CustomTabsFragment.class.getName());
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE, R.string.tabs);
                startActivityForResult(intent, REQUEST_CUSTOM_TABS);
            } else if (WIZARD_PREFERENCE_KEY_USE_DEFAULTS.equals(key)) {
                applyInitialTabSettings();
            }
            return true;
        }

        @Override
        protected int getHeaderSummary() {
            return R.string.wizard_page_tabs_text;
        }

        @Override
        protected int getHeaderTitle() {
            return R.string.tabs;
        }

        @Override
        protected int getNextPageTitle() {
            return 0;
        }

        @Override
        protected int getPreferenceResource() {
            return R.xml.settings_wizard_page_tab;
        }

        public static class TabsUnchangedDialogFragment extends BaseDialogFragment implements
                DialogInterface.OnClickListener {

            @Override
            public void onCancel(final DialogInterface dialog) {
                gotoNextPage();
            }

            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                gotoNextPage();
            }

            @Override
            public Dialog onCreateDialog(final Bundle savedInstanceState) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.wizard_page_tabs_unchanged_message);
                builder.setPositiveButton(android.R.string.ok, this);
                return builder.create();
            }

            private void gotoNextPage() {
                final Activity a = getActivity();
                if (a instanceof SettingsWizardActivity) {
                    ((SettingsWizardActivity) a).gotoNextPage();
                }
            }

        }
    }

    public static class WizardPageThemeFragment extends BaseWizardPageFragment implements OnPreferenceClickListener {

        @Override
        protected int getHeaderSummary() {
            return R.string.wizard_page_theme_text;
        }

        @Override
        protected int getHeaderTitle() {
            return R.string.theme;
        }

        @Override
        protected int getPreferenceResource() {
            return R.xml.preferences_theme;
        }
    }

    public static class WizardPageWelcomeFragment extends BaseWizardPageFragment implements OnPreferenceClickListener {

        public void applyInitialSettings() {
            final Activity a = getActivity();
            if (a instanceof SettingsWizardActivity) {
                ((SettingsWizardActivity) a).applyInitialSettings();
            }
        }

        @Override
        public void onActivityCreated(final Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            findPreference(WIZARD_PREFERENCE_KEY_NEXT_PAGE).setOnPreferenceClickListener(this);
            findPreference(WIZARD_PREFERENCE_KEY_USE_DEFAULTS).setOnPreferenceClickListener(this);
            findPreference(WIZARD_PREFERENCE_KEY_IMPORT_SETTINGS).setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceClick(final Preference preference) {
            final String key = preference.getKey();
            if (WIZARD_PREFERENCE_KEY_NEXT_PAGE.equals(key)) {
                gotoNextPage();
            } else if (WIZARD_PREFERENCE_KEY_USE_DEFAULTS.equals(key)) {
                applyInitialSettings();
            } else if (WIZARD_PREFERENCE_KEY_IMPORT_SETTINGS.equals(key)) {
                openImportSettingsDialog();
            }
            return true;
        }

        @Override
        protected int getHeaderSummary() {
            return R.string.wizard_page_welcome_text;
        }

        @Override
        protected int getHeaderTitle() {
            return R.string.wizard_page_welcome_title;
        }

        @Override
        protected int getNextPageTitle() {
            return 0;
        }

        @Override
        protected int getPreferenceResource() {
            return R.xml.settings_wizard_page_welcome;
        }

        private void openImportSettingsDialog() {
            final Activity a = getActivity();
            if (a instanceof SettingsWizardActivity) {
                ((SettingsWizardActivity) a).openImportSettingsDialog();
            }
        }
    }

    static abstract class AbsInitialSettingsTask extends AsyncTask<Object, Object, Boolean> {

        private static final String FRAGMENT_TAG = "initial_settings_dialog";

        private static final String[] DEFAULT_TAB_TYPES = {TAB_TYPE_HOME_TIMELINE, TAB_TYPE_MENTIONS_TIMELINE,
                TAB_TYPE_TRENDS_SUGGESTIONS, TAB_TYPE_DIRECT_MESSAGES};

        private final SettingsWizardActivity mActivity;

        AbsInitialSettingsTask(final SettingsWizardActivity activity) {
            mActivity = activity;
        }

        @Override
        protected Boolean doInBackground(final Object... params) {
            final ContentResolver resolver = mActivity.getContentResolver();
            final List<SupportTabSpec> tabs = CustomTabUtils.getHomeTabs(mActivity);
            if (wasConfigured(tabs)) return true;
            Collections.sort(tabs);
            int i = 0;
            final List<ContentValues> values_list = new ArrayList<>();
            for (final String type : DEFAULT_TAB_TYPES) {
                final ContentValues values = new ContentValues();
                final CustomTabConfiguration conf = CustomTabUtils.getTabConfiguration(type);
                values.put(Tabs.TYPE, type);
                values.put(Tabs.ICON, CustomTabUtils.findTabIconKey(conf.getDefaultIcon()));
                values.put(Tabs.POSITION, i++);
                values_list.add(values);
            }
            for (final SupportTabSpec spec : tabs) {
                final String type = CustomTabUtils.findTabType(spec.cls);
                if (type != null) {
                    final ContentValues values = new ContentValues();
                    values.put(Tabs.TYPE, type);
                    values.put(Tabs.ARGUMENTS, ParseUtils.bundleToJSON(spec.args));
                    values.put(Tabs.NAME, ParseUtils.parseString(spec.name));
                    if (spec.icon instanceof Integer) {
                        values.put(Tabs.ICON, CustomTabUtils.findTabIconKey((Integer) spec.icon));
                    } else if (spec.icon instanceof File) {
                        values.put(Tabs.ICON, ((File) spec.icon).getPath());
                    }
                    values.put(Tabs.POSITION, i++);
                }
            }
            resolver.delete(Tabs.CONTENT_URI, null, null);
            resolver.bulkInsert(Tabs.CONTENT_URI, values_list.toArray(new ContentValues[values_list.size()]));
            return true;
        }

        protected SettingsWizardActivity getActivity() {
            return mActivity;
        }

        protected abstract void nextStep();

        @Override
        protected void onPostExecute(final Boolean result) {
            final FragmentManager fm = mActivity.getFragmentManager();
            final DialogFragment f = (DialogFragment) fm.findFragmentByTag(FRAGMENT_TAG);
            if (f != null) {
                f.dismiss();
            }
            nextStep();
        }

        @Override
        protected void onPreExecute() {
            ProgressDialogFragment.show(mActivity, FRAGMENT_TAG).setCancelable(false);
        }

        private boolean wasConfigured(final List<SupportTabSpec> tabs) {
            for (final SupportTabSpec spec : tabs) {
                if (classEquals(spec.cls, HomeTimelineFragment.class)
                        || classEquals(spec.cls, MentionsTimelineFragment.class)
                        || classEquals(spec.cls, DirectMessagesFragment.class)) return true;
            }
            return false;
        }

    }

    static class InitialSettingsTask extends AbsInitialSettingsTask {

        InitialSettingsTask(final SettingsWizardActivity activity) {
            super(activity);
        }

        @Override
        protected void nextStep() {
            final SettingsWizardActivity activity = getActivity();
            activity.gotoPage(activity.getPageCount() - 3);
        }

    }

    private void gotoPage(int page) {
        mViewPager.setCurrentItem(MathUtils.clamp(page, 0, getPageCount() - 1));
    }

    private int getPageCount() {
        return mAdapter.getCount();
    }

    static class InitialTabSettingsTask extends AbsInitialSettingsTask {

        InitialTabSettingsTask(final SettingsWizardActivity activity) {
            super(activity);
        }

        @Override
        protected void nextStep() {
            getActivity().gotoNextPage();
        }

    }

}
