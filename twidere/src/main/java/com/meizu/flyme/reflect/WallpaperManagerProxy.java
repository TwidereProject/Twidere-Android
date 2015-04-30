package com.meizu.flyme.reflect;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.lang.reflect.Method;

import android.app.WallpaperManager;
import android.content.Context;

public class WallpaperManagerProxy extends Proxy {
	private final static String TAG = "WallpaperManagerProxy";
	private static Class<?> sClass = WallpaperManager.class;
	private static Method sSetLockWallpaper;

	/**
	 * 从参数所提供的路径，读取图片并设置为锁屏界面
	 * @param context 上下文
	 * @param path 图片的路径
	 * @return boolean 成功执行返回true
	 */
	public static boolean setLockWallpaper(Context context, String path) {
		boolean result = false;
		WallpaperManager wm = WallpaperManager.getInstance(context);
		try {
			InputStream is = new FileInputStream(path);
			sSetLockWallpaper = getMethod(sSetLockWallpaper, sClass,
					"setStreamToLockWallpaper", InputStream.class);
			if (wm != null) {
				result = invoke(sSetLockWallpaper, wm, is);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 从参数所提供的路径，读取图片并设置为Home界面
	 * @param context 上下文
	 * @param path 图片的路径
	 * @return boolean 成功执行返回true
	 */
	public static boolean setHomeWallpaper(Context context, String path) {
		boolean result = false;
		WallpaperManager wm = WallpaperManager.getInstance(context);
		try {
			InputStream is = new FileInputStream(path);
			wm.setStream(is);
			result = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
