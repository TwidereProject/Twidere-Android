/*
 * Twidere - Twitter client for Android
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

package twitter4j.internal.json;

import org.json.JSONObject;

import twitter4j.PageableResponseList;
import twitter4j.http.HttpResponse;
import twitter4j.internal.util.InternalParseUtil;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.3
 */
@SuppressWarnings("rawtypes")
class PageableResponseListImpl<T> extends ResponseListImpl implements PageableResponseList {
	private static final long serialVersionUID = 9098876089678648404L;
	private final long previousCursor;
	private final long nextCursor;

	PageableResponseListImpl(final int size, final JSONObject json, final HttpResponse res) {
		super(size, res);
		this.previousCursor = InternalParseUtil.getLong("previous_cursor", json);
		this.nextCursor = InternalParseUtil.getLong("next_cursor", json);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getNextCursor() {
		return nextCursor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getPreviousCursor() {
		return previousCursor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return 0 != nextCursor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasPrevious() {
		return 0 != previousCursor;
	}

}
