/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.mariotaku.twidere.activity.WebLinkHandlerActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.UserFragment;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableMediaUtils;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;
import org.mariotaku.twidere.util.media.preview.PreviewMediaExtractor;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.LinkEvent;

import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY;
import static org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NEW_DOCUMENT_API;

public class OnLinkClickHandler implements OnLinkClickListener {

    @NonNull
    protected final Context context;
    @Nullable
    protected final MultiSelectManager manager;
    @NonNull
    protected final SharedPreferencesWrapper preferences;

    public OnLinkClickHandler(@NonNull final Context context, @Nullable final MultiSelectManager manager,
                              @NonNull SharedPreferencesWrapper preferences) {
        this.context = context;
        this.manager = manager;
        this.preferences = preferences;
    }

    @Override
    public boolean onLinkClick(final String link, final String orig, final UserKey accountKey,
                               final long extraId, final int type, final boolean sensitive,
                               final int start, final int end) {
        if (manager != null && manager.isActive()) return false;
        if (!isPrivateData()) {
            // BEGIN HotMobi
            final LinkEvent event = LinkEvent.create(context, link, type);
            HotMobiLogger.getInstance(context).log(accountKey, event);
            // END HotMobi
        }

        switch (type) {
            case TwidereLinkify.LINK_TYPE_MENTION: {
                IntentUtils.openUserProfile(context, accountKey, null, link, null,
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                        UserFragment.Referral.USER_MENTION);
                return true;
            }
            case TwidereLinkify.LINK_TYPE_HASHTAG: {
                IntentUtils.openTweetSearch(context, accountKey, "#" + link);
                return true;
            }
            case TwidereLinkify.LINK_TYPE_LINK_IN_TEXT: {
                if (isMedia(link, extraId)) {
                    openMedia(accountKey, extraId, sensitive, link, start, end);
                } else {
                    openLink(link);
                }
                return true;
            }
            case TwidereLinkify.LINK_TYPE_ENTITY_URL: {
                if (isMedia(link, extraId)) {
                    openMedia(accountKey, extraId, sensitive, link, start, end);
                } else {
                    final String authority = UriUtils.getAuthority(link);
                    if (authority == null) {
                        openLink(link);
                        return true;
                    }
                    switch (authority) {
                        case "fanfou.com": {
                            if (orig != null) {
                                // Process special case for fanfou
                                final char ch = orig.charAt(0);
                                // Extend selection
                                final int length = orig.length();
                                if (TwidereLinkify.isAtSymbol(ch)) {
                                    String id = UriUtils.getPath(link);
                                    if (id != null) {
                                        int idxOfSlash = id.indexOf('/');
                                        if (idxOfSlash == 0) {
                                            id = id.substring(1);
                                        }
                                        final String screenName = orig.substring(1, length);
                                        IntentUtils.openUserProfile(context, accountKey, UserKey.valueOf(id),
                                                screenName, null, preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                                                UserFragment.Referral.USER_MENTION);
                                        return true;
                                    }
                                } else if (TwidereLinkify.isHashSymbol(ch) &&
                                        TwidereLinkify.isHashSymbol(orig.charAt(length - 1))) {
                                    IntentUtils.openSearch(context, accountKey, orig.substring(1, length - 1));
                                    return true;
                                }
                            }
                            break;
                        }
                        default: {
                            if (IntentUtils.isWebLinkHandled(context, Uri.parse(link))) {
                                openTwitterLink(link, accountKey);
                                return true;
                            }
                            break;
                        }
                    }
                    openLink(link);
                }
                return true;
            }
            case TwidereLinkify.LINK_TYPE_LIST: {
                final String[] mentionList = StringUtils.split(link, "/");
                if (mentionList.length != 2) {
                    return false;
                }
                IntentUtils.openUserListDetails(context, accountKey, null, null, mentionList[0],
                        mentionList[1]);
                return true;
            }
            case TwidereLinkify.LINK_TYPE_CASHTAG: {
                IntentUtils.openTweetSearch(context, accountKey, link);
                return true;
            }
            case TwidereLinkify.LINK_TYPE_USER_ID: {
                IntentUtils.openUserProfile(context, accountKey, UserKey.valueOf(link), null, null,
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                        UserFragment.Referral.USER_MENTION);
                return true;
            }
        }
        return false;
    }

    protected boolean isPrivateData() {
        return false;
    }

    protected boolean isMedia(String link, long extraId) {
        return PreviewMediaExtractor.isSupported(link);
    }

    protected void openMedia(UserKey accountKey, long extraId, boolean sensitive, String link, int start, int end) {
        final ParcelableMedia[] media = {ParcelableMediaUtils.image(link)};
        IntentUtils.openMedia(context, accountKey, sensitive, null, media, null,
                preferences.getBoolean(KEY_NEW_DOCUMENT_API));
    }

    protected void openLink(final String link) {
        if (manager != null && manager.isActive()) return;
        final Uri uri = Uri.parse(link);
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(IntentUtils.getDefaultBrowserPackage(context, uri, true));
        try {
            context.startActivity(intent);
        } catch (final ActivityNotFoundException e) {
            // TODO
        }
    }

    protected void openTwitterLink(final String link, final UserKey accountKey) {
        if (manager != null && manager.isActive()) return;
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, WebLinkHandlerActivity.class);
        intent.putExtra(EXTRA_ACCOUNT_KEY, accountKey);
        intent.setExtrasClassLoader(TwidereApplication.class.getClassLoader());
        try {
            context.startActivity(intent);
        } catch (final ActivityNotFoundException e) {
            // TODO
        }
    }
}
