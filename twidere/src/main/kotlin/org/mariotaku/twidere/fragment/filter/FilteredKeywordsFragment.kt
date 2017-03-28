package org.mariotaku.twidere.fragment.filter

import android.net.Uri
import org.mariotaku.twidere.provider.TwidereDataStore.Filters

class FilteredKeywordsFragment : BaseFiltersFragment() {

    override val contentUri: Uri = Filters.Keywords.CONTENT_URI

    override val contentColumns: Array<String> = Filters.Keywords.COLUMNS

}