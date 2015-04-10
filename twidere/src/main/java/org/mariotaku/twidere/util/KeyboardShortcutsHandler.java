package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.ComposeActivity;
import org.mariotaku.twidere.activity.support.QuickSearchBarActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class KeyboardShortcutsHandler implements Constants {

    private static final HashMap<String, Integer> sActionLabelMap = new HashMap<>();

    static {
        sActionLabelMap.put("compose", R.string.compose);
        sActionLabelMap.put("search", R.string.search);
    }

    private static final String KEYCODE_STRING_PREFIX = "KEYCODE_";
    private final Context mContext;
    private final SharedPreferencesWrapper mPreferences;

    public KeyboardShortcutsHandler(final Context context) {
        mContext = context;
        mPreferences = SharedPreferencesWrapper.getInstance(context, KEYBOARD_SHORTCUTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static String getActionLabel(Context context, String action) {
        if (!sActionLabelMap.containsKey(action)) return null;
        final int labelRes = sActionLabelMap.get(action);
        return context.getString(labelRes);
    }

    public static Set<String> getActions() {
        return sActionLabelMap.keySet();
    }

    public static String getKeyEventKey(String contextTag, int keyCode, KeyEvent event) {
        final StringBuilder keyNameBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(contextTag)) {
            keyNameBuilder.append(contextTag);
            keyNameBuilder.append("_");
        }
        if (event.isCtrlPressed()) {
            keyNameBuilder.append("ctrl_");
        }
        if (event.isAltPressed()) {
            keyNameBuilder.append("alt_");
        }
        if (event.isShiftPressed()) {
            keyNameBuilder.append("shift_");
        }
        final String keyCodeString = KeyEvent.keyCodeToString(keyCode);
        if (keyCodeString.startsWith(KEYCODE_STRING_PREFIX)) {
            keyNameBuilder.append(keyCodeString.substring(KEYCODE_STRING_PREFIX.length()).toLowerCase(Locale.US));
        }
        return keyNameBuilder.toString();
    }

    public boolean handleKey(final String contextTag, final int keyCode, final KeyEvent event) {
        if (!isValidForHotkey(keyCode, event)) return false;
        final String key = getKeyEventKey(contextTag, keyCode, event);
        final String action = mPreferences.getString(key, null);
        if (action == null) return false;
        switch (action) {
            case "compose": {
                mContext.startActivity(new Intent(mContext, ComposeActivity.class).setAction(INTENT_ACTION_COMPOSE));
                return true;
            }
            case "search": {
                mContext.startActivity(new Intent(mContext, QuickSearchBarActivity.class).setAction(INTENT_ACTION_QUICK_SEARCH));
                return true;
            }
        }
        return false;
    }

    public static boolean isValidForHotkey(int keyCode, KeyEvent event) {
        return !event.isSystem() && !KeyEvent.isModifierKey(keyCode) && keyCode != KeyEvent.KEYCODE_UNKNOWN;
    }

}
