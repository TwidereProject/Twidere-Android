package com.meizu.flyme.reflect;

import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;

public class StatusBarProxy {
	private final static String TAG = "StatusBar";

	/**
	 * 设置状态栏图标为深色和魅族特定的文字风格
	 * @param window 需要设置的窗口
	 * @param dark 是否把状态栏颜色设置为深色
	 * @return  boolean 成功执行返回true
	 */
	public static boolean setStatusBarDarkIcon(Window window, boolean dark) {
		boolean result = false;
		if (window != null) {
			try {
				WindowManager.LayoutParams lp = window.getAttributes();
				Field darkFlag = WindowManager.LayoutParams.class
						.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
				Field meizuFlags = WindowManager.LayoutParams.class
						.getDeclaredField("meizuFlags");
				darkFlag.setAccessible(true);
				meizuFlags.setAccessible(true);
				int bit = darkFlag.getInt(null);
				int value = meizuFlags.getInt(lp);
				if (dark) {
					value |= bit;
				} else {
					value &= ~bit;
				}
				meizuFlags.setInt(lp, value);
				window.setAttributes(lp);
				result = true;
			} catch (Exception ignored) {
			}
		}
		return result;
	}

}
