package org.mariotaku.twidere.model.tab.conf

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.layout_extra_config_user_list.view.*
import kotlinx.android.synthetic.main.list_item_simple_user_list.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.UserListSelectorActivity
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.fragment.CustomTabsFragment.TabEditorDialogFragment
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.tab.TabConfiguration
import org.mariotaku.twidere.util.dagger.DependencyHolder
import org.mariotaku.twidere.util.view.display
import org.mariotaku.twidere.view.holder.SimpleUserListViewHolder

/**
 * Created by mariotaku on 2016/11/28.
 */

class UserListExtraConfiguration(key: String) : TabConfiguration.ExtraConfiguration(key) {
    var value: ParcelableUserList? = null
        private set

    private lateinit var viewHolder: SimpleUserListViewHolder
    private lateinit var dependencyHolder: DependencyHolder
    private lateinit var hintView: View

    override fun onCreate(context: Context) {
        super.onCreate(context)
        this.dependencyHolder = DependencyHolder.get(context)
    }

    override fun onCreateView(context: Context, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_extra_config_user_list, parent, false)
    }

    override fun onViewCreated(context: Context, view: View, fragment: TabEditorDialogFragment) {
        view.setOnClickListener {
            val intent = Intent(INTENT_ACTION_SELECT_USER_LIST)
            intent.putExtra(EXTRA_ACCOUNT_KEY, fragment.account.account_key)
            intent.setClass(context, UserListSelectorActivity::class.java)
            fragment.startExtraConfigurationActivityForResult(this@UserListExtraConfiguration, intent, 1)
        }
        hintView = view.selectUserListHint
        viewHolder = SimpleUserListViewHolder(view.listItem)

        viewHolder.itemView.visibility = View.GONE
        hintView.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK) {
                    val userList: ParcelableUserList = data.getParcelableExtra(EXTRA_USER_LIST)
                    viewHolder.display(userList, dependencyHolder.mediaLoader,
                            dependencyHolder.userColorNameManager, true)
                    viewHolder.itemView.visibility = View.VISIBLE
                    hintView.visibility = View.GONE

                    this.value = userList
                }
            }
        }
    }
}

