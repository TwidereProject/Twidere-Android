package org.mariotaku.twidere.activity

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_premium_dashboard.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class PremiumDashboardActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium_dashboard)
        if (extraFeaturesService.isSupported()) {
            extraFeaturesService.getDashboardLayouts().forEach { layout ->
                View.inflate(this, layout, cardsContainer)
            }
        }
    }

    override fun onDestroy() {
        extraFeaturesService.release()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_premium_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.consume_purchase -> {
                if (BuildConfig.DEBUG) {
                    return true
                }
                val dfRef = WeakReference(ProgressDialogFragment.show(supportFragmentManager, "consume_purchase_progress"))
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
                        val df = dfRef.get() ?: (fm?.findFragmentByTag("consume_purchase_progress") as? DialogFragment)
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
}
