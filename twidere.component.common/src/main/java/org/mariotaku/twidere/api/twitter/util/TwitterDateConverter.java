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

package org.mariotaku.twidere.api.twitter.util;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by mariotaku on 15/5/7.
 */
public class TwitterDateConverter extends StringBasedTypeConverter<Date> {

    static final DateFormat sFormat = new ThreadLocalSimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy",
            Locale.ENGLISH);

    static {
        sFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        sFormat.setLenient(true);
    }

    @Override
    public Date getFromString(final String string) {
        if (string == null) return null;
        try {
            return sFormat.parse(string);
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public String convertToString(final Date date) {
        if (date == null) return null;
        return sFormat.format(date);
    }

}
