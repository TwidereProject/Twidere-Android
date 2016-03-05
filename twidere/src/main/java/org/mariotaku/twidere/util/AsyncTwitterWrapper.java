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
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

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
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserList;
import org.mariotaku.twidere.api.twitter.model.UserListUpdate;
import org.mariotaku.twidere.model.AccountId;
import org.mariotaku.twidere.model.BaseRefreshTaskParam;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.RefreshTaskParam;
import org.mariotaku.twidere.model.Response;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.message.FavoriteTaskEvent;
import org.mariotaku.twidere.model.message.FollowRequestTaskEvent;
import org.mariotaku.twidere.model.message.FriendshipUpdatedEvent;
import org.mariotaku.twidere.model.message.FriendshipUserUpdatedEvent;
import org.mariotaku.twidere.model.message.ProfileUpdatedEvent;
import org.mariotaku.twidere.model.message.SavedSearchDestroyedEvent;
import org.mariotaku.twidere.model.message.StatusDestroyedEvent;
import org.mariotaku.twidere.model.message.StatusListChangedEvent;
import org.mariotaku.twidere.model.message.StatusRetweetedEvent;
import org.mariotaku.twidere.model.message.UserListCreatedEvent;
import org.mariotaku.twidere.model.message.UserListDestroyedEvent;
import org.mariotaku.twidere.model.util.ParcelableStatusUtils;
import org.mariotaku.twidere.model.util.ParcelableUserListUtils;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Inbox;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Outbox;
import org.mariotaku.twidere.provider.TwidereDataStore.Drafts;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.service.BackgroundOperationService;
import org.mariotaku.twidere.task.AbstractTask;
import org.mariotaku.twidere.task.GetActivitiesAboutMeTask;
import org.mariotaku.twidere.task.GetDirectMessagesTask;
import org.mariotaku.twidere.task.GetHomeTimelineTask;
import org.mariotaku.twidere.task.GetLocalTrendsTask;
import org.mariotaku.twidere.task.GetSavedSearchesTask;
import org.mariotaku.twidere.task.ManagedAsyncTask;
import org.mariotaku.twidere.task.twitter.GetActivitiesTask;
import org.mariotaku.twidere.task.util.TaskStarter;
import org.mariotaku.twidere.util.collection.LongSparseMap;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
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

    private LongSparseMap<Long> mCreatingFavoriteIds = new LongSparseMap<>();
    private LongSparseMap<Long> mDestroyingFavoriteIds = new LongSparseMap<>();
    private LongSparseMap<Long> mCreatingRetweetIds = new LongSparseMap<>();
    private LongSparseMap<Long> mDestroyingStatusIds = new LongSparseMap<>();
    private IntList mProcessingFriendshipRequestIds = new ArrayIntList();

    private final LongList mSendingDraftIds = new ArrayLongList();

    public AsyncTwitterWrapper(Context context, Bus bus, SharedPreferencesWrapper preferences,
                               AsyncTaskManager asyncTaskManager) {
        mContext = context;
        mResolver = context.getContentResolver();
        mBus = bus;
        mPreferences = preferences;
        mAsyncTaskManager = asyncTaskManager;
    }

    public int acceptFriendshipAsync(final AccountId accountId, final long userId) {
        final AcceptFriendshipTask task = new AcceptFriendshipTask(mContext, accountId, userId);
        return mAsyncTaskManager.add(task, true);
    }

    public void addSendingDraftId(long id) {
        synchronized (mSendingDraftIds) {
            mSendingDraftIds.add(id);
            mResolver.notifyChange(Drafts.CONTENT_URI_UNSENT, null);
        }
    }

    public int addUserListMembersAsync(final AccountId accountId, final long listId, final ParcelableUser... users) {
        final AddUserListMembersTask task = new AddUserListMembersTask(accountId, listId, users);
        return mAsyncTaskManager.add(task, true);
    }

    public int cancelRetweetAsync(AccountId accountId, long statusId, long myRetweetId) {
        if (myRetweetId > 0)
            return destroyStatusAsync(accountId, myRetweetId);
        else if (statusId > 0)
            return destroyStatusAsync(accountId, statusId);
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

    public int createBlockAsync(final AccountId accountId, final long user_id) {
        final CreateBlockTask task = new CreateBlockTask(accountId, user_id);
        return mAsyncTaskManager.add(task, true);
    }

    public int createFavoriteAsync(final AccountId accountId, final long statusId) {
        final CreateFavoriteTask task = new CreateFavoriteTask(accountId, statusId);
        return mAsyncTaskManager.add(task, true);
    }

    public int createFriendshipAsync(final AccountId accountId, final long userId) {
        final CreateFriendshipTask task = new CreateFriendshipTask(accountId, userId);
        return mAsyncTaskManager.add(task, true);
    }

    public int createMultiBlockAsync(final AccountId accountId, final long[] userIds) {
        final CreateMultiBlockTask task = new CreateMultiBlockTask(accountId, userIds);
        return mAsyncTaskManager.add(task, true);
    }

    public int createMuteAsync(final AccountId accountId, final long user_id) {
        final CreateMuteTask task = new CreateMuteTask(accountId, user_id);
        return mAsyncTaskManager.add(task, true);
    }

    public int createSavedSearchAsync(final AccountId accountId, final String query) {
        final CreateSavedSearchTask task = new CreateSavedSearchTask(accountId, query);
        return mAsyncTaskManager.add(task, true);
    }

    public int createUserListAsync(final AccountId accountId, final String listName, final boolean isPublic,
                                   final String description) {
        final CreateUserListTask task = new CreateUserListTask(mContext, accountId, listName, isPublic,
                description);
        return mAsyncTaskManager.add(task, true);
    }

    public int createUserListSubscriptionAsync(final AccountId accountId, final long listId) {
        final CreateUserListSubscriptionTask task = new CreateUserListSubscriptionTask(accountId, listId);
        return mAsyncTaskManager.add(task, true);
    }

    public int deleteUserListMembersAsync(final AccountId accountId, final long listId, final ParcelableUser... users) {
        final DeleteUserListMembersTask task = new DeleteUserListMembersTask(accountId, listId, users);
        return mAsyncTaskManager.add(task, true);
    }

    public int denyFriendshipAsync(final AccountId accountId, final long userId) {
        final DenyFriendshipTask task = new DenyFriendshipTask(accountId, userId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyBlockAsync(final AccountId accountId, final long userId) {
        final DestroyBlockTask task = new DestroyBlockTask(accountId, userId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyDirectMessageAsync(final AccountId accountId, final long messageId) {
        final DestroyDirectMessageTask task = new DestroyDirectMessageTask(accountId, messageId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyMessageConversationAsync(final AccountId accountId, final long userId) {
        final DestroyMessageConversationTask task = new DestroyMessageConversationTask(accountId, userId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyFavoriteAsync(final AccountId accountId, final long statusId) {
        final DestroyFavoriteTask task = new DestroyFavoriteTask(accountId, statusId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyFriendshipAsync(final AccountId accountId, final long userId) {
        final DestroyFriendshipTask task = new DestroyFriendshipTask(accountId, userId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyMuteAsync(final AccountId accountId, final long userId) {
        final DestroyMuteTask task = new DestroyMuteTask(accountId, userId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroySavedSearchAsync(final AccountId accountId, final long searchId) {
        final DestroySavedSearchTask task = new DestroySavedSearchTask(accountId, searchId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyStatusAsync(final AccountId accountId, final long statusId) {
        final DestroyStatusTask task = new DestroyStatusTask(accountId, statusId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyUserListAsync(final AccountId accountId, final long listId) {
        final DestroyUserListTask task = new DestroyUserListTask(mContext, accountId, listId);
        return mAsyncTaskManager.add(task, true);
    }

    public int destroyUserListSubscriptionAsync(final AccountId accountId, final long listId) {
        final DestroyUserListSubscriptionTask task = new DestroyUserListSubscriptionTask(accountId, listId);
        return mAsyncTaskManager.add(task, true);
    }

    public Context getContext() {
        return mContext;
    }

    public boolean getHomeTimelineAsync(final AccountId[] accountIds, final long[] maxIds, final long[] sinceIds) {
        return getHomeTimelineAsync(new BaseRefreshTaskParam(accountIds, maxIds, sinceIds));
    }

    public boolean getHomeTimelineAsync(RefreshTaskParam param) {
        final GetHomeTimelineTask task = new GetHomeTimelineTask(getContext());
        task.setParams(param);
        TaskStarter.execute(task);
        return true;
    }

    public void getLocalTrendsAsync(final long accountId, final int woeid) {
        final GetLocalTrendsTask task = new GetLocalTrendsTask(mContext, accountId, woeid);
        TaskStarter.execute(task);
    }

    public void getReceivedDirectMessagesAsync(final AccountId[] accountIds,
                                               final long[] maxIds, final long[] sinceIds) {
        getActivitiesAboutMeAsync(new BaseRefreshTaskParam(accountIds, maxIds, sinceIds));
    }

    public void getReceivedDirectMessagesAsync(RefreshTaskParam param) {
        final GetReceivedDirectMessagesTask task = new GetReceivedDirectMessagesTask(mContext);
        task.setParams(param);
        TaskStarter.execute(task);
    }

    public void getSentDirectMessagesAsync(final AccountId[] accountIds,
                                           final long[] maxIds, final long[] sinceIds) {
        final GetSentDirectMessagesTask task = new GetSentDirectMessagesTask(mContext);
        task.setParams(new BaseRefreshTaskParam(accountIds, maxIds, sinceIds));
        TaskStarter.execute(task);
    }

    public int getSavedSearchesAsync(AccountId[] accountIds) {
        final GetSavedSearchesTask task = new GetSavedSearchesTask(mContext);
        task.setParams(accountIds);
        TaskStarter.execute(task);
        return System.identityHashCode(task);
    }

    @NonNull
    public long[] getSendingDraftIds() {
        return mSendingDraftIds.toArray();
    }

    public boolean isCreatingFavorite(final long accountId, final long statusId) {
        return mCreatingFavoriteIds.has(accountId, statusId);
    }

    public boolean isCreatingFriendship(final AccountId accountId, final long userId) {
        // TODO implementation
        return false;
    }

    public boolean isCreatingRetweet(final AccountId accountId, final long statusId) {
        return mCreatingRetweetIds.has(accountId.getId(), statusId);
    }

    public boolean isDestroyingFavorite(final AccountId accountId, final long statusId) {
        return mDestroyingFavoriteIds.has(accountId.getId(), statusId);
    }

    public boolean isDestroyingFriendship(final AccountId accountId, final long userId) {
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

    public boolean refreshAll(final AccountId[] accountIds) {
        AsyncTaskUtils.executeTask(new AsyncTask<AccountId, Object, Object[]>() {
            @Override
            protected Object[] doInBackground(AccountId... accountIds) {
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

    public void reportMultiSpam(final long accountId, final long[] userIds) {
        // TODO implementation
    }

    public int reportSpamAsync(final AccountId accountId, final long userId) {
        final ReportSpamTask task = new ReportSpamTask(accountId, userId);
        return mAsyncTaskManager.add(task, true);
    }

    public int retweetStatusAsync(final AccountId accountId, final long statusId) {
        final RetweetStatusTask task = new RetweetStatusTask(accountId, statusId);
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

    public int updateUserListDetails(final AccountId accountId, final long listId,
                                     final UserListUpdate update) {
        final UpdateUserListDetailsTask task = new UpdateUserListDetailsTask(mContext, accountId,
                listId, update);
        return mAsyncTaskManager.add(task, true);
    }

    public static <T extends Response<?>> Exception getException(List<T> responses) {
        for (T response : responses) {
            if (response.hasException()) return response.getException();
        }
        return null;
    }

    public void updateFriendship(final AccountId accountId, final long userId, final FriendshipUpdate update) {
        final Bus bus = mBus;
        if (bus == null) return;
        TaskStarter.execute(new AbstractTask<Object, SingleResponse<Relationship>, Bus>() {
            @Override
            public SingleResponse<Relationship> doLongOperation(Object param) {
                final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountId, true);
                try {
                    return SingleResponse.getInstance(twitter.updateFriendship(userId, update));
                } catch (TwitterException e) {
                    return SingleResponse.getInstance(e);
                }
            }

            @Override
            public void afterExecute(Bus handler, SingleResponse<Relationship> result) {
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

    public void getActivitiesAboutMeAsync(final AccountId[] accountIds,
                                          long[] maxIds, long[] sinceIds) {
        getActivitiesAboutMeAsync(new BaseRefreshTaskParam(accountIds, maxIds, sinceIds));
    }

    public void getActivitiesAboutMeAsync(final RefreshTaskParam param) {
        final GetActivitiesTask task = new GetActivitiesAboutMeTask(getContext());
        task.setParams(param);
        TaskStarter.execute(task);
    }

    public void setActivitiesAboutMeUnreadAsync(final AccountId[] accountIds, final long cursor) {
        AbstractTask<Object, Object, AsyncTwitterWrapper> task = new AbstractTask<Object, Object, AsyncTwitterWrapper>() {

            @Override
            public Object doLongOperation(Object o) {
                for (AccountId accountId : accountIds) {
                    Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountId, false);
                    if (!Utils.isOfficialCredentials(mContext, accountId)) continue;
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
        TaskStarter.execute(task);
    }

    private void addProcessingFriendshipRequestId(AccountId accountId, long userId) {
        mProcessingFriendshipRequestIds.add(ParcelableUser.calculateHashCode(accountId.getId(), userId));
    }

    private void removeProcessingFriendshipRequestId(AccountId accountId, long userId) {
        mProcessingFriendshipRequestIds.removeElement(ParcelableUser.calculateHashCode(accountId.getId(), userId));
    }

    public boolean isProcessingFollowRequest(long accountId, long userId) {
        return mProcessingFriendshipRequestIds.contains(ParcelableUser.calculateHashCode(accountId, userId));
    }

    public static class UpdateProfileBannerImageTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final AccountId mAccountId;
        private final Uri mImageUri;
        private final boolean mDeleteImage;
        private final Context mContext;

        public UpdateProfileBannerImageTask(final Context context, final AccountId accountId,
                                            final Uri imageUri, final boolean deleteImage) {
            super(context);
            mContext = context;
            mAccountId = accountId;
            mImageUri = imageUri;
            mDeleteImage = deleteImage;
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
                final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId,
                        true);
                TwitterWrapper.updateProfileBannerImage(mContext, twitter, mImageUri, mDeleteImage);
                // Wait for 5 seconds, see
                // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    Log.w(LOGTAG, e);
                }
                final User user = TwitterWrapper.tryShowUser(twitter, mAccountId.getId(), null);
                return SingleResponse.getInstance(ParcelableUserUtils.fromUser(user, mAccountId));
            } catch (TwitterException | FileNotFoundException e) {
                return SingleResponse.getInstance(e);
            }
        }


    }

    public static class UpdateProfileImageTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final AccountId mAccountId;
        private final Uri mImageUri;
        private final boolean mDeleteImage;
        private final Context mContext;

        public UpdateProfileImageTask(final Context context, final AccountId accountId,
                                      String accountHost, final Uri imageUri,
                                      final boolean deleteImage) {
            super(context);
            this.mContext = context;
            this.mAccountId = accountId;
            this.mImageUri = imageUri;
            this.mDeleteImage = deleteImage;
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {
            try {
                final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, true);
                TwitterWrapper.updateProfileImage(mContext, twitter, mImageUri, mDeleteImage);
                // Wait for 5 seconds, see
                // https://dev.twitter.com/rest/reference/post/account/update_profile_image
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    Log.w(LOGTAG, e);
                }
                final User user = TwitterWrapper.tryShowUser(twitter, mAccountId.getId(), null);
                return SingleResponse.getInstance(ParcelableUserUtils.fromUser(user, mAccountId));
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

        private final AccountId mAccountId;
        private final long mUserId;

        public AcceptFriendshipTask(final Context context, final AccountId accountId, final long userId) {
            super(context);
            mAccountId = accountId;
            mUserId = userId;
        }

        public AccountId getAccountId() {
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

        private final AccountId accountId;
        private final long listId;
        private final ParcelableUser[] users;

        public AddUserListMembersTask(final AccountId accountId, final long listId, final ParcelableUser[] users) {
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
                final UserList result = twitter.addUserListMembers(listId, userIds);
                final ParcelableUserList list = ParcelableUserListUtils.from(result, accountId);
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

        private final AccountId mAccountId;
        private final long mUserId;

        public CreateBlockTask(final AccountId accountId, final long userId) {
            super(mContext);
            this.mAccountId = accountId;
            this.mUserId = userId;
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final User user = twitter.createBlock(mUserId);
                Utils.setLastSeen(mContext, user.getId(), -1);
                for (final Uri uri : TwidereDataStore.STATUSES_URIS) {
                    final Expression where = Expression.and(
                            Utils.getAccountCompareExpression(),
                            Expression.equalsArgs(Statuses.USER_ID)
                    );
                    final String[] whereArgs = {String.valueOf(mAccountId.getId()),
                            mAccountId.getHost(), String.valueOf(mUserId)};
                    mResolver.delete(uri, where.getSQL(), whereArgs);

                }
                // I bet you don't want to see this user in your auto complete list.
                final ContentValues values = new ContentValues();
                values.put(CachedRelationships.ACCOUNT_ID, mAccountId.getId());
                values.put(CachedRelationships.ACCOUNT_HOST, mAccountId.getHost());
                values.put(CachedRelationships.USER_ID, mUserId);
                values.put(CachedRelationships.BLOCKING, true);
                mResolver.insert(CachedRelationships.CONTENT_URI, values);
                return SingleResponse.getInstance(ParcelableUserUtils.fromUser(user, mAccountId), null);
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

        private final AccountId mAccountId;
        private final long mStatusId;

        public CreateFavoriteTask(final AccountId accountId, final long statusId) {
            super(mContext);
            this.mAccountId = accountId;
            this.mStatusId = statusId;
        }

        @Override
        protected SingleResponse<ParcelableStatus> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, true);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final ParcelableStatus status = ParcelableStatusUtils.fromStatus(twitter.createFavorite(mStatusId),
                        mAccountId, false);
                Utils.setLastSeen(mContext, status.mentions, System.currentTimeMillis());
                final ContentValues values = new ContentValues();
                values.put(Statuses.IS_FAVORITE, true);
                values.put(Statuses.REPLY_COUNT, status.reply_count);
                values.put(Statuses.RETWEET_COUNT, status.retweet_count);
                values.put(Statuses.FAVORITE_COUNT, status.favorite_count);
                final Expression where = Expression.and(Utils.getAccountCompareExpression(),
                        Expression.or(Expression.equals(Statuses.STATUS_ID, mStatusId),
                                Expression.equals(Statuses.RETWEET_ID, mStatusId)));
                final String[] whereArgs = {String.valueOf(mAccountId.getId()), mAccountId.getHost(),
                        String.valueOf(mStatusId), String.valueOf(mStatusId)};
                for (final Uri uri : TwidereDataStore.STATUSES_URIS) {
                    mResolver.update(uri, values, where.getSQL(), whereArgs);
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
            mCreatingFavoriteIds.put(mAccountId.getId(), mStatusId);
            bus.post(new StatusListChangedEvent());
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableStatus> result) {
            mCreatingFavoriteIds.remove(mAccountId.getId(), mStatusId);
            final FavoriteTaskEvent taskEvent = new FavoriteTaskEvent(FavoriteTaskEvent.Action.CREATE,
                    mAccountId, mStatusId);
            taskEvent.setFinished(true);
            if (result.hasData()) {
                final ParcelableStatus status = result.getData();
                taskEvent.setStatus(status);
                taskEvent.setSucceeded(true);
                // BEGIN HotMobi
                final TweetEvent tweetEvent = TweetEvent.create(getContext(), status, TimelineType.OTHER);
                tweetEvent.setAction(TweetEvent.Action.FAVORITE);
                HotMobiLogger.getInstance(getContext()).log(mAccountId.getId(), tweetEvent);
                // END HotMobi
            } else {
                taskEvent.setSucceeded(false);
                Utils.showErrorMessage(mContext, R.string.action_favoriting, result.getException(), true);
            }
            bus.post(taskEvent);
            bus.post(new StatusListChangedEvent());
            super.onPostExecute(result);
        }

    }

    class CreateFriendshipTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final AccountId mAccountId;
        private final long user_id;

        public CreateFriendshipTask(final AccountId accountId, final long userId) {
            super(mContext);
            this.mAccountId = accountId;
            this.user_id = userId;
        }

        public AccountId getAccountId() {
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
                return SingleResponse.getInstance(ParcelableUserUtils.fromUser(user, mAccountId), null);
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

        private final AccountId mAccountId;
        private final long[] mUserIds;

        public CreateMultiBlockTask(final AccountId accountId, final long[] userIds) {
            super(mContext);
            this.mAccountId = accountId;
            this.mUserIds = userIds;
        }

        private void deleteCaches(final List<Long> list) {
            for (final Uri uri : TwidereDataStore.STATUSES_URIS) {
                // TODO delete caches
                // ContentResolverUtils.bulkDelete(mResolver, uri, Statuses.USER_ID, list,
                // Statuses.ACCOUNT_ID + " = " + mAccountId, false);
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
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter != null) {
                for (final long user_id : mUserIds) {
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
            intent.putExtra(EXTRA_USER_ID, mUserIds);
            mContext.sendBroadcast(intent);
            super.onPostExecute(result);
        }


    }

    class CreateMuteTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final AccountId mAccountId;
        private final long mUserId;

        public CreateMuteTask(final AccountId accountId, final long userId) {
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

                return SingleResponse.getInstance(ParcelableUserUtils.fromUser(user, mAccountId), null);
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

        private final AccountId mAccountId;
        private final String mQuery;

        CreateSavedSearchTask(final AccountId accountId, final String query) {
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

        private final AccountId accountId;
        private final long listId;

        public CreateUserListSubscriptionTask(final AccountId accountId, final long listId) {
            super(mContext);
            this.accountId = accountId;
            this.listId = listId;
        }

        @Override
        protected SingleResponse<ParcelableUserList> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final UserList userList = twitter.createUserListSubscription(listId);
                final ParcelableUserList list = ParcelableUserListUtils.from(userList, accountId);
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

        private final AccountId mAccountId;
        private final String mListName, mDescription;
        private final boolean mIsPublic;

        public CreateUserListTask(Context context, final AccountId accountId, final String listName,
                                  final boolean isPublic, final String description) {
            super(context);
            this.mAccountId = accountId;
            this.mListName = listName;
            this.mDescription = description;
            this.mIsPublic = isPublic;
        }

        @Override
        protected SingleResponse<ParcelableUserList> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(getContext(), mAccountId,
                    false);
            if (twitter == null || mListName == null) return SingleResponse.getInstance();
            try {
                final UserListUpdate userListUpdate = new UserListUpdate();
                userListUpdate.setName(mListName);
                userListUpdate.setMode(mIsPublic ? UserList.Mode.PUBLIC : UserList.Mode.PRIVATE);
                userListUpdate.setDescription(mDescription);
                final UserList list = twitter.createUserList(userListUpdate);
                return SingleResponse.getInstance(ParcelableUserListUtils.from(list, mAccountId), null);
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

        private final AccountId mAccountId;
        private final long mUserListId;
        private final ParcelableUser[] users;

        public DeleteUserListMembersTask(final AccountId accountId, final long userListId, final ParcelableUser[] users) {
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
                final UserList userList = twitter.deleteUserListMembers(mUserListId, userIds);
                final ParcelableUserList list = ParcelableUserListUtils.from(userList, mAccountId);
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

        private final AccountId mAccountId;
        private final long mUserId;

        public DenyFriendshipTask(final AccountId accountId, final long userId) {
            super(mContext);
            mAccountId = accountId;
            mUserId = userId;
        }

        public AccountId getAccountId() {
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

        private final AccountId mAccountId;
        private final long mUserId;

        public DestroyBlockTask(final AccountId accountId, final long userId) {
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
                return SingleResponse.getInstance(ParcelableUserUtils.fromUser(user, mAccountId), null);
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

        private final AccountId mAccountId;
        private final long mMessageId;

        public DestroyDirectMessageTask(final AccountId accountId, final long messageId) {
            super(mContext);
            mAccountId = accountId;
            mMessageId = messageId;
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
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final DirectMessage message = twitter.destroyDirectMessage(mMessageId);
                deleteMessages(mMessageId);
                return SingleResponse.getInstance(message, null);
            } catch (final TwitterException e) {
                if (isMessageNotFound(e)) {
                    deleteMessages(mMessageId);
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

        private final long mUserId;
        private final AccountId mAccountId;

        public DestroyMessageConversationTask(final AccountId accountId, final long userId) {
            super(mContext);
            mAccountId = accountId;
            mUserId = userId;
        }

        private void deleteMessages(final AccountId accountId, final long userId) {
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
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                twitter.destroyDirectMessagesConversation(mAccountId.getId(), mUserId);
                deleteMessages(mAccountId, mUserId);
                return SingleResponse.getInstance();
            } catch (final TwitterException e) {
                if (isMessageNotFound(e)) {
                    deleteMessages(mAccountId, mUserId);
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

        @NonNull
        private final AccountId mAccountId;
        private final long mStatusId;

        public DestroyFavoriteTask(@NonNull final AccountId accountId, final long statusId) {
            super(mContext);
            this.mAccountId = accountId;
            this.mStatusId = statusId;
        }

        @Override
        protected SingleResponse<ParcelableStatus> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, true);

            if (twitter == null) {
                return SingleResponse.getInstance();
            }
            try {
                final ParcelableStatus status = ParcelableStatusUtils.fromStatus(twitter.destroyFavorite(mStatusId),
                        mAccountId, false);
                final ContentValues values = new ContentValues();
                values.put(Statuses.IS_FAVORITE, false);
                values.put(Statuses.FAVORITE_COUNT, status.favorite_count - 1);
                final Expression where = Expression.and(Expression.equals(Statuses.ACCOUNT_ID, mAccountId),
                        Expression.or(Expression.equals(Statuses.STATUS_ID, mStatusId),
                                Expression.equals(Statuses.RETWEET_ID, mStatusId)));
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
            mDestroyingFavoriteIds.put(mAccountId, mStatusId);
            bus.post(new StatusListChangedEvent());
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableStatus> result) {
            mDestroyingFavoriteIds.remove(mAccountId, mStatusId);
            final FavoriteTaskEvent taskEvent = new FavoriteTaskEvent(FavoriteTaskEvent.Action.DESTROY,
                    mAccountId, mStatusId);
            taskEvent.setFinished(true);
            if (result.hasData()) {
                final ParcelableStatus status = result.getData();
                taskEvent.setStatus(status);
                taskEvent.setSucceeded(true);
                // BEGIN HotMobi
                final TweetEvent tweetEvent = TweetEvent.create(getContext(), status, TimelineType.OTHER);
                tweetEvent.setAction(TweetEvent.Action.UNFAVORITE);
                HotMobiLogger.getInstance(getContext()).log(mAccountId, tweetEvent);
                // END HotMobi
                Utils.showInfoMessage(mContext, R.string.status_unfavorited, false);
            } else {
                taskEvent.setSucceeded(false);
                Utils.showErrorMessage(mContext, R.string.action_unfavoriting, result.getException(), true);
            }
            bus.post(taskEvent);
            bus.post(new StatusListChangedEvent());
            super.onPostExecute(result);
        }

    }

    class DestroyFriendshipTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final AccountId mAccountId;
        private final long mUserId;

        public DestroyFriendshipTask(final AccountId accountId, final long userId) {
            super(mContext);
            mAccountId = accountId;
            mUserId = userId;
        }

        public AccountId getAccountId() {
            return mAccountId;
        }

        public long getUserId() {
            return mUserId;
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {

            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter != null) {
                try {
                    final User user = twitter.destroyFriendship(mUserId);
                    // remove user tweets and retweets
                    Utils.setLastSeen(mContext, user.getId(), -1);
                    final Expression where = Expression.and(Expression.equals(Statuses.ACCOUNT_ID, mAccountId),
                            Expression.or(Expression.equals(Statuses.USER_ID, mUserId),
                                    Expression.equals(Statuses.RETWEETED_BY_USER_ID, mUserId)));
                    mResolver.delete(Statuses.CONTENT_URI, where.getSQL(), null);
                    return SingleResponse.getInstance(ParcelableUserUtils.fromUser(user, mAccountId), null);
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

        private final AccountId mAccountId;
        private final long mUserId;

        public DestroyMuteTask(final AccountId accountId, final long userId) {
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
                return SingleResponse.getInstance(ParcelableUserUtils.fromUser(user, mAccountId), null);
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

        private final AccountId mAccountId;
        private final long mSearchId;

        DestroySavedSearchTask(final AccountId accountId, final long searchId) {
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
                bus.post(new SavedSearchDestroyedEvent(mAccountId, mSearchId));
            } else {
                Utils.showErrorMessage(mContext, R.string.action_deleting_search, result.getException(), false);
            }
            super.onPostExecute(result);
        }

    }

    class DestroyStatusTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableStatus>> {

        private final AccountId mAccountId;
        private final long mStatusId;

        public DestroyStatusTask(final AccountId accountId, final long statusId) {
            super(mContext);
            this.mAccountId = accountId;
            this.mStatusId = statusId;
        }

        @Override
        protected SingleResponse<ParcelableStatus> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            ParcelableStatus status = null;
            TwitterException exception = null;
            try {
                status = ParcelableStatusUtils.fromStatus(twitter.destroyStatus(mStatusId),
                        mAccountId, false);
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
                    mResolver.delete(uri, Statuses.STATUS_ID + " = " + mStatusId, null);
                    mResolver.update(uri, values, Statuses.MY_RETWEET_ID + " = " + mStatusId, null);
                }
            }
            return SingleResponse.getInstance(status, exception);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDestroyingStatusIds.put(mAccountId.getId(), mStatusId);
            bus.post(new StatusListChangedEvent());
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableStatus> result) {
            mDestroyingStatusIds.remove(mAccountId.getId(), mStatusId);
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

        private final AccountId mAccountId;
        private final long mListId;

        public DestroyUserListSubscriptionTask(@NonNull final AccountId accountId, final long listId) {
            super(mContext);
            mAccountId = accountId;
            mListId = listId;
        }

        @Override
        protected SingleResponse<ParcelableUserList> doInBackground(final Object... params) {

            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final UserList userList = twitter.destroyUserListSubscription(mListId);
                final ParcelableUserList list = ParcelableUserListUtils.from(userList, mAccountId);
                return SingleResponse.getInstance(list, null);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(null, e);
            }
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

        private final AccountId mAccountId;
        private final long mListId;

        public DestroyUserListTask(Context context, final AccountId accountId, final long listId) {
            super(context);
            mAccountId = accountId;
            mListId = listId;
        }

        @Override
        protected SingleResponse<ParcelableUserList> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(getContext(), mAccountId,
                    false);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final UserList userList = twitter.destroyUserList(mListId);
                final ParcelableUserList list = ParcelableUserListUtils.from(userList, mAccountId);
                return SingleResponse.getInstance(list);
            } catch (final TwitterException e) {
                return SingleResponse.getInstance(e);
            }
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
        public void beforeExecute() {
            final Intent intent = new Intent(BROADCAST_RESCHEDULE_DIRECT_MESSAGES_REFRESHING);
            context.sendBroadcast(intent);
            super.beforeExecute();
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

    class ReportSpamTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUser>> {

        private final AccountId mAccountId;
        private final long mUserId;

        public ReportSpamTask(final AccountId accountId, final long userId) {
            super(mContext);
            this.mAccountId = accountId;
            this.mUserId = userId;
        }

        @Override
        protected SingleResponse<ParcelableUser> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, mAccountId, false);
            if (twitter != null) {
                try {
                    final User user = twitter.reportSpam(mUserId);
                    return SingleResponse.getInstance(ParcelableUserUtils.fromUser(user, mAccountId), null);
                } catch (final TwitterException e) {
                    return SingleResponse.getInstance(null, e);
                }
            }
            return SingleResponse.getInstance();
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
            if (result.hasData()) {
                // TODO delete cached status
                Utils.showInfoMessage(mContext, R.string.reported_user_for_spam, false);


                bus.post(new FriendshipUserUpdatedEvent(result.getData()));
            } else {
                Utils.showErrorMessage(mContext, R.string.action_reporting_for_spam, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }

    class RetweetStatusTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableStatus>> {

        private final AccountId accountId;
        private final long statusId;

        public RetweetStatusTask(@NonNull final AccountId accountId, final long statusId) {
            super(mContext);
            this.accountId = accountId;
            this.statusId = statusId;
        }

        @Override
        protected SingleResponse<ParcelableStatus> doInBackground(final Object... params) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(mContext, accountId, true);
            if (twitter == null) {
                return SingleResponse.getInstance();
            }
            try {
                final ParcelableStatus status = ParcelableStatusUtils.fromStatus(twitter.retweetStatus(statusId),
                        accountId, false);
                Utils.setLastSeen(mContext, status.mentions, System.currentTimeMillis());
                final ContentValues values = new ContentValues();
                values.put(Statuses.MY_RETWEET_ID, status.id);
                values.put(Statuses.REPLY_COUNT, status.reply_count);
                values.put(Statuses.RETWEET_COUNT, status.retweet_count);
                values.put(Statuses.FAVORITE_COUNT, status.favorite_count);
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
            mCreatingRetweetIds.put(accountId.getId(), statusId);
            bus.post(new StatusListChangedEvent());
        }

        @Override
        protected void onPostExecute(final SingleResponse<ParcelableStatus> result) {
            mCreatingRetweetIds.remove(accountId.getId(), statusId);
            if (result.hasData()) {
                final ParcelableStatus status = result.getData();
                // BEGIN HotMobi
                final TweetEvent event = TweetEvent.create(getContext(), status, TimelineType.OTHER);
                event.setAction(TweetEvent.Action.RETWEET);
                HotMobiLogger.getInstance(getContext()).log(accountId.getId(), event);
                // END HotMobi

                bus.post(new StatusRetweetedEvent(status));
            } else {
                Utils.showErrorMessage(mContext, R.string.action_retweeting, result.getException(), true);
            }
            super.onPostExecute(result);
        }

    }


    static class UpdateUserListDetailsTask extends ManagedAsyncTask<Object, Object, SingleResponse<ParcelableUserList>> {

        private final AccountId accountId;
        private final long listId;
        private final UserListUpdate update;
        private Context mContext;

        public UpdateUserListDetailsTask(Context context, final AccountId accountId,
                                         final long listId, UserListUpdate update) {
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
                    return SingleResponse.getInstance(ParcelableUserListUtils.from(list, accountId));
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
