/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.microblog.library.model.twitter;

import org.mariotaku.microblog.library.util.InternalArrayUtil;
import org.mariotaku.restfu.http.SimpleValueMap;

/**
 * Created by mariotaku on 16/3/1.
 */
public class NewDm extends SimpleValueMap {

    public void setText(String text) {
        put("text", text);
    }

    public void setConversationId(String conversationId) {
        put("conversation_id", conversationId);
    }

    /**
     * Help you identify which is new message, this id will be attached in request result
     */
    public void setRequestId(String requestId) {
        put("request_id", requestId);
    }

    public void setRecipientIds(String[] recipientIds) {
        put("recipient_ids", InternalArrayUtil.join(recipientIds, ","));
    }

    public void setMediaId(String mediaId) {
        put("media_id", mediaId);
    }

}
