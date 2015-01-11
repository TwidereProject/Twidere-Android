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

package twitter4j.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public final class StreamingGZIPInputStream extends GZIPInputStream {

	private final InputStream wrapped;

	public StreamingGZIPInputStream(final InputStream is) throws IOException {
		super(is);
		wrapped = is;
	}

	/**
	 * Overrides behavior of GZIPInputStream which assumes we have all the data
	 * available which is not true for streaming. We instead rely on the
	 * underlying stream to tell us how much data is available.
	 * <p>
	 * Programs should not count on this method to return the actual number of
	 * bytes that could be read without blocking.
	 * 
	 * @return - whatever the wrapped InputStream returns
	 * @exception java.io.IOException if an I/O error occurs.
	 */
	@Override
	public int available() throws IOException {
		return wrapped.available();
	}
}
