package org.mariotaku.microblog.library.twitter.util;

import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TwitterTrendsDateConverter implements TypeConverter<Date> {
    private static final Object FORMATTER_LOCK = new Object();

    private static final SimpleDateFormat DATE_FORMAT_1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
    private static final SimpleDateFormat DATE_FORMAT_2 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private static final SimpleDateFormat DATE_FORMAT_3 = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH);

    @Override
    public Date parse(JsonParser jsonParser) throws IOException {
        String dateString = jsonParser.getValueAsString(null);
        if (dateString == null) throw new IOException();
        try {
            synchronized (FORMATTER_LOCK) {
                switch (dateString.length()) {
                    case 10:
                        return new Date(Long.parseLong(dateString) * 1000);
                    case 20:
                        return DATE_FORMAT_1.parse(dateString);
                    default:
                        return parse(dateString, new DateFormat[]{DATE_FORMAT_2, DATE_FORMAT_3});
                }
            }
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void serialize(Date object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) {
        throw new UnsupportedOperationException();
    }

    private static Date parse(String dateString, DateFormat[] formats) throws ParseException {
        for (final DateFormat format : formats) {
            try {
                return format.parse(dateString);
            } catch (ParseException e) {
                // Ignore
            }
        }
        throw new ParseException("Unrecognized date " + dateString, 0);
    }
}
