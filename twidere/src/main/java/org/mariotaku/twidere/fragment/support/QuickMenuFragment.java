package org.mariotaku.twidere.fragment.support;

import static org.mariotaku.twidere.util.Utils.getTableNameByUri;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.content.TwidereContextThemeWrapper;
import org.mariotaku.twidere.fragment.support.TrendsSuggectionsFragment.TrendsAdapter;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;

public class QuickMenuFragment extends BaseSupportFragment {

	private SharedPreferences mPreferences;
	private Context mThemedContext;
	private ListView mListView;
	private SlidingUpPanelLayout mSlidingUpPanel;

	private MergeAdapter mAdapter;
	private TrendsAdapter mTrendsAdapter;

	private static final int LOADER_ID_TRENDS = 1;

	private final LoaderCallbacks<Cursor> mTrendsCallback = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
			final Uri uri = CachedTrends.Local.CONTENT_URI;
			final String table = getTableNameByUri(uri);
			final String where = table != null ? CachedTrends.TIMESTAMP + " = " + "(SELECT " + CachedTrends.TIMESTAMP
					+ " FROM " + table + " ORDER BY " + CachedTrends.TIMESTAMP + " DESC LIMIT 1)" : null;
			return new CursorLoader(getActivity(), uri, CachedTrends.COLUMNS, where, null, null);
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {
			mTrendsAdapter.swapCursor(null);
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
			mTrendsAdapter.swapCursor(data);
		}

	};

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (mPreferences.getBoolean(KEY_QUICK_MENU_EXPANDED, false)) {
		} else {
		}
		mAdapter = new MergeAdapter();
		mTrendsAdapter = new TrendsAdapter(getThemedContext());
		mAdapter.addAdapter(mTrendsAdapter);
		mListView.setAdapter(mAdapter);
		getLoaderManager().initLoader(LOADER_ID_TRENDS, null, mTrendsCallback);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return LayoutInflater.from(getThemedContext()).inflate(R.layout.fragment_quick_menu, container, false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.putBoolean(KEY_QUICK_MENU_EXPANDED, mSlidingUpPanel.isExpanded());
		editor.apply();
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mListView = (ListView) view.findViewById(android.R.id.list);
		mSlidingUpPanel = (SlidingUpPanelLayout) view.findViewById(R.id.activities_drawer);
		final View activitiesContainer = view.findViewById(R.id.activities_container);
		ViewAccessor.setBackground(activitiesContainer, ThemeUtils.getWindowBackground(getThemedContext()));
	}

	private Context getThemedContext() {
		if (mThemedContext != null) return mThemedContext;
		final Context context = getActivity();
		if (!ThemeUtils.isDarkDrawerEnabled(context)) return mThemedContext = context;
		final int themeResource = ThemeUtils.getDrawerThemeResource(context);
		final int accentColor = ThemeUtils.getUserThemeColor(context);
		return mThemedContext = new TwidereContextThemeWrapper(context, themeResource, accentColor);
	}

}
