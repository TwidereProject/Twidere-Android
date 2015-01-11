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

package twitter4j.internal.logging;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.0
 */
public abstract class Logger {
    private static final LoggerFactory LOGGER_FACTORY = new AndroidLoggerFactory();

    /**
     * @param message message
     */
    public abstract void debug(String message);

    /**
     * @param message  message
     * @param message2 message2
     */
    public abstract void debug(String message, String message2);

    /**
     * @param message message
     */
    public abstract void error(String message);

    /**
     * @param message message
     * @param th      throwable
     */
    public abstract void error(String message, Throwable th);

    /**
     * @param message message
     */
    public abstract void info(String message);

    /**
     * @param message  message
     * @param message2 message2
     */
    public abstract void info(String message, String message2);

    /**
     * tests if debug level logging is enabled
     *
     * @return if debug level logging is enabled
     */
    public abstract boolean isDebugEnabled();

    /**
     * tests if error level logging is enabled
     *
     * @return if error level logging is enabled
     */
    public abstract boolean isErrorEnabled();

    /**
     * tests if info level logging is enabled
     *
     * @return if info level logging is enabled
     */
    public abstract boolean isInfoEnabled();

    /**
     * tests if warn level logging is enabled
     *
     * @return if warn level logging is enabled
     */
    public abstract boolean isWarnEnabled();

    /**
     * @param message message
     */
    public abstract void warn(String message);

    /**
     * @param message  message
     * @param message2 message2
     */
    public abstract void warn(String message, String message2);

    /**
     * Returns a Logger instance associated with the specified class.
     *
     * @return logger instance
     */
    @Deprecated
    public static Logger getLogger() {
        return LOGGER_FACTORY.getLogger();
    }

    public static Logger getLogger(final Class<?> clz) {
        return LOGGER_FACTORY.getLogger(clz);
    }

    public static Logger getLogger(final String tag) {
        return LOGGER_FACTORY.getLogger(tag);
    }

}
