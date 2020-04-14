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

package org.mariotaku.twidere.model.presentation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.commons.logansquare.StringBasedListTypeConverter;
import org.mariotaku.twidere.model.CronExpression;
import org.mariotaku.twidere.model.util.CronExpressionConverter;

import java.util.List;

/**
 * Created by mariotaku on 2017/8/16.
 */
@JsonObject
public class LaunchPresentation {

    @JsonField(name = "images")
    List<Image> images;

    @JsonField(name = "locales", typeConverter = Locale.ListConverter.class)
    List<Locale> locales;

    @JsonField(name = "url")
    String url;

    @JsonField(name = "schedule")
    Schedule schedule;

    @JsonField(name = "is_promotion")
    boolean isPromotion;

    public List<Image> getImages() {
        return images;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    public String getUrl() {
        return url;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public boolean isPromotion() {
        return isPromotion;
    }

    @JsonObject
    public static class Schedule {
        @JsonField(name = "cron", typeConverter = CronExpressionConverter.class)
        CronExpression cron;
        @JsonField(name = "local")
        boolean local;

        public CronExpression getCron() {
            return cron;
        }

        public boolean isLocal() {
            return local;
        }
    }

    public static class Locale {
        @NonNull
        final String language;
        @Nullable
        final String country;

        public Locale(@NonNull String language, @Nullable String country) {
            this.language = language;
            this.country = country;
        }

        @NonNull
        public String getLanguage() {
            return language;
        }

        @Nullable
        public String getCountry() {
            return country;
        }

        @Override
        public String toString() {
            if (country == null) return language;
            return language + "-" + country;
        }

        public static Locale valueOf(@NonNull String str) {
            int dashIndex = str.indexOf('-');
            if (dashIndex == -1) return new Locale(str, null);
            return new Locale(str.substring(0, dashIndex),
                    str.substring(dashIndex + 1));
        }

        static class ListConverter extends StringBasedListTypeConverter<Locale> {

            @Override
            public Locale getItemFromString(String str) {
                if (str == null) return null;
                return Locale.valueOf(str);
            }

            @Override
            public String convertItemToString(Locale item) {
                if (item == null) return null;
                return item.toString();
            }
        }
    }

    @JsonObject
    public static class Image {
        @JsonField(name = "url")
        String url;

        @JsonField(name = "density")
        float density;

        @JsonField(name = "width")
        int width;

        @JsonField(name = "height")
        int height;

        public String getUrl() {
            return url;
        }

        public float getDensity() {
            return density;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
