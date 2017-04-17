package org.mariotaku.twidere.model.tab.impl

import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.TabAccountFlags
import org.mariotaku.twidere.fragment.message.MessagesEntriesFragment
import org.mariotaku.twidere.model.tab.DrawableHolder
import org.mariotaku.twidere.model.tab.StringHolder
import org.mariotaku.twidere.model.tab.TabConfiguration

/**
 * Created by mariotaku on 2016/11/27.
 */

class MessagesTabConfiguration : TabConfiguration() {
    override val name = StringHolder.resource(R.string.title_direct_messages)

    override val icon = DrawableHolder.Builtin.MESSAGE

    override val accountFlags = TabAccountFlags.FLAG_HAS_ACCOUNT or
            TabAccountFlags.FLAG_ACCOUNT_MULTIPLE or TabAccountFlags.FLAG_ACCOUNT_MUTABLE

    override val fragmentClass = MessagesEntriesFragment::class.java
}
