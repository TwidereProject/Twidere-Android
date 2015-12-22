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

package org.mariotaku.twidere.text.util;

import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.widget.TextView;

import org.mariotaku.twidere.util.EmojiSupportUtils;
import org.mariotaku.twidere.util.ExternalThemeManager;
import org.mariotaku.twidere.util.dagger.ApplicationModule;
import org.mariotaku.twidere.util.dagger.DaggerGeneralComponent;

import javax.inject.Inject;

/**
 * Created by mariotaku on 15/12/20.
 */
public class EmojiEditableFactory extends SafeEditableFactory {

    @Inject
    ExternalThemeManager externalThemeManager;

    public EmojiEditableFactory(TextView textView) {
        DaggerGeneralComponent.builder().applicationModule(ApplicationModule.get(textView.getContext())).build().inject(this);
    }

    @Override
    public Editable newEditable(CharSequence source) {
        final Editable editable = super.newEditable(source);
        EmojiSupportUtils.applyEmoji(externalThemeManager, editable);
        editable.setSpan(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count <= 0) return;
                EmojiSupportUtils.applyEmoji(externalThemeManager, editable,
                        start, count);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        }, 0, editable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return editable;
    }
}
