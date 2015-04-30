package com.meizu.flyme.reflect;

import java.lang.reflect.Method;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

/**
 * @author MEIZU.SDK Team
 *
 */
public class InputMethodProxy extends Proxy {

	private final static String TAG = "InputMethod";
	private static Class<?> sClass = InputMethodManager.class;
	private static Method sSetMzInputThemeLight;


	/**
	 * 设置导航栏和输入法背景颜色，在App启动第一个Actiity onCreate方法中调用该方法，执行成功后，App中使用系统输入法都是白色样式
	 * @param context 上下文
	 * @param light 是否把导航栏和输入法背景设置为白色    
	 * @return boolean 执行结果，成功执行返回true
	 */
	public static boolean setInputThemeLight(Context context, boolean light) {
		sSetMzInputThemeLight = getMethod(sSetMzInputThemeLight, sClass,
				"setMzInputThemeLight", boolean.class);
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			return invoke(sSetMzInputThemeLight, imm, light);
		}
		return false;
	}
}
