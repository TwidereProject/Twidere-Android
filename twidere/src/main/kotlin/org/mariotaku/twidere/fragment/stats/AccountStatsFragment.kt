package org.mariotaku.twidere.fragment.stats

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_account_stats.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.content.database.TwidereDatabase
import org.mariotaku.twidere.content.model.AccountStats
import org.mariotaku.twidere.data.ComputableLiveData
import org.mariotaku.twidere.databinding.AdapterItemAccountStatCardBinding
import org.mariotaku.twidere.fragment.BaseFragment
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.Utils
import java.util.*

class AccountStatsFragment : BaseFragment() {

    private lateinit var liveStats: AccountStatsLiveData

    private lateinit var statusesBinding: AdapterItemAccountStatCardBinding
    private lateinit var followersBinding: AdapterItemAccountStatCardBinding

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        liveStats = AccountStatsLiveData(context!!, Utils.getAccountKeys(context!!,
                arguments)!!.first())


        statusesBinding = AdapterItemAccountStatCardBinding.inflate(layoutInflater,
                statsScrollContent, true)
        followersBinding = AdapterItemAccountStatCardBinding.inflate(layoutInflater,
                statsScrollContent, true)

        liveStats.observe(this, Observer {
            displaySummaries(it)
        })
        liveStats.load()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_stats, container, false)
    }

    private fun displaySummaries(summaries: AccountStats.Summaries?) {
        if (summaries == null) return
        statusesBinding.statTitle.text = getString(R.string.title_statuses)
        followersBinding.statTitle.text = getString(R.string.title_followers)

        statusesBinding.summary = summaries.statuses
        followersBinding.summary = summaries.followers
    }

    class AccountStatsLiveData(
            val context: Context,
            val accountKey: UserKey
    ) : ComputableLiveData<AccountStats.Summaries>(false) {
        override fun compute(): AccountStats.Summaries {
            return TwidereDatabase.get(context).accountDailyStats().monthlySummary(accountKey, Date())
        }

    }
}