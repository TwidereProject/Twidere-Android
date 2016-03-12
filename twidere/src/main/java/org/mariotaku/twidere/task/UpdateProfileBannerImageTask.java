package org.mariotaku.twidere.task;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.ProfileUpdatedEvent;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.TwitterWrapper;
import org.mariotaku.twidere.util.Utils;

import java.io.FileNotFoundException;

/**
 * Created by mariotaku on 16/3/11.
 */
public class UpdateProfileBannerImageTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

    private final UserKey mAccountKey;
    private final Uri mImageUri;
    private final boolean mDeleteImage;
    private final Context mContext;

    public UpdateProfileBannerImageTask(final Context context, final UserKey accountKey,
                                        final Uri imageUri, final boolean deleteImage) {
        super(context);
        mContext = context;
        mAccountKey = accountKey;
        mImageUri = imageUri;
        mDeleteImage = deleteImage;
    }

    @Override
    protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
        super.onPostExecute(result);
        if (result.hasData()) {
            Utils.showOkMessage(mContext, R.string.profile_banner_image_updated, false);
            bus.post(new ProfileUpdatedEvent(result.getData()));
        } else {
            Utils.showErrorMessage(mContext, R.string.action_updating_profile_banner_image, result.getException(),
                    true);
        }
    }

    @Override
    protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {
        try {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountKey,
                    true);
            TwitterWrapper.updateProfileBannerImage(mContext, twitter, mImageUri, mDeleteImage);
            // Wait for 5 seconds, see
            // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                Log.w(LOGTAG, e);
            }
            final User user = twitter.verifyCredentials();
            return SingleResponse.getInstance(ParcelableUserUtils.fromUser(user, mAccountKey));
        } catch (TwitterException | FileNotFoundException e) {
            return SingleResponse.getInstance(e);
        }
    }


}
