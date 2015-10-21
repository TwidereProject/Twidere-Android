/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.api.twitter.model;

import org.mariotaku.library.logansquare.extension.annotation.Implementation;
import org.mariotaku.twidere.api.twitter.model.impl.UrlEntityImpl;

/**
 * A data interface representing one single URL entity.
 * 
 * @author Mocel - mocel at guma.jp
 * @since Twitter4J 2.1.9
 */
@Implementation(UrlEntityImpl.class)
public interface UrlEntity  {

	/**
	 * Returns the display URL if mentioned URL is shorten.
	 * 
	 * @return the display URL if mentioned URL is shorten, or null if no
	 *         shorten URL was mentioned.
	 */
	String getDisplayUrl();

	/**
	 * Returns the index of the end character of the URL mentioned in the tweet.
	 * 
	 * @return the index of the end character of the URL mentioned in the tweet
	 */
	int getEnd();

	/**
	 * Returns the expanded URL if mentioned URL is shorten.
	 * 
	 * @return the expanded URL if mentioned URL is shorten, or null if no
	 *         shorten URL was mentioned.
	 */
	String getExpandedUrl();

	/**
	 * Returns the index of the start character of the URL mentioned in the
	 * tweet.
	 * 
	 * @return the index of the start character of the URL mentioned in the
	 *         tweet
	 */
	int getStart();

	/**
	 * Returns the URL mentioned in the tweet.
	 * 
	 * @return the mentioned URL
	 */
	String getUrl();
}
