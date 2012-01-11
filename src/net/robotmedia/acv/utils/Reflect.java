/*******************************************************************************
 * Copyright 2009 Robot Media SL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.robotmedia.acv.utils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.view.MotionEvent;

public class Reflect {
	private static int ACTION_MASK_DEFAULT = 1;
	private static Field mMotionEvent_ACTION_MASK = null;
	private static Field mMotionEvent_ACTION_POINTER_UP = null;
	private static Field mMotionEvent_ACTION_POINTER_DOWN = null;
	private static Method mMotionEvent_getX = null;
	private static Method mMotionEvent_getY = null;
	private static Method mMotionEvent_getPointerCount = null;
	
	static {
		initCompatibility();
	};

	private static void initCompatibility() {
		try {
			mMotionEvent_ACTION_MASK = MotionEvent.class.getField("ACTION_MASK");
			mMotionEvent_ACTION_POINTER_UP = MotionEvent.class.getField("ACTION_POINTER_UP");
			mMotionEvent_ACTION_POINTER_DOWN = MotionEvent.class.getField("ACTION_POINTER_DOWN");
			mMotionEvent_getX = MotionEvent.class.getMethod("getX", new Class[] {int.class});
			mMotionEvent_getY = MotionEvent.class.getMethod("getY", new Class[] {int.class});
			mMotionEvent_getPointerCount = MotionEvent.class.getMethod("getPointerCount", new Class[] {});
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		} catch (NoSuchMethodException e) {
		}
	}

	public static int getPointerCount(MotionEvent e) {
		if (mMotionEvent_getPointerCount != null) {
			try {
				return (Integer) mMotionEvent_getPointerCount.invoke(e);
			} catch (Exception ex) {
				return 1;
			}
		} else {
			return 1;
		}
	}
	
	public static float getX(MotionEvent e, int pointerIndex) {
		if (mMotionEvent_getX != null) {
			try {
				return (Float) mMotionEvent_getX.invoke(e, pointerIndex);
			} catch (Exception ex) {
				return e.getX();
			}
		} else {
			return e.getX();
		}
	}
	
	public static float getY(MotionEvent e, int pointerIndex) {
		if (mMotionEvent_getY != null) {
			try {
				return (Float) mMotionEvent_getY.invoke(e, pointerIndex);
			} catch (Exception ex) {
				return e.getY();
			}
		} else {
			return e.getY();
		}
	}
	
	private static int getIntegerField(Field f, int defaultValue) {
		if (f != null) {
			try {
				return (Integer) f.get(null);
			} catch (IllegalArgumentException e) {
				return defaultValue;
			} catch (IllegalAccessException e) {
				return defaultValue;
			}
		} else {
			return defaultValue;
		}
	}
	
	public static int ACTION_MASK() {
		return getIntegerField(mMotionEvent_ACTION_MASK, ACTION_MASK_DEFAULT);
	}
	
	public static int ACTION_POINTER_UP() {
		return getIntegerField(mMotionEvent_ACTION_POINTER_UP, -1);
	}
	
	public static int ACTION_POINTER_DOWN() {
		return getIntegerField(mMotionEvent_ACTION_POINTER_DOWN, -1);
	}
}
