package org.mariotaku.twidere.activity;

import android.content.Intent;
import android.os.Bundle;

import org.mariotaku.twidere.R;

public class PlusServiceDashboardActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus_service_dashboard);
        startActivity(new Intent(this, PlusServiceSignInActivity.class));
    }
}
