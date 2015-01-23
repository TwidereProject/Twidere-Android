package org.mariotaku.twidere.extension.twitlonger;

import org.mariotaku.twidere.Twidere;
import org.mariotaku.twidere.extension.twitlonger.TwitLonger.TwitLongerException;
import org.mariotaku.twidere.extension.twitlonger.TwitLonger.TwitLongerResponse;
import org.mariotaku.twidere.model.ComposingStatus;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TwitLongerPostActivity extends Activity implements Constants, OnClickListener {

	private TextView mPreview;
	private ImageButton mActionButton;
	private ProgressBar mProgress;
	private String mResult;
	private ComposingStatus mStatus;
	private TwitLongerPostTask mTwitLongerPostTask;

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.action: {
				if (mResult == null) {
					if (mTwitLongerPostTask != null) {
						mTwitLongerPostTask.cancel(true);
					}
					mTwitLongerPostTask = new TwitLongerPostTask();
					mTwitLongerPostTask.execute();
				} else {
					Twidere.replaceComposeActivityText(this, mResult);
				}
				break;
			}
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mPreview = (TextView) findViewById(R.id.text);
		mActionButton = (ImageButton) findViewById(R.id.action);
		mProgress = (ProgressBar) findViewById(R.id.progress);
		mStatus = Twidere.getComposingStatusFromIntent(getIntent());
		if (mStatus == null) {
			finish();
			return;
		}
		mResult = savedInstanceState != null ? savedInstanceState.getString(Intent.EXTRA_TEXT) : null;
		mPreview.setText(mResult != null ? mResult : mStatus.text);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		outState.putString(Intent.EXTRA_TEXT, mResult);
		super.onSaveInstanceState(outState);
	}

	public final class TwitLongerPostTask extends AsyncTask<Void, Void, Object> {

		@Override
		protected Object doInBackground(final Void... args) {
			final TwitLonger tl = new TwitLonger(TWITLONGER_APP_NAME, TWITLONGER_API_KEY);
			try {
				return tl.post(mStatus.text, mStatus.screen_name, mStatus.in_reply_to_id,
						mStatus.in_reply_to_screen_name);
			} catch (final TwitLongerException e) {
				return e;
			}
		}

		@Override
		protected void onPostExecute(final Object result) {
			mProgress.setVisibility(View.GONE);
			mActionButton.setVisibility(View.VISIBLE);
			mActionButton.setImageResource(result instanceof TwitLongerResponse ? R.drawable.ic_menu_mark
					: R.drawable.ic_menu_send);
			if (result instanceof TwitLongerResponse) {
				mResult = ((TwitLongerResponse) result).content;
				mPreview.setText(mResult);
			} else if (result instanceof TwitLongerException) {
				Toast.makeText(TwitLongerPostActivity.this,
						getString(R.string.error_message, ((TwitLongerException) result).getMessage()),
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(TwitLongerPostActivity.this, R.string.error_unknown_error, Toast.LENGTH_LONG).show();
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
