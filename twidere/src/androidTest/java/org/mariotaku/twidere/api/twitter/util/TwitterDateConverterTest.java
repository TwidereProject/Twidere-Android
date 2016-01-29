package org.mariotaku.twidere.api.twitter.util;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Created by mariotaku on 16/1/29.
 */
public class TwitterDateConverterTest {

    private final TwitterDateConverter converter = new TwitterDateConverter();
    private final SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);

    @Test
    public void testGetFromString() throws Exception {
        testDate("Fri Jan 29 04:12:49 +0100 2016");
        testDate("Thu Jan 28 11:08:47 +0000 2016");
        testDate("Sat Oct 03 16:05:32 +0000 2015");
        testDate("Tue Jan 26 18:30:19 +0100 2016");
    }

    private void testDate(String s) throws ParseException {
        assertEquals(converter.getFromString(s), format.parse(s));
    }

    @Test
    public void testConvertToString() throws Exception {

    }
}