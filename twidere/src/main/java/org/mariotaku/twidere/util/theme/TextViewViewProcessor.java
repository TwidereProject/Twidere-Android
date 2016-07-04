package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

import static org.mariotaku.twidere.constant.SharedPreferenceConstants.VALUE_THEME_NAME_DARK;

/**
 * Created by mariotaku on 16/3/30.
 */
public class TextViewViewProcessor implements ViewProcessor<TextView, Void> {
    @Override
    public void process(@NonNull final Context context, @Nullable final String key,
                        @Nullable final TextView target, @Nullable Void extra) {
        if (target == null) return;
        switch (target.getId()) {
            case android.support.v7.appcompat.R.id.action_bar_title: {
                if (VALUE_THEME_NAME_DARK.equals(key)) return;
                target.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        if (Config.isLightToolbar(context, null, key, Config.toolbarColor(context, key, null))) {
                            target.setTextColor(Config.textColorPrimary(context, key));
                        } else {
                            target.setTextColor(Config.textColorPrimaryInverse(context, key));
                        }
                    }
                });
                break;
            }
            case android.support.v7.appcompat.R.id.action_bar_subtitle: {
                if (VALUE_THEME_NAME_DARK.equals(key)) return;
                target.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        if (Config.isLightToolbar(context, null, key, Config.toolbarColor(context, key, null))) {
                            target.setTextColor(Config.textColorSecondary(context, key));
                        } else {
                            target.setTextColor(Config.textColorSecondaryInverse(context, key));
                        }
                    }
                });
                break;
            }
        }
    }

    abstract static class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
