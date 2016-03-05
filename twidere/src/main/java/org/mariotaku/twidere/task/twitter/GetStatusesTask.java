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
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.sqliteqb.library.SQLFunctions;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.model.AccountId;
import org.mariotaku.twidere.model.RefreshTaskParam;
import org.mariotaku.twidere.model.message.GetStatusesTaskEvent;
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

    private void storeStatus(final long accountId, final String accountHost,
                             final List<Status> statuses,
                             final long sinceId, final long maxId, final boolean notify) {
        if (statuses == null || statuses.isEmpty() || accountId <= 0) {
            return;
        }
        final Uri uri = getContentUri();
        final ContentResolver resolver = context.getContentResolver();
        final boolean noItemsBefore = DataStoreUtils.getStatusCount(context, uri, accountId) <= 0;
        final ContentValues[] values = new ContentValues[statuses.size()];
        final long[] statusIds = new long[statuses.size()];
        long minId = -1;
        int minIdx = -1;
        boolean hasIntersection = false;
        for (int i = 0, j = statuses.size(); i < j; i++) {
            final Status status = statuses.get(i);
            values[i] = ContentValuesCreator.createStatus(status, accountId, accountHost);
            values[i].put(Statuses.INSERTED_DATE, System.currentTimeMillis());
            final long id = status.getId();
            if (sinceId > 0 && id <= sinceId) {
                hasIntersection = true;
            }
            if (minId == -1 || id < minId) {
                minId = id;
                minIdx = i;
            }
            statusIds[i] = id;
        }
        // Delete all rows conflicting before new data inserted.
        final Expression accountWhere = Expression.equals(Statuses.ACCOUNT_ID, accountId);
        final Expression statusWhere = Expression.in(new Columns.Column(Statuses.STATUS_ID),
                new RawItemArray(statusIds));
        final String countWhere = Expression.and(accountWhere, statusWhere).getSQL();
        final String[] projection = {SQLFunctions.COUNT()};
        final int rowsDeleted;
        final Cursor countCur = resolver.query(uri, projection, countWhere, null, null);
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
        HotMobiLogger.getInstance(context).log(accountId, event);
        // END HotMobi

        // Insert a gap.
        final boolean deletedOldGap = rowsDeleted > 0 && ArrayUtils.contains(statusIds, maxId);
        final boolean noRowsDeleted = rowsDeleted == 0;
        final boolean insertGap = minId > 0 && (noRowsDeleted || deletedOldGap) && !noItemsBefore
                && !hasIntersection;
        if (insertGap && minIdx != -1) {
            values[minIdx].put(Statuses.IS_GAP, true);
        }
        // Insert previously fetched items.
        final Uri insertUri = UriUtils.appendQueryParameters(uri, QUERY_PARAM_NOTIFY, notify);
        ContentResolverUtils.bulkInsert(resolver, insertUri, values);

    }

    @TimelineType
    protected abstract String getTimelineType();


    @Override
    public void afterExecute(List<TwitterWrapper.StatusListResponse> result) {
        bus.post(new GetStatusesTaskEvent(getContentUri(), false, AsyncTwitterWrapper.getException(result)));
    }

    @UiThread
    public void notifyStart() {
        bus.post(new GetStatusesTaskEvent(getContentUri(), true, null));
    }

    @Override
    public List<TwitterWrapper.StatusListResponse> doLongOperation(final RefreshTaskParam param) {
        final AccountId[] accountIds = param.getAccountIds();
        final long[] maxIds = param.getMaxIds();
        final long[] sinceIds = param.getSinceIds();
        final List<TwitterWrapper.StatusListResponse> result = new ArrayList<>();
        int idx = 0;
        final int loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
        for (final AccountId accountId : accountIds) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, accountId.getId(),
                    accountId.getHost(), true);
            if (twitter == null) continue;
            try {
                final Paging paging = new Paging();
                paging.count(loadItemLimit);
                final long maxId, sinceId;
                if (maxIds != null && maxIds[idx] > 0) {
                    maxId = maxIds[idx];
                    paging.maxId(maxId);
                } else {
                    maxId = -1;
                }
                if (sinceIds != null && sinceIds[idx] > 0) {
                    sinceId = sinceIds[idx];
                    paging.sinceId(sinceId - 1);
                    if (maxIds == null || sinceIds[idx] <= 0) {
                        paging.setLatestResults(true);
                    }
                } else {
                    sinceId = -1;
                }
                final List<Status> statuses = getStatuses(twitter, paging);
                InternalTwitterContentUtils.getStatusesWithQuoteData(twitter, statuses);
                storeStatus(accountId.getId(), accountId.getHost(), statuses, sinceId, maxId, true);
                // TODO cache related data and preload
                final CacheUsersStatusesTask cacheTask = new CacheUsersStatusesTask(context);
                cacheTask.setParams(new TwitterWrapper.StatusListResponse(accountId, statuses));
                TaskStarter.execute(cacheTask);
                errorInfoStore.remove(getErrorInfoKey(), accountId.getId());
            } catch (final TwitterException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, e);
                }
                if (e.isCausedByNetworkIssue()) {
                    errorInfoStore.put(getErrorInfoKey(), accountId.getId(),
                            ErrorInfoStore.CODE_NETWORK_ERROR);
                }
                result.add(new TwitterWrapper.StatusListResponse(accountId, e));
            }
            idx++;
        }
        return result;
    }

    @NonNull
    protected abstract String getErrorInfoKey();


}
