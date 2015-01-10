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

package org.mariotaku.twidere.extension.streaming.model;

public class PreviewMedia {

	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_IMAGE = 1;
	public static final int TYPE_VIDEO = 2;
	public static final int TYPE_AUDIO = 3;

	private static final PreviewMedia EMPTY_INSTANCE = new PreviewMedia(null, null, TYPE_UNKNOWN);

	public final String url, original;
	public final int type;

	public PreviewMedia(final String url, final String original, final int type) {
		this.url = url;
		this.original = original;
		this.type = type;
	}

	@Override
	public String toString() {
		return "PreviewMedia{url=" + url + ", original=" + original + ", type=" + type + "}";
	}

	public static PreviewMedia getEmpty() {
		return EMPTY_INSTANCE;
	}

	public static PreviewMedia newImage(final String url, final String original) {
		return new PreviewMedia(url, original, TYPE_IMAGE);
	}

	public static PreviewMedia newVideo(final String url, final String original) {
		return new PreviewMedia(url, original, TYPE_VIDEO);
	}
}
