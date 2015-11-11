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

package edu.tsinghua.hotmobi.model;

import android.content.Context;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

/**
 * Created by mariotaku on 15/11/11.
 */
@JsonObject
public class ScreenEvent extends BaseEvent {

    @JsonField(name = "action", typeConverter = Action.ScreenActionConverter.class)
    Action action;

    public static ScreenEvent create(Context context, Action action) {
        final ScreenEvent event = new ScreenEvent();
        event.markStart(context);
        event.setAction(action);
        return event;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "ScreenEvent{" +
                "action=" + action +
                "} " + super.toString();
    }

    public enum Action {
        ON("on"), OFF("off"), UNKNOWN("unknown");
        private final String value;

        Action(String value) {
            this.value = value;
        }

        public static Action parse(String action) {
            if (ON.value.equalsIgnoreCase(action)) {
                return ON;
            } else if (OFF.value.equalsIgnoreCase(action)) {
                return OFF;
            }
            return UNKNOWN;
        }


        public static class ScreenActionConverter extends StringBasedTypeConverter<Action> {

            @Override
            public Action getFromString(String string) {
                return Action.parse(string);
            }

            @Override
            public String convertToString(Action action) {
                if (action == null) return null;
                return action.value;
            }
        }
    }
}
