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
import org.mariotaku.twidere.model.AccountKey;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.RefreshTaskParam;
import org.mariotaku.twidere.model.message.GetActivitiesTaskEvent;
import org.mariotaku.twidere.model.util.ParcelableActivityUtils;
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
        final AccountKey[] accountIds = param.getAccountKeys();
        final long[] maxIds = param.getMaxIds();
        final long[] sinceIds = param.getSinceIds();
        final ContentResolver cr = context.getContentResolver();
        final int loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT);
        boolean saveReadPosition = false;
        for (int i = 0; i < accountIds.length; i++) {
            final AccountKey accountKey = accountIds[i];
            final boolean noItemsBefore = DataStoreUtils.getActivitiesCount(context, getContentUri(),
                    accountKey) <= 0;
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, accountKey, true);
            if (twitter == null) continue;
            final Paging paging = new Paging();
            paging.count(loadItemLimit);
            if (maxIds != null && maxIds[i] > 0) {
                paging.maxId(maxIds[i]);
            }
            if (sinceIds != null && sinceIds[i] > 0) {
                paging.sinceId(sinceIds[i]);
                if (maxIds == null || maxIds[i] <= 0) {
                    paging.setLatestResults(true);
                    saveReadPosition = true;
                }
            }
            // We should delete old activities has intersection with new items
            try {
                final ResponseList<Activity> activities = getActivities(twitter, accountKey, paging);
                storeActivities(cr, loadItemLimit, accountKey, noItemsBefore, activities);
//                if (saveReadPosition && TwitterAPIFactory.isOfficialTwitterInstance(context, twitter)) {
                if (saveReadPosition) {
                    saveReadPosition(accountKey, twitter);
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

    private void storeActivities(ContentResolver cr, int loadItemLimit, AccountKey accountKey,
                                 boolean noItemsBefore, ResponseList<Activity> activities) {
        long[] deleteBound = new long[2];
        Arrays.fill(deleteBound, -1);
        List<ContentValues> valuesList = new ArrayList<>();
        for (Activity activity : activities) {
            final ParcelableActivity parcelableActivity = ParcelableActivityUtils.fromActivity(activity,
                    accountKey, false);
            if (deleteBound[0] < 0) {
                deleteBound[0] = parcelableActivity.min_position;
            } else {
                deleteBound[0] = Math.min(deleteBound[0], parcelableActivity.min_position);
            }
            if (deleteBound[1] < 0) {
                deleteBound[1] = parcelableActivity.max_position;
            } else {
                deleteBound[1] = Math.max(deleteBound[1], parcelableActivity.max_position);
            }
            final ContentValues values = ContentValuesCreator.createActivity(parcelableActivity);
            values.put(Statuses.INSERTED_DATE, System.currentTimeMillis());
            valuesList.add(values);
        }
        if (deleteBound[0] > 0 && deleteBound[1] > 0) {
            final Expression where = Expression.and(
                    Expression.equalsArgs(Activities.ACCOUNT_KEY),
                    Expression.greaterEqualsArgs(Activities.MIN_POSITION),
                    Expression.lesserEqualsArgs(Activities.MAX_POSITION)
            );
            final String[] whereArgs = {accountKey.toString(), String.valueOf(deleteBound[0]),
                    String.valueOf(deleteBound[1])};
            int rowsDeleted = cr.delete(getContentUri(), where.getSQL(), whereArgs);
            boolean insertGap = valuesList.size() >= loadItemLimit && !noItemsBefore
                    && rowsDeleted <= 0;
            if (insertGap && !valuesList.isEmpty()) {
                valuesList.get(valuesList.size() - 1).put(Activities.IS_GAP, true);
            }
        }
        ContentResolverUtils.bulkInsert(cr, getContentUri(), valuesList);
    }

    protected abstract void saveReadPosition(@NonNull final AccountKey accountId,
                                             @NonNull final Twitter twitter);

    protected abstract ResponseList<Activity> getActivities(@NonNull final Twitter twitter,
                                                            @NonNull final AccountKey accountId,
                                                            @NonNull final Paging paging)
            throws TwitterException;

    @Override
    public void afterExecute(Object result) {
        bus.post(new GetActivitiesTaskEvent(getContentUri(), false, null));
    }

    protected abstract Uri getContentUri();

    @UiThread
    @Override
    public void beforeExecute() {
        bus.post(new GetActivitiesTaskEvent(getContentUri(), true, null));
    }
}
