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

package org.mariotaku.twidere.api.twitter.model.impl;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.api.twitter.model.IDs;
import org.mariotaku.twidere.api.twitter.model.StatusActivitySummary;

/**
 * Created by mariotaku on 15/5/13.
 */
@JsonObject
public class StatusActivitySummaryImpl extends TwitterResponseImpl implements StatusActivitySummary {

    @JsonField(name = "favoriters")
    IDs favoriters;
    @JsonField(name = "repliers")
    IDs repliers;
    @JsonField(name = "retweeters")
    IDs retweeters;

    @JsonField(name = "favoriters_count")
    long favoritersCount;
    @JsonField(name = "repliers_count")
    long repliersCount;
    @JsonField(name = "retweeters_count")
    long retweetersCount;
    @JsonField(name = "descendent_reply_count")
    long descendentReplyCount;

    @Override
    public IDs getFavoriters() {
        return favoriters;
    }

    @Override
    public IDs getRepliers() {
        return repliers;
    }

    @Override
    public IDs getRetweeters() {
        return retweeters;
    }

    @Override
    public long getFavoritersCount() {
        return favoritersCount;
    }

    @Override
    public long getRepliersCount() {
        return repliersCount;
    }

    @Override
    public long getRetweetersCount() {
        return retweetersCount;
    }

    @Override
    public long getDescendentReplyCount() {
        return descendentReplyCount;
    }
}
