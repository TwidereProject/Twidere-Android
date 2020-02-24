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

package org.mariotaku.twidere.annotation;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 2017/4/17.
 */
@IntDef(value = {TabAccountFlags.FLAG_HAS_ACCOUNT, TabAccountFlags.FLAG_ACCOUNT_REQUIRED,
        TabAccountFlags.FLAG_ACCOUNT_MULTIPLE, TabAccountFlags.FLAG_ACCOUNT_MUTABLE}, flag = true)
@Retention(RetentionPolicy.SOURCE)
public @interface TabAccountFlags {

    int FLAG_HAS_ACCOUNT = 1;
    int FLAG_ACCOUNT_REQUIRED = 2;
    int FLAG_ACCOUNT_MULTIPLE = 4;
    int FLAG_ACCOUNT_MUTABLE = 8;
}
