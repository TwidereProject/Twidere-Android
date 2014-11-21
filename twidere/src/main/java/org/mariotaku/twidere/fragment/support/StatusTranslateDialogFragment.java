/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment.support;

import static org.mariotaku.twidere.model.ParcelableLocation.isValidLocation;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserColor;
import static org.mariotaku.twidere.util.Utils.getCardHighlightColor;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;
import static org.mariotaku.twidere.util.Utils.getStatusTypeIconRes;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.StatusListViewHolder;

import twitter4j.TranslationResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class StatusTranslateDialogFragment extends BaseSupportDialogFragment implements
		LoaderCallbacks<SingleResponse<TranslationResult>> {

	private StatusListViewHolder mHolder;
	private ProgressBar mProgressBar;
	private TextView mMessageView;
	private View mProgressContainer;
	private View mStatusContainer;

	public StatusTranslateDialogFragment() {
		setStyle(STYLE_NO_TITLE, 0);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Bundle args = getArguments();
		if (args == null || args.getParcelable(EXTRA_STATUS) == null) {
			dismiss();
			return;
		}
		getLoaderManager().initLoader(0, args, this);
	}

	@Override
	public Loader<SingleResponse<TranslationResult>> onCreateLoader(final int id, final Bundle args) {
		final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
		mStatusContainer.setVisibility(View.GONE);
		mProgressContainer.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.VISIBLE);
		mMessageView.setVisibility(View.VISIBLE);
		mMessageView.setText(R.string.please_wait);
		return new TranslationResultLoader(getActivity(), status.account_id, status.id);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.dialog_translate_status, parent, false);
		mProgressContainer = view.findViewById(R.id.progress_container);
		mProgressBar = (ProgressBar) mProgressContainer.findViewById(android.R.id.progress);
		mMessageView = (TextView) mProgressContainer.findViewById(android.R.id.message);
		mStatusContainer = view.findViewById(R.id.status_container);
		mHolder = new StatusListViewHolder(mStatusContainer);
		mHolder.setShowAsGap(false);
		mHolder.setAccountColorEnabled(true);
		((View) mHolder.content).setPadding(0, 0, 0, 0);
		mHolder.content.setItemBackground(null);
		mHolder.content.setItemSelector(null);
		return view;
	}

	@Override
	public void onLoaderReset(final Loader<SingleResponse<TranslationResult>> loader) {

	}

	@Override
	public void onLoadFinished(final Loader<SingleResponse<TranslationResult>> loader,
			final SingleResponse<TranslationResult> data) {
		final Bundle args = getArguments();
		final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
		if (status != null && data.getData() != null) {
			displayTranslatedStatus(status, data.getData());
			mStatusContainer.setVisibility(View.VISIBLE);
			mProgressContainer.setVisibility(View.GONE);
		} else {
			mStatusContainer.setVisibility(View.GONE);
			mProgressContainer.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);
			mMessageView.setVisibility(View.VISIBLE);
			mMessageView.setText(Utils.getErrorMessage(getActivity(), data.getException()));
		}
	}

	private void displayTranslatedStatus(final ParcelableStatus status, final TranslationResult translated) {
		if (status == null || translated == null) return;
		final TwidereApplication application = getApplication();
		final ImageLoaderWrapper loader = application.getImageLoaderWrapper();
		final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mHolder.setTextSize(prefs.getInt(KEY_TEXT_SIZE, getDefaultTextSize(getActivity())));
		mHolder.text.setText(translated.getText());
		mHolder.name.setText(status.user_name);
		mHolder.screen_name.setText("@" + status.user_screen_name);
		mHolder.screen_name.setVisibility(View.VISIBLE);

		final String retweeted_by_name = status.retweeted_by_name;
		final String retweeted_by_screen_name = status.retweeted_by_screen_name;

		final boolean isMyStatus = status.account_id == status.user_id;
		final boolean hasMedia = status.media != null && status.media.length > 0;
		mHolder.setUserColor(getUserColor(getActivity(), status.user_id, true));
		mHolder.setHighlightColor(getCardHighlightColor(false, status.is_favorite, status.is_retweet));

		mHolder.setIsMyStatus(isMyStatus && !prefs.getBoolean(KEY_INDICATE_MY_STATUS, true));

		mHolder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0,
				getUserTypeIconRes(status.user_is_verified, status.user_is_protected), 0);
		mHolder.time.setTime(status.timestamp);
		final int type_icon = getStatusTypeIconRes(status.is_favorite, isValidLocation(status.location), hasMedia,
				status.is_possibly_sensitive);
		mHolder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0, type_icon, 0);
		mHolder.reply_retweet_status
				.setVisibility(status.in_reply_to_status_id != -1 || status.is_retweet ? View.VISIBLE : View.GONE);
		if (status.is_retweet && !TextUtils.isEmpty(retweeted_by_name) && !TextUtils.isEmpty(retweeted_by_screen_name)) {
			if (!prefs.getBoolean(KEY_NAME_FIRST, true)) {
				mHolder.reply_retweet_status.setText(status.retweet_count > 1 ? getString(
						R.string.retweeted_by_with_count, retweeted_by_screen_name, status.retweet_count - 1)
						: getString(R.string.retweeted_by, retweeted_by_screen_name));
			} else {
				mHolder.reply_retweet_status.setText(status.retweet_count > 1 ? getString(
						R.string.retweeted_by_with_count, retweeted_by_name, status.retweet_count - 1) : getString(
						R.string.retweeted_by, retweeted_by_name));
			}
			mHolder.reply_retweet_status.setText(status.retweet_count > 1 ? getString(R.string.retweeted_by_with_count,
					retweeted_by_name, status.retweet_count - 1) : getString(R.string.retweeted_by, retweeted_by_name));
			mHolder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_retweet, 0, 0,
					0);
		} else if (status.in_reply_to_status_id > 0 && !TextUtils.isEmpty(status.in_reply_to_screen_name)) {
			mHolder.reply_retweet_status.setText(getString(R.string.in_reply_to, status.in_reply_to_screen_name));
			mHolder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_conversation,
					0, 0, 0);
		}
		if (prefs.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true)) {
			loader.displayProfileImage(mHolder.my_profile_image, status.user_profile_image_url);
			loader.displayProfileImage(mHolder.profile_image, status.user_profile_image_url);
		} else {
			mHolder.profile_image.setVisibility(View.GONE);
			mHolder.my_profile_image.setVisibility(View.GONE);
		}
		mHolder.image_preview_container.setVisibility(View.GONE);
	}

	public static void show(final FragmentManager fm, final ParcelableStatus status) {
		final StatusTranslateDialogFragment df = new StatusTranslateDialogFragment();
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_STATUS, status);
		df.setArguments(args);
		df.show(fm, "translate_status");
	}

	public static final class TranslationResultLoader extends AsyncTaskLoader<SingleResponse<TranslationResult>> {

		private final long mAccountId;
		private final long mStatusId;

		public TranslationResultLoader(final Context context, final long accountId, final long statusId) {
			super(context);
			mAccountId = accountId;
			mStatusId = statusId;
		}

		@Override
		public SingleResponse<TranslationResult> loadInBackground() {
			final Context context = getContext();
			final Twitter twitter = Utils.getTwitterInstance(context, mAccountId, false);
			final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
			if (twitter == null) return SingleResponse.getInstance();
			try {
				final String prefDest = prefs.getString(KEY_TRANSLATION_DESTINATION, null);
				final String dest;
				if (TextUtils.isEmpty(prefDest)) {
					dest = twitter.getAccountSettings().getLanguage();
					final Editor editor = prefs.edit();
					editor.putString(KEY_TRANSLATION_DESTINATION, dest);
					editor.apply();
				} else {
					dest = prefDest;
				}
				return SingleResponse.getInstance(twitter.showTranslation(mStatusId, dest));
			} catch (final TwitterException e) {
				return SingleResponse.getInstance(e);
			}
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

	}

}
