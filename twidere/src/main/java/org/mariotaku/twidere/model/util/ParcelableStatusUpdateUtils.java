package org.mariotaku.twidere.model.util;

import android.content.Context;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.DraftItem;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.util.DataStoreUtils;

/**
 * Created by mariotaku on 16/2/12.
 */
public class ParcelableStatusUpdateUtils implements Constants {

    public static ParcelableStatusUpdate fromDraftItem(final Context context, final DraftItem draft) {
        ParcelableStatusUpdate statusUpdate = new ParcelableStatusUpdate();
        statusUpdate.accounts = DataStoreUtils.getAccounts(context, draft.account_ids);
        statusUpdate.text = draft.text;
        statusUpdate.location = draft.location;
        statusUpdate.media = draft.media;
        if (draft.action_extras != null) {
            statusUpdate.in_reply_to_status = draft.action_extras.getParcelable(EXTRA_IN_REPLY_TO_STATUS);
            statusUpdate.is_possibly_sensitive = draft.action_extras.getBoolean(EXTRA_IS_POSSIBLY_SENSITIVE);
        }
        return statusUpdate;
    }

}
