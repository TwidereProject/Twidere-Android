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

package org.mariotaku.twidere.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.AccountPreferences;
import org.mariotaku.twidere.model.SimpleRefreshTaskParam;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.receiver.PowerStateReceiver;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import java.util.Arrays;

import javax.inject.Inject;

import edu.tsinghua.hotmobi.model.BatteryRecord;
import edu.tsinghua.hotmobi.model.ScreenEvent;

public class RefreshService extends Service implements Constants {

    @Inject
    SharedPreferencesWrapper mPreferences;

    private AlarmManager mAlarmManager;
    @Inject
    AsyncTwitterWrapper mTwitterWrapper;
    private PendingIntent mPendingRefreshHomeTimelineIntent, mPendingRefreshMentionsIntent,
            mPendingRefreshDirectMessagesIntent, mPendingRefreshTrendsIntent;

    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (BuildConfig.DEBUG) {
                Log.d(LOGTAG, String.format("Refresh service received action %s", action));
            }
            switch (action) {
                case BROADCAST_RESCHEDULE_HOME_TIMELINE_REFRESHING: {
                    rescheduleHomeTimelineRefreshing();
                    break;
                }
                case BROADCAST_RESCHEDULE_MENTIONS_REFRESHING: {
                    rescheduleMentionsRefreshing();
                    break;
                }
                case BROADCAST_RESCHEDULE_DIRECT_MESSAGES_REFRESHING: {
                    rescheduleDirectMessagesRefreshing();
                    break;
                }
                case BROADCAST_RESCHEDULE_TRENDS_REFRESHING: {
                    rescheduleTrendsRefreshing();
                    break;
                }
                case BROADCAST_REFRESH_HOME_TIMELINE: {
                    if (isAutoRefreshAllowed()) {
                        mTwitterWrapper.getHomeTimelineAsync(new SimpleRefreshTaskParam() {
                            private UserKey[] accountIds;

                            @NonNull
                            @Override
                            public UserKey[] getAccountKeysWorker() {
                                if (accountIds != null) return accountIds;
                                final AccountPreferences[] prefs = AccountPreferences.getAccountPreferences(context,
                                        DataStoreUtils.getAccountKeys(context));
                                return accountIds = getRefreshableIds(prefs, HomeRefreshableFilter.INSTANCE);
                            }

                            @Nullable
                            @Override
                            public String[] getSinceIds() {
                                return DataStoreUtils.getNewestStatusIds(context,
                                        Statuses.CONTENT_URI, getAccountKeys());
                            }
                        });
                    }
                    break;
                }
                case BROADCAST_REFRESH_NOTIFICATIONS: {
                    if (isAutoRefreshAllowed()) {
                        mTwitterWrapper.getActivitiesAboutMeAsync(new SimpleRefreshTaskParam() {
                            private UserKey[] accountIds;

                            @NonNull
                            @Override
                            public UserKey[] getAccountKeysWorker() {
                                if (accountIds != null) return accountIds;
                                final AccountPreferences[] prefs = AccountPreferences.getAccountPreferences(context,
                                        DataStoreUtils.getAccountKeys(context));
                                return accountIds = getRefreshableIds(prefs, MentionsRefreshableFilter.INSTANCE);
                            }

                            @Nullable
                            @Override
                            public String[] getSinceIds() {
                                return DataStoreUtils.getNewestActivityMaxPositions(context,
                                        Activities.AboutMe.CONTENT_URI, getAccountKeys());
                            }
                        });
                    }
                    break;
                }
                case BROADCAST_REFRESH_DIRECT_MESSAGES: {
                    if (isAutoRefreshAllowed()) {
                        mTwitterWrapper.getReceivedDirectMessagesAsync(new SimpleRefreshTaskParam() {
                            private UserKey[] accountIds;

                            @NonNull
                            @Override
                            public UserKey[] getAccountKeysWorker() {
                                if (accountIds != null) return accountIds;
                                final AccountPreferences[] prefs = AccountPreferences.getAccountPreferences(context,
                                        DataStoreUtils.getAccountKeys(context));
                                return accountIds = getRefreshableIds(prefs, MessagesRefreshableFilter.INSTANCE);
                            }

                            @Nullable
                            @Override
                            public String[] getSinceIds() {
                                return DataStoreUtils.getNewestMessageIds(context,
                                        DirectMessages.Inbox.CONTENT_URI, getAccountKeys());
                            }
                        });
                    }
                    break;
                }
                case BROADCAST_REFRESH_TRENDS: {
                    if (isAutoRefreshAllowed()) {
                        final AccountPreferences[] prefs = AccountPreferences.getAccountPreferences(context,
                                DataStoreUtils.getAccountKeys(context));
                        final UserKey[] refreshIds = getRefreshableIds(prefs, TrendsRefreshableFilter.INSTANCE);
                        if (BuildConfig.DEBUG) {
                            Log.d(LOGTAG, String.format("Auto refreshing trends for %s", Arrays.toString(refreshIds)));
                        }
                        getLocalTrends(refreshIds);
                        break;
                    }
                }
            }
        }

    };

    private final BroadcastReceiver mPowerStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_BATTERY_CHANGED: {
                    BatteryRecord.log(context, intent);
                    break;
                }
                default: {
                    BatteryRecord.log(context);
                    break;
                }
            }
        }
    };

    private final BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        public long mPresentTime = -1;

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_SCREEN_ON: {
                    ScreenEvent.log(context, ScreenEvent.Action.ON, getPresentDuration());
                    break;
                }
                case Intent.ACTION_SCREEN_OFF: {
                    ScreenEvent.log(context, ScreenEvent.Action.OFF, getPresentDuration());
                    mPresentTime = -1;
                    break;
                }
                case Intent.ACTION_USER_PRESENT: {
                    mPresentTime = SystemClock.elapsedRealtime();
                    ScreenEvent.log(context, ScreenEvent.Action.PRESENT, -1);
                    break;
                }
            }
        }

        private long getPresentDuration() {
            if (mPresentTime < 0) return -1;
            return SystemClock.elapsedRealtime() - mPresentTime;
        }
    };

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        GeneralComponentHelper.build(this).inject(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        mPendingRefreshHomeTimelineIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                BROADCAST_REFRESH_HOME_TIMELINE), 0);
        mPendingRefreshMentionsIntent = PendingIntent.getBroadcast(this, 0, new Intent(BROADCAST_REFRESH_NOTIFICATIONS), 0);
        mPendingRefreshDirectMessagesIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                BROADCAST_REFRESH_DIRECT_MESSAGES), 0);
        mPendingRefreshTrendsIntent = PendingIntent.getBroadcast(this, 0, new Intent(BROADCAST_REFRESH_TRENDS), 0);
        final IntentFilter refreshFilter = new IntentFilter(BROADCAST_NOTIFICATION_DELETED);
        refreshFilter.addAction(BROADCAST_REFRESH_HOME_TIMELINE);
        refreshFilter.addAction(BROADCAST_REFRESH_NOTIFICATIONS);
        refreshFilter.addAction(BROADCAST_REFRESH_DIRECT_MESSAGES);
        refreshFilter.addAction(BROADCAST_RESCHEDULE_HOME_TIMELINE_REFRESHING);
        refreshFilter.addAction(BROADCAST_RESCHEDULE_MENTIONS_REFRESHING);
        refreshFilter.addAction(BROADCAST_RESCHEDULE_DIRECT_MESSAGES_REFRESHING);
        registerReceiver(mStateReceiver, refreshFilter);
        final IntentFilter batteryFilter = new IntentFilter();
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryFilter.addAction(Intent.ACTION_BATTERY_OKAY);
        batteryFilter.addAction(Intent.ACTION_BATTERY_LOW);
        batteryFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        batteryFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mPowerStateReceiver, batteryFilter);
        registerReceiver(mScreenStateReceiver, screenFilter);
        PowerStateReceiver.setServiceReceiverStarted(true);
        if (Utils.hasAutoRefreshAccounts(this)) {
            startAutoRefresh();
        } else {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        PowerStateReceiver.setServiceReceiverStarted(false);
        unregisterReceiver(mScreenStateReceiver);
        unregisterReceiver(mPowerStateReceiver);
        unregisterReceiver(mStateReceiver);
        if (Utils.hasAutoRefreshAccounts(this)) {
            // Auto refresh enabled, so I will try to start service after it was
            // stopped.
            startService(new Intent(this, getClass()));
        }
        super.onDestroy();
    }

    protected boolean isAutoRefreshAllowed() {
        return Utils.isNetworkAvailable(this) && (Utils.isBatteryOkay(this) || !Utils.shouldStopAutoRefreshOnBatteryLow(this));
    }

    private void getLocalTrends(final UserKey[] accountIds) {
        final UserKey account_id = Utils.getDefaultAccountKey(this);
        final int woeid = mPreferences.getInt(KEY_LOCAL_TRENDS_WOEID, 1);
        mTwitterWrapper.getLocalTrendsAsync(account_id, woeid);
    }

    private UserKey[] getRefreshableIds(final AccountPreferences[] prefs, final RefreshableAccountFilter filter) {
        if (prefs == null) return null;
        final UserKey[] temp = new UserKey[prefs.length];
        int i = 0;
        for (final AccountPreferences pref : prefs) {
            if (pref.isAutoRefreshEnabled() && filter.isRefreshable(pref)) {
                temp[i++] = pref.getAccountKey();
            }
        }
        final UserKey[] result = new UserKey[i];
        System.arraycopy(temp, 0, result, 0, i);
        return result;
    }

    private long getRefreshInterval() {
        if (mPreferences == null) return 0;
        final int prefValue = NumberUtils.toInt(mPreferences.getString(KEY_REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL), -1);
        return Math.max(prefValue, 3) * 60 * 1000;
    }

    private void rescheduleDirectMessagesRefreshing() {
        mAlarmManager.cancel(mPendingRefreshDirectMessagesIntent);
        final long refreshInterval = getRefreshInterval();
        if (refreshInterval > 0) {
            mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + refreshInterval,
                    refreshInterval, mPendingRefreshDirectMessagesIntent);
        }
    }

    private void rescheduleHomeTimelineRefreshing() {
        mAlarmManager.cancel(mPendingRefreshHomeTimelineIntent);
        final long refreshInterval = getRefreshInterval();
        if (refreshInterval > 0) {
            mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + refreshInterval,
                    refreshInterval, mPendingRefreshHomeTimelineIntent);
        }
    }

    private void rescheduleMentionsRefreshing() {
        mAlarmManager.cancel(mPendingRefreshMentionsIntent);
        final long refreshInterval = getRefreshInterval();
        if (refreshInterval > 0) {
            mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + refreshInterval,
                    refreshInterval, mPendingRefreshMentionsIntent);
        }
    }

    private void rescheduleTrendsRefreshing() {
        mAlarmManager.cancel(mPendingRefreshTrendsIntent);
        final long refreshInterval = getRefreshInterval();
        if (refreshInterval > 0) {
            mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + refreshInterval,
                    refreshInterval, mPendingRefreshTrendsIntent);
        }
    }

    private boolean startAutoRefresh() {
        stopAutoRefresh();
        final long refreshInterval = getRefreshInterval();
        if (refreshInterval <= 0) return false;
        rescheduleHomeTimelineRefreshing();
        rescheduleMentionsRefreshing();
        rescheduleDirectMessagesRefreshing();
        rescheduleTrendsRefreshing();
        return true;
    }

    private void stopAutoRefresh() {
        mAlarmManager.cancel(mPendingRefreshHomeTimelineIntent);
        mAlarmManager.cancel(mPendingRefreshMentionsIntent);
        mAlarmManager.cancel(mPendingRefreshDirectMessagesIntent);
        mAlarmManager.cancel(mPendingRefreshTrendsIntent);
    }

    private interface RefreshableAccountFilter {
        boolean isRefreshable(AccountPreferences pref);
    }

    private static class HomeRefreshableFilter implements RefreshableAccountFilter {
        public static final RefreshableAccountFilter INSTANCE = new HomeRefreshableFilter();

        @Override
        public boolean isRefreshable(final AccountPreferences pref) {
            return pref.isAutoRefreshHomeTimelineEnabled();
        }
    }

    private static class MentionsRefreshableFilter implements RefreshableAccountFilter {

        static final RefreshableAccountFilter INSTANCE = new MentionsRefreshableFilter();

        @Override
        public boolean isRefreshable(final AccountPreferences pref) {
            return pref.isAutoRefreshMentionsEnabled();
        }

    }

    private static class MessagesRefreshableFilter implements RefreshableAccountFilter {
        public static final RefreshableAccountFilter INSTANCE = new MentionsRefreshableFilter();

        @Override
        public boolean isRefreshable(final AccountPreferences pref) {
            return pref.isAutoRefreshDirectMessagesEnabled();
        }
    }

    private static class TrendsRefreshableFilter implements RefreshableAccountFilter {
        public static final RefreshableAccountFilter INSTANCE = new TrendsRefreshableFilter();

        @Override
        public boolean isRefreshable(final AccountPreferences pref) {
            return pref.isAutoRefreshTrendsEnabled();
        }
    }
}
