package org.mariotaku.twidere.extension.twitlonger;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.twidere.Twidere;
import org.mariotaku.twidere.model.ParcelableStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitLongerReaderActivity extends Activity implements Constants, OnClickListener {

    private TextView mPreview;
    private ImageButton mActionButton;
    private ProgressBar mProgress;
    private String mResult, mUser;
    private ParcelableStatus mStatus;
    private TwitLongerReaderTask mTwitLongerPostTask;
    private static final Pattern PATTERN_TWITLONGER = Pattern.compile(
            "((tl\\.gd|www.twitlonger.com\\/show)\\/([\\w\\d]+))", Pattern.CASE_INSENSITIVE);
    private static final int GROUP_TWITLONGER_ID = 3;

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.action: {

                if (mResult == null) {
                    if (mStatus == null) return;
                    if (mTwitLongerPostTask != null) {
                        mTwitLongerPostTask.cancel(true);
                    }
                    final Matcher m = PATTERN_TWITLONGER.matcher(mStatus.text_html);
                    if (m.find()) {
                        mTwitLongerPostTask = new TwitLongerReaderTask(m.group(GROUP_TWITLONGER_ID));
                        mTwitLongerPostTask.execute();
                    }
                } else {
                    if (mUser == null || mResult == null) return;
                    final Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "@" + mUser + ": " + mResult);
                    startActivity(Intent.createChooser(intent, getString(R.string.share)));
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final Uri data = intent.getData();
        final String action = intent.getAction();
        setContentView(R.layout.main);
        mPreview = (TextView) findViewById(R.id.text);
        mActionButton = (ImageButton) findViewById(R.id.action);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mResult = savedInstanceState != null ? savedInstanceState.getString(Twidere.EXTRA_TEXT) : null;
        mUser = savedInstanceState != null ? savedInstanceState.getString(Twidere.EXTRA_USER) : null;
        if (mResult == null || mUser == null) {
            if (Twidere.INTENT_ACTION_EXTENSION_OPEN_STATUS.equals(action)) {
                mStatus = Twidere.getStatusFromIntent(getIntent());
                if (mStatus == null || mStatus.text_html == null) {
                    finish();
                    return;
                }
                mUser = mStatus.user_screen_name;
                mPreview.setText(Html.fromHtml(mStatus.text_html));
                final Matcher m = PATTERN_TWITLONGER.matcher(mStatus.text_html);
                mActionButton.setEnabled(m.find());
            } else if (Intent.ACTION_VIEW.equals(action) && data != null) {
                mPreview.setText(data.toString());
                final Matcher m = PATTERN_TWITLONGER.matcher(data.toString());
                if (m.find()) {
                    if (mTwitLongerPostTask != null) {
                        mTwitLongerPostTask.cancel(true);
                    }
                    mTwitLongerPostTask = new TwitLongerReaderTask(m.group(GROUP_TWITLONGER_ID));
                    mTwitLongerPostTask.execute();
                } else {
                    finish();
                    return;
                }
            }
        } else {
            mPreview.setText(mResult);
        }

    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putString(Twidere.EXTRA_TEXT, mResult);
        outState.putString(Twidere.EXTRA_USER, mUser);
        super.onSaveInstanceState(outState);
    }

    public final class TwitLongerReaderTask extends AsyncTask<String, Object, TaskResponse<Post, TwitLongerException>> {

        private final String id;

        public TwitLongerReaderTask(final String id) {
            this.id = id;
        }

        @Override
        protected TaskResponse<Post, TwitLongerException> doInBackground(final String... args) {
            final TwitLonger tl = TwitLongerFactory.getInstance(TWITLONGER_API_KEY, null);
            try {
                return TaskResponse.getInstance(tl.getPost(args[0]));
            } catch (final TwitLongerException e) {
                return TaskResponse.getInstance(e);
            }
        }

        @Override
        protected void onPostExecute(final TaskResponse<Post, TwitLongerException> result) {
            mProgress.setVisibility(View.GONE);
            mActionButton.setVisibility(View.VISIBLE);
            if (result.hasError()) {
                mActionButton.setImageResource(R.drawable.ic_menu_send);
            } else {
                mActionButton.setImageResource(R.drawable.ic_menu_share);
            }

            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            mProgress.setVisibility(View.VISIBLE);
            mActionButton.setVisibility(View.GONE);
            super.onPreExecute();
        }

    }
}
