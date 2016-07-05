package org.mariotaku.twidere.task;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.squareup.otto.Bus;

import org.mariotaku.abstask.library.AbstractTask;
import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.ProfileUpdatedEvent;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.TwitterWrapper;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import java.io.IOException;

import javax.inject.Inject;

/**
 * Created by mariotaku on 16/3/11.
 */
public class UpdateProfileBannerImageTask<ResultHandler> extends AbstractTask<Object,
        SingleResponse<ParcelableUser>, ResultHandler> implements Constants {

    @Inject
    protected Bus mBus;

    private final UserKey mAccountKey;
    private final Uri mImageUri;
    private final boolean mDeleteImage;
    private final Context mContext;

    public UpdateProfileBannerImageTask(final Context context, final UserKey accountKey,
                                        final Uri imageUri, final boolean deleteImage) {
        //noinspection unchecked
        GeneralComponentHelper.build(context).inject((UpdateProfileBannerImageTask<Object>) this);
        mContext = context;
        mAccountKey = accountKey;
        mImageUri = imageUri;
        mDeleteImage = deleteImage;
    }

    @Override
    protected void afterExecute(ResultHandler callback, final SingleResponse<ParcelableUser> result) {
        super.afterExecute(callback, result);
        if (result.hasData()) {
            Utils.showOkMessage(mContext, R.string.profile_banner_image_updated, false);
            mBus.post(new ProfileUpdatedEvent(result.getData()));
        } else {
            Utils.showErrorMessage(mContext, R.string.action_updating_profile_banner_image, result.getException(),
                    true);
        }
    }

    @Override
    protected SingleResponse<ParcelableUser> doLongOperation(final Object params) {
        try {
            final MicroBlog twitter = MicroBlogAPIFactory.getInstance(mContext, mAccountKey,
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
            return SingleResponse.Companion.getInstance(ParcelableUserUtils.fromUser(user, mAccountKey));
        } catch (MicroBlogException | IOException e) {
            return SingleResponse.Companion.getInstance(e);
        }
    }


}
