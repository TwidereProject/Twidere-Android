package com.meizu.flyme.reflect;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;
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
			} catch (Exception e) {
				Log.e(TAG, "setStatusBarDarkIcon: failed");
			}
		}
		return result;
	}

	/**
	 * 设置沉浸式窗口，设置成功后，状态栏则透明显示
	 * @param window 需要设置的窗口
	 * @param immersive 是否把窗口设置为沉浸
	 * @return boolean 成功执行返回true
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static boolean setImmersedWindow(Window window, boolean immersive) {
		boolean result = false;
		if (window != null) {
			WindowManager.LayoutParams lp = window.getAttributes();
			int trans_status = 0;
			Field flags;
			if (android.os.Build.VERSION.SDK_INT < 19) {
				try {
					trans_status = 1 << 6;
					flags = lp.getClass().getDeclaredField("meizuFlags");
					flags.setAccessible(true);
					int value = flags.getInt(lp);
					if (immersive) {
						value = value | trans_status;
					} else {
						value = value & ~trans_status;
					}
					flags.setInt(lp, value);
					result = true;
				} catch (Exception e) {
					Log.e(TAG, "setImmersedWindow: failed");
				}
			} else {
				lp.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
				window.setAttributes(lp);
				result = true;
			}
		}
		return result;
	}

	/**
	 * 获取状态栏高度
	 * @param context 上下文
	 * @return int 状态栏高度
	 */
	public static int getStatusBarHeight(Context context) {
		try {
			Class<?> c = Class.forName("com.android.internal.R$dimen");
			Object obj = c.newInstance();
			Field field = c.getField("status_bar_height");
			int height = Integer.parseInt(field.get(obj).toString());
			return context.getResources().getDimensionPixelSize(height);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 75;
	}

}
