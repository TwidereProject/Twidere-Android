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

package twitter4j.auth;

import java.io.ObjectStreamException;

import twitter4j.http.HttpRequest;

/**
 * An interface represents credentials.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class NullAuthorization implements Authorization {
	private static NullAuthorization SINGLETON = new NullAuthorization();

	private NullAuthorization() {

	}

	@Override
	public boolean equals(final Object o) {
		return SINGLETON == o;
	}

	@Override
	public String getAuthorizationHeader(final HttpRequest req) {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public String toString() {
		return "NullAuthentication{SINGLETON}";
	}

	private Object readResolve() throws ObjectStreamException {
		return SINGLETON;
	}

	public static NullAuthorization getInstance() {
		return SINGLETON;
	}

}
