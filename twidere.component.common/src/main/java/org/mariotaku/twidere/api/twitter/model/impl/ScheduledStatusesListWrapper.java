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
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;

import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.twidere.api.twitter.model.RateLimitStatus;
import org.mariotaku.twidere.api.twitter.model.ScheduledStatus;
import org.mariotaku.twidere.api.twitter.model.ScheduledStatusesList;

import java.util.ArrayList;

/**
 * Created by mariotaku on 15/7/9.
 */
@JsonObject
public class ScheduledStatusesListWrapper implements Wrapper<ScheduledStatusesList> {

    @JsonField(name = "results")
    ArrayList<ScheduledStatus> list;
    private ScheduledStatusesListImpl wrapped;

    @Override
    public ScheduledStatusesList getWrapped(Object extra) {
        return wrapped;
    }

    @Override
    public void processResponseHeader(RestHttpResponse resp) {
        wrapped.processResponseHeader(resp);
    }

    @OnJsonParseComplete
    void onParseComplete() {
        // Do some fancy post-processing stuff after parsing here
        wrapped = new ScheduledStatusesListImpl();
        wrapped.addAll(list);
    }

    @Override
    public int getAccessLevel() {
        return wrapped.getAccessLevel();
    }

    @Override
    public RateLimitStatus getRateLimitStatus() {
        return wrapped.getRateLimitStatus();
    }

    @JsonObject
    public static class ScheduledStatusesListImpl extends ResponseListImpl<ScheduledStatus> implements ScheduledStatusesList {
    }
}
