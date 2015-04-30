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
import android.app.Notification;
import android.app.Notification.Builder;


/**
 * <p>用以调用Flyme定制的API</p>
 *
 * @author MEIZU.SDK Team
 *
 */
public class NotificationProxy extends Proxy {

	private static Class<?> sClass = Notification.Builder.class;
	private static Field  sField  = null;
    private static Object sObject = null;
	private static Method sSetProgressBarStype       = null;
	private static Method sSetCircleProgressBarColor = null;
	private static Method ssetCircleProgressRimColor = null;

    /**
     * 设置ProgressBar的类型
     * @param builder 为Notification.Builder类
     * @param isCircle true为圆环形，false为普通直线形
     */
    public static  void setProgressBarStype(Builder builder, boolean isCircle){      
		try{			
			sField  = sClass.getField("mFlymeNotificationBuilder");
            sObject = sField.get(builder);
            sSetProgressBarStype = sField.getType().getDeclaredMethod("setCircleProgressBar", boolean.class);
            
            if(sObject != null){
            	invoke(sSetProgressBarStype, sObject, isCircle);
            }            
		}catch(Exception ignore){
		        ignore.printStackTrace();
		}
    }
    
    /**
     * 设置圆环形ProgressBar活动进度条的颜色
     * @param color 为颜色值
     */
    public static void  setCircleProgressBarColor(int color) {
        try{
        	if(sField != null && sObject != null){
        		sSetCircleProgressBarColor = sField.getType().getDeclaredMethod("setCircleProgressBarColor", int.class);
        		invoke(sSetCircleProgressBarColor, sObject, color);
        	}        	
        }catch(Exception ignore){
            ignore.printStackTrace();
        }
    }

    /**
     * 设置圆环形ProgressBar外边环的颜色
     * @param color 为颜色值
     */
    public static void setCircleProgressRimColor(int color) {
        try{
        	if(sField != null && sObject != null){
        		ssetCircleProgressRimColor = sField.getType().getDeclaredMethod("ssetCircleProgressRimColor", int.class);
        		invoke(ssetCircleProgressRimColor, sObject, color);
        	}       	
        }catch(Exception ignore){
            ignore.printStackTrace();
        }
    }    
}
