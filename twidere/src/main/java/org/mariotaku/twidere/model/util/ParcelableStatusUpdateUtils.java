package org.mariotaku.twidere.model.util;

import android.content.Context;

import org.mariotaku.twidere.model.DraftItem;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.util.DataStoreUtils;

/**
 * Created by mariotaku on 16/2/12.
 */
public class ParcelableStatusUpdateUtils {
        public static ParcelableStatusUpdate fromDraftItem(final Context context, final DraftItem draft) {
            ParcelableStatusUpdate statusUpdate = new ParcelableStatusUpdate();
            statusUpdate.accounts = DataStoreUtils.getAccounts(context, draft.account_ids);
            statusUpdate.text = draft.text;
            statusUpdate.location = draft.location;
            statusUpdate.media = draft.media;
            statusUpdate.in_reply_to_status_id = draft.in_reply_to_status_id;
            statusUpdate.is_possibly_sensitive = draft.is_possibly_sensitive;
            return statusUpdate;
        }
}
