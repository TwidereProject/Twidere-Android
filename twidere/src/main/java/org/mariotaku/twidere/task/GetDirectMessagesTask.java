package org.mariotaku.twidere.task;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.squareup.otto.Bus;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterErrorCode;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.DirectMessage;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.model.AccountKey;
import org.mariotaku.twidere.model.RefreshTaskParam;
import org.mariotaku.twidere.model.message.GetMessagesTaskEvent;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.ErrorInfoStore;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.TwitterWrapper;
import org.mariotaku.twidere.util.UriUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.content.ContentResolverUtils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by mariotaku on 16/2/14.
 */
public abstract class GetDirectMessagesTask extends AbstractTask<RefreshTaskParam,
        List<TwitterWrapper.MessageListResponse>, Object> implements Constants {

    protected final Context context;
    @Inject
    protected ErrorInfoStore errorInfoStore;
    @Inject
    protected SharedPreferencesWrapper preferences;
    @Inject
    protected Bus bus;

    public GetDirectMessagesTask(Context context) {
        this.context = context;
        GeneralComponentHelper.build(context).inject(this);
    }

    public abstract ResponseList<DirectMessage> getDirectMessages(Twitter twitter, Paging paging)
            throws TwitterException;

    protected abstract Uri getDatabaseUri();

    protected abstract boolean isOutgoing();

    @Override
    public List<TwitterWrapper.MessageListResponse> doLongOperation(final RefreshTaskParam param) {
        final AccountKey[] accountKeys = param.getAccountKeys();
        final long[] sinceIds = param.getSinceIds(), maxIds = param.getMaxIds();
        final List<TwitterWrapper.MessageListResponse> result = new ArrayList<>();
        int idx = 0;
        final int loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
        for (final AccountKey accountKey : accountKeys) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, accountKey, true);
            if (twitter == null) continue;
            try {
                final Paging paging = new Paging();
                paging.setCount(loadItemLimit);
                long max_id = -1, sinceId = -1;
                if (maxIds != null && maxIds[idx] > 0) {
                    max_id = maxIds[idx];
                    paging.setMaxId(max_id);
                }
                if (sinceIds != null && sinceIds[idx] > 0) {
                    sinceId = sinceIds[idx];
                    paging.setSinceId(sinceId - 1);
                }
                final List<DirectMessage> messages = new ArrayList<>();
                final boolean truncated = Utils.truncateMessages(getDirectMessages(twitter, paging), messages,
                        sinceId);
                result.add(new TwitterWrapper.MessageListResponse(accountKey, max_id, sinceId, messages,
                        truncated));
                storeMessages(accountKey, messages, isOutgoing(), true);
                errorInfoStore.remove(ErrorInfoStore.KEY_DIRECT_MESSAGES, accountKey);
            } catch (final TwitterException e) {
                if (e.getErrorCode() == TwitterErrorCode.NO_DM_PERMISSION) {
                    errorInfoStore.put(ErrorInfoStore.KEY_DIRECT_MESSAGES, accountKey,
                            ErrorInfoStore.CODE_NO_DM_PERMISSION);
                } else if (e.isCausedByNetworkIssue()) {
                    errorInfoStore.put(ErrorInfoStore.KEY_DIRECT_MESSAGES, accountKey,
                            ErrorInfoStore.CODE_NETWORK_ERROR);
                }
                if (BuildConfig.DEBUG) {
                    Log.w(TwidereConstants.LOGTAG, e);
                }
                result.add(new TwitterWrapper.MessageListResponse(accountKey, e));
            }
            idx++;
        }
        return result;

    }

    private boolean storeMessages(AccountKey accountKey, List<DirectMessage> messages, boolean isOutgoing, boolean notify) {
        if (messages == null) return true;
        final Uri uri = getDatabaseUri();
        final ContentValues[] valuesArray = new ContentValues[messages.size()];

        for (int i = 0, j = messages.size(); i < j; i++) {
            final DirectMessage message = messages.get(i);
            valuesArray[i] = ContentValuesCreator.createDirectMessage(message, accountKey, isOutgoing);
        }

        // Delete all rows conflicting before new data inserted.
//            final Expression deleteWhere = Expression.and(Expression.equals(DirectMessages.ACCOUNT_ID, accountKey),
//                    Expression.in(new Column(DirectMessages.MESSAGE_ID), new RawItemArray(messageIds)));
//            final Uri deleteUri = UriUtils.appendQueryParameters(uri, QUERY_PARAM_NOTIFY, false);
//            mResolver.delete(deleteUri, deleteWhere.getSQL(), null);


        // Insert previously fetched items.
        final Uri insertUri = UriUtils.appendQueryParameters(uri, TwidereConstants.QUERY_PARAM_NOTIFY, notify);
        ContentResolverUtils.bulkInsert(context.getContentResolver(), insertUri, valuesArray);
        return false;
    }


    public void beforeExecute() {
        bus.post(new GetMessagesTaskEvent(getDatabaseUri(), true, null));
    }

    @Override
    protected void afterExecute(List<TwitterWrapper.MessageListResponse> result) {
        bus.post(new GetMessagesTaskEvent(getDatabaseUri(), false, AsyncTwitterWrapper.getException(result)));
    }
}
