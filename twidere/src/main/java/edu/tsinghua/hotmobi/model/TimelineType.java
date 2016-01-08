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

import android.support.annotation.StringDef;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 15/8/18.
 */
@StringDef({TimelineType.HOME, TimelineType.INTERACTIONS, TimelineType.DETAILS, TimelineType.SEARCH,
        TimelineType.USER, TimelineType.OTHER})
@Retention(RetentionPolicy.CLASS)
@Inherited
public @interface TimelineType {
    String HOME = "home";
    String INTERACTIONS = "interactions";
    String DETAILS = "details";
    String SEARCH = "search";
    String USER = "user";
    String OTHER = "other";

}
