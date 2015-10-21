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

package org.mariotaku.twidere.api.twitter.model;

import org.mariotaku.library.logansquare.extension.annotation.Implementation;
import org.mariotaku.library.logansquare.extension.annotation.Wrapper;
import org.mariotaku.twidere.api.twitter.model.impl.CardEntityImpl;

import java.util.Map;

/**
 * Created by mariotaku on 14/12/31.
 */
@Implementation(CardEntityImpl.class)
public interface CardEntity {

    String getName();

    String getUrl();

    User[] getUsers();

    BindingValue getBindingValue(String key);

    Map<String, BindingValue> getBindingValues();

    @Wrapper(CardEntityImpl.BindingValueWrapper.class)
    interface BindingValue {

        String TYPE_STRING = "STRING";
        String TYPE_IMAGE = "IMAGE";
        String TYPE_USER = "USER";
        String TYPE_BOOLEAN = "BOOLEAN";

    }


    interface UserValue extends BindingValue {
        long getUserId();
    }

    interface StringValue extends BindingValue {
        String getValue();
    }

    interface BooleanValue extends BindingValue {
        boolean getValue();
    }

    interface ImageValue extends BindingValue {
        int getWidth();

        int getHeight();

        String getUrl();
    }
}
