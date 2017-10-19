/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.fragment.filter

import android.accounts.AccountManager
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.Toast
import kotlinx.android.synthetic.main.dialog_filter_rule_editor.*
import org.mariotaku.ktextension.ContentValues
import org.mariotaku.ktextension.set
import org.mariotaku.ktextension.string
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.ComposeAutoCompleteAdapter
import org.mariotaku.twidere.adapter.SourceAutoCompleteAdapter
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.extension.util.isAdvancedFiltersEnabled
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.ExtraFeaturesIntroductionDialogFragment
import org.mariotaku.twidere.model.filter.FilterScopesHolder
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.util.premium.ExtraFeaturesService

class AddEditItemFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    private val contentUri: Uri
        get() = arguments.getParcelable(EXTRA_URI)

    private val rowId: Long
        get() = arguments.getLong(EXTRA_ID, -1)

    private val defaultValue: String?
        get() = arguments.getString(EXTRA_VALUE)

    private val defaultScopes: FilterScopesHolder
        get() = FilterScopesHolder(filterMasks, arguments.getInt(EXTRA_SCOPE, FilterScope.DEFAULT))

    private val filterMasks: Int
        get() = when (contentUri) {
            Filters.Users.CONTENT_URI -> FilterScope.VALID_MASKS_USERS
            Filters.Keywords.CONTENT_URI -> FilterScope.VALID_MASKS_KEYWORDS
            Filters.Sources.CONTENT_URI -> FilterScope.VALID_MASKS_SOURCES
            Filters.Links.CONTENT_URI -> FilterScope.VALID_MASKS_LINKS
            else -> 0
        }

    private val canEditValue: Boolean
        get() = contentUri != Filters.Users.CONTENT_URI

    private var Dialog.value: String?
        get() = editText.string?.takeIf(String::isNotEmpty)
        set(value) {
            editText.string = value
        }

    private var Dialog.scopes: FilterScopesHolder?
        get() = defaultScopes.also {
            if (extraFeaturesService.isAdvancedFiltersEnabled) {
                saveScopes(it)
            }
        }
        set(value) {
            loadScopes(value ?: defaultScopes)
        }

    private var Dialog.advancedExpanded: Boolean
        get() = advancedContainer.visibility == View.VISIBLE
        set(value) {
            advancedContainer.setVisible(value)
            advancedCollapseIndicator.rotation = if (value) 90f else 0f
        }

    override fun onClick(dialog: DialogInterface, which: Int) {
        dialog as AlertDialog
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val scope = dialog.scopes ?: return
                if (!canEditValue) {
                    saveScopeOnly(scope)
                } else {
                    val value = dialog.value ?: return
                    saveItem(value, scope)
                }
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_filter_rule_editor)

        if (arguments.getLong(EXTRA_ID, -1) >= 0) {
            builder.setTitle(R.string.action_edit_filter_rule)
        } else {
            builder.setTitle(R.string.action_add_filter_rule)
        }
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, this)
        val dialog = builder.create()
        dialog.applyOnShow {
            applyTheme()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            editText.setAdapter(when (contentUri) {
                Filters.Sources.CONTENT_URI -> SourceAutoCompleteAdapter(activity)
                Filters.Users.CONTENT_URI -> ComposeAutoCompleteAdapter(activity, requestManager).apply {
                    val am = AccountManager.get(activity)
                    account = AccountUtils.getDefaultAccountDetails(activity, am, false)
                }
                else -> null
            })
            editText.threshold = 1
            editText.isEnabled = canEditValue
            advancedToggle.setOnClickListener {
                advancedExpanded = !advancedExpanded
            }
            advancedContainer.children.filter { it is CheckBox }.forEach {
                val checkBox = it as CheckBox
                checkBox.setOnClickListener onClick@ {
                    if (extraFeaturesService.isAdvancedFiltersEnabled) return@onClick
                    // Revert check state
                    checkBox.isChecked = !checkBox.isChecked
                    val df = ExtraFeaturesIntroductionDialogFragment.create(
                            ExtraFeaturesService.FEATURE_ADVANCED_FILTERS)
                    df.setTargetFragment(this@AddEditItemFragment, REQUEST_CHANGE_SCOPE_PURCHASE)
                    df.show(fragmentManager, ExtraFeaturesIntroductionDialogFragment.FRAGMENT_TAG)
                }
            }

            if (savedInstanceState == null) {
                value = defaultValue
                scopes = defaultScopes
                advancedExpanded = false
                editText.setSelection(editText.length().coerceAtLeast(0))
                if (editText.isEnabled) {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE)
                            as InputMethodManager
                    imm.showSoftInput(editText, 0)
                }
            } else {
                value = savedInstanceState.getString(EXTRA_VALUE)
                scopes = savedInstanceState.getParcelable(EXTRA_SCOPE)
            }
        }
        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EXTRA_VALUE, dialog.value)
        outState.putParcelable(EXTRA_SCOPE, dialog.scopes)
    }

    private fun Dialog.saveScopes(scopes: FilterScopesHolder) {
        targetText.saveScope(scopes, FilterScope.TARGET_TEXT)
        targetName.saveScope(scopes, FilterScope.TARGET_NAME)
        targetDescription.saveScope(scopes, FilterScope.TARGET_DESCRIPTION)
        scopeHome.saveScope(scopes, FilterScope.HOME)
        scopeInteractions.saveScope(scopes, FilterScope.INTERACTIONS)
        scopeMessages.saveScope(scopes, FilterScope.MESSAGES)
        scopeSearchResults.saveScope(scopes, FilterScope.SEARCH_RESULTS)
        scopeOther.saveScope(scopes, FilterScope.UGC_TIMELINE)
    }

    private fun Dialog.loadScopes(scopes: FilterScopesHolder) {
        labelTarget.setVisible(scopes.hasMask(FilterScope.MASK_TARGET))
        targetText.loadScope(scopes, FilterScope.TARGET_TEXT)
        targetName.loadScope(scopes, FilterScope.TARGET_NAME)
        targetDescription.loadScope(scopes, FilterScope.TARGET_DESCRIPTION)

        labelScope.setVisible(scopes.hasMask(FilterScope.MASK_SCOPE))
        scopeHome.loadScope(scopes, FilterScope.HOME)
        scopeInteractions.loadScope(scopes, FilterScope.INTERACTIONS)
        scopeMessages.loadScope(scopes, FilterScope.MESSAGES)
        scopeSearchResults.loadScope(scopes, FilterScope.SEARCH_RESULTS)
        scopeOther.loadScope(scopes, FilterScope.UGC_TIMELINE)
    }

    private fun CheckBox.saveScope(scopes: FilterScopesHolder, scope: Int) {
        if (!isEnabled || visibility != View.VISIBLE) return
        scopes[scope] = isChecked
    }

    private fun CheckBox.loadScope(scopes: FilterScopesHolder, scope: Int) {
        if (scope in scopes) {
            isEnabled = true
            visibility = View.VISIBLE
        } else {
            isEnabled = false
            visibility = View.GONE
            return
        }
        isChecked = scopes[scope]
    }

    private fun saveScopeOnly(scopes: FilterScopesHolder) {
        val resolver = context.contentResolver
        val contentUri = contentUri
        val rowId = rowId

        if (rowId < 0) return

        val values = ContentValues {
            this[Filters.SCOPE] = scopes.value
        }
        val idWhere = Expression.equals(Filters._ID, rowId).sql
        resolver.update(contentUri, values, idWhere, null)
    }

    private fun saveItem(value: String, scopes: FilterScopesHolder) {
        val resolver = context.contentResolver
        val uri = contentUri
        val rowId = rowId
        val values = ContentValues {
            this[Filters.VALUE] = value
            this[Filters.SCOPE] = scopes.value
        }
        if (rowId >= 0) {
            val valueWhere = Expression.equalsArgs(Filters.VALUE).sql
            val valueWhereArgs = arrayOf(value)
            val matchedId = resolver.queryLong(uri, Filters._ID, valueWhere, valueWhereArgs,
                    -1)
            if (matchedId != -1L && matchedId != rowId) {
                Toast.makeText(context, R.string.message_toast_duplicate_filter_rule,
                        Toast.LENGTH_SHORT).show()
            } else {
                val idWhere = Expression.equals(Filters._ID, rowId).sql
                resolver.update(uri, values, idWhere, null)
            }
        } else {
            resolver.insert(uri, values)
        }
    }

    companion object {
        private const val REQUEST_CHANGE_SCOPE_PURCHASE = 101
    }

}