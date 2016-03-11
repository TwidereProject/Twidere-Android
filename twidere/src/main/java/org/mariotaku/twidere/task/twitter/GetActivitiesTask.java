package org.mariotaku.twidere.task.twitter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

import com.squareup.otto.Bus;

import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.RefreshTaskParam;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.GetActivitiesTaskEvent;
import org.mariotaku.twidere.model.util.ParcelableActivityUtils;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.task.AbstractTask;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.ErrorInfoStore;
import org.mariotaku.twidere.util.ReadStateManager;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.content.ContentResolverUtils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by mariotaku on 16/1/4.
 */
public abstract class GetActivitiesTask extends AbstractTask<RefreshTaskParam, Object, Object> implements Constants {

    protected final Context context;
    @Inject
    protected SharedPreferencesWrapper preferences;
    @Inject
    protected Bus bus;
    @Inject
    protected ErrorInfoStore errorInfoStore;
    @Inject
    protected ReadStateManager readStateManager;

    public GetActivitiesTask(Context context) {
        this.context = context;
        GeneralComponentHelper.build(context).inject(this);
    }

    @Override
    public Object doLongOperation(RefreshTaskParam param) {
        final UserKey[] accountIds = param.getAccountKeys();
        final String[] maxIds = param.getMaxIds();
        final String[] sinceIds = param.getSinceIds();
        final ContentResolver cr = context.getContentResolver();
        final int loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT);
        boolean saveReadPosition = false;
        for (int i = 0; i < accountIds.length; i++) {
            final UserKey accountKey = accountIds[i];
            final boolean noItemsBefore = DataStoreUtils.getActivitiesCount(context, getContentUri(),
                    accountKey) <= 0;
            final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(context,
                    accountKey);
            if (credentials == null) continue;
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, credentials, true,
                    true);
            if (twitter == null) continue;
            final Paging paging = new Paging();
            paging.count(loadItemLimit);
            String maxId = null;
            if (maxIds != null) {
                maxId = maxIds[i];
                if (maxId != null) {
                    paging.maxId(maxId);
                }
            }
            String sinceId = null;
            if (sinceIds != null) {
                sinceId = sinceIds[i];
                if (sinceId != null) {
                    paging.sinceId(sinceId);
                    if (maxIds == null || maxId == null) {
                        paging.setLatestResults(true);
                        saveReadPosition = true;
                    }
                }
            }
            // We should delete old activities has intersection with new items
            try {
                final ResponseList<Activity> activities = getActivities(twitter, credentials, paging);
                storeActivities(cr, loadItemLimit, accountKey, noItemsBefore, activities, sinceId, maxId);
                if (saveReadPosition) {
                    saveReadPosition(accountKey, credentials, twitter);
                }
                errorInfoStore.remove(getErrorInfoKey(), accountKey);
            } catch (TwitterException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, e);
                }
                if (e.getErrorCode() == 220) {
                    errorInfoStore.put(getErrorInfoKey(), accountKey,
                            ErrorInfoStore.CODE_NO_ACCESS_FOR_CREDENTIALS);
                } else if (e.isCausedByNetworkIssue()) {
                    errorInfoStore.put(getErrorInfoKey(), accountKey,
                            ErrorInfoStore.CODE_NETWORK_ERROR);
                }
            }
        }
        return null;
    }

    @NonNull
    protected abstract String getErrorInfoKey();

    private void storeActivities(ContentResolver cr, int loadItemLimit, UserKey accountKey,
                                 boolean noItemsBefore, ResponseList<Activity> activities,
                                 final String sinceId, final String maxId) {
        long[] deleteBound = new long[2];
        Arrays.fill(deleteBound, -1);
        List<ContentValues> valuesList = new ArrayList<>();
        for (Activity activity : activities) {
            final ParcelableActivity parcelableActivity = ParcelableActivityUtils.fromActivity(activity,
                    accountKey, false);
            if (deleteBound[0] < 0) {
                deleteBound[0] = parcelableActivity.min_sort_position;
            } else {
                deleteBound[0] = Math.min(deleteBound[0], parcelableActivity.min_sort_position);
            }
            if (deleteBound[1] < 0) {
                deleteBound[1] = parcelableActivity.max_sort_position;
            } else {
                deleteBound[1] = Math.max(deleteBound[1], parcelableActivity.max_sort_position);
            }
            final ContentValues values = ContentValuesCreator.createActivity(parcelableActivity);
            values.put(Statuses.INSERTED_DATE, System.currentTimeMillis());
            valuesList.add(values);
        }
        if (deleteBound[0] > 0 && deleteBound[1] > 0) {
            final Expression where = Expression.and(
                    Expression.equalsArgs(Activities.ACCOUNT_KEY),
                    Expression.greaterEqualsArgs(Activities.MIN_SORT_POSITION),
                    Expression.lesserEqualsArgs(Activities.MAX_SORT_POSITION)
            );
            final String[] whereArgs = {accountKey.toString(), String.valueOf(deleteBound[0]),
                    String.valueOf(deleteBound[1])};
            int rowsDeleted = cr.delete(getContentUri(), where.getSQL(), whereArgs);
            boolean insertGap = valuesList.size() >= loadItemLimit && !noItemsBefore
                    && rowsDeleted <= 0 && activities.size() > 1;
            if (insertGap && !valuesList.isEmpty()) {
                valuesList.get(valuesList.size() - 1).put(Activities.IS_GAP, true);
            }
        }
        ContentResolverUtils.bulkInsert(cr, getContentUri(), valuesList);

        if (maxId != null && sinceId == null) {
            final ContentValues noGapValues = new ContentValues();
            noGapValues.put(Activities.IS_GAP, false);
            final String noGapWhere = Expression.and(Expression.equalsArgs(Activities.ACCOUNT_KEY),
                    Expression.equalsArgs(Activities.MIN_REQUEST_POSITION),
                    Expression.equalsArgs(Activities.MAX_REQUEST_POSITION)).getSQL();
            final String[] noGapWhereArgs = {accountKey.toString(), maxId, maxId};
            cr.update(getContentUri(), noGapValues, noGapWhere, noGapWhereArgs);
        }
    }

    protected abstract void saveReadPosition(@NonNull final UserKey accountId,
                                             ParcelableCredentials credentials, @NonNull final Twitter twitter);

    protected abstract ResponseList<Activity> getActivities(@NonNull final Twitter twitter,
                                                            @NonNull final ParcelableCredentials credentials,
                                                            @NonNull final Paging paging)
            throws TwitterException;

    @Override
    public void afterExecute(RefreshTaskParam params, Object result) {
        bus.post(new GetActivitiesTaskEvent(getContentUri(), false, null));
    }

    protected abstract Uri getContentUri();

    @UiThread
    @Override
    public void beforeExecute(RefreshTaskParam params) {
        bus.post(new GetActivitiesTaskEvent(getContentUri(), true, null));
    }
}
