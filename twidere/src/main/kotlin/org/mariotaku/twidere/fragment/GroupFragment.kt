package org.mariotaku.twidere.fragment

import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.FixedAsyncTaskLoader
import androidx.loader.content.Loader
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.statusnet.model.Group
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.SupportTabsAdapter
import org.mariotaku.twidere.fragment.statuses.GroupTimelineFragment
import org.mariotaku.twidere.fragment.users.GroupMembersFragment
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
        adapter.add(cls = GroupTimelineFragment::class.java, args = args, name = getString(R.string.title_statuses), tag = "statuses")
        adapter.add(cls = GroupMembersFragment::class.java, args = args, name = getString(R.string.members), tag = "members")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            Utils.setNdefPushMessageCallback(it, NfcAdapter.CreateNdefMessageCallback {
            val url = group?.url ?: return@CreateNdefMessageCallback null
            NdefMessage(arrayOf(NdefRecord.createUri(url)))
        })
        }

        getGroupInfo(false)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<SingleResponse<ParcelableGroup>> {
        val accountKey = args!!.getParcelable<UserKey?>(EXTRA_ACCOUNT_KEY)
        val groupId = args.getString(EXTRA_GROUP_ID)
        val groupName = args.getString(EXTRA_GROUP_NAME)
        val omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true)
        return ParcelableGroupLoader(requireContext(), omitIntentExtra, arguments, accountKey, groupId,
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
        LoaderManager.getInstance(this).destroyLoader(0)
        this.group = group

        if (group != null) {
            activity.title = group.fullname
        } else {
            activity.setTitle(R.string.title_user_list)
        }
        activity.invalidateOptionsMenu()
    }


    fun getGroupInfo(omitIntentExtra: Boolean) {
        val lm = LoaderManager.getInstance(this)
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
                val cache = extras.getParcelable<ParcelableGroup?>(EXTRA_GROUP)
                if (cache != null) return SingleResponse(cache)
            }
            try {
                if (accountKey == null) throw MicroBlogException("No account")
                val twitter = MicroBlogAPIFactory.getInstance(context, accountKey) ?:
                        throw MicroBlogException("No account")
                val group: Group
                group = when {
                    groupId != null -> {
                        twitter.showGroup(groupId)
                    }
                    groupName != null -> {
                        twitter.showGroupByName(groupName)
                    }
                    else -> {
                        return SingleResponse()
                    }
                }
                return SingleResponse.getInstance(ParcelableGroupUtils.from(group, accountKey, 0,
                        group.isMember))
            } catch (e: MicroBlogException) {
                return SingleResponse(e)
            }

        }

        override fun onStartLoading() {
            forceLoad()
        }

    }
}
