package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import org.mariotaku.twidere.Constants;

public class HotKeyHandler implements Constants {

	private final Context mContext;

	public HotKeyHandler(final Context context) {
		mContext = context;
	}

	public boolean handleKey(final int keyCode, final KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_N: {
				mContext.startActivity(new Intent(INTENT_ACTION_COMPOSE));
				return true;
			}
		}
		return false;
	}

}
