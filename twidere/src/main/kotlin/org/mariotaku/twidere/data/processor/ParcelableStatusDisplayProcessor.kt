package org.mariotaku.twidere.data.processor

import android.content.ContentResolver
import android.content.Context
import org.mariotaku.twidere.extension.model.generateDisplayInfo
import org.mariotaku.twidere.model.ParcelableStatus

class ParcelableStatusDisplayProcessor(val context: Context) : DataSourceItemProcessor<ParcelableStatus> {
    override fun init(resolver: ContentResolver) {

    }

    override fun invalidate() {
    }

    override fun process(obj: ParcelableStatus): ParcelableStatus = obj.apply {
        display = generateDisplayInfo(context)
    }

}
