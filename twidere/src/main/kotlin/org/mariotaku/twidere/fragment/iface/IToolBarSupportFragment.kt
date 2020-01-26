package org.mariotaku.twidere.fragment.iface

import androidx.fragment.app.FragmentActivity
import androidx.appcompat.widget.Toolbar

/**
 * Created by mariotaku on 16/3/16.
 */
interface IToolBarSupportFragment {

    val toolbar: Toolbar

    var controlBarOffset: Float

    val controlBarHeight: Int

    fun setupWindow(activity: FragmentActivity): Boolean
}
