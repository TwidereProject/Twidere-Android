package org.mariotaku.twidere.fragment.iface

import android.support.v7.widget.RecyclerView

interface RecycledViewPoolAwareHost {
    val recycledViewPool: RecyclerView.RecycledViewPool
}
