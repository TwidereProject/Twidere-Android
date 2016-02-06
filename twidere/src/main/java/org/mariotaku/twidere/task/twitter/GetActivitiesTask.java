package org.mariotaku.twidere.task.twitter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.task.ManagedAsyncTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.ErrorInfoStore;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.content.ContentResolverUtils;
import org.mariotaku.twidere.util.message.GetActivitiesTaskEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mariotaku on 16/1/4.
 */
public abstract class GetActivitiesTask extends ManagedAsyncTask<Object, Object, Object> {

    protected final AsyncTwitterWrapper twitterWrapper;
    protected final long[] accountIds;
    protected final long[] maxIds;
    protected final long[] sinceIds;

    public GetActivitiesTask(AsyncTwitterWrapper twitterWrapper, String tag, long[] accountIds, long[] maxIds, long[] sinceIds) {
        super(twitterWrapper.getContext(), tag);
        this.twitterWrapper = twitterWrapper;
        this.accountIds = accountIds;
        this.maxIds = maxIds;
        this.sinceIds = sinceIds;
    }

    @Override
    protected Object doInBackground(Object... params) {
        final Context context = twitterWrapper.getContext();
        final ContentResolver cr = context.getContentResolver();
        final int loadItemLimit = twitterWrapper.getPreferences().getInt(KEY_LOAD_ITEM_LIMIT);
        boolean saveReadPosition = false;
        for (int i = 0; i < accountIds.length; i++) {
            final long accountId = accountIds[i];
            final boolean noItemsBefore = DataStoreUtils.getActivitiesCount(context,
                    getContentUri(), accountId) <= 0;
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, accountId,
                    true);
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
            final ErrorInfoStore errorInfoStore = twitterWrapper.getErrorInfoStore();
            // We should delete old activities has intersection with new items
            try {
                final ResponseList<Activity> activities = getActivities(accountId, twitter, paging);
                storeActivities(cr, loadItemLimit, accountId, noItemsBefore, activities);
//                if (saveReadPosition && TwitterAPIFactory.isOfficialTwitterInstance(context, twitter)) {
                if (saveReadPosition) {
                    saveReadPosition(accountId, twitter);
                }
                errorInfoStore.remove(ErrorInfoStore.KEY_INTERACTIONS, accountId);
            } catch (TwitterException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, e);
                }
                if (e.getErrorCode() == 220) {
                    errorInfoStore.put(ErrorInfoStore.KEY_INTERACTIONS, accountId,
                            ErrorInfoStore.CODE_NO_ACCESS_FOR_CREDENTIALS);
                }
            }
        }
        return null;
    }

    private void storeActivities(ContentResolver cr, int loadItemLimit, long accountId,
                                 boolean noItemsBefore, ResponseList<Activity> activities) {
        long[] deleteBound = new long[2];
        Arrays.fill(deleteBound, -1);
        List<ContentValues> valuesList = new ArrayList<>();
        for (Activity activity : activities) {
            final ParcelableActivity parcelableActivity = new ParcelableActivity(activity, accountId, false);
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
            Expression where = Expression.and(
                    Expression.equals(Activities.ACCOUNT_ID, accountId),
                    Expression.greaterEquals(Activities.MIN_POSITION, deleteBound[0]),
                    Expression.lesserEquals(Activities.MAX_POSITION, deleteBound[1])
            );
            int rowsDeleted = cr.delete(getContentUri(), where.getSQL(), null);
            boolean insertGap = valuesList.size() >= loadItemLimit && !noItemsBefore
                    && rowsDeleted <= 0;
            if (insertGap && !valuesList.isEmpty()) {
                valuesList.get(valuesList.size() - 1).put(Activities.IS_GAP, true);
            }
        }
        ContentResolverUtils.bulkInsert(cr, getContentUri(), valuesList);
    }

    protected abstract void saveReadPosition(long accountId, Twitter twitter);

    protected abstract ResponseList<Activity> getActivities(long accountId, Twitter twitter, Paging paging) throws TwitterException;

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        bus.post(new GetActivitiesTaskEvent(getContentUri(), false, null));
    }

    protected abstract Uri getContentUri();

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        bus.post(new GetActivitiesTaskEvent(getContentUri(), true, null));
    }
}
