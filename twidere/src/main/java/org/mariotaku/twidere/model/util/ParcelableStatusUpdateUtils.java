package org.mariotaku.twidere.model.util;

import android.content.Context;

import org.mariotaku.twidere.model.Draft;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.draft.UpdateStatusActionExtra;

/**
 * Created by mariotaku on 16/2/12.
 */
public class ParcelableStatusUpdateUtils {
    private ParcelableStatusUpdateUtils() {
    }

    public static ParcelableStatusUpdate fromDraftItem(final Context context, final Draft draft) {
        ParcelableStatusUpdate statusUpdate = new ParcelableStatusUpdate();
        if (draft.account_keys != null) {
            statusUpdate.accounts = ParcelableAccountUtils.getAccounts(context, draft.account_keys);
        } else {
            statusUpdate.accounts = new ParcelableAccount[0];
        }
        statusUpdate.text = draft.text;
        statusUpdate.location = draft.location;
        statusUpdate.media = draft.media;
        if (draft.action_extras instanceof UpdateStatusActionExtra) {
            final UpdateStatusActionExtra extra = (UpdateStatusActionExtra) draft.action_extras;
            statusUpdate.in_reply_to_status = extra.getInReplyToStatus();
            statusUpdate.is_possibly_sensitive = extra.isPossiblySensitive();
            statusUpdate.display_coordinates = extra.getDisplayCoordinates();
        }
        return statusUpdate;
    }

}
