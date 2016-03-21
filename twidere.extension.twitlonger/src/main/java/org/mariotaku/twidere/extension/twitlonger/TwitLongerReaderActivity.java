package org.mariotaku.twidere.extension.twitlonger;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.twidere.Twidere;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SpanItem;

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
            "((tl\\.gd|www.twitlonger.com/show)/([\\w\\d]+))", Pattern.CASE_INSENSITIVE);
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
                    if (mStatus.spans != null) {
                        for (SpanItem span : mStatus.spans) {
                            final Matcher m = PATTERN_TWITLONGER.matcher(span.link);
                            if (m.find()) {
                                mTwitLongerPostTask = new TwitLongerReaderTask(this);
                                mTwitLongerPostTask.execute(m.group(GROUP_TWITLONGER_ID));
                                break;
                            }
                        }
                    }
                } else {
                    if (mUser == null) return;
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
                if (mStatus == null || mStatus.spans == null) {
                    finish();
                    return;
                }
                mUser = mStatus.user_screen_name;
                mPreview.setText(mStatus.text_unescaped);
                mActionButton.setEnabled(false);
                for (SpanItem span : mStatus.spans) {
                    final Matcher m = PATTERN_TWITLONGER.matcher(span.link);
                    if (m.find()) {
                        mActionButton.setEnabled(true);
                        break;
                    }
                }
            } else if (Intent.ACTION_VIEW.equals(action) && data != null) {
                mPreview.setText(data.toString());
                final Matcher m = PATTERN_TWITLONGER.matcher(data.toString());
                if (m.find()) {
                    if (mTwitLongerPostTask != null) {
                        mTwitLongerPostTask.cancel(true);
                    }
                    mTwitLongerPostTask = new TwitLongerReaderTask(this);
                    mTwitLongerPostTask.execute(m.group(GROUP_TWITLONGER_ID));
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

    public static class TwitLongerReaderTask extends AsyncTask<String, Object, TaskResponse<Post, TwitLongerException>> {

        private final TwitLongerReaderActivity activity;

        public TwitLongerReaderTask(TwitLongerReaderActivity activity) {
            this.activity = activity;
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
            if (result.hasError()) {
                activity.showError(result.getThrowable());
            } else {
                activity.showResult(result.getObject());
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            activity.showProgress();
        }

    }

    private void showProgress() {
        mProgress.setVisibility(View.VISIBLE);
        mActionButton.setVisibility(View.GONE);
    }

    private void showResult(Post post) {
        mProgress.setVisibility(View.GONE);
        mActionButton.setVisibility(View.VISIBLE);
        mActionButton.setImageResource(R.drawable.ic_menu_share);
        mPreview.setText(post.content);
    }

    private void showError(TwitLongerException e) {
        mProgress.setVisibility(View.GONE);
        mActionButton.setVisibility(View.VISIBLE);
        mActionButton.setImageResource(R.drawable.ic_menu_send);

    }
}
