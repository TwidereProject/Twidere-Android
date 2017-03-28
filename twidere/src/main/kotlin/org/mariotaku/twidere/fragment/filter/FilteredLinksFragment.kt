package org.mariotaku.twidere.fragment.filter

import android.net.Uri
import org.mariotaku.twidere.provider.TwidereDataStore.Filters

class FilteredLinksFragment : BaseFiltersFragment() {

    override val contentColumns: Array<String> = Filters.Links.COLUMNS

    override val contentUri: Uri = Filters.Links.CONTENT_URI

}