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

package edu.tsinghua.hotmobi.model;

import android.content.Context;
import android.content.res.Configuration;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 15/8/8.
 */
@JsonObject
public class SessionEvent extends BaseEvent {

    @JsonField(name = "configuration")
    String configuration;


    public static SessionEvent create(Context context) {
        final SessionEvent event = new SessionEvent();
        event.markStart(context);
        final Context appContext = context.getApplicationContext();
        final Configuration conf = appContext.getResources().getConfiguration();
        event.setConfiguration(conf.toString());
        return event;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }
}
