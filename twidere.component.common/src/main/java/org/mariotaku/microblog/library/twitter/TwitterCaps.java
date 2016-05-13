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

package org.mariotaku.microblog.library.twitter;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.CardDataMap;
import org.mariotaku.microblog.library.twitter.model.CardResponse;
import org.mariotaku.microblog.library.twitter.model.CreateCardData;
import org.mariotaku.microblog.library.twitter.model.CreateCardResult;

/**
 * Card API maybe??
 * Host is caps.twitter.com
 * Created by mariotaku on 15/12/30.
 */
public interface TwitterCaps {

    @GET("/v2/capi/passthrough/1")
    CardResponse getPassThrough(@Query CardDataMap params)
            throws MicroBlogException;

    @POST("/v2/capi/passthrough/1")
    @BodyType(BodyType.FORM)
    CardResponse sendPassThrough(@Param CardDataMap params) throws MicroBlogException;

    @POST("/v2/cards/create.json")
    CreateCardResult createCard(@Param("card_data") CreateCardData cardData) throws MicroBlogException;
}
