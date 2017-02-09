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

package org.mariotaku.twidere.model;

import junit.framework.TestCase;

/**
 * Created by mariotaku on 16/3/10.
 */
public class UserKeyLocalTest extends TestCase {

    public void testToString() throws Exception {
        assertEquals("abc@twitter.com", new UserKey("abc", "twitter.com").toString());
        assertEquals("\\@user@twitter.com", new UserKey("@user", "twitter.com").toString());
        assertEquals("\\@u\\\\ser@twitter.com", new UserKey("@u\\ser", "twitter.com").toString());
    }

    public void testValueOf() throws Exception {
        assertEquals(new UserKey("abc", "twitter.com"), UserKey.valueOf("abc@twitter.com"));
        assertEquals(new UserKey("abc@", "twitter.com"), UserKey.valueOf("abc\\@@twitter.com"));
        assertEquals(new UserKey("abc@", "twitter.com"), UserKey.valueOf("a\\bc\\@@twitter.com"));
        assertEquals(new UserKey("a\\bc@", "twitter.com"), UserKey.valueOf("a\\\\bc\\@@twitter.com"));
        assertEquals(new UserKey("abc", "twitter.com"), UserKey.valueOf("abc@twitter.com,def@twitter.com"));
        assertEquals(new UserKey("@abc", "twitter.com"), UserKey.valueOf("\\@abc@twitter.com,def@twitter.com"));
    }
}