package org.mariotaku.twidere.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.dagger.DependencyHolder;
import org.mariotaku.twidere.util.net.TwidereDns;
import org.xbill.DNS.ResolverConfig;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import okhttp3.Dns;

/**
 * Created by mariotaku on 16/2/9.
 */
public class NetworkDiagnosticsFragment extends BaseFragment {

    private TextView mLogTextView;
    private Button mStartDiagnosticsButton;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mStartDiagnosticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogTextView.setText(null);
                new DiagnosticsTask(NetworkDiagnosticsFragment.this).execute();
            }
        });
        mLogTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mStartDiagnosticsButton = (Button) view.findViewById(R.id.start_diagnostics);
        mLogTextView = (TextView) view.findViewById(R.id.log_text);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_network_diagnostics, container, false);
    }

    private void appendMessage(LogText message) {
        SpannableString coloredText = SpannableString.valueOf(message.message);
        switch (message.state) {
            case LogText.State.GOOD: {
                coloredText.setSpan(new ForegroundColorSpan(Color.GREEN), 0, coloredText.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            }
            case LogText.State.BAD: {
                coloredText.setSpan(new ForegroundColorSpan(Color.RED), 0, coloredText.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            }
        }
        mLogTextView.append(coloredText);
    }

    static class DiagnosticsTask extends AsyncTask<Object, LogText, Object> {

        private final WeakReference<NetworkDiagnosticsFragment> mFragmentRef;

        private final Context mContext;
        private final ConnectivityManager mConnectivityManager;

        DiagnosticsTask(NetworkDiagnosticsFragment fragment) {
            mFragmentRef = new WeakReference<>(fragment);
            mContext = fragment.getActivity().getApplicationContext();
            mConnectivityManager = (ConnectivityManager)
                    mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        @Override
        protected Object doInBackground(Object... params) {
            publishProgress(new LogText("Build information: "));
            publishProgress(new LogText("version_code: " + BuildConfig.VERSION_CODE), LogText.LINEBREAK);
            publishProgress(new LogText("version_name: " + BuildConfig.VERSION_NAME), LogText.LINEBREAK);
            publishProgress(new LogText("flavor: " + BuildConfig.FLAVOR), LogText.LINEBREAK);
            publishProgress(LogText.LINEBREAK);
            publishProgress(new LogText("Basic system information: "));
            publishProgress(new LogText(String.valueOf(mContext.getResources().getConfiguration())));
            publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);
            publishProgress(new LogText("Active network info: "));
            publishProgress(new LogText(String.valueOf(mConnectivityManager.getActiveNetworkInfo())));
            publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);
            publishProgress(new LogText("**** NOTICE ****"));
            publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);
            publishProgress(new LogText("Text below may have personal information, BE CAREFUL TO MAKE IT PUBLIC"));
            publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);
            DependencyHolder holder = DependencyHolder.get(mContext);
            final Dns dns = holder.getDns();
            final SharedPreferencesWrapper prefs = holder.getPreferences();
            publishProgress(new LogText("Network preferences"), LogText.LINEBREAK);
            publishProgress(new LogText("using_resolver: " + prefs.getBoolean(KEY_BUILTIN_DNS_RESOLVER)), LogText.LINEBREAK);
            publishProgress(new LogText("tcp_dns_query: " + prefs.getBoolean(KEY_TCP_DNS_QUERY)), LogText.LINEBREAK);
            publishProgress(new LogText("dns_server: " + prefs.getString(KEY_DNS_SERVER, null)), LogText.LINEBREAK);
            publishProgress(LogText.LINEBREAK);
            publishProgress(new LogText("System DNS servers"), LogText.LINEBREAK);


            final String[] servers = ResolverConfig.getCurrentConfig().servers();
            if (servers != null) {
                publishProgress(new LogText(Arrays.toString(servers)));
            } else {
                publishProgress(new LogText("null"));
            }
            publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);

            for (long accountId : DataStoreUtils.getAccountIds(mContext)) {
                final ParcelableCredentials credentials = ParcelableCredentials.getCredentials(mContext, accountId);
                final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountId, false);
                if (credentials == null || twitter == null) continue;
                publishProgress(new LogText("Testing connection for account " + accountId));
                publishProgress(LogText.LINEBREAK);
                publishProgress(new LogText("api_url_format: " + credentials.api_url_format), LogText.LINEBREAK);
                publishProgress(new LogText("same_oauth_signing_url: " + credentials.same_oauth_signing_url), LogText.LINEBREAK);
                publishProgress(new LogText("auth_type: " + credentials.auth_type));

                publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);

                publishProgress(new LogText("Testing DNS functionality"));
                publishProgress(LogText.LINEBREAK);
                final Endpoint endpoint = TwitterAPIFactory.getEndpoint(credentials, Twitter.class);
                final Uri uri = Uri.parse(endpoint.getUrl());
                final String host = uri.getHost();
                if (host != null) {
                    testDns(dns, host);
                    testNativeLookup(host);
                } else {
                    publishProgress(new LogText("API URL format is invalid", LogText.State.BAD));
                    publishProgress(LogText.LINEBREAK);
                }

                publishProgress(LogText.LINEBREAK);

                publishProgress(new LogText("Testing API functionality"));
                publishProgress(LogText.LINEBREAK);
                testTwitter("verify_credentials", twitter, new TwitterTest() {
                    @Override
                    public void execute(Twitter twitter) throws TwitterException {
                        twitter.verifyCredentials();
                    }
                });
                testTwitter("get_home_timeline", twitter, new TwitterTest() {
                    @Override
                    public void execute(Twitter twitter) throws TwitterException {
                        twitter.getHomeTimeline(new Paging().count(1));
                    }
                });
                publishProgress(LogText.LINEBREAK);
            }

            publishProgress(LogText.LINEBREAK);

            publishProgress(new LogText("Testing common host names"));
            publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);

            testDns(dns, "www.google.com");
            testNativeLookup("www.google.com");
            publishProgress(LogText.LINEBREAK);
            testDns(dns, "github.com");
            testNativeLookup("github.com");
            publishProgress(LogText.LINEBREAK);
            testDns(dns, "twitter.com");
            testNativeLookup("twitter.com");

            publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);
            publishProgress(new LogText("Done. You can send this log to me, and I'll contact you to solve related issue."));
            return null;
        }

        private void testDns(Dns dns, final String host) {
            publishProgress(new LogText(String.format("Lookup %s...", host)));
            try {
                final long start = SystemClock.uptimeMillis();
                if (dns instanceof TwidereDns) {
                    publishProgress(new LogText(String.valueOf(((TwidereDns) dns).lookupResolver(host))));
                } else {
                    publishProgress(new LogText(String.valueOf(dns.lookup(host))));
                }
                publishProgress(new LogText(String.format(" OK (%d ms)", SystemClock.uptimeMillis()
                        - start), LogText.State.GOOD));
            } catch (UnknownHostException e) {
                publishProgress(new LogText("ERROR: " + e.getMessage(), LogText.State.BAD));
            }
            publishProgress(LogText.LINEBREAK);
        }

        private void testNativeLookup(final String host) {
            publishProgress(new LogText(String.format("Native lookup %s...", host)));
            try {
                final long start = SystemClock.uptimeMillis();
                publishProgress(new LogText(Arrays.toString(InetAddress.getAllByName(host))));
                publishProgress(new LogText(String.format(" OK (%d ms)", SystemClock.uptimeMillis()
                        - start), LogText.State.GOOD));
            } catch (UnknownHostException e) {
                publishProgress(new LogText("ERROR: " + e.getMessage(), LogText.State.BAD));
            }
            publishProgress(LogText.LINEBREAK);
        }

        private void testTwitter(String name, Twitter twitter, TwitterTest test) {
            publishProgress(new LogText(String.format("Testing %s...", name)));
            try {
                final long start = SystemClock.uptimeMillis();
                test.execute(twitter);
                publishProgress(new LogText(String.format("OK (%d ms)", SystemClock.uptimeMillis()
                        - start), LogText.State.GOOD));
            } catch (TwitterException e) {
                publishProgress(new LogText("ERROR: " + e.getMessage(), LogText.State.BAD));
            }
            publishProgress(LogText.LINEBREAK);
        }

        interface TwitterTest {
            void execute(Twitter twitter) throws TwitterException;
        }


        @Override
        protected void onProgressUpdate(LogText... values) {
            NetworkDiagnosticsFragment fragment = mFragmentRef.get();
            if (fragment == null) return;
            for (LogText value : values) {
                fragment.appendMessage(value);
            }
        }

        @Override
        protected void onPreExecute() {
            NetworkDiagnosticsFragment fragment = mFragmentRef.get();
            if (fragment == null) return;
            fragment.diagStart();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object o) {
            NetworkDiagnosticsFragment fragment = mFragmentRef.get();
            if (fragment == null) return;
            fragment.logReady();
            super.onPostExecute(o);
        }
    }

    private void diagStart() {
        mStartDiagnosticsButton.setText(R.string.please_wait);
        mStartDiagnosticsButton.setEnabled(false);
    }

    private void logReady() {
        mStartDiagnosticsButton.setText(R.string.send);
        mStartDiagnosticsButton.setEnabled(true);
        mStartDiagnosticsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Twidere Network Diagnostics");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, mLogTextView.getText());
                startActivity(Intent.createChooser(intent, getString(R.string.send)));
            }
        });
    }

    static class LogText {
        static final LogText LINEBREAK = new LogText("\n");
        String message;
        @State
        int state = State.DEFAULT;

        LogText(String message, @State int state) {
            this.message = message;
            this.state = state;
        }

        LogText(String message) {
            this.message = message;
        }

        @IntDef({State.DEFAULT, State.GOOD, State.BAD})
        @interface State {
            int DEFAULT = 0;
            int GOOD = 1;
            int BAD = 2;
        }
    }

}
