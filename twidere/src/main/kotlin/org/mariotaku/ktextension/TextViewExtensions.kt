package org.mariotaku.ktextension

import android.widget.TextView

val TextView.empty: Boolean
    get() = length() <= 0
