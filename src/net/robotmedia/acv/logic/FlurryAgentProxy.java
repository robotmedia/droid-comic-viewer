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
package net.robotmedia.acv.logic;

import java.lang.reflect.Method;
import java.util.Map;

import android.content.Context;

public class FlurryAgentProxy {
	
	private static Method onStartSession = null;
	private static Method onEndSession = null;
	private static Method onEvent = null;
	private static Method onError = null;
	
	static {
		init();
	};

	private static void init() {
		try {
			final Class<?> aClass = Class.forName("com.flurry.android.FlurryAgent");
			onStartSession = aClass.getMethod("onStartSession", new Class[] {Context.class, String.class});
			onEndSession = aClass.getMethod("onEndSession", new Class[] {Context.class});
			onError = aClass.getMethod("onError", new Class[] {Context.class});
			onEvent = aClass.getMethod("onEvent", new Class[] {Context.class});
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (ClassNotFoundException e) {
		}
	}
	
	public static void onStartSession(Context context, String key) {
		if (onStartSession != null) {
			try {
				onStartSession.invoke(null, new Object[] {context, key});
			} catch (Exception e) {
			}
		}
	}

	public static void onEndSession(Context context) {
		if (onEndSession != null) {
			try {
				onEndSession.invoke(null, new Object[] {context});
			} catch (Exception e) {
			}
		}
	}
	
	public static void onEvent(String eventId, Map<String, String> parameters) {
		if (onEvent != null) {
			try {
				onEvent.invoke(null, new Object[] {eventId, parameters});
			} catch (Exception e) {
			}
		}
	}
	
	public static void onError(String errorId, String message, String errorClass) {
		if (onError != null) {
			try {
				onError.invoke(null, new Object[] {errorId, message, errorClass});
			} catch (Exception e) {
			}
		}
	}
	
}
