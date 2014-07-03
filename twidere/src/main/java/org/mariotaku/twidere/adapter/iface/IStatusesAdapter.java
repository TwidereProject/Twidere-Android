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

import org.mariotaku.twidere.model.ParcelableStatus;

public interface IStatusesAdapter<Data> extends IBaseCardAdapter {

	public int findPositionByStatusId(final long statusId);

	public long getAccountId(final int position);

	public int getActualCount();

	public ParcelableStatus getLastStatus();

	public long getLastStatusId();

	public ParcelableStatus getStatus(int position);

	public long getStatusId(final int position);

	public boolean isLastItemFiltered();

	public void setCardHighlightOption(String option);

	public void setData(Data data);

	public void setDisplayImagePreview(boolean display);

	public void setDisplaySensitiveContents(boolean display);

	public void setFavoritesHightlightDisabled(boolean disable);

	public void setFiltersEnabled(boolean enabled);

	public void setGapDisallowed(boolean disallowed);

	public void setHighlightKeyword(String... keywords);

	public void setIgnoredFilterFields(final boolean user, final boolean textPlain, final boolean textHtml,
			final boolean source, final boolean retweetedById);

	public void setImagePreviewScaleType(String scaleType);

	public void setIndicateMyStatusDisabled(boolean disable);

	public void setMentionsHightlightDisabled(boolean disable);

}
