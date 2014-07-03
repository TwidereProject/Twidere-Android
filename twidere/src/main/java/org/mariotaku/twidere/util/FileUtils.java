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

package org.mariotaku.twidere.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public final class FileUtils {

	/**
	 * The number of bytes in a megabyte.
	 */
	private static final long ONE_MB = 1048576;

	/**
	 * The file copy buffer size (30 MB)
	 */
	private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;

	/**
	 * Copies a file to a new location preserving the file date.
	 * <p>
	 * This method copies the contents of the specified source file to the
	 * specified destination file. The directory holding the destination file is
	 * created if it does not exist. If the destination file exists, then this
	 * method will overwrite it.
	 * <p>
	 * <strong>Note:</strong> This method tries to preserve the file's last
	 * modified date/times using {@link File#setLastModified(long)}, however it
	 * is not guaranteed that the operation will succeed. If the modification
	 * operation fails, no indication is provided.
	 * 
	 * @param srcFile an existing file to copy, must not be {@code null}
	 * @param destFile the new file, must not be {@code null}
	 * @throws NullPointerException if source or destination is {@code null}
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @see #copyFileToDirectory(File, File)
	 */
	public static void copyFile(final File srcFile, final File destFile) throws IOException {
		if (srcFile == null) throw new NullPointerException("Source must not be null");
		if (destFile == null) throw new NullPointerException("Destination must not be null");
		if (srcFile.exists() == false) throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
		if (srcFile.isDirectory()) throw new IOException("Source '" + srcFile + "' exists but is a directory");
		if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath()))
			throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
		final File parentFile = destFile.getParentFile();
		if (parentFile != null) {
			if (!parentFile.mkdirs() && !parentFile.isDirectory())
				throw new IOException("Destination '" + parentFile + "' directory cannot be created");
		}
		if (destFile.exists() && destFile.canWrite() == false)
			throw new IOException("Destination '" + destFile + "' exists but is read-only");
		doCopyFile(srcFile, destFile, true);
	}

	// -----------------------------------------------------------------------
	/**
	 * Copies a file to a directory preserving the file date.
	 * <p>
	 * This method copies the contents of the specified source file to a file of
	 * the same name in the specified destination directory. The destination
	 * directory is created if it does not exist. If the destination file
	 * exists, then this method will overwrite it.
	 * <p>
	 * <strong>Note:</strong> This method tries to preserve the file's last
	 * modified date/times using {@link File#setLastModified(long)}, however it
	 * is not guaranteed that the operation will succeed. If the modification
	 * operation fails, no indication is provided.
	 * 
	 * @param srcFile an existing file to copy, must not be {@code null}
	 * @param destDir the directory to place the copy in, must not be
	 *            {@code null}
	 * @throws NullPointerException if source or destination is null
	 * @throws IOException if source or destination is invalid
	 * @throws IOException if an IO error occurs during copying
	 * @see #copyFile(File, File, boolean)
	 */
	public static void copyFileToDirectory(final File srcFile, final File destDir) throws IOException {
		if (destDir == null) throw new NullPointerException("Destination must not be null");
		if (destDir.exists() && destDir.isDirectory() == false)
			throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
		final File destFile = new File(destDir, srcFile.getName());
		copyFile(srcFile, destFile);
	}

	/**
	 * Unconditionally close a <code>Closeable</code>.
	 * <p>
	 * Equivalent to {@link Closeable#close()}, except any exceptions will be
	 * ignored. This is typically used in finally blocks.
	 * <p>
	 * Example code:
	 * 
	 * <pre>
	 * Closeable closeable = null;
	 * try {
	 * 	closeable = new FileReader(&quot;foo.txt&quot;);
	 * 	// process closeable
	 * 	closeable.close();
	 * } catch (Exception e) {
	 * 	// error handling
	 * } finally {
	 * 	IOUtils.closeQuietly(closeable);
	 * }
	 * </pre>
	 * 
	 * @param closeable the object to close, may be null or already closed
	 * @since 2.0
	 */
	private static void closeQuietly(final Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (final IOException ioe) {
			// ignore
		}
	}

	/**
	 * Unconditionally close an <code>InputStream</code>.
	 * <p>
	 * Equivalent to {@link InputStream#close()}, except any exceptions will be
	 * ignored. This is typically used in finally blocks.
	 * <p>
	 * Example code:
	 * 
	 * <pre>
	 * byte[] data = new byte[1024];
	 * InputStream in = null;
	 * try {
	 * 	in = new FileInputStream(&quot;foo.txt&quot;);
	 * 	in.read(data);
	 * 	in.close(); // close errors are handled
	 * } catch (Exception e) {
	 * 	// error handling
	 * } finally {
	 * 	IOUtils.closeQuietly(in);
	 * }
	 * </pre>
	 * 
	 * @param input the InputStream to close, may be null or already closed
	 */
	private static void closeQuietly(final InputStream input) {
		closeQuietly((Closeable) input);
	}

	/**
	 * Unconditionally close an <code>OutputStream</code>.
	 * <p>
	 * Equivalent to {@link OutputStream#close()}, except any exceptions will be
	 * ignored. This is typically used in finally blocks.
	 * <p>
	 * Example code:
	 * 
	 * <pre>
	 * byte[] data = &quot;Hello, World&quot;.getBytes();
	 * 
	 * OutputStream out = null;
	 * try {
	 * 	out = new FileOutputStream(&quot;foo.txt&quot;);
	 * 	out.write(data);
	 * 	out.close(); // close errors are handled
	 * } catch (IOException e) {
	 * 	// error handling
	 * } finally {
	 * 	IOUtils.closeQuietly(out);
	 * }
	 * </pre>
	 * 
	 * @param output the OutputStream to close, may be null or already closed
	 */
	private static void closeQuietly(final OutputStream output) {
		closeQuietly((Closeable) output);
	}

	/**
	 * Internal copy file method.
	 * 
	 * @param srcFile the validated source file, must not be {@code null}
	 * @param destFile the validated destination file, must not be {@code null}
	 * @param preserveFileDate whether to preserve the file date
	 * @throws IOException if an error occurs
	 */
	private static void doCopyFile(final File srcFile, final File destFile, final boolean preserveFileDate)
			throws IOException {
		if (destFile.exists() && destFile.isDirectory())
			throw new IOException("Destination '" + destFile + "' exists but is a directory");

		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel input = null;
		FileChannel output = null;
		try {
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);
			input = fis.getChannel();
			output = fos.getChannel();
			final long size = input.size();
			long pos = 0;
			long count = 0;
			while (pos < size) {
				count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
				pos += output.transferFrom(input, pos, count);
			}
		} finally {
			closeQuietly(output);
			closeQuietly(fos);
			closeQuietly(input);
			closeQuietly(fis);
		}

		if (srcFile.length() != destFile.length())
			throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
		if (preserveFileDate) {
			destFile.setLastModified(srcFile.lastModified());
		}
	}
}
