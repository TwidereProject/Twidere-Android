/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.api;

import org.mariotaku.twidere.model.ParcelableUser;

import java.lang.reflect.Type;

import retrofit.RestAdapter;
import retrofit.RestAdapter.Builder;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Created by mariotaku on 15/2/3.
 */
public class APIFactory {

    public static TwitterAPI getTwitterAPI() {
        Builder builder = new RestAdapter.Builder();
        builder.setEndpoint("https://api.twitter.com");
        builder.setConverter(new ParcelableDataConverter());
        return builder.build().create(TwitterAPI.class);
    }

    private static class ParcelableDataConverter implements Converter {
        @Override
        public Object fromBody(TypedInput typedInput, Type type) throws ConversionException {
            Class<?> typeClass = (Class<?>) type;
            if (typeClass.isAssignableFrom(ParcelableUser.class)) {

            }
            return null;
        }

        @Override
        public TypedOutput toBody(Object o) {
            throw new UnsupportedOperationException();
        }
    }
}
