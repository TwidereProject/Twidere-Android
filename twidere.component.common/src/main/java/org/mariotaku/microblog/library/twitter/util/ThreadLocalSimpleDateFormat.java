package org.mariotaku.microblog.library.twitter.util;

import android.support.annotation.NonNull;

import java.text.AttributedCharacterIterator;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by mariotaku on 16/4/5.
 */
public final class ThreadLocalSimpleDateFormat extends DateFormat {

    private final ThreadLocal<SimpleDateFormat> threadLocal;

    public ThreadLocalSimpleDateFormat(@NonNull final String pattern, final Locale locale) {
        threadLocal = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat(pattern, locale);
            }
        };
    }

    @Override
    public void setTimeZone(TimeZone timezone) {
        threadLocal.get().setTimeZone(timezone);
    }

    @Override
    public void setNumberFormat(NumberFormat format) {
        threadLocal.get().setNumberFormat(format);
    }

    @Override
    public void setLenient(boolean value) {
        threadLocal.get().setLenient(value);
    }

    @Override
    public void setCalendar(Calendar cal) {
        threadLocal.get().setCalendar(cal);
    }

    @Override
    public Object parseObject(String string, @NonNull ParsePosition position) {
        return threadLocal.get().parseObject(string, position);
    }

    @Override
    public Date parse(String string) throws ParseException {
        return threadLocal.get().parse(string);
    }

    @Override
    public boolean isLenient() {
        return threadLocal.get().isLenient();
    }

    @Override
    public TimeZone getTimeZone() {
        return threadLocal.get().getTimeZone();
    }

    @Override
    public NumberFormat getNumberFormat() {
        return threadLocal.get().getNumberFormat();
    }

    @Override
    public Calendar getCalendar() {
        return threadLocal.get().getCalendar();
    }

    @Override
    public AttributedCharacterIterator formatToCharacterIterator(@NonNull Object object) {
        return threadLocal.get().formatToCharacterIterator(object);
    }

    @Override
    public Object parseObject(String string) throws ParseException {
        return threadLocal.get().parseObject(string);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer buffer, FieldPosition field) {
        return threadLocal.get().format(date, buffer, field);
    }

    @Override
    public Date parse(String string, ParsePosition position) {
        return threadLocal.get().parse(string, position);
    }
}
