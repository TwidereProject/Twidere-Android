package org.mariotaku.twidere.model.tab.conf

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.mariotaku.microblog.library.twitter.model.Location
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.TrendsLocationSelectorActivity
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_LOCATION
import org.mariotaku.twidere.fragment.CustomTabsFragment.TabEditorDialogFragment
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.tab.StringHolder
import org.mariotaku.twidere.model.tab.TabConfiguration

/**
 * Created by mariotaku on 2016/12/5.
 */
open class TrendsLocationExtraConfiguration(
        key: String,
        title: StringHolder
) : TabConfiguration.ExtraConfiguration(key, title) {

    open var value: Place? = null
        set(value) {
            field = value
            if (value != null) {
                summaryView.visibility = View.VISIBLE
                summaryView.text = value.name
            } else {
                summaryView.visibility = View.GONE
            }
        }

    private lateinit var summaryView: TextView

    constructor(key: String, titleRes: Int) : this(key, StringHolder.resource(titleRes))

    override fun onCreateView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_extra_config_checkbox, parent, false)
    }

    override fun onViewCreated(context: Context, view: View, fragment: TabEditorDialogFragment) {
        super.onViewCreated(context, view, fragment)
        val titleView = view.findViewById<TextView>(android.R.id.title)
        titleView.text = title.createString(context)

        summaryView = view.findViewById<TextView>(android.R.id.summary)
        summaryView.visibility = View.GONE
        view.setOnClickListener {
            val account = fragment.account ?: return@setOnClickListener
            val intent = Intent(context, TrendsLocationSelectorActivity::class.java)
            intent.putExtra(EXTRA_ACCOUNT_KEY, account.key)
            fragment.startExtraConfigurationActivityForResult(this@TrendsLocationExtraConfiguration, intent, 1)
        }
    }


    override fun onActivityResult(fragment: TabEditorDialogFragment, requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.getParcelableExtra<Location>(EXTRA_LOCATION)?.let { location ->
                        value = Place(location.woeid, location.name)
                    }
                }
            }
        }
    }

    override fun onAccountSelectionChanged(account: AccountDetails?) {
        super.onAccountSelectionChanged(account)
        val titleView: TextView = view.findViewById(android.R.id.title)
        val summaryView: TextView = view.findViewById(android.R.id.summary)
        val canSelectLocation = account?.type == AccountType.TWITTER
        view.isEnabled = canSelectLocation
        titleView.isEnabled = canSelectLocation
        summaryView.isEnabled = canSelectLocation
        if (!canSelectLocation) {
            value = null
        }
    }

    data class Place(var woeId: Int, var name: String)
}
