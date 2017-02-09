/*
 *         Twidere - Twitter client for Android
 *
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
 */

package org.mariotaku.twidere;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by mariotaku on 16/2/24.
 */
public class TwidereTest {

    @Test
    public void testCheckPermissionRequirement() {
        assertEquals(Twidere.Permission.GRANTED, Twidere.checkPermissionRequirement(new String[]{"a", "b", "c"}, new String[]{"a", "b", "c"}));
        assertEquals(Twidere.Permission.GRANTED, Twidere.checkPermissionRequirement(new String[]{"a", "b"}, new String[]{"a", "b", "c"}));
        assertEquals(Twidere.Permission.NONE, Twidere.checkPermissionRequirement(new String[]{"a", "b"}, new String[]{"a", "c"}));
        assertEquals(Twidere.Permission.DENIED, Twidere.checkPermissionRequirement(new String[]{"a", "b"}, new String[]{"denied"}));
    }
}