package org.mariotaku.twidere.task

import android.content.Context
import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.model.SingleResponse

/**
 * Created by mariotaku on 2017/2/8.
 */
class SendMessageTask(
        context: Context
) : BaseAbstractTask<Unit, SingleResponse<ParcelableMessage>, Unit>(context) {
    override fun doLongOperation(params: Unit?): SingleResponse<ParcelableMessage> {
        return SingleResponse(UnsupportedOperationException())
    }

}