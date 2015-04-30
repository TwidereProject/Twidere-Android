/*
 * 版本：1.0
 * 日期：2014-10-16
 * Copyright (C) 2010 中国广东省珠海市魅族科技有限公司版权所有
 * 修改历史记录：
 * 2014-10-16    初始版本创建
 */
package com.meizu.flyme.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;


/**
 * <p>用以调用Flyme定制的API</p>
 *
 * @author MEIZU.SDK Team
 *
 */
public class ActionBarProxy extends Proxy{
	
	private static Class<?> sClass = ActionBar.class;
	private static Method sSetBackButtonDrawableMethod;
	private static Method sSetActionModeHeaderHiddenMethod;
	private static Method sSetActionBarViewCollapsableMethod;
	private static Method sSetOverFlowButtonDrawableMethod;
	private static Method sSetTabsShowAtBottom;
	
	   
	/**
	 * 判断设备是否支持smart bar
	 * @return boolean true支持,false不支持
	 */
	public static boolean hasSmartBar() {
		try {
			Method method = Class.forName("android.os.Build").getMethod(
					"hasSmartBar");
			return ((Boolean) method.invoke(null)).booleanValue();
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 设置返回按钮图标
	 * @param actionbar 相应的ActionBar参数
	 * @param backIcon 返回按键的Icon
	 * @return boolean 执行结果
	 */
	public static boolean SetBackButtonDrawable(android.app.ActionBar actionbar,
			Drawable backIcon) {
		sSetBackButtonDrawableMethod  = getMethod(sSetBackButtonDrawableMethod, sClass, "setBackButtonDrawable", new Class[] { Drawable.class });
		return invoke(sSetBackButtonDrawableMethod, actionbar, backIcon);
	}

	/**
	 * 设置more按钮图标
	 * @return boolean 执行结果
	 */
	public static boolean SetOverFlowButtonDrawable(android.app.ActionBar actionbar,
			Drawable drawable) {
		sSetOverFlowButtonDrawableMethod  = getMethod(sSetOverFlowButtonDrawableMethod, sClass, "setOverFlowButtonDrawable", new Class[] { Drawable.class });
		return invoke(sSetOverFlowButtonDrawableMethod, actionbar, drawable);
	}
	

	/**
	 * 设置ActionMode顶栏是否隐藏。
	 * @param bar 对应的ActionBar
	 * @param hide为true表示隐藏
	 * @return boolean 执行结果
	 */
	public static boolean setActionModeHeaderHidden(ActionBar bar, boolean hide) {
		sSetActionModeHeaderHiddenMethod = getMethod(sSetActionModeHeaderHiddenMethod, sClass, "setActionModeHeaderHidden", boolean.class);
		return invoke(sSetActionModeHeaderHiddenMethod, bar, hide);
	}	
	

	/**
	 * 设置ActionBar顶栏无显示内容时是否隐藏。
	 * @param bar
	 * @param collapsable
	 * @return boolen执行结果
	 */
	public static boolean setActionBarViewCollapsable(ActionBar bar, boolean collapsable) {
		sSetActionBarViewCollapsableMethod = getMethod(sSetActionBarViewCollapsableMethod, sClass, "setActionBarViewCollapsable", boolean.class);
		return invoke(sSetActionBarViewCollapsableMethod, bar, collapsable);
	}
	
	/**
	 * <p>
	 * 设置ActionBar Tabs显示在底栏，不过需要配合
	 * android:uiOptions="splitActionBarWhenNarrow"
	 * <p>
	 * @param actionbar
	 * @param showAtBottom
	 * @return boolen 执行结果
	 */
	public static boolean setActionBarTabsShowAtBottom(
			android.app.ActionBar actionbar, boolean showAtBottom) {
		sSetTabsShowAtBottom = getMethod(sSetTabsShowAtBottom, sClass, "setTabsShowAtBottom", boolean.class);
		return invoke(sSetTabsShowAtBottom, actionbar, showAtBottom);
	}


	/**
	 * 获取actionbar高度 
	 * @param context 上下文
	 * @param actionbar 对应的ActionBar
	 * @return int ActionBar的高度值
	 */
	public static int getActionBarHeight(Context context, ActionBar actionbar) {
		if(actionbar != null){
			TypedValue tv = new TypedValue();
			if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize,
					tv, true)) {
				return TypedValue.complexToDimensionPixelSize(tv.data, context
						.getResources().getDisplayMetrics());
			}
			return actionbar.getHeight();
		}
		return 0;
	}
	
    /**
     * 获取smartbar高度
     * @param context
     * @param actionbar
     * @return int SmartBar的高度值
     */
    public static int getSmartBarHeight(Context context,ActionBar actionbar) {
    	if(actionbar != null){
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object obj = c.newInstance();
                Field field = c.getField("mz_action_button_min_height");
                int height = Integer.parseInt(field.get(obj).toString());
                return context.getResources().getDimensionPixelSize(height);
            } catch (Exception e) {
            	e.printStackTrace();
            }
            actionbar.getHeight();
    	}
        return 0;
    }
}
