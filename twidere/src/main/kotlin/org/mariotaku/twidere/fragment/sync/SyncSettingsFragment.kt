package org.mariotaku.twidere.fragment.sync

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import org.mariotaku.twidere.Constants.SHARED_PREFERENCES_NAME
import org.mariotaku.twidere.R
import org.mariotaku.twidere.fragment.BasePreferenceFragment

/**
 * Created by mariotaku on 2017/1/3.
 */

class SyncSettingsFragment : BasePreferenceFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SHARED_PREFERENCES_NAME
        addPreferencesFromResource(R.xml.preferences_sync)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sync_settings, menu)
    }
}
