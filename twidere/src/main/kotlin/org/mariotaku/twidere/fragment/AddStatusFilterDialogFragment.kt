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
import android.content.ContentValues
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import com.twitter.twittertext.Extractor
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_STATUS
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.onShow
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUserMention
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Filters
import org.mariotaku.twidere.util.ContentValuesCreator
import org.mariotaku.twidere.util.HtmlEscapeHelper
import org.mariotaku.twidere.util.ParseUtils
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.content.ContentResolverUtils
import java.util.*

class AddStatusFilterDialogFragment : BaseDialogFragment() {

    private val extractor = Extractor()
    private var filterItems: Array<FilterItemInfo>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        filterItems = filterItemsInfo
        val entries = arrayOfNulls<String>(filterItems!!.size)
        val nameFirst = preferences[nameFirstKey]
        for (i in entries.indices) {
            val info = filterItems!![i]
            when (info.type) {
                FilterItemInfo.FILTER_TYPE_USER -> {
                    entries[i] = getString(R.string.user_filter_name, getName(userColorNameManager,
                            info.value, nameFirst))
                }
                FilterItemInfo.FILTER_TYPE_KEYWORD -> {
                    entries[i] = getString(R.string.keyword_filter_name, getName(userColorNameManager,
                            info.value, nameFirst))
                }
                FilterItemInfo.FILTER_TYPE_SOURCE -> {
                    entries[i] = getString(R.string.source_filter_name, getName(userColorNameManager,
                            info.value, nameFirst))
                }
            }
        }
        builder.setTitle(R.string.action_add_to_filter)
        builder.setMultiChoiceItems(entries, null, null)
        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            val alertDialog = dialog as AlertDialog
            val checkPositions = alertDialog.listView.checkedItemPositions

            val userKeys = HashSet<UserKey>()
            val keywords = HashSet<String>()
            val sources = HashSet<String>()
            val userValues = ArrayList<ContentValues>()
            val keywordValues = ArrayList<ContentValues>()
            val sourceValues = ArrayList<ContentValues>()
            loop@ for (i in 0 until checkPositions.size()) {
                if (!checkPositions.valueAt(i)) {
                    continue@loop
                }
                val info = filterItems!![checkPositions.keyAt(i)]
                val value = info.value
                when {
                    value is ParcelableUserMention -> {
                        userKeys.add(value.key)
                        userValues.add(ContentValuesCreator.createFilteredUser(value))
                    }
                    value is UserItem -> {
                        userKeys.add(value.key)
                        userValues.add(createFilteredUser(value))
                    }
                    info.type == FilterItemInfo.FILTER_TYPE_KEYWORD -> {
                        val keyword = ParseUtils.parseString(value)
                        keywords.add(keyword)
                        val values = ContentValues()
                        values.put(Filters.Keywords.VALUE, "#$keyword")
                        keywordValues.add(values)
                    }
                    info.type == FilterItemInfo.FILTER_TYPE_SOURCE -> {
                        val source = ParseUtils.parseString(value)
                        sources.add(source)
                        val values = ContentValues()
                        values.put(Filters.Sources.VALUE, source)
                        sourceValues.add(values)
                    }
                }
            }
            context?.contentResolver?.let { resolver ->
                ContentResolverUtils.bulkDelete(resolver, Filters.Users.CONTENT_URI,
                        Filters.Users.USER_KEY, false, userKeys, null, null)
                ContentResolverUtils.bulkDelete(resolver, Filters.Keywords.CONTENT_URI,
                        Filters.Keywords.VALUE, false, keywords, null, null)
                ContentResolverUtils.bulkDelete(resolver, Filters.Sources.CONTENT_URI,
                        Filters.Sources.VALUE, false, sources, null, null)
                ContentResolverUtils.bulkInsert(resolver, Filters.Users.CONTENT_URI, userValues)
                ContentResolverUtils.bulkInsert(resolver, Filters.Keywords.CONTENT_URI, keywordValues)
                ContentResolverUtils.bulkInsert(resolver, Filters.Sources.CONTENT_URI, sourceValues)
            }
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.onShow { it.applyTheme() }
        return dialog
    }

    private val filterItemsInfo: Array<FilterItemInfo>
        get() {
            val args = arguments
            if (args == null || !args.containsKey(EXTRA_STATUS)) return emptyArray()
            val status = args.getParcelable<ParcelableStatus>(EXTRA_STATUS) ?: return emptyArray()
            val list = ArrayList<FilterItemInfo>()
            if (status.is_retweet && status.retweeted_by_user_key != null) {
                list.add(FilterItemInfo(FilterItemInfo.FILTER_TYPE_USER,
                        UserItem(status.retweeted_by_user_key!!, status.retweeted_by_user_name,
                                status.retweeted_by_user_screen_name)))
            }
            if (status.is_quote && status.quoted_user_key != null) {
                list.add(FilterItemInfo(FilterItemInfo.FILTER_TYPE_USER,
                        UserItem(status.quoted_user_key!!, status.quoted_user_name,
                                status.quoted_user_screen_name)))
            }
            list.add(FilterItemInfo(FilterItemInfo.FILTER_TYPE_USER, UserItem(status.user_key,
                    status.user_name, status.user_screen_name)))
            val mentions = status.mentions
            if (mentions != null) {
                for (mention in mentions) {
                    if (mention.key != status.user_key) {
                        list.add(FilterItemInfo(FilterItemInfo.FILTER_TYPE_USER, mention))
                    }
                }
            }
            val hashtags = HashSet<String>()
            hashtags.addAll(extractor.extractHashtags(status.text_plain))
            for (hashtag in hashtags) {
                list.add(FilterItemInfo(FilterItemInfo.FILTER_TYPE_KEYWORD, hashtag))
            }
            val source = status.source?.let(HtmlEscapeHelper::toPlainText)
            if (source != null) {
                list.add(FilterItemInfo(FilterItemInfo.FILTER_TYPE_SOURCE, source))
            }
            return list.toTypedArray()
        }

    private fun getName(manager: UserColorNameManager, value: Any, nameFirst: Boolean): String {
        return when (value) {
            is ParcelableUserMention -> {
                manager.getDisplayName(value.key, value.name, value.screen_name, nameFirst)
            }
            is UserItem -> {
                manager.getDisplayName(value.key, value.name, value.screen_name, nameFirst)
            }
            else -> ParseUtils.parseString(value)
        }
    }

    internal data class FilterItemInfo(
            val type: Int,
            val value: Any
    ) {

        companion object {

            internal const val FILTER_TYPE_USER = 1
            internal const val FILTER_TYPE_KEYWORD = 2
            internal const val FILTER_TYPE_SOURCE = 3
        }

    }

    internal data class UserItem(
            val key: UserKey,
            val name: String,
            val screen_name: String
    )

    companion object {

        const val FRAGMENT_TAG = "add_status_filter"

        private fun createFilteredUser(item: UserItem): ContentValues {
            val values = ContentValues()
            values.put(Filters.Users.USER_KEY, item.key.toString())
            values.put(Filters.Users.NAME, item.name)
            values.put(Filters.Users.SCREEN_NAME, item.screen_name)
            return values
        }

        fun show(fm: FragmentManager, status: ParcelableStatus): AddStatusFilterDialogFragment {
            val args = Bundle()
            args.putParcelable(EXTRA_STATUS, status)
            val f = AddStatusFilterDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }

}
