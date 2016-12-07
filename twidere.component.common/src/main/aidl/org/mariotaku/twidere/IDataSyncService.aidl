// IDataSyncService.aidl
package org.mariotaku.twidere;

// Declare any non-default types here with import statements
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;

import org.mariotaku.twidere.model.SyncAuthInfo;

interface IDataSyncService {
    SyncAuthInfo getAuthInfo();

    Intent getAuthRequestIntent(in SyncAuthInfo info);

    void onPerformSync(in SyncAuthInfo info, in Bundle extras, in SyncResult syncResult);
}
