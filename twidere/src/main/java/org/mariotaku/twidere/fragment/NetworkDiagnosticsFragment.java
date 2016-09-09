package org.mariotaku.twidere.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Selection;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.restfu.RestAPIFactory;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.RestHttpClient;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.DependencyHolder;
import org.mariotaku.twidere.util.net.TwidereDns;
import org.xbill.DNS.ResolverConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;

import static org.mariotaku.twidere.Constants.DEFAULT_TWITTER_API_URL_FORMAT;
import static org.mariotaku.twidere.Constants.KEY_BUILTIN_DNS_RESOLVER;
import static org.mariotaku.twidere.Constants.KEY_DNS_SERVER;
import static org.mariotaku.twidere.Constants.KEY_TCP_DNS_QUERY;

/**
 * Network diagnostics
 * Created by mariotaku on 16/2/9.
 */
public class NetworkDiagnosticsFragment extends BaseSupportFragment {

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
        final Activity activity = getActivity();
        if (activity == null) return;
        SpannableString coloredText = SpannableString.valueOf(message.message);
        switch (message.state) {
            case LogText.State.OK: {
                coloredText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity,
                        R.color.material_light_green)), 0, coloredText.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            }
            case LogText.State.ERROR: {
                coloredText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity,
                        R.color.material_red)), 0, coloredText.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            }
            case LogText.State.WARNING: {
                coloredText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity,
                        R.color.material_amber)), 0, coloredText.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                break;
            }
            case LogText.State.DEFAULT:
                break;
        }
        mLogTextView.append(coloredText);
        Selection.setSelection(mLogTextView.getEditableText(), mLogTextView.length());
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
            publishProgress(new LogText("**** NOTICE ****", LogText.State.WARNING));
            publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);
            publishProgress(new LogText("Text below may have personal information, BE CAREFUL TO MAKE IT PUBLIC",
                    LogText.State.WARNING));
            publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);
            DependencyHolder holder = DependencyHolder.Companion.get(mContext);
            final TwidereDns dns = holder.getDns();
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

            for (UserKey accountKey : DataStoreUtils.getAccountKeys(mContext)) {
                final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(mContext, accountKey);
                final MicroBlog twitter = MicroBlogAPIFactory.getInstance(mContext, accountKey, false);
                if (credentials == null || twitter == null) continue;
                publishProgress(new LogText("Testing connection for account " + accountKey));
                publishProgress(LogText.LINEBREAK);
                publishProgress(new LogText("api_url_format: " + credentials.api_url_format), LogText.LINEBREAK);
                publishProgress(new LogText("same_oauth_signing_url: " + credentials.same_oauth_signing_url), LogText.LINEBREAK);
                publishProgress(new LogText("auth_type: " + credentials.auth_type));

                publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);

                publishProgress(new LogText("Testing DNS functionality"));
                publishProgress(LogText.LINEBREAK);
                final Endpoint endpoint = MicroBlogAPIFactory.getEndpoint(credentials, MicroBlog.class);
                final Uri uri = Uri.parse(endpoint.getUrl());
                final String host = uri.getHost();
                if (host != null) {
                    testDns(dns, host);
                    testNativeLookup(host);
                } else {
                    publishProgress(new LogText("API URL format is invalid", LogText.State.ERROR));
                    publishProgress(LogText.LINEBREAK);
                }

                publishProgress(LogText.LINEBREAK);

                publishProgress(new LogText("Testing Network connectivity"));
                publishProgress(LogText.LINEBREAK);

                final String baseUrl;
                if (credentials.api_url_format != null) {
                    baseUrl = MicroBlogAPIFactory.getApiBaseUrl(credentials.api_url_format, "api");
                } else {
                    baseUrl = MicroBlogAPIFactory.getApiBaseUrl(DEFAULT_TWITTER_API_URL_FORMAT, "api");
                }
                RestHttpClient client = RestAPIFactory.getRestClient(twitter).getRestClient();
                HttpResponse response = null;
                try {
                    publishProgress(new LogText("Connecting to " + baseUrl + "..."));
                    HttpRequest.Builder builder = new HttpRequest.Builder();
                    builder.method(GET.METHOD);
                    builder.url(baseUrl);
                    final long start = SystemClock.uptimeMillis();
                    response = client.newCall(builder.build()).execute();
                    publishProgress(new LogText(String.format(Locale.US, " OK (%d ms)",
                            SystemClock.uptimeMillis() - start), LogText.State.OK));
                } catch (IOException e) {
                    publishProgress(new LogText("ERROR: " + e.getMessage(), LogText.State.ERROR));
                }
                publishProgress(LogText.LINEBREAK);
                try {
                    if (response != null) {
                        publishProgress(new LogText("Reading response..."));
                        final long start = SystemClock.uptimeMillis();
                        final CountOutputStream os = new CountOutputStream();
                        response.getBody().writeTo(os);
                        publishProgress(new LogText(String.format(Locale.US, " %d bytes (%d ms)",
                                os.getTotal(), SystemClock.uptimeMillis() - start), LogText.State.OK));
                    }
                } catch (IOException e) {
                    publishProgress(new LogText("ERROR: " + e.getMessage(), LogText.State.ERROR));
                } finally {
                    Utils.closeSilently(response);
                }
                publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);

                publishProgress(new LogText("Testing API functionality"));
                publishProgress(LogText.LINEBREAK);
                testTwitter("verify_credentials", twitter, new TwitterTest() {
                    @Override
                    public void execute(MicroBlog twitter) throws MicroBlogException {
                        twitter.verifyCredentials();
                    }
                });
                testTwitter("get_home_timeline", twitter, new TwitterTest() {
                    @Override
                    public void execute(MicroBlog twitter) throws MicroBlogException {
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

            publishProgress(new LogText("Build information: "));
            publishProgress(new LogText("version_code: " + BuildConfig.VERSION_CODE), LogText.LINEBREAK);
            publishProgress(new LogText("version_name: " + BuildConfig.VERSION_NAME), LogText.LINEBREAK);
            publishProgress(new LogText("flavor: " + BuildConfig.FLAVOR), LogText.LINEBREAK);
            publishProgress(new LogText("debug: " + BuildConfig.DEBUG), LogText.LINEBREAK);
            publishProgress(LogText.LINEBREAK);
            publishProgress(new LogText("Basic system information: "));
            publishProgress(new LogText(String.valueOf(mContext.getResources().getConfiguration())));
            publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);
            publishProgress(new LogText("Active network info: "));
            publishProgress(new LogText(String.valueOf(mConnectivityManager.getActiveNetworkInfo())));
            publishProgress(LogText.LINEBREAK, LogText.LINEBREAK);

            publishProgress(new LogText("Done. You can send this log to me, and I'll contact you to solve related issue."));
            return null;
        }

        private void testDns(TwidereDns dns, final String host) {
            publishProgress(new LogText(String.format("Lookup %s...", host)));
            try {
                final long start = SystemClock.uptimeMillis();
                publishProgress(new LogText(String.valueOf(dns.lookupResolver(host))));
                publishProgress(new LogText(String.format(Locale.US, " OK (%d ms)",
                        SystemClock.uptimeMillis() - start), LogText.State.OK));
            } catch (UnknownHostException e) {
                publishProgress(new LogText("ERROR: " + e.getMessage(), LogText.State.ERROR));
            }
            publishProgress(LogText.LINEBREAK);
        }

        private void testNativeLookup(final String host) {
            publishProgress(new LogText(String.format("Native lookup %s...", host)));
            try {
                final long start = SystemClock.uptimeMillis();
                publishProgress(new LogText(Arrays.toString(InetAddress.getAllByName(host))));
                publishProgress(new LogText(String.format(Locale.US, " OK (%d ms)",
                        SystemClock.uptimeMillis() - start), LogText.State.OK));
            } catch (UnknownHostException e) {
                publishProgress(new LogText("ERROR: " + e.getMessage(), LogText.State.ERROR));
            }
            publishProgress(LogText.LINEBREAK);
        }

        private void testTwitter(String name, MicroBlog twitter, TwitterTest test) {
            publishProgress(new LogText(String.format("Testing %s...", name)));
            try {
                final long start = SystemClock.uptimeMillis();
                test.execute(twitter);
                publishProgress(new LogText(String.format(Locale.US, "OK (%d ms)",
                        SystemClock.uptimeMillis() - start), LogText.State.OK));
            } catch (MicroBlogException e) {
                publishProgress(new LogText("ERROR: " + e.getMessage(), LogText.State.ERROR));
            }
            publishProgress(LogText.LINEBREAK);
        }

        interface TwitterTest {
            void execute(MicroBlog twitter) throws MicroBlogException;
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

        @IntDef({State.DEFAULT, State.OK, State.ERROR, State.WARNING})
        @interface State {
            int DEFAULT = 0;
            int OK = 1;
            int ERROR = 2;
            int WARNING = 3;
        }
    }

    private static class CountOutputStream extends OutputStream {
        private long total;

        public long getTotal() {
            return total;
        }

        @Override
        public void write(int oneByte) throws IOException {
            total++;
        }
    }
}
