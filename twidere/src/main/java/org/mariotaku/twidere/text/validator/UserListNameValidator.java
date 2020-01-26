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

package org.mariotaku.twidere.text.validator;

import androidx.annotation.NonNull;

import com.rengwuxian.materialedittext.validation.METValidator;

/**
 * Created by mariotaku on 15/5/8.
 */
public class UserListNameValidator extends METValidator {
    public UserListNameValidator(@NonNull String errorMessage) {
        super(errorMessage);
    }

    @Override
    public boolean isValid(@NonNull CharSequence text, boolean isEmpty) {
        if (isEmpty) return false;
        for (int i = 0, j = text.length(); i < j; i++) {
            final char ch = text.charAt(i);
            if (i == 0 && !Character.isLetter(ch)) return false;
            if (!Character.isLetterOrDigit(ch) && ch != '-' && ch != '_') return false;
        }
        return true;
    }
}
