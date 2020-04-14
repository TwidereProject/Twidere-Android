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

package org.mariotaku.twidere.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;

import org.mariotaku.twidere.model.DraftTableInfo;
import org.mariotaku.twidere.model.FiltersData$BaseItemTableInfo;
import org.mariotaku.twidere.model.FiltersData$UserItemTableInfo;
import org.mariotaku.twidere.model.FiltersSubscriptionTableInfo;
import org.mariotaku.twidere.model.ParcelableActivityTableInfo;
import org.mariotaku.twidere.model.ParcelableMessageConversationTableInfo;
import org.mariotaku.twidere.model.ParcelableMessageTableInfo;
import org.mariotaku.twidere.model.ParcelableRelationshipTableInfo;
import org.mariotaku.twidere.model.ParcelableStatusTableInfo;
import org.mariotaku.twidere.model.ParcelableTrendTableInfo;
import org.mariotaku.twidere.model.ParcelableUserTableInfo;

@SuppressWarnings("unused")
public interface TwidereDataStore {

    String AUTHORITY = "twidere";

    String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY AUTOINCREMENT";
    String TYPE_INT = "INTEGER";
    String TYPE_INT_UNIQUE = "INTEGER UNIQUE";
    String TYPE_BOOLEAN = "INTEGER(1)";
    String TYPE_BOOLEAN_DEFAULT_TRUE = "INTEGER(1) DEFAULT 1";
    String TYPE_BOOLEAN_DEFAULT_FALSE = "INTEGER(1) DEFAULT 0";
    String TYPE_TEXT = "TEXT";
    String TYPE_DOUBLE_NOT_NULL = "DOUBLE NOT NULL";
    String TYPE_TEXT_NOT_NULL = "TEXT NOT NULL";
    String TYPE_TEXT_NOT_NULL_UNIQUE = "TEXT NOT NULL UNIQUE";

    String CONTENT_PATH_NULL = "null_content";
    String CONTENT_PATH_EMPTY = "empty_content";
    String CONTENT_PATH_RAW_QUERY = "raw_query";
    String CONTENT_PATH_DATABASE_PREPARE = "database_prepare";

    Uri BASE_CONTENT_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY);

    Uri CONTENT_URI_NULL = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH_NULL);
    Uri CONTENT_URI_EMPTY = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH_EMPTY);
    Uri CONTENT_URI_RAW_QUERY = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH_RAW_QUERY);
    Uri CONTENT_URI_DATABASE_PREPARE = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH_DATABASE_PREPARE);

    interface InsertedDateColumns {
        String INSERTED_DATE = "inserted_date";
        String INSERTED_DATE_TYPE = TYPE_INT;
    }

    interface AccountSupportColumns {

        String ACCOUNT_KEY = "account_id";

    }

    interface Accounts extends BaseColumns, AccountSupportColumns {

        String TABLE_NAME = "accounts";

        /**
         * Login name of the account<br>
         * Type: TEXT NOT NULL
         */
        String SCREEN_NAME = "screen_name";

        String NAME = "name";


        /**
         * Auth type of the account.</br> Type: INTEGER
         */
        String AUTH_TYPE = "auth_type";

        String BASIC_AUTH_USERNAME = "basic_auth_username";

        /**
         * Password of the account for basic auth.<br>
         * Type: TEXT
         */
        String BASIC_AUTH_PASSWORD = "basic_auth_password";

        /**
         * OAuth Token of the account.<br>
         * Type: TEXT
         */
        String OAUTH_TOKEN = "oauth_token";

        /**
         * Token Secret of the account.<br>
         * Type: TEXT
         */
        String OAUTH_TOKEN_SECRET = "oauth_token_secret";

        String COLOR = "color";

        /**
         * Set to a non-zero integer if the account is activated. <br>
         * Type: INTEGER (boolean)
         */
        String IS_ACTIVATED = "is_activated";

        String CONSUMER_KEY = "consumer_key";

        String CONSUMER_SECRET = "consumer_secret";

        String SORT_POSITION = "sort_position";

        /**
         * User's profile image URL of the status. <br>
         * Type: TEXT
         */
        String PROFILE_IMAGE_URL = "profile_image_url";

        String PROFILE_BANNER_URL = "profile_banner_url";
        String API_URL_FORMAT = "api_url_format";
        String SAME_OAUTH_SIGNING_URL = "same_oauth_signing_url";
        String NO_VERSION_SUFFIX = "no_version_suffix";

        String ACCOUNT_TYPE = "account_type";

        String ACCOUNT_EXTRAS = "account_extras";

        String ACCOUNT_USER = "account_user";

        String[] COLUMNS = {_ID, NAME, SCREEN_NAME, ACCOUNT_KEY, AUTH_TYPE, BASIC_AUTH_USERNAME,
                BASIC_AUTH_PASSWORD, OAUTH_TOKEN, OAUTH_TOKEN_SECRET, CONSUMER_KEY, CONSUMER_SECRET,
                API_URL_FORMAT, SAME_OAUTH_SIGNING_URL, NO_VERSION_SUFFIX, PROFILE_IMAGE_URL,
                PROFILE_BANNER_URL, COLOR, IS_ACTIVATED, SORT_POSITION, ACCOUNT_TYPE, ACCOUNT_EXTRAS,
                ACCOUNT_USER};

        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL, TYPE_TEXT_NOT_NULL,
                TYPE_TEXT_NOT_NULL_UNIQUE, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
                TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_TEXT, TYPE_TEXT,
                TYPE_INT, TYPE_BOOLEAN, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT};

    }

    interface CachedHashtags extends CachedValues {

        String[] COLUMNS = {_ID, NAME};
        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_TEXT};

        String TABLE_NAME = "cached_hashtags";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);
    }

    interface CachedStatuses extends Statuses {
        String TABLE_NAME = "cached_statuses";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);
    }

    interface CachedTrends extends CachedValues, AccountSupportColumns {

        String TIMESTAMP = "timestamp";
        String WOEID = "woeid";
        String TREND_ORDER = "trend_order";

        String[] COLUMNS = ParcelableTrendTableInfo.COLUMNS;
        String[] TYPES = ParcelableTrendTableInfo.TYPES;

        interface Local extends CachedTrends {
            String TABLE_NAME = "local_trends";
            String CONTENT_PATH = TABLE_NAME;

            Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        }

    }

    interface CachedUsers extends CachedValues {

        String TABLE_NAME = "cached_users";

        String CONTENT_PATH = TABLE_NAME;

        String CONTENT_PATH_WITH_RELATIONSHIP = CONTENT_PATH + "/with_relationship";

        String CONTENT_PATH_WITH_SCORE = CONTENT_PATH + "/with_score";

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        Uri CONTENT_URI_WITH_RELATIONSHIP = Uri.withAppendedPath(BASE_CONTENT_URI,
                CONTENT_PATH_WITH_RELATIONSHIP);
        Uri CONTENT_URI_WITH_SCORE = Uri.withAppendedPath(BASE_CONTENT_URI,
                CONTENT_PATH_WITH_SCORE);

        String USER_KEY = "user_id";

        String CREATED_AT = "created_at";

        String IS_PROTECTED = "is_protected";

        String IS_VERIFIED = "is_verified";

        String IS_FOLLOWING = "is_following";

        String DESCRIPTION_PLAIN = "description_plain";

        String DESCRIPTION_UNESCAPED = "description_unescaped";

        String DESCRIPTION_SPANS = "description_spans";

        String LOCATION = "location";

        String URL = "url";

        String URL_EXPANDED = "url_expanded";

        String PROFILE_BANNER_URL = "profile_banner_url";

        String PROFILE_BACKGROUND_URL = "profile_background_url";

        String FOLLOWERS_COUNT = "followers_count";

        String FRIENDS_COUNT = "friends_count";

        String STATUSES_COUNT = "statuses_count";

        String FAVORITES_COUNT = "favorites_count";

        String LISTED_COUNT = "listed_count";
        String MEDIA_COUNT = "media_count";

        String BACKGROUND_COLOR = "background_color";

        String LINK_COLOR = "link_color";

        String TEXT_COLOR = "text_color";

        /**
         * User's screen name of the status.<br>
         * Type: TEXT
         */
        String SCREEN_NAME = "screen_name";

        /**
         * User's profile image URL of the status.<br>
         * Type: TEXT NOT NULL
         */
        String PROFILE_IMAGE_URL = "profile_image_url";

        String LAST_SEEN = "last_seen";

        String SCORE = "score";

        String USER_TYPE = "user_type";

        String EXTRAS = "extras";

        String[] COLUMNS = ParcelableUserTableInfo.COLUMNS;

        String[] BASIC_COLUMNS = {_ID, USER_KEY, NAME, SCREEN_NAME, PROFILE_IMAGE_URL};

        String[] TYPES = ParcelableUserTableInfo.TYPES;

    }

    interface Suggestions extends BaseColumns {
        String TYPE = "type";
        String TITLE = "title";
        String SUMMARY = "summary";
        String ICON = "icon";
        String EXTRA_ID = "extra_id";
        String EXTRA = "extra";
        String VALUE = "value";

        String TABLE_NAME = "suggestions";

        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String[] COLUMNS = {_ID, TYPE, TITLE, SUMMARY, ICON, EXTRA_ID, EXTRA, VALUE};
        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
                TYPE_INT, TYPE_TEXT, TYPE_TEXT};

        interface AutoComplete extends Suggestions {

            String TYPE_USERS = "users";
            String TYPE_HASHTAGS = "hashtags";

            String CONTENT_PATH = Suggestions.CONTENT_PATH + "/auto_complete";
            Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);
        }

        interface Search extends Suggestions {

            String CONTENT_PATH = Suggestions.CONTENT_PATH + "/search";
            Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

            String TYPE_SAVED_SEARCH = "saved_search";
            String TYPE_USER = "user";
            String TYPE_SEARCH_HISTORY = "search_history";
            String TYPE_SCREEN_NAME = "screen_name";
            String TYPE_MESSAGE = "message";
        }
    }

    interface CachedValues extends BaseColumns {

        String NAME = "name";
    }

    interface CacheFiles extends BaseColumns {
        String TABLE_NAME = "cache_files";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String NAME = "name";

        String PATH = "path";

        String[] MATRIX_COLUMNS = {NAME, PATH};

        String[] COLUMNS = {_ID, NAME, PATH};
    }

    interface Messages extends BaseColumns, InsertedDateColumns, AccountSupportColumns {
        String MESSAGE_ID = "message_id";
        String CONVERSATION_ID = "conversation_id";
        String MESSAGE_TYPE = "message_type";
        String MESSAGE_TIMESTAMP = "message_timestamp";
        String LOCAL_TIMESTAMP = "local_timestamp";
        String SORT_ID = "sort_id";
        String TEXT_UNESCAPED = "text_unescaped";
        String MEDIA = "media";
        String SPANS = "spans";
        String EXTRAS = "extras";
        String SENDER_KEY = "sender_key";
        String RECIPIENT_KEY = "recipient_key";
        String REQUEST_CURSOR = "request_cursor";
        String IS_OUTGOING = "is_outgoing";

        String[] COLUMNS = ParcelableMessageTableInfo.COLUMNS;
        String[] TYPES = ParcelableMessageTableInfo.TYPES;

        String TABLE_NAME = "messages";
        String CONTENT_PATH = "messages";

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        interface Conversations extends BaseColumns, AccountSupportColumns {
            String ACCOUNT_COLOR = "account_color";
            String CONVERSATION_ID = "conversation_id";
            String CONVERSATION_TYPE = "conversation_type";
            String CONVERSATION_NAME = "conversation_name";
            String CONVERSATION_AVATAR = "conversation_avatar";
            String MESSAGE_TYPE = "message_type";
            String MESSAGE_TIMESTAMP = "message_timestamp";
            String SORT_ID = "sort_id";
            String LOCAL_TIMESTAMP = "local_timestamp";
            String TEXT_UNESCAPED = "text_unescaped";
            String MEDIA = "media";
            String SPANS = "spans";
            String MESSAGE_EXTRAS = "message_extras";
            String PARTICIPANTS = "participants";
            String PARTICIPANT_KEYS = "participant_keys";
            String SENDER_KEY = "sender_key";
            String RECIPIENT_KEY = "recipient_key";
            String REQUEST_CURSOR = "request_cursor";
            String LAST_READ_ID = "last_read_id";
            String LAST_READ_TIMESTAMP = "last_read_timestamp";
            String IS_OUTGOING = "is_outgoing";
            String IS_TEMP = "is_temp";
            String CONVERSATION_EXTRAS = "conversation_extras";
            String CONVERSATION_EXTRAS_TYPE = "conversation_extras_type";

            /**
             * This column isn't available in database, you'll need to calculate by yourself instead
             */
            String UNREAD_COUNT = "unread_count";

            String[] COLUMNS = ParcelableMessageConversationTableInfo.COLUMNS;

            String[] TYPES = ParcelableMessageConversationTableInfo.TYPES;

            String TABLE_NAME = "messages_conversations";
            String CONTENT_PATH = "messages/conversations";
            Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);
        }
    }

    interface SearchHistory extends BaseColumns {

        String TABLE_NAME = "search_history";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String QUERY = "query";

        String RECENT_QUERY = "recent_query";

        String[] COLUMNS = {_ID, RECENT_QUERY, QUERY};
        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_INT, TYPE_TEXT_NOT_NULL_UNIQUE};
        String DEFAULT_SORT_ORDER = RECENT_QUERY + " DESC";
    }

    interface SavedSearches extends BaseColumns, AccountSupportColumns {

        String TABLE_NAME = "saved_searches";

        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String SEARCH_ID = "search_id";
        String QUERY = "query";
        String NAME = "name";
        String CREATED_AT = "created_at";

        String[] COLUMNS = {_ID, ACCOUNT_KEY, SEARCH_ID, CREATED_AT, QUERY, NAME};
        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_TEXT, TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT};
        String DEFAULT_SORT_ORDER = CREATED_AT + " DESC";
    }

    interface Drafts extends BaseColumns {

        String TABLE_NAME = "drafts";
        String CONTENT_PATH = TABLE_NAME;
        String CONTENT_PATH_UNSENT = TABLE_NAME + "/unsent";
        String CONTENT_PATH_NOTIFICATIONS = TABLE_NAME + "/notifications";

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);
        Uri CONTENT_URI_UNSENT = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH_UNSENT);
        Uri CONTENT_URI_NOTIFICATIONS = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH_NOTIFICATIONS);

        /**
         * Status content.<br>
         * Type: TEXT
         */
        String TEXT = "text";

        /**
         * Account IDs of unsent status.<br>
         * Type: TEXT
         */
        String ACCOUNT_KEYS = "account_ids";

        String LOCATION = "location";

        String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";

        String MEDIA = "media";

        String IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";

        String TIMESTAMP = "timestamp";

        String ACTION_TYPE = "action_type";

        String ACTION_EXTRAS = "action_extras";

        String UNIQUE_ID = "unique_id";

        String[] COLUMNS = DraftTableInfo.COLUMNS;

        String[] TYPES = DraftTableInfo.TYPES;

    }

    interface Filters extends BaseColumns {

        String CONTENT_PATH = "filters";

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String VALUE = "value";

        String SOURCE = "source";

        String USER_KEY = "user_key";

        String SCOPE = "scope";

        String[] COLUMNS = FiltersData$BaseItemTableInfo.COLUMNS;

        String[] TYPES = FiltersData$BaseItemTableInfo.TYPES;

        interface Keywords extends Filters {

            String TABLE_NAME = "filtered_keywords";
            String CONTENT_PATH_SEGMENT = "keywords";
            String CONTENT_PATH = Filters.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;
            Uri CONTENT_URI = Uri.withAppendedPath(Filters.CONTENT_URI, CONTENT_PATH_SEGMENT);
        }

        interface Links extends Filters {

            String TABLE_NAME = "filtered_links";
            String CONTENT_PATH_SEGMENT = "links";
            String CONTENT_PATH = Filters.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;
            Uri CONTENT_URI = Uri.withAppendedPath(Filters.CONTENT_URI, CONTENT_PATH_SEGMENT);
        }

        interface Sources extends Filters {

            String TABLE_NAME = "filtered_sources";
            String CONTENT_PATH_SEGMENT = "sources";
            String CONTENT_PATH = Filters.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;
            Uri CONTENT_URI = Uri.withAppendedPath(Filters.CONTENT_URI, CONTENT_PATH_SEGMENT);
        }

        interface Users extends BaseColumns {

            String TABLE_NAME = "filtered_users";
            String CONTENT_PATH_SEGMENT = "users";
            String CONTENT_PATH = Filters.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;
            Uri CONTENT_URI = Uri.withAppendedPath(Filters.CONTENT_URI, CONTENT_PATH_SEGMENT);

            String USER_KEY = "user_id";
            String NAME = "name";
            String SCREEN_NAME = "screen_name";
            String SOURCE = "source";
            String SCOPE = "scope";

            String[] COLUMNS = FiltersData$UserItemTableInfo.COLUMNS;

            String[] TYPES = FiltersData$UserItemTableInfo.TYPES;
        }

        interface Subscriptions extends BaseColumns {
            String TABLE_NAME = "filters_subscriptions";
            String CONTENT_PATH_SEGMENT = "subscriptions";
            String CONTENT_PATH = Filters.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;
            Uri CONTENT_URI = Uri.withAppendedPath(Filters.CONTENT_URI, CONTENT_PATH_SEGMENT);

            String NAME = "name";
            String COMPONENT = "component";
            String ARGUMENTS = "arguments";


            String[] COLUMNS = FiltersSubscriptionTableInfo.COLUMNS;

            String[] TYPES = FiltersSubscriptionTableInfo.TYPES;
        }
    }

    interface Permissions extends BaseColumns {
        String TABLE_NAME = "permissions";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String PERMISSION = "permissions";

        String PACKAGE_NAME = "package_name";

        String[] MATRIX_COLUMNS = {PACKAGE_NAME, PERMISSION};

        String[] COLUMNS = {_ID, PACKAGE_NAME, PERMISSION};
    }

    interface Statuses extends BaseColumns, InsertedDateColumns, AccountSupportColumns {

        String TABLE_NAME = "statuses";
        String CONTENT_PATH = TABLE_NAME;

        @NonNull
        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        /**
         * Id of the status.<br>
         */
        String ID = "id";

        /**
         * Sort ID of the status.<br>
         */
        String SORT_ID = "sort_id";

        /**
         * User's ID of the status.<br>
         * Type: INTEGER (long)
         */
        String USER_KEY = "user_key";

        /**
         * User name of the status.<br>
         * Type: TEXT
         */
        String USER_NAME = "user_name";

        /**
         * User's screen name of the status.<br>
         * Type: TEXT
         */
        String USER_SCREEN_NAME = "user_screen_name";

        /**
         * User's profile image URL of the status.<br>
         * Type: TEXT NOT NULL
         */
        String USER_PROFILE_IMAGE = "user_profile_image";

        String TEXT_PLAIN = "text_plain";

        String TEXT_UNESCAPED = "text_unescaped";

        /**
         * Retweet count of the status.<br>
         * Type: INTEGER (long)
         */
        String RETWEET_COUNT = "retweet_count";
        String FAVORITE_COUNT = "favorite_count";
        String REPLY_COUNT = "reply_count";

        /**
         * Set to an non-zero integer if the status is a retweet, set to
         * negative value if the status is retweeted by user.<br>
         * Type: INTEGER
         */
        String IS_RETWEET = "is_retweet";

        String IS_QUOTE = "is_quote";

        /**
         * Set to 1 if the status is a favorite.<br>
         * Type: INTEGER (boolean)
         */
        String IS_FAVORITE = "is_favorite";

        String IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";

        /**
         * Set to 1 if the status is a gap.<br>
         * Type: INTEGER (boolean)
         */
        String IS_GAP = "is_gap";

        String LOCATION = "location";

        String PLACE_FULL_NAME = "place_full_name";

        String LANG = "lang";

        String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";

        String IN_REPLY_TO_USER_KEY = "in_reply_to_user_key";

        String IN_REPLY_TO_USER_NAME = "in_reply_to_user_name";

        String IN_REPLY_TO_USER_SCREEN_NAME = "in_reply_to_user_screen_name";

        String SOURCE = "source";

        String IS_PROTECTED = "is_protected";

        String IS_VERIFIED = "is_verified";

        String IS_FOLLOWING = "is_following";

        String RETWEET_ID = "retweet_id";

        String RETWEET_TIMESTAMP = "retweet_timestamp";

        String RETWEETED_BY_USER_KEY = "retweeted_by_user_key";

        String RETWEETED_BY_USER_NAME = "retweeted_by_user_name";

        String RETWEETED_BY_USER_SCREEN_NAME = "retweeted_by_user_screen_name";

        String RETWEETED_BY_USER_PROFILE_IMAGE = "retweeted_by_user_profile_image";

        /**
         * Timestamp of the status.<br>
         * Type: INTEGER (long)
         */
        String TIMESTAMP = "timestamp";

        String MY_RETWEET_ID = "my_retweet_id";

        String MEDIA_JSON = "media_json";

        String MENTIONS_JSON = "mentions_json";

        String CARD = "card";

        String CARD_NAME = "card_type";

        String QUOTED_ID = "quoted_id";

        String QUOTED_TEXT_PLAIN = "quoted_text_plain";
        String QUOTED_TEXT_UNESCAPED = "quoted_text_unescaped";
        String QUOTED_MEDIA_JSON = "quoted_media_json";
        String QUOTED_TIMESTAMP = "quoted_timestamp";
        String QUOTED_SOURCE = "quoted_source";
        String QUOTED_USER_KEY = "quoted_user_key";
        String QUOTED_USER_NAME = "quoted_user_name";
        String QUOTED_USER_SCREEN_NAME = "quoted_user_screen_name";
        String QUOTED_USER_PROFILE_IMAGE = "quoted_user_profile_image";
        String QUOTED_USER_IS_VERIFIED = "quoted_user_is_verified";
        String QUOTED_USER_IS_PROTECTED = "quoted_user_is_protected";
        String QUOTED_LOCATION = "quoted_location";
        String QUOTED_PLACE_FULL_NAME = "quoted_place_full_name";
        String RETWEETED = "retweeted";

        String EXTRAS = "extras";

        String SPANS = "spans";
        String QUOTED_SPANS = "quoted_spans";
        String POSITION_KEY = "position_key";

        String ACCOUNT_COLOR = "account_color";

        String FILTER_FLAGS = "filter_flags";
        String FILTER_USERS = "filter_users";
        String FILTER_LINKS = "filter_links";
        String FILTER_SOURCES = "filter_sources";
        String FILTER_NAMES = "filter_names";
        String FILTER_TEXTS = "filter_texts";
        String FILTER_DESCRIPTIONS = "filter_descriptions";

        String DEFAULT_SORT_ORDER = TIMESTAMP + " DESC, " + SORT_ID + " DESC, " + ID
                + " DESC";

        String[] COLUMNS = ParcelableStatusTableInfo.COLUMNS;

        String[] TYPES = ParcelableStatusTableInfo.TYPES;

    }

    interface Activities extends Statuses, BaseColumns, InsertedDateColumns, AccountSupportColumns {

        String ACTIVITY_ID = "activity_id";

        String ACTION = "action";

        String MIN_SORT_POSITION = "min_position";
        String MAX_SORT_POSITION = "max_position";

        String MIN_REQUEST_POSITION = "min_request_position";
        String MAX_REQUEST_POSITION = "max_request_position";
        String SOURCES = "sources";
        String TARGETS = "targets";

        String TARGET_OBJECTS = "target_objects";

        String SOURCES_LITE = "sources_lite";
        String SOURCE_KEYS = "source_keys";

        String SUMMARY_LINE = "summary_line";
        String HAS_FOLLOWING_SOURCE = "has_following_source";

        String[] COLUMNS = ParcelableActivityTableInfo.COLUMNS;
        String[] TYPES = ParcelableActivityTableInfo.TYPES;

        String DEFAULT_SORT_ORDER = TIMESTAMP + " DESC";

        interface AboutMe extends Activities {

            String CONTENT_PATH = "activities_about_me";
            String TABLE_NAME = "activities_about_me";

            @NonNull
            Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);
        }

    }

    interface Tabs extends BaseColumns {
        String TABLE_NAME = "tabs";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String NAME = "name";

        String ICON = "icon";

        String TYPE = "type";

        String ARGUMENTS = "arguments";

        String EXTRAS = "extras";

        String POSITION = "position";

        String[] COLUMNS = {_ID, NAME, ICON, TYPE, ARGUMENTS, EXTRAS,
                POSITION};

        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_TEXT, TYPE_TEXT,
                TYPE_TEXT_NOT_NULL, TYPE_TEXT, TYPE_TEXT, TYPE_INT};

        String DEFAULT_SORT_ORDER = POSITION + " ASC";
    }


    interface CachedRelationships extends BaseColumns, AccountSupportColumns {

        String TABLE_NAME = "cached_relationships";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String USER_KEY = "user_id";

        String FOLLOWING = "following";

        String FOLLOWED_BY = "followed_by";

        String BLOCKING = "blocking";

        String BLOCKED_BY = "blocked_by";

        String MUTING = "muting";

        String RETWEET_ENABLED = "retweet_enabled";

        String NOTIFICATIONS_ENABLED = "notifications_enabled";

        String[] COLUMNS = ParcelableRelationshipTableInfo.COLUMNS;

        String[] TYPES = ParcelableRelationshipTableInfo.TYPES;
    }


}
