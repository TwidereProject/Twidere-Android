package org.mariotaku.twidere.fragment.filter

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_URI
import org.mariotaku.twidere.fragment.filter.BaseFiltersFragment
import org.mariotaku.twidere.provider.TwidereDataStore

class FilteredSourcesFragment : BaseFiltersFragment() {

    override val contentColumns: Array<String>
        get() = TwidereDataStore.Filters.Sources.COLUMNS

    override val contentUri: Uri
        get() = TwidereDataStore.Filters.Sources.CONTENT_URI

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add -> {
                val args = Bundle()
                args.putInt(EXTRA_AUTO_COMPLETE_TYPE, AUTO_COMPLETE_TYPE_SOURCES)
                args.putParcelable(EXTRA_URI, contentUri)
                val dialog = AddItemFragment()
                dialog.arguments = args
                dialog.show(fragmentManager, "add_rule")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}