package org.mariotaku.twidere.task.twitter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.sqliteqb.library.SQLFunctions;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.task.CacheUsersStatusesTask;
import org.mariotaku.twidere.task.ManagedAsyncTask;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.TwitterContentUtils;
import org.mariotaku.twidere.util.TwitterWrapper;
import org.mariotaku.twidere.util.UriUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.content.ContentResolverUtils;
import org.mariotaku.twidere.util.message.GetStatusesTaskEvent;

import java.util.ArrayList;
import java.util.List;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.RefreshEvent;
import edu.tsinghua.hotmobi.model.TimelineType;

/**
 * Created by mariotaku on 16/1/2.
 */
public abstract class GetStatusesTask extends ManagedAsyncTask<Object, TwitterWrapper.TwitterListResponse<Status>, List<TwitterWrapper.StatusListResponse>> {

    private final long[] accountIds, maxIds, sinceIds;
    private AsyncTwitterWrapper twitterWrapper;

    public GetStatusesTask(AsyncTwitterWrapper twitterWrapper, final long[] accountIds,
                           final long[] maxIds, final long[] sinceIds, final String tag) {
        super(twitterWrapper.getContext(), tag);
        this.accountIds = accountIds;
        this.maxIds = maxIds;
        this.sinceIds = sinceIds;
        this.twitterWrapper = twitterWrapper;
    }

    public abstract ResponseList<org.mariotaku.twidere.api.twitter.model.Status> getStatuses(Twitter twitter, Paging paging)
            throws TwitterException;

    @NonNull
    protected abstract Uri getDatabaseUri();

    final boolean isMaxIdsValid() {
        return maxIds != null && maxIds.length == accountIds.length;
    }

    final boolean isSinceIdsValid() {
        return sinceIds != null && sinceIds.length == accountIds.length;
    }

    private void storeStatus(long accountId, List<org.mariotaku.twidere.api.twitter.model.Status> statuses, long maxId, boolean truncated, boolean notify) {
        if (statuses == null || statuses.isEmpty() || accountId <= 0) {
            return;
        }
        final Uri uri = getDatabaseUri();
        final Context context = twitterWrapper.getContext();
        final ContentResolver resolver = context.getContentResolver();
        final boolean noItemsBefore = DataStoreUtils.getStatusCountInDatabase(context, uri, accountId) <= 0;
        final ContentValues[] values = new ContentValues[statuses.size()];
        final long[] statusIds = new long[statuses.size()];
        long minId = -1;
        int minIdx = -1;
        for (int i = 0, j = statuses.size(); i < j; i++) {
            final org.mariotaku.twidere.api.twitter.model.Status status = statuses.get(i);
            values[i] = ContentValuesCreator.createStatus(status, accountId);
            final long id = status.getId();
            if (minId == -1 || id < minId) {
                minId = id;
                minIdx = i;
            }
            statusIds[i] = id;
        }
        // Delete all rows conflicting before new data inserted.
        final Expression accountWhere = Expression.equals(TwidereDataStore.Statuses.ACCOUNT_ID, accountId);
        final Expression statusWhere = Expression.in(new Columns.Column(TwidereDataStore.Statuses.STATUS_ID), new RawItemArray(statusIds));
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
        final boolean insertGap = minId > 0 && (noRowsDeleted || deletedOldGap) && !truncated
                && !noItemsBefore && statuses.size() > 1;
        if (insertGap && minIdx != -1) {
            values[minIdx].put(TwidereDataStore.Statuses.IS_GAP, true);
        }
        // Insert previously fetched items.
        final Uri insertUri = UriUtils.appendQueryParameters(uri, QUERY_PARAM_NOTIFY, notify);
        ContentResolverUtils.bulkInsert(resolver, insertUri, values);

    }

    protected abstract TimelineType getTimelineType();

    @SafeVarargs
    @Override
    protected final void onProgressUpdate(TwitterWrapper.TwitterListResponse<org.mariotaku.twidere.api.twitter.model.Status>... values) {
        AsyncTaskUtils.executeTask(new CacheUsersStatusesTask(twitterWrapper.getContext()), values);
    }


    @Override
    protected void onPostExecute(List<TwitterWrapper.StatusListResponse> result) {
        super.onPostExecute(result);
        bus.post(new GetStatusesTaskEvent(getDatabaseUri(), false, AsyncTwitterWrapper.getException(result)));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        bus.post(new GetStatusesTaskEvent(getDatabaseUri(), true, null));
    }

    @Override
    protected List<TwitterWrapper.StatusListResponse> doInBackground(final Object... params) {
        final List<TwitterWrapper.StatusListResponse> result = new ArrayList<>();
        if (accountIds == null) return result;
        int idx = 0;
        final SharedPreferencesWrapper preferences = twitterWrapper.getPreferences();
        final Context context = twitterWrapper.getContext();
        final int loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
        for (final long accountId : accountIds) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, accountId, true);
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
                    paging.sinceId(sinceId);
                    if (maxIds == null || sinceIds[idx] <= 0) {
                        paging.setLatestResults(true);
                    }
                } else {
                    sinceId = -1;
                }
                final List<org.mariotaku.twidere.api.twitter.model.Status> statuses = new ArrayList<>();
                final boolean truncated = Utils.truncateStatuses(getStatuses(twitter, paging), statuses, sinceId);
                TwitterContentUtils.getStatusesWithQuoteData(twitter, statuses);
                storeStatus(accountId, statuses, maxId, truncated, true);
                publishProgress(new TwitterWrapper.StatusListResponse(accountId, statuses));
            } catch (final TwitterException e) {
                Log.w(LOGTAG, e);
                result.add(new TwitterWrapper.StatusListResponse(accountId, e));
            }
            idx++;
        }
        return result;
    }

}
