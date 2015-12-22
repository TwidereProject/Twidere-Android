/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.mariotaku.twidere.util.net;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.InetAddress;

/**
 * A collection of utilities relating to InetAddresses.
 *
 * @since 4.0
 */
public class InetAddressUtils {

    static {
        System.loadLibrary("twidere");
    }

    private InetAddressUtils() {
        throw new AssertionError("Trying to instantiate this class");
    }

    /**
     * @param input IP address in string
     * @return type corresponding to &lt;sys/socket.h&gt;
     */
    public native static int getInetAddressType(final String input);

    @Nullable
    public native static InetAddress getResolvedIPAddress(@Nullable final String host, @NonNull final String address);
}