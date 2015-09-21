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

import android.util.Log;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mariotaku.twidere.util.AbsLogger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by mariotaku on 15/5/7.
 */
public class TwitterDateConverter extends StringBasedTypeConverter<Date> {

    private static final String[] WEEK_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final String[] MONTH_NAMES = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
            "Aug", "Sep", "Oct", "Nov", "Dec"};

    private static final long ONE_MINUTE = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");
    private static final Locale LOCALE = Locale.ENGLISH;
    private final DateFormat mDateFormat;

    public TwitterDateConverter() {
        mDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", LOCALE);
        mDateFormat.setLenient(true);
        mDateFormat.setTimeZone(TIME_ZONE);
    }

    @Override
    public Date getFromString(String string) {
        Date date = null;
        try {
            date = parseTwitterDate(string);
        } catch (NumberFormatException e) {
            Log.w("Twidere", e);
            // Ignore
        }
        if (date != null) return date;
        try {
            date = mDateFormat.parse(string);
        } catch (ParseException e) {
            AbsLogger.error("Unrecognized date: " + string, e);
            return null;
        }
        return date;
    }

    private Date parseTwitterDate(String string) {
        final String[] segs = StringUtils.split(string, ' ');
        if (segs.length != 6) {
            return null;
        }
        final String[] timeSegs = StringUtils.split(segs[3], ':');
        if (timeSegs.length != 3) {
            return null;
        }

        final GregorianCalendar calendar = new GregorianCalendar(TIME_ZONE, LOCALE);
        calendar.clear();
        final int monthIdx = ArrayUtils.indexOf(MONTH_NAMES, segs[1]);
        if (monthIdx < 0) {
            return null;
        }
        calendar.set(Calendar.YEAR, Integer.parseInt(segs[5]));
        calendar.set(Calendar.MONTH, monthIdx);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(segs[2]));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeSegs[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeSegs[1]));
        calendar.set(Calendar.SECOND, Integer.parseInt(timeSegs[2]));
        calendar.set(Calendar.ZONE_OFFSET, SimpleTimeZone.getTimeZone("GMT" + segs[4]).getRawOffset());
        final Date date = calendar.getTime();
        if (!WEEK_NAMES[calendar.get(Calendar.DAY_OF_WEEK) - 1].equals(segs[0])) {
            AbsLogger.error("Week mismatch " + string + " => " + date);
            return null;
        }
        return date;
    }

    @Override
    public String convertToString(Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        final StringBuilder sb = new StringBuilder();
        sb.append(WEEK_NAMES[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
        sb.append(' ');
        sb.append(MONTH_NAMES[calendar.get(Calendar.MONTH)]);
        sb.append(' ');
        sb.append(calendar.get(Calendar.DAY_OF_MONTH));
        sb.append(' ');
        sb.append(calendar.get(Calendar.HOUR_OF_DAY));
        sb.append(':');
        sb.append(calendar.get(Calendar.MINUTE));
        sb.append(':');
        sb.append(calendar.get(Calendar.SECOND));
        sb.append(' ');
        final long offset = TimeUnit.MILLISECONDS.toMinutes(calendar.get(Calendar.ZONE_OFFSET));
        sb.append(offset > 0 ? '+' : '-');
        sb.append(String.format(Locale.ROOT, "%02d%02d", Math.abs(offset) / 60, Math.abs(offset) % 60));
        sb.append(' ');
        sb.append(calendar.get(Calendar.YEAR));
        return sb.toString();
    }

}
