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

import org.mariotaku.twidere.util.AbsLogger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by mariotaku on 15/5/7.
 */
public class TwitterDateConverter extends StringBasedTypeConverter<Date> {

    private static final long ONE_MINUTE = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
    private final DateFormat mDateFormat;

    public TwitterDateConverter() {
        mDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
        mDateFormat.setLenient(true);
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public Date getFromString(String string) {
        final Date date;
        try {
            date = mDateFormat.parse(string);
        } catch (ParseException e) {
            AbsLogger.error("Unrecognized date: " + string, e);
            return null;
        }
        final long currentTime = System.currentTimeMillis();
        if (date.getTime() - currentTime > ONE_MINUTE) {
            AbsLogger.error("Tweet date from future, raw string: " + string + ", date parsed: "
                    + date + ", current time is " + currentTime);
        }
        return date;
    }

    @Override
    public String convertToString(Date date) {
        return mDateFormat.format(date);
    }

}
