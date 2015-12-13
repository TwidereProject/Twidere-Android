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

package org.mariotaku.twidere.api.twitter.model;

import org.mariotaku.library.logansquare.extension.annotation.Implementation;
import org.mariotaku.library.logansquare.extension.annotation.ParameterizedImplementation;
import org.mariotaku.library.logansquare.extension.annotation.ParameterizedMapper;
import org.mariotaku.library.logansquare.extension.annotation.TypeImplementation;
import org.mariotaku.library.logansquare.extension.annotation.TypeMapper;
import org.mariotaku.twidere.api.twitter.model.impl.ResponseArrayList;
import org.mariotaku.twidere.api.twitter.model.impl.ScheduledStatusesListImpl;
import org.mariotaku.twidere.api.twitter.model.impl.mapper.list.ActivityResponseListMapper;
import org.mariotaku.twidere.api.twitter.model.impl.mapper.list.DirectMessageResponseListMapper;
import org.mariotaku.twidere.api.twitter.model.impl.mapper.list.LanguageResponseListMapper;
import org.mariotaku.twidere.api.twitter.model.impl.mapper.list.LocationResponseListMapper;
import org.mariotaku.twidere.api.twitter.model.impl.mapper.list.PlaceResponseListMapper;
import org.mariotaku.twidere.api.twitter.model.impl.mapper.list.SavedSearchResponseListMapper;
import org.mariotaku.twidere.api.twitter.model.impl.mapper.list.StatusResponseListMapper;
import org.mariotaku.twidere.api.twitter.model.impl.mapper.list.TrendsResponseListMapper;
import org.mariotaku.twidere.api.twitter.model.impl.mapper.list.UserListResponseListMapper;
import org.mariotaku.twidere.api.twitter.model.impl.mapper.list.UserResponseListMapper;

import java.util.List;

/**
 * List of TwitterResponse.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
@ParameterizedMapper({
        @TypeMapper(parameter = Activity.class, mapper = ActivityResponseListMapper.class),
        @TypeMapper(parameter = DirectMessage.class, mapper = DirectMessageResponseListMapper.class),
        @TypeMapper(parameter = Language.class, mapper = LanguageResponseListMapper.class),
        @TypeMapper(parameter = Location.class, mapper = LocationResponseListMapper.class),
        @TypeMapper(parameter = Place.class, mapper = PlaceResponseListMapper.class),
        @TypeMapper(parameter = SavedSearch.class, mapper = SavedSearchResponseListMapper.class),
        @TypeMapper(parameter = Status.class, mapper = StatusResponseListMapper.class),
        @TypeMapper(parameter = Trends.class, mapper = TrendsResponseListMapper.class),
        @TypeMapper(parameter = UserList.class, mapper = UserListResponseListMapper.class),
        @TypeMapper(parameter = User.class, mapper = UserResponseListMapper.class),
})
@ParameterizedImplementation({
        @TypeImplementation(parameter = ScheduledStatus.class, implementation = ScheduledStatusesListImpl.class)
})
@Implementation(ResponseArrayList.class)
public interface ResponseList<T> extends TwitterResponse, List<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    RateLimitStatus getRateLimitStatus();

}
