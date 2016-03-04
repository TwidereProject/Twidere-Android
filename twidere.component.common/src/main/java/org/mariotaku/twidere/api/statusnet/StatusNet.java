package org.mariotaku.twidere.api.statusnet;

import org.mariotaku.twidere.api.statusnet.api.GroupResources;
import org.mariotaku.twidere.api.statusnet.api.SearchResources;
import org.mariotaku.twidere.api.statusnet.api.StatusNetResources;
import org.mariotaku.twidere.api.statusnet.api.UserResources;

/**
 * Created by mariotaku on 16/3/4.
 */
public interface StatusNet extends StatusNetResources, GroupResources, SearchResources, UserResources {
}
