/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.desmond.asyncmanager.AsyncManager;
import com.desmond.asyncmanager.BackgroundTask;
import com.desmond.asyncmanager.TaskRunnable;
import com.squareup.otto.Bus;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.ArrayLongList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.LongList;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.http.HttpResponseCode;
import org.mariotaku.twidere.api.twitter.model.DirectMessage;
import org.mariotaku.twidere.api.twitter.model.ErrorInfo;
import org.mariotaku.twidere.api.twitter.model.FriendshipUpdate;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.Relationship;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.SavedSearch;
import org.mariotaku.twidere.api.twitter.model.Trends;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserList;
import org.mariotaku.twidere.api.twitter.model.UserListUpdate;
import org.mariotaku.twidere.model.BaseRefreshTaskParam;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableMediaUpdate;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.RefreshTaskParam;
import org.mariotaku.twidere.model.Response;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.message.FavoriteCreatedEvent;
import org.mariotaku.twidere.model.message.FavoriteDestroyedEvent;
import org.mariotaku.twidere.model.message.FollowRequestTaskEvent;
import org.mariotaku.twidere.model.message.FriendshipUpdatedEvent;
import org.mariotaku.twidere.model.message.FriendshipUserUpdatedEvent;
import org.mariotaku.twidere.model.message.ProfileUpdatedEvent;
import org.mariotaku.twidere.model.message.StatusDestroyedEvent;
import org.mariotaku.twidere.model.message.StatusListChangedEvent;
import org.mariotaku.twidere.model.message.StatusRetweetedEvent;
import org.mariotaku.twidere.model.message.UserListCreatedEvent;
import org.mariotaku.twidere.model.message.UserListDestroyedEvent;
import org.mariotaku.twidere.model.util.ParcelableStatusUtils;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedHashtags;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedTrends;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Inbox;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Outbox;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.service.BackgroundOperationService;
import org.mariotaku.twidere.task.GetActivitiesAboutMeTask;
import org.mariotaku.twidere.task.GetActivitiesByFriendsTask;
import org.mariotaku.twidere.task.GetDirectMessagesTask;
import org.mariotaku.twidere.task.GetHomeTimelineTask;
import org.mariotaku.twidere.task.GetSavedSearchesTask;
import org.mariotaku.twidere.task.ManagedAsyncTask;
import org.mariotaku.twidere.task.twitter.GetActivitiesTask;
import org.mariotaku.twidere.util.collection.LongSparseMap;
import org.mariotaku.twidere.util.content.ContentResolverUtils;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.TimelineType;
import edu.tsinghua.hotmobi.model.TweetEvent;

public class AsyncTwitterWrapper extends TwitterWrapper {

    private final Context mContext;
    private final ContentResolver mResolver;

    private final AsyncTaskManager mAsyncTaskManager;
    private final SharedPreferencesWrapper mPreferences;
    private final Bus mBus;
    private final UserColorNameManager mUserColorNameManager;
    private final ErrorInfoStore mErrorInfoStore;

    private LongSparseMap<Long> mCreatingFavoriteIds = new LongSparseMap<>();
    private LongSparseMap<Long> mDestroyingFavoriteIds = new LongSparseMap<>();
    private LongSparseMap<Long> mCreatingRetweetIds = new LongSparseMap<>();
    private LongSparseMap<Long> mDestroyingStatusIds = new LongSparseMap<>();
    private IntList mProcessingFriendshipRequestIds = new ArrayIntList();

    private final LongList mSendingDraftIds = new ArrayLongList();

    public AsyncTwitterWrapper(Context context, UserColorNameManager userColorNameManager,
                               Bus bus, SharedPreferencesWrapper preferences,
                               AsyncTaskManager asyncTaskManager, ErrorInfoStore errorInfoStore) {
        mContext = context;
        mResolver = context.getContentResolver();
        mUserColorNameManager = userColorNameManager;
        mBus = bus;
        mPreferences = preferences;
        mAsyncTaskManager = asyncTaskManager;
        mErrorInfoStore = errorInfoStore;
    }

    public int acceptFriendshipAsync(final long accountId, final long userId) {
        final AcceptFriendshipTask task = new AcceptFriendshipTask(mContext, accountId, userId);
        return mAsyncTaskManager.add(task, true);
    }

    public void addSendingDraftId(long id) {
        synchronized (mSendingDraftIds) {
            mSendingDraftIds.add(id);
            mResolver.notifyChange(Drafts.CONTENT_URI_UNSENT, null);
        }
    }

    public int addUserListMembersAsync(final long accountId, final long listId, final ParcelableUser... users) {
        final AddUserListMembersTask task = new AddUserListMembersTask(accountId, listId, users);
        return mAsyncTaskManager.add(task, true);
    }

    public int cancelRetweetAsync(long account_id, long status_id, long my_retweet_id) {
        if (my_retweet_id > 0)
            return destroyStatusAsync(account_id, my_retweet_id);
        else if (status_id > 0)
            return destroyStatusAsync(account_id, status_id);
        return -1;
    }

    public void clearNotificationAsync(final int notificationType) {
        clearNotificationAsync(notificationType, 0);
    }

    public void clearNotificationAsync(final int notificationId, final long notificationAccount) {
        final ClearNotificationTask task = new ClearNotificationTask(notificationId, notificationAccount);
        AsyncTaskUtils.executeTask(task);
    }

    public void clearUnreadCountAsync(final int position) {
        final ClearUnreadCountTask task = new ClearUnreadCountTask(position);
        AsyncTaskUtils.executeTask(task);
    }

    public int createBlockAsync(final long accountId, final long user_id) {
        final CreateBlockTask task = new CreateBlockTask(accountId, user_id);
        return mAsyncTaskManager.add(task, true);
    }

    public int createFavoriteAsync(final long accountId, final long status_id) {
        final CreateFavoriteTask task = new CreateFavoriteTask(accountId, status_id);
        return mAsyncTaskManager.add(task, true);
    }

    public int createFriendshipAsync(final long accountId, final long userId) {
        final CreateFriendshipTask task = new CreateFriendshipTask(accountId, userId);
        return mAsyncTaskManager.add(task, true);
    }

    public int createMultiBlockAsync(final long accountId, final long[] userIds) {
        final CreateMultiBlockTask task = new CreateMultiBlockTask(accountId, userIds);
        return mAsyncTaskManager.add(task, true);
    }

    public int createMuteAsync(final long accountId, final long user_id) {
        final CreateMuteTask task = new CreateMuteTask(accountId, user_id);
        return mAsyncTaskManager.add(task, true);
    }

    public int createSavedSearchAsync(final long accountId, final String query) {
        final CreateSavedSearchTask task = new CreateSavedSearchTask(accountId, query);
        return mAsyncTaskManager.add(task, true);
    }

    public int createUserListAsync(final long accountId, final String listName, final boolean isPublic,
                                   final String description) {
        final CreateUserListTask task = new CreateUserListTask(mContext, accountId, listName, isPublic,
                description);
        return mAsyncTaskManager.add(task, true);
    }

    public int createUserListSubscriptionAsync(final long accountId, final long listId) {
        final CreateUserListSubscriptionTask task = new CreateUserListSubscriptionTask(accountId, listId);
        return mAsyncTaskManager.add(task, true);
    }

    public int deleteUserListMembersAsync(final long accountId, final long listId, final ParcelableUser... users) {
        final DeleteUserListMembersTask task = new DeleteUserListMembersTask(accountId, listId, users);
        return mAsyncTaskManager.add(task, true);
    }

    public int denyFriendshipAsync(final long accountId, final long userId) {
        final DenyFriendshipTask task = new DenyFriendshipTask(accountId, userId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyBlockAsync(final long accountId, final long user_id) {
        final DestroyBlockTask task = new DestroyBlockTask(accountId, user_id);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyDirectMessageAsync(final long accountId, final long message_id) {
        final DestroyDirectMessageTask task = new DestroyDirectMessageTask(accountId, message_id);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyMessageConversationAsync(final long accountId, final long userId) {
        final DestroyMessageConversationTask task = new DestroyMessageConversationTask(accountId, userId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyFavoriteAsync(final long accountId, final long status_id) {
        final DestroyFavoriteTask task = new DestroyFavoriteTask(accountId, status_id);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyFriendshipAsync(final long accountId, final long user_id) {
        final DestroyFriendshipTask task = new DestroyFriendshipTask(accountId, user_id);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyMuteAsync(final long accountId, final long user_id) {
        final DestroyMuteTask task = new DestroyMuteTask(accountId, user_id);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroySavedSearchAsync(final long accountId, final long searchId) {
        final DestroySavedSearchTask task = new DestroySavedSearchTask(accountId, searchId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyStatusAsync(final long accountId, final long status_id) {
        final DestroyStatusTask task = new DestroyStatusTask(accountId, status_id);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyUserListAsync(final long accountId, final long listId) {
        final DestroyUserListTask task = new DestroyUserListTask(mContext, accountId, listId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyUserListSubscriptionAsync(final long accountId, final long listId) {
        final DestroyUserListSubscriptionTask task = new DestroyUserListSubscriptionTask(accountId, listId);
        return mAsyncTaskManager.add(task, true);
    }

    public Context getContext() {
        return mContext;
    }

    public boolean getHomeTimelineAsync(final long[] accountIds, final long[] maxIds, final long[] sinceIds) {
        return getHomeTimelineAsync(new BaseRefreshTaskParam(accountIds, maxIds, sinceIds));
    }

    public boolean getHomeTimelineAsync(RefreshTaskParam param) {
        final GetHomeTimelineTask task = new GetHomeTimelineTask(getContext());
        task.setParams(param);
        task.notifyStart();
        AsyncManager.runBackgroundTask(task);
        return true;
    }

    public int getLocalTrendsAsync(final long accountId, final int woeid) {
        final GetLocalTrendsTask task = new GetLocalTrendsTask(accountId, woeid);
        return mAsyncTaskManager.add(task, true);
    }

    public void getReceivedDirectMessagesAsync(final long[] accountIds, final long[] maxIds, final long[] sinceIds) {
        getActivitiesAboutMeAsync(new BaseRefreshTaskParam(accountIds, maxIds, sinceIds));
    }

    public void getReceivedDirectMessagesAsync(RefreshTaskParam param) {
        final GetReceivedDirectMessagesTask task = new GetReceivedDirectMessagesTask(mContext);
        task.setParams(param);
        task.notifyStart();
        AsyncManager.runBackgroundTask(task);
    }

    public void getSentDirectMessagesAsync(final long[] accountIds, final long[] maxIds, final long[] sinceIds) {
        final GetSentDirectMessagesTask task = new GetSentDirectMessagesTask(mContext);
        task.setParams(new BaseRefreshTaskParam(accountIds, maxIds, sinceIds));
        task.notifyStart();
        AsyncManager.runBackgroundTask(task);
    }

    public int getSavedSearchesAsync(long[] accountIds) {
        final GetSavedSearchesTask task = new GetSavedSearchesTask(mContext);
        task.setParams(accountIds);
        AsyncManager.runBackgroundTask(task);
        return System.identityHashCode(task);
    }

    @NonNull
    public long[] getSendingDraftIds() {
        return mSendingDraftIds.toArray();
    }

    public AsyncTaskManager getTaskManager() {
        return mAsyncTaskManager;
    }

    public boolean isCreatingFavorite(final long accountId, final long statusId) {
        return mCreatingFavoriteIds.has(accountId, statusId);
    }

    public boolean isCreatingFriendship(final long accountId, final long userId) {
        for (final ManagedAsyncTask<?, ?, ?> task : mAsyncTaskManager.getTaskSpecList()) {
            if (task instanceof CreateFriendshipTask) {
                final CreateFriendshipTask createFriendshipTask = (CreateFriendshipTask) task;
                if (createFriendshipTask.getStatus() == AsyncTask.Status.RUNNING
                        && createFriendshipTask.getAccountId() == accountId
                        && createFriendshipTask.getUserId() == userId)
                    return true;
            }
        }
        return false;
    }

    public boolean isCreatingRetweet(final long accountId, final long statusId) {
        return mCreatingRetweetIds.has(accountId, statusId);
    }

    public boolean isDestroyingFavorite(final long accountId, final long statusId) {
        return mDestroyingFavoriteIds.has(accountId, statusId);
    }

    public boolean isDestroyingFriendship(final long accountId, final long userId) {
        for (final ManagedAsyncTask<?, ?, ?> task : mAsyncTaskManager.getTaskSpecList()) {
            if (task instanceof DestroyFriendshipTask) {
                final DestroyFriendshipTask destroyFriendshipTask = (DestroyFriendshipTask) task;
                if (destroyFriendshipTask.getStatus() == AsyncTask.Status.RUNNING
                        && destroyFriendshipTask.getAccountId() == accountId
                        && destroyFriendshipTask.getUserId() == userId)
                    return true;
            }
        }
        return false;
    }

    public boolean isDestroyingStatus(final long accountId, final long statusId) {
        return mDestroyingStatusIds.has(accountId, statusId);
    }

    public boolean isHomeTimelineRefreshing() {
        return mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_GET_HOME_TIMELINE);
    }

    public boolean isLocalTrendsRefreshing() {
        return mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_GET_TRENDS)
                || mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_STORE_TRENDS);
    }

    public boolean isReceivedDirectMessagesRefreshing() {
        return mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_GET_RECEIVED_DIRECT_MESSAGES);
    }

    public boolean isSentDirectMessagesRefreshing() {
        return mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_GET_SENT_DIRECT_MESSAGES);
    }

    @Deprecated
    public void refreshAll() {
        refreshAll(DataStoreUtils.getActivatedAccountIds(mContext));
    }

    public boolean refreshAll(final long[] accountIds) {
        AsyncTaskUtils.executeTask(new AsyncTask<long[], Object, Object[]>() {
            @Override
            protected Object[] doInBackground(long[][] params) {
                final Object[] result = new Object[8];
                result[0] = DataStoreUtils.getNewestStatusIds(mContext, Statuses.CONTENT_URI, accountIds);
                if (Boolean.TRUE.equals(result[1] = mPreferences.getBoolean(KEY_HOME_REFRESH_MENTIONS))) {
                    result[2] = DataStoreUtils.getNewestActivityMaxPositions(mContext,
                            Activities.AboutMe.CONTENT_URI, accountIds);
                }
                if (Boolean.TRUE.equals(result[3] = mPreferences.getBoolean(KEY_HOME_REFRESH_DIRECT_MESSAGES))) {
                    result[4] = DataStoreUtils.getNewestMessageIds(mContext, DirectMessages.Inbox.CONTENT_URI, accountIds);
                }
                if (Boolean.TRUE.equals(result[5] = mPreferences.getBoolean(KEY_HOME_REFRESH_TRENDS))) {
                    result[6] = Utils.getDefaultAccountId(mContext);
                    result[7] = mPreferences.getInt(KEY_LOCAL_TRENDS_WOEID, 1);
                }
                return result;
            }

            @Override
            protected void onPostExecute(Object[] result) {
                getHomeTimelineAsync(accountIds, null, (long[]) result[0]);
                if (Boolean.TRUE.equals(result[1])) {
                    getActivitiesAboutMeAsync(accountIds, null, (long[]) result[2]);
                }
                if (Boolean.TRUE.equals(result[3])) {
                    getReceivedDirectMessagesAsync(accountIds, null, (long[]) result[4]);
                    getSentDirectMessagesAsync(accountIds, null, null);
                }
                if (Boolean.TRUE.equals(result[5])) {
                    getLocalTrendsAsync((Long) result[6], (Integer) result[7]);
                }
                getSavedSearchesAsync(accountIds);
            }
        }, accountIds);
        return true;
    }

    public void removeSendingDraftId(long id) {
        synchronized (mSendingDraftIds) {
            mSendingDraftIds.removeElement(id);
            mResolver.notifyChange(Drafts.CONTENT_URI_UNSENT, null);
        }
    }

    public void removeUnreadCountsAsync(final int position, final LongSparseArray<Set<Long>> counts) {
        final RemoveUnreadCountsTask task = new RemoveUnreadCountsTask(position, counts);
        AsyncTaskUtils.executeTask(task);
    }

    public int reportMultiSpam(final long accountId, final long[] user_ids) {
        final ReportMultiSpamTask task = new ReportMultiSpamTask(accountId, user_ids);
        return mAsyncTaskManager.add(task, true);
    }

    public int reportSpamAsync(final long accountId, final long user_id) {
        final ReportSpamTask task = new ReportSpamTask(accountId, user_id);
        return mAsyncTaskManager.add(task, true);
    }

    public int retweetStatusAsync(final long accountId, final long status_id) {
        final RetweetStatusTask task = new RetweetStatusTask(accountId, status_id);
        return mAsyncTaskManager.add(task, true);
    }

    public int sendDirectMessageAsync(final long accountId, final long recipientId, final String text,
                                      final String imageUri) {
        final Intent intent = new Intent(mContext, BackgroundOperationService.class);
        intent.setAction(INTENT_ACTION_SEND_DIRECT_MESSAGE);
        intent.putExtra(EXTRA_ACCOUNT_ID, accountId);
        intent.putExtra(EXTRA_RECIPIENT_ID, recipientId);
        intent.putExtra(EXTRA_TEXT, text);
        intent.putExtra(EXTRA_IMAGE_URI, imageUri);
        mContext.startService(intent);
        return 0;
    }

    public int updateStatusAsync(final long[] accountIds, final String text, final ParcelableLocation location,
                                 final ParcelableMediaUpdate[] media, final long inReplyToStatusId,
                                 final boolean isPossiblySensitive) {
        final ParcelableStatusUpdate.Builder builder = new ParcelableStatusUpdate.Builder();
        builder.accounts(DataStoreUtils.getAccounts(mContext, accountIds));
        builder.text(text);
        builder.location(location);
        builder.media(media);
        builder.inReplyToStatusId(inReplyToStatusId);
        builder.isPossiblySensitive(isPossiblySensitive);
        return updateStatusesAsync(builder.build());
    }

    public int updateStatusesAsync(final ParcelableStatusUpdate... statuses) {
        final Intent intent = new Intent(mContext, BackgroundOperationService.class);
        intent.setAction(INTENT_ACTION_UPDATE_STATUS);
        intent.putExtra(EXTRA_STATUSES, statuses);
        mContext.startService(intent);
        return 0;
    }

    public int updateUserListDetails(final long accountId, final long listId, final UserListUpdate update) {
        final UpdateUserListDetailsTask task = new UpdateUserListDetailsTask(mContext, accountId, listId, update);
        return mAsyncTaskManager.add(task, true);
    }

    public static <T extends Response<?>> Exception getException(List<T> responses) {
        for (T response : responses) {
            if (response.hasException()) return response.getException();
        }
        return null;
    }

    public BackgroundTask updateFriendship(final long accountId, final long userId, final FriendshipUpdate update) {
        final Bus bus = mBus;
        if (bus == null) return null;
        return AsyncManager.runBackgroundTask(new TaskRunnable<Object, SingleResponse<Relationship>, Bus>() {
            @Override
            public SingleResponse<Relationship> doLongOperation(Object param) throws InterruptedException {
                final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountId, true);
                try {
                    return SingleResponse.getInstance(twitter.updateFriendship(userId, update));
                } catch (TwitterException e) {
                    return SingleResponse.getInstance(e);
                }
            }

            @Override
            public void callback(Bus handler, SingleResponse<Relationship> result) {
                if (result.hasData()) {
                    handler.post(new FriendshipUpdatedEvent(accountId, userId, result.getData()));
                } else if (result.hasException()) {
                    if (BuildConfig.DEBUG) {
                        Log.w(LOGTAG, "Unable to update friendship", result.getException());
                    }
                }
            }
        }.setResultHandler(bus));
    }

    public void getActivitiesAboutMeAsync(final long[] accountIds, long[] maxIds, long[] sinceIds) {
        getActivitiesAboutMeAsync(new BaseRefreshTaskParam(accountIds, maxIds, sinceIds));
    }

    public void getActivitiesAboutMeAsync(final RefreshTaskParam param) {
        final GetActivitiesTask task = new GetActivitiesAboutMeTask(getContext());
        task.setParams(param);
        task.notifyStart();
        AsyncManager.runBackgroundTask(task);
    }

    public void getActivitiesByFriendsAsync(long[] accountIds, long[] maxIds, long[] sinceIds) {
        final GetActivitiesTask task = new GetActivitiesByFriendsTask(getContext());
        task.setParams(new BaseRefreshTaskParam(accountIds, maxIds, sinceIds));
        task.notifyStart();
        AsyncManager.runBackgroundTask(task);
    }

    public void setActivitiesAboutMeUnreadAsync(final long[] accountIds, final long cursor) {
        TaskRunnable<Object, Object, AsyncTwitterWrapper> task = new TaskRunnable<Object, Object, AsyncTwitterWrapper>() {

            @Override
            public Object doLongOperation(Object o) throws InterruptedException {
                for (long accountId : accountIds) {
                    Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountId, false);
                    if (TwitterAPIFactory.isOfficialTwitterInstance(mContext, twitter)) continue;
                    try {
                        twitter.setActivitiesAboutMeUnread(cursor);
                    } catch (TwitterException e) {
                        if (BuildConfig.DEBUG) {
                            Log.w(LOGTAG, e);
                        }
                    }
                }
                return null;
            }
        };
        AsyncManager.runBackgroundTask(task);
    }

    public ErrorInfoStore getErrorInfoStore() {
        return mErrorInfoStore;
    }

    private void addProcessingFriendshipRequestId(long accountId, long userId) {
        mProcessingFriendshipRequestIds.add(ParcelableUser.calculateHashCode(accountId, userId));
    }

    private void removeProcessingFriendshipRequestId(long accountId, long userId) {
        mProcessingFriendshipRequestIds.removeElement(ParcelableUser.calculateHashCode(accountId, userId));
    }

    public boolean isProcessingFollowRequest(long accountId, long userId) {
        return mProcessingFriendshipRequestIds.contains(ParcelableUser.calculateHashCode(accountId, userId));
    }

    public static class UpdateProfileBannerImageTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final long mAccountId;
        private final Uri mImageUri;
        private final boolean mDeleteImage;
        private final Context mContext;

        public UpdateProfileBannerImageTask(final Context context, final AsyncTaskManager manager,
                                            final long account_id, final Uri image_uri, final boolean delete_image) {
            super(context);
            mContext = context;
            mAccountId = account_id;
            mImageUri = image_uri;
            mDeleteImage = delete_image;
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
            super.onPostExecute(result);
            if (result.hasData()) {
                Utils.showOkMessage(mContext, R.string.profile_banner_image_updated, false);


                bus.post(new ProfileUpdatedEvent(result.getData()));
            } else {
                Utils.showErrorMessage(mContext, R.string.action_updating_profile_banner_image, result.getException(),
                        true);
            }
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {
            try {
                final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, true);
                TwitterWrapper.updateProfileBannerImage(mContext, twitter, mImageUri, mDeleteImage);
                // Wait for 5 seconds, see
                // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    Log.w(LOGTAG, e);
                }
                final User user = TwitterWrapper.tryShowUser(twitter, mAccountId, null);
                return SingleResponse.getInstance(new ParcelableUser(user, mAccountId));
            } catch (TwitterException | FileNotFoundException e) {
                return SingleResponse.getInstance(e);
            }
        }


    }

    public static class UpdateProfileImageTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final long mAccountId;
        private final Uri mImageUri;
        private final boolean mDeleteImage;
        private final Context mContext;

        public UpdateProfileImageTask(final Context context, final long account_id,
                                      final Uri image_uri, final boolean delete_image) {
            super(context);
            this.mContext = context;
            this.mAccountId = account_id;
            this.mImageUri = image_uri;
            this.mDeleteImage = delete_image;
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {
            try {
                final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, true);
                TwitterWrapper.updateProfileImage(mContext, twitter, mImageUri, mDeleteImage);
                // Wait for 5 seconds, see
                // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    Log.w(LOGTAG, e);
                }
                final User user = TwitterWrapper.tryShowUser(twitter, mAccountId, null);
                return SingleResponse.getInstance(new ParcelableUser(user, mAccountId));
            } catch (TwitterException | FileNotFoundException e) {
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
            super.onPostExecute(result);
            if (result.hasData()) {
                Utils.showOkMessage(mContext, R.string.profile_image_updated, false);
                bus.post(new ProfileUpdatedEvent(result.getData()));
            } else {
                Utils.showErrorMessage(mContext, R.string.action_updating_profile_image, result.getException(), true);
            }
        }

    }

    static class AcceptFriendshipTask extends ManagedAsyncTask<Object, Object, SingleResponse<User>> {

        private final long mAccountId;
        private final long mUserId;

        public AcceptFriendshipTask(final Context context, final long accountId, final long userId) {
            super(context);
            mAccountId = accountId;
            mUserId = userId;
        }

        public long getAccountId() {
            return mAccountId;
        }

        public long getUserId() {
            return mUserId;
        }

        @Override
        protected void onPreExecute() {
            final FollowRequestTaskEvent event = new FollowRequestTaskEvent(FollowRequestTaskEvent.Action.ACCEPT,
                    mAccountId, mUserId);
            event.setFinished(false);
            bus.post(event);
            mAsyncTwitterWrapper.addProcessingFriendshipRequestId(mAccountId, mUserId);
            super.onPreExecute();
        }


        @Override
        protected SingleResponse<User> doInBackground(final Object... params) {

            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(getContext(), mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final User user = twitter.acceptFriendship(mUserId);
                return SingleResponse.getInstance(user, null);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(null, e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<User> result) {
            final FollowRequestTaskEvent event = new FollowRequestTaskEvent(FollowRequestTaskEvent.Action.ACCEPT,
                    mAccountId, mUserId);
            event.setFinished(true);
            if (result.hasData()) {
                final User user = result.getData();
                final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
                final String message = getContext().getString(R.string.accepted_users_follow_request,
                        mUserColorNameManager.getDisplayName(user, nameFirst, true));
                Utils.showOkMessage(getContext(), message, false);
                event.setSucceeded(true);
            } else {
                Utils.showErrorMessage(getContext(), R.string.action_accepting_follow_request,
                        result.getException(), false);
                event.setSucceeded(false);
            }
            mAsyncTwitterWrapper.removeProcessingFriendshipRequestId(mAccountId, mUserId);
            bus.post(event);
            super.onPostExecute(result);
        }

    }

    class AddUserListMembersTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUserList>> {

        private final long accountId;
        private final long listId;
        private final ParcelableUser[] users;

        public AddUserListMembersTask(final long accountId, final long listId, final ParcelableUser[] users) {
            super(mContext);
            this.accountId = accountId;
            this.listId = listId;
            this.users = users;
        }

        @Override
        protected SingleResponse<ParcelableUserList> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountId, false);
            if (twitter == null || users == null) return SingleResponse.getInstance();
            try {
                final long[] userIds = new long[users.length];
                for (int i = 0, j = users.length; i < j; i++) {
                    userIds[i] = users[i].id;
                }
                final ParcelableUserList list = new ParcelableUserList(twitter.addUserListMembers(listId, userIds),
                        accountId);
                return SingleResponse.getInstance(list, null);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(null, e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
            final boolean succeed = result.hasData() && result.getData().id > 0;
            if (succeed) {
                final String message;
                if (users.length == 1) {
                    final ParcelableUser user = users[0];
                    final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
                    final String displayName = mUserColorNameManager.getDisplayName(user.id, user.name,
                            user.screen_name, nameFirst, false);
                    message = mContext.getString(R.string.added_user_to_list, displayName, result.getData().name);
                } else {
                    final Resources res = mContext.getResources();
                    message = res.getQuantityString(R.plurals.added_N_users_to_list, users.length, users.length,
                            result.getData().name);
                }
                Utils.showOkMessage(mContext, message, false);
            } else {
                Utils.showErrorMessage(mContext, R.string.action_adding_member, result.getException(), true);
            }
            final Intent intent = new Intent(BROADCAST_USER_LIST_MEMBERS_ADDED);
            intent.putExtra(EXTRA_USER_LIST, result.getData());
            intent.putExtra(EXTRA_USERS, users);
            mContext.sendBroadcast(intent);
            super.onPostExecute(result);
        }

    }

    final class ClearNotificationTask extends AsyncTask<Object, Object, Integer> {
        private final int notificationType;
        private final long accountId;

        ClearNotificationTask(final int notificationType, final long accountId) {
            this.notificationType = notificationType;
            this.accountId = accountId;
        }

        @Override
        protected Integer doInBackground(final Object... params) {
            return clearNotification(mContext, notificationType, accountId);
        }

    }

    final class ClearUnreadCountTask extends AsyncTask<Object, Object, Integer> {
        private final int position;

        ClearUnreadCountTask(final int position) {
            this.position = position;
        }

        @Override
        protected Integer doInBackground(final Object... params) {
            return clearUnreadCount(mContext, position);
        }

    }

    class CreateBlockTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final long account_id, user_id;

        public CreateBlockTask(final long account_id, final long user_id) {
            super(mContext);
            this.account_id = account_id;
            this.user_id = user_id;
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, account_id, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final User user = twitter.createBlock(user_id);
                Utils.setLastSeen(mContext, user.getId(), -1);
                for (final Uri uri : TwidereDataStore.STATUSES_URIS) {
                    final Expression where = Expression.and(Expression.equals(Statuses.ACCOUNT_ID, account_id),
                            Expression.equals(Statuses.USER_ID, user_id));
                    mResolver.delete(uri, where.getSQL(), null);

                }
                // I bet you don't want to see this user in your auto complete list.
                final ContentValues values = new ContentValues();
                values.put(CachedRelationships.ACCOUNT_ID, account_id);
                values.put(CachedRelationships.USER_ID, user_id);
                values.put(CachedRelationships.BLOCKING, true);
                mResolver.insert(CachedRelationships.CONTENT_URI, values);
                return SingleResponse.getInstance(new ParcelableUser(user, account_id), null);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(null, e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
            if (result.hasData()) {
                final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
                final String message = mContext.getString(R.string.blocked_user,
                        mUserColorNameManager.getDisplayName(result.getData(), nameFirst, true));
                Utils.showInfoMessage(mContext, message, false);


                bus.post(new FriendshipUserUpdatedEvent(result.getData()));
            } else {
                Utils.showErrorMessage(mContext, R.string.action_blocking, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    class CreateFavoriteTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableStatus>> {

        private final long account_id, status_id;

        public CreateFavoriteTask(final long account_id, final long status_id) {
            super(mContext);
            this.account_id = account_id;
            this.status_id = status_id;
        }

        @Override
        protected SingleResponse<ParcelableStatus> doInBackground(final Object... params) {
            if (account_id < 0) return SingleResponse.getInstance();
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, account_id, true);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final ParcelableStatus status = ParcelableStatusUtils.fromStatus(twitter.createFavorite(status_id), account_id, false);
                Utils.setLastSeen(mContext, status.mentions, System.currentTimeMillis());
                final ContentValues values = new ContentValues();
                values.put(Statuses.IS_FAVORITE, true);
                values.put(Statuses.FAVORITE_COUNT, status.favorite_count);
                final Expression where = Expression.and(Expression.equals(Statuses.ACCOUNT_ID, account_id),
                        Expression.or(Expression.equals(Statuses.STATUS_ID, status_id),
                                Expression.equals(Statuses.RETWEET_ID, status_id)));
                for (final Uri uri : TwidereDataStore.STATUSES_URIS) {
                    mResolver.update(uri, values, where.getSQL(), null);
                }
                return SingleResponse.getInstance(status);
            } catch (final TwitterException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, e);
                }
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCreatingFavoriteIds.put(account_id, status_id);


            bus.post(new StatusListChangedEvent());
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableStatus> result) {
            mCreatingFavoriteIds.remove(account_id, status_id);
            if (result.hasData()) {
                final ParcelableStatus status = result.getData();

                // BEGIN HotMobi

                final TweetEvent event = TweetEvent.create(getContext(), status, TimelineType.OTHER);
                event.setAction(TweetEvent.Action.FAVORITE);
                HotMobiLogger.getInstance(getContext()).log(account_id, event);

                // END HotMobi


                bus.post(new FavoriteCreatedEvent(status));
                Utils.showOkMessage(mContext, R.string.status_favorited, false);
            } else {
                Utils.showErrorMessage(mContext, R.string.action_favoriting, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    class CreateFriendshipTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final long mAccountId;
        private final long user_id;

        public CreateFriendshipTask(final long accountId, final long user_id) {
            super(mContext);
            this.mAccountId = accountId;
            this.user_id = user_id;
        }

        public long getAccountId() {
            return mAccountId;
        }

        public long getUserId() {
            return user_id;
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {

            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final User user = twitter.createFriendship(user_id);
                Utils.setLastSeen(mContext, user.getId(), System.currentTimeMillis());
                return SingleResponse.getInstance(new ParcelableUser(user, mAccountId), null);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(null, e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
            if (result.hasData()) {
                final ParcelableUser user = result.getData();
                final String message;
                final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
                if (user.is_protected) {
                    message = mContext.getString(R.string.sent_follow_request_to_user,
                            mUserColorNameManager.getDisplayName(user, nameFirst, true));
                } else {
                    message = mContext.getString(R.string.followed_user,
                            mUserColorNameManager.getDisplayName(user, nameFirst, true));
                }
                Utils.showOkMessage(mContext, message, false);


                bus.post(new FriendshipUserUpdatedEvent(result.getData()));
            } else {
                Utils.showErrorMessage(mContext, R.string.action_following, result.getException(), false);
            }
            super.onPostExecute(result);
        }

    }

    class CreateMultiBlockTask extends ManagedAsyncTask<Object, Object, ListResponse<Long>> {

        private final long account_id;
        private final long[] user_ids;

        public CreateMultiBlockTask(final long account_id, final long[] user_ids) {
            super(mContext);
            this.account_id = account_id;
            this.user_ids = user_ids;
        }

        private void deleteCaches(final List<Long> list) {
            for (final Uri uri : TwidereDataStore.STATUSES_URIS) {
                ContentResolverUtils.bulkDelete(mResolver, uri, Statuses.USER_ID, list, Statuses.ACCOUNT_ID + " = " + account_id, false);
            }
            // I bet you don't want to see these users in your auto complete list.
            //TODO insert to blocked users data
            final ContentValues values = new ContentValues();
            values.put(CachedRelationships.BLOCKING, true);
            values.put(CachedRelationships.FOLLOWING, false);
            values.put(CachedRelationships.FOLLOWED_BY, false);
            mResolver.update(CachedRelationships.CONTENT_URI, values,
                    Expression.inArgs(CachedRelationships.USER_ID, list.size()).getSQL(),
                    TwidereListUtils.toStringArray(list));
        }

        @Override
        protected ListResponse<Long> doInBackground(final Object... params) {
            final List<Long> blocked_users = new ArrayList<>();
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, account_id, false);
            if (twitter != null) {
                for (final long user_id : user_ids) {
                    try {
                        final User user = twitter.createBlock(user_id);
                        if (user == null || user.getId() <= 0) {
                            continue;
                        }
                        blocked_users.add(user.getId());
                    } catch (final TwitterException e) {
                        deleteCaches(blocked_users);
                        return ListResponse.getListInstance(e);
                    }
                }
            }
            deleteCaches(blocked_users);
            return ListResponse.getListInstance(blocked_users);
        }

        @Override
        protected void onPostExecute(final ListResponse<Long> result) {
            if (result.hasData()) {
                Utils.showInfoMessage(mContext, R.string.users_blocked, false);
            } else {
                Utils.showErrorMessage(mContext, R.string.action_blocking, result.getException(), true);
            }
            final Intent intent = new Intent(BROADCAST_MULTI_BLOCKSTATE_CHANGED);
            intent.putExtra(EXTRA_USER_ID, user_ids);
            mContext.sendBroadcast(intent);
            super.onPostExecute(result);
        }


    }

    class CreateMuteTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final long mAccountId, mUserId;

        public CreateMuteTask(final long accountId, final long userId) {
            super(mContext);
            this.mAccountId = accountId;
            this.mUserId = userId;
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final User user = twitter.createMute(mUserId);
                Utils.setLastSeen(mContext, user.getId(), -1);
                final Expression where = Expression.and(Expression.equals(Statuses.ACCOUNT_ID, mAccountId),
                        Expression.equals(Statuses.USER_ID, mUserId));
                mResolver.delete(Statuses.CONTENT_URI, where.getSQL(), null);

                return SingleResponse.getInstance(new ParcelableUser(user, mAccountId), null);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(null, e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
            if (result.hasData()) {
                final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
                final String message = mContext.getString(R.string.muted_user,
                        mUserColorNameManager.getDisplayName(result.getData(), nameFirst, true));
                Utils.showInfoMessage(mContext, message, false);


                bus.post(new FriendshipUserUpdatedEvent(result.getData()));
            } else {
                Utils.showErrorMessage(mContext, R.string.action_muting, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    class CreateSavedSearchTask extends ManagedAsyncTask<Object, Object, SingleResponse<SavedSearch>> {

        private final long mAccountId;
        private final String mQuery;

        CreateSavedSearchTask(final long accountId, final String query) {
            super(mContext);
            mAccountId = accountId;
            mQuery = query;
        }

        @Override
        protected SingleResponse<SavedSearch> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter == null) return null;
            try {
                return SingleResponse.getInstance(twitter.createSavedSearch(mQuery));
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<SavedSearch> result) {
            if (result.hasData()) {
                final String message = mContext.getString(R.string.search_name_saved, result.getData().getQuery());
                Utils.showOkMessage(mContext, message, false);
            } else if (result.hasException()) {
                final Exception exception = result.getException();
                // https://github.com/TwidereProject/Twidere-Android/issues/244
                if (exception instanceof TwitterException && ((TwitterException) exception).getStatusCode() == 403) {
                    final String desc = mContext.getString(R.string.saved_searches_already_saved_hint);
                    Utils.showErrorMessage(mContext, R.string.action_saving_search, desc, false);
                } else {
                    Utils.showErrorMessage(mContext, R.string.action_saving_search, exception, false);
                }
            }
            super.onPostExecute(result);
        }

    }

    class CreateUserListSubscriptionTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUserList>> {

        private final long accountId;
        private final long listId;

        public CreateUserListSubscriptionTask(final long accountId, final long listId) {
            super(mContext);
            this.accountId = accountId;
            this.listId = listId;
        }

        @Override
        protected SingleResponse<ParcelableUserList> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountId, false);
            if (twitter == null) return SingleResponse.getInstance();

            try {
                final ParcelableUserList list = new ParcelableUserList(twitter.createUserListSubscription(listId),
                        accountId);
                return SingleResponse.getInstance(list);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
            final boolean succeed = result.hasData();
            if (succeed) {
                final String message = mContext.getString(R.string.subscribed_to_list, result.getData().name);
                Utils.showOkMessage(mContext, message, false);
                final Intent intent = new Intent(BROADCAST_USER_LIST_SUBSCRIBED);
                intent.putExtra(EXTRA_USER_LIST, result.getData());
                mContext.sendBroadcast(intent);
            } else {
                Utils.showErrorMessage(mContext, R.string.action_subscribing_to_list, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    static class CreateUserListTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUserList>> {

        private final long accountId;
        private final String listName, description;
        private final boolean isPublic;

        public CreateUserListTask(Context context, final long accountId, final String listName, final boolean isPublic,
                                  final String description) {
            super(context);
            this.accountId = accountId;
            this.listName = listName;
            this.description = description;
            this.isPublic = isPublic;
        }

        @Override
        protected SingleResponse<ParcelableUserList> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(getContext(), accountId, false);
            if (twitter == null || listName == null) return SingleResponse.getInstance();
            try {
                final UserListUpdate userListUpdate = new UserListUpdate();
                userListUpdate.setName(listName);
                userListUpdate.setMode(isPublic ? UserList.Mode.PUBLIC : UserList.Mode.PRIVATE);
                userListUpdate.setDescription(description);
                final UserList list = twitter.createUserList(userListUpdate);
                return SingleResponse.getInstance(new ParcelableUserList(list, accountId), null);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(null, e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
            final Context context = getContext();
            if (result.hasData()) {
                final ParcelableUserList userList = result.getData();
                final String message = context.getString(R.string.created_list, userList.name);
                Utils.showOkMessage(context, message, false);
                bus.post(new UserListCreatedEvent(userList));
            } else {
                Utils.showErrorMessage(context, R.string.action_creating_list, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    class DeleteUserListMembersTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUserList>> {

        private final long mAccountId;
        private final long mUserListId;
        private final ParcelableUser[] users;

        public DeleteUserListMembersTask(final long accountId, final long userListId, final ParcelableUser[] users) {
            super(mContext);
            mAccountId = accountId;
            mUserListId = userListId;
            this.users = users;
        }

        @Override
        protected SingleResponse<ParcelableUserList> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final long[] userIds = new long[users.length];
                for (int i = 0, j = users.length; i < j; i++) {
                    userIds[i] = users[i].id;
                }
                final ParcelableUserList list = new ParcelableUserList(twitter.deleteUserListMembers(mUserListId,
                        userIds), mAccountId);
                return SingleResponse.getInstance(list, null);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(null, e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
            final boolean succeed = result.hasData() && result.getData().id > 0;
            final String message;
            if (succeed) {
                if (users.length == 1) {
                    final ParcelableUser user = users[0];
                    final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
                    final String displayName = mUserColorNameManager.getDisplayName(user.id,
                            user.name, user.screen_name, nameFirst, false);
                    message = mContext.getString(R.string.deleted_user_from_list, displayName,
                            result.getData().name);
                } else {
                    final Resources res = mContext.getResources();
                    message = res.getQuantityString(R.plurals.deleted_N_users_from_list, users.length, users.length,
                            result.getData().name);
                }
                Utils.showInfoMessage(mContext, message, false);
                final Intent intent = new Intent(BROADCAST_USER_LIST_MEMBERS_DELETED);
                intent.putExtra(EXTRA_USER_LIST, result.getData());
                intent.putExtra(EXTRA_USERS, users);
                mContext.sendBroadcast(intent);
            } else {
                Utils.showErrorMessage(mContext, R.string.action_deleting, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    class DenyFriendshipTask extends ManagedAsyncTask<Object, Object, SingleResponse<User>> {

        private final long mAccountId;
        private final long mUserId;

        public DenyFriendshipTask(final long accountId, final long userId) {
            super(mContext);
            mAccountId = accountId;
            mUserId = userId;
        }

        public long getAccountId() {
            return mAccountId;
        }

        public long getUserId() {
            return mUserId;
        }

        @Override
        protected SingleResponse<User> doInBackground(final Object... params) {

            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final User user = twitter.denyFriendship(mUserId);
                return SingleResponse.getInstance(user, null);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(null, e);
            }
        }

        @Override
        protected void onPreExecute() {
            addProcessingFriendshipRequestId(mAccountId, mUserId);
            final FollowRequestTaskEvent event = new FollowRequestTaskEvent(FollowRequestTaskEvent.Action.ACCEPT,
                    mAccountId, mUserId);
            event.setFinished(false);
            bus.post(event);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final SingleResponse<User> result) {
            final FollowRequestTaskEvent event = new FollowRequestTaskEvent(FollowRequestTaskEvent.Action.ACCEPT,
                    mAccountId, mUserId);
            event.setFinished(true);
            if (result.hasData()) {
                final User user = result.getData();
                final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
                final String message = mContext.getString(R.string.denied_users_follow_request,
                        mUserColorNameManager.getDisplayName(user, nameFirst, true));
                Utils.showOkMessage(mContext, message, false);
                event.setSucceeded(true);
            } else {
                Utils.showErrorMessage(mContext, R.string.action_denying_follow_request, result.getException(), false);
                event.setSucceeded(false);
            }
            super.onPostExecute(result);
            removeProcessingFriendshipRequestId(mAccountId, mUserId);
            bus.post(event);
        }

    }

    class DestroyBlockTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final long mAccountId;
        private final long mUserId;

        public DestroyBlockTask(final long accountId, final long userId) {
            super(mContext);
            mAccountId = accountId;
            mUserId = userId;
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final User user = twitter.destroyBlock(mUserId);
                Utils.setLastSeen(mContext, user.getId(), -1);
                return SingleResponse.getInstance(new ParcelableUser(user, mAccountId), null);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(null, e);
            }

        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
            if (result.hasData()) {
                final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
                final String message = mContext.getString(R.string.unblocked_user,
                        mUserColorNameManager.getDisplayName(result.getData(), nameFirst, true));
                Utils.showInfoMessage(mContext, message, false);


                bus.post(new FriendshipUserUpdatedEvent(result.getData()));
            } else {
                Utils.showErrorMessage(mContext, R.string.action_unblocking, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    class DestroyDirectMessageTask extends ManagedAsyncTask<Object, Object, SingleResponse<DirectMessage>> {

        private final long message_id;
        private final long account_id;

        public DestroyDirectMessageTask(final long account_id, final long message_id) {
            super(mContext);

            this.account_id = account_id;
            this.message_id = message_id;
        }

        private void deleteMessages(final long message_id) {
            final String where = DirectMessages.MESSAGE_ID + " = " + message_id;
            mResolver.delete(DirectMessages.Inbox.CONTENT_URI, where, null);
            mResolver.delete(DirectMessages.Outbox.CONTENT_URI, where, null);
        }

        private boolean isMessageNotFound(final Exception e) {
            if (!(e instanceof TwitterException)) return false;
            final TwitterException te = (TwitterException) e;
            return te.getErrorCode() == ErrorInfo.PAGE_NOT_FOUND
                    || te.getStatusCode() == HttpResponseCode.NOT_FOUND;
        }

        @Override
        protected SingleResponse<DirectMessage> doInBackground(final Object... args) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, account_id, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final DirectMessage message = twitter.destroyDirectMessage(message_id);
                deleteMessages(message_id);
                return SingleResponse.getInstance(message, null);
            } catch (final TwitterException e) {
                if (isMessageNotFound(e)) {
                    deleteMessages(message_id);
                }
                return SingleResponse.getInstance(null, e);
            }
        }


        @Override
        protected void onPostExecute(final SingleResponse<DirectMessage> result) {
            super.onPostExecute(result);
            if (result == null) return;
            if (result.hasData() || isMessageNotFound(result.getException())) {
                Utils.showInfoMessage(mContext, R.string.direct_message_deleted, false);
            } else {
                Utils.showErrorMessage(mContext, R.string.action_deleting, result.getException(), true);
            }
        }


    }


    class DestroyMessageConversationTask extends ManagedAsyncTask<Object, Object, SingleResponse<Void>> {

        private final long userId;
        private final long accountId;

        public DestroyMessageConversationTask(final long accountId, final long userId) {
            super(mContext);

            this.accountId = accountId;
            this.userId = userId;
        }

        private void deleteMessages(final long accountId, final long userId) {
            mResolver.delete(DirectMessages.Inbox.CONTENT_URI, Expression.and(Expression.equals(Inbox.ACCOUNT_ID, accountId),
                    Expression.equals(Inbox.SENDER_ID, userId)).getSQL(), null);
            mResolver.delete(DirectMessages.Outbox.CONTENT_URI, Expression.and(Expression.equals(Outbox.ACCOUNT_ID, accountId),
                    Expression.equals(Outbox.RECIPIENT_ID, userId)).getSQL(), null);
        }

        private boolean isMessageNotFound(final Exception e) {
            if (!(e instanceof TwitterException)) return false;
            final TwitterException te = (TwitterException) e;
            return te.getErrorCode() == ErrorInfo.PAGE_NOT_FOUND
                    || te.getStatusCode() == HttpResponseCode.NOT_FOUND;
        }

        @Override
        protected SingleResponse<Void> doInBackground(final Object... args) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                twitter.destroyDirectMessagesConversation(accountId, userId);
                deleteMessages(accountId, userId);
                return SingleResponse.getInstance();
            } catch (final TwitterException e) {
                if (isMessageNotFound(e)) {
                    deleteMessages(accountId, userId);
                }
                return SingleResponse.getInstance(e);
            }
        }


        @Override
        protected void onPostExecute(final SingleResponse<Void> result) {
            super.onPostExecute(result);
            if (result == null) return;
            if (result.hasData() || isMessageNotFound(result.getException())) {
                Utils.showInfoMessage(mContext, R.string.direct_message_deleted, false);
            } else {
                Utils.showErrorMessage(mContext, R.string.action_deleting, result.getException(), true);
            }
        }


    }


    class DestroyFavoriteTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableStatus>> {

        private final long account_id;

        private final long status_id;

        public DestroyFavoriteTask(final long account_id, final long status_id) {
            super(mContext);
            this.account_id = account_id;
            this.status_id = status_id;
        }

        @Override
        protected SingleResponse<ParcelableStatus> doInBackground(final Object... params) {
            if (account_id < 0) return SingleResponse.getInstance();
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, account_id, true);
            if (twitter != null) {
                try {
                    final ParcelableStatus status = ParcelableStatusUtils.fromStatus(twitter.destroyFavorite(status_id), account_id, false);
                    final ContentValues values = new ContentValues();
                    values.put(Statuses.IS_FAVORITE, false);
                    values.put(Statuses.FAVORITE_COUNT, status.favorite_count - 1);
                    final Expression where = Expression.and(Expression.equals(Statuses.ACCOUNT_ID, account_id),
                            Expression.or(Expression.equals(Statuses.STATUS_ID, status_id),
                                    Expression.equals(Statuses.RETWEET_ID, status_id)));
                    for (final Uri uri : TwidereDataStore.STATUSES_URIS) {
                        mResolver.update(uri, values, where.getSQL(), null);
                    }
                    return SingleResponse.getInstance(status);
                } catch (final TwitterException e) {
                    return SingleResponse.getInstance(e);
                }
            }
            return SingleResponse.getInstance();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDestroyingFavoriteIds.put(account_id, status_id);


            bus.post(new StatusListChangedEvent());
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableStatus> result) {
            mDestroyingFavoriteIds.remove(account_id, status_id);
            if (result.hasData()) {
                final ParcelableStatus status = result.getData();
                // BEGIN HotMobi

                final TweetEvent event = TweetEvent.create(getContext(), status, TimelineType.OTHER);
                event.setAction(TweetEvent.Action.UNFAVORITE);
                HotMobiLogger.getInstance(getContext()).log(account_id, event);

                // END HotMobi
                bus.post(new FavoriteDestroyedEvent(status));
                Utils.showInfoMessage(mContext, R.string.status_unfavorited, false);
            } else {
                Utils.showErrorMessage(mContext, R.string.action_unfavoriting, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    class DestroyFriendshipTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final long mAccountId;
        private final long user_id;

        public DestroyFriendshipTask(final long accountId, final long user_id) {
            super(mContext);
            mAccountId = accountId;
            this.user_id = user_id;
        }

        public long getAccountId() {
            return mAccountId;
        }

        public long getUserId() {
            return user_id;
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {

            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter != null) {
                try {
                    final User user = twitter.destroyFriendship(user_id);
                    // remove user tweets and retweets
                    Utils.setLastSeen(mContext, user.getId(), -1);
                    final Expression where = Expression.and(Expression.equals(Statuses.ACCOUNT_ID, mAccountId),
                            Expression.or(Expression.equals(Statuses.USER_ID, user_id),
                                    Expression.equals(Statuses.RETWEETED_BY_USER_ID, user_id)));
                    mResolver.delete(Statuses.CONTENT_URI, where.getSQL(), null);
                    return SingleResponse.getInstance(new ParcelableUser(user, mAccountId), null);
                } catch (final TwitterException e) {
                    return SingleResponse.getInstance(null, e);
                }
            }
            return SingleResponse.getInstance();
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
            if (result.hasData()) {
                final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
                final String message = mContext.getString(R.string.unfollowed_user,
                        mUserColorNameManager.getDisplayName(result.getData(), nameFirst, true));
                Utils.showInfoMessage(mContext, message, false);
                bus.post(new FriendshipUserUpdatedEvent(result.getData()));
            } else {
                Utils.showErrorMessage(mContext, R.string.action_unfollowing, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    class DestroyMuteTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final long mAccountId;
        private final long mUserId;

        public DestroyMuteTask(final long accountId, final long userId) {
            super(mContext);
            mAccountId = accountId;
            mUserId = userId;
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final User user = twitter.destroyMute(mUserId);
                Utils.setLastSeen(mContext, user.getId(), -1);
                return SingleResponse.getInstance(new ParcelableUser(user, mAccountId), null);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(null, e);
            }

        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
            if (result.hasData()) {
                final boolean nameFirst = mPreferences.getBoolean(KEY_NAME_FIRST);
                final String message = mContext.getString(R.string.unmuted_user,
                        mUserColorNameManager.getDisplayName(result.getData(), nameFirst, true));
                Utils.showInfoMessage(mContext, message, false);


                bus.post(new FriendshipUserUpdatedEvent(result.getData()));
            } else {
                Utils.showErrorMessage(mContext, R.string.action_unmuting, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    class DestroySavedSearchTask extends ManagedAsyncTask<Object, Object, SingleResponse<SavedSearch>> {

        private final long mAccountId;
        private final long mSearchId;

        DestroySavedSearchTask(final long accountId, final long searchId) {
            super(mContext);
            mAccountId = accountId;
            mSearchId = searchId;
        }

        @Override
        protected SingleResponse<SavedSearch> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                return SingleResponse.getInstance(twitter.destroySavedSearch(mSearchId));
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<SavedSearch> result) {
            if (result.hasData()) {
                final String message = mContext.getString(R.string.search_name_deleted, result.getData().getQuery());
                Utils.showOkMessage(mContext, message, false);
            } else {
                Utils.showErrorMessage(mContext, R.string.action_deleting_search, result.getException(), false);
            }
            super.onPostExecute(result);
        }

    }

    class DestroyStatusTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableStatus>> {

        private final long account_id;

        private final long status_id;

        public DestroyStatusTask(final long account_id, final long status_id) {
            super(mContext);
            this.account_id = account_id;
            this.status_id = status_id;
        }

        @Override
        protected SingleResponse<ParcelableStatus> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, account_id, false);
            if (twitter == null) return SingleResponse.getInstance();
            ParcelableStatus status = null;
            TwitterException exception = null;
            try {
                status = ParcelableStatusUtils.fromStatus(twitter.destroyStatus(status_id), account_id, false);
            } catch (final TwitterException e) {
                exception = e;
            }
            if (status != null || (exception != null && exception.getErrorCode() == HttpResponseCode.NOT_FOUND)) {
                final ContentValues values = new ContentValues();
                values.put(Statuses.MY_RETWEET_ID, -1);
                if (status != null) {
                    values.put(Statuses.RETWEET_COUNT, status.retweet_count - 1);
                }
                for (final Uri uri : TwidereDataStore.STATUSES_URIS) {
                    mResolver.delete(uri, Statuses.STATUS_ID + " = " + status_id, null);
                    mResolver.update(uri, values, Statuses.MY_RETWEET_ID + " = " + status_id, null);
                }
            }
            return SingleResponse.getInstance(status, exception);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDestroyingStatusIds.put(account_id, status_id);
            bus.post(new StatusListChangedEvent());
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableStatus> result) {
            mDestroyingStatusIds.remove(account_id, status_id);
            if (result.hasData()) {
                final ParcelableStatus status = result.getData();
                if (status.retweet_id > 0) {
                    Utils.showInfoMessage(mContext, R.string.retweet_cancelled, false);
                } else {
                    Utils.showInfoMessage(mContext, R.string.status_deleted, false);
                }
                bus.post(new StatusDestroyedEvent(status));
            } else {
                Utils.showErrorMessage(mContext, R.string.action_deleting, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    class DestroyUserListSubscriptionTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUserList>> {

        private final long mAccountId;
        private final long mListId;

        public DestroyUserListSubscriptionTask(final long accountId, final long listId) {
            super(mContext);
            mAccountId = accountId;
            mListId = listId;
        }

        @Override
        protected SingleResponse<ParcelableUserList> doInBackground(final Object... params) {

            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter != null) {
                try {
                    final ParcelableUserList list = new ParcelableUserList(
                            twitter.destroyUserListSubscription(mListId), mAccountId);
                    return SingleResponse.getInstance(list, null);
                } catch (final TwitterException e) {
                    return SingleResponse.getInstance(null, e);
                }
            }
            return SingleResponse.getInstance();
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
            final boolean succeed = result.hasData();
            if (succeed) {
                final String message = mContext.getString(R.string.unsubscribed_from_list, result.getData().name);
                Utils.showOkMessage(mContext, message, false);
                final Intent intent = new Intent(BROADCAST_USER_LIST_UNSUBSCRIBED);
                intent.putExtra(EXTRA_USER_LIST, result.getData());
                mContext.sendBroadcast(intent);
            } else {
                Utils.showErrorMessage(mContext, R.string.action_unsubscribing_from_list, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    static class DestroyUserListTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUserList>> {

        private final long mAccountId;
        private final long mListId;

        public DestroyUserListTask(Context context, final long accountId, final long listId) {
            super(context);
            mAccountId = accountId;
            mListId = listId;
        }

        @Override
        protected SingleResponse<ParcelableUserList> doInBackground(final Object... params) {

            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(getContext(), mAccountId, false);
            if (twitter != null) {
                try {
                    if (mListId > 0) {
                        final ParcelableUserList list = new ParcelableUserList(twitter.destroyUserList(mListId),
                                mAccountId);
                        return SingleResponse.getInstance(list);
                    }
                } catch (final TwitterException e) {
                    return SingleResponse.getInstance(e);
                }
            }
            return SingleResponse.getInstance();
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
            final boolean succeed = result.hasData();
            final Context context = getContext();
            if (succeed) {
                final String message = context.getString(R.string.deleted_list, result.getData().name);
                Utils.showInfoMessage(context, message, false);
                bus.post(new UserListDestroyedEvent(result.getData()));
            } else {
                Utils.showErrorMessage(context, R.string.action_deleting, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    class GetLocalTrendsTask extends GetTrendsTask {

        private final int woeid;

        public GetLocalTrendsTask(final long account_id, final int woeid) {
            super(account_id);
            this.woeid = woeid;
        }

        @Override
        public List<Trends> getTrends(@NonNull final Twitter twitter) throws TwitterException {
            return twitter.getLocationTrends(woeid);
        }

        @Override
        protected void onPostExecute(final ListResponse<Trends> result) {
            mAsyncTaskManager.add(new StoreLocalTrendsTask(result), true);
            super.onPostExecute(result);

        }

    }

    static class GetReceivedDirectMessagesTask extends GetDirectMessagesTask {

        public GetReceivedDirectMessagesTask(Context context) {
            super(context);
        }

        @Override
        public ResponseList<DirectMessage> getDirectMessages(final Twitter twitter, final Paging paging)
                throws TwitterException {
            return twitter.getDirectMessages(paging);
        }

        @Override
        protected Uri getDatabaseUri() {
            return Inbox.CONTENT_URI;
        }


        @Override
        protected boolean isOutgoing() {
            return false;
        }

        @Override
        public void notifyStart() {
            final Intent intent = new Intent(BROADCAST_RESCHEDULE_DIRECT_MESSAGES_REFRESHING);
            context.sendBroadcast(intent);
            super.notifyStart();
        }
    }

    static class GetSentDirectMessagesTask extends GetDirectMessagesTask {

        public GetSentDirectMessagesTask(Context context) {
            super(context);
        }

        @Override
        public ResponseList<DirectMessage> getDirectMessages(final Twitter twitter, final Paging paging)
                throws TwitterException {
            return twitter.getSentDirectMessages(paging);
        }

        @Override
        protected boolean isOutgoing() {
            return true;
        }

        @Override
        protected Uri getDatabaseUri() {
            return Outbox.CONTENT_URI;
        }

    }

    public SharedPreferencesWrapper getPreferences() {
        return mPreferences;
    }

    abstract class GetTrendsTask extends ManagedAsyncTask<Object, Object, ListResponse<Trends>> {

        private final long account_id;

        public GetTrendsTask(final long account_id) {
            super(mContext, TASK_TAG_GET_TRENDS);
            this.account_id = account_id;
        }

        public abstract List<Trends> getTrends(@NonNull Twitter twitter) throws TwitterException;

        @Override
        protected ListResponse<Trends> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, account_id, false);
            final Bundle extras = new Bundle();
            extras.putLong(EXTRA_ACCOUNT_ID, account_id);
            if (twitter != null) {
                try {
                    return new ListResponse<>(getTrends(twitter), null, extras);
                } catch (final TwitterException e) {
                    return new ListResponse<>(null, e, extras);
                }
            }
            return new ListResponse<>(null, null, extras);
        }

    }

    final class RemoveUnreadCountsTask extends AsyncTask<Object, Object, Integer> {
        private final int position;
        private final LongSparseArray<Set<Long>> counts;

        RemoveUnreadCountsTask(final int position, final LongSparseArray<Set<Long>> counts) {
            this.position = position;
            this.counts = counts;
        }

        @Override
        protected Integer doInBackground(final Object... params) {
            return removeUnreadCounts(mContext, position, counts);
        }

    }

    class ReportMultiSpamTask extends ManagedAsyncTask<Object, Object, ListResponse<Long>> {

        private final long account_id;
        private final long[] user_ids;

        public ReportMultiSpamTask(final long account_id, final long[] user_ids) {
            super(mContext);
            this.account_id = account_id;
            this.user_ids = user_ids;
        }

        @Override
        protected ListResponse<Long> doInBackground(final Object... params) {

            final Bundle extras = new Bundle();
            extras.putLong(EXTRA_ACCOUNT_ID, account_id);
            final List<Long> reported_users = new ArrayList<>();
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, account_id, false);
            if (twitter != null) {
                for (final long user_id : user_ids) {
                    try {
                        final User user = twitter.reportSpam(user_id);
                        if (user == null || user.getId() <= 0) {
                            continue;
                        }
                        reported_users.add(user.getId());
                    } catch (final TwitterException e) {
                        return new ListResponse<>(null, e, extras);
                    }
                }
            }
            return new ListResponse<>(reported_users, null, extras);
        }

        @Override
        protected void onPostExecute(final ListResponse<Long> result) {
            if (result != null) {
                final String user_id_where = TwidereListUtils.toString(result.getData(), ',', false);
                for (final Uri uri : TwidereDataStore.STATUSES_URIS) {
                    final Expression where = Expression.and(Expression.equals(Statuses.ACCOUNT_ID, account_id),
                            new Expression(String.format(Locale.ROOT, "%s IN (%s)", Statuses.USER_ID, user_id_where)));
                    mResolver.delete(uri, where.getSQL(), null);
                }
                Utils.showInfoMessage(mContext, R.string.reported_users_for_spam, false);
                final Intent intent = new Intent(BROADCAST_MULTI_BLOCKSTATE_CHANGED);
                intent.putExtra(EXTRA_USER_IDS, user_ids);
                intent.putExtra(EXTRA_ACCOUNT_ID, account_id);
                mContext.sendBroadcast(intent);
            }
            super.onPostExecute(result);
        }

    }

    class ReportSpamTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final long mAccountId;
        private final long user_id;

        public ReportSpamTask(final long accountId, final long user_id) {
            super(mContext);
            this.mAccountId = accountId;
            this.user_id = user_id;
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter != null) {
                try {
                    final User user = twitter.reportSpam(user_id);
                    return SingleResponse.getInstance(new ParcelableUser(user, mAccountId), null);
                } catch (final TwitterException e) {
                    return SingleResponse.getInstance(null, e);
                }
            }
            return SingleResponse.getInstance();
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
            if (result.hasData()) {
                for (final Uri uri : TwidereDataStore.STATUSES_URIS) {
                    final String where = Statuses.ACCOUNT_ID + " = " + mAccountId + " AND " + Statuses.USER_ID + " = "
                            + user_id;
                    mResolver.delete(uri, where, null);
                }
                Utils.showInfoMessage(mContext, R.string.reported_user_for_spam, false);


                bus.post(new FriendshipUserUpdatedEvent(result.getData()));
            } else {
                Utils.showErrorMessage(mContext, R.string.action_reporting_for_spam, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    class RetweetStatusTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableStatus>> {

        private final long accountId;
        private final long statusId;

        public RetweetStatusTask(final long accountId, final long statusId) {
            super(mContext);
            this.accountId = accountId;
            this.statusId = statusId;
        }

        @Override
        protected SingleResponse<ParcelableStatus> doInBackground(final Object... params) {
            if (accountId < 0) return SingleResponse.getInstance();
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountId, true);
            if (twitter == null) {
                return SingleResponse.getInstance();
            }
            try {
                final ParcelableStatus status = ParcelableStatusUtils.fromStatus(twitter.retweetStatus(statusId), accountId, false);
                Utils.setLastSeen(mContext, status.mentions, System.currentTimeMillis());
                final ContentValues values = new ContentValues();
                values.put(Statuses.MY_RETWEET_ID, status.id);
                values.put(Statuses.RETWEET_COUNT, status.retweet_count);
                final Expression where = Expression.or(
                        Expression.equals(Statuses.STATUS_ID, statusId),
                        Expression.equals(Statuses.RETWEET_ID, statusId)
                );
                for (final Uri uri : TwidereDataStore.STATUSES_URIS) {
                    mResolver.update(uri, values, where.getSQL(), null);
                }
                return SingleResponse.getInstance(status);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCreatingRetweetIds.put(accountId, statusId);


            bus.post(new StatusListChangedEvent());
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableStatus> result) {
            mCreatingRetweetIds.remove(accountId, statusId);
            if (result.hasData()) {
                final ParcelableStatus status = result.getData();
                // BEGIN HotMobi

                final TweetEvent event = TweetEvent.create(getContext(), status, TimelineType.OTHER);
                event.setAction(TweetEvent.Action.RETWEET);
                HotMobiLogger.getInstance(getContext()).log(accountId, event);

                // END HotMobi

                bus.post(new StatusRetweetedEvent(status));
                Utils.showOkMessage(mContext, R.string.status_retweeted, false);
            } else {
                Utils.showErrorMessage(mContext, R.string.action_retweeting, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }


    class StoreLocalTrendsTask extends StoreTrendsTask {

        public StoreLocalTrendsTask(final ListResponse<Trends> result) {
            super(mContext, result, CachedTrends.Local.CONTENT_URI);
        }

    }

    static class StoreTrendsTask extends ManagedAsyncTask<Object, Object, SingleResponse<Boolean>> {

        private final ListResponse<Trends> response;
        private final Uri uri;
        private Context context;

        public StoreTrendsTask(Context context, final ListResponse<Trends> response, final Uri uri) {
            super(context, TASK_TAG_STORE_TRENDS);
            this.response = response;
            this.uri = uri;
            this.context = context;
        }

        @Override
        protected SingleResponse<Boolean> doInBackground(final Object... args) {
            if (response == null) return SingleResponse.getInstance(false);
            ContentResolver cr = context.getContentResolver();
            final List<Trends> messages = response.getData();
            final ArrayList<String> hashtags = new ArrayList<>();
            final ArrayList<ContentValues> hashtagValues = new ArrayList<>();
            if (messages != null && messages.size() > 0) {
                final ContentValues[] valuesArray = ContentValuesCreator.createTrends(messages);
                for (final ContentValues values : valuesArray) {
                    final String hashtag = values.getAsString(CachedTrends.NAME).replaceFirst("#", "");
                    if (hashtags.contains(hashtag)) {
                        continue;
                    }
                    hashtags.add(hashtag);
                    final ContentValues hashtagValue = new ContentValues();
                    hashtagValue.put(CachedHashtags.NAME, hashtag);
                    hashtagValues.add(hashtagValue);
                }
                cr.delete(uri, null, null);
                ContentResolverUtils.bulkInsert(cr, uri, valuesArray);
                ContentResolverUtils.bulkDelete(cr, CachedHashtags.CONTENT_URI, CachedHashtags.NAME, hashtags, null, true);
                ContentResolverUtils.bulkInsert(cr, CachedHashtags.CONTENT_URI,
                        hashtagValues.toArray(new ContentValues[hashtagValues.size()]));
            }
            return SingleResponse.getInstance(true);
        }

        @Override
        protected void onPostExecute(final SingleResponse<Boolean> response) {
            // if (response != null && response.data != null &&
            // response.data.getBoolean(EXTRA_SUCCEED)) {
            // final Intent intent = new Intent(BROADCAST_TRENDS_UPDATED);
            // intent.putExtra(EXTRA_SUCCEED, true);
            // mContext.sendBroadcast(intent);
            // }
            super.onPostExecute(response);
        }

    }

    static class UpdateUserListDetailsTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUserList>> {

        private final long accountId;
        private final long listId;
        private final UserListUpdate update;
        private Context mContext;

        public UpdateUserListDetailsTask(Context context, final long accountId, final long listId, UserListUpdate update) {
            super(context);
            this.accountId = accountId;
            this.listId = listId;
            this.update = update;
            this.mContext = context;
        }

        @Override
        protected SingleResponse<ParcelableUserList> doInBackground(final Object... params) {

            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountId, false);
            if (twitter != null) {
                try {
                    final UserList list = twitter.updateUserList(listId, update);
                    return SingleResponse.getInstance(new ParcelableUserList(list, accountId));
                } catch (final TwitterException e) {
                    return SingleResponse.getInstance(e);
                }
            }
            return SingleResponse.getInstance();
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
            if (result.hasData() && result.getData().id > 0) {
                final String message = mContext.getString(R.string.updated_list_details, result.getData().name);
                Utils.showOkMessage(mContext, message, false);
                final Intent intent = new Intent(BROADCAST_USER_LIST_DETAILS_UPDATED);
                intent.putExtra(EXTRA_LIST_ID, listId);
                mContext.sendBroadcast(intent);
            } else {
                Utils.showErrorMessage(mContext, R.string.action_updating_details, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

}
