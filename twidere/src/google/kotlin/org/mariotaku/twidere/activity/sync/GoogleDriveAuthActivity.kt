package org.mariotaku.twidere.activity.sync

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import org.mariotaku.kpreferences.set
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.constant.dataSyncProviderInfoKey
import org.mariotaku.twidere.model.sync.GoogleDriveSyncProviderInfo


class GoogleDriveAuthActivity : BaseActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private lateinit var googleApiClient: GoogleApiClient

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleApiClient = GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
    }

    override fun onStart() {
        super.onStart()
        googleApiClient.connect()
    }

    override fun onStop() {
        googleApiClient.disconnect()
        super.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            RESOLVE_CONNECTION_REQUEST_CODE -> if (resultCode == Activity.RESULT_OK) {
                googleApiClient.connect()
            }
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE)
            } catch (e: IntentSender.SendIntentException) {
                // Unable to resolve, message user appropriately
            }

        } else {
            preferences[dataSyncProviderInfoKey] = null
            GooglePlayServicesUtil.showErrorDialogFragment(connectionResult.errorCode, this, null, 0) {
                finish()
            }
        }
    }

    override fun onConnected(connectionHint: Bundle?) {
        preferences[dataSyncProviderInfoKey] = GoogleDriveSyncProviderInfo()
        finish()
    }

    override fun onConnectionSuspended(cause: Int) {
        finish()
    }

    companion object {

        private const val RESOLVE_CONNECTION_REQUEST_CODE: Int = 101
    }
}