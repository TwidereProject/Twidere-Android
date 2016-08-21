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
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import com.twitter.Extractor
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_STATUS
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NAME_FIRST
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
        val builder = AlertDialog.Builder(context)
        filterItems = filterItemsInfo
        val entries = arrayOfNulls<String>(filterItems!!.size)
        val nameFirst = preferences.getBoolean(KEY_NAME_FIRST)
        for (i in 0 until entries.size) {
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
        builder.setTitle(R.string.add_to_filter)
        builder.setMultiChoiceItems(entries, null, null)
        builder.setPositiveButton(android.R.string.ok) { dialog, which ->
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
                if (value is ParcelableUserMention) {
                    userKeys.add(value.key)
                    userValues.add(ContentValuesCreator.createFilteredUser(value))
                } else if (value is UserItem) {
                    userKeys.add(value.key)
                    userValues.add(createFilteredUser(value))
                } else if (info.type == FilterItemInfo.FILTER_TYPE_KEYWORD) {
                    val keyword = ParseUtils.parseString(value)
                    keywords.add(keyword)
                    val values = ContentValues()
                    values.put(Filters.Keywords.VALUE, "#" + keyword)
                    keywordValues.add(values)
                } else if (info.type == FilterItemInfo.FILTER_TYPE_SOURCE) {
                    val source = ParseUtils.parseString(value)
                    sources.add(source)
                    val values = ContentValues()
                    values.put(Filters.Sources.VALUE, source)
                    sourceValues.add(values)
                }
            }
            val resolver = contentResolver
            ContentResolverUtils.bulkDelete(resolver, Filters.Users.CONTENT_URI, Filters.Users.USER_KEY, userKeys, null)
            ContentResolverUtils.bulkDelete(resolver, Filters.Keywords.CONTENT_URI, Filters.Keywords.VALUE, keywords, null)
            ContentResolverUtils.bulkDelete(resolver, Filters.Sources.CONTENT_URI, Filters.Sources.VALUE, sources, null)
            ContentResolverUtils.bulkInsert(resolver, Filters.Users.CONTENT_URI, userValues)
            ContentResolverUtils.bulkInsert(resolver, Filters.Keywords.CONTENT_URI, keywordValues)
            ContentResolverUtils.bulkInsert(resolver, Filters.Sources.CONTENT_URI, sourceValues)
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        return builder.create()
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
            val source = HtmlEscapeHelper.toPlainText(status.source)
            list.add(FilterItemInfo(FilterItemInfo.FILTER_TYPE_SOURCE, source))
            return list.toTypedArray()
        }

    private fun getName(manager: UserColorNameManager, value: Any, nameFirst: Boolean): String {
        if (value is ParcelableUserMention) {
            return manager.getDisplayName(value.key, value.name, value.screen_name, nameFirst)
        } else if (value is UserItem) {
            return manager.getDisplayName(value.key, value.name, value.screen_name, nameFirst)
        } else
            return ParseUtils.parseString(value)
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

        val FRAGMENT_TAG = "add_status_filter"

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
