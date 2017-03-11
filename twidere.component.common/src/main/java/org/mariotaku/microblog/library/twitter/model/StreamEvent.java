/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.microblog.library.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.microblog.library.twitter.util.TwitterDateConverter;

import java.util.Date;

/**
 * Created by mariotaku on 16/2/26.
 */
@JsonObject
public class StreamEvent {
    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;
    @JsonField(name = "source")
    User source;
    @JsonField(name = "target")
    User target;

    public Date getCreatedAt() {
        return createdAt;
    }

    public User getSource() {
        return source;
    }

    public User getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "StreamEvent{" +
                "createdAt=" + createdAt +
                ", source=" + source +
                ", target=" + target +
                '}';
    }
}
