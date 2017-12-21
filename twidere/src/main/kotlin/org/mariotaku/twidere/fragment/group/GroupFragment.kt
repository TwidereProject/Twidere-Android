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

package org.mariotaku.twidere.fragment.group

import android.accounts.AccountManager
import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.FixedAsyncTaskLoader
import android.support.v4.content.Loader
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.model.statusnet.Group
import org.mariotaku.twidere.Constants.EXTRA_OMIT_INTENT_EXTRA
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.SupportTabsAdapter
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.extension.model.api.gnusocial.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.fragment.AbsToolbarTabPagesFragment
import org.mariotaku.twidere.fragment.timeline.GroupTimelineFragment
import org.mariotaku.twidere.fragment.users.GroupMembersFragment
import org.mariotaku.twidere.model.ParcelableGroup
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 16/3/23.
 */
class GroupFragment : AbsToolbarTabPagesFragment(), LoaderCallbacks<SingleResponse<ParcelableGroup>> {
    var group: ParcelableGroup? = null
        private set
    private var groupLoaderInitialized: Boolean = false

    override fun addTabs(adapter: SupportTabsAdapter) {
        val args = arguments
        adapter.add(cls = GroupTimelineFragment::class.java, args = args, name = getString(R.string.title_statuses), tag = "statuses")
        adapter.add(cls = GroupMembersFragment::class.java, args = args, name = getString(R.string.members), tag = "members")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Utils.setNdefPushMessageCallback(activity!!, NfcAdapter.CreateNdefMessageCallback {
            val url = group?.url ?: return@CreateNdefMessageCallback null
            NdefMessage(arrayOf(NdefRecord.createUri(url)))
        })

        getGroupInfo(false)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<SingleResponse<ParcelableGroup>> {
        val accountKey = args.accountKey
        val groupId = args.groupId
        val groupName = args.groupName
        val omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true)
        return ParcelableGroupLoader(context!!, omitIntentExtra, arguments, accountKey, groupId,
                groupName)
    }

    override fun onLoadFinished(loader: Loader<SingleResponse<ParcelableGroup>>, data: SingleResponse<ParcelableGroup>) {
        if (data.hasData()) {
            displayGroup(data.data)
        }
    }

    override fun onLoaderReset(loader: Loader<SingleResponse<ParcelableGroup>>) {

    }

    fun displayGroup(group: ParcelableGroup?) {
        val activity = activity ?: return
        loaderManager.destroyLoader(0)
        this.group = group

        if (group != null) {
            activity.title = group.fullname
        } else {
            activity.setTitle(R.string.title_user_list)
        }
        activity.invalidateOptionsMenu()
    }


    fun getGroupInfo(omitIntentExtra: Boolean) {
        val lm = loaderManager
        lm.destroyLoader(0)
        val args = Bundle(arguments)
        args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omitIntentExtra)
        if (!groupLoaderInitialized) {
            lm.initLoader(0, args, this)
            groupLoaderInitialized = true
        } else {
            lm.restartLoader(0, args, this)
        }
    }

    internal class ParcelableGroupLoader(
            context: Context,
            private val omitIntentExtra: Boolean,
            private val extras: Bundle?,
            private val accountKey: UserKey?,
            private val groupId: String?,
            private val groupName: String?
    ) : FixedAsyncTaskLoader<SingleResponse<ParcelableGroup>>(context) {

        override fun loadInBackground(): SingleResponse<ParcelableGroup> {
            if (!omitIntentExtra && extras != null) {
                val cache = extras.group
                if (cache != null) return SingleResponse(cache)
            }
            try {
                if (accountKey == null) throw AccountNotFoundException()
                val twitter = AccountManager.get(context).getDetailsOrThrow(accountKey, true)
                        .newMicroBlogInstance(context, MicroBlog::class.java)
                val group: Group
                if (groupId != null) {
                    group = twitter.showGroup(groupId)
                } else if (groupName != null) {
                    group = twitter.showGroupByName(groupName)
                } else {
                    return SingleResponse()
                }
                return SingleResponse.getInstance(group.toParcelable(accountKey, member = group.isMember))
            } catch (e: MicroBlogException) {
                return SingleResponse(e)
            }

        }

        override fun onStartLoading() {
            forceLoad()
        }

    }
}
