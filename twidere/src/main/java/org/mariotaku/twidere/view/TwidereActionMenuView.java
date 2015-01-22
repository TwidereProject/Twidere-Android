/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.support.v7.internal.view.menu.MenuPopupHelper;
import android.support.v7.widget.ActionMenuView;
import android.util.AttributeSet;

import java.lang.reflect.Field;

/**
 * Created by mariotaku on 15/1/22.
 */
public class TwidereActionMenuView extends ActionMenuView {

    public TwidereActionMenuView(Context context) {
        super(context);
    }

    public TwidereActionMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    @Override
//    public boolean post(Runnable action) {
//        try {
//            final Class<?> actionCls = action.getClass();
//            final Field popupField = actionCls.getField("mPopup");
//            popupField.setAccessible(true);
//            final Object popupObject = popupField.get(action);
//            if (popupObject instanceof MenuPopupHelper) {
//                ((MenuPopupHelper) popupObject).setForceShowIcon(true);
//            }
//        } catch (NoSuchFieldException | IllegalAccessException ignore) {
//        }
//        return super.post(action);
//    }
}
