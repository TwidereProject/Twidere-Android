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

import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.api.twitter.util.TwitterDateConverter;

import java.util.Date;

import org.mariotaku.twidere.api.twitter.model.SavedSearch;

/**
 * Created by mariotaku on 15/5/7.
 */
@JsonObject
public class SavedSearchImpl extends TwitterResponseImpl implements SavedSearch {

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @JsonField(name = "id")
    int id;

    @JsonField(name = "created_at", typeConverter = TwitterDateConverter.class)
    Date createdAt;

    @JsonField(name = "name")
    String name;

    @JsonField(name = "position")
    int position;

    @JsonField(name = "query")
    String query;

    @Override
    public int compareTo(@NonNull SavedSearch another) {
        return id - another.getId();
    }

}
