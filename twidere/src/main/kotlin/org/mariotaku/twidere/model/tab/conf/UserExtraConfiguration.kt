package org.mariotaku.twidere.model.tab.conf

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.layout_extra_config_user.view.*
import kotlinx.android.synthetic.main.list_item_simple_user.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.UserListSelectorActivity
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.fragment.CustomTabsFragment.TabEditorDialogFragment
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.tab.TabConfiguration
import org.mariotaku.twidere.util.dagger.DependencyHolder
import org.mariotaku.twidere.util.display
import org.mariotaku.twidere.view.holder.TwoLineWithIconViewHolder

/**
 * Created by mariotaku on 2016/11/28.
 */

class UserExtraConfiguration(key: String) : TabConfiguration.ExtraConfiguration(key) {
    var value: ParcelableUser? = null
        private set

    private lateinit var viewHolder: TwoLineWithIconViewHolder
    private lateinit var dependencyHolder: DependencyHolder
    private lateinit var hintView: View

    override fun onCreate(context: Context) {
        super.onCreate(context)
        this.dependencyHolder = DependencyHolder.get(context)
    }

    override fun onCreateView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_extra_config_user, parent, false)
    }

    override fun onViewCreated(context: Context, view: View, fragment: TabEditorDialogFragment) {
        view.setOnClickListener {
            val intent = Intent(INTENT_ACTION_SELECT_USER)
            intent.putExtra(EXTRA_ACCOUNT_KEY, fragment.account.key)
            intent.setClass(context, UserListSelectorActivity::class.java)
            fragment.startExtraConfigurationActivityForResult(this@UserExtraConfiguration, intent, 1)
        }
        hintView = view.selectUserHint
        viewHolder = TwoLineWithIconViewHolder(view.listItem)

        viewHolder.view.visibility = View.GONE
        hintView.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK) {
                    val user: ParcelableUser = data.getParcelableExtra(EXTRA_USER)
                    viewHolder.display(user, dependencyHolder.mediaLoader, dependencyHolder.userColorNameManager, true)
                    viewHolder.view.visibility = View.VISIBLE
                    hintView.visibility = View.GONE

                    this.value = user
                }
            }
        }
    }
}
