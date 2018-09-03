package org.mariotaku.twidere.extension.databinding

import android.databinding.BindingAdapter
import android.view.View

@BindingAdapter("activated")
fun View.setActivatedBinding(activated: Boolean) {
    isActivated = activated
}