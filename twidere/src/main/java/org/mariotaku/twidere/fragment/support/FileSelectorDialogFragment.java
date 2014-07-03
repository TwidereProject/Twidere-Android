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

package org.mariotaku.twidere.fragment.support;

import static android.os.Environment.getExternalStorageDirectory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.fragment.iface.ISupportDialogFragmentCallback;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.ThemeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class FileSelectorDialogFragment extends BaseSupportDialogFragment implements LoaderCallbacks<List<File>>,
		OnClickListener, OnItemClickListener {

	private FilesAdapter mAdapter;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Bundle args = getArguments();
		getLoaderManager().initLoader(0, args, this);
	}

	@Override
	public void onCancel(final DialogInterface dialog) {
		super.onCancel(dialog);
		final FragmentActivity a = getActivity();
		if (a instanceof Callback) {
			((Callback) a).onCancelled(this);
		}
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE: {
				final FragmentActivity a = getActivity();
				if (isPickDirectory() && a instanceof Callback) {
					((Callback) a).onFilePicked(getCurrentDirectory());
				}
				dismiss();
				break;
			}
		}
		return;

	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		mAdapter = new FilesAdapter(getActivity());
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setAdapter(mAdapter, this);
		builder.setTitle(R.string.pick_file);
		if (isPickDirectory()) {
			builder.setPositiveButton(android.R.string.ok, this);
		}
		final AlertDialog dialog = builder.create();
		final ListView listView = dialog.getListView();
		listView.setOnItemClickListener(this);
		return dialog;
	}

	@Override
	public Loader<List<File>> onCreateLoader(final int id, final Bundle args) {
		final String[] extensions = args.getStringArray(EXTRA_FILE_EXTENSIONS);
		final String path = args.getString(EXTRA_PATH);
		File currentDir = path != null ? new File(path) : getExternalStorageDirectory();
		if (currentDir == null) {
			currentDir = new File("/");
		}
		getArguments().putString(EXTRA_PATH, currentDir.getAbsolutePath());
		return new FilesLoader(getActivity(), currentDir, extensions);
	}

	@Override
	public void onDismiss(final DialogInterface dialog) {
		super.onDismiss(dialog);
		final FragmentActivity a = getActivity();
		if (a instanceof Callback) {
			((Callback) a).onDismissed(this);
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> view, final View child, final int position, final long id) {
		final File file = mAdapter.getItem(position);
		if (file == null) return;
		if (file.isDirectory()) {
			final Bundle args = getArguments();
			args.putString(EXTRA_PATH, file.getAbsolutePath());
			getLoaderManager().restartLoader(0, args, this);
		} else if (file.isFile() && !isPickDirectory()) {
			final FragmentActivity a = getActivity();
			if (a instanceof Callback) {
				((Callback) a).onFilePicked(file);
			}
			dismiss();
		}
	}

	@Override
	public void onLoaderReset(final Loader<List<File>> loader) {
		mAdapter.setData(null, null);
	}

	@Override
	public void onLoadFinished(final Loader<List<File>> loader, final List<File> data) {
		final File currentDir = getCurrentDirectory();
		if (currentDir != null) {
			mAdapter.setData(currentDir, data);
			if (currentDir.getParent() == null) {
				setTitle("/");
			} else {
				setTitle(currentDir.getName());
			}
		}
	}

	private File getCurrentDirectory() {
		final Bundle args = getArguments();
		final String path = args.getString(EXTRA_PATH);
		return path != null ? new File(path) : null;
	}

	private boolean isPickDirectory() {
		final Bundle args = getArguments();
		final String action = args != null ? args.getString(EXTRA_ACTION) : null;
		return INTENT_ACTION_PICK_DIRECTORY.equals(action);
	}

	private void setTitle(final CharSequence title) {
		final Dialog dialog = getDialog();
		if (dialog == null) return;
		dialog.setTitle(title);
	}

	public static interface Callback extends ISupportDialogFragmentCallback {

		void onFilePicked(File file);
	}

	private static class FilesAdapter extends ArrayAdapter<File> {

		private final int mPadding;
		private final int mActionIconColor;
		private final Resources mResources;

		private File mCurrentPath;

		public FilesAdapter(final Context context) {
			super(context, android.R.layout.simple_list_item_1);
			mResources = context.getResources();
			mActionIconColor = ThemeUtils.isDarkTheme(context) ? 0xffffffff : 0xc0333333;
			mPadding = (int) (4 * mResources.getDisplayMetrics().density);
		}

		@Override
		public long getItemId(final int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			final TextView text = (TextView) (view instanceof TextView ? view : view.findViewById(android.R.id.text1));
			final File file = getItem(position);
			if (file == null || text == null) return view;
			if (mCurrentPath != null && file.equals(mCurrentPath.getParentFile())) {
				text.setText("..");
			} else {
				text.setText(file.getName());
			}
			text.setSingleLine(true);
			text.setEllipsize(TruncateAt.MARQUEE);
			text.setPadding(mPadding, mPadding, position, mPadding);
			final Drawable icon = mResources
					.getDrawable(file.isDirectory() ? R.drawable.ic_folder : R.drawable.ic_file);
			icon.mutate();
			icon.setColorFilter(mActionIconColor, PorterDuff.Mode.SRC_ATOP);
			text.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
			return view;
		}

		public void setData(final File current, final List<File> data) {
			mCurrentPath = current;
			clear();
			if (data != null) {
				addAll(data);
			}
		}

	}

	private static class FilesLoader extends AsyncTaskLoader<List<File>> {

		private final File path;
		private final String[] extensions;
		private final Pattern extensions_regex;

		private static final Comparator<File> NAME_COMPARATOR = new Comparator<File>() {
			@Override
			public int compare(final File file1, final File file2) {
				final Locale loc = Locale.getDefault();
				return file1.getName().toLowerCase(loc).compareTo(file2.getName().toLowerCase(loc));
			}
		};

		public FilesLoader(final Context context, final File path, final String[] extensions) {
			super(context);
			this.path = path;
			this.extensions = extensions;
			extensions_regex = extensions != null ? Pattern.compile(ArrayUtils.toString(extensions, '|', false),
					Pattern.CASE_INSENSITIVE) : null;
		}

		@Override
		public List<File> loadInBackground() {
			if (path == null || !path.isDirectory()) return Collections.emptyList();
			final File[] listed_files = path.listFiles();
			if (listed_files == null) return Collections.emptyList();
			final List<File> dirs = new ArrayList<File>();
			final List<File> files = new ArrayList<File>();
			for (final File file : listed_files) {
				if (!file.canRead() || file.isHidden()) {
					continue;
				}
				if (file.isDirectory()) {
					dirs.add(file);
				} else if (file.isFile()) {
					final String name = file.getName();
					final int idx = name.lastIndexOf(".");
					if (extensions == null || extensions.length == 0 || idx == -1 || idx > -1
							&& extensions_regex.matcher(name.substring(idx + 1)).matches()) {
						files.add(file);
					}
				}
			}
			Collections.sort(dirs, NAME_COMPARATOR);
			Collections.sort(files, NAME_COMPARATOR);
			final List<File> list = new ArrayList<File>();
			final File parent = path.getParentFile();
			if (path.getParentFile() != null) {
				list.add(parent);
			}
			list.addAll(dirs);
			list.addAll(files);
			return list;
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

		@Override
		protected void onStopLoading() {
			cancelLoad();
		}
	}

}
