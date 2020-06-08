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

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.XmlRes
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceFragmentCompat.OnPreferenceStartFragmentCallback
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import kotlinx.android.synthetic.main.activity_settings.*
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.ACTION_NAVIGATION_BACK
import org.mariotaku.twidere.constant.KeyboardShortcutConstants.CONTEXT_TAG_NAVIGATION
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.fragment.*
import org.mariotaku.twidere.util.DeviceUtils
import org.mariotaku.twidere.util.KeyboardShortcutsHandler
import org.mariotaku.twidere.util.ThemeUtils
import java.util.*
import kotlin.system.exitProcess

class SettingsActivity : BaseActivity(), OnItemClickListener, OnPreferenceStartFragmentCallback {

    var shouldRecreate: Boolean = false
    var shouldRestart: Boolean = false
    var shouldTerminate: Boolean = false
    private lateinit var entriesAdapter: EntriesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        entriesAdapter = EntriesAdapter(this)

        if (savedInstanceState != null) {
            shouldRecreate = savedInstanceState.getBoolean(EXTRA_SHOULD_RECREATE, shouldRecreate)
            shouldRestart = savedInstanceState.getBoolean(EXTRA_SHOULD_RESTART, shouldRestart)
            shouldTerminate = savedInstanceState.getBoolean(EXTRA_SHOULD_TERMINATE, shouldTerminate)
        } else if (intent.getBooleanExtra(EXTRA_SHOULD_TERMINATE, false)) {
            finishNoRestart()
            exitProcess(0)
        }

        val backgroundOption = currentThemeBackgroundOption
        val backgroundAlpha = currentThemeBackgroundAlpha

        detailFragmentContainer.setBackgroundColor(ThemeUtils.getColorBackground(this,
                backgroundOption, backgroundAlpha))

        slidingPane.setShadowResourceLeft(R.drawable.sliding_pane_shadow_left)
        slidingPane.setShadowResourceRight(R.drawable.sliding_pane_shadow_right)
        slidingPane.sliderFadeColor = 0

        ViewCompat.setOnApplyWindowInsetsListener(slidingPane) listener@ { view, insets ->
            onApplyWindowInsets(view, insets)
            entriesList.setPadding(0, insets.systemWindowInsetTop, 0, insets.systemWindowInsetBottom)
            return@listener insets
        }

        initEntries()
        entriesList.adapter = entriesAdapter
        entriesList.choiceMode = AbsListView.CHOICE_MODE_SINGLE
        entriesList.onItemClickListener = this

        if (savedInstanceState == null) {
            val initialTag = intent.data?.authority
            var initialItem = -1
            var firstEntry = -1
            for (i in 0 until entriesAdapter.count) {
                val entry = entriesAdapter.getItem(i)
                if (entry is PreferenceEntry) {
                    if (firstEntry == -1) {
                        firstEntry = i
                    }
                    if (initialTag == entry.tag) {
                        initialItem = i
                        break
                    }
                }
            }
            if (initialItem == -1) {
                initialItem = firstEntry
            }
            if (initialItem != -1) {
                openDetails(initialItem)
                entriesList.setItemChecked(initialItem, true)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_SETTINGS_CHANGED && data != null) {
            shouldRecreate = data.getBooleanExtra(EXTRA_SHOULD_RECREATE, false)
            shouldRestart = data.getBooleanExtra(EXTRA_SHOULD_RESTART, false)
            shouldTerminate = data.getBooleanExtra(EXTRA_SHOULD_TERMINATE, false)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun finish() {
        if (shouldRecreate || shouldRestart) {
            val data = Intent()
            data.putExtra(EXTRA_SHOULD_RECREATE, shouldRecreate)
            data.putExtra(EXTRA_SHOULD_RESTART, shouldRestart)
            setResult(RESULT_SETTINGS_CHANGED, data)
        }
        super.finish()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(EXTRA_SHOULD_RECREATE, shouldRecreate)
        outState.putBoolean(EXTRA_SHOULD_RESTART, shouldRestart)
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (ACTION_NAVIGATION_BACK == action) {
            onBackPressed()
            return true
        }
        return super.handleKeyboardShortcutSingle(handler, keyCode, event, metaState)
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        return ACTION_NAVIGATION_BACK == action
    }

    override fun onSupportNavigateUp(): Boolean {
        if (notifyUnsavedChange()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (notifyUnsavedChange()) return
        super.onBackPressed()
    }

    override fun onPreferenceStartFragment(fragment: PreferenceFragmentCompat, preference: Preference): Boolean {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        val f = Fragment.instantiate(this, preference.fragment, preference.extras)
        ft.replace(R.id.detailFragmentContainer, f)
        ft.addToBackStack(preference.title.toString())
        ft.commit()
        return true
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        openDetails(position)
    }

    private fun finishNoRestart() {
        super.finish()
    }


    private val isTopSettings: Boolean = true

    private fun initEntries() {
        entriesAdapter.addHeader(getString(R.string.appearance))
        entriesAdapter.addPreference("theme", R.drawable.ic_action_color_palette, getString(R.string.theme),
                R.xml.preferences_theme)
        entriesAdapter.addPreference("cards", R.drawable.ic_action_card, getString(R.string.cards),
                R.xml.preferences_cards)
        if (DeviceUtils.isDeviceTablet(this)) {
            entriesAdapter.addPreference("tablet_mode", R.drawable.ic_action_tablet, getString(R.string.preference_title_tablet_mode),
                    R.xml.preferences_tablet_mode)
        }

        entriesAdapter.addHeader(getString(R.string.function))
        entriesAdapter.addPreference("tabs", R.drawable.ic_action_tab, getString(R.string.tabs),
                CustomTabsFragment::class.java)
        entriesAdapter.addPreference("extension", R.drawable.ic_action_extension, getString(R.string.extensions),
                ExtensionsListFragment::class.java)
        entriesAdapter.addPreference("refresh", R.drawable.ic_action_refresh, getString(R.string.action_refresh),
                R.xml.preferences_refresh)
        entriesAdapter.addPreference("notifications", R.drawable.ic_action_notification, getString(R.string.settings_notifications),
                R.xml.preferences_notifications)
        entriesAdapter.addPreference("network", R.drawable.ic_action_web, getString(R.string.network),
                R.xml.preferences_network)
        entriesAdapter.addPreference("compose", R.drawable.ic_action_status_compose, getString(R.string.action_compose),
                R.xml.preferences_compose)
        entriesAdapter.addPreference("content", R.drawable.ic_action_twidere_square, getString(R.string.content),
                R.xml.preferences_content)
        entriesAdapter.addPreference("storage", R.drawable.ic_action_storage, getString(R.string.preference_title_storage),
                R.xml.preferences_storage)
        entriesAdapter.addPreference("other", R.drawable.ic_action_more_horizontal, getString(R.string.other_settings),
                R.xml.preferences_other)

        entriesAdapter.addHeader(getString(R.string.title_about))
        entriesAdapter.addPreference("about", R.drawable.ic_action_info, getString(R.string.title_about),
                R.xml.preferences_about)
        val browserArgs = Bundle()
        browserArgs.putString(EXTRA_URI, "file:///android_asset/gpl-3.0-standalone.html")
        entriesAdapter.addPreference("license", R.drawable.ic_action_open_source, getString(R.string.title_open_source_license),
                BrowserFragment::class.java, browserArgs)
    }

    private fun openDetails(position: Int) {
        if (isFinishing) return
        val entry = entriesAdapter.getItem(position) as? PreferenceEntry ?: return
        val fm = supportFragmentManager
        fm.popBackStackImmediate(null, 0)
        val ft = fm.beginTransaction()
        if (entry.preference != 0) {
            val args = Bundle()
            args.putInt(EXTRA_RESID, entry.preference)
            val f = Fragment.instantiate(this, SettingsDetailsFragment::class.java.name,
                    args)
            ft.replace(R.id.detailFragmentContainer, f)
        } else if (entry.fragment != null) {
            ft.replace(R.id.detailFragmentContainer, Fragment.instantiate(this, entry.fragment,
                    entry.args))
        }
        ft.setBreadCrumbTitle(entry.title)
        ft.commit()
        slidingPane.closePane()
    }

    private fun notifyUnsavedChange(): Boolean {
        if (isTopSettings && (shouldRecreate || shouldRestart || shouldTerminate)) {
            executeAfterFragmentResumed {
                if (it.isFinishing) return@executeAfterFragmentResumed
                it as SettingsActivity
                val df = RestartConfirmDialogFragment()
                df.arguments = Bundle {
                    this[EXTRA_SHOULD_RECREATE] = it.shouldRecreate
                    this[EXTRA_SHOULD_RESTART] = it.shouldRestart
                    this[EXTRA_SHOULD_TERMINATE] = it.shouldTerminate
                }
                df.show(it.supportFragmentManager, "restart_confirm")
            }
            return true
        }
        return false
    }

    internal class EntriesAdapter(context: Context) : BaseAdapter() {
        private val inflater: LayoutInflater = LayoutInflater.from(context)
        private val entries: MutableList<Entry> = ArrayList()

        fun addPreference(tag: String, @DrawableRes icon: Int, title: String, @XmlRes preference: Int) {
            entries.add(PreferenceEntry(tag, icon, title, preference, null, null))
            notifyDataSetChanged()
        }


        fun addPreference(tag: String, @DrawableRes icon: Int, title: String, cls: Class<out Fragment>,
                          args: Bundle? = null) {
            entries.add(PreferenceEntry(tag, icon, title, 0, cls.name, args))
            notifyDataSetChanged()
        }

        fun addHeader(title: String) {
            entries.add(HeaderEntry(title))
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return entries.size
        }

        override fun getItem(position: Int): Entry {
            return entries[position]
        }

        override fun getItemId(position: Int): Long {
            return getItem(position).hashCode().toLong()
        }

        override fun isEnabled(position: Int): Boolean {
            return getItemViewType(position) == VIEW_TYPE_PREFERENCE_ENTRY
        }

        override fun getViewTypeCount(): Int {
            return 2
        }

        override fun getItemViewType(position: Int): Int {
            val entry = getItem(position)
            if (entry is PreferenceEntry) {
                return VIEW_TYPE_PREFERENCE_ENTRY
            } else if (entry is HeaderEntry) {
                return VIEW_TYPE_HEADER_ENTRY
            }
            throw UnsupportedOperationException()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val viewType = getItemViewType(position)
            val entry = getItem(position)
            val view: View = convertView ?: let {
                when (viewType) {
                    VIEW_TYPE_PREFERENCE_ENTRY -> {
                        return@let inflater.inflate(R.layout.list_item_preference_header_item, parent, false)
                    }
                    VIEW_TYPE_HEADER_ENTRY -> {
                        return@let inflater.inflate(R.layout.list_item_preference_header_category, parent, false)
                    }
                    else -> {
                        throw UnsupportedOperationException()
                    }
                }
            }
            entry.bind(view)
            return view
        }

        companion object {

            const val VIEW_TYPE_PREFERENCE_ENTRY = 0
            const val VIEW_TYPE_HEADER_ENTRY = 1
        }
    }

    internal abstract class Entry {
        abstract fun bind(view: View)

    }

    internal class PreferenceEntry(
            val tag: String,
            val icon: Int,
            val title: String,
            val preference: Int,
            val fragment: String?,
            val args: Bundle?
    ) : Entry() {

        override fun bind(view: View) {
            view.findViewById<ImageView>(android.R.id.icon).setImageResource(icon)
            view.findViewById<TextView>(android.R.id.title).text = title
        }

    }

    internal class HeaderEntry(private val title: String) : Entry() {

        override fun bind(view: View) {
            val theme = Chameleon.getOverrideTheme(view.context, view.context)
            val textView: TextView = view.findViewById(android.R.id.title)
            textView.setTextColor(ThemeUtils.getOptimalAccentColor(theme.colorAccent,
                    theme.colorForeground))
            textView.text = title
        }
    }

    class RestartConfirmDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireActivity())
            if (arguments?.getBoolean(EXTRA_SHOULD_TERMINATE) == true) {
                builder.setMessage(R.string.app_terminate_confirm)
                builder.setNegativeButton(R.string.action_dont_terminate, this)
            } else {
                builder.setMessage(R.string.app_restart_confirm)
                builder.setNegativeButton(R.string.action_dont_restart, this)
            }
            builder.setPositiveButton(android.R.string.ok, this)
            val dialog = builder.create()
            dialog.onShow { it.applyTheme() }
            return dialog
        }

        override fun onClick(dialog: DialogInterface, which: Int) {
            val activity = activity as SettingsActivity
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    if (arguments?.getBoolean(EXTRA_SHOULD_TERMINATE) == true) {
                        val intent = Intent(context, SettingsActivity::class.java)
                        intent.putExtra(EXTRA_SHOULD_TERMINATE, true)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        activity.startActivity(intent)
                    } else {
                        activity.finish()
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    activity.finishNoRestart()
                }
            }
        }
    }

    companion object {

        private const val RESULT_SETTINGS_CHANGED = 10

        fun setShouldRecreate(activity: Activity) {
            if (activity !is SettingsActivity) return
            activity.shouldRecreate = true
        }

        fun setShouldRestart(activity: Activity) {
            if (activity !is SettingsActivity) return
            activity.shouldRestart = true
        }

        fun setShouldTerminate(activity: Activity) {
            if (activity !is SettingsActivity) return
            activity.shouldTerminate = true
        }
    }


}
