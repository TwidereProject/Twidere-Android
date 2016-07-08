package org.mariotaku.twidere.fragment.iface

import android.support.v4.app.FragmentActivity
import android.support.v7.widget.Toolbar

/**
 * Created by mariotaku on 16/3/16.
 */
interface IToolBarSupportFragment {

    val toolbar: Toolbar

    var controlBarOffset: Float

    val controlBarHeight: Int

    fun setupWindow(activity: FragmentActivity): Boolean
}
