/*
 * Twidere - Twitter client for Android
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

package twitter4j;

import java.io.Serializable;

/**
 * Created by mariotaku on 14/12/31.
 */
public interface CardEntity extends Serializable {

    String getName();

    User[] gerUsers();

    BindingValue getBindingValue(String key);

    BindingValue[] getBindingValues();

    public interface BindingValue extends Serializable {

        public static final String TYPE_STRING = "STRING";
        public static final String TYPE_IMAGE = "IMAGE";
        public static final String TYPE_USER = "USER";
        public static final String TYPE_BOOLEAN = "BOOLEAN";

        String getName();

        String getType();
    }


    public interface UserValue extends BindingValue {
        long getUserId();
    }

    public interface StringValue extends BindingValue {
        String getValue();
    }

    public interface BooleanValue extends BindingValue {
        boolean getValue();
    }

    public interface ImageValue extends BindingValue {
        int getWidth();

        int getHeight();

        String getUrl();
    }
}
