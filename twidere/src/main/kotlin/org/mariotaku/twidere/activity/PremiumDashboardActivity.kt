package org.mariotaku.twidere.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_premium_dashboard.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.METADATA_KEY_PLUS_SERVICE_SIGN_IN_LABEL
import org.mariotaku.twidere.adapter.ArrayAdapter
import org.mariotaku.twidere.constant.IntentConstants.INTENT_ACTION_PLUS_SERVICE_SIGN_IN
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.util.premium.ExtraFeaturesChecker

class PremiumDashboardActivity : BaseActivity() {

    private lateinit var extraFeaturesChecker: ExtraFeaturesChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        extraFeaturesChecker = ExtraFeaturesChecker.newInstance(this)
        setContentView(R.layout.activity_premium_dashboard)
        if (extraFeaturesChecker.isSupported()) {
            if (extraFeaturesChecker.isEnabled()) {
                View.inflate(this, extraFeaturesChecker.statusLayout, cardsContainer)
            } else {
                View.inflate(this, extraFeaturesChecker.introductionLayout, cardsContainer)
            }
        }
    }

    override fun onDestroy() {
        extraFeaturesChecker.release()
        super.onDestroy()
    }

    class SignInChooserDialogFragment : BaseDialogFragment(), LoaderManager.LoaderCallbacks<List<ResolveInfo>> {
        private var adapter: ProviderAdapter? = null
        private var loaderInitialized: Boolean = false

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context = context
            adapter = ProviderAdapter(context)
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.sign_in_with_ellip)
            builder.setAdapter(adapter) { dialog, which -> startLogin(adapter!!.getItem(which)) }
            val dialog = builder.create()
            dialog.setOnShowListener { loadInfo() }
            return dialog
        }

        private fun startLogin(info: ResolveInfo) {
            val activity = activity ?: return
            val intent = Intent(INTENT_ACTION_PLUS_SERVICE_SIGN_IN)
            intent.setClassName(info.activityInfo.packageName, info.activityInfo.name)
            activity.startActivityForResult(intent, REQUEST_PLUS_SERVICE_SIGN_IN)
            dismiss()
        }

        private fun loadInfo() {
            val lm = loaderManager
            if (loaderInitialized) {
                lm.restartLoader(0, null, this)
            } else {
                lm.initLoader(0, null, this)
                loaderInitialized = true
            }
        }

        override fun onCreateLoader(id: Int, args: Bundle): Loader<List<ResolveInfo>> {
            return SignInActivitiesLoader(context)
        }

        override fun onLoadFinished(loader: Loader<List<ResolveInfo>>, data: List<ResolveInfo>?) {
            adapter!!.clear()
            if (data != null) {
                adapter!!.addAll(data)
            }
        }

        override fun onLoaderReset(loader: Loader<List<ResolveInfo>>) {
            adapter!!.clear()
        }

        class SignInActivitiesLoader(context: Context) : AsyncTaskLoader<List<ResolveInfo>>(context) {

            override fun loadInBackground(): List<ResolveInfo> {
                val context = context
                val intent = Intent(INTENT_ACTION_PLUS_SERVICE_SIGN_IN)
                intent.`package` = context.packageName
                val pm = context.packageManager
                return pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)
            }

            override fun onStartLoading() {
                forceLoad()
            }
        }

        internal class ProviderAdapter(context: Context) : ArrayAdapter<ResolveInfo>(context, android.R.layout.simple_list_item_1) {

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val metaData = getItem(position).activityInfo.metaData
                (view.findViewById(android.R.id.text1) as TextView).setText(metaData.getInt(METADATA_KEY_PLUS_SERVICE_SIGN_IN_LABEL))
                return view
            }
        }
    }

    companion object {

        private val REQUEST_PLUS_SERVICE_SIGN_IN = 101
    }
}
