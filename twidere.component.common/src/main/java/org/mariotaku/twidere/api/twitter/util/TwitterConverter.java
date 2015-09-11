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

package org.mariotaku.twidere.api.twitter.util;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

import org.mariotaku.restfu.Converter;
import org.mariotaku.restfu.Utils;
import org.mariotaku.restfu.http.ContentType;
import org.mariotaku.restfu.http.RestHttpResponse;
import org.mariotaku.restfu.http.mime.TypedData;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.api.twitter.model.AccountSettings;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.api.twitter.model.CardEntity;
import org.mariotaku.twidere.api.twitter.model.DirectMessage;
import org.mariotaku.twidere.api.twitter.model.ErrorInfo;
import org.mariotaku.twidere.api.twitter.model.ExtendedProfile;
import org.mariotaku.twidere.api.twitter.model.GeoLocation;
import org.mariotaku.twidere.api.twitter.model.HashtagEntity;
import org.mariotaku.twidere.api.twitter.model.IDs;
import org.mariotaku.twidere.api.twitter.model.Language;
import org.mariotaku.twidere.api.twitter.model.Location;
import org.mariotaku.twidere.api.twitter.model.MediaEntity;
import org.mariotaku.twidere.api.twitter.model.MediaUploadResponse;
import org.mariotaku.twidere.api.twitter.model.PageableResponseList;
import org.mariotaku.twidere.api.twitter.model.Place;
import org.mariotaku.twidere.api.twitter.model.QueryResult;
import org.mariotaku.twidere.api.twitter.model.Relationship;
import org.mariotaku.twidere.api.twitter.model.ResponseCode;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.SavedSearch;
import org.mariotaku.twidere.api.twitter.model.ScheduledStatus;
import org.mariotaku.twidere.api.twitter.model.ScheduledStatusesList;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.StatusActivitySummary;
import org.mariotaku.twidere.api.twitter.model.StatusDeletionNotice;
import org.mariotaku.twidere.api.twitter.model.TimeZone;
import org.mariotaku.twidere.api.twitter.model.TranslationResult;
import org.mariotaku.twidere.api.twitter.model.Trend;
import org.mariotaku.twidere.api.twitter.model.Trends;
import org.mariotaku.twidere.api.twitter.model.UrlEntity;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserList;
import org.mariotaku.twidere.api.twitter.model.UserMentionEntity;
import org.mariotaku.twidere.api.twitter.model.Warning;
import org.mariotaku.twidere.api.twitter.model.impl.AccountSettingsImpl;
import org.mariotaku.twidere.api.twitter.model.impl.ActivityImpl;
import org.mariotaku.twidere.api.twitter.model.impl.CardEntityImpl;
import org.mariotaku.twidere.api.twitter.model.impl.DirectMessageImpl;
import org.mariotaku.twidere.api.twitter.model.impl.ErrorInfoImpl;
import org.mariotaku.twidere.api.twitter.model.impl.ExtendedProfileImpl;
import org.mariotaku.twidere.api.twitter.model.impl.HashtagEntityImpl;
import org.mariotaku.twidere.api.twitter.model.impl.IDsImpl;
import org.mariotaku.twidere.api.twitter.model.impl.Indices;
import org.mariotaku.twidere.api.twitter.model.impl.LanguageImpl;
import org.mariotaku.twidere.api.twitter.model.impl.LocationImpl;
import org.mariotaku.twidere.api.twitter.model.impl.MediaEntityImpl;
import org.mariotaku.twidere.api.twitter.model.impl.MediaUploadResponseImpl;
import org.mariotaku.twidere.api.twitter.model.impl.PageableResponseListWrapper;
import org.mariotaku.twidere.api.twitter.model.impl.PlaceImpl;
import org.mariotaku.twidere.api.twitter.model.impl.QueryResultWrapper;
import org.mariotaku.twidere.api.twitter.model.impl.RelationshipImpl;
import org.mariotaku.twidere.api.twitter.model.impl.RelationshipWrapper;
import org.mariotaku.twidere.api.twitter.model.impl.ResponseListImpl;
import org.mariotaku.twidere.api.twitter.model.impl.SavedSearchImpl;
import org.mariotaku.twidere.api.twitter.model.impl.ScheduledStatusImpl;
import org.mariotaku.twidere.api.twitter.model.impl.ScheduledStatusesListWrapper;
import org.mariotaku.twidere.api.twitter.model.impl.StatusActivitySummaryImpl;
import org.mariotaku.twidere.api.twitter.model.impl.StatusDeletionNoticeImpl;
import org.mariotaku.twidere.api.twitter.model.impl.StatusImpl;
import org.mariotaku.twidere.api.twitter.model.impl.TimeZoneImpl;
import org.mariotaku.twidere.api.twitter.model.impl.TranslationResultImpl;
import org.mariotaku.twidere.api.twitter.model.impl.TrendImpl;
import org.mariotaku.twidere.api.twitter.model.impl.TrendsImpl;
import org.mariotaku.twidere.api.twitter.model.impl.TwitterResponseImpl;
import org.mariotaku.twidere.api.twitter.model.impl.TypeConverterMapper;
import org.mariotaku.twidere.api.twitter.model.impl.UrlEntityImpl;
import org.mariotaku.twidere.api.twitter.model.impl.UserImpl;
import org.mariotaku.twidere.api.twitter.model.impl.UserListImpl;
import org.mariotaku.twidere.api.twitter.model.impl.UserMentionEntityImpl;
import org.mariotaku.twidere.api.twitter.model.impl.WarningImpl;
import org.mariotaku.twidere.api.twitter.model.impl.Wrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mariotaku on 15/5/5.
 */
public class TwitterConverter implements Converter {

    private static final Map<Class<?>, Class<? extends Wrapper<?>>> wrapperMap = new HashMap<>();

    static {
        TypeConverterMapper.register(Status.class, StatusImpl.class);
        TypeConverterMapper.register(User.class, UserImpl.class);
        TypeConverterMapper.register(UserList.class, UserListImpl.class);
        TypeConverterMapper.register(DirectMessage.class, DirectMessageImpl.class);
        TypeConverterMapper.register(SavedSearch.class, SavedSearchImpl.class);
        TypeConverterMapper.register(UrlEntity.class, UrlEntityImpl.class);
        TypeConverterMapper.register(MediaEntity.class, MediaEntityImpl.class);
        TypeConverterMapper.register(MediaEntity.Size.class, MediaEntityImpl.SizeImpl.class);
        TypeConverterMapper.register(MediaEntity.Feature.class, MediaEntityImpl.FeatureImpl.class);
        TypeConverterMapper.register(MediaEntity.Feature.Face.class, MediaEntityImpl.FeatureImpl.FaceImpl.class);
        TypeConverterMapper.register(MediaEntity.VideoInfo.class, MediaEntityImpl.VideoInfoImpl.class);
        TypeConverterMapper.register(MediaEntity.VideoInfo.Variant.class, MediaEntityImpl.VideoInfoImpl.VariantImpl.class);
        TypeConverterMapper.register(UserMentionEntity.class, UserMentionEntityImpl.class);
        TypeConverterMapper.register(HashtagEntity.class, HashtagEntityImpl.class);
        TypeConverterMapper.register(CardEntity.class, CardEntityImpl.class);
        TypeConverterMapper.register(Place.class, PlaceImpl.class);
        TypeConverterMapper.register(Relationship.class, RelationshipImpl.class);
        TypeConverterMapper.register(MediaUploadResponse.class, MediaUploadResponseImpl.class);
        TypeConverterMapper.register(MediaUploadResponse.Image.class, MediaUploadResponseImpl.ImageImpl.class);
        TypeConverterMapper.register(ErrorInfo.class, ErrorInfoImpl.class);
        TypeConverterMapper.register(TranslationResult.class, TranslationResultImpl.class);
        TypeConverterMapper.register(Language.class, LanguageImpl.class);
        TypeConverterMapper.register(Trend.class, TrendImpl.class);
        TypeConverterMapper.register(Trends.class, TrendsImpl.class);
        TypeConverterMapper.register(Location.class, LocationImpl.class);
        TypeConverterMapper.register(Location.PlaceType.class, LocationImpl.PlaceTypeImpl.class);
        TypeConverterMapper.register(StatusActivitySummary.class, StatusActivitySummaryImpl.class);
        TypeConverterMapper.register(TimeZone.class, TimeZoneImpl.class);
        TypeConverterMapper.register(AccountSettings.class, AccountSettingsImpl.class);
        TypeConverterMapper.register(IDs.class, IDsImpl.class, IDsImpl.MAPPER);
        TypeConverterMapper.register(Activity.class, ActivityImpl.class, ActivityImpl.MAPPER);
        TypeConverterMapper.register(Warning.class, WarningImpl.class);
        TypeConverterMapper.register(StatusDeletionNotice.class, StatusDeletionNoticeImpl.class);
        TypeConverterMapper.register(ScheduledStatus.class, ScheduledStatusImpl.class);
        TypeConverterMapper.register(ExtendedProfile.class, ExtendedProfileImpl.class);

        LoganSquare.registerTypeConverter(Indices.class, Indices.CONVERTER);
        LoganSquare.registerTypeConverter(GeoLocation.class, GeoLocation.CONVERTER);
        LoganSquare.registerTypeConverter(CardEntity.BindingValue.class, CardEntityImpl.BindingValueWrapper.CONVERTER);
        LoganSquare.registerTypeConverter(IDs.class, IDsImpl.CONVERTER);
        LoganSquare.registerTypeConverter(MediaEntity.Type.class, EnumConverter.get(MediaEntity.Type.class));
        LoganSquare.registerTypeConverter(UserList.Mode.class, EnumConverter.get(UserList.Mode.class));
        LoganSquare.registerTypeConverter(Activity.Action.class, EnumConverter.get(Activity.Action.class));
        LoganSquare.registerTypeConverter(ScheduledStatus.State.class, EnumConverter.get(ScheduledStatus.State.class));

        registerWrapper(QueryResult.class, QueryResultWrapper.class);
        registerWrapper(PageableResponseList.class, PageableResponseListWrapper.class);
        registerWrapper(Relationship.class, RelationshipWrapper.class);
        registerWrapper(CardEntity.BindingValue.class, CardEntityImpl.BindingValueWrapper.class);
        registerWrapper(ScheduledStatusesList.class, ScheduledStatusesListWrapper.class);
    }

    public static TwitterException parseTwitterException(RestHttpResponse resp) {
        try {
            final TypedData body = resp.getBody();
            if (body == null) return new TwitterException(resp);
            final TwitterException parse = LoganSquare.parse(body.stream(), TwitterException.class);
            if (parse != null) return parse;
            return new TwitterException(resp);
        } catch (JsonParseException e) {
            return new TwitterException("Malformed JSON Data", e, resp);
        } catch (IOException e) {
            return new TwitterException("IOException while throwing exception", e, resp);
        }
    }

    private static <T> T parseOrThrow(RestHttpResponse resp, InputStream stream, Class<T> cls) throws IOException, TwitterException {
        try {
            final T parse = LoganSquare.parse(stream, cls);
            if (TwitterException.class.isAssignableFrom(cls) && parse == null) {
                throw new TwitterException();
            }
            return parse;
        } catch (JsonParseException e) {
            throw new TwitterException("Malformed JSON Data", resp);
        }
    }

    private static <T> List<T> parseListOrThrow(RestHttpResponse resp, InputStream stream, Class<T> elementCls) throws IOException, TwitterException {
        try {
            return LoganSquare.parseList(stream, elementCls);
        } catch (JsonParseException e) {
            throw new TwitterException("Malformed JSON Data", resp);
        }
    }

    private static <T> void registerWrapper(Class<T> cls, Class<? extends Wrapper<? extends T>> wrapperCls) {
        wrapperMap.put(cls, wrapperCls);
    }

    @Override
    public Object convert(RestHttpResponse response, Type type) throws Exception {
        final TypedData body = response.getBody();
        if (!response.isSuccessful()) {
            throw parseOrThrow(response, body.stream(), TwitterException.class);
        }
        final ContentType contentType = body.contentType();
        final InputStream stream = body.stream();
        try {
            if (type instanceof Class<?>) {
                final Class<?> cls = (Class<?>) type;
                final Class<?> wrapperCls = wrapperMap.get(cls);
                if (wrapperCls != null) {
                    final Wrapper<?> wrapper = (Wrapper<?>) parseOrThrow(response, stream, wrapperCls);
                    wrapper.processResponseHeader(response);
                    return wrapper.getWrapped(null);
                } else if (OAuthToken.class.isAssignableFrom(cls)) {
                    final ByteArrayOutputStream os = new ByteArrayOutputStream();
                    body.writeTo(os);
                    Charset charset = contentType != null ? contentType.getCharset() : null;
                    if (charset == null) {
                        charset = Charset.defaultCharset();
                    }
                    try {
                        return new OAuthToken(os.toString(charset.name()), charset);
                    } catch (ParseException e) {
                        throw new IOException(e);
                    }
                } else if (ResponseCode.class.isAssignableFrom(cls)) {
                    return new ResponseCode(response);
                }
                final Object object = parseOrThrow(response, stream, cls);
                checkResponse(cls, object, response);
                if (object instanceof TwitterResponseImpl) {
                    ((TwitterResponseImpl) object).processResponseHeader(response);
                }
                return object;
            } else if (type instanceof ParameterizedType) {
                final Type rawType = ((ParameterizedType) type).getRawType();
                if (rawType instanceof Class<?>) {
                    final Class<?> rawClass = (Class<?>) rawType;
                    final Class<?> wrapperCls = wrapperMap.get(rawClass);
                    if (wrapperCls != null) {
                        final Wrapper<?> wrapper = (Wrapper<?>) parseOrThrow(response, stream, wrapperCls);
                        wrapper.processResponseHeader(response);
                        return wrapper.getWrapped(((ParameterizedType) type).getActualTypeArguments());
                    } else if (ResponseList.class.isAssignableFrom(rawClass)) {
                        final Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                        final ResponseListImpl<?> responseList = new ResponseListImpl<>(parseListOrThrow(response, stream, (Class<?>) elementType));
                        responseList.processResponseHeader(response);
                        return responseList;
                    }
                }
            }
            throw new UnsupportedTypeException(type);
        } finally {
            Utils.closeSilently(stream);
        }
    }

    private void checkResponse(Class<?> cls, Object object, RestHttpResponse response) throws TwitterException {
        if (User.class.isAssignableFrom(cls)) {
            if (object == null) throw new TwitterException("User is null");
        }
    }

    private static class EnumConverter<T extends Enum<T>> implements TypeConverter<T> {
        private final Class<T> cls;

        EnumConverter(Class<T> cls) {
            this.cls = cls;
        }

        public static <T extends Enum<T>> EnumConverter<T> get(Class<T> cls) {
            return new EnumConverter<>(cls);
        }

        @SuppressWarnings({"unchecked", "TryWithIdenticalCatches"})
        @Override
        public T parse(JsonParser jsonParser) throws IOException {
            try {
                final Method method = cls.getMethod("parse", String.class);
                return (T) method.invoke(null, jsonParser.getValueAsString());
            } catch (NoSuchMethodException e) {
                return Enum.valueOf(cls, jsonParser.getValueAsString());
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void serialize(T object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) {
            throw new UnsupportedOperationException();
        }
    }

    public static class UnsupportedTypeException extends UnsupportedOperationException {
        public UnsupportedTypeException(Type type) {
            super("Unsupported type " + type);
        }
    }
}
