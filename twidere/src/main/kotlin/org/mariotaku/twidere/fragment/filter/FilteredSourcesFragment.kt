package org.mariotaku.twidere.fragment.filter

import android.net.Uri
import org.mariotaku.twidere.provider.TwidereDataStore.Filters

class FilteredSourcesFragment : BaseFiltersFragment() {

    override val contentColumns: Array<String> = Filters.Sources.COLUMNS

    override val contentUri: Uri = Filters.Sources.CONTENT_URI

}