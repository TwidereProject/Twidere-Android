package org.mariotaku.twidere.extension.twitlonger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by mariotaku on 16/2/20.
 */
public class RequestPermissionActivity extends Activity implements Constants {
    private static final int REQUEST_REQUEST_PERMISSION = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_REQUEST_PERMISSION: {
                finish();
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = new Intent(INTENT_ACTION_REQUEST_PERMISSIONS);
        intent.setPackage(TWIDERE_PACKAGE_NAME);
        startActivityForResult(intent, REQUEST_REQUEST_PERMISSION);
    }
}
