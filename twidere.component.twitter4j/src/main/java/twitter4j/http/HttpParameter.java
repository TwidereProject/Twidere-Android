/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.http;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

/**
 * A data class representing HTTP Post parameter
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class HttpParameter implements Comparable<HttpParameter> {
	private String name = null;
	private String value = null;
	private String fileName = null;
	private File file = null;
	private InputStream fileBody = null;
	private String overrideFileType;

	private static final String JPEG = "image/jpeg";

	private static final String GIF = "image/gif";

	private static final String PNG = "image/png";

	private static final String OCTET = "application/octet-stream";

	public HttpParameter(final String name, final boolean value) {
		this.name = name;
		this.value = String.valueOf(value);
	}

	public HttpParameter(final String name, final double value) {
		this.name = name;
		this.value = String.valueOf(value);
	}

	public HttpParameter(final String name, final File file) {
		this(name, file, null);
	}

	public HttpParameter(final String name, final File file, final String overrideContentType) {
		this.name = name;
		this.file = file;
		overrideFileType = overrideContentType;
	}

	public HttpParameter(final String name, final int value) {
		this.name = name;
		this.value = String.valueOf(value);
	}

	public HttpParameter(final String name, final long value) {
		this.name = name;
		this.value = String.valueOf(value);
	}

	public HttpParameter(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	public HttpParameter(final String name, final String fileName, final InputStream fileBody) {
		this(name, fileName, fileBody, null);
	}

	public HttpParameter(final String name, final String fileName, final InputStream fileBody,
			final String overrideFileType) {
		this.name = name;
		this.fileName = fileName;
		this.fileBody = fileBody;
		this.overrideFileType = overrideFileType;
	}

	@Override
	public int compareTo(final HttpParameter o) {
		int compared;
		final HttpParameter that = o;
		compared = name.compareTo(that.name);
		if (0 == compared) {
			compared = value.compareTo(that.value);
		}
		return compared;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof HttpParameter)) return false;

		final HttpParameter that = (HttpParameter) o;

		if (file != null ? !file.equals(that.file) : that.file != null) return false;
		if (fileBody != null ? !fileBody.equals(that.fileBody) : that.fileBody != null) return false;
		if (!name.equals(that.name)) return false;
		if (value != null ? !value.equals(that.value) : that.value != null) return false;

		return true;
	}

	/**
	 * @return content-type
	 */
	public String getContentType() {
		if (!isFile()) throw new IllegalStateException("not a file");
		if (overrideFileType != null) return overrideFileType;
		String contentType;
		String extensions = getFileName();
		final int index = extensions.lastIndexOf(".");
		if (-1 == index) {
			// no extension
			contentType = OCTET;
		} else {
			extensions = extensions.substring(extensions.lastIndexOf(".") + 1).toLowerCase(Locale.US);
			if (extensions.length() == 3) {
				if ("gif".equals(extensions)) {
					contentType = GIF;
				} else if ("png".equals(extensions)) {
					contentType = PNG;
				} else if ("jpg".equals(extensions)) {
					contentType = JPEG;
				} else {
					contentType = OCTET;
				}
			} else if (extensions.length() == 4) {
				if ("jpeg".equals(extensions)) {
					contentType = JPEG;
				} else {
					contentType = OCTET;
				}
			} else {
				contentType = OCTET;
			}
		}
		return contentType;
	}

	public File getFile() {
		return file;
	}

	public InputStream getFileBody() {
		return fileBody;
	}

	public String getFileName() {
		return file != null ? file.getName() : fileName;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public boolean hasFileBody() {
		return fileBody != null;
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + (value != null ? value.hashCode() : 0);
		result = 31 * result + (file != null ? file.hashCode() : 0);
		result = 31 * result + (fileBody != null ? fileBody.hashCode() : 0);
		return result;
	}

	public boolean isFile() {
		return fileBody != null || file != null;
	}

	@Override
	public String toString() {
		return "PostParameter{" + "name='" + name + '\'' + ", value='" + value + '\'' + ", file=" + file
				+ ", fileBody=" + fileBody + '}';
	}

	public static boolean containsFile(final HttpParameter[] params) {
		boolean containsFile = false;
		if (null == params) return false;
		for (final HttpParameter param : params) {
			if (param.isFile()) {
				containsFile = true;
				break;
			}
		}
		return containsFile;
	}

	/**
	 * @param value string to be encoded
	 * @return encoded string
	 * @see <a href="http://wiki.oauth.net/TestCases">OAuth / TestCases</a>
	 * @see <a
	 *      href="http://groups.google.com/group/oauth/browse_thread/thread/a8398d0521f4ae3d/9d79b698ab217df2?hl=en&lnk=gst&q=space+encoding#9d79b698ab217df2">Space
	 *      encoding - OAuth | Google Groups</a>
	 * @see <a href="http://tools.ietf.org/html/rfc3986#section-2.1">RFC 3986 -
	 *      Uniform Resource Identifier (URI): Generic Syntax - 2.1.
	 *      Percent-Encoding</a>
	 */
	public static String encode(final String value) {
		String encoded = null;
		try {
			encoded = URLEncoder.encode(value, "UTF-8");
		} catch (final UnsupportedEncodingException ignore) {
			return null;
		}
		final StringBuffer buf = new StringBuffer(encoded.length());
		char focus;
		for (int i = 0; i < encoded.length(); i++) {
			focus = encoded.charAt(i);
			if (focus == '*') {
				buf.append("%2A");
			} else if (focus == '+') {
				buf.append("%20");
			} else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7'
					&& encoded.charAt(i + 2) == 'E') {
				buf.append('~');
				i += 2;
			} else {
				buf.append(focus);
			}
		}
		return buf.toString();
	}

	public static String encodeParameters(final HttpParameter[] httpParams) {
		if (null == httpParams) return "";
		final StringBuffer buf = new StringBuffer();
		for (int j = 0; j < httpParams.length; j++) {
			if (httpParams[j].isFile())
				throw new IllegalArgumentException("parameter [" + httpParams[j].name + "]should be text");
			if (j != 0) {
				buf.append("&");
			}
			buf.append(encode(httpParams[j].name)).append("=").append(encode(httpParams[j].value));
		}
		return buf.toString();
	}

	public static HttpParameter[] getParameterArray(final String name, final int value) {
		return getParameterArray(name, String.valueOf(value));
	}

	public static HttpParameter[] getParameterArray(final String name1, final int value1, final String name2,
			final int value2) {
		return getParameterArray(name1, String.valueOf(value1), name2, String.valueOf(value2));
	}

	public static HttpParameter[] getParameterArray(final String name, final String value) {
		return new HttpParameter[] { new HttpParameter(name, value) };
	}

	public static HttpParameter[] getParameterArray(final String name1, final String value1, final String name2,
			final String value2) {
		return new HttpParameter[] { new HttpParameter(name1, value1), new HttpParameter(name2, value2) };
	}

	public static HttpParameter[] merge(final HttpParameter[] params, final HttpParameter... extraParams) {
		if (params == null) return extraParams;
		if (extraParams == null) return params;
		final HttpParameter[] merged = new HttpParameter[params.length + extraParams.length];
		System.arraycopy(params, 0, merged, 0, params.length);
		System.arraycopy(extraParams, 0, merged, params.length, extraParams.length);
		return merged;
	}

	/* package */
	static boolean containsFile(final List<HttpParameter> params) {
		boolean containsFile = false;
		for (final HttpParameter param : params) {
			if (param.isFile()) {
				containsFile = true;
				break;
			}
		}
		return containsFile;
	}
}
