package org.mariotaku.twidere.model.util;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.api.twitter.model.CardEntity;
import org.mariotaku.twidere.model.AccountId;
import org.mariotaku.twidere.model.ParcelableCardEntity;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Created by mariotaku on 16/2/24.
 */
public class ParcelableCardEntityUtils implements TwidereConstants {

    @Nullable
    public static ParcelableCardEntity fromCardEntity(@Nullable CardEntity card, AccountId accountId) {
        if (card == null) return null;
        final ParcelableCardEntity obj = new ParcelableCardEntity();
        obj.name = card.getName();
        obj.url = card.getUrl();
        obj.users = ParcelableUserUtils.fromUsers(card.getUsers(), accountId);
        obj.account_id = accountId.getId();
        obj.values = from(card.getBindingValues());
        return obj;
    }

    public static Map<String, ParcelableCardEntity.ParcelableBindingValue> from(@Nullable Map<String, CardEntity.BindingValue> bindingValues) {
        if (bindingValues == null) return null;
        final ArrayMap<String, ParcelableCardEntity.ParcelableBindingValue> map = new ArrayMap<>();
        for (Map.Entry<String, CardEntity.BindingValue> entry : bindingValues.entrySet()) {
            map.put(entry.getKey(), new ParcelableCardEntity.ParcelableBindingValue(entry.getValue()));
        }
        return map;
    }

    public static boolean getAsBoolean(@NonNull ParcelableCardEntity obj, @NonNull String key, boolean def) {
        final ParcelableCardEntity.ParcelableBindingValue value = obj.getValue(key);
        if (value == null) return def;
        return Boolean.parseBoolean(value.value);
    }

    public static String getAsString(@NonNull ParcelableCardEntity obj, @NonNull String key, String def) {
        final ParcelableCardEntity.ParcelableBindingValue value = obj.getValue(key);
        if (value == null) return def;
        return value.value;
    }

    public static String getString(@NonNull ParcelableCardEntity obj, @NonNull String key) {
        final ParcelableCardEntity.ParcelableBindingValue value = obj.getValue(key);
        if (value == null || !CardEntity.BindingValue.TYPE_STRING.equals(value.type)) return null;
        return getAsString(obj, key, null);
    }

    public static int getAsInteger(@NonNull ParcelableCardEntity obj, @NonNull String key, int def) {
        final ParcelableCardEntity.ParcelableBindingValue value = obj.getValue(key);
        if (value == null) return def;
        return NumberUtils.toInt(value.value, def);
    }

    public static long getAsLong(@NonNull ParcelableCardEntity obj, @NonNull String key, long def) {
        final ParcelableCardEntity.ParcelableBindingValue value = obj.getValue(key);
        if (value == null) return def;
        return NumberUtils.toLong(value.value, def);
    }

    public static Date getAsDate(@NonNull ParcelableCardEntity obj, @NonNull String key, Date def) {
        final ParcelableCardEntity.ParcelableBindingValue value = obj.getValue(key);
        if (value == null) return def;
        try {
            return DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.parse(value.value);
        } catch (ParseException e) {
            return def;
        }
    }
}
