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

package org.mariotaku.twidere.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextUtils.isEmpty
import android.text.TextWatcher
import android.view.*
import android.widget.*
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.CompoundButton.OnCheckedChangeListener
import org.apache.commons.lang3.StringUtils
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.HOST_MAPPING_PREFERENCES_NAME
import org.mariotaku.twidere.adapter.ArrayAdapter
import org.mariotaku.twidere.util.ParseUtils
import org.mariotaku.twidere.util.SharedPreferencesWrapper

class HostMappingsListFragment : BaseListFragment(), MultiChoiceModeListener, OnSharedPreferenceChangeListener {

    private var mAdapter: HostMappingAdapter? = null
    private var mHostMapping: SharedPreferencesWrapper? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        mHostMapping = SharedPreferencesWrapper.getInstance(activity,
                Constants.HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE)
        mHostMapping!!.registerOnSharedPreferenceChangeListener(this)
        mAdapter = HostMappingAdapter(activity)
        listAdapter = mAdapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        listView.setMultiChoiceModeListener(this)
        reloadHostMappings()
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.action_multi_select_items, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        updateTitle(mode)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                val editor = mHostMapping!!.edit()
                val array = listView!!.checkedItemPositions ?: return false
                var i = 0
                val size = array.size()
                while (i < size) {
                    if (array.valueAt(i)) {
                        editor.remove(mAdapter!!.getItem(i))
                    }
                    i++
                }
                editor.apply()
                reloadHostMappings()
            }
            else -> {
                return false
            }
        }
        mode.finish()
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_host_mapping, menu)
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        val host = mAdapter!!.getItem(position)
        val address = mAdapter!!.getAddress(host)
        val args = Bundle()
        args.putString(EXTRA_HOST, host)
        args.putString(EXTRA_ADDRESS, address)
        args.putBoolean(EXTRA_EXCLUDED, StringUtils.equals(host, address))
        args.putBoolean(EXTRA_EDIT_MODE, true)
        val df = AddMappingDialogFragment()
        df.arguments = args
        df.show(fragmentManager, "add_mapping")
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.add -> {
                val df = AddMappingDialogFragment()
                df.show(fragmentManager, "add_mapping")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long,
                                           checked: Boolean) {
        updateTitle(mode)
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        reloadHostMappings()
    }

    fun reloadHostMappings() {
        if (mAdapter == null) return
        mAdapter!!.reload()
    }

    private fun updateTitle(mode: ActionMode?) {
        if (listView == null || mode == null || activity == null) return
        val count = listView!!.checkedItemCount
        mode.title = resources.getQuantityString(R.plurals.Nitems_selected, count, count)
    }

    class AddMappingDialogFragment : BaseDialogFragment(), OnClickListener, TextWatcher, OnCheckedChangeListener {


        private var mEditHost: EditText? = null
        private var mEditAddress: EditText? = null
        private var mCheckExclude: CheckBox? = null

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            updateButton()
        }

        override fun afterTextChanged(s: Editable) {

        }

        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            updateAddressField()
            updateButton()
        }

        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val host = ParseUtils.parseString(mEditHost!!.text)
                    val address = if (mCheckExclude!!.isChecked) host else ParseUtils.parseString(mEditAddress!!.text)
                    if (isEmpty(host) || isEmpty(address)) return
                    val prefs = context.getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME,
                            Context.MODE_PRIVATE)
                    val editor = prefs.edit()
                    editor.putString(host, address)
                    editor.apply()
                }
            }

        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context = activity
            val builder = AlertDialog.Builder(context)
            builder.setView(R.layout.dialog_host_mapping)
            builder.setTitle(R.string.add_host_mapping)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.setOnShowListener { dialog ->
                val alertDialog = dialog as AlertDialog
                mEditHost = alertDialog.findViewById(R.id.host) as EditText?
                mEditAddress = alertDialog.findViewById(R.id.address) as EditText?
                mCheckExclude = alertDialog.findViewById(R.id.exclude) as CheckBox?
                mEditHost!!.addTextChangedListener(this@AddMappingDialogFragment)
                mEditAddress!!.addTextChangedListener(this@AddMappingDialogFragment)
                mCheckExclude!!.setOnCheckedChangeListener(this@AddMappingDialogFragment)
                val args = arguments
                if (args != null) {
                    mEditHost!!.isEnabled = !args.getBoolean(EXTRA_EDIT_MODE, false)
                    if (savedInstanceState == null) {
                        mEditHost!!.setText(args.getCharSequence(EXTRA_HOST))
                        mEditAddress!!.setText(args.getCharSequence(EXTRA_ADDRESS))
                        mCheckExclude!!.isChecked = args.getBoolean(EXTRA_EXCLUDED)
                    }
                }
                updateButton()
            }
            return dialog
        }

        override fun onSaveInstanceState(outState: Bundle) {
            outState.putCharSequence(EXTRA_HOST, mEditHost!!.text)
            outState.putCharSequence(EXTRA_ADDRESS, mEditAddress!!.text)
            outState.putCharSequence(EXTRA_EXCLUDED, mEditAddress!!.text)
            super.onSaveInstanceState(outState)
        }

        private fun updateAddressField() {
            mEditAddress!!.visibility = if (mCheckExclude!!.isChecked) View.GONE else View.VISIBLE
        }

        private fun updateButton() {
            val dialog = dialog as AlertDialog
            val hostValid = !isEmpty(mEditHost!!.text)
            val addressValid = !isEmpty(mEditAddress!!.text) || mCheckExclude!!.isChecked
            val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            positiveButton.isEnabled = hostValid && addressValid
        }
    }

    internal class HostMappingAdapter(context: Context) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_activated_2) {

        private val mHostMapping: SharedPreferences

        init {
            mHostMapping = context.getSharedPreferences(Constants.HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val text1 = view.findViewById(android.R.id.text1) as TextView
            val text2 = view.findViewById(android.R.id.text2) as TextView
            val key = getItem(position)
            text1.text = key
            val value = getAddress(key)
            if (StringUtils.equals(key, value)) {
                text2.setText(R.string.excluded)
            } else {
                text2.text = value
            }
            return view
        }

        fun reload() {
            clear()
            val all = mHostMapping.all
            addAll(all.keys)
        }

        fun getAddress(key: String): String {
            return mHostMapping.getString(key, null)
        }
    }

    companion object {

        private val EXTRA_EDIT_MODE = "edit_mode"
        private val EXTRA_HOST = "host"
        private val EXTRA_ADDRESS = "address"
        private val EXTRA_EXCLUDED = "excluded"
    }

}
