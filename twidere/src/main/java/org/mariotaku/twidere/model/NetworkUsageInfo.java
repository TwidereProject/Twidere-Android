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

package org.mariotaku.twidere.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;

import com.db.chart.model.BarSet;
import com.db.chart.model.ChartSet;

import org.mariotaku.querybuilder.Expression;
import org.mariotaku.twidere.preference.NetworkUsageSummaryPreferences;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.util.MathUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by mariotaku on 15/6/25.
 */
public class NetworkUsageInfo {
    private final double totalSent;
    private final double totalReceived;
    private final ArrayList<ChartSet> chartData;
    private final double dayMax;

    public NetworkUsageInfo(ArrayList<ChartSet> chartData, double totalReceived, double totalSent, double dayMax) {
        this.chartData = chartData;
        this.totalReceived = totalReceived;
        this.totalSent = totalSent;
        this.dayMax = dayMax;
    }

    public static NetworkUsageInfo get(Context context, Date start, Date end) {
        final ContentResolver cr = context.getContentResolver();
        final long startTime = TimeUnit.HOURS.convert(start.getTime(), TimeUnit.MILLISECONDS);
        final long endTime = TimeUnit.HOURS.convert(end.getTime(), TimeUnit.MILLISECONDS);
        final Expression where = Expression.and(Expression.greaterEquals(TwidereDataStore.NetworkUsages.TIME_IN_HOURS, startTime),
                Expression.lesserThan(TwidereDataStore.NetworkUsages.TIME_IN_HOURS, endTime));
        final int days = (int) TimeUnit.DAYS.convert(endTime - startTime, TimeUnit.HOURS);
        final Cursor c = cr.query(TwidereDataStore.NetworkUsages.CONTENT_URI, TwidereDataStore.NetworkUsages.COLUMNS,
                where.getSQL(), null, TwidereDataStore.NetworkUsages.TIME_IN_HOURS);
        final int idxDate = c.getColumnIndex(TwidereDataStore.NetworkUsages.TIME_IN_HOURS);
        final int idxSent = c.getColumnIndex(TwidereDataStore.NetworkUsages.KILOBYTES_SENT);
        final int idxReceived = c.getColumnIndex(TwidereDataStore.NetworkUsages.KILOBYTES_RECEIVED);
        final int idxType = c.getColumnIndex(TwidereDataStore.NetworkUsages.REQUEST_TYPE);
        final double[][] usageArray = new double[days][RequestType.values().length];
        double totalReceived = 0, totalSent = 0;
        c.moveToFirst();
        while (!c.isAfterLast()) {
            final long hours = c.getLong(idxDate);
            final int idx = (int) TimeUnit.DAYS.convert((hours - startTime), TimeUnit.HOURS);
            final double sent = c.getDouble(idxSent);
            final double received = c.getDouble(idxReceived);
            final String type = c.getString(idxType);
            usageArray[idx][RequestType.getValue(type)] += (sent + received);
            totalReceived += received;
            totalSent += sent;
            c.moveToNext();
        }
        c.close();

        final BarSet apiSet = new BarSet();
        final BarSet mediaSet = new BarSet();
        final BarSet usageStatisticsSet = new BarSet();

        double dayMax = 0;
        for (int i = 0; i < days; i++) {
            String day = String.valueOf(i + 1);
            final double[] dayUsage = usageArray[i];
            apiSet.addBar(day, (float) dayUsage[RequestType.API.getValue()]);
            mediaSet.addBar(day, (float) dayUsage[RequestType.MEDIA.getValue()]);
            usageStatisticsSet.addBar(day, (float) dayUsage[RequestType.USAGE_STATISTICS.getValue()]);
            dayMax = Math.max(dayMax, MathUtils.sum(dayUsage));
        }

        apiSet.setColor(Color.RED);
        mediaSet.setColor(Color.GREEN);
        usageStatisticsSet.setColor(Color.BLUE);

        final ArrayList<ChartSet> data = new ArrayList<>();
        data.add(apiSet);
        data.add(mediaSet);
        data.add(usageStatisticsSet);
        return new NetworkUsageInfo(data, totalReceived, totalSent, dayMax);
    }

    public double getDayMax() {
        return dayMax;
    }

    public double getTotalSent() {
        return totalSent;
    }

    public double getTotalReceived() {
        return totalReceived;
    }

    public ArrayList<ChartSet> getChartData() {
        return chartData;
    }
}
