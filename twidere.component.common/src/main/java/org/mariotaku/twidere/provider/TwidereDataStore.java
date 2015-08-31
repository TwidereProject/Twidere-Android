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

package org.mariotaku.twidere.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

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

    String CONTENT_PATH_DATABASE_READY = "database_ready";

    Uri BASE_CONTENT_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY).build();

    Uri CONTENT_URI_NULL = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH_NULL);

    Uri CONTENT_URI_DATABASE_READY = Uri.withAppendedPath(BASE_CONTENT_URI,
            CONTENT_PATH_DATABASE_READY);

    Uri[] STATUSES_URIS = new Uri[]{Statuses.CONTENT_URI, Mentions.CONTENT_URI,
            CachedStatuses.CONTENT_URI};
    Uri[] CACHE_URIS = new Uri[]{CachedUsers.CONTENT_URI, CachedStatuses.CONTENT_URI,
            CachedHashtags.CONTENT_URI, CachedTrends.Local.CONTENT_URI};
    Uri[] DIRECT_MESSAGES_URIS = new Uri[]{DirectMessages.Inbox.CONTENT_URI,
            DirectMessages.Outbox.CONTENT_URI};

    interface Accounts extends BaseColumns {

        String TABLE_NAME = "accounts";
        String CONTENT_PATH = TABLE_NAME;
        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        /**
         * Login name of the account<br>
         * Type: TEXT NOT NULL
         */
        String SCREEN_NAME = "screen_name";

        String NAME = "name";

        /**
         * Unique ID of the account<br>
         * Type: INTEGER (long)
         */
        String ACCOUNT_ID = "account_id";

        /**
         * Auth type of the account.</br> Type: INTEGER
         */
        String AUTH_TYPE = "auth_type";

        /**
         * Password of the account. (It will not stored)<br>
         * Type: TEXT
         */
        String PASSWORD = "password";

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

        String[] COLUMNS_NO_CREDENTIALS = {_ID, NAME, SCREEN_NAME, ACCOUNT_ID,
                PROFILE_IMAGE_URL, PROFILE_BANNER_URL, COLOR, IS_ACTIVATED};

        String[] COLUMNS = {_ID, NAME, SCREEN_NAME, ACCOUNT_ID, AUTH_TYPE,
                BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD, OAUTH_TOKEN, OAUTH_TOKEN_SECRET, CONSUMER_KEY,
                CONSUMER_SECRET, API_URL_FORMAT, SAME_OAUTH_SIGNING_URL, NO_VERSION_SUFFIX, PROFILE_IMAGE_URL, PROFILE_BANNER_URL, COLOR,
                IS_ACTIVATED, SORT_POSITION};

        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL, TYPE_TEXT_NOT_NULL,
                TYPE_INT_UNIQUE, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
                TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_TEXT, TYPE_TEXT, TYPE_INT, TYPE_BOOLEAN, TYPE_INT};

    }

    interface CachedHashtags extends CachedValues {

        String[] COLUMNS = {_ID, NAME};
        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_TEXT};

        String TABLE_NAME = "cached_hashtags";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);
    }

    interface CachedImages extends BaseColumns {
        String TABLE_NAME = "cached_images";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String URL = "url";

        String PATH = "path";

        String[] MATRIX_COLUMNS = {URL, PATH};

        String[] COLUMNS = {_ID, URL, PATH};
    }

    interface CachedStatuses extends Statuses {
        String TABLE_NAME = "cached_statuses";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);
    }

    interface CachedTrends extends CachedValues {

        String TIMESTAMP = "timestamp";

        String[] COLUMNS = {_ID, NAME, TIMESTAMP};
        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_TEXT, TYPE_INT};

        interface Local extends CachedTrends {
            String TABLE_NAME = "local_trends";
            String CONTENT_PATH = TABLE_NAME;

            Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        }

    }

    interface CachedUsers extends CachedValues {

        String TABLE_NAME = "cached_users";

        String CONTENT_PATH = TABLE_NAME;

        String CONTENT_PATH_WITH_RELATIONSHIP = TABLE_NAME + "/with_relationship";

        String CONTENT_PATH_WITH_SCORE = TABLE_NAME + "/with_score";

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        Uri CONTENT_URI_WITH_RELATIONSHIP = Uri.withAppendedPath(BASE_CONTENT_URI,
                CONTENT_PATH_WITH_RELATIONSHIP);
        Uri CONTENT_URI_WITH_SCORE = Uri.withAppendedPath(BASE_CONTENT_URI,
                CONTENT_PATH_WITH_SCORE);

        String USER_ID = "user_id";

        String CREATED_AT = "created_at";

        String IS_PROTECTED = "is_protected";

        String IS_VERIFIED = "is_verified";

        String IS_FOLLOWING = "is_following";

        String DESCRIPTION_PLAIN = "description_plain";

        String DESCRIPTION_HTML = "description_html";

        String DESCRIPTION_EXPANDED = "description_expanded";

        String LOCATION = "location";

        String URL = "url";

        String URL_EXPANDED = "url_expanded";

        String PROFILE_BANNER_URL = "profile_banner_url";

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

        String[] COLUMNS = {_ID, USER_ID, CREATED_AT, NAME, SCREEN_NAME,
                DESCRIPTION_PLAIN, LOCATION, URL, PROFILE_IMAGE_URL, PROFILE_BANNER_URL, IS_PROTECTED,
                IS_VERIFIED, IS_FOLLOWING, FOLLOWERS_COUNT, FRIENDS_COUNT, STATUSES_COUNT, FAVORITES_COUNT,
                LISTED_COUNT, MEDIA_COUNT, DESCRIPTION_HTML, DESCRIPTION_EXPANDED, URL_EXPANDED,
                BACKGROUND_COLOR, LINK_COLOR, TEXT_COLOR, LAST_SEEN};

        String[] BASIC_COLUMNS = {_ID, USER_ID,
                NAME, SCREEN_NAME, PROFILE_IMAGE_URL};

        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_INT_UNIQUE, TYPE_INT,
                TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_BOOLEAN,
                TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT,
                TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT};

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

    interface DirectMessages extends BaseColumns {

        String TABLE_NAME = "messages";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String ACCOUNT_ID = "account_id";
        String MESSAGE_ID = "message_id";
        String MESSAGE_TIMESTAMP = "message_timestamp";
        String SENDER_ID = "sender_id";
        String RECIPIENT_ID = "recipient_id";
        String CONVERSATION_ID = "conversation_id";

        String IS_OUTGOING = "is_outgoing";

        String TEXT_HTML = "text_html";
        String TEXT_PLAIN = "text_plain";
        String TEXT_UNESCAPED = "text_unescaped";
        String SENDER_NAME = "sender_name";
        String RECIPIENT_NAME = "recipient_name";
        String SENDER_SCREEN_NAME = "sender_screen_name";
        String RECIPIENT_SCREEN_NAME = "recipient_screen_name";
        String SENDER_PROFILE_IMAGE_URL = "sender_profile_image_url";
        String RECIPIENT_PROFILE_IMAGE_URL = "recipient_profile_image_url";

        String MEDIA_JSON = "media_json";

        String[] COLUMNS = {_ID, ACCOUNT_ID, MESSAGE_ID, MESSAGE_TIMESTAMP,
                SENDER_ID, RECIPIENT_ID, CONVERSATION_ID, IS_OUTGOING, TEXT_HTML, TEXT_PLAIN, TEXT_UNESCAPED,
                SENDER_NAME, RECIPIENT_NAME, SENDER_SCREEN_NAME, RECIPIENT_SCREEN_NAME, SENDER_PROFILE_IMAGE_URL,
                RECIPIENT_PROFILE_IMAGE_URL, MEDIA_JSON};
        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_INT, TYPE_INT, TYPE_INT,
                TYPE_INT, TYPE_INT, TYPE_INT, TYPE_BOOLEAN, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
                TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT};

        String DEFAULT_SORT_ORDER = MESSAGE_ID + " DESC";

        interface Conversation extends DirectMessages {

            String DEFAULT_SORT_ORDER = MESSAGE_TIMESTAMP + " ASC";

            String CONTENT_PATH_SEGMENT = "conversation";
            String CONTENT_PATH_SEGMENT_SCREEN_NAME = "conversation_screen_name";

            String CONTENT_PATH = DirectMessages.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;
            String CONTENT_PATH_SCREEN_NAME = DirectMessages.CONTENT_PATH + "/"
                    + CONTENT_PATH_SEGMENT_SCREEN_NAME;

            Uri CONTENT_URI = Uri
                    .withAppendedPath(DirectMessages.CONTENT_URI, CONTENT_PATH_SEGMENT);

            Uri CONTENT_URI_SCREEN_NAME = Uri.withAppendedPath(DirectMessages.CONTENT_URI,
                    CONTENT_PATH_SEGMENT_SCREEN_NAME);
        }

        interface ConversationEntries extends BaseColumns {

            String TABLE_NAME = "messages_conversation_entries";

            String CONTENT_PATH_SEGMENT = "conversation_entries";
            String CONTENT_PATH = DirectMessages.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;

            Uri CONTENT_URI = Uri
                    .withAppendedPath(DirectMessages.CONTENT_URI, CONTENT_PATH_SEGMENT);

            String MESSAGE_ID = DirectMessages.MESSAGE_ID;
            String ACCOUNT_ID = DirectMessages.ACCOUNT_ID;
            String IS_OUTGOING = DirectMessages.IS_OUTGOING;
            String MESSAGE_TIMESTAMP = DirectMessages.MESSAGE_TIMESTAMP;
            String NAME = "name";
            String SCREEN_NAME = "screen_name";
            String PROFILE_IMAGE_URL = "profile_image_url";
            String TEXT_HTML = DirectMessages.TEXT_HTML;
            String CONVERSATION_ID = "conversation_id";

            int IDX__ID = 0;
            int IDX_MESSAGE_TIMESTAMP = 1;
            int IDX_MESSAGE_ID = 2;
            int IDX_ACCOUNT_ID = 3;
            int IDX_IS_OUTGOING = 4;
            int IDX_NAME = 5;
            int IDX_SCREEN_NAME = 6;
            int IDX_PROFILE_IMAGE_URL = 7;
            int IDX_TEXT = 8;
            int IDX_CONVERSATION_ID = 9;
        }

        interface Inbox extends DirectMessages {

            String TABLE_NAME = "messages_inbox";

            String CONTENT_PATH_SEGMENT = "inbox";
            String CONTENT_PATH = DirectMessages.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;

            Uri CONTENT_URI = Uri
                    .withAppendedPath(DirectMessages.CONTENT_URI, CONTENT_PATH_SEGMENT);

        }

        interface Outbox extends DirectMessages {

            String TABLE_NAME = "messages_outbox";

            String CONTENT_PATH_SEGMENT = "outbox";
            String CONTENT_PATH = DirectMessages.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;

            Uri CONTENT_URI = Uri
                    .withAppendedPath(DirectMessages.CONTENT_URI, CONTENT_PATH_SEGMENT);

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

    interface DNS extends BaseColumns {
        String TABLE_NAME = "dns";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String HOST = "host";

        String ADDRESS = "address";

        String[] MATRIX_COLUMNS = {HOST, ADDRESS};

        String[] COLUMNS = {_ID, HOST, ADDRESS};
    }

    interface SavedSearches extends BaseColumns {

        String TABLE_NAME = "saved_searches";

        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String ACCOUNT_ID = "account_id";
        String SEARCH_ID = "search_id";
        String QUERY = "query";
        String NAME = "name";
        String CREATED_AT = "created_at";

        String[] COLUMNS = {_ID, ACCOUNT_ID, SEARCH_ID, CREATED_AT,
                QUERY, NAME};
        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_INT, TYPE_INT,
                TYPE_INT, TYPE_TEXT, TYPE_TEXT};
        String DEFAULT_SORT_ORDER = CREATED_AT + " DESC";
    }

    interface Drafts extends BaseColumns {

        int ACTION_UPDATE_STATUS = 1;
        int ACTION_SEND_DIRECT_MESSAGE = 2;
        int ACTION_CREATE_FRIENDSHIP = 3;

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
        String ACCOUNT_IDS = "account_ids";

        String LOCATION = "location";

        String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";

        String MEDIA = "media";

        String IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";

        String TIMESTAMP = "timestamp";

        String ACTION_TYPE = "action_type";

        String ACTION_EXTRAS = "action_extras";

        String[] COLUMNS = {_ID, TEXT, ACCOUNT_IDS, LOCATION, MEDIA,
                IN_REPLY_TO_STATUS_ID, IS_POSSIBLY_SENSITIVE, TIMESTAMP, ACTION_TYPE, ACTION_EXTRAS};

        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
                TYPE_INT, TYPE_INT, TYPE_BOOLEAN, TYPE_INT, TYPE_INT, TYPE_TEXT};

    }

    interface Filters extends BaseColumns {

        String CONTENT_PATH = "filters";

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String VALUE = "value";

        String ENABLE_IN_HOME_TIMELINE = "enable_in_home_timeline";

        String ENABLE_IN_MENTIONS = "enable_in_mentions";

        String ENABLE_FOR_RETWEETS = "enable_for_retweets";

        String[] COLUMNS = {_ID, VALUE};

        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL_UNIQUE};

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

            String USER_ID = "user_id";
            String NAME = "name";
            String SCREEN_NAME = "screen_name";

            String[] COLUMNS = {_ID, USER_ID, NAME, SCREEN_NAME};

            String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_INT_UNIQUE, TYPE_TEXT_NOT_NULL,
                    TYPE_TEXT_NOT_NULL};
        }
    }

    interface Mentions extends Statuses {

        String TABLE_NAME = "mentions";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

    }

    interface Notifications extends BaseColumns {

        String TABLE_NAME = "notifications";

        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String ID = "id";

        String COUNT = "count";

        String[] MATRIX_COLUMNS = {ID, COUNT};

        String[] COLUMNS = {_ID, ID, COUNT};
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

    interface Preferences extends BaseColumns {
        String TABLE_NAME = "preferences";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        int TYPE_INVALID = -1;

        int TYPE_NULL = 0;

        int TYPE_BOOLEAN = 1;

        int TYPE_INTEGER = 2;

        int TYPE_LONG = 3;

        int TYPE_FLOAT = 4;

        int TYPE_STRING = 5;

        String KEY = "key";

        String VALUE = "value";

        String TYPE = "type";

        String[] MATRIX_COLUMNS = {KEY, VALUE, TYPE};

        String[] COLUMNS = {_ID, KEY, VALUE, TYPE};
    }

    interface Statuses extends BaseColumns {

        String TABLE_NAME = "statuses";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);
        /**
         * Account ID of the status.<br>
         * Type: TEXT
         */
        String ACCOUNT_ID = "account_id";

        /**
         * Status content, in HTML. Please note, this is not actually original
         * text.<br>
         * Type: TEXT
         */
        String TEXT_HTML = "text_html";

        /**
         *
         */
        String TEXT_PLAIN = "text_plain";

        String TEXT_UNESCAPED = "text_unescaped";

        /**
         * User name of the status.<br>
         * Type: TEXT
         */
        String USER_NAME = "name";

        /**
         * User's screen name of the status.<br>
         * Type: TEXT
         */
        String USER_SCREEN_NAME = "screen_name";

        /**
         * User's profile image URL of the status.<br>
         * Type: TEXT NOT NULL
         */
        String USER_PROFILE_IMAGE_URL = "profile_image_url";

        /**
         * Unique id of the status.<br>
         * Type: INTEGER UNIQUE(long)
         */
        String STATUS_ID = "status_id";

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

        /**
         * User's ID of the status.<br>
         * Type: INTEGER (long)
         */
        String USER_ID = "user_id";

        String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";

        String IN_REPLY_TO_USER_ID = "in_reply_to_user_id";

        String IN_REPLY_TO_USER_NAME = "in_reply_to_user_name";

        String IN_REPLY_TO_USER_SCREEN_NAME = "in_reply_to_user_screen_name";

        String SOURCE = "source";

        String IS_PROTECTED = "is_protected";

        String IS_VERIFIED = "is_verified";

        String IS_FOLLOWING = "is_following";

        String RETWEET_ID = "retweet_id";

        String RETWEET_TIMESTAMP = "retweet_timestamp";

        String RETWEETED_BY_USER_ID = "retweeted_by_user_id";

        String RETWEETED_BY_USER_NAME = "retweeted_by_user_name";

        String RETWEETED_BY_USER_SCREEN_NAME = "retweeted_by_user_screen_name";

        String RETWEETED_BY_USER_PROFILE_IMAGE = "retweeted_by_user_profile_image";

        /**
         * Timestamp of the status.<br>
         * Type: INTEGER (long)
         */
        String STATUS_TIMESTAMP = "status_timestamp";

        String MY_RETWEET_ID = "my_retweet_id";

        String MEDIA_JSON = "media_json";

        String MENTIONS_JSON = "mentions_json";

        String CARD = "card";

        String CARD_NAME = "card_type";

        String SORT_ORDER_TIMESTAMP_DESC = STATUS_TIMESTAMP + " DESC";

        String SORT_ORDER_STATUS_ID_DESC = STATUS_ID + " DESC";

        String DEFAULT_SORT_ORDER = SORT_ORDER_TIMESTAMP_DESC;

        String QUOTED_ID = "quoted_id";
        String QUOTED_TEXT_HTML = "quoted_text_html";
        String QUOTED_TEXT_PLAIN = "quoted_text_plain";
        String QUOTED_TEXT_UNESCAPED = "quoted_text_unescaped";
        String QUOTED_MEDIA_JSON = "quoted_media_json";
        String QUOTED_TIMESTAMP = "quoted_timestamp";
        String QUOTED_SOURCE = "quoted_source";
        String QUOTED_USER_ID = "quoted_user_id";
        String QUOTED_USER_NAME = "quoted_user_name";
        String QUOTED_USER_SCREEN_NAME = "quoted_user_screen_name";
        String QUOTED_USER_PROFILE_IMAGE = "quoted_user_profile_image";
        String QUOTED_USER_IS_VERIFIED = "quoted_user_is_verified";
        String QUOTED_USER_IS_PROTECTED = "quoted_user_is_protected";

        String[] COLUMNS = {_ID, ACCOUNT_ID, STATUS_ID, USER_ID,
                STATUS_TIMESTAMP, TEXT_HTML, TEXT_PLAIN, TEXT_UNESCAPED, USER_NAME, USER_SCREEN_NAME,
                USER_PROFILE_IMAGE_URL, IN_REPLY_TO_STATUS_ID, IN_REPLY_TO_USER_ID, IN_REPLY_TO_USER_NAME,
                IN_REPLY_TO_USER_SCREEN_NAME, SOURCE, LOCATION, RETWEET_COUNT, FAVORITE_COUNT, REPLY_COUNT,
                RETWEET_ID, RETWEET_TIMESTAMP, RETWEETED_BY_USER_ID, RETWEETED_BY_USER_NAME,
                RETWEETED_BY_USER_SCREEN_NAME, RETWEETED_BY_USER_PROFILE_IMAGE, QUOTED_ID, QUOTED_TEXT_HTML,
                QUOTED_TEXT_PLAIN, QUOTED_TEXT_UNESCAPED, QUOTED_TIMESTAMP, QUOTED_SOURCE, QUOTED_USER_ID,
                QUOTED_USER_NAME, QUOTED_USER_SCREEN_NAME, QUOTED_USER_PROFILE_IMAGE,
                QUOTED_USER_IS_VERIFIED, QUOTED_USER_IS_PROTECTED, MY_RETWEET_ID, IS_RETWEET,
                IS_QUOTE, IS_FAVORITE, IS_PROTECTED, IS_VERIFIED, IS_FOLLOWING, IS_GAP,
                IS_POSSIBLY_SENSITIVE, MEDIA_JSON, MENTIONS_JSON, QUOTED_MEDIA_JSON, CARD_NAME, CARD,
                PLACE_FULL_NAME};

        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_INT, TYPE_INT,
                TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
                TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_INT,
                TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT,
                TYPE_TEXT, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_INT, TYPE_TEXT, TYPE_INT,
                TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_INT, TYPE_BOOLEAN,
                TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN,
                TYPE_BOOLEAN, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
                TYPE_TEXT, TYPE_TEXT};

    }

    interface Activities extends BaseColumns {

        String ACCOUNT_ID = "account_id";
        String TIMESTAMP = "timestamp";
        String STATUS_USER_ID = "status_user_id";
        String STATUS_RETWEETED_BY_USER_ID = "status_retweeted_by_user_id";
        String STATUS_QUOTED_USER_ID = "status_quoted_user_id";
        String STATUS_SOURCE = "status_source";
        String STATUS_QUOTE_SOURCE = "status_quote_source";
        String STATUS_TEXT_PLAIN = "status_text_plain";
        String STATUS_QUOTE_TEXT_PLAIN = "status_quote_text_plain";
        String STATUS_TEXT_HTML = "status_text_html";
        String STATUS_QUOTE_TEXT_HTML = "status_quote_text_html";
        String STATUS_IS_GAP = "status_is_gap";
        String MIN_POSITION = "min_position";
        String MAX_POSITION = "max_position";
        String SOURCES = "sources";
        String TARGET_STATUSES = "target_statuses";
        String TARGET_USERS = "target_users";
        String TARGET_USER_LISTS = "target_user_lists";
        String TARGET_OBJECT_STATUSES = "target_object_statuses";
        String TARGET_OBJECT_USER_LISTS = "target_object_user_lists";

        interface AboutMe extends Activities {

            String CONTENT_PATH = "activities_about_me";

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

    interface CachedRelationships extends BaseColumns {

        String TABLE_NAME = "cached_relationships";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String ACCOUNT_ID = "account_id";

        String USER_ID = "user_id";

        String FOLLOWING = "following";

        String FOLLOWED_BY = "followed_by";

        String BLOCKING = "blocking";

        String BLOCKED_BY = "blocked_by";

        String MUTING = "muting";

        String RETWEET_ENABLED = "retweet_enabled";

        String[] COLUMNS = {_ID, ACCOUNT_ID, USER_ID, FOLLOWING, FOLLOWED_BY, BLOCKING,
                BLOCKED_BY, MUTING, RETWEET_ENABLED};

        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_INT, TYPE_INT, TYPE_BOOLEAN_DEFAULT_FALSE,
                TYPE_BOOLEAN_DEFAULT_FALSE, TYPE_BOOLEAN_DEFAULT_FALSE, TYPE_BOOLEAN_DEFAULT_FALSE,
                TYPE_BOOLEAN_DEFAULT_FALSE, TYPE_BOOLEAN_DEFAULT_TRUE};
    }

    interface UnreadCounts extends BaseColumns {

        String CONTENT_PATH = "unread_counts";

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String TAB_POSITION = "tab_position";

        String TAB_TYPE = "tab_type";

        String COUNT = "count";

        String[] MATRIX_COLUMNS = {TAB_POSITION, TAB_TYPE, COUNT};

        String[] COLUMNS = {_ID, TAB_POSITION, TAB_TYPE, COUNT};

        interface ByType extends UnreadCounts {

            String CONTENT_PATH_SEGMENT = "by_type";

            String CONTENT_PATH = UnreadCounts.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;

            Uri CONTENT_URI = Uri.withAppendedPath(UnreadCounts.CONTENT_URI, CONTENT_PATH_SEGMENT);
        }
    }

    interface NetworkUsages extends BaseColumns {

        String TABLE_NAME = "network_usages";
        String CONTENT_PATH = TABLE_NAME;

        Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        String TIME_IN_HOURS = "time_in_hours";

        String REQUEST_TYPE = "request_type";

        String REQUEST_NETWORK = "request_network";

        String KILOBYTES_SENT = "kilobytes_sent";

        String KILOBYTES_RECEIVED = "kilobytes_received";

        String[] COLUMNS = {_ID, TIME_IN_HOURS, REQUEST_TYPE, REQUEST_NETWORK, KILOBYTES_SENT,
                KILOBYTES_RECEIVED};

        String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_INT, TYPE_TEXT_NOT_NULL, TYPE_TEXT_NOT_NULL,
                TYPE_DOUBLE_NOT_NULL, TYPE_DOUBLE_NOT_NULL};
    }
}
