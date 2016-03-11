package org.mariotaku.twidere.task;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.otto.Bus;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.FriendshipTaskEvent;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import javax.inject.Inject;

/**
 * Created by mariotaku on 16/3/11.
 */
public abstract class AbsFriendshipOperationTask extends AbstractTask<AbsFriendshipOperationTask.Arguments,
        SingleResponse<ParcelableUser>, Object> implements Constants {

    protected final Context context;
    @FriendshipTaskEvent.Action
    protected final int action;
    @Inject
    protected Bus bus;
    @Inject
    protected AsyncTwitterWrapper twitter;
    @Inject
    protected SharedPreferencesWrapper preferences;
    @Inject
    protected UserColorNameManager manager;

    public AbsFriendshipOperationTask(Context context, @FriendshipTaskEvent.Action int action) {
        this.context = context;
        this.action = action;
        GeneralComponentHelper.build(context).inject(this);
    }


    @Override
    protected final void beforeExecute(Arguments params) {
        twitter.addUpdatingRelationshipId(params.accountKey, params.userKey);
        final FriendshipTaskEvent event = new FriendshipTaskEvent(action, params.accountKey,
                params.userKey);
        event.setFinished(false);
        bus.post(event);
    }

    @Override
    protected final void afterExecute(Arguments params, SingleResponse<ParcelableUser> result) {
        twitter.removeUpdatingRelationshipId(params.accountKey, params.userKey);
        final FriendshipTaskEvent event = new FriendshipTaskEvent(action, params.accountKey,
                params.userKey);
        event.setFinished(true);
        if (result.hasData()) {
            final ParcelableUser user = result.getData();
            showSucceededMessage(params, user);
            event.setSucceeded(true);
            event.setUser(result.getData());
        } else {
            showErrorMessage(params, result.getException());
        }
        bus.post(event);
    }

    @Override
    public final SingleResponse<ParcelableUser> doLongOperation(final Arguments args) {
        final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(context,
                args.accountKey);
        if (credentials == null) return SingleResponse.getInstance();
        final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, credentials, false, false);
        if (twitter == null) return SingleResponse.getInstance();
        try {
            final User user = perform(twitter, credentials, args);
            final ParcelableUser parcelableUser = ParcelableUserUtils.fromUser(user, args.accountKey);
            succeededWorker(twitter, credentials, args, parcelableUser);
            return SingleResponse.getInstance(parcelableUser, null);
        } catch (final TwitterException e) {
            return SingleResponse.getInstance(null, e);
        }
    }

    @NonNull
    protected abstract User perform(@NonNull Twitter twitter,
                                    @NonNull ParcelableCredentials credentials,
                                    @NonNull Arguments args) throws TwitterException;

    protected abstract void succeededWorker(@NonNull Twitter twitter,
                                            @NonNull ParcelableCredentials credentials,
                                            @NonNull Arguments args,
                                            @NonNull ParcelableUser user);

    protected abstract void showSucceededMessage(@NonNull Arguments params, @NonNull ParcelableUser user);

    protected abstract void showErrorMessage(@NonNull Arguments params, @Nullable Exception exception);

    public final void setup(@NonNull UserKey accountKey, @NonNull UserKey userKey) {
        setParams(new Arguments(accountKey, userKey));
    }

    protected static class Arguments {
        @NonNull
        protected final UserKey accountKey;
        @NonNull
        protected final UserKey userKey;

        protected Arguments(@NonNull UserKey accountKey, @NonNull UserKey userKey) {
            this.accountKey = accountKey;
            this.userKey = userKey;
        }
    }

}
