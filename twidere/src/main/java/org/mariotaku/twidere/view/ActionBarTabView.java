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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;

public class ActionBarTabView extends FrameLayout {

	private CharSequence mTitle;
	private Drawable mIcon;

	public ActionBarTabView(final Context context) {
		this(context, null);
	}

	public ActionBarTabView(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.actionBarTabStyle);
	}

	public ActionBarTabView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		inflate(context, R.layout.tab_item_ab, this);
	}

	public void setIcon(final Drawable icon) {
		mIcon = icon;
		setIconView(icon);
	}

	public void setIcon(final int iconRes) {
		setIcon(getResources().getDrawable(iconRes));
	}

	public void setIconView(final Drawable icon) {
		final ImageView iconView = (ImageView) findViewById(android.R.id.icon);
		if (iconView == null) return;
		iconView.setVisibility(icon != null ? GONE : VISIBLE);
		iconView.setImageDrawable(icon);
	}

	public void setTitle(final CharSequence title) {
		mTitle = title;
		setTitleView(title);
	}

	public void setTitle(final int titleRes) {
		setTitle(getResources().getText(titleRes));
	}

	public void setTitleView(final CharSequence title) {
		final TextView titleView = (TextView) findViewById(android.R.id.title);
		final ImageView iconView = (ImageView) findViewById(android.R.id.icon);
		if (titleView == null || iconView == null) return;
		titleView.setVisibility(TextUtils.isEmpty(title) ? GONE : VISIBLE);
		titleView.setText(title);
		iconView.setContentDescription(title);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setTitleView(mTitle);
		setIconView(mIcon);
	}

}
