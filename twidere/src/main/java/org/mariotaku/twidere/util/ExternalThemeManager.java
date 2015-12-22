/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.util.SparseIntArray;

import org.mariotaku.twidere.Constants;

/**
 * Created by mariotaku on 15/12/20.
 */
public class ExternalThemeManager implements Constants {
    private final Emoji emoji;

    public ExternalThemeManager(Application application, SharedPreferencesWrapper preferences) {
        final String emojiComponentName = preferences.getString(KEY_EMOJI_SUPPORT, null);
        String packageName = null;
        if (emojiComponentName != null) {
            final ComponentName componentName = ComponentName.unflattenFromString(emojiComponentName);
            if (componentName != null) {
                packageName = componentName.getPackageName();
            }
        }
        emoji = new Emoji(application, packageName);
    }

    @NonNull
    public Emoji getEmoji() {
        return emoji;
    }

    public static class Emoji {
        private final String packageName;
        private boolean useMipmap;
        private Resources resources;
        private SparseIntArray identifierCache = new SparseIntArray();

        public Emoji(Application application, String packageName) {
            this.packageName = packageName;
            initResources(application, packageName);
        }

        private void initResources(Application application, String packageName) {
            if (packageName == null) {
                useMipmap = false;
                resources = null;
                return;
            }
            try {
                final PackageManager pm = application.getPackageManager();
                final ApplicationInfo info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                if (info.metaData != null) {
                    this.useMipmap = info.metaData.getBoolean("org.mariotaku.twidere.extension.emoji.mipmap");
                }
                this.resources = pm.getResourcesForApplication(info);
            } catch (PackageManager.NameNotFoundException e) {
                // Ignore
            }
        }

        public Drawable getEmojiDrawableFor(int codePoint) {
            int cached = identifierCache.get(codePoint, -1);
            if (cached == 0) return null;
            else if (cached != -1)
                return ResourcesCompat.getDrawable(resources, cached, null);
            final int identifier = resources.getIdentifier("emoji_u" + Integer.toHexString(codePoint),
                    useMipmap ? "mipmap" : "drawable", packageName);
            identifierCache.put(codePoint, identifier);
            if (identifier == 0) return null;
            return ResourcesCompat.getDrawable(resources, identifier, null);
        }

        public boolean isSupported() {
            return resources != null;
        }
    }
}
