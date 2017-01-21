package org.mariotaku.twidere.activity.sync

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.DriveScopes
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.set
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.constant.dataSyncProviderInfoKey
import org.mariotaku.twidere.model.sync.GoogleDriveSyncProviderInfo


class GoogleDriveAuthActivity : BaseActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private lateinit var googleApiClient: GoogleApiClient

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(DriveScopes.DRIVE), Scope(DriveScopes.DRIVE_METADATA))
                .requestServerAuthCode(GoogleDriveSyncProviderInfo.WEB_CLIENT_ID, true)
                .build()

        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleApiClient.connect();
    }

    override fun onDestroy() {
        googleApiClient.disconnect()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_RESOLVE_ERROR -> {
                if (!googleApiClient.isConnected && !googleApiClient.isConnecting) {
                    googleApiClient.connect()
                }
            }
            REQUEST_GOOGLE_SIGN_IN -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                    val authCode = result.signInAccount?.serverAuthCode ?: return
                    val httpTransport = NetHttpTransport()
                    val jsonFactory = JacksonFactory.getDefaultInstance()
                    val tokenRequest = GoogleAuthorizationCodeTokenRequest(httpTransport, jsonFactory,
                            "https://www.googleapis.com/oauth2/v4/token", GoogleDriveSyncProviderInfo.WEB_CLIENT_ID,
                            GoogleDriveSyncProviderInfo.WEB_CLIENT_SECRET, authCode, "")
                    task {
                        tokenRequest.execute()
                    }.successUi { response ->
                        preferences[dataSyncProviderInfoKey] = GoogleDriveSyncProviderInfo(response.refreshToken)
                        setResult(Activity.RESULT_OK)
                        finish()
                    }.fail { ex ->
                        ex.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onConnected(connectionHint: Bundle?) {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback {
            // Start sign in
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
            startActivityForResult(signInIntent, REQUEST_GOOGLE_SIGN_IN)
        }
    }

    override fun onConnectionSuspended(cause: Int) {
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        if (connectionResult.hasResolution()) {
            connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR)
        } else {

        }
    }

    companion object {

        private const val REQUEST_RESOLVE_ERROR: Int = 101
        private const val REQUEST_GOOGLE_SIGN_IN: Int = 102
    }
}