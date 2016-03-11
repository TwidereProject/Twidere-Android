package org.mariotaku.twidere.task.twitter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

import com.squareup.otto.Bus;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.SQLFunctions;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.model.RefreshTaskParam;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.GetStatusesTaskEvent;
import org.mariotaku.twidere.provider.TwidereDataStore.AccountSupportColumns;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.task.AbstractTask;
import org.mariotaku.twidere.task.CacheUsersStatusesTask;
import org.mariotaku.twidere.task.util.TaskStarter;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.ErrorInfoStore;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
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

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.RefreshEvent;
import edu.tsinghua.hotmobi.model.TimelineType;

/**
 * Created by mariotaku on 16/1/2.
 */
public abstract class GetStatusesTask extends AbstractTask<RefreshTaskParam,
        List<TwitterWrapper.StatusListResponse>, Object> implements Constants {

    protected final Context context;
    @Inject
    protected SharedPreferencesWrapper preferences;
    @Inject
    protected Bus bus;
    @Inject
    protected ErrorInfoStore errorInfoStore;

    public GetStatusesTask(Context context) {
        this.context = context;
        GeneralComponentHelper.build(context).inject(this);
    }

    @NonNull
    public abstract ResponseList<Status> getStatuses(Twitter twitter, Paging paging)
            throws TwitterException;

    @NonNull
    protected abstract Uri getContentUri();

    @TimelineType
    protected abstract String getTimelineType();

    @Override
    public void afterExecute(RefreshTaskParam params, List<TwitterWrapper.StatusListResponse> result) {
        bus.post(new GetStatusesTaskEvent(getContentUri(), false, AsyncTwitterWrapper.getException(result)));
    }


    @UiThread
    public void notifyStart() {
        bus.post(new GetStatusesTaskEvent(getContentUri(), true, null));
    }

    @Override
    public List<TwitterWrapper.StatusListResponse> doLongOperation(final RefreshTaskParam param) {
        final UserKey[] accountKeys = param.getAccountKeys();
        final String[] maxIds = param.getMaxIds();
        final String[] sinceIds = param.getSinceIds();
        final long[] maxSortIds = param.getMaxSortIds();
        final long[] sinceSortIds = param.getSinceSortIds();
        final List<TwitterWrapper.StatusListResponse> result = new ArrayList<>();
        int idx = 0;
        final int loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
        for (final UserKey accountKey : accountKeys) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, accountKey, true);
            if (twitter == null) continue;
            try {
                final Paging paging = new Paging();
                paging.count(loadItemLimit);
                final String maxId, sinceId;
                long maxSortId = -1, sinceSortId = -1;
                if (maxIds != null && maxIds[idx] != null) {
                    maxId = maxIds[idx];
                    paging.maxId(maxId);
                    if (maxSortIds != null) {
                        maxSortId = maxSortIds[idx];
                    }
                } else {
                    maxSortId = -1;
                    maxId = null;
                }
                if (sinceIds != null && sinceIds[idx] != null) {
                    sinceId = sinceIds[idx];
                    long sinceIdLong = NumberUtils.toLong(sinceId, -1);
                    //TODO handle non-twitter case
                    if (sinceIdLong != -1) {
                        paging.sinceId(String.valueOf(sinceIdLong - 1));
                    } else {
                        paging.sinceId(sinceId);
                    }
                    if (sinceSortIds != null) {
                        sinceSortId = sinceSortIds[idx];
                    }
                    if (maxIds == null || sinceIds[idx] == null) {
                        paging.setLatestResults(true);
                    }
                } else {
                    sinceId = null;
                }
                final List<Status> statuses = getStatuses(twitter, paging);
                InternalTwitterContentUtils.getStatusesWithQuoteData(twitter, statuses);
                storeStatus(accountKey, statuses, sinceId, maxId, sinceSortId, maxSortId,
                        loadItemLimit, true);
                // TODO cache related data and preload
                final CacheUsersStatusesTask cacheTask = new CacheUsersStatusesTask(context);
                cacheTask.setParams(new TwitterWrapper.StatusListResponse(accountKey, statuses));
                TaskStarter.execute(cacheTask);
                errorInfoStore.remove(getErrorInfoKey(), accountKey.getId());
            } catch (final TwitterException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, e);
                }
                if (e.isCausedByNetworkIssue()) {
                    errorInfoStore.put(getErrorInfoKey(), accountKey.getId(),
                            ErrorInfoStore.CODE_NETWORK_ERROR);
                }
                result.add(new TwitterWrapper.StatusListResponse(accountKey, e));
            }
            idx++;
        }
        return result;
    }

    @NonNull
    protected abstract String getErrorInfoKey();

    private void storeStatus(final UserKey accountKey, final List<Status> statuses,
                             final String sinceId, final String maxId,
                             final long sinceSortId, final long maxSortId,
                             int loadItemLimit, final boolean notify) {
        if (statuses == null || statuses.isEmpty() || accountKey == null) {
            return;
        }
        final Uri uri = getContentUri();
        final ContentResolver resolver = context.getContentResolver();
        final boolean noItemsBefore = DataStoreUtils.getStatusCount(context, uri, accountKey) <= 0;
        final ContentValues[] values = new ContentValues[statuses.size()];
        final String[] statusIds = new String[statuses.size()];
        int minIdx = -1;
        boolean hasIntersection = false;
        for (int i = 0, j = statuses.size(); i < j; i++) {
            final Status status = statuses.get(i);
            values[i] = ContentValuesCreator.createStatus(status, accountKey);
            values[i].put(Statuses.INSERTED_DATE, System.currentTimeMillis());
            if (minIdx == -1 || status.compareTo(statuses.get(minIdx)) < 0) {
                minIdx = i;
            }
            if (sinceId != null && status.getSortId() <= sinceSortId) {
                hasIntersection = true;
            }
            statusIds[i] = status.getId();
        }
        // Delete all rows conflicting before new data inserted.
        final Expression accountWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY);
        final Expression statusWhere = Expression.inArgs(new Columns.Column(Statuses.STATUS_ID),
                statusIds.length);
        final String countWhere = Expression.and(accountWhere, statusWhere).getSQL();
        final String[] whereArgs = new String[statusIds.length + 1];
        System.arraycopy(statusIds, 0, whereArgs, 1, statusIds.length);
        whereArgs[0] = accountKey.toString();
        final String[] projection = {SQLFunctions.COUNT()};
        final int rowsDeleted;
        final Cursor countCur = resolver.query(uri, projection, countWhere, whereArgs, null);
        try {
            if (countCur != null && countCur.moveToFirst()) {
                rowsDeleted = countCur.getInt(0);
            } else {
                rowsDeleted = 0;
            }
        } finally {
            Utils.closeSilently(countCur);
        }

        // BEGIN HotMobi
        final RefreshEvent event = RefreshEvent.create(context, statusIds, getTimelineType());
        HotMobiLogger.getInstance(context).log(accountKey, event);
        // END HotMobi

        // Insert a gap.
        final boolean deletedOldGap = rowsDeleted > 0 && ArrayUtils.contains(statusIds, maxId);
        final boolean noRowsDeleted = rowsDeleted == 0;
        // Why loadItemLimit / 2? because it will not acting strange in most cases
        final boolean insertGap = minIdx != -1 && (noRowsDeleted || deletedOldGap) && !noItemsBefore
                && !hasIntersection && statuses.size() > loadItemLimit / 2;
        if (insertGap) {
            values[minIdx].put(Statuses.IS_GAP, true);
        }
        // Insert previously fetched items.
        final Uri insertUri = UriUtils.appendQueryParameters(uri, QUERY_PARAM_NOTIFY, notify);
        ContentResolverUtils.bulkInsert(resolver, insertUri, values);

        if (maxId != null && sinceId == null) {
            final ContentValues noGapValues = new ContentValues();
            noGapValues.put(Statuses.IS_GAP, false);
            final String noGapWhere = Expression.and(Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                    Expression.equalsArgs(Statuses.STATUS_ID)).getSQL();
            final String[] noGapWhereArgs = {accountKey.toString(), maxId};
            resolver.update(getContentUri(), noGapValues, noGapWhere, noGapWhereArgs);
        }
    }


}
