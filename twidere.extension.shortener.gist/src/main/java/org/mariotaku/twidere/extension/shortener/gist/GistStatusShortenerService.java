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

package org.mariotaku.twidere.extension.shortener.gist;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.StatusShortenResult;
import org.mariotaku.twidere.service.StatusShortenerService;

/**
 * Created by mariotaku on 15/6/4.
 */
public class GistStatusShortenerService extends StatusShortenerService {

    @Override
    protected StatusShortenResult shorten(ParcelableStatusUpdate status, long currentAccountId, String overrideStatusText) {
        final Github github = GithubFactory.getInstance(getApiKey());
        final NewGist newGist = new NewGist();
        newGist.setDescription("long tweet");
        newGist.setIsPublic(false);
        final String content = overrideStatusText != null ? overrideStatusText : status.text;
        newGist.putFile("long_tweet.txt", new GistFile(content));
        try {
            Gist gist = github.createGist(newGist);
            final StatusShortenResult shortened = StatusShortenResult.shortened(getShortenedStatus(content, gist.getHtmlUrl()));
            shortened.extra = gist.getId();
            return shortened;
        } catch (GithubException e) {
            return StatusShortenResult.error(-1, e.getMessage());
        }
    }

    @Override
    protected boolean callback(StatusShortenResult result, ParcelableStatus status) {
        final String apiKey = getApiKey();
        if (apiKey == null) return false;
        final Github github = GithubFactory.getInstance(apiKey);
        final NewGist newGist = new NewGist();
        newGist.setDescription("https://twitter.com/" + status.user_screen_name + "/status/" + status.id);
        try {
            github.updateGist(result.extra, newGist);
        } catch (GithubException e) {
            return false;
        }
        return true;
    }

    private String getApiKey() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString("api_key", null);
    }

    private String getShortenedStatus(String content, String htmlUrl) {
        final int codePointCount = content.codePointCount(0, content.length());
        final int offset = content.offsetByCodePoints(0, Math.min(99, codePointCount));
        return content.substring(0, offset) + " " + htmlUrl;
    }

}
