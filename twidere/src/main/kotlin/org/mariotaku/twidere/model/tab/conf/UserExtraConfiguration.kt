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
import org.mariotaku.twidere.activity.UserSelectorActivity
import org.mariotaku.twidere.adapter.DummyItemAdapter
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.fragment.CustomTabsFragment.TabEditorDialogFragment
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.tab.TabConfiguration
import org.mariotaku.twidere.util.dagger.DependencyHolder
import org.mariotaku.twidere.view.holder.SimpleUserViewHolder

/**
 * Created by mariotaku on 2016/11/28.
 */

class UserExtraConfiguration(key: String) : TabConfiguration.ExtraConfiguration(key,
        R.string.title_user) {
    var value: ParcelableUser? = null
        private set

    private lateinit var viewHolder: SimpleUserViewHolder<*>
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
        super.onViewCreated(context, view, fragment)
        view.setOnClickListener {
            val account = fragment.account ?: return@setOnClickListener
            val intent = Intent(INTENT_ACTION_SELECT_USER)
            intent.putExtra(EXTRA_ACCOUNT_KEY, account.key)
            intent.setClass(context, UserSelectorActivity::class.java)
            fragment.startExtraConfigurationActivityForResult(this@UserExtraConfiguration, intent, 1)
        }
        hintView = view.selectUserHint
        val adapter = DummyItemAdapter(context, requestManager = fragment.requestManager)
        adapter.updateOptions()
        viewHolder = SimpleUserViewHolder(view.listItem, adapter)

        viewHolder.itemView.visibility = View.GONE
        hintView.visibility = View.VISIBLE
    }

    override fun onActivityResult(fragment: TabEditorDialogFragment, requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.getParcelableExtra<ParcelableUser>(EXTRA_USER)?.let { user ->
                        viewHolder.displayUser(user)
                        viewHolder.itemView.visibility = View.VISIBLE
                        hintView.visibility = View.GONE
                        this.value = user
                    }
                }
            }
        }
    }
}
