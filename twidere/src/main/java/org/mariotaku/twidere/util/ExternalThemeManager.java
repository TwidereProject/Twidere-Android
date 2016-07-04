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
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.LruCache;

import java.util.Locale;

import javax.inject.Singleton;

import static org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_EMOJI_SUPPORT;

/**
 * Created by mariotaku on 15/12/20.
 */
@Singleton
public class ExternalThemeManager {
    private final Application application;
    private final SharedPreferencesWrapper preferences;

    private Emoji emoji;
    private String emojiPackageName;

    public ExternalThemeManager(Application application, SharedPreferencesWrapper preferences) {
        this.application = application;
        this.preferences = preferences;
        reloadEmojiPreferences();
    }

    public String getEmojiPackageName() {
        return emojiPackageName;
    }

    public void reloadEmojiPreferences() {
        final String emojiComponentName = preferences.getString(KEY_EMOJI_SUPPORT, null);
        if (emojiComponentName != null) {
            final ComponentName componentName = ComponentName.unflattenFromString(emojiComponentName);
            if (componentName != null) {
                emojiPackageName = componentName.getPackageName();
            } else {
                emojiPackageName = null;
            }
        } else {
            emojiPackageName = null;
        }
        initEmojiSupport();
    }

    public void initEmojiSupport() {
        if (emojiPackageName == null) {
            emoji = null;
            return;
        }
        emoji = new Emoji(application, emojiPackageName);
    }

    @Nullable
    public Emoji getEmoji() {
        return emoji;
    }

    public static class Emoji {
        private final String packageName;
        private boolean useMipmap;
        private Resources resources;
        private LruCache<int[], Integer> identifierCache = new LruCache<>(512);

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

        public Drawable getEmojiDrawableFor(int... codePoints) {
            final Integer cached = identifierCache.get(codePoints);
            if (cached == null) {
                final StringBuilder sb = new StringBuilder("emoji_u");
                for (int i = 0; i < codePoints.length; i++) {
                    if (i != 0) {
                        sb.append("_");
                    }
                    sb.append(String.format(Locale.US, "%04x", codePoints[i]));
                }
                final int identifier = resources.getIdentifier(sb.toString(),
                        useMipmap ? "mipmap" : "drawable", packageName);
                identifierCache.put(codePoints, identifier);
                if (identifier == 0) return null;
                return ResourcesCompat.getDrawable(resources, identifier, null);
            } else if (cached != 0) {
                return ResourcesCompat.getDrawable(resources, cached, null);
            }
            return null;
        }

        public boolean isSupported() {
            return resources != null;
        }
    }
}
