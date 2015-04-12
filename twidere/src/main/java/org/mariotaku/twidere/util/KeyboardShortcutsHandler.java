package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;
import android.view.KeyEvent;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.ComposeActivity;
import org.mariotaku.twidere.activity.support.QuickSearchBarActivity;
import org.mariotaku.twidere.app.TwidereApplication;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

public class KeyboardShortcutsHandler implements Constants {

    public String findAction(@NonNull KeyboardShortcutSpec spec) {
        return mPreferences.getString(spec.getRawKey(), null);
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    private static final HashMap<String, Integer> sActionLabelMap = new HashMap<>();
    private static final SparseArrayCompat<String> sMetaNameMap = new SparseArrayCompat<>();

    static {
        sActionLabelMap.put("compose", R.string.compose);
        sActionLabelMap.put("search", R.string.search);
        sActionLabelMap.put("message", R.string.new_direct_message);
        sActionLabelMap.put("status.reply", R.string.reply);
        sActionLabelMap.put("status.retweet", R.string.retweet);
        sActionLabelMap.put("status.favorite", R.string.favorite);
        sActionLabelMap.put("navigation.previous", R.string.previous);
        sActionLabelMap.put("navigation.next", R.string.next);
        sActionLabelMap.put("navigation.refresh", R.string.refresh);

        sMetaNameMap.put(KeyEvent.META_FUNCTION_ON, "fn");
        sMetaNameMap.put(KeyEvent.META_META_ON, "meta");
        sMetaNameMap.put(KeyEvent.META_CTRL_ON, "ctrl");
        sMetaNameMap.put(KeyEvent.META_ALT_ON, "alt");
        sMetaNameMap.put(KeyEvent.META_SHIFT_ON, "shift");
    }

    private static final String KEYCODE_STRING_PREFIX = "KEYCODE_";
    private final Context mContext;
    private final SharedPreferencesWrapper mPreferences;

    public KeyboardShortcutSpec findKey(String action) {
        for (Entry<String, ?> entry : mPreferences.getAll().entrySet()) {
            if (action.equals(entry.getValue())) {
                final KeyboardShortcutSpec spec = new KeyboardShortcutSpec(entry.getKey(), action);
                if (spec.isValid()) return spec;
            }
        }
        return null;
    }

    public static int getKeyEventMeta(String name) {
        for (int i = 0, j = sMetaNameMap.size(); i < j; i++) {
            if (sMetaNameMap.valueAt(i).equalsIgnoreCase(name)) return sMetaNameMap.keyAt(i);
        }
        return 0;
    }

    public KeyboardShortcutsHandler(final TwidereApplication context) {
        mContext = context;
        mPreferences = SharedPreferencesWrapper.getInstance(context, KEYBOARD_SHORTCUTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static String getActionLabel(Context context, String action) {
        if (!sActionLabelMap.containsKey(action)) return null;
        final int labelRes = sActionLabelMap.get(action);
        return context.getString(labelRes);
    }

    public static String metaToFriendlyString(int metaState) {
        final StringBuilder keyNameBuilder = new StringBuilder();
        for (int i = 0, j = sMetaNameMap.size(); i < j; i++) {
            if ((sMetaNameMap.keyAt(i) & metaState) != 0) {
                final String value = sMetaNameMap.valueAt(i);
                keyNameBuilder.append(value.substring(0, 1).toUpperCase(Locale.US));
                keyNameBuilder.append(value.substring(1));
                keyNameBuilder.append("+");
            }
        }
        return keyNameBuilder.toString();
    }

    public static Set<String> getActions() {
        return sActionLabelMap.keySet();
    }

    public static String getKeyEventKey(String contextTag, int keyCode, KeyEvent event) {
        if (!isValidForHotkey(keyCode, event)) return null;
        final StringBuilder keyNameBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(contextTag)) {
            keyNameBuilder.append(contextTag);
            keyNameBuilder.append(".");
        }
        final int metaState = KeyEvent.normalizeMetaState(event.getMetaState());

        for (int i = 0, j = sMetaNameMap.size(); i < j; i++) {
            if ((sMetaNameMap.keyAt(i) & metaState) != 0) {
                keyNameBuilder.append(sMetaNameMap.valueAt(i));
                keyNameBuilder.append("+");
            }
        }
        final String keyCodeString = KeyEvent.keyCodeToString(keyCode);
        if (keyCodeString.startsWith(KEYCODE_STRING_PREFIX)) {
            keyNameBuilder.append(keyCodeString.substring(KEYCODE_STRING_PREFIX.length()).toLowerCase(Locale.US));
        }
        return keyNameBuilder.toString();
    }

    public static String getKeyEventKey(String contextTag, int metaState, String keyName) {
        final StringBuilder keyNameBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(contextTag)) {
            keyNameBuilder.append(contextTag);
            keyNameBuilder.append(".");
        }

        for (int i = 0, j = sMetaNameMap.size(); i < j; i++) {
            if ((sMetaNameMap.keyAt(i) & metaState) != 0) {
                keyNameBuilder.append(sMetaNameMap.valueAt(i));
                keyNameBuilder.append("+");
            }
        }
        keyNameBuilder.append(keyName);
        return keyNameBuilder.toString();
    }

    public static KeyboardShortcutSpec getKeyboardShortcutSpec(String contextTag, int keyCode, KeyEvent event) {
        if (!isValidForHotkey(keyCode, event)) return null;
        final int metaState = KeyEvent.normalizeMetaState(event.getMetaState());
        int metaStateNormalized = 0;
        for (int i = 0, j = sMetaNameMap.size(); i < j; i++) {
            if ((sMetaNameMap.keyAt(i) & metaState) != 0) {
                metaStateNormalized |= sMetaNameMap.keyAt(i);
            }
        }
        final String keyCodeString = KeyEvent.keyCodeToString(keyCode);
        if (keyCodeString.startsWith(KEYCODE_STRING_PREFIX)) {
            final String keyName = keyCodeString.substring(KEYCODE_STRING_PREFIX.length()).toLowerCase(Locale.US);
            return new KeyboardShortcutSpec(contextTag, metaStateNormalized, keyName, null);
        }
        return null;
    }

    public boolean handleKey(final Context context, final String contextTag, final int keyCode, final KeyEvent event) {
        final String action = getKeyAction(contextTag, keyCode, event);
        if (action == null) return false;
        switch (action) {
            case "compose": {
                context.startActivity(new Intent(context, ComposeActivity.class).setAction(INTENT_ACTION_COMPOSE));
                return true;
            }
            case "search": {
                context.startActivity(new Intent(context, QuickSearchBarActivity.class).setAction(INTENT_ACTION_QUICK_SEARCH));
                return true;
            }
            case "message": {
                Utils.openMessageConversation(context, -1, -1);
                return true;
            }
        }
        return false;
    }

    @Nullable
    public String getKeyAction(final String contextTag, final int keyCode, final KeyEvent event) {
        if (!isValidForHotkey(keyCode, event)) return null;
        final String key = getKeyEventKey(contextTag, keyCode, event);
        return mPreferences.getString(key, null);
    }

    public static boolean isValidForHotkey(int keyCode, KeyEvent event) {
        // These keys must use with modifiers
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            case KeyEvent.KEYCODE_TAB: {
                if (event.hasNoModifiers()) return false;
                break;
            }
        }
        return !event.isSystem() && !KeyEvent.isModifierKey(keyCode) && keyCode != KeyEvent.KEYCODE_UNKNOWN;
    }

    public boolean isEmpty() {
        return mPreferences.getAll().isEmpty();
    }

    public void reset() {
        final Editor editor = mPreferences.edit();
        editor.clear();
        editor.putString("n", "compose");
        editor.putString("m", "message");
        editor.putString("slash", "search");
        editor.putString("navigation.period", "navigation.refresh");
        editor.putString("navigation.j", "navigation.next");
        editor.putString("navigation.k", "navigation.previous");
        editor.putString("status.f", "status.favorite");
        editor.putString("status.r", "status.reply");
        editor.putString("status.t", "status.retweet");
        editor.apply();
    }

    public void register(KeyboardShortcutSpec spec, String action) {
        unregister(action);
        mPreferences.edit().putString(spec.getRawKey(), action).apply();
    }

    public void unregister(String action) {
        final Editor editor = mPreferences.edit();
        for (Entry<String, ?> entry : mPreferences.getAll().entrySet()) {
            if (action.equals(entry.getValue())) {
                final KeyboardShortcutSpec spec = new KeyboardShortcutSpec(entry.getKey(), action);
                if (spec.isValid()) {
                    editor.remove(spec.getRawKey());
                }
            }
        }
        editor.apply();
    }

    public static interface ShortcutCallback {
        boolean handleKeyboardShortcutSingle(int keyCode, @NonNull KeyEvent event);

        boolean handleKeyboardShortcutRepeat(int keyCode, int repeatCount, @NonNull KeyEvent event);
    }

    /**
     * Created by mariotaku on 15/4/11.
     */
    public static class KeyboardShortcutSpec {

        private String action;
        private String contextTag;
        private int keyMeta;
        private String keyName;

        public KeyboardShortcutSpec(String contextTag, int keyMeta, String keyName, String action) {
            this.contextTag = contextTag;
            this.keyMeta = keyMeta;
            this.keyName = keyName;
            this.action = action;
        }

        public KeyboardShortcutSpec(String key, String action) {
            final int contextDotIdx = key.indexOf('.');
            if (contextDotIdx != -1) {
                contextTag = key.substring(0, contextDotIdx);
            }
            int idx = contextDotIdx, previousIdx = idx;
            while ((idx = key.indexOf('+', idx + 1)) != -1) {
                keyMeta |= getKeyEventMeta(key.substring(previousIdx + 1, idx));
                previousIdx = idx;
            }
            keyName = key.substring(previousIdx + 1);
            this.action = action;
        }

        public String getContextTag() {
            return contextTag;
        }

        public int getKeyMeta() {
            return keyMeta;
        }

        public String getKeyName() {
            return keyName;
        }

        public String getRawKey() {
            return getKeyEventKey(contextTag, keyMeta, keyName);
        }

        public String getAction() {
            return action;
        }

        public String getValueName(Context context) {
            return getActionLabel(context, action);
        }

        public boolean isValid() {
            return keyName != null;
        }

        public String toKeyString() {
            return metaToFriendlyString(keyMeta) + keyToFriendlyString(keyName);
        }

        private static String keyToFriendlyString(String keyName) {
            if (keyName == null) return null;
            final String upperName = keyName.toUpperCase(Locale.US);
            final int keyCode = KeyEvent.keyCodeFromString(KEYCODE_STRING_PREFIX + upperName);
            if (keyCode == KeyEvent.KEYCODE_UNKNOWN) return upperName;
            return String.valueOf(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode).getDisplayLabel());
        }

        @Override
        public String toString() {
            return "KeyboardShortcutSpec{" +
                    "action='" + action + '\'' +
                    ", contextTag='" + contextTag + '\'' +
                    ", keyMeta=" + keyMeta +
                    ", keyName='" + keyName + '\'' +
                    '}';
        }
    }
}
