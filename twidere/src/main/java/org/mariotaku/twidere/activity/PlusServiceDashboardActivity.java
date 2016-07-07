package org.mariotaku.twidere.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.fragment.BaseDialogFragment;

import java.util.List;

public class PlusServiceDashboardActivity extends BaseActivity {

    private static final int REQUEST_PLUS_SERVICE_SIGN_IN = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus_service_dashboard);
        SignInChooserDialogFragment df = new SignInChooserDialogFragment();
        df.show(getSupportFragmentManager(), "sign_in_chooser");
    }

    public static class SignInChooserDialogFragment extends BaseDialogFragment implements
            LoaderManager.LoaderCallbacks<List<ResolveInfo>> {
        private ProviderAdapter mAdapter;
        private boolean mLoaderInitialized;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getContext();
            mAdapter = new ProviderAdapter(context);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.sign_in_with_ellip);
            builder.setAdapter(mAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startLogin(mAdapter.getItem(which));
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    loadInfo();
                }
            });
            return dialog;
        }

        private void startLogin(ResolveInfo info) {
            final FragmentActivity activity = getActivity();
            if (activity == null) return;
            Intent intent = new Intent(INTENT_ACTION_PLUS_SERVICE_SIGN_IN);
            intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
            activity.startActivityForResult(intent, REQUEST_PLUS_SERVICE_SIGN_IN);
            dismiss();
        }

        private void loadInfo() {
            LoaderManager lm = getLoaderManager();
            if (mLoaderInitialized) {
                lm.restartLoader(0, null, this);
            } else {
                lm.initLoader(0, null, this);
                mLoaderInitialized = true;
            }
        }

        @Override
        public Loader<List<ResolveInfo>> onCreateLoader(int id, Bundle args) {
            return new SignInActivitiesLoader(getContext());
        }

        @Override
        public void onLoadFinished(Loader<List<ResolveInfo>> loader, List<ResolveInfo> data) {
            mAdapter.clear();
            if (data != null) {
                mAdapter.addAll(data);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<ResolveInfo>> loader) {
            mAdapter.clear();
        }

        public static class SignInActivitiesLoader extends AsyncTaskLoader<List<ResolveInfo>> {

            public SignInActivitiesLoader(Context context) {
                super(context);
            }

            @Override
            public List<ResolveInfo> loadInBackground() {
                final Context context = getContext();
                Intent intent = new Intent(INTENT_ACTION_PLUS_SERVICE_SIGN_IN);
                intent.setPackage(context.getPackageName());
                final PackageManager pm = context.getPackageManager();
                return pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
            }

            @Override
            protected void onStartLoading() {
                forceLoad();
            }
        }

        static class ProviderAdapter extends ArrayAdapter<ResolveInfo> {

            public ProviderAdapter(Context context) {
                super(context, android.R.layout.simple_list_item_1);
            }

            @Override
            public View getView(int position, @Nullable View convertView, ViewGroup parent) {
                final View view = super.getView(position, convertView, parent);
                final Bundle metaData = getItem(position).activityInfo.metaData;
                ((TextView) view.findViewById(android.R.id.text1)).setText(metaData.getInt(METADATA_KEY_PLUS_SERVICE_SIGN_IN_LABEL));
                return view;
            }
        }
    }
}
