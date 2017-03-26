/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment

import android.app.Activity
import android.app.Dialog
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.*
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_drafts.*
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.iface.IBaseActivity
import org.mariotaku.twidere.adapter.DraftsAdapter
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.constant.textSizeKey
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.analyzer.PurchaseFinished
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts
import org.mariotaku.twidere.service.LengthyOperationsService
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.AsyncTaskUtils
import org.mariotaku.twidere.util.deleteDrafts
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import java.lang.ref.WeakReference

class DraftsFragment : BaseFragment(), LoaderCallbacks<Cursor?>, OnItemClickListener, MultiChoiceModeListener {

    private lateinit var adapter: DraftsAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        adapter = DraftsAdapter(activity, Glide.with(this)).apply {
            textSize = preferences[textSizeKey].toFloat()
        }

        listView.adapter = adapter
        listView.emptyView = emptyView
        listView.onItemClickListener = this
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        listView.setMultiChoiceModeListener(this)
        emptyIcon.setImageResource(R.drawable.ic_info_draft)
        emptyText.setText(R.string.drafts_hint_messages)
        loaderManager.initLoader(0, null, this)
        setListShown(false)
    }

    override fun onStart() {
        twitterWrapper.clearNotificationAsync(NOTIFICATION_ID_DRAFTS)
        super.onStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PURCHASE_EXTRA_FEATURES -> {
                if (resultCode == Activity.RESULT_OK) {
                    Analyzer.log(PurchaseFinished.create(data!!))
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_drafts, container, false)
    }

    // MARK: Loader callbacks
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
        val uri = Drafts.CONTENT_URI_UNSENT
        val cols = Drafts.COLUMNS
        val orderBy = Drafts.TIMESTAMP + " DESC"
        return CursorLoader(activity, uri, cols, null, null, orderBy)
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, cursor: Cursor?) {
        adapter.swapCursor(cursor)
        setListShown(true)
    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {
        adapter.swapCursor(null)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_drafts, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val scheduleSupported = extraFeaturesService.isSupported(ExtraFeaturesService.FEATURE_SCHEDULE_STATUS)
        menu.setItemAvailability(R.id.scheduled_statuses, scheduleSupported)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.scheduled_statuses -> {
                if (extraFeaturesService.isEnabled(ExtraFeaturesService.FEATURE_SCHEDULE_STATUS)) {
                    val scheduleManageIntent = statusScheduleController?.createManageIntent()
                    startActivity(scheduleManageIntent)
                } else {
                    ExtraFeaturesIntroductionDialogFragment.show(childFragmentManager,
                            ExtraFeaturesService.FEATURE_SCHEDULE_STATUS,
                            requestCode = REQUEST_PURCHASE_EXTRA_FEATURES)
                }
                return true
            }
        }
        return false
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.action_multi_select_drafts, menu)
        listView.updateSelectionItems(menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        updateTitle(mode)
        listView.updateSelectionItems(menu)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                val f = DeleteDraftsConfirmDialogFragment()
                val args = Bundle()
                args.putLongArray(EXTRA_IDS, listView.checkedItemIds)
                f.arguments = args
                f.show(childFragmentManager, "delete_drafts_confirm")
                mode.finish()
            }
            R.id.send -> {
                sendDrafts(listView.checkedItemIds)
                mode.finish()
            }
            R.id.select_all -> {
                listView.selectAll()
            }
            R.id.select_none -> {
                listView.selectNone()
            }
            R.id.invert_selection -> {
                listView.invertSelection()
            }
            else -> {
                return false
            }
        }
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {

    }


    override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long,
            checked: Boolean) {
        updateTitle(mode)
        listView.updateSelectionItems(mode.menu)
    }

    override fun onItemClick(view: AdapterView<*>, child: View, position: Int, id: Long) {
        val item = adapter.getDraft(position)
        if (TextUtils.isEmpty(item.action_type)) {
            editDraft(item)
            return
        }
        when (item.action_type) {
            "0", "1", Draft.Action.UPDATE_STATUS, Draft.Action.REPLY, Draft.Action.QUOTE -> {
                editDraft(item)
            }
        }
    }


    fun setListShown(listShown: Boolean) {
        listContainer.visibility = if (listShown) View.VISIBLE else View.GONE
        progressContainer.visibility = if (listShown) View.GONE else View.VISIBLE
        emptyView.visibility = if (listShown && adapter.isEmpty) View.VISIBLE else View.GONE
    }

    private fun editDraft(draft: Draft) {
        val intent = Intent(INTENT_ACTION_EDIT_DRAFT)
        intent.putExtra(EXTRA_DRAFT, draft)
        context.contentResolver.delete(Drafts.CONTENT_URI, Expression.equals(Drafts._ID, draft._id).sql, null)
        startActivityForResult(intent, REQUEST_COMPOSE)
    }

    private fun sendDrafts(list: LongArray): Boolean {
        for (id in list) {
            val uri = Uri.withAppendedPath(Drafts.CONTENT_URI, id.toString())
            notificationManager.cancel(uri.toString(), NOTIFICATION_ID_DRAFTS)
            val sendIntent = Intent(context, LengthyOperationsService::class.java)
            sendIntent.action = IntentConstants.INTENT_ACTION_SEND_DRAFT
            sendIntent.data = uri
            context.startService(sendIntent)
        }
        return true
    }

    private fun updateTitle(mode: ActionMode?) {
        if (listView == null || mode == null) return
        val count = listView.checkedItemCount
        mode.title = resources.getQuantityString(R.plurals.Nitems_selected, count, count)
    }

    class DeleteDraftsConfirmDialogFragment : BaseDialogFragment(), OnClickListener {

        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val args = arguments ?: return
                    AsyncTaskUtils.executeTask(DeleteDraftsTask(activity, args.getLongArray(IntentConstants.EXTRA_IDS)))
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val context = activity
            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.delete_drafts_confirm)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setNegativeButton(android.R.string.cancel, null)
            val dialog = builder.create()
            dialog.setOnShowListener {
                it as AlertDialog
                it.applyTheme()
            }
            return dialog
        }

    }

    private class DeleteDraftsTask(
            activity: FragmentActivity,
            private val ids: LongArray
    ) : AsyncTask<Any, Any, Unit>() {

        private val activityRef = WeakReference(activity)

        override fun doInBackground(vararg params: Any) {
            val activity = activityRef.get() ?: return
            deleteDrafts(activity, ids)
        }

        override fun onPreExecute() {
            val activity = activityRef.get() ?: return
            (activity as IBaseActivity<*>).executeAfterFragmentResumed { activity ->
                val f = ProgressDialogFragment.show(activity.supportFragmentManager, FRAGMENT_TAG_DELETING_DRAFTS)
                f.isCancelable = false
            }
        }

        override fun onPostExecute(result: Unit) {
            val activity = activityRef.get() ?: return
            (activity as IBaseActivity<*>).executeAfterFragmentResumed { activity ->
                val fm = activity.supportFragmentManager
                val f = fm.findFragmentByTag(FRAGMENT_TAG_DELETING_DRAFTS)
                if (f is DialogFragment) {
                    f.dismiss()
                }
            }
            val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            ids.forEach { id ->
                val tag = Uri.withAppendedPath(Drafts.CONTENT_URI, id.toString()).toString()
                notificationManager.cancel(tag, NOTIFICATION_ID_DRAFTS)
            }
        }

        companion object {

            private val FRAGMENT_TAG_DELETING_DRAFTS = "deleting_drafts"
        }
    }
}
