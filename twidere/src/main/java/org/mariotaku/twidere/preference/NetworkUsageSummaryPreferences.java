/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.preference;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.db.chart.model.BarSet;
import com.desmond.asyncmanager.AsyncManager;
import com.desmond.asyncmanager.TaskRunnable;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.NetworkUsageInfo;
import org.mariotaku.twidere.model.RequestType;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.Utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by mariotaku on 15/6/24.
 */
public class NetworkUsageSummaryPreferences extends Preference {

    private NetworkUsageInfo mUsage;
    private TextView mTotalUsage, mTotalUsageSent, mTotalUsageReceived;
    private TextView mDayUsageMax;
    private TextView mDayMin, mDayMid, mDayMax;

    public NetworkUsageSummaryPreferences(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.layout_preference_network_usage);
        getUsageInfo();
    }

    public NetworkUsageSummaryPreferences(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    public NetworkUsageSummaryPreferences(Context context) {
        this(context, null);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        final View view = super.onCreateView(parent);
        mTotalUsage = (TextView) view.findViewById(R.id.total_usage);
        mTotalUsageSent = (TextView) view.findViewById(R.id.total_usage_sent);
        mTotalUsageReceived = (TextView) view.findViewById(R.id.total_usage_received);
        mDayUsageMax = (TextView) view.findViewById(R.id.day_usage_max);
        mDayMin = (TextView) view.findViewById(R.id.day_min);
        mDayMid = (TextView) view.findViewById(R.id.day_mid);
        mDayMax = (TextView) view.findViewById(R.id.day_max);

        return view;
    }

    private void getUsageInfo() {
        final Calendar now = Calendar.getInstance();
        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
        cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
        final int dayMin = now.getActualMinimum(Calendar.DAY_OF_MONTH);
        final int dayMax = now.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, dayMin);
        cal.setTimeZone(now.getTimeZone());
        final Date start = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, dayMax);
        cal.add(Calendar.DATE, 1);
        final Date end = cal.getTime();
        final TaskRunnable<Object[], NetworkUsageInfo, NetworkUsageSummaryPreferences> task;
        task = new TaskRunnable<Object[], NetworkUsageInfo, NetworkUsageSummaryPreferences>() {
            @Override
            public NetworkUsageInfo doLongOperation(Object[] params) throws InterruptedException {
                final int[] network = {ConnectivityManager.TYPE_MOBILE};
                return NetworkUsageInfo.get((Context) params[0], (Date) params[1], (Date) params[2], dayMin, dayMax, network);
            }

            @Override
            public void callback(NetworkUsageSummaryPreferences handler, NetworkUsageInfo result) {
                handler.setUsage(result);
            }
        };
        task.setResultHandler(this);
        task.setParams(new Object[]{getContext(), start, end});
        AsyncManager.runBackgroundTask(task);
    }

    private void setUsage(NetworkUsageInfo usage) {
        mUsage = usage;
        notifyChanged();
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        final NetworkUsageInfo usage = mUsage;
        if (usage == null) return;
        final double[][] chartUsage = usage.getChartUsage();
        final int days = chartUsage.length;

        final BarSet apiSet = new BarSet();
        final BarSet mediaSet = new BarSet();
        final BarSet usageStatisticsSet = new BarSet();

        double dayUsageMax = 0;
        for (int i = 0; i < days; i++) {
            String day = String.valueOf(i + 1);
            final double[] dayUsage = chartUsage[i];
            apiSet.addBar(day, (float) dayUsage[RequestType.API.getValue()]);
            mediaSet.addBar(day, (float) dayUsage[RequestType.MEDIA.getValue()]);
            usageStatisticsSet.addBar(day, (float) dayUsage[RequestType.USAGE_STATISTICS.getValue()]);
            dayUsageMax = Math.max(dayUsageMax, MathUtils.sum(dayUsage));
        }

        apiSet.setColor(Color.RED);
        mediaSet.setColor(Color.GREEN);
        usageStatisticsSet.setColor(Color.BLUE);

        mTotalUsage.setText(Utils.calculateProperSize((usage.getTotalSent() + usage.getTotalReceived()) * 1024));
        mTotalUsageSent.setText(Utils.calculateProperSize(usage.getTotalSent() * 1024));
        mTotalUsageReceived.setText(Utils.calculateProperSize(usage.getTotalReceived() * 1024));
        mDayUsageMax.setText(Utils.calculateProperSize((usage.getDayUsageMax()) * 1024));
        mDayMin.setText(String.valueOf(usage.getDayMin()));
        mDayMid.setText(String.valueOf((usage.getDayMin() + usage.getDayMax()) / 2));
        mDayMax.setText(String.valueOf(usage.getDayMax()));
    }

}
