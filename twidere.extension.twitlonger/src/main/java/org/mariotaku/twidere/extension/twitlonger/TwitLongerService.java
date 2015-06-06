package org.mariotaku.twidere.extension.twitlonger;

import java.lang.ref.WeakReference;

import org.mariotaku.twidere.ITweetShortener;
import org.mariotaku.twidere.extension.twitlonger.TwitLonger.TwitLongerException;
import org.mariotaku.twidere.extension.twitlonger.TwitLonger.TwitLongerResponse;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Tweet shortener example
 * 
 * @author mariotaku
 */
public class TwitLongerService extends Service implements Constants {

	private final TweetShortenerStub mBinder = new TweetShortenerStub(this);
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	/**
	 * @return Shortened tweet.
	 */
	public String shorten(String text, String screen_name, long in_reply_to_status_id) {
		final TwitLonger tl = new TwitLonger(TWITLONGER_APP_NAME, TWITLONGER_API_KEY);
		try {
			final TwitLongerResponse response = tl.post(text, screen_name, in_reply_to_status_id, null);
			if (response != null) return response.content;
		} catch (TwitLongerException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * By making this a static class with a WeakReference to the Service, we
	 * ensure that the Service can be GCd even when the system process still has
	 * a remote reference to the stub.
	 */
	private static final class TweetShortenerStub extends ITweetShortener.Stub {

		final WeakReference<TwitLongerService> mService;

		public TweetShortenerStub(TwitLongerService service) {
			mService = new WeakReference<>(service);
		}

		@Override
		public String shorten(String text, String screen_name, long in_reply_to_status_id) {
			return mService.get().shorten(text, screen_name, in_reply_to_status_id);
		}
		
	}

}
