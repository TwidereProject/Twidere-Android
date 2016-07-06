package org.mariotaku.twidere.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.mariotaku.twidere.R

class InvalidTabFragment : BaseSupportFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_invalid_tab, container, false)
    }

}
