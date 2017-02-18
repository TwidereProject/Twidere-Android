package org.mariotaku.twidere.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.NavUtils
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_premium_dashboard.*
import kotlinx.android.synthetic.main.card_item_extra_feature.view.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.REQUEST_PURCHASE_EXTRA_FEATURES
import org.mariotaku.twidere.adapter.BaseRecyclerViewAdapter
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.model.analyzer.PurchaseFinished
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.SharedPreferencesWrapper
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.view.ContainerView
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class PremiumDashboardActivity : BaseActivity() {

    private lateinit var adapter: ControllersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium_dashboard)
        adapter = ControllersAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (extraFeaturesService.isSupported()) {
            adapter.controllers = extraFeaturesService.getDashboardControllers()
        } else {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        val lm = recyclerView.layoutManager as LinearLayoutManager
        for (pos in lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()) {
            val holder = recyclerView.findViewHolderForLayoutPosition(pos) as? ControllerViewHolder ?: return
            val controller = holder.controller as? ExtraFeatureViewController ?: return
            controller.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        val lm = recyclerView.layoutManager as LinearLayoutManager
        for (pos in lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()) {
            val holder = recyclerView.findViewHolderForLayoutPosition(pos) as? ControllerViewHolder ?: return
            val controller = holder.controller as? ExtraFeatureViewController ?: return
            controller.onResume()
        }
    }

    override fun onDestroy() {
        extraFeaturesService.release()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            REQUEST_PURCHASE_EXTRA_FEATURES -> {
                if (resultCode == Activity.RESULT_OK) {
                    Analyzer.log(PurchaseFinished.create(data!!))
                }
            }
            else -> {
                val position = ((requestCode and 0xFF00) shr 8) - 1
                if (position >= 0) {
                    val holder = recyclerView.findViewHolderForLayoutPosition(position) as? ControllerViewHolder ?: return
                    val controller = holder.controller as? ExtraFeatureViewController ?: return
                    controller.onActivityResult(requestCode and 0xFF, resultCode, data)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_premium_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
            }
            R.id.consume_purchase -> {
                if (!BuildConfig.DEBUG) {
                    return true
                }
                ProgressDialogFragment.show(supportFragmentManager, "consume_purchase_progress")
                val weakThis = WeakReference(this)
                val recreate = AtomicBoolean()
                task {
                    val activity = weakThis.get() ?: throw IllegalStateException()
                    if (!activity.extraFeaturesService.destroyPurchase()) {
                        throw IllegalStateException()
                    }
                }.successUi {
                    recreate.set(true)
                }.failUi {
                    val activity = weakThis.get() ?: return@failUi
                    Toast.makeText(activity, R.string.message_unable_to_consume_purchase, Toast.LENGTH_SHORT).show()
                }.alwaysUi {
                    weakThis.get()?.executeAfterFragmentResumed {
                        val fm = weakThis.get()?.supportFragmentManager
                        val df = fm?.findFragmentByTag("consume_purchase_progress") as? DialogFragment
                        df?.dismiss()
                        if (recreate.get()) {
                            weakThis.get()?.recreate()
                        }
                    }
                }
            }
        }
        return true
    }

    fun startActivityForControllerResult(intent: Intent, position: Int, requestCode: Int) {
        if (position + 1 > 0xFF || requestCode > 0xFF) throw IllegalArgumentException()
        startActivityForResult(intent, ((position + 1) shl 8) or requestCode)
    }

    class ControllerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val containerView by lazy { itemView.containerView }
        var controller: ContainerView.ViewController?
            get() = containerView.viewController
            set(value) {
                containerView.viewController = value
            }

    }

    open class ExtraFeatureViewController : ContainerView.ViewController() {
        protected val titleView by lazy { view.findViewById(R.id.title) as TextView }
        protected val messageView by lazy { view.findViewById(R.id.message) as TextView }
        protected val button1 by lazy { view.findViewById(R.id.button1) as Button }

        protected val button2 by lazy { view.findViewById(R.id.button2) as Button }
        @Inject
        protected lateinit var extraFeaturesService: ExtraFeaturesService

        @Inject
        protected lateinit var preferences: SharedPreferencesWrapper

        var position: Int = RecyclerView.NO_POSITION
            internal set

        protected val activity: PremiumDashboardActivity get() = context as PremiumDashboardActivity

        override fun onCreate() {
            super.onCreate()
            GeneralComponentHelper.build(context).inject(this)
        }

        override fun onCreateView(parent: ContainerView): View {
            return LayoutInflater.from(parent.context).inflate(R.layout.layout_controller_extra_feature,
                    parent, false)
        }

        open fun onPause() {}
        open fun onResume() {}

        open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}

    }

    class ControllersAdapter(context: Context) : BaseRecyclerViewAdapter<ControllerViewHolder>(context) {

        var controllers: List<Class<out ContainerView.ViewController>>? = null
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getItemCount(): Int {
            return controllers?.size ?: 0
        }

        override fun onBindViewHolder(holder: ControllerViewHolder, position: Int) {
            val controller = controllers!![position].newInstance()
            if (controller is ExtraFeatureViewController) {
                controller.position = holder.layoutPosition
            }
            holder.controller = controller
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControllerViewHolder {
            return ControllerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_item_extra_feature,
                    parent, false))
        }

    }
}
