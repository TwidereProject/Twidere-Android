package com.meizu.flyme.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Proxy {  
    
	/**
	 * 获取方法
	 * @param method
	 * @param clazz
	 * @param name
	 * @param parameterTypes
	 * @return method
	 */
	protected static Method getMethod (Method method, Class<?> clazz, String name, Class<?>... parameterTypes) {
		if (method == null) {
			try {
				method = clazz.getMethod(name, parameterTypes);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return method;
	}
	
	/**
	 * 执行方法
	 * @param method 方法
	 * @param obj 对像
	 * @param args 参数
	 * @return boolean 执行结果
	 */
	protected static boolean invoke (Method method, Object obj, Object... args) {
		if (method != null) {
			try {
				method.invoke(obj, args);
				return true;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
}
