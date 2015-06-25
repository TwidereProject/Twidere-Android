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
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.db.chart.model.ChartSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.StackBarChartView;
import com.desmond.asyncmanager.AsyncManager;
import com.desmond.asyncmanager.TaskRunnable;

import org.apache.commons.lang3.tuple.Triple;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.NetworkUsageInfo;
import org.mariotaku.twidere.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by mariotaku on 15/6/24.
 */
public class NetworkUsageSummaryPreferences extends Preference {

    private StackBarChartView mChartView;
    private NetworkUsageInfo mUsage;
    private TextView mTotalUsage;

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
        mChartView = (StackBarChartView) view.findViewById(R.id.chart);
        mTotalUsage = (TextView) view.findViewById(R.id.total_usage);
        mChartView.setXLabels(AxisController.LabelPosition.NONE);
        mChartView.setYLabels(AxisController.LabelPosition.NONE);
        return view;
    }

    private void getUsageInfo() {
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
        final Date start = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));
        final Date end = cal.getTime();
        TaskRunnable<Triple<Context, Date, Date>, NetworkUsageInfo, NetworkUsageSummaryPreferences> task;
        task = new TaskRunnable<Triple<Context, Date, Date>, NetworkUsageInfo, NetworkUsageSummaryPreferences>() {
            @Override
            public NetworkUsageInfo doLongOperation(Triple<Context, Date, Date> params) throws InterruptedException {
                return NetworkUsageInfo.get(params.getLeft(), params.getMiddle(), params.getRight());
            }

            @Override
            public void callback(NetworkUsageSummaryPreferences handler, NetworkUsageInfo result) {
                handler.setUsage(result);
            }
        };
        task.setResultHandler(this);
        task.setParams(Triple.of(getContext(), start, end));
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
        final ArrayList<ChartSet> chartData = usage.getChartData();
        if (mChartView.getData() != chartData) {
            mChartView.addData(chartData);
            mChartView.show();
        }
        mTotalUsage.setText(Utils.calculateProperSize((usage.getTotalSent() + usage.getTotalReceived()) * 1024));
    }

}
