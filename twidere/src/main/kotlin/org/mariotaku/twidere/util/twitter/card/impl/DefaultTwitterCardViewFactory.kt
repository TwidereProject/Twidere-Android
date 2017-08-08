package org.mariotaku.twidere.util.twitter.card.impl

import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.util.twitter.card.TwitterCardViewFactory
import org.mariotaku.twidere.view.ContainerView

/**
 * Created by mariotaku on 2017/1/25.
 */

class DefaultTwitterCardViewFactory : TwitterCardViewFactory() {
    override fun from(status: ParcelableStatus): ContainerView.ViewController? {
        return null
    }

}
