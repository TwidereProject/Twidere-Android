/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Preference {

    boolean defaultBoolean() default false;

    float defaultFloat() default 0;

    int defaultInt() default 0;

    long defaultLong() default 0;

    int defaultResource() default 0;

    String defaultString() default "";

    boolean exportable() default true;

    boolean hasDefault() default false;

    @Type int type() default Type.NULL;

    @IntDef({Type.BOOLEAN, Type.INT, Type.LONG, Type.FLOAT, Type.STRING, Type.NULL, Type.INVALID})
    @interface Type {
        int BOOLEAN = 1, INT = 2, LONG = 3, FLOAT = 4, STRING = 5, NULL = 0, INVALID = -1;
    }
}
