package org.mariotaku.twidere.fragment.premium

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.google.fragment_extra_features_status_play_store.*
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.DropboxAuthStarterActivity
import org.mariotaku.twidere.dropboxAuthTokenKey
import org.mariotaku.twidere.fragment.BaseSupportFragment
import org.mariotaku.twidere.service.DropboxDataSyncService

/**
 * Created by mariotaku on 2016/12/28.
 */

class PlayStoreExtraFeaturesStatusFragment : BaseSupportFragment() {
    private val REQUEST_DROPBOX_AUTH: Int = 201

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateButtons()
        connectStorageService.setOnClickListener {
            startActivityForResult(Intent(context, DropboxAuthStarterActivity::class.java), REQUEST_DROPBOX_AUTH)
        }
        performSync.setOnClickListener {
            context.startService(Intent(context, DropboxDataSyncService::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_DROPBOX_AUTH -> {
                updateButtons()
            }
        }
    }

    private fun updateButtons() {
        if (preferences[dropboxAuthTokenKey] == null) {
            connectStorageService.visibility = View.VISIBLE
            performSync.visibility = View.GONE
        } else {
            connectStorageService.visibility = View.GONE
            performSync.visibility = View.VISIBLE

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_extra_features_status_play_store, container, false)
    }
}
