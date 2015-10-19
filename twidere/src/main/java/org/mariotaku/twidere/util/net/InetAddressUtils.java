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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

/**
 * A collection of utilities relating to InetAddresses.
 *
 * @since 4.0
 */
public class InetAddressUtils {
    private InetAddressUtils() {
    }

    /**
     * Checks whether the parameter is a valid IPv4 address
     *
     * @param input the address string to check for validity
     * @return true if the input parameter is a valid IPv4 address
     */
    public static boolean isIPv4Address(final String input) {
        try {
            return Inet4Address.getByName(input) != null;
        } catch (UnknownHostException ex) {
            return false;
        }
    }

    /**
     * Checks whether the parameter is a valid IPv6 address (including compressed).
     *
     * @param input the address string to check for validity
     * @return true if the input parameter is a valid standard or compressed IPv6 address
     */
    public static boolean isIPv6Address(final String input) {
        try {
            return Inet6Address.getByName(input) != null;
        } catch (UnknownHostException ex) {
            return false;
        }
    }
}