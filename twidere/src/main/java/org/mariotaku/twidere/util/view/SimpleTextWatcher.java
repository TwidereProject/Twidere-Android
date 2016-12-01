package org.mariotaku.twidere.util.view;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by mariotaku on 2016/11/30.
 */
public abstract class SimpleTextWatcher implements TextWatcher {

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
