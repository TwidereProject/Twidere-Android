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

import android.support.v4.util.SimpleArrayMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class HostsFileParser {

    private final SimpleArrayMap<String, String> mHosts = new SimpleArrayMap<>();
    private final String mPath;

    public HostsFileParser() {
        this("/etc/hosts");
    }

    public HostsFileParser(final String path) {
        mPath = path;
    }

    public boolean contains(final String host) {
        return mHosts.containsKey(host);
    }

    public String getAddress(final String host) {
        return mHosts.get(host);
    }

    public boolean reload() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(mPath));
            mHosts.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                final String trimmed = line.trim();
                // Skip if this line is empty or commented out
                if (trimmed.length() == 0 || trimmed.startsWith("#")) {
                    continue;
                }
                final String[] segments = trimmed.replaceAll("(\\s|\t)+", " ").split("\\s");
                if (segments.length < 2) {
                    continue;
                }
                final String host = segments[1];
                if (!contains(host)) {
                    mHosts.put(host, segments[0]);
                }
            }
            return true;
        } catch (final IOException e) {
            return false;
        } finally {
            if (reader != null) {
                Utils.closeSilently(reader);
            }
        }
    }

    public void reloadIfNeeded() {
        if (!mHosts.isEmpty()) return;
        reload();
    }
}
