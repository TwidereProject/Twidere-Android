/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Single-file Cron expression parser
 * <p>
 * Supports POSIX standard syntax only
 */
public class CronExpression {

    /**
     * Equivalent to {@code 0 0 1 1 *}
     */
    public static final CronExpression YEARLY = new CronExpression(new Field[]{
            BasicField.zero(FieldType.MINUTE),
            BasicField.zero(FieldType.HOUR_OF_DAY),
            BasicField.one(FieldType.DAY_OF_MONTH),
            BasicField.one(FieldType.MONTH),
            AnyField.INSTANCE,
            null,
    });

    public static final CronExpression ANNUALLY = YEARLY;

    /**
     * Equivalent to {@code 0 0 1 * *}
     */
    public static final CronExpression MONTHLY = new CronExpression(new Field[]{
            BasicField.zero(FieldType.MINUTE),
            BasicField.zero(FieldType.HOUR_OF_DAY),
            BasicField.one(FieldType.DAY_OF_MONTH),
            AnyField.INSTANCE,
            AnyField.INSTANCE,
            null,
    });

    /**
     * Equivalent to {@code 0 0 * * 0}
     */
    public static final CronExpression WEEKLY = new CronExpression(new Field[]{
            BasicField.zero(FieldType.MINUTE),
            BasicField.zero(FieldType.HOUR_OF_DAY),
            AnyField.INSTANCE,
            AnyField.INSTANCE,
            BasicField.zero(FieldType.DAY_OF_WEEK),
            null,
    });

    /**
     * Equivalent to {@code 0 0 * * *}
     */
    public static final CronExpression DAILY = new CronExpression(new Field[]{
            BasicField.zero(FieldType.MINUTE),
            BasicField.zero(FieldType.HOUR_OF_DAY),
            AnyField.INSTANCE,
            AnyField.INSTANCE,
            AnyField.INSTANCE,
            null,
    });

    /**
     * Equivalent to {@code 0 * * * *}
     */
    public static final CronExpression HOURLY = new CronExpression(new Field[]{
            BasicField.zero(FieldType.MINUTE),
            AnyField.INSTANCE,
            AnyField.INSTANCE,
            AnyField.INSTANCE,
            AnyField.INSTANCE,
            null,
    });

    private Field[] fields;

    private CronExpression(@NonNull Field[] fields) {
        if (fields.length < 5) throw new IllegalArgumentException("Fields count must >= 5");
        this.fields = fields;
    }

    @NonNull
    public static CronExpression valueOf(@NonNull String string) throws ParseException {
        if (string.length() == 0) {
            throw new ParseException("Cron expression is empty", -1);
        }
        if (string.charAt(0) == '@') {
            // Parse predefined
            final String substr = string.substring(1);
            switch (substr) {
                case "yearly":
                    return YEARLY;
                case "annually":
                    return ANNUALLY;
                case "monthly":
                    return MONTHLY;
                case "weekly":
                    return WEEKLY;
                case "daily":
                    return DAILY;
                case "hourly":
                    return HOURLY;
            }
            throw new ParseException("Unknown pre-defined value " + substr, 1);
        }
        final String[] segments = split(string, ' ');
        if (segments.length > 6) {
            throw new ParseException("Unrecognized segments " + string, -1);
        }

        // Parse minute field
        Field[] fields = new Field[6];
        fields[0] = FieldType.MINUTE.parseField(segments[0]);
        // Parse hour field
        fields[1] = FieldType.HOUR_OF_DAY.parseField(segments[1]);
        // Parse day-of-month field
        fields[2] = FieldType.DAY_OF_MONTH.parseField(segments[2]);
        // Parse month field
        fields[3] = FieldType.MONTH.parseField(segments[3]);
        // Parse day-of-week field
        fields[4] = FieldType.DAY_OF_WEEK.parseField(segments[4]);
        return new CronExpression(fields);
    }

    public boolean matches(Calendar cal) {
        for (Field field : fields) {
            if (field == null) continue;
            if (!field.contains(cal)) return false;
        }
        return true;
    }

    public String toExpression() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, j = fields.length; i < j; i++) {
            Field field = fields[i];
            if (field == null) continue;
            if (i != 0) {
                sb.append(' ');
            }
            sb.append(field.toExpression());
        }
        return sb.toString();
    }

    interface Field {
        boolean contains(Calendar cal);

        String toExpression();
    }

    public enum FieldType {

        MINUTE(Calendar.MINUTE, 0, new Range(0, 59), null),
        HOUR_OF_DAY(Calendar.HOUR_OF_DAY, 0, new Range(0, 23), null),
        DAY_OF_MONTH(Calendar.DAY_OF_MONTH, 0, new Range(1, 31), null),
        MONTH(Calendar.MONTH, 1, new Range(1, 12), new String[]{"JAN", "FEB", "MAR", "APR", "JUN", "JUL",
                "AUG", "SEP", "OCT", "NOV", "DEC"}),
        DAY_OF_WEEK(Calendar.DAY_OF_WEEK, -1, new Range(0, 6), new String[]{"SUN", "MON", "TUE", "WED", "THU",
                "FRI", "SAT"}),
        /* Used in nncron, not decided whether to implement */
        YEAR(Calendar.YEAR, 0, new Range(1970, 2099), null);

        final int calendarField;
        final int calendarOffset;
        final Range allowedRange;
        final String[] textRepresentations;

        FieldType(int calendarField, int calendarOffset, Range allowedRange,
                @Nullable String[] textRepresentations) {
            this.calendarField = calendarField;
            this.calendarOffset = calendarOffset;
            this.allowedRange = allowedRange;
            this.textRepresentations = textRepresentations;
        }

        Field parseField(@NonNull String text) throws ParseException {
            if ("*".equals(text)) return AnyField.INSTANCE;
            return BasicField.parse(text, this);
        }
    }

    enum AnyField implements Field {

        INSTANCE;

        @Override
        public boolean contains(Calendar cal) {
            return true;
        }

        @Override
        public String toExpression() {
            return "*";
        }

    }

    /**
     * POSIX-compliant CRON field
     */
    final static class BasicField implements Field {

        @NonNull
        final Range[] ranges;
        final int calendarField;
        final int calendarOffset;

        BasicField(@NonNull final Range[] ranges, int calendarField, int calendarOffset) {
            this.ranges = ranges;
            this.calendarField = calendarField;
            this.calendarOffset = calendarOffset;
        }

        public static Field parse(String text, FieldType fieldType) throws ParseException {
            final Range allowedRange = fieldType.allowedRange;
            final String[] rangeStrings = split(text, ',');
            final Range[] ranges = new Range[rangeStrings.length];
            final String[] textRepresentations = fieldType.textRepresentations;
            for (int i = 0, l = rangeStrings.length; i < l; i++) {
                String rangeString = rangeStrings[i];
                ranges[i] = Range.parse(rangeString, allowedRange, textRepresentations);
                if (!allowedRange.contains(ranges[i])) {
                    throw new ParseException(ranges[i] + " out of range " + allowedRange, -1);
                }
            }
            return new BasicField(ranges, fieldType.calendarField, fieldType.calendarOffset);
        }

        public static Field zero(FieldType fieldType) {
            final Range[] ranges = {Range.single(0)};
            return new BasicField(ranges, fieldType.calendarField, fieldType.calendarOffset);
        }

        public static Field one(FieldType fieldType) {
            final Range[] ranges = {Range.single(1)};
            return new BasicField(ranges, fieldType.calendarField, fieldType.calendarOffset);
        }

        @Override
        public boolean contains(Calendar cal) {
            final int cmp = cal.get(calendarField) + calendarOffset;
            for (Range range : ranges) {
                if (range.contains(cmp)) return true;
            }
            return false;
        }

        @Override
        public String toExpression() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0, j = ranges.length; i < j; i++) {
                Range range = ranges[i];
                if (i != 0) {
                    sb.append(',');
                }
                sb.append(range.toExpression());
            }
            return sb.toString();
        }
    }

    final static class DayOfMonthField implements Field {
        static Field parse(String text, @NonNull Range allowedRange,
                @Nullable String[] textRepresentations) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Calendar cal) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toExpression() {
            throw new UnsupportedOperationException();
        }
    }

    final static class DayOfWeekField implements Field {

        public static Field parse(String text, @NonNull Range allowedRange,
                @Nullable String[] textRepresentations) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Calendar cal) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toExpression() {
            throw new UnsupportedOperationException();
        }
    }

    final static class Range {
        final int start, endInclusive;

        Range(int start, int endInclusive) {
            if (endInclusive < start) {
                throw new IllegalArgumentException("endInclusive < start");
            }
            this.start = start;
            this.endInclusive = endInclusive;
        }

        public boolean contains(int num) {
            return num >= start && num <= endInclusive;
        }

        public boolean contains(Range that) {
            return this.start <= that.start && this.endInclusive >= that.endInclusive;
        }

        public int size() {
            return endInclusive - start + 1;
        }

        public int valueAt(int index) {
            if (index >= size()) {
                throw new IndexOutOfBoundsException("Range index out of bounds");
            }
            return start + index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Range range = (Range) o;

            if (start != range.start) return false;
            return endInclusive == range.endInclusive;
        }

        @Override
        public int hashCode() {
            int result = start;
            result = 31 * result + endInclusive;
            return result;
        }

        @Override
        public String toString() {
            return "Range{" +
                    "start=" + start +
                    ", endInclusive=" + endInclusive +
                    '}';
        }

        public static Range single(int num) {
            return new Range(num, num);
        }

        public static Range parse(String string, Range allowedRange,
                @Nullable String[] textRepresentations) throws ParseException {
            int dashIdx = string.indexOf('-');
            if (dashIdx == -1) {
                return single(parseNumber(string, allowedRange, textRepresentations));
            }
            final int start = parseNumber(string.substring(0, dashIdx), allowedRange,
                    textRepresentations);
            final int endInclusive = parseNumber(string.substring(dashIdx + 1),
                    allowedRange, textRepresentations);
            return new Range(start, endInclusive);
        }

        private static int parseNumber(String input, Range allowedRange,
                @Nullable String[] textRepresentations) throws ParseException {
            if (textRepresentations != null) {
                int textRepresentationIndex = indexOf(textRepresentations, input);
                if (textRepresentationIndex != -1) {
                    return allowedRange.valueAt(textRepresentationIndex);
                }
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                throw new ParseException(e.getMessage(), -1);
            }
        }

        public String toExpression() {
            if (endInclusive == start) return Integer.toString(start);
            return start + "-" + endInclusive;
        }
    }

    private static <T> int indexOf(@NonNull final T[] array, @NonNull final T objectToFind) {
        int length = array.length;
        for (int i = 0; i < length; i++) {
            if (objectToFind.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }

    private static String[] split(@NonNull final String str, final char separatorChar) {
        // Performance tuned for 2.0 (JDK1.4)
        final int len = str.length();
        if (len == 0) {
            return new String[0];
        }
        final List<String> list = new ArrayList<>();
        int i = 0, start = 0;
        boolean match = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match) {
                    list.add(str.substring(start, i));
                    match = false;
                }
                start = ++i;
                continue;
            }
            match = true;
            i++;
        }
        if (match) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[0]);
    }

}
