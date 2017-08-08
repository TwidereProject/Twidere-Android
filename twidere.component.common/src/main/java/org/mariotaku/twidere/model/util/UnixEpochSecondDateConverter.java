/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.model.util;

import com.bluelinelabs.logansquare.typeconverters.LongBasedTypeConverter;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by mariotaku on 2017/3/25.
 */

public class UnixEpochSecondDateConverter extends LongBasedTypeConverter<Date> {
    @Override
    public Date getFromLong(final long l) {
        return new Date(TimeUnit.SECONDS.toMillis(l));
    }

    @Override
    public long convertToLong(final Date object) {
        if (object == null) return -1;
        return TimeUnit.MILLISECONDS.toSeconds(object.getTime());
    }

}
