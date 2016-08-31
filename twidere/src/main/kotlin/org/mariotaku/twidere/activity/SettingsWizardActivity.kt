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

package org.mariotaku.twidere.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.preference.Preference
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import com.afollestad.appthemeengine.Config
import com.afollestad.appthemeengine.util.ATEUtil
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.SupportTabsAdapter
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.fragment.*
import org.mariotaku.twidere.model.SupportTabSpec
import org.mariotaku.twidere.preference.WizardPageHeaderPreference
import org.mariotaku.twidere.preference.WizardPageNavPreference
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.view.LinePageIndicator
import java.io.File
import java.util.*

class SettingsWizardActivity : BaseActivity() {

    private var viewPager: ViewPager? = null

    private var indicator: LinePageIndicator? = null
    private var adapter: SupportTabsAdapter? = null

    private var task: AbsInitialSettingsTask? = null

    fun applyInitialSettings() {
        if (task != null && task!!.status == AsyncTask.Status.RUNNING) return
        task = InitialSettingsTask(this)
        AsyncTaskUtils.executeTask<AbsInitialSettingsTask, Any>(task)
    }

    fun applyInitialTabSettings() {
        if (task != null && task!!.status == AsyncTask.Status.RUNNING) return
        task = InitialTabSettingsTask(this)
        AsyncTaskUtils.executeTask<AbsInitialSettingsTask, Any>(task)
    }

    fun exitWizard() {
        val prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SETTINGS_WIZARD_COMPLETED, true).apply()
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(IntentConstants.EXTRA_OPEN_ACCOUNTS_DRAWER, true)
        startActivity(intent)
        finish()
    }

    fun gotoFinishPage() {
        if (viewPager == null || adapter == null) return
        val last = adapter!!.count - 1
        viewPager!!.currentItem = Math.max(last, 0)
    }

    fun gotoLastPage() {
        if (viewPager == null || adapter == null) return
        gotoPage(pageCount - 2)
    }

    fun gotoNextPage() {
        if (viewPager == null || adapter == null) return
        val current = viewPager!!.currentItem
        viewPager!!.currentItem = TwidereMathUtils.clamp(current + 1, adapter!!.count - 1, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onContentChanged() {
        super.onContentChanged()
        viewPager = findViewById(R.id.pager) as ViewPager
        indicator = findViewById(R.id.indicator) as LinePageIndicator
    }

    override fun getStatusBarColor(): Int {
        if (VALUE_THEME_NAME_DARK == ateKey) return Color.BLACK
        return ATEUtil.darkenColor(ThemeUtils.getColorBackground(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_IMPORT_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    gotoLastPage()
                } else {
                    gotoNextPage()
                }
            }
            else -> {
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override val themeBackgroundOption: String
        get() = ThemeUtils.getThemeBackgroundOption(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_wizard)
        adapter = SupportTabsAdapter(this, supportFragmentManager, null)
        viewPager!!.adapter = adapter
        viewPager!!.isEnabled = false
        indicator!!.setViewPager(viewPager)
        indicator!!.selectedColor = Config.accentColor(this, ateKey)
        initPages()
        val initialPage = intent.getIntExtra(IntentConstants.EXTRA_PAGE, -1)
        if (initialPage != -1) {
            viewPager!!.setCurrentItem(initialPage, false)
        }
    }

    private fun initPages() {
        adapter!!.addTab(WizardPageWelcomeFragment::class.java, null, getString(R.string.wizard_page_welcome_title), null, 0, null)
        adapter!!.addTab(WizardPageThemeFragment::class.java, null, getString(R.string.theme), null, 0, null)
        adapter!!.addTab(WizardPageTabsFragment::class.java, null, getString(R.string.tabs), null, 0, null)
        adapter!!.addTab(WizardPageCardsFragment::class.java, null, getString(R.string.cards), null, 0, null)
        adapter!!.addTab(WizardPageUsageStatisticsFragment::class.java, null, getString(R.string.usage_statistics), null, 0, null)
        adapter!!.addTab(WizardPageHintsFragment::class.java, null, getString(R.string.hints), null, 0, null)
        adapter!!.addTab(WizardPageFinishedFragment::class.java, null, getString(R.string.wizard_page_finished_title), null, 0, null)
    }

    private fun openImportSettingsDialog() {
        val intent = Intent(this, DataImportActivity::class.java)
        startActivityForResult(intent, REQUEST_IMPORT_SETTINGS)
    }

    abstract class BaseWizardPageFragment : BasePreferenceFragment(), Preference.OnPreferenceClickListener {

        fun gotoFinishPage() {
            val a = activity
            if (a is SettingsWizardActivity) {
                a.gotoFinishPage()
            }
        }

        fun gotoLastPage() {
            val a = activity
            if (a is SettingsWizardActivity) {
                a.gotoLastPage()
            }
        }

        open fun gotoNextPage() {
            val a = activity
            if (a is SettingsWizardActivity) {
                a.gotoNextPage()
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            val preferenceManager = preferenceManager
            preferenceManager.sharedPreferencesName = SHARED_PREFERENCES_NAME
            addPreferencesFromResource(preferenceResource)

            val context = activity
            val wizardHeader = WizardPageHeaderPreference(context)
            wizardHeader.setTitle(headerTitle)
            wizardHeader.setSummary(headerSummary)
            wizardHeader.order = 0
            val screen = preferenceScreen
            screen.addPreference(wizardHeader)
            val nextPageTitle = nextPageTitle
            if (nextPageTitle != 0) {
                val nextPage = WizardPageNavPreference(context)
                nextPage.order = 999
                nextPage.key = WIZARD_PREFERENCE_KEY_NEXT_PAGE
                nextPage.setTitle(nextPageTitle)
                nextPage.onPreferenceClickListener = this
                screen.addPreference(nextPage)
            }


            val listener = Preference.OnPreferenceChangeListener { preference, newValue ->
                val extras = preference.extras
                if (extras != null && extras.getBoolean(IntentConstants.EXTRA_RESTART_ACTIVITY)) {
                    (activity as SettingsWizardActivity).restartWithCurrentPage()
                    return@OnPreferenceChangeListener true
                }
                true
            }

            for (i in 0 until screen.preferenceCount) {
                screen.getPreference(i).onPreferenceChangeListener = listener
            }
        }

        override fun onPreferenceClick(preference: Preference): Boolean {
            if (WIZARD_PREFERENCE_KEY_NEXT_PAGE == preference.key) {
                gotoNextPage()
            }
            return true
        }

        protected abstract val headerSummary: Int

        protected abstract val headerTitle: Int

        protected open val nextPageTitle: Int
            get() = R.string.next_step

        protected abstract val preferenceResource: Int

    }

    private fun restartWithCurrentPage() {
        val intent = intent
        intent.putExtra(EXTRA_PAGE, viewPager!!.currentItem)
        setIntent(intent)
        recreate()
    }

    class WizardPageCardsFragment : BaseWizardPageFragment() {

        override val headerSummary: Int
            get() = R.string.wizard_page_cards_text

        override val headerTitle: Int
            get() = R.string.cards

        override val preferenceResource: Int
            get() = R.xml.preferences_cards
    }

    class WizardPageFinishedFragment : BaseSupportFragment(), OnClickListener {

        override fun onClick(v: View) {
            val a = activity
            if (a is SettingsWizardActivity) {
                a.exitWizard()
            }
        }

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val view = inflater!!.inflate(R.layout.fragment_wizard_page_finished, container, false)
            view.findViewById(R.id.exit_wizard).setOnClickListener(this)
            return view
        }

    }

    class WizardPageUsageStatisticsFragment : BaseWizardPageFragment() {


        override val headerSummary: Int
            get() = R.string.wizard_page_usage_statistics_text

        override val headerTitle: Int
            get() = R.string.usage_statistics

        override val nextPageTitle: Int
            get() = R.string.next_step

        override val preferenceResource: Int
            get() = R.xml.settings_wizard_page_usage_statistics
    }

    class WizardPageHintsFragment : BaseWizardPageFragment() {

        override val headerSummary: Int
            get() = R.string.wizard_page_hints_text

        override val headerTitle: Int
            get() = R.string.hints

        override val nextPageTitle: Int
            get() = R.string.finish

        override val preferenceResource: Int
            get() = R.xml.settings_wizard_page_hints

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            super.gotoNextPage()
        }

        override fun gotoNextPage() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permissions, REQUEST_REQUEST_PERMISSIONS)
            } else {
                // Try getting location, some custom rom will popup requirement dialog
                Utils.getCachedLocation(activity)
                super.gotoNextPage()
            }
        }
    }

    class WizardPageTabsFragment : BaseWizardPageFragment() {

        fun applyInitialTabSettings() {
            val a = activity
            if (a is SettingsWizardActivity) {
                a.applyInitialTabSettings()
            }
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            findPreference(WIZARD_PREFERENCE_KEY_EDIT_CUSTOM_TABS).onPreferenceClickListener = this
            findPreference(WIZARD_PREFERENCE_KEY_USE_DEFAULTS).onPreferenceClickListener = this
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            when (requestCode) {
                REQUEST_CUSTOM_TABS -> if (resultCode != Activity.RESULT_OK) {
                    TabsUnchangedDialogFragment().show(fragmentManager, "tabs_unchanged")
                } else {
                    gotoNextPage()
                }
            }
            super.onActivityResult(requestCode, resultCode, data)
        }

        override fun onPreferenceClick(preference: Preference): Boolean {
            val key = preference.key
            if (WIZARD_PREFERENCE_KEY_EDIT_CUSTOM_TABS == key) {
                val intent = Intent(activity, SettingsActivity::class.java)
                intent.putExtra(SettingsActivity.EXTRA_INITIAL_TAG, "tabs")
                startActivityForResult(intent, REQUEST_CUSTOM_TABS)
            } else if (WIZARD_PREFERENCE_KEY_USE_DEFAULTS == key) {
                applyInitialTabSettings()
            }
            return true
        }

        override val headerSummary: Int
            get() = R.string.wizard_page_tabs_text

        override val headerTitle: Int
            get() = R.string.tabs

        override val nextPageTitle: Int
            get() = 0

        override val preferenceResource: Int
            get() = R.xml.settings_wizard_page_tab

        class TabsUnchangedDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

            override fun onCancel(dialog: DialogInterface?) {
                gotoNextPage()
            }

            override fun onClick(dialog: DialogInterface, which: Int) {
                gotoNextPage()
            }

            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                val builder = AlertDialog.Builder(activity)
                builder.setMessage(R.string.wizard_page_tabs_unchanged_message)
                builder.setPositiveButton(android.R.string.ok, this)
                return builder.create()
            }

            private fun gotoNextPage() {
                val a = activity
                if (a is SettingsWizardActivity) {
                    a.gotoNextPage()
                }
            }

        }

        companion object {

            private val REQUEST_CUSTOM_TABS = 1
        }
    }

    class WizardPageThemeFragment : BaseWizardPageFragment(), Preference.OnPreferenceClickListener {

        override val headerSummary: Int
            get() = R.string.wizard_page_theme_text

        override val headerTitle: Int
            get() = R.string.theme

        override val preferenceResource: Int
            get() = R.xml.preferences_theme
    }

    class WizardPageWelcomeFragment : BaseWizardPageFragment(), Preference.OnPreferenceClickListener {

        fun applyInitialSettings() {
            val a = activity
            if (a is SettingsWizardActivity) {
                a.applyInitialSettings()
            }
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            findPreference(WIZARD_PREFERENCE_KEY_NEXT_PAGE).onPreferenceClickListener = this
            findPreference(WIZARD_PREFERENCE_KEY_USE_DEFAULTS).onPreferenceClickListener = this
            findPreference(WIZARD_PREFERENCE_KEY_IMPORT_SETTINGS).onPreferenceClickListener = this
        }

        override fun onPreferenceClick(preference: Preference): Boolean {
            val key = preference.key
            if (WIZARD_PREFERENCE_KEY_NEXT_PAGE == key) {
                gotoNextPage()
            } else if (WIZARD_PREFERENCE_KEY_USE_DEFAULTS == key) {
                applyInitialSettings()
            } else if (WIZARD_PREFERENCE_KEY_IMPORT_SETTINGS == key) {
                openImportSettingsDialog()
            }
            return true
        }

        override val headerSummary: Int
            get() = R.string.wizard_page_welcome_text

        override val headerTitle: Int
            get() = R.string.wizard_page_welcome_title

        override val nextPageTitle: Int
            get() = 0

        override val preferenceResource: Int
            get() = R.xml.settings_wizard_page_welcome

        private fun openImportSettingsDialog() {
            val a = activity
            if (a is SettingsWizardActivity) {
                a.openImportSettingsDialog()
            }
        }
    }

    internal abstract class AbsInitialSettingsTask(protected val activity: SettingsWizardActivity) : AsyncTask<Any, Any, Boolean>() {

        override fun doInBackground(vararg params: Any): Boolean? {
            val resolver = activity.contentResolver
            val tabs = CustomTabUtils.getHomeTabs(activity)
            if (wasConfigured(tabs)) return true
            Collections.sort(tabs)
            var i = 0
            val values_list = ArrayList<ContentValues>()
            for (type in DEFAULT_TAB_TYPES) {
                val values = ContentValues()
                val conf = CustomTabUtils.getTabConfiguration(type)
                values.put(Tabs.TYPE, type)
                values.put(Tabs.ICON, CustomTabUtils.findTabIconKey(conf.defaultIcon))
                values.put(Tabs.POSITION, i++)
                values_list.add(values)
            }
            for (spec in tabs) {
                val type = CustomTabUtils.findTabType(spec.cls)
                if (type != null) {
                    val values = ContentValues()
                    values.put(Tabs.TYPE, type)
                    values.put(Tabs.ARGUMENTS, InternalParseUtils.bundleToJSON(spec.args))
                    values.put(Tabs.NAME, ParseUtils.parseString(spec.name))
                    val icon = spec.icon
                    if (icon is Int) {
                        values.put(Tabs.ICON, CustomTabUtils.findTabIconKey(icon))
                    } else if (icon is File) {
                        values.put(Tabs.ICON, icon.path)
                    }
                    values.put(Tabs.POSITION, i++)
                }
            }
            resolver.delete(Tabs.CONTENT_URI, null, null)
            resolver.bulkInsert(Tabs.CONTENT_URI, values_list.toTypedArray())
            return true
        }

        protected abstract fun nextStep()

        override fun onPostExecute(result: Boolean?) {
            activity.executeAfterFragmentResumed {
                val activity = it as SettingsWizardActivity
                val fm = activity.supportFragmentManager
                val f = fm.findFragmentByTag(FRAGMENT_TAG) as? DialogFragment
                f?.dismiss()
            }
            nextStep()
        }

        override fun onPreExecute() {
            activity.executeAfterFragmentResumed {
                val activity = it as SettingsWizardActivity
                ProgressDialogFragment.show(activity.supportFragmentManager, FRAGMENT_TAG).isCancelable = false
            }
        }

        private fun wasConfigured(tabs: List<SupportTabSpec>): Boolean {
            for (spec in tabs) {
                if (spec.cls == HomeTimelineFragment::class.java
                        || spec.cls == InteractionsTimelineFragment::class.java
                        || spec.cls == DirectMessagesFragment::class.java
                        || spec.cls == MessagesEntriesFragment::class.java)
                    return true
            }
            return false
        }

        companion object {

            private val FRAGMENT_TAG = "initial_settings_dialog"

            private val DEFAULT_TAB_TYPES = arrayOf(CustomTabType.HOME_TIMELINE, CustomTabType.NOTIFICATIONS_TIMELINE, CustomTabType.TRENDS_SUGGESTIONS, CustomTabType.DIRECT_MESSAGES)
        }

    }

    internal class InitialSettingsTask(activity: SettingsWizardActivity) : AbsInitialSettingsTask(activity) {

        override fun nextStep() {
            val activity = activity
            activity.gotoPage(activity.pageCount - 3)
        }

    }

    private fun gotoPage(page: Int) {
        viewPager!!.currentItem = TwidereMathUtils.clamp(page, 0, pageCount - 1)
    }

    private val pageCount: Int
        get() = adapter!!.count

    internal class InitialTabSettingsTask(activity: SettingsWizardActivity) : AbsInitialSettingsTask(activity) {

        override fun nextStep() {
            activity.gotoNextPage()
        }

    }

    companion object {

        val WIZARD_PREFERENCE_KEY_NEXT_PAGE = "next_page"
        val WIZARD_PREFERENCE_KEY_USE_DEFAULTS = "use_defaults"
        val WIZARD_PREFERENCE_KEY_EDIT_CUSTOM_TABS = "edit_custom_tabs"
        val WIZARD_PREFERENCE_KEY_IMPORT_SETTINGS = "import_settings"

        private val REQUEST_IMPORT_SETTINGS = 201
    }

}
