package org.mariotaku.twidere.model.util;

import android.content.Context;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.Draft;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtra;

/**
 * Created by mariotaku on 16/2/12.
 */
public class ParcelableStatusUpdateUtils implements Constants {

    public static ParcelableStatusUpdate fromDraftItem(final Context context, final Draft draft) {
        ParcelableStatusUpdate statusUpdate = new ParcelableStatusUpdate();
        statusUpdate.accounts = ParcelableAccountUtils.getAccounts(context, draft.account_ids);
        statusUpdate.text = draft.text;
        statusUpdate.location = draft.location;
        statusUpdate.media = draft.media;
        if (draft.action_extras instanceof UpdateStatusActionExtra) {
            final UpdateStatusActionExtra extra = (UpdateStatusActionExtra) draft.action_extras;
            statusUpdate.in_reply_to_status = extra.getInReplyToStatus();
            statusUpdate.is_possibly_sensitive = extra.isPossiblySensitive();
        }
        return statusUpdate;
    }

}
