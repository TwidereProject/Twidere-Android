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

package twitter4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.management.APIStatistics;
import twitter4j.management.APIStatisticsMBean;

/**
 * Singleton instance of all Twitter API monitoring. Handles URL parsing and
 * "wire off" logic. We could avoid using a singleton here if Twitter objects
 * were instantiated from a factory.
 * 
 * @author Nick Dellamaggiore (nick.dellamaggiore <at> gmail.com)
 * @since Twitter4J 2.2.1
 */
public class TwitterAPIMonitor {
	// https?:\/\/[^\/]+\/([a-zA-Z_\.]*).*
	// finds the "method" part a Twitter REST API url, ignoring member-specific
	// resource names
	private static final Pattern pattern = Pattern.compile("https?:\\/\\/[^\\/]+\\/([a-zA-Z_\\.]*).*");

	private static final TwitterAPIMonitor SINGLETON = new TwitterAPIMonitor();

	private final APIStatistics STATISTICS = new APIStatistics(100);

	static {
		System.setProperty("http.keepAlive", "false");
	}

	/**
	 * Constructor
	 */
	private TwitterAPIMonitor() {
	}

	public APIStatisticsMBean getStatistics() {
		return STATISTICS;
	}

	void methodCalled(final String twitterUrl, final long elapsedTime, final boolean success) {
		final Matcher matcher = pattern.matcher(twitterUrl);
		if (matcher.matches() && matcher.groupCount() > 0) {
			final String method = matcher.group();
			STATISTICS.methodCalled(method, elapsedTime, success);
		}
	}

	public static TwitterAPIMonitor getInstance() {
		return SINGLETON;
	}
}
