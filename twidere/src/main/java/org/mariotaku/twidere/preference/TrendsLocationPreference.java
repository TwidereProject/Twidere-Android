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

package org.mariotaku.twidere.preference;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Location;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.mariotaku.twidere.TwidereConstants.LOGTAG;

public class TrendsLocationPreference extends Preference {

    private static final long EMPTY = 0;
    private static final long WORLDWIDE = 1;
    private final ExpandableTrendLocationsListAdapter mAdapter;
    private GetAvailableTrendsTask mGetAvailableTrendsTask;
    private AlertDialog mDialog;

    public TrendsLocationPreference(final Context context) {
        this(context, null);
    }

    public TrendsLocationPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    public TrendsLocationPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mAdapter = new ExpandableTrendLocationsListAdapter(context);
    }

    @Override
    protected void onClick() {
        if (mGetAvailableTrendsTask != null) {
            mGetAvailableTrendsTask.cancel(false);
        }
        mGetAvailableTrendsTask = new GetAvailableTrendsTask(getContext());
        mGetAvailableTrendsTask.execute();
    }

    static class ExpandableTrendLocationsListAdapter extends BaseExpandableListAdapter {

        private final LayoutInflater mInflater;
        @Nullable
        SimpleArrayMap<Location, List<Location>> mData;

        public ExpandableTrendLocationsListAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getGroupCount() {
            if (mData == null) return 0;
            return mData.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (mData == null) return 0;
            return mData.valueAt(groupPosition).size();
        }

        @Override
        public Location getGroup(int groupPosition) {
            assert mData != null;
            return mData.keyAt(groupPosition);
        }

        @Override
        public Location getChild(int groupPosition, int childPosition) {
            assert mData != null;
            return mData.valueAt(groupPosition).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return getGroup(groupPosition).getWoeid();
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return getChild(groupPosition, childPosition).getWoeid();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(android.R.layout.simple_expandable_list_item_1, parent, false);
            }
            ((TextView) view.findViewById(android.R.id.text1)).setText(getGroup(groupPosition).getName());
            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            final Location location = getChild(groupPosition, childPosition);
            final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            if (location.getParentId() == 1) {
                text1.setText(R.string.location_countrywide);
            } else {
                text1.setText(location.getName());
            }
            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public void setData(@Nullable SimpleArrayMap<Location, List<Location>> data) {
            mData = data;
            notifyDataSetChanged();
        }
    }

    static class LocationsMap {

        final LongSparseArray<List<Location>> map = new LongSparseArray<>();
        final LongSparseArray<Location> parents = new LongSparseArray<>();
        private final LocationComparator comparator;

        LocationsMap(Locale locale) {
            comparator = new LocationComparator(Collator.getInstance(locale));
        }

        void put(Location location) {
            final long parentId = location.getParentId();
            if (parentId == EMPTY || parentId == WORLDWIDE) {
                putParent(location);
            } else {
                putChild(parentId, location);
            }
        }

        void putParent(Location location) {
            final long woeid = location.getWoeid();
            parents.put(woeid, location);
            final List<Location> list = getList(woeid);
            // Don't add child for 'worldwide'
            if (woeid != WORLDWIDE) {
                addToList(list, location);
            }
        }

        void putChild(long parentId, Location location) {
            addToList(getList(parentId), location);
        }

        List<Location> getList(long parentId) {
            List<Location> list = map.get(parentId);
            if (list == null) {
                list = new ArrayList<>();
                map.put(parentId, list);
            }
            return list;
        }

        void addToList(List<Location> list, Location location) {
            int loc = Collections.binarySearch(list, location, comparator);
            if (loc < 0) {
                list.add(-(loc + 1), location);
            }
        }

        SimpleArrayMap<Location, List<Location>> pack() {
            SimpleArrayMap<Location, List<Location>> result = new SimpleArrayMap<>(map.size());
            for (int i = 0, j = map.size(); i < j; i++) {
                Location parent = parents.get(map.keyAt(i));
                if (parent == null) continue;
                result.put(parent, map.valueAt(i));
            }
            return result;
        }
    }

    private static class LocationComparator implements Comparator<Location> {
        private final Collator collator;

        public LocationComparator(Collator collator) {
            this.collator = collator;
        }

        private boolean isCountryOrWorldwide(Location location) {
            final long parentId = location.getParentId();
            return parentId == 0 || parentId == 1;
        }

        @Override
        public int compare(Location lhs, Location rhs) {
            if (isCountryOrWorldwide(lhs)) return Integer.MIN_VALUE;
            if (isCountryOrWorldwide(rhs)) return Integer.MAX_VALUE;
            return collator.compare(lhs.getName(), rhs.getName());
        }
    }

    class GetAvailableTrendsTask extends AsyncTask<Object, Object, SimpleArrayMap<Location,
            List<Location>>> implements OnCancelListener {
        private final ProgressDialog mProgress;

        public GetAvailableTrendsTask(final Context context) {
            mProgress = new ProgressDialog(context);
        }

        @Override
        public void onCancel(final DialogInterface dialog) {
            cancel(true);
        }

        @Override
        protected SimpleArrayMap<Location, List<Location>> doInBackground(final Object... args) {
            final MicroBlog twitter = MicroBlogAPIFactory.getDefaultTwitterInstance(getContext(), false);
            if (twitter == null) return null;
            try {
                LocationsMap map = new LocationsMap(Locale.getDefault());
                for (Location location : twitter.getAvailableTrends()) {
                    map.put(location);
                }
                return map.pack();
            } catch (final MicroBlogException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(final SimpleArrayMap<Location, List<Location>> result) {
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
            mAdapter.setData(result);
            if (result == null) return;
            final AlertDialog.Builder selectorBuilder = new AlertDialog.Builder(getContext());
            selectorBuilder.setTitle(getTitle());
            selectorBuilder.setView(R.layout.dialog_trends_location_selector);
            selectorBuilder.setNegativeButton(android.R.string.cancel, null);
            mDialog = selectorBuilder.create();
            mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    final Dialog dialog = (Dialog) dialogInterface;
                    final ExpandableListView listView = (ExpandableListView) dialog.findViewById(R.id.expandable_list);
                    listView.setAdapter(mAdapter);
                    listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                        @Override
                        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                            final Location group = mAdapter.getGroup(groupPosition);
                            if (group.getWoeid() == WORLDWIDE) {
                                persistInt(group.getWoeid());
                                dialog.dismiss();
                                return true;
                            }
                            return false;
                        }
                    });
                    listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                        @Override
                        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                            final Location child = mAdapter.getChild(groupPosition, childPosition);
                            persistInt(child.getWoeid());
                            dialog.dismiss();
                            return true;
                        }
                    });
                }
            });
            mDialog.show();
        }

        @Override
        protected void onPreExecute() {
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
            mProgress.setMessage(getContext().getString(R.string.please_wait));
            mProgress.setOnCancelListener(this);
            mProgress.show();
        }

    }
}
