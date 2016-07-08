package org.mariotaku.twidere.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import org.mariotaku.twidere.Constants.GOOGLE_APIS_SERVER_CLIENT_ID
import org.mariotaku.twidere.Constants.LOGTAG

/**
 * Created by mariotaku on 16/5/14.
 */
class PlusServiceGoogleSignInActivity : BasePlusServiceSignInActivity(), GoogleApiClient.OnConnectionFailedListener {

    private var mGoogleApiClient: GoogleApiClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(GOOGLE_APIS_SERVER_CLIENT_ID).build()
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build()
        signInWithGoogle()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_GOOGLE_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    private fun signInWithGoogle() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, REQUEST_GOOGLE_SIGN_IN)
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        Log.d(LOGTAG, "handleSignInResult:" + result.isSuccess)
        if (result.isSuccess) {
            // TODO Signed in successfully, show authenticated UI.
            val acct = result.signInAccount
            acct!!.idToken
            Log.d(LOGTAG, "sign in name:" + acct.displayName!!)
        } else {
            // TODO Signed out, show unauthenticated UI.
        }
        finish()
    }

    companion object {

        private val REQUEST_GOOGLE_SIGN_IN = 101
    }

}
