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

package org.mariotaku.twidere.adapter.iface;

import android.widget.ListAdapter;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.ImageLoaderWrapper;

public interface IBaseAdapter extends Constants, ListAdapter {

	public ImageLoaderWrapper getImageLoader();

	public int getLinkHighlightColor();

	public int getLinkHighlightOption();

	public float getTextSize();

	public boolean isDisplayNameFirst();

	public boolean isDisplayProfileImage();

	public boolean isNicknameOnly();

	public boolean isShowAccountColor();

	public void notifyDataSetChanged();

	public void setDisplayNameFirst(boolean nameFirst);

	public void setDisplayProfileImage(boolean display);

	public void setLinkHighlightColor(int color);

	public void setLinkHighlightOption(String option);

	public void setNicknameOnly(boolean nickname_only);

	public void setShowAccountColor(boolean show);

	public void setTextSize(float text_size);
}
