package org.mariotaku.twidere.activity.support;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ResolveInfoListAdapter;
import org.mariotaku.twidere.loader.support.IntentActivitiesLoader;

import java.util.List;

public class ActivityPickerActivity extends BaseSupportDialogActivity implements LoaderCallbacks<List<ResolveInfo>>,
		OnItemClickListener {

	private ResolveInfoListAdapter mAdapter;

	private ListView mListView;

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mListView = (ListView) findViewById(android.R.id.list);
	}

	@Override
	public Loader<List<ResolveInfo>> onCreateLoader(final int id, final Bundle args) {
		final Intent intent = getIntent();
		final Intent extraIntent = intent.getParcelableExtra(EXTRA_INTENT);

		final String[] blacklist = intent.getStringArrayExtra(EXTRA_BLACKLIST);
		return new IntentActivitiesLoader(this, extraIntent, blacklist, 0);
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		final Intent intent = getIntent(), data = new Intent();
		data.putExtra(EXTRA_DATA, mAdapter.getItem(position));
		data.putExtra(EXTRA_INTENT, intent.getParcelableExtra(EXTRA_INTENT));
		setResult(RESULT_OK, data);
		finish();
	}

	@Override
	public void onLoaderReset(final Loader<List<ResolveInfo>> loader) {
		mAdapter.clear();
	}

	@Override
	public void onLoadFinished(final Loader<List<ResolveInfo>> loader, final List<ResolveInfo> data) {
		mAdapter.clear();
		if (data != null) {
			mAdapter.addAll(data);
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity_picker);
		mAdapter = new ResolveInfoListAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		getSupportLoaderManager().initLoader(0, null, this);
	}

}
