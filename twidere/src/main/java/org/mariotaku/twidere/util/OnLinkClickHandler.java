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

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;

import edu.tsinghua.spice.Utilies.SpiceProfilingUtil;
import edu.tsinghua.spice.Utilies.TypeMapingUtil;
import edu.ucdavis.earlybird.ProfilingUtil;

import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.openTweetSearch;
import static org.mariotaku.twidere.util.Utils.openUserListDetails;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

public class OnLinkClickHandler implements OnLinkClickListener, Constants {

    protected final Context context;
    protected final MultiSelectManager manager;

    public OnLinkClickHandler(final Context context, final MultiSelectManager manager) {
        this.context = context;
        this.manager = manager;
    }

    @Override
    public void onLinkClick(final String link, final String orig, final long account_id, final int type,
                            final boolean sensitive, int start, int end) {
        if (context == null || (manager != null && manager.isActive())) return;
        // UCD
        ProfilingUtil.profile(context, account_id, "Click, " + link + ", " + type);
        //spice
        SpiceProfilingUtil.profile(context, account_id, account_id + ",Visit," + link + "," + TypeMapingUtil.getLinkType(type));
        //end

        switch (type) {
            case TwidereLinkify.LINK_TYPE_MENTION: {
                openUserProfile(context, account_id, -1, link, null);
                break;
            }
            case TwidereLinkify.LINK_TYPE_HASHTAG: {
                openTweetSearch(context, account_id, "#" + link);
                break;
            }
            case TwidereLinkify.LINK_TYPE_LINK: {
                if (MediaPreviewUtils.isLinkSupported(link)) {
                    openMedia(account_id, sensitive, link, start, end);
                } else {
                    openLink(link);
                }
                break;
            }
            case TwidereLinkify.LINK_TYPE_LIST: {
                final String[] mentionList = link.split("/");
                if (mentionList.length != 2) {
                    break;
                }
                openUserListDetails(context, account_id, -1, -1, mentionList[0], mentionList[1]);
                break;
            }
            case TwidereLinkify.LINK_TYPE_CASHTAG: {
                openTweetSearch(context, account_id, link);
                break;
            }
            case TwidereLinkify.LINK_TYPE_USER_ID: {
                openUserProfile(context, account_id, ParseUtils.parseLong(link), null, null);
                break;
            }
            case TwidereLinkify.LINK_TYPE_STATUS: {
                openStatus(context, account_id, ParseUtils.parseLong(link));
                break;
            }
        }
    }

    protected void openMedia(long account_id, boolean sensitive, String link, int start, int end) {
        final ParcelableMedia[] media = {ParcelableMedia.newImage(link, link)};
        Utils.openMedia(context, account_id, sensitive, null, media);
    }

    protected void openLink(final String link) {
        if (context == null || (manager != null && manager.isActive())) return;
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (final ActivityNotFoundException e) {
            // TODO
        }
    }
}
