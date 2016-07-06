/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.util

import android.net.Uri
import android.support.v4.app.Fragment
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import org.mariotaku.twidere.model.ParcelableCardEntity
import org.mariotaku.twidere.model.util.ParcelableCardEntityUtils

/**
 * Created by mariotaku on 15/1/1.
 */
class TwitterCardFragmentFactoryImpl : TwitterCardFragmentFactory() {

    override fun createAnimatedGifFragment(card: ParcelableCardEntity): Fragment? {
        return null
    }

    override fun createAudioFragment(card: ParcelableCardEntity): Fragment? {
        return null
    }

    override fun createPlayerFragment(card: ParcelableCardEntity): Fragment? {
        if (java.lang.Boolean.parseBoolean("true")) return null
        val appUrlResolved = ParcelableCardEntityUtils.getString(card, "app_url_resolved")
        val domain = ParcelableCardEntityUtils.getString(card, "domain")
        if (domain != null && appUrlResolved != null) {
            val uri = Uri.parse(appUrlResolved)
            val paramV = uri.getQueryParameter("v")
            if ("www.youtube.com" == domain && paramV != null) {
                val fragment = YouTubePlayerSupportFragment.newInstance()
                fragment.initialize(YOUTUBE_DATA_API_KEY, object : YouTubePlayer.OnInitializedListener {
                    override fun onInitializationSuccess(provider: YouTubePlayer.Provider, player: YouTubePlayer, wasRestored: Boolean) {
                        if (!wasRestored) {
                            player.cueVideo(paramV)
                        }
                    }

                    override fun onInitializationFailure(provider: YouTubePlayer.Provider, errorReason: YouTubeInitializationResult) {
                        val activity = fragment.activity ?: return
//                        if (errorReason.isUserRecoverableError()) {
                        //                            errorReason.getErrorDialog(activity, RECOVERY_DIALOG_REQUEST).show();
                        //                        } else {
                        //                            Toast.makeText(activity, errorReason.toString(), Toast.LENGTH_LONG).show();
                        //                        }
                    }
                })
                return fragment
            }
        }
        return null
    }

    companion object {

        private val YOUTUBE_DATA_API_KEY = "AIzaSyCVdCIMFFxdNqHnCPrJ9yKUzoTfs8jhYGc"
    }

}
