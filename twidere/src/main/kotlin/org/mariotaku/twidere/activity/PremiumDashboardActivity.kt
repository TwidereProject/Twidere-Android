package org.mariotaku.twidere.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.activity_premium_dashboard.*
import kotlinx.android.synthetic.main.adapter_item_extra_feature_normal.view.*
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.get
import org.mariotaku.kpreferences.set
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.REQUEST_PURCHASE_EXTRA_FEATURES
import org.mariotaku.twidere.adapter.BaseRecyclerViewAdapter
import org.mariotaku.twidere.constant.promotionsEnabledKey
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.model.analyzer.PurchaseFinished
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.promotion.PromotionService
import org.mariotaku.twidere.util.schedule.StatusScheduleProvider
import org.mariotaku.twidere.view.ContainerView
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class PremiumDashboardActivity : BaseActivity() {

    private lateinit var adapter: ControllersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium_dashboard)

        adapter = ControllersAdapter(this, requestManager)
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
            val controller = holder.controller as? BaseItemViewController ?: return
            controller.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        val lm = recyclerView.layoutManager as LinearLayoutManager
        for (pos in lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition()) {
            val holder = recyclerView.findViewHolderForLayoutPosition(pos) as? ControllerViewHolder ?: return
            val controller = holder.controller as? BaseItemViewController ?: return
            controller.onResume()
        }
    }

    override fun onDestroy() {
        extraFeaturesService.release()
        if (isFinishing) {
            // Make sure promotionsEnabled set
            preferences[promotionsEnabledKey] = preferences[promotionsEnabledKey]
        }
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
                    val controller = holder.controller as? BaseItemViewController ?: return
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

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        val promotionsEnabled = preferences[promotionsEnabledKey]
        menu.setItemAvailability(R.id.enable_promotions, BuildConfig.DEBUG && !promotionsEnabled)
        menu.setItemAvailability(R.id.disable_promotions, promotionsEnabled)
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
                val weakThis = WeakReference(this)
                val recreate = AtomicBoolean()
                executeAfterFragmentResumed {
                    ProgressDialogFragment.show(it.supportFragmentManager, "consume_purchase_progress")
                } and task {
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
            R.id.enable_promotions -> {
                preferences[promotionsEnabledKey] = true
                recreate()
            }
            R.id.disable_promotions -> {
                preferences[promotionsEnabledKey] = false
                recreate()
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

    abstract class BaseItemViewController : ContainerView.ViewController() {

        @Inject
        protected lateinit var extraFeaturesService: ExtraFeaturesService

        @Inject
        protected lateinit var preferences: SharedPreferences

        @Inject
        protected lateinit var scheduleProviderFactory: StatusScheduleProvider.Factory

        @Inject
        protected lateinit var promotionService: PromotionService

        var position: Int = RecyclerView.NO_POSITION
            internal set

        open val useCardBackground: Boolean = true

        protected val activity: PremiumDashboardActivity get() = context as PremiumDashboardActivity

        override fun onCreate() {
            super.onCreate()
            GeneralComponent.get(context).inject(this)
        }

        open fun onPause() {}

        open fun onResume() {}

        open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}

    }

    open class ExtraFeatureViewController : BaseItemViewController() {
        protected val titleView: TextView by lazy { view.findViewById<TextView>(R.id.title) }
        protected val messageView: TextView by lazy { view.findViewById<TextView>(R.id.message) }
        protected val button1: Button by lazy { view.findViewById<Button>(R.id.button1) }
        protected val button2: Button  by lazy { view.findViewById<Button>(R.id.button2) }


        override fun onCreateView(parent: ContainerView): View {
            return LayoutInflater.from(parent.context).inflate(R.layout.layout_controller_extra_feature,
                    parent, false)
        }

    }

    class ControllersAdapter(
            context: Context,
            requestManager: RequestManager
    ) : BaseRecyclerViewAdapter<ControllerViewHolder>(context, requestManager) {

        init {
            setHasStableIds(true)
        }

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
            if (controller is BaseItemViewController) {
                controller.position = holder.layoutPosition
            }
            holder.controller = controller
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControllerViewHolder {
            return ControllerViewHolder(LayoutInflater.from(parent.context).inflate(when (viewType) {
                VIEW_TYPE_NO_CARD -> R.layout.adapter_item_extra_feature_nocard
                else -> R.layout.adapter_item_extra_feature_normal
            }, parent, false))
        }

        override fun getItemViewType(position: Int): Int {
            val controller = controllers!![position].newInstance()
            if (controller is BaseItemViewController) {
                if (!controller.useCardBackground) {
                    return VIEW_TYPE_NO_CARD
                }
            }
            return VIEW_TYPE_NORMAL
        }

        override fun getItemId(position: Int): Long {
            return System.identityHashCode(controllers!![position]).toLong()
        }

        companion object {
            private const val VIEW_TYPE_NORMAL = 1
            private const val VIEW_TYPE_NO_CARD = 2
        }
    }
}
