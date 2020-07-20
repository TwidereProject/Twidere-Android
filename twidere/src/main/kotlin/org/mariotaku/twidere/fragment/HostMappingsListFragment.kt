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
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.AdapterView
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.ListView
import android.widget.TextView
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.dialog_add_host_mapping.*
import kotlinx.android.synthetic.main.dialog_user_list_detail_editor.*
import kotlinx.android.synthetic.main.fragment_content_listview.*
import org.mariotaku.ktextension.empty
import org.mariotaku.ktextension.string
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.HOST_MAPPING_PREFERENCES_NAME
import org.mariotaku.twidere.adapter.ArrayAdapter
import org.mariotaku.twidere.extension.applyOnShow
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.positive
import org.mariotaku.twidere.util.net.TwidereDns

class HostMappingsListFragment : AbsContentListViewFragment<HostMappingsListFragment.HostMappingAdapter>(),
        AdapterView.OnItemClickListener, MultiChoiceModeListener, OnSharedPreferenceChangeListener {

    private lateinit var hostMapping: SharedPreferences

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        hostMapping = requireActivity().getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE)
        hostMapping.registerOnSharedPreferenceChangeListener(this)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        listView.setMultiChoiceModeListener(this)
        reloadHostMappings()
    }

    override fun onCreateAdapter(context: Context, requestManager: RequestManager): HostMappingAdapter {
        return HostMappingAdapter(requireActivity())
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
                val array = listView.checkedItemPositions ?: return false
                (dns as? TwidereDns)?.beginMappingTransaction {
                    (0 until array.size()).filter {
                        array.valueAt(it)
                    }.forEach {
                        remove(adapter.getItem(it).first)
                    }
                }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_host_mapping, menu)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val (host, address) = adapter.getItem(position)
        val args = Bundle()
        args.putString(EXTRA_HOST, host)
        args.putString(EXTRA_ADDRESS, address)
        args.putBoolean(EXTRA_EXCLUDED, host == address)
        args.putBoolean(EXTRA_EDIT_MODE, true)
        val df = AddMappingDialogFragment()
        df.arguments = args
        parentFragmentManager.let { df.show(it, "add_mapping") }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                val df = AddMappingDialogFragment()
                parentFragmentManager.let { df.show(it, "add_mapping") }
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
        adapter.clear()
        adapter.addAll(hostMapping.all.mapNotNull { entry ->
            val value = entry.value?.toString() ?: return@mapNotNull null
            return@mapNotNull Pair(entry.key, value)
        })
        if (adapter.isEmpty) {
            showEmpty(R.drawable.ic_info_info_generic, getString(R.string.add_host_mapping))
        } else {
            showContent()
        }
    }

    private fun updateTitle(mode: ActionMode?) {
        if (listView == null || mode == null || activity == null) return
        val count = listView.checkedItemCount
        mode.title = resources.getQuantityString(R.plurals.Nitems_selected, count, count)
    }

    class AddMappingDialogFragment : BaseDialogFragment(), TextWatcher, OnCheckedChangeListener {


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

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context = activity
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(R.layout.dialog_add_host_mapping)
            builder.setTitle(R.string.add_host_mapping)
            builder.positive(android.R.string.ok, this::onPositiveClick)
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.applyOnShow {
                applyTheme()
                editHost.addTextChangedListener(this@AddMappingDialogFragment)
                editAddress.addTextChangedListener(this@AddMappingDialogFragment)
                isExcluded.setOnCheckedChangeListener(this@AddMappingDialogFragment)
                val args = arguments
                if (args != null) {
                    editHost.isEnabled = !args.getBoolean(EXTRA_EDIT_MODE, false)
                    if (savedInstanceState == null) {
                        editHost.setText(args.getCharSequence(EXTRA_HOST))
                        editAddress.setText(args.getCharSequence(EXTRA_ADDRESS))
                        isExcluded.isChecked = args.getBoolean(EXTRA_EXCLUDED)
                    }
                }
                updateButton()
            }
            return dialog
        }

        private fun onPositiveClick(dialog: Dialog) {
            val host = dialog.editHost.string.takeUnless(String?::isNullOrEmpty) ?: return
            val address = (if (dialog.isExcluded.isChecked) {
                host
            } else {
                dialog.editAddress.string
            }).takeUnless(String?::isNullOrEmpty) ?: return
            (dns as? TwidereDns)?.putMapping(host, address)
        }

        override fun onSaveInstanceState(outState: Bundle) {
            (dialog as? AlertDialog)?.let {
                outState.putCharSequence(EXTRA_HOST, it.editHost.text)
                outState.putCharSequence(EXTRA_ADDRESS, it.editAddress.text)
                outState.putCharSequence(EXTRA_EXCLUDED, it.isPublic.text)
            }
            super.onSaveInstanceState(outState)
        }

        private fun updateAddressField() {
            val dialog = dialog as AlertDialog
            dialog.editAddress.visibility = if (dialog.isExcluded.isChecked) View.GONE else View.VISIBLE
        }

        private fun updateButton() {
            val dialog = dialog as AlertDialog
            val hostValid = !dialog.editHost.empty
            val addressValid = !dialog.editAddress.empty || dialog.isExcluded.isChecked
            val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            positiveButton.isEnabled = hostValid && addressValid
        }
    }

    class HostMappingAdapter(context: Context) : ArrayAdapter<Pair<String, String>>(context,
            android.R.layout.simple_list_item_activated_2) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val text1 = view.findViewById<TextView>(android.R.id.text1)
            val text2 = view.findViewById<TextView>(android.R.id.text2)
            val (key, value) = getItem(position)
            text1.text = key
            if (key == value) {
                text2.setText(R.string.excluded)
            } else {
                text2.text = value
            }
            return view
        }
    }

    companion object {

        private const val EXTRA_EDIT_MODE = "edit_mode"
        private const val EXTRA_HOST = "host"
        private const val EXTRA_ADDRESS = "address"
        private const val EXTRA_EXCLUDED = "excluded"
    }

}
