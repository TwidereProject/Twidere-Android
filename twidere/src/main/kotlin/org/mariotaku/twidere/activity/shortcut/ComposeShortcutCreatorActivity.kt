/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity.shortcut

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants.INTENT_ACTION_COMPOSE

class ComposeShortcutCreatorActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val shortcut = ShortcutInfoCompat.Builder(this, "compose")
                .setIcon(IconCompat.createWithResource(this, R.mipmap.ic_shortcut_compose))
                .setShortLabel(getString(R.string.action_compose))
                .setIntent(Intent(INTENT_ACTION_COMPOSE).setPackage(BuildConfig.APPLICATION_ID))
                .build()
        setResult(RESULT_OK, ShortcutManagerCompat.createShortcutResultIntent(this, shortcut))
        finish()
    }
}
