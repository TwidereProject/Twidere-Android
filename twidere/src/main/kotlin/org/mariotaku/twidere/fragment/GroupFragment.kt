package org.mariotaku.twidere.fragment

import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.statusnet.model.Group
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.SupportTabsAdapter
import org.mariotaku.twidere.model.ParcelableGroup
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableGroupUtils
import org.mariotaku.twidere.util.MicroBlogAPIFactory
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
        adapter.addTab(GroupTimelineFragment::class.java, args, getString(R.string.statuses), 0, 0, "statuses")
        adapter.addTab(GroupMembersFragment::class.java, args, getString(R.string.members), 0, 1, "members")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Utils.setNdefPushMessageCallback(activity, NfcAdapter.CreateNdefMessageCallback {
            val url = group?.url ?: return@CreateNdefMessageCallback null
            NdefMessage(arrayOf(NdefRecord.createUri(url)))
        })

        getGroupInfo(false)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<SingleResponse<ParcelableGroup>> {
        val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val groupId = args.getString(EXTRA_GROUP_ID)
        val groupName = args.getString(EXTRA_GROUP_NAME)
        val omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true)
        return ParcelableGroupLoader(context, omitIntentExtra, arguments, accountKey,
                groupId, groupName)
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
            activity.setTitle(R.string.user_list)
        }
        invalidateOptionsMenu()
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
            private val accountKey: UserKey,
            private val groupId: String?,
            private val groupName: String?
    ) : AsyncTaskLoader<SingleResponse<ParcelableGroup>>(context) {

        override fun loadInBackground(): SingleResponse<ParcelableGroup> {
            if (!omitIntentExtra && extras != null) {
                val cache = extras.getParcelable<ParcelableGroup>(EXTRA_GROUP)
                if (cache != null) return SingleResponse.getInstance(cache)
            }
            val twitter = MicroBlogAPIFactory.getInstance(context, accountKey,
                    true) ?: return SingleResponse.getInstance<ParcelableGroup>()
            try {
                val group: Group
                if (groupId != null) {
                    group = twitter.showGroup(groupId)
                } else if (groupName != null) {
                    group = twitter.showGroupByName(groupName)
                } else {
                    return SingleResponse.getInstance<ParcelableGroup>()
                }
                return SingleResponse.getInstance(ParcelableGroupUtils.from(group, accountKey, 0,
                        group.isMember))
            } catch (e: MicroBlogException) {
                return SingleResponse.getInstance<ParcelableGroup>(e)
            }

        }

        public override fun onStartLoading() {
            forceLoad()
        }

    }
}
