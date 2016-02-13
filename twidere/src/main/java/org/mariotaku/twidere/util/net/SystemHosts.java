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

package org.mariotaku.twidere.util.net;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.mariotaku.twidere.util.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Created by mariotaku on 15/11/23.
 */
public class SystemHosts {

    private static final String HOSTS_PATH = "/system/etc/hosts";

    @NonNull
    public List<InetAddress> resolve(String hostToResolve) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(HOSTS_PATH));
            String line;
            while ((line = br.readLine()) != null) {
                Scanner scanner = new Scanner(line);
                if (!scanner.hasNext()) continue;
                String address = scanner.next();
                if (address.startsWith("#")) continue;
                while (scanner.hasNext()) {
                    final String host = scanner.next();
                    if (host.startsWith("#")) break;
                    if (TextUtils.equals(hostToResolve, host)) {
                        final InetAddress resolved = TwidereDns.getResolvedIPAddress(host, address);
                        if (resolved != null) return Collections.singletonList(resolved);
                    }
                }
            }
        } finally {
            Utils.closeSilently(br);
        }
        throw new UnknownHostException(hostToResolve);
    }

}
