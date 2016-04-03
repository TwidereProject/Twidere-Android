package org.mariotaku.twidere.task.twitter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.squareup.otto.Bus;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.abstask.library.AbstractTask;
import org.mariotaku.abstask.library.TaskStarter;
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusValuesCreator;
import org.mariotaku.twidere.model.RefreshTaskParam;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.message.GetStatusesTaskEvent;
import org.mariotaku.twidere.model.util.ParcelableCredentialsUtils;
import org.mariotaku.twidere.model.util.ParcelableStatusUtils;
import org.mariotaku.twidere.provider.TwidereDataStore.AccountSupportColumns;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.task.CacheUsersStatusesTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.ErrorInfoStore;
import org.mariotaku.twidere.util.InternalTwitterContentUtils;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.TwitterWrapper;
import org.mariotaku.twidere.util.UriUtils;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.content.ContentResolverUtils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import java.util.ArrayList;
import java.util.Collections;
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
    @Inject
    protected UserColorNameManager manager;

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
    public void afterExecute(List<TwitterWrapper.StatusListResponse> result) {
        context.getContentResolver().notifyChange(getContentUri(), null);
        bus.post(new GetStatusesTaskEvent(getContentUri(), false, AsyncTwitterWrapper.getException(result)));
    }

    @Override
    protected void beforeExecute() {
        bus.post(new GetStatusesTaskEvent(getContentUri(), true, null));
    }

    @Override
    public List<TwitterWrapper.StatusListResponse> doLongOperation(final RefreshTaskParam param) {
        if (param.shouldAbort()) return Collections.emptyList();
        final UserKey[] accountKeys = param.getAccountKeys();
        final String[] maxIds = param.getMaxIds();
        final String[] sinceIds = param.getSinceIds();
        final long[] maxSortIds = param.getMaxSortIds();
        final long[] sinceSortIds = param.getSinceSortIds();
        final List<TwitterWrapper.StatusListResponse> result = new ArrayList<>();
        int idx = 0;
        final int loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
        for (final UserKey accountKey : accountKeys) {
            final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(context,
                    accountKey);
            if (credentials == null) continue;
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, credentials,
                    true, true);
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
                storeStatus(accountKey, credentials, statuses, sinceId, maxId, sinceSortId,
                        maxSortId, loadItemLimit, false);
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

    private void storeStatus(@NonNull final UserKey accountKey, ParcelableCredentials credentials,
                             @NonNull final List<Status> statuses,
                             final String sinceId, final String maxId,
                             final long sinceSortId, final long maxSortId,
                             int loadItemLimit, final boolean notify) {
        final Uri uri = getContentUri();
        final Uri writeUri = UriUtils.appendQueryParameters(uri, QUERY_PARAM_NOTIFY, notify);
        final ContentResolver resolver = context.getContentResolver();
        final boolean noItemsBefore = DataStoreUtils.getStatusCount(context, uri, accountKey) <= 0;
        final ContentValues[] values = new ContentValues[statuses.size()];
        final String[] statusIds = new String[statuses.size()];
        int minIdx = -1;
        long minPositionKey = -1;
        boolean hasIntersection = false;
        if (!statuses.isEmpty()) {
            final long firstSortId = statuses.get(0).getSortId();
            final long lastSortId = statuses.get(statuses.size() - 1).getSortId();
            // Get id diff of first and last item
            final long sortDiff = firstSortId - lastSortId;

            for (int i = 0, j = statuses.size(); i < j; i++) {
                final Status item = statuses.get(i);
                final ParcelableStatus status = ParcelableStatusUtils.fromStatus(item, accountKey,
                        false);
                ParcelableStatusUtils.updateExtraInformation(status, credentials, manager);
                status.position_key = getPositionKey(status.timestamp, status.sort_id, lastSortId,
                        sortDiff, i, j);
                status.inserted_date = System.currentTimeMillis();
                values[i] = ParcelableStatusValuesCreator.create(status);
                if (minIdx == -1 || item.compareTo(statuses.get(minIdx)) < 0) {
                    minIdx = i;
                    minPositionKey = status.position_key;
                }
                if (sinceId != null && item.getSortId() <= sinceSortId) {
                    hasIntersection = true;
                }
                statusIds[i] = item.getId();
            }
        }
        // Delete all rows conflicting before new data inserted.
        final Expression accountWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY);
        final Expression statusWhere = Expression.inArgs(new Columns.Column(Statuses.STATUS_ID),
                statusIds.length);
        final String deleteWhere = Expression.and(accountWhere, statusWhere).getSQL();
        final String[] deleteWhereArgs = new String[statusIds.length + 1];
        System.arraycopy(statusIds, 0, deleteWhereArgs, 1, statusIds.length);
        deleteWhereArgs[0] = accountKey.toString();
        int olderCount = -1;
        if (minPositionKey > 0) {
            olderCount = DataStoreUtils.getStatusesCount(context, uri, minPositionKey,
                    Statuses.POSITION_KEY, false, accountKey);
        }
        final int rowsDeleted = resolver.delete(writeUri, deleteWhere, deleteWhereArgs);

        // BEGIN HotMobi
        final RefreshEvent event = RefreshEvent.create(context, statusIds, getTimelineType());
        HotMobiLogger.getInstance(context).log(accountKey, event);
        // END HotMobi

        // Insert a gap.
        final boolean deletedOldGap = rowsDeleted > 0 && ArrayUtils.contains(statusIds, maxId);
        final boolean noRowsDeleted = rowsDeleted == 0;
        // Why loadItemLimit / 2? because it will not acting strange in most cases
        final boolean insertGap = minIdx != -1 && olderCount > 0 && (noRowsDeleted || deletedOldGap)
                && !noItemsBefore && !hasIntersection && statuses.size() > loadItemLimit / 2;
        if (insertGap) {
            values[minIdx].put(Statuses.IS_GAP, true);
        }
        // Insert previously fetched items.
        ContentResolverUtils.bulkInsert(resolver, writeUri, values);

        if (maxId != null && sinceId == null) {
            final ContentValues noGapValues = new ContentValues();
            noGapValues.put(Statuses.IS_GAP, false);
            final String noGapWhere = Expression.and(Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                    Expression.equalsArgs(Statuses.STATUS_ID)).getSQL();
            final String[] noGapWhereArgs = {accountKey.toString(), maxId};
            resolver.update(writeUri, noGapValues, noGapWhere, noGapWhereArgs);
        }
    }

    public static long getPositionKey(long timestamp, long sortId, long lastSortId, long sortDiff,
                                      int position, int count) {
        if (sortDiff == 0) return timestamp;
        int extraValue;
        if (sortDiff > 0) {
            // descent sorted by time
            extraValue = count - 1 - position;
        } else {
            // ascent sorted by time
            extraValue = position;
        }
        return timestamp + (sortId - lastSortId) * (499 - count) / sortDiff + extraValue;
    }

}
