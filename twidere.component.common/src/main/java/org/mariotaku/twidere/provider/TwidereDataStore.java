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

    public static final String AUTHORITY = "twidere";

    public static final String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final String TYPE_INT = "INTEGER";
    public static final String TYPE_INT_UNIQUE = "INTEGER UNIQUE";
    public static final String TYPE_BOOLEAN = "INTEGER(1)";
    public static final String TYPE_BOOLEAN_DEFAULT_TRUE = "INTEGER(1) DEFAULT 1";
    public static final String TYPE_BOOLEAN_DEFAULT_FALSE = "INTEGER(1) DEFAULT 0";
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_TEXT_NOT_NULL = "TEXT NOT NULL";
    public static final String TYPE_TEXT_NOT_NULL_UNIQUE = "TEXT NOT NULL UNIQUE";

    public static final String CONTENT_PATH_NULL = "null_content";

    public static final String CONTENT_PATH_DATABASE_READY = "database_ready";

    public static final Uri BASE_CONTENT_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY).build();

    public static final Uri CONTENT_URI_NULL = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH_NULL);

    public static final Uri CONTENT_URI_DATABASE_READY = Uri.withAppendedPath(BASE_CONTENT_URI,
            CONTENT_PATH_DATABASE_READY);

    public static final Uri[] STATUSES_URIS = new Uri[]{Statuses.CONTENT_URI, Mentions.CONTENT_URI,
            CachedStatuses.CONTENT_URI};
    public static final Uri[] CACHE_URIS = new Uri[]{CachedUsers.CONTENT_URI, CachedStatuses.CONTENT_URI,
            CachedHashtags.CONTENT_URI, CachedTrends.Local.CONTENT_URI};
    public static final Uri[] DIRECT_MESSAGES_URIS = new Uri[]{DirectMessages.Inbox.CONTENT_URI,
            DirectMessages.Outbox.CONTENT_URI};

    public static interface Accounts extends BaseColumns {

        public static final int AUTH_TYPE_OAUTH = 0;
        public static final int AUTH_TYPE_XAUTH = 1;
        public static final int AUTH_TYPE_BASIC = 2;
        public static final int AUTH_TYPE_TWIP_O_MODE = 3;

        public static final String TABLE_NAME = "accounts";
        public static final String CONTENT_PATH = TABLE_NAME;
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        /**
         * Login name of the account<br>
         * Type: TEXT NOT NULL
         */
        public static final String SCREEN_NAME = "screen_name";

        public static final String NAME = "name";

        /**
         * Unique ID of the account<br>
         * Type: INTEGER (long)
         */
        public static final String ACCOUNT_ID = "account_id";

        /**
         * Auth type of the account.</br> Type: INTEGER
         */
        public static final String AUTH_TYPE = "auth_type";

        /**
         * Password of the account. (It will not stored)<br>
         * Type: TEXT
         */
        public static final String PASSWORD = "password";

        public static final String BASIC_AUTH_USERNAME = "basic_auth_username";

        /**
         * Password of the account for basic auth.<br>
         * Type: TEXT
         */
        public static final String BASIC_AUTH_PASSWORD = "basic_auth_password";

        /**
         * OAuth Token of the account.<br>
         * Type: TEXT
         */
        public static final String OAUTH_TOKEN = "oauth_token";

        /**
         * Token Secret of the account.<br>
         * Type: TEXT
         */
        public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";

        public static final String COLOR = "color";

        /**
         * Set to a non-zero integer if the account is activated. <br>
         * Type: INTEGER (boolean)
         */
        public static final String IS_ACTIVATED = "is_activated";

        public static final String CONSUMER_KEY = "consumer_key";

        public static final String CONSUMER_SECRET = "consumer_secret";

        public static final String SORT_POSITION = "sort_position";

        /**
         * User's profile image URL of the status. <br>
         * Type: TEXT
         */
        public static final String PROFILE_IMAGE_URL = "profile_image_url";

        public static final String PROFILE_BANNER_URL = "profile_banner_url";
        public static final String API_URL_FORMAT = "api_url_format";
        public static final String SAME_OAUTH_SIGNING_URL = "same_oauth_signing_url";
        public static final String NO_VERSION_SUFFIX = "no_version_suffix";

        public static final String[] COLUMNS_NO_CREDENTIALS = new String[]{_ID, NAME, SCREEN_NAME, ACCOUNT_ID,
                PROFILE_IMAGE_URL, PROFILE_BANNER_URL, COLOR, IS_ACTIVATED};

        public static final String[] COLUMNS = new String[]{_ID, NAME, SCREEN_NAME, ACCOUNT_ID, AUTH_TYPE,
                BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD, OAUTH_TOKEN, OAUTH_TOKEN_SECRET, CONSUMER_KEY,
                CONSUMER_SECRET, API_URL_FORMAT, SAME_OAUTH_SIGNING_URL, NO_VERSION_SUFFIX, PROFILE_IMAGE_URL, PROFILE_BANNER_URL, COLOR,
                IS_ACTIVATED, SORT_POSITION};

        public static final String[] TYPES = new String[]{TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL, TYPE_TEXT_NOT_NULL,
                TYPE_INT_UNIQUE, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
                TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_TEXT, TYPE_TEXT, TYPE_INT, TYPE_BOOLEAN, TYPE_INT};

    }

    public static interface CachedHashtags extends CachedValues {

        public static final String[] COLUMNS = new String[]{_ID, NAME};
        public static final String[] TYPES = new String[]{TYPE_PRIMARY_KEY, TYPE_TEXT};

        public static final String TABLE_NAME = "cached_hashtags";
        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);
    }

    public static interface CachedImages extends BaseColumns {
        public static final String TABLE_NAME = "cached_images";
        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final String URL = "url";

        public static final String PATH = "path";

        public static final String[] MATRIX_COLUMNS = new String[]{URL, PATH};

        public static final String[] COLUMNS = new String[]{_ID, URL, PATH};
    }

    public static interface CachedStatuses extends Statuses {
        public static final String TABLE_NAME = "cached_statuses";
        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);
    }

    public static interface CachedTrends extends CachedValues {

        public static final String TIMESTAMP = "timestamp";

        public static final String[] COLUMNS = new String[]{_ID, NAME, TIMESTAMP};
        public static final String[] TYPES = new String[]{TYPE_PRIMARY_KEY, TYPE_TEXT, TYPE_INT};

        public static interface Local extends CachedTrends {
            public static final String TABLE_NAME = "local_trends";
            public static final String CONTENT_PATH = TABLE_NAME;

            public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        }

    }

    public static interface CachedUsers extends CachedValues {

        public static final String TABLE_NAME = "cached_users";

        public static final String CONTENT_PATH = TABLE_NAME;

        public static final String CONTENT_PATH_WITH_RELATIONSHIP = TABLE_NAME + "/with_relationship";

        public static final String CONTENT_PATH_WITH_SCORE = TABLE_NAME + "/with_score";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final Uri CONTENT_URI_WITH_RELATIONSHIP = Uri.withAppendedPath(BASE_CONTENT_URI,
                CONTENT_PATH_WITH_RELATIONSHIP);
        public static final Uri CONTENT_URI_WITH_SCORE = Uri.withAppendedPath(BASE_CONTENT_URI,
                CONTENT_PATH_WITH_SCORE);

        public static final String USER_ID = "user_id";

        public static final String CREATED_AT = "created_at";

        public static final String IS_PROTECTED = "is_protected";

        public static final String IS_VERIFIED = "is_verified";

        public static final String IS_FOLLOWING = "is_following";

        public static final String DESCRIPTION_PLAIN = "description_plain";

        public static final String DESCRIPTION_HTML = "description_html";

        public static final String DESCRIPTION_EXPANDED = "description_expanded";

        public static final String LOCATION = "location";

        public static final String URL = "url";

        public static final String URL_EXPANDED = "url_expanded";

        public static final String PROFILE_BANNER_URL = "profile_banner_url";

        public static final String FOLLOWERS_COUNT = "followers_count";

        public static final String FRIENDS_COUNT = "friends_count";

        public static final String STATUSES_COUNT = "statuses_count";

        public static final String FAVORITES_COUNT = "favorites_count";

        public static final String LISTED_COUNT = "listed_count";

        public static final String BACKGROUND_COLOR = "background_color";

        public static final String LINK_COLOR = "link_color";

        public static final String TEXT_COLOR = "text_color";

        /**
         * User's screen name of the status.<br>
         * Type: TEXT
         */
        public static final String SCREEN_NAME = "screen_name";

        /**
         * User's profile image URL of the status.<br>
         * Type: TEXT NOT NULL
         */
        public static final String PROFILE_IMAGE_URL = "profile_image_url";

        public static final String LAST_SEEN = "last_seen";

        public static final String[] COLUMNS = new String[]{_ID, USER_ID, CREATED_AT, NAME, SCREEN_NAME,
                DESCRIPTION_PLAIN, LOCATION, URL, PROFILE_IMAGE_URL, PROFILE_BANNER_URL, IS_PROTECTED,
                IS_VERIFIED, IS_FOLLOWING, FOLLOWERS_COUNT, FRIENDS_COUNT, STATUSES_COUNT, FAVORITES_COUNT,
                LISTED_COUNT, DESCRIPTION_HTML, DESCRIPTION_EXPANDED, URL_EXPANDED, BACKGROUND_COLOR,
                LINK_COLOR, TEXT_COLOR, LAST_SEEN};

        public static final String[] BASIC_COLUMNS = new String[]{_ID, USER_ID,
                NAME, SCREEN_NAME, PROFILE_IMAGE_URL};

        public static final String[] TYPES = new String[]{TYPE_PRIMARY_KEY, TYPE_INT_UNIQUE, TYPE_INT,
                TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_BOOLEAN,
                TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_TEXT,
                TYPE_TEXT, TYPE_TEXT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT};

    }

    public static interface CachedValues extends BaseColumns {

        public static final String NAME = "name";
    }

    public static interface CacheFiles extends BaseColumns {
        public static final String TABLE_NAME = "cache_files";
        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final String NAME = "name";

        public static final String PATH = "path";

        public static final String[] MATRIX_COLUMNS = new String[]{NAME, PATH};

        public static final String[] COLUMNS = new String[]{_ID, NAME, PATH};
    }

    public static interface DirectMessages extends BaseColumns {

        public static final String TABLE_NAME = "messages";
        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final String ACCOUNT_ID = "account_id";
        public static final String MESSAGE_ID = "message_id";
        public static final String MESSAGE_TIMESTAMP = "message_timestamp";
        public static final String SENDER_ID = "sender_id";
        public static final String RECIPIENT_ID = "recipient_id";

        public static final String IS_OUTGOING = "is_outgoing";

        public static final String TEXT_HTML = "text_html";
        public static final String TEXT_PLAIN = "text_plain";
        public static final String TEXT_UNESCAPED = "text_unescaped";
        public static final String SENDER_NAME = "sender_name";
        public static final String RECIPIENT_NAME = "recipient_name";
        public static final String SENDER_SCREEN_NAME = "sender_screen_name";
        public static final String RECIPIENT_SCREEN_NAME = "recipient_screen_name";
        public static final String SENDER_PROFILE_IMAGE_URL = "sender_profile_image_url";
        public static final String RECIPIENT_PROFILE_IMAGE_URL = "recipient_profile_image_url";

        public static final String MEDIA_LIST = "media_list";

        public static final String[] COLUMNS = new String[]{_ID, ACCOUNT_ID, MESSAGE_ID, MESSAGE_TIMESTAMP,
                SENDER_ID, RECIPIENT_ID, IS_OUTGOING, TEXT_HTML, TEXT_PLAIN, TEXT_UNESCAPED, SENDER_NAME,
                RECIPIENT_NAME, SENDER_SCREEN_NAME, RECIPIENT_SCREEN_NAME, SENDER_PROFILE_IMAGE_URL,
                RECIPIENT_PROFILE_IMAGE_URL, MEDIA_LIST};
        public static final String[] TYPES = new String[]{TYPE_PRIMARY_KEY, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT,
                TYPE_INT, TYPE_BOOLEAN, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
                TYPE_TEXT, TYPE_TEXT, TYPE_TEXT};

        public static final String DEFAULT_SORT_ORDER = MESSAGE_ID + " DESC";

        public static interface Conversation extends DirectMessages {

            public static final String DEFAULT_SORT_ORDER = MESSAGE_TIMESTAMP + " ASC";

            public static final String CONTENT_PATH_SEGMENT = "conversation";
            public static final String CONTENT_PATH_SEGMENT_SCREEN_NAME = "conversation_screen_name";

            public static final String CONTENT_PATH = DirectMessages.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;
            public static final String CONTENT_PATH_SCREEN_NAME = DirectMessages.CONTENT_PATH + "/"
                    + CONTENT_PATH_SEGMENT_SCREEN_NAME;

            public static final Uri CONTENT_URI = Uri
                    .withAppendedPath(DirectMessages.CONTENT_URI, CONTENT_PATH_SEGMENT);

            public static final Uri CONTENT_URI_SCREEN_NAME = Uri.withAppendedPath(DirectMessages.CONTENT_URI,
                    CONTENT_PATH_SEGMENT_SCREEN_NAME);
        }

        public static interface ConversationEntries extends BaseColumns {

            public static final String TABLE_NAME = "messages_conversation_entries";

            public static final String CONTENT_PATH_SEGMENT = "conversation_entries";
            public static final String CONTENT_PATH = DirectMessages.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;

            public static final Uri CONTENT_URI = Uri
                    .withAppendedPath(DirectMessages.CONTENT_URI, CONTENT_PATH_SEGMENT);

            public static final String MESSAGE_ID = DirectMessages.MESSAGE_ID;
            public static final String ACCOUNT_ID = DirectMessages.ACCOUNT_ID;
            public static final String IS_OUTGOING = DirectMessages.IS_OUTGOING;
            public static final String MESSAGE_TIMESTAMP = DirectMessages.MESSAGE_TIMESTAMP;
            public static final String NAME = "name";
            public static final String SCREEN_NAME = "screen_name";
            public static final String PROFILE_IMAGE_URL = "profile_image_url";
            public static final String TEXT_HTML = DirectMessages.TEXT_HTML;
            public static final String CONVERSATION_ID = "conversation_id";

            public static final int IDX__ID = 0;
            public static final int IDX_MESSAGE_TIMESTAMP = 1;
            public static final int IDX_MESSAGE_ID = 2;
            public static final int IDX_ACCOUNT_ID = 3;
            public static final int IDX_IS_OUTGOING = 4;
            public static final int IDX_NAME = 5;
            public static final int IDX_SCREEN_NAME = 6;
            public static final int IDX_PROFILE_IMAGE_URL = 7;
            public static final int IDX_TEXT = 8;
            public static final int IDX_CONVERSATION_ID = 9;
        }

        public static interface Inbox extends DirectMessages {

            public static final String TABLE_NAME = "messages_inbox";

            public static final String CONTENT_PATH_SEGMENT = "inbox";
            public static final String CONTENT_PATH = DirectMessages.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;

            public static final Uri CONTENT_URI = Uri
                    .withAppendedPath(DirectMessages.CONTENT_URI, CONTENT_PATH_SEGMENT);

        }

        public static interface Outbox extends DirectMessages {

            public static final String TABLE_NAME = "messages_outbox";

            public static final String CONTENT_PATH_SEGMENT = "outbox";
            public static final String CONTENT_PATH = DirectMessages.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;

            public static final Uri CONTENT_URI = Uri
                    .withAppendedPath(DirectMessages.CONTENT_URI, CONTENT_PATH_SEGMENT);

        }

    }

    public static interface SearchHistory extends BaseColumns {

        public static final String TABLE_NAME = "search_history";
        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final String QUERY = "query";

        public static final String RECENT_QUERY = "recent_query";

        public static final String[] COLUMNS = new String[]{_ID, RECENT_QUERY, QUERY};
        public static final String[] TYPES = new String[]{TYPE_PRIMARY_KEY, TYPE_INT, TYPE_TEXT_NOT_NULL_UNIQUE};
        public static final String DEFAULT_SORT_ORDER = RECENT_QUERY + " DESC";
    }

    public static interface DNS extends BaseColumns {
        public static final String TABLE_NAME = "dns";
        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final String HOST = "host";

        public static final String ADDRESS = "address";

        public static final String[] MATRIX_COLUMNS = new String[]{HOST, ADDRESS};

        public static final String[] COLUMNS = new String[]{_ID, HOST, ADDRESS};
    }

    public static interface SavedSearches extends BaseColumns {

        public static final String TABLE_NAME = "saved_searches";

        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final String ACCOUNT_ID = "account_id";
        public static final String SEARCH_ID = "search_id";
        public static final String QUERY = "query";
        public static final String NAME = "name";
        public static final String CREATED_AT = "created_at";

        public static final String[] COLUMNS = new String[]{_ID, ACCOUNT_ID, SEARCH_ID, CREATED_AT,
                QUERY, NAME};
        public static final String[] TYPES = new String[]{TYPE_PRIMARY_KEY, TYPE_INT, TYPE_INT,
                TYPE_INT, TYPE_TEXT, TYPE_TEXT};
        public static final String DEFAULT_SORT_ORDER = CREATED_AT + " DESC";
    }

    public static interface Drafts extends BaseColumns {

        public static final int ACTION_UPDATE_STATUS = 1;
        public static final int ACTION_SEND_DIRECT_MESSAGE = 2;
        public static final int ACTION_CREATE_FRIENDSHIP = 3;

        public static final String TABLE_NAME = "drafts";
        public static final String CONTENT_PATH = TABLE_NAME;
        public static final String CONTENT_PATH_UNSENT = TABLE_NAME + "/unsent";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);
        public static final Uri CONTENT_URI_UNSENT = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH_UNSENT);

        /**
         * Status content.<br>
         * Type: TEXT
         */
        public static final String TEXT = "text";

        /**
         * Account IDs of unsent status.<br>
         * Type: TEXT
         */
        public static final String ACCOUNT_IDS = "account_ids";

        public static final String LOCATION = "location";

        public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";

        public static final String MEDIA = "media";

        public static final String IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";

        public static final String TIMESTAMP = "timestamp";

        public static final String ACTION_TYPE = "action_type";

        public static final String ACTION_EXTRAS = "action_extras";

        public static final String[] COLUMNS = new String[]{_ID, TEXT, ACCOUNT_IDS, LOCATION, MEDIA,
                IN_REPLY_TO_STATUS_ID, IS_POSSIBLY_SENSITIVE, TIMESTAMP, ACTION_TYPE, ACTION_EXTRAS};

        public static final String[] TYPES = new String[]{TYPE_PRIMARY_KEY, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
                TYPE_INT, TYPE_INT, TYPE_BOOLEAN, TYPE_INT, TYPE_INT, TYPE_TEXT};

    }

    public static interface Filters extends BaseColumns {

        public static final String CONTENT_PATH = "filters";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final String VALUE = "value";

        public static final String ENABLE_IN_HOME_TIMELINE = "enable_in_home_timeline";

        public static final String ENABLE_IN_MENTIONS = "enable_in_mentions";

        public static final String ENABLE_FOR_RETWEETS = "enable_for_retweets";

        public static final String[] COLUMNS = new String[]{_ID, VALUE};

        public static final String[] TYPES = new String[]{TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL_UNIQUE};

        public static interface Keywords extends Filters {

            public static final String TABLE_NAME = "filtered_keywords";
            public static final String CONTENT_PATH_SEGMENT = "keywords";
            public static final String CONTENT_PATH = Filters.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Filters.CONTENT_URI, CONTENT_PATH_SEGMENT);
        }

        public static interface Links extends Filters {

            public static final String TABLE_NAME = "filtered_links";
            public static final String CONTENT_PATH_SEGMENT = "links";
            public static final String CONTENT_PATH = Filters.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Filters.CONTENT_URI, CONTENT_PATH_SEGMENT);
        }

        public static interface Sources extends Filters {

            public static final String TABLE_NAME = "filtered_sources";
            public static final String CONTENT_PATH_SEGMENT = "sources";
            public static final String CONTENT_PATH = Filters.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Filters.CONTENT_URI, CONTENT_PATH_SEGMENT);
        }

        public static interface Users extends BaseColumns {

            public static final String TABLE_NAME = "filtered_users";
            public static final String CONTENT_PATH_SEGMENT = "users";
            public static final String CONTENT_PATH = Filters.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;
            public static final Uri CONTENT_URI = Uri.withAppendedPath(Filters.CONTENT_URI, CONTENT_PATH_SEGMENT);

            public static final String USER_ID = "user_id";
            public static final String NAME = "name";
            public static final String SCREEN_NAME = "screen_name";

            public static final String[] COLUMNS = new String[]{_ID, USER_ID, NAME, SCREEN_NAME};

            public static final String[] TYPES = new String[]{TYPE_PRIMARY_KEY, TYPE_INT_UNIQUE, TYPE_TEXT_NOT_NULL,
                    TYPE_TEXT_NOT_NULL};
        }
    }

    public static interface Mentions extends Statuses {

        public static final String TABLE_NAME = "mentions";
        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

    }

    public static interface Notifications extends BaseColumns {

        public static final String TABLE_NAME = "notifications";

        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final String ID = "id";

        public static final String COUNT = "count";

        public static final String[] MATRIX_COLUMNS = new String[]{ID, COUNT};

        public static final String[] COLUMNS = new String[]{_ID, ID, COUNT};
    }

    public static interface Permissions extends BaseColumns {
        public static final String TABLE_NAME = "permissions";
        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final String PERMISSION = "permissions";

        public static final String PACKAGE_NAME = "package_name";

        public static final String[] MATRIX_COLUMNS = new String[]{PACKAGE_NAME, PERMISSION};

        public static final String[] COLUMNS = new String[]{_ID, PACKAGE_NAME, PERMISSION};
    }

    public static interface Preferences extends BaseColumns {
        public static final String TABLE_NAME = "preferences";
        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final int TYPE_INVALID = -1;

        public static final int TYPE_NULL = 0;

        public static final int TYPE_BOOLEAN = 1;

        public static final int TYPE_INTEGER = 2;

        public static final int TYPE_LONG = 3;

        public static final int TYPE_FLOAT = 4;

        public static final int TYPE_STRING = 5;

        public static final String KEY = "key";

        public static final String VALUE = "value";

        public static final String TYPE = "type";

        public static final String[] MATRIX_COLUMNS = new String[]{KEY, VALUE, TYPE};

        public static final String[] COLUMNS = new String[]{_ID, KEY, VALUE, TYPE};
    }

    public static interface Statuses extends BaseColumns {

        public static final String TABLE_NAME = "statuses";
        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);
        /**
         * Account ID of the status.<br>
         * Type: TEXT
         */
        public static final String ACCOUNT_ID = "account_id";

        /**
         * Status content, in HTML. Please note, this is not actually original
         * text.<br>
         * Type: TEXT
         */
        public static final String TEXT_HTML = "text_html";

        /**
         *
         */
        public static final String TEXT_PLAIN = "text_plain";

        public static final String TEXT_UNESCAPED = "text_unescaped";

        /**
         * User name of the status.<br>
         * Type: TEXT
         */
        public static final String USER_NAME = "name";

        /**
         * User's screen name of the status.<br>
         * Type: TEXT
         */
        public static final String USER_SCREEN_NAME = "screen_name";

        /**
         * User's profile image URL of the status.<br>
         * Type: TEXT NOT NULL
         */
        public static final String USER_PROFILE_IMAGE_URL = "profile_image_url";

        /**
         * Unique id of the status.<br>
         * Type: INTEGER UNIQUE(long)
         */
        public static final String STATUS_ID = "status_id";

        /**
         * Retweet count of the status.<br>
         * Type: INTEGER (long)
         */
        public static final String RETWEET_COUNT = "retweet_count";
        public static final String FAVORITE_COUNT = "favorite_count";
        public static final String REPLY_COUNT = "reply_count";
        public static final String DESCENDENT_REPLY_COUNT = "descendent_reply_count";

        /**
         * Set to an non-zero integer if the status is a retweet, set to
         * negative value if the status is retweeted by user.<br>
         * Type: INTEGER
         */
        public static final String IS_RETWEET = "is_retweet";

        /**
         * Set to 1 if the status is a favorite.<br>
         * Type: INTEGER (boolean)
         */
        public static final String IS_FAVORITE = "is_favorite";

        public static final String IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";

        /**
         * Set to 1 if the status is a gap.<br>
         * Type: INTEGER (boolean)
         */
        public static final String IS_GAP = "is_gap";

        public static final String LOCATION = "location";

        /**
         * User's ID of the status.<br>
         * Type: INTEGER (long)
         */
        public static final String USER_ID = "user_id";

        public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";

        public static final String IN_REPLY_TO_USER_ID = "in_reply_to_user_id";

        public static final String IN_REPLY_TO_USER_NAME = "in_reply_to_user_name";

        public static final String IN_REPLY_TO_USER_SCREEN_NAME = "in_reply_to_user_screen_name";

        public static final String SOURCE = "source";

        public static final String IS_PROTECTED = "is_protected";

        public static final String IS_VERIFIED = "is_verified";

        public static final String IS_FOLLOWING = "is_following";

        public static final String RETWEET_ID = "retweet_id";

        public static final String RETWEET_TIMESTAMP = "retweet_timestamp";

        public static final String RETWEETED_BY_USER_ID = "retweeted_by_user_id";

        public static final String RETWEETED_BY_USER_NAME = "retweeted_by_user_name";

        public static final String RETWEETED_BY_USER_SCREEN_NAME = "retweeted_by_user_screen_name";

        public static final String RETWEETED_BY_USER_PROFILE_IMAGE = "retweeted_by_user_profile_image";

        /**
         * Timestamp of the status.<br>
         * Type: INTEGER (long)
         */
        public static final String STATUS_TIMESTAMP = "status_timestamp";

        public static final String MY_RETWEET_ID = "my_retweet_id";

        public static final String MEDIA_LIST = "media_list";

        public static final String MENTIONS_LIST = "mentions_list";

        public static final String CARD = "card";

        public static final String CARD_NAME = "card_type";

        public static final String SORT_ORDER_TIMESTAMP_DESC = STATUS_TIMESTAMP + " DESC";

        public static final String SORT_ORDER_STATUS_ID_DESC = STATUS_ID + " DESC";

        public static final String DEFAULT_SORT_ORDER = SORT_ORDER_TIMESTAMP_DESC;

        public static final String[] COLUMNS = new String[]{_ID, ACCOUNT_ID, STATUS_ID, USER_ID,
                STATUS_TIMESTAMP, TEXT_HTML, TEXT_PLAIN, TEXT_UNESCAPED, USER_NAME, USER_SCREEN_NAME,
                USER_PROFILE_IMAGE_URL, IN_REPLY_TO_STATUS_ID, IN_REPLY_TO_USER_ID, IN_REPLY_TO_USER_NAME,
                IN_REPLY_TO_USER_SCREEN_NAME, SOURCE, LOCATION, RETWEET_COUNT, FAVORITE_COUNT, REPLY_COUNT,
                DESCENDENT_REPLY_COUNT, RETWEET_ID, RETWEET_TIMESTAMP, RETWEETED_BY_USER_ID,
                RETWEETED_BY_USER_NAME, RETWEETED_BY_USER_SCREEN_NAME, RETWEETED_BY_USER_PROFILE_IMAGE,
                MY_RETWEET_ID, IS_RETWEET, IS_FAVORITE, IS_PROTECTED, IS_VERIFIED, IS_FOLLOWING, IS_GAP,
                IS_POSSIBLY_SENSITIVE, MEDIA_LIST, MENTIONS_LIST, CARD_NAME, CARD};

        public static final String[] TYPES = new String[]{TYPE_PRIMARY_KEY, TYPE_INT, TYPE_INT,
                TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
                TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_INT, TYPE_INT,
                TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT,
                TYPE_TEXT, TYPE_INT, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN,
                TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT};

    }

    public static interface Tabs extends BaseColumns {
        public static final String TABLE_NAME = "tabs";
        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final String NAME = "name";

        public static final String ICON = "icon";

        public static final String TYPE = "type";

        public static final String ARGUMENTS = "arguments";

        public static final String EXTRAS = "extras";

        public static final String POSITION = "position";

        public static final String[] COLUMNS = new String[]{_ID, NAME, ICON, TYPE, ARGUMENTS, EXTRAS,
                POSITION};

        public static final String[] TYPES = new String[]{TYPE_PRIMARY_KEY, TYPE_TEXT, TYPE_TEXT,
                TYPE_TEXT_NOT_NULL, TYPE_TEXT, TYPE_TEXT, TYPE_INT};

        public static final String DEFAULT_SORT_ORDER = POSITION + " ASC";
    }

    public static interface CachedRelationships extends BaseColumns {

        public static final String TABLE_NAME = "cached_relationships";
        public static final String CONTENT_PATH = TABLE_NAME;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final String ACCOUNT_ID = "account_id";

        public static final String USER_ID = "user_id";

        public static final String FOLLOWING = "following";

        public static final String FOLLOWED_BY = "followed_by";

        public static final String BLOCKING = "blocking";

        public static final String BLOCKED_BY = "blocked_by";

        public static final String MUTING = "muting";

        public static final String[] COLUMNS = {_ID, ACCOUNT_ID, USER_ID, FOLLOWING, FOLLOWED_BY, BLOCKING,
                BLOCKED_BY, MUTING};

        public static final String[] TYPES = {TYPE_PRIMARY_KEY, TYPE_INT, TYPE_INT, TYPE_BOOLEAN_DEFAULT_FALSE,
                TYPE_BOOLEAN_DEFAULT_FALSE, TYPE_BOOLEAN_DEFAULT_FALSE, TYPE_BOOLEAN_DEFAULT_FALSE,
                TYPE_BOOLEAN_DEFAULT_FALSE};
    }

    public static interface UnreadCounts extends BaseColumns {

        public static final String CONTENT_PATH = "unread_counts";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, CONTENT_PATH);

        public static final String TAB_POSITION = "tab_position";

        public static final String TAB_TYPE = "tab_type";

        public static final String COUNT = "count";

        public static final String[] MATRIX_COLUMNS = new String[]{TAB_POSITION, TAB_TYPE, COUNT};

        public static final String[] COLUMNS = new String[]{_ID, TAB_POSITION, TAB_TYPE, COUNT};

        public static interface ByType extends UnreadCounts {

            public static final String CONTENT_PATH_SEGMENT = "by_type";

            public static final String CONTENT_PATH = UnreadCounts.CONTENT_PATH + "/" + CONTENT_PATH_SEGMENT;

            public static final Uri CONTENT_URI = Uri.withAppendedPath(UnreadCounts.CONTENT_URI, CONTENT_PATH_SEGMENT);
        }
    }
}
