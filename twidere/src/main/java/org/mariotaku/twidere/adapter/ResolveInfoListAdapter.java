package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;

public class ResolveInfoListAdapter extends ArrayAdapter<ResolveInfo> {

	private final PackageManager mPackageManager;

	public ResolveInfoListAdapter(final Context context) {
		super(context, R.layout.list_item_two_line_small);
		mPackageManager = context.getPackageManager();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final ResolveInfo info = getItem(position);
		final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
		final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
		icon.setImageDrawable(info.loadIcon(mPackageManager));
		text1.setText(info.loadLabel(mPackageManager));
		text2.setVisibility(View.GONE);
		return view;
	}

}
