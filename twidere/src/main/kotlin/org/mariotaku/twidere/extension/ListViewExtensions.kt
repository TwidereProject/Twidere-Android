package org.mariotaku.twidere.extension

import android.view.Menu
import android.widget.ListView
import org.mariotaku.ktextension.setItemAvailability
import org.mariotaku.twidere.R

fun ListView.selectAll() {
    for (i in 0 until count) {
        setItemChecked(i, true)
    }

}

fun ListView.selectNone() {
    for (i in 0 until count) {
        setItemChecked(i, false)
    }
}

fun ListView.invertSelection() {
    val positions = checkedItemPositions
    for (i in 0 until count) {
        setItemChecked(i, !positions.get(i))
    }
}

fun ListView.updateSelectionItems(menu: Menu) {
    val checkedCount = checkedItemCount
    val listCount = count
    menu.setItemAvailability(R.id.select_none, checkedCount > 0)
    menu.setItemAvailability(R.id.select_all, checkedCount < listCount)
    menu.setItemAvailability(R.id.invert_selection, checkedCount in 1 until listCount)
}
