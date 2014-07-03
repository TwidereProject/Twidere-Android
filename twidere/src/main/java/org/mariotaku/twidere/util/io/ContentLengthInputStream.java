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

package org.mariotaku.twidere.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ContentLengthInputStream extends InputStream {

	private final InputStream stream;
	private final long length;
	private ReadListener readListener;

	private long pos;

	public ContentLengthInputStream(final File file) throws FileNotFoundException {
		this(new FileInputStream(file), file.length());
	}

	public ContentLengthInputStream(final InputStream stream, final long length) {
		this.stream = stream;
		this.length = length;
	}

	public ContentLengthInputStream(final String file) throws FileNotFoundException {
		this(new FileInputStream(file), file.length());
	}

	@Override
	public synchronized int available() {
		return (int) (length - pos);
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	public long length() {
		return length;
	}

	@Override
	public void mark(final int readlimit) {
		pos = readlimit;
		stream.mark(readlimit);
	}

	@Override
	public int read() throws IOException {
		pos++;
		if (readListener != null) {
			readListener.onRead(length, pos);
		}
		return stream.read();
	}

	@Override
	public int read(final byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}

	@Override
	public int read(final byte[] buffer, final int byteOffset, final int byteCount) throws IOException {
		pos += byteCount;
		if (readListener != null) {
			readListener.onRead(length, pos);
		}
		return stream.read(buffer, byteOffset, byteCount);
	}

	@Override
	public synchronized void reset() throws IOException {
		pos = 0;
		stream.reset();
	}

	public void setReadListener(final ReadListener readListener) {
		this.readListener = readListener;
	}

	@Override
	public long skip(final long byteCount) throws IOException {
		pos += byteCount;
		return stream.skip(byteCount);
	}

	public interface ReadListener {
		void onRead(long length, long position);
	}

}
