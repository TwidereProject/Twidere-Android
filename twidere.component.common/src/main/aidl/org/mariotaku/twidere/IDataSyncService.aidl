// IDataSyncService.aidl
package org.mariotaku.twidere;

// Declare any non-default types here with import statements
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;

interface IDataSyncService {
    String getAuthInfo();

    Intent getAuthRequestIntent(in String info);

    void onPerformSync(in String info, in Bundle extras, in SyncResult syncResult);
}
