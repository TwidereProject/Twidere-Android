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

package twitter4j.internal.logging;

import android.util.Log;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.1
 */
final class AndroidLogger extends Logger {

	private static final String DEFAULT_LOGTAG = "Twitter4J";

	private final String logTag;

	AndroidLogger() {
		logTag = DEFAULT_LOGTAG;
	}

	AndroidLogger(final String tag) {
		logTag = tag;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final String message) {
		if (isDebugEnabled()) {
			Log.d(logTag, message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(final String message, final String message2) {
		if (isDebugEnabled()) {
			Log.d(logTag, message + message2);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final String message) {
		if (isErrorEnabled()) {
			Log.e(logTag, message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(final String message, final Throwable th) {
		if (isErrorEnabled()) {
			Log.e(logTag, message, th);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(final String message) {
		if (isInfoEnabled()) {
			Log.i(logTag, message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(final String message, final String message2) {
		if (isInfoEnabled()) {
			Log.i(logTag, message + message2);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDebugEnabled() {
		return false;
		// return Log.isLoggable(DEFAULT_LOGTAG, Log.DEBUG);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isErrorEnabled() {
		return Log.isLoggable(DEFAULT_LOGTAG, Log.ERROR);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInfoEnabled() {
		return false;
		// return Log.isLoggable(DEFAULT_LOGTAG, Log.INFO);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isWarnEnabled() {
		return Log.isLoggable(DEFAULT_LOGTAG, Log.WARN);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final String message) {
		if (isWarnEnabled()) {
			Log.w(logTag, message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(final String message, final String message2) {
		if (isWarnEnabled()) {
			Log.w(logTag, message + message2);
		}
	}
}
