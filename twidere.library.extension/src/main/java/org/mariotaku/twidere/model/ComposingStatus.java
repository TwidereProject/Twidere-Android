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

import android.content.Intent;

import org.mariotaku.twidere.TwidereConstants;

public class ComposingStatus implements TwidereConstants {

    public final String text, name, screen_name, in_reply_to_screen_name, in_reply_to_name;
    public final long in_reply_to_id;

    public ComposingStatus(final Intent intent) {
        text = intent.getStringExtra(EXTRA_TEXT);
        name = intent.getStringExtra(EXTRA_NAME);
        screen_name = intent.getStringExtra(EXTRA_SCREEN_NAME);
        in_reply_to_screen_name = intent.getStringExtra(EXTRA_IN_REPLY_TO_SCREEN_NAME);
        in_reply_to_name = intent.getStringExtra(EXTRA_IN_REPLY_TO_NAME);
        in_reply_to_id = intent.getLongExtra(EXTRA_IN_REPLY_TO_ID, -1);
    }
}
