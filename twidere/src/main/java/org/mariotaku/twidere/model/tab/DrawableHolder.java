package org.mariotaku.twidere.model.tab;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import org.apache.commons.lang3.text.WordUtils;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.dagger.DependencyHolder;

import java.util.ArrayList;
import java.util.List;

import static org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_I_WANT_MY_STARS_BACK;

/**
 * Created by mariotaku on 2016/11/27.
 */

public abstract class DrawableHolder {


    private String name;

    @Nullable
    public static DrawableHolder builtin(@NonNull String key) {
        switch (key) {
            // Default built-in icons map, don't remove icons below
            case "accounts":
                return Builtin.ACCOUNTS;
            case "hashtag":
                return Builtin.HASHTAG;
            case "heart":
                return Builtin.HEART;
            case "home":
                return Builtin.HOME;
            case "list":
                return Builtin.LIST;
            case "mention":
                return Builtin.MENTION;
            case "notifications":
                return Builtin.NOTIFICATIONS;
            case "gallery":
                return Builtin.GALLERY;
            case "message":
                return Builtin.MESSAGE;
            case "quote":
                return Builtin.QUOTE;
            case "search":
                return Builtin.SEARCH;
            case "staggered":
                return Builtin.STAGGERED;
            case "star":
                return Builtin.STAR;
            case "trends":
                return Builtin.TRENDS;
            case "twidere":
                return Builtin.TWIDERE;
            case "twitter":
                return Builtin.TWITTER;
            case "user":
                return Builtin.USER;
            // End of default built-in icons
            case "favorite":
                return Builtin.FAVORITE;
        }
        return null;
    }

    @NonNull
    public static DrawableHolder resource(int resId) {
        return new Resource(resId);
    }

    @NonNull
    public abstract String getPersistentKey();

    @NonNull
    public abstract Drawable createDrawable(Context context);

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static DrawableHolder parse(String str) {
        DrawableHolder icon = builtin(str);
        if (icon != null) {
            return icon;
        }
        return null;
    }

    public static List<DrawableHolder> builtins() {
        String[] keys = {
                // Default built-in icons map, don't remove icons below
                "accounts",
                "hashtag",
                "heart",
                "home",
                "list",
                "mention",
                "notifications",
                "gallery",
                "message",
                "quote",
                "search",
                "staggered",
                "star",
                "trends",
                "twidere",
                "twitter",
                "user",
                // End of default built-in icons
                "favorite",
        };
        List<DrawableHolder> list = new ArrayList<>();
        for (String key : keys) {
            list.add(builtin(key));
        }
        return list;
    }

    private static class Resource extends DrawableHolder {
        private int resId;

        public Resource(int resId) {
            this.resId = resId;
        }

        @Override
        @NonNull
        public String getPersistentKey() {
            return String.valueOf(resId);
        }

        @NonNull
        @Override
        public Drawable createDrawable(Context context) {
            return ContextCompat.getDrawable(context, resId);
        }
    }

    public static class Builtin extends DrawableHolder {
        public static final DrawableHolder HOME = new Builtin("home", R.drawable.ic_action_home);
        public static final DrawableHolder HEART = new Builtin("heart", R.drawable.ic_action_heart);
        public static final DrawableHolder HASHTAG = new Builtin("hashtag", R.drawable.ic_action_hashtag);
        public static final DrawableHolder ACCOUNTS = new Builtin("accounts", R.drawable.ic_action_accounts);
        public static final DrawableHolder LIST = new Builtin("list", R.drawable.ic_action_list);
        public static final DrawableHolder MENTION = new Builtin("mention", R.drawable.ic_action_at);
        public static final DrawableHolder NOTIFICATIONS = new Builtin("notifications", R.drawable.ic_action_notification);
        public static final DrawableHolder GALLERY = new Builtin("gallery", R.drawable.ic_action_gallery);
        public static final DrawableHolder MESSAGE = new Builtin("message", R.drawable.ic_action_message);
        public static final DrawableHolder QUOTE = new Builtin("quote", R.drawable.ic_action_quote);
        public static final DrawableHolder SEARCH = new Builtin("search", R.drawable.ic_action_search);
        public static final DrawableHolder STAGGERED = new Builtin("staggered", R.drawable.ic_action_view_quilt);
        public static final DrawableHolder STAR = new Builtin("star", R.drawable.ic_action_star);
        public static final DrawableHolder TRENDS = new Builtin("trends", R.drawable.ic_action_trends);
        public static final DrawableHolder TWIDERE = new Builtin("twidere", R.drawable.ic_action_twidere);
        public static final DrawableHolder TWITTER = new Builtin("twitter", R.drawable.ic_action_twitter);
        public static final DrawableHolder USER = new Builtin("user", R.drawable.ic_action_user);
        public static final DrawableHolder FAVORITE = new DrawableHolder() {
            @NonNull
            @Override
            public String getPersistentKey() {
                return "favorite";
            }

            @Nullable
            @Override
            public String getName() {
                return "Favorite/Like";
            }

            @NonNull
            @Override
            public Drawable createDrawable(Context context) {
                if (DependencyHolder.Companion.get(context).preferences.getBoolean(KEY_I_WANT_MY_STARS_BACK)) {
                    return ContextCompat.getDrawable(context, R.drawable.ic_action_star);
                }
                return ContextCompat.getDrawable(context, R.drawable.ic_action_heart);
            }
        };

        private final String key;
        private final int resId;

        private Builtin(String key, int resId) {
            this.key = key;
            this.resId = resId;
            setName(WordUtils.capitalize(key));
        }

        @Override
        @NonNull
        public String getPersistentKey() {
            return key;
        }

        @NonNull
        @Override
        public Drawable createDrawable(Context context) {
            return ContextCompat.getDrawable(context, resId);
        }
    }
}
