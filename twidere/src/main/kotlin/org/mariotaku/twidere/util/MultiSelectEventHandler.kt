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

package org.mariotaku.twidere.util

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.twitter.twittertext.Extractor
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.extension.model.getAccountUser
import org.mariotaku.twidere.menu.AccountActionProvider
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.util.content.ContentResolverUtils
import org.mariotaku.twidere.util.dagger.GeneralComponent
import java.util.*
import javax.inject.Inject

@SuppressLint("Registered")
class MultiSelectEventHandler(
        private val activity: BaseActivity
) : ActionMode.Callback, MultiSelectManager.Callback {

    @Inject
    lateinit var twitterWrapper: AsyncTwitterWrapper

    @Inject
    lateinit var multiSelectManager: MultiSelectManager

    private var actionMode: ActionMode? = null

    private var accountActionProvider: AccountActionProvider? = null

    init {
        GeneralComponent.get(activity).inject(this)
    }

    /**
     * Call before super.onCreate
     */
    fun dispatchOnCreate() {
    }

    /**
     * Call after super.onStart
     */
    fun dispatchOnStart() {
        multiSelectManager.registerCallback(this)
        updateMultiSelectState()
    }

    /**
     * Call before super.onStop
     */
    fun dispatchOnStop() {
        multiSelectManager.unregisterCallback(this)
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        val selectedItems = multiSelectManager.selectedItems
        if (selectedItems.isEmpty()) return false
        when (item.itemId) {
            R.id.reply -> {
                val extractor = Extractor()
                val am = AccountManager.get(activity)
                val intent = Intent(INTENT_ACTION_REPLY_MULTIPLE)
                val bundle = Bundle()
                val accountScreenNames = AccountUtils.getAccounts(am).map { it.getAccountUser(am).screen_name }
                val allMentions = TreeSet(String.CASE_INSENSITIVE_ORDER)
                for (selected in selectedItems) {
                    if (selected is ParcelableStatus) {
                        allMentions.add(selected.user_screen_name)
                        allMentions.addAll(extractor.extractMentionedScreennames(selected.text_plain))
                    } else if (selected is ParcelableUser) {
                        allMentions.add(selected.screen_name)
                    }
                }
                allMentions.removeAll(accountScreenNames)
                val firstObj = selectedItems[0]
                if (firstObj is ParcelableStatus) {
                    bundle.putString(EXTRA_IN_REPLY_TO_ID, firstObj.id)
                }
                bundle.putParcelable(EXTRA_ACCOUNT_KEY, multiSelectManager.accountKey)
                bundle.putStringArray(EXTRA_SCREEN_NAMES, allMentions.toTypedArray())
                intent.putExtras(bundle)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                activity.startActivity(intent)
                mode.finish()
            }
            R.id.mute_user -> {
                val resolver = activity.contentResolver
                val valuesList = ArrayList<ContentValues>()
                val userKeys = HashSet<UserKey>()
                for (selectedItem in selectedItems) {
                    when (selectedItem) {
                        is ParcelableStatus -> {
                            userKeys.add(selectedItem.user_key)
                            valuesList.add(ContentValuesCreator.createFilteredUser(selectedItem))
                        }
                        is ParcelableUser -> {
                            userKeys.add(selectedItem.key)
                            valuesList.add(ContentValuesCreator.createFilteredUser(selectedItem))
                        }
                    }
                }
                ContentResolverUtils.bulkDelete(resolver, Filters.Users.CONTENT_URI,
                        Filters.Users.USER_KEY, false, userKeys, null, null)
                ContentResolverUtils.bulkInsert(resolver, Filters.Users.CONTENT_URI, valuesList)
                Toast.makeText(activity, R.string.message_toast_users_filters_added, Toast.LENGTH_SHORT).show()
                mode.finish()
            }
            R.id.block -> {
                val accountKey = multiSelectManager.accountKey
                val userIds = UserKey.getIds(MultiSelectManager.getSelectedUserKeys(selectedItems))
                if (accountKey != null && userIds != null) {
                    twitterWrapper.createMultiBlockAsync(accountKey, userIds)
                }
                mode.finish()
            }
            R.id.report_spam -> {
                val accountKey = multiSelectManager.accountKey
                val userIds = UserKey.getIds(MultiSelectManager.getSelectedUserKeys(selectedItems))
                if (accountKey != null && userIds != null) {
                    twitterWrapper.reportMultiSpam(accountKey, userIds)
                }
                mode.finish()
            }
        }
        if (item.groupId == AccountActionProvider.MENU_GROUP) {
            val intent = item.intent
            if (intent == null || !intent.hasExtra(EXTRA_ACCOUNT)) return false
            val account: AccountDetails = intent.getParcelableExtra(EXTRA_ACCOUNT) ?: return false
            multiSelectManager.accountKey = account.key
            accountActionProvider?.selectedAccountKeys = arrayOf(account.key)
            mode.invalidate()
        }
        return true
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.action_multi_select_contents, menu)
        accountActionProvider = menu.findItem(R.id.select_account).actionProvider as AccountActionProvider
        accountActionProvider?.selectedAccountKeys = arrayOf(multiSelectManager.firstSelectAccountKey)
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        if (multiSelectManager.count != 0) {
            multiSelectManager.clearSelectedItems()
        }
        accountActionProvider = null
        actionMode = null
    }

    override fun onItemsCleared() {
        updateMultiSelectState()
    }

    override fun onItemSelected(item: Any) {
        updateMultiSelectState()
    }

    override fun onItemUnselected(item: Any) {
        updateMultiSelectState()
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        updateSelectedCount(mode)
        return true
    }

    private fun updateMultiSelectState() {
        if (multiSelectManager.isActive) {
            if (actionMode == null) {
                actionMode = activity.startActionMode(this)
            }
            updateSelectedCount(actionMode)
        } else {
            actionMode?.finish()
            actionMode = null
        }
    }

    private fun updateSelectedCount(mode: ActionMode?) {
        if (mode == null) return
        val count = multiSelectManager.count
        mode.title = activity.resources.getQuantityString(R.plurals.Nitems_selected, count, count)
    }

    companion object {

        const val MENU_GROUP = 201
    }

}
